package bberp.model;

import java.math.BigDecimal;

public class SalesOrderLineModel {
    private int id;
    private int productId;
    private String productCode;
    private String productName;
    private int quantity;
    private String uom;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal lineTotal = BigDecimal.ZERO;
    private int allocatedQty;
    private int backorderQty;

    public SalesOrderLineModel() {}

    public SalesOrderLineModel(int productId, String code, String name,
                               int qty, String uom, BigDecimal price) {
        this.productId = productId;
        this.productCode = code;
        this.productName = name;
        this.quantity = qty;
        this.uom = uom;
        this.unitPrice = price;
        recalc();
    }

    public void recalc() {
        lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getId()               { return id; }
    public void setId(int v)         { id = v; }
    public int getProductId()        { return productId; }
    public void setProductId(int v)  { productId = v; }
    public String getProductCode()   { return productCode; }
    public void setProductCode(String v) { productCode = v; }
    public String getProductName()   { return productName; }
    public void setProductName(String v) { productName = v; }
    public int getQuantity()         { return quantity; }
    public void setQuantity(int q)   { quantity = q; recalc(); }
    public String getUom()           { return uom; }
    public void setUom(String v)     { uom = v; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal v) { unitPrice = v == null ? BigDecimal.ZERO : v; recalc(); }
    public BigDecimal getLineTotal() { return lineTotal; }
    public int getAllocatedQty()     { return allocatedQty; }
    public void setAllocatedQty(int v) { allocatedQty = v; }
    public int getBackorderQty()     { return backorderQty; }
    public void setBackorderQty(int v) { backorderQty = v; }
}
