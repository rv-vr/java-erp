package bberp.model;

import java.math.BigDecimal;

public class CustomerModel {
    private final int id;
    private final String name;
    private String address;
    private String phone;
    private String email;
    private BigDecimal creditLimit = BigDecimal.ZERO;
    private BigDecimal arBalance = BigDecimal.ZERO;

    public CustomerModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId()               { return id; }
    public String getName()          { return name; }
    public String getAddress()       { return address; }
    public void setAddress(String v) { address = v; }
    public String getPhone()         { return phone; }
    public void setPhone(String v)   { phone = v; }
    public String getEmail()         { return email; }
    public void setEmail(String v)   { email = v; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal v) { creditLimit = v; }
    public BigDecimal getArBalance() { return arBalance; }
    public void setArBalance(BigDecimal v) { arBalance = v; }

    @Override
    public String toString() { return name; }
}
