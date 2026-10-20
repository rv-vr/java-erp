package bberp.model;

import java.math.BigDecimal;

public class ProductModel {
    private final int id;
    private final String code;
    private final String name;
    private final BigDecimal unitPrice;
    private final int stockQty;
    private final String uom;

    public ProductModel(int id, String code, String name,
                        BigDecimal unitPrice, int stockQty, String uom) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        this.stockQty = stockQty;
        this.uom = uom;
    }

    public int getId()               { return id; }
    public String getCode()          { return code; }
    public String getName()          { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getStockQty()         { return stockQty; }
    public String getUom()           { return uom; }

    @Override
    public String toString() {
        return code + " - " + name + " (" + stockQty + " " + uom + " left)";
    }
}
