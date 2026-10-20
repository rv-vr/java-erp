package bberp.service;

import com.zaxxer.hikari.HikariDataSource;
import bberp.common.AppSession;
import bberp.dao.CustomerDao;
import bberp.dao.ProductDao;
import bberp.dao.SalesOrderDao;
import bberp.model.CustomerModel;
import bberp.model.ProductModel;
import bberp.model.SalesOrderLineModel;
import bberp.model.SalesOrderModel;
import java.math.BigDecimal;
import java.util.List;

/**
 * Sales Order workflow and business rules.
 *
 * Draft -> Pending Approval -> Confirmed | Credit Hold -> Closed
 * Credit/inventory checks run on confirmation; stock is committed only then.
 */
public class SalesOrderService {

    /** Outcome of confirming an order, for the UI to report. */
    public static class Result {
        public final SalesOrderModel order;
        public final BigDecimal exposure;
        public final BigDecimal creditLimit;
        public final int backorderTotal;

        Result(SalesOrderModel order, BigDecimal exposure, BigDecimal creditLimit, int backorderTotal) {
            this.order = order;
            this.exposure = exposure;
            this.creditLimit = creditLimit;
            this.backorderTotal = backorderTotal;
        }
    }

    private final SalesOrderDao orderDao;
    private final ProductDao productDao;
    private final CustomerDao customerDao;

    public SalesOrderService(HikariDataSource ds) {
        this.orderDao = new SalesOrderDao(ds);
        this.productDao = new ProductDao(ds);
        this.customerDao = new CustomerDao(ds);
    }

    public String nextOrderNo()                          { return orderDao.nextOrderNo(); }
    public List<SalesOrderModel> list()                  { return orderDao.findAll(); }
    public SalesOrderModel find(int id)                  { return orderDao.findById(id); }

    /** Validates the header and lines, then stores the order as a Draft. No stock is committed. */
    public SalesOrderModel create(SalesOrderModel order) {
        validate(order);
        order.setEmployeeId(AppSession.userId() == 0
                ? order.getEmployeeId() : AppSession.userId());
        order.recalcTotal();
        order.setStatus(SalesOrderModel.DRAFT);
        order.setHoldReason(null);
        if (orderDao.save(order) < 0) {
            throw new IllegalStateException("Database refused the order. See console for details.");
        }
        return order;
    }

    /** Draft -> Pending Approval. */
    public SalesOrderModel submit(int id) {
        SalesOrderModel order = load(id);
        require(order.getStatus(), SalesOrderModel.DRAFT);
        if (order.getLines().isEmpty()) {
            throw new IllegalArgumentException("Add at least one line item before submitting.");
        }
        if (order.getCustomerPoNo() == null || order.getCustomerPoNo().isBlank()) {
            throw new IllegalArgumentException("Customer PO Number is required before submitting.");
        }
        if (!orderDao.updateStatus(id, SalesOrderModel.DRAFT, SalesOrderModel.PENDING_APPROVAL, null)) {
            throw new IllegalStateException("Order changed underneath you. Refresh and try again.");
        }
        order.setStatus(SalesOrderModel.PENDING_APPROVAL);
        return order;
    }

    /**
     * Pending Approval -> Confirmed or Credit Hold, running the credit and inventory
     * checks. From Credit Hold, confirming is a manager override.
     */
    public Result confirm(int id) {
        SalesOrderModel order = load(id);
        boolean override = SalesOrderModel.CREDIT_HOLD.equals(order.getStatus());
        require(order.getStatus(), SalesOrderModel.PENDING_APPROVAL, SalesOrderModel.CREDIT_HOLD);

        CustomerModel customer = customerDao.findById(order.getCustomerId());
        BigDecimal limit = customer == null ? BigDecimal.ZERO : customer.getCreditLimit();
        BigDecimal ar = customer == null ? BigDecimal.ZERO : customer.getArBalance();
        BigDecimal exposure = ar.add(orderDao.openOrderTotal(order.getCustomerId(), id))
                .add(order.getTotalAmount());
        boolean overLimit = limit.signum() > 0 && exposure.compareTo(limit) > 0;

        if (overLimit && !override) {
            for (SalesOrderLineModel line : order.getLines()) {
                line.setAllocatedQty(0);
                line.setBackorderQty(0);
            }
            order.setStatus(SalesOrderModel.CREDIT_HOLD);
            order.setHoldReason(String.format(
                    "Credit limit exceeded: exposure %s vs limit %s", exposure, limit));
            if (!orderDao.confirm(order)) {
                throw new IllegalStateException("Order changed underneath you. Refresh and try again.");
            }
            return new Result(order, exposure, limit, 0);
        }

        int backorderTotal = 0;
        for (SalesOrderLineModel line : order.getLines()) {
            ProductModel product = productDao.findById(line.getProductId());
            int available = product == null ? 0 : Math.max(0, product.getStockQty());
            int allocated = Math.min(line.getQuantity(), available);
            line.setAllocatedQty(allocated);
            line.setBackorderQty(line.getQuantity() - allocated);
            backorderTotal += line.getBackorderQty();
        }

        order.setStatus(SalesOrderModel.CONFIRMED);
        order.setHoldReason(override
                ? String.format("Credit override approved (exposure %s vs limit %s)", exposure, limit)
                : null);
        if (!orderDao.confirm(order)) {
            throw new IllegalStateException("Order changed underneath you. Refresh and try again.");
        }
        return new Result(order, exposure, limit, backorderTotal);
    }

    /** Confirmed -> Closed. Terminal; the order can no longer change. */
    public void close(int id) {
        SalesOrderModel order = load(id);
        require(order.getStatus(), SalesOrderModel.CONFIRMED);
        if (!orderDao.updateStatus(id, SalesOrderModel.CONFIRMED, SalesOrderModel.CLOSED,
                order.getHoldReason())) {
            throw new IllegalStateException("Order changed underneath you. Refresh and try again.");
        }
    }

    /** Deletes an order that has not been confirmed; confirmed/closed orders are kept for audit. */
    public void delete(int id) {
        SalesOrderModel order = load(id);
        if (SalesOrderModel.CONFIRMED.equals(order.getStatus())
                || SalesOrderModel.CLOSED.equals(order.getStatus())) {
            throw new IllegalArgumentException(
                    "A " + order.getStatus() + " order cannot be deleted. Close it instead.");
        }
        if (!orderDao.delete(id, order.getStatus())) {
            throw new IllegalStateException("Order changed underneath you. Refresh and try again.");
        }
    }

    private void validate(SalesOrderModel order) {
        if (order.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Pick a customer.");
        }
        if (order.getCustomerPoNo() == null || order.getCustomerPoNo().isBlank()) {
            throw new IllegalArgumentException("Customer PO Number is required for the audit trail.");
        }
        if (order.getLines().isEmpty()) {
            throw new IllegalArgumentException("Add at least one line item.");
        }
        for (SalesOrderLineModel line : order.getLines()) {
            if (line.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for " + line.getProductCode() + ".");
            }
        }
    }

    private SalesOrderModel load(int id) {
        SalesOrderModel order = orderDao.findById(id);
        if (order == null) {
            throw new IllegalArgumentException("Order not found.");
        }
        return order;
    }

    private static void require(String actual, String... allowed) {
        for (String a : allowed) {
            if (a.equals(actual)) return;
        }
        throw new IllegalArgumentException("This action is not allowed from status " + actual + ".");
    }
}
