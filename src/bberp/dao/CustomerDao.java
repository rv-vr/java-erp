package bberp.dao;

import com.zaxxer.hikari.HikariDataSource;
import bberp.model.CustomerModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {
    private final HikariDataSource ds;

    public CustomerDao(HikariDataSource ds) { this.ds = ds; }

    public List<CustomerModel> findAll() {
        String sql = "SELECT ID, CustomerName, Address, Phone, Email, CreditLimit, CurrentARBalance " +
                     "FROM tblcustomer ORDER BY CustomerName";
        List<CustomerModel> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CustomerModel m = new CustomerModel(rs.getInt("ID"), rs.getString("CustomerName"));
                m.setAddress(rs.getString("Address"));
                m.setPhone(rs.getString("Phone"));
                m.setEmail(rs.getString("Email"));
                m.setCreditLimit(rs.getBigDecimal("CreditLimit"));
                m.setArBalance(rs.getBigDecimal("CurrentARBalance"));
                out.add(m);
            }
        } catch (SQLException e) {
            System.err.println("CustomerDao.findAll: " + e.getMessage());
        }
        return out;
    }
}
