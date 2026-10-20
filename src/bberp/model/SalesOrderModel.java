package bberp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderModel {
    public static final String DRAFT = "Draft";
    public static final String CREDIT_HOLD = "Credit Hold";
    public static final String CONFIRMED = "Confirmed";
    public static final String CLOSED = "Closed";

    private int id;
    private String orderNo;
    private LocalDate orderDate = LocalDate.now();
    private int customerId;
    private String customerName;
    private int employeeId;
    private String status = DRAFT;
    private String remarks;
    private String customerPoNo;
    private String sqRef;
    private String paymentTerms = "Net 30";
    private String shipToAddress;
    private LocalDate requestedDeliveryDate;
    private String deliveryInstructions;
    private String holdReason;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    // Customer credit snapshot, supplied by the UI from the selected customer.
    private BigDecimal customerCreditLimit = BigDecimal.ZERO;
    private BigDecimal customerArBalance = BigDecimal.ZERO;
    private final List<SalesOrderLineModel> lines = new ArrayList<>();

    public void addLine(SalesOrderLineModel line) {
        lines.add(line);
        recalcTotal();
    }

    public void recalcTotal() {
        totalAmount = lines.stream()
                .map(SalesOrderLineModel::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getId()                            { return id; }
    public void setId(int v)                      { id = v; }
    public String getOrderNo()                    { return orderNo; }
    public void setOrderNo(String v)              { orderNo = v; }
    public LocalDate getOrderDate()               { return orderDate; }
    public void setOrderDate(LocalDate v)         { orderDate = v; }
    public int getCustomerId()                    { return customerId; }
    public void setCustomerId(int v)              { customerId = v; }
    public String getCustomerName()               { return customerName; }
    public void setCustomerName(String v)         { customerName = v; }
    public int getEmployeeId()                    { return employeeId; }
    public void setEmployeeId(int v)              { employeeId = v; }
    public String getStatus()                     { return status; }
    public void setStatus(String v)               { status = v; }
    public String getRemarks()                    { return remarks; }
    public void setRemarks(String v)              { remarks = v; }
    public String getCustomerPoNo()               { return customerPoNo; }
    public void setCustomerPoNo(String v)         { customerPoNo = v; }
    public String getSqRef()                      { return sqRef; }
    public void setSqRef(String v)                { sqRef = v; }
    public String getPaymentTerms()               { return paymentTerms; }
    public void setPaymentTerms(String v)         { paymentTerms = v; }
    public String getShipToAddress()              { return shipToAddress; }
    public void setShipToAddress(String v)        { shipToAddress = v; }
    public LocalDate getRequestedDeliveryDate()   { return requestedDeliveryDate; }
    public void setRequestedDeliveryDate(LocalDate v) { requestedDeliveryDate = v; }
    public String getDeliveryInstructions()       { return deliveryInstructions; }
    public void setDeliveryInstructions(String v) { deliveryInstructions = v; }
    public String getHoldReason()                 { return holdReason; }
    public void setHoldReason(String v)           { holdReason = v; }
    public BigDecimal getTotalAmount()            { return totalAmount; }
    public void setTotalAmount(BigDecimal v)      { totalAmount = v == null ? BigDecimal.ZERO : v; }
    public BigDecimal getCustomerCreditLimit()    { return customerCreditLimit; }
    public void setCustomerCreditLimit(BigDecimal v) { customerCreditLimit = v == null ? BigDecimal.ZERO : v; }
    public BigDecimal getCustomerArBalance()      { return customerArBalance; }
    public void setCustomerArBalance(BigDecimal v) { customerArBalance = v == null ? BigDecimal.ZERO : v; }
    public List<SalesOrderLineModel> getLines()   { return lines; }
}
