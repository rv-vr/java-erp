package bberp.dao;

import com.zaxxer.hikari.HikariDataSource;
import bberp.model.EmployeeModel;
import java.sql.*;

public class EmployeeDao {
    private final HikariDataSource ds;

    public EmployeeDao(HikariDataSource ds) { this.ds = ds; }

    /** Returns the matching employee, or null if the username/password is wrong. */
    public EmployeeModel findByCredentials(String username, String password) {
        String sql = "SELECT ID, EmployeeName, Username FROM tblemployee WHERE Username = ? AND Password = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new EmployeeModel(rs.getInt("ID"), rs.getString("EmployeeName"), rs.getString("Username"));
            }
        } catch (SQLException e) {
            System.err.println("EmployeeDao.findByCredentials: " + e.getMessage());
            return null;
        }
    }
}
