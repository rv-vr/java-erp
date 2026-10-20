package bberp.dao;

import com.zaxxer.hikari.HikariDataSource;
import bberp.model.ProductModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    private final HikariDataSource ds;

    public ProductDao(HikariDataSource ds) { this.ds = ds; }

    public List<ProductModel> findAll() {
        String sql = "SELECT ID, ProductCode, ProductName, UnitPrice, StockQty, UOM " +
                     "FROM tblproduct ORDER BY ProductName";
        List<ProductModel> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            System.err.println("ProductDao.findAll: " + e.getMessage());
        }
        return out;
    }

    /** Fresh stock level for one product, used for the allocation check. */
    public ProductModel findById(int id) {
        String sql = "SELECT ID, ProductCode, ProductName, UnitPrice, StockQty, UOM FROM tblproduct WHERE ID = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("ProductDao.findById: " + e.getMessage());
            return null;
        }
    }

    /** Decrements stock only when enough is available; returns rows changed. */
    public int reduceStock(Connection conn, int productId, int qty) throws SQLException {
        String sql = "UPDATE tblproduct SET StockQty = StockQty - ? WHERE ID = ? AND StockQty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            return ps.executeUpdate();
        }
    }

    private ProductModel map(ResultSet rs) throws SQLException {
        return new ProductModel(
                rs.getInt("ID"),
                rs.getString("ProductCode"),
                rs.getString("ProductName"),
                rs.getBigDecimal("UnitPrice"),
                rs.getInt("StockQty"),
                rs.getString("UOM"));
    }
}
