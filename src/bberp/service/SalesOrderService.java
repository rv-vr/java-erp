package bberp.service;

import com.zaxxer.hikari.HikariDataSource;
import bberp.common.AppSession;
import bberp.dao.ProductDao;
import bberp.dao.SalesOrderDao;
import bberp.model.ProductModel;
import bberp.model.SalesOrderLineModel;
import bberp.model.SalesOrderModel;
import java.math.BigDecimal;
import java.util.List;

/**
 * Sales Order business rules: credit check against exposure, stock allocation
 * with backorder, and confirmation only when both pass.
 */
public class SalesOrderService {

    /** Outcome of creating an order, for the UI to report. */
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

    public SalesOrderService(HikariDataSource ds) {
        this.orderDao = new SalesOrderDao(ds);
        this.productDao = new ProductDao(ds);
    }

    public String nextOrderNo()                          { return orderDao.nextOrderNo(); }
    public List<SalesOrderModel> list()                  { return orderDao.findAll(); }
    public SalesOrderModel find(int id)                  { return orderDao.findById(id); }

    /**
     * Validates, runs the credit and inventory checks, allocates stock and saves.
     * A credit failure still stores the order, as Credit Hold with no stock committed.
     */
    public Result create(SalesOrderModel order) {
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

        order.setEmployeeId(AppSession.userId() == 0
                ? order.getEmployeeId() : AppSession.userId());
        order.recalcTotal();

        BigDecimal exposure = order.getCustomerArBalance().add(orderDao.openOrderTotal(order.getCustomerId()))
                .add(order.getTotalAmount());
        boolean overLimit = order.getCustomerCreditLimit().signum() > 0
                && exposure.compareTo(order.getCustomerCreditLimit()) > 0;

        int backorderTotal = 0;
        for (SalesOrderLineModel line : order.getLines()) {
            ProductModel product = productDao.findById(line.getProductId());
            int available = product == null ? 0 : Math.max(0, product.getStockQty());
            int allocated = Math.min(line.getQuantity(), available);
            line.setAllocatedQty(allocated);
            line.setBackorderQty(line.getQuantity() - allocated);
            backorderTotal += line.getBackorderQty();
        }

        if (overLimit) {
            order.setStatus(SalesOrderModel.CREDIT_HOLD);
            order.setHoldReason(String.format(
                    "Credit limit exceeded: exposure %.2f vs limit %.2f", exposure, order.getCustomerCreditLimit()));
        } else {
            order.setStatus(SalesOrderModel.CONFIRMED);
            order.setHoldReason(null);
        }

        if (orderDao.save(order) < 0) {
            throw new IllegalStateException("Database refused the order. See console for details.");
        }
        return new Result(order, exposure, order.getCustomerCreditLimit(), backorderTotal);
    }
}
