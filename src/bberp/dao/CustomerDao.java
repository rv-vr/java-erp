package bberp.dao;

import com.zaxxer.hikari.HikariDataSource;
import bberp.model.CustomerModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {
    private static final String SELECT =
            "SELECT ID, CustomerName, Address, Phone, Email, CreditLimit, CurrentARBalance FROM tblcustomer ";

    private final HikariDataSource ds;

    public CustomerDao(HikariDataSource ds) { this.ds = ds; }

    public List<CustomerModel> findAll() {
        List<CustomerModel> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY CustomerName");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            System.err.println("CustomerDao.findAll: " + e.getMessage());
        }
        return out;
    }

    /** Customer row including the credit limit and AR balance used by the credit check. */
    public CustomerModel findById(int id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE ID = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("CustomerDao.findById: " + e.getMessage());
            return null;
        }
    }

    private static CustomerModel map(ResultSet rs) throws SQLException {
        CustomerModel m = new CustomerModel(rs.getInt("ID"), rs.getString("CustomerName"));
        m.setAddress(rs.getString("Address"));
        m.setPhone(rs.getString("Phone"));
        m.setEmail(rs.getString("Email"));
        m.setCreditLimit(rs.getBigDecimal("CreditLimit"));
        m.setArBalance(rs.getBigDecimal("CurrentARBalance"));
        return m;
    }
}
