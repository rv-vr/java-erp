package bberp.dao;

import com.zaxxer.hikari.HikariDataSource;
import bberp.model.SalesOrderLineModel;
import bberp.model.SalesOrderModel;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDao {
    private final HikariDataSource ds;
    private final ProductDao productDao;

    public SalesOrderDao(HikariDataSource ds) {
        this.ds = ds;
        this.productDao = new ProductDao(ds);
    }

    /** Persists a draft header + all lines in one transaction. Returns the new ID, or -1. */
    public int save(SalesOrderModel order) {
        final String headerSql =
            "INSERT INTO tblsalesorder " +
            "(OrderNo, OrderDate, CustomerID, EmployeeID, Status, TotalAmount, Remarks, " +
            " CustomerPONo, SQRef, PaymentTerms, ShipToAddress, RequestedDeliveryDate, " +
            " DeliveryInstructions, HoldReason) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        final String lineSql =
            "INSERT INTO tblsalesorderline " +
            "(SalesOrderID, ProductID, Quantity, UnitPrice, LineTotal, AllocatedQty, BackorderQty) " +
            "VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNo());
                ps.setDate(2, Date.valueOf(order.getOrderDate()));
                ps.setInt(3, order.getCustomerId());
                ps.setInt(4, order.getEmployeeId());
                ps.setString(5, order.getStatus());
                ps.setBigDecimal(6, order.getTotalAmount());
                ps.setString(7, order.getRemarks());
                ps.setString(8, order.getCustomerPoNo());
                ps.setString(9, order.getSqRef());
                ps.setString(10, order.getPaymentTerms());
                ps.setString(11, order.getShipToAddress());
                if (order.getRequestedDeliveryDate() == null) ps.setNull(12, Types.DATE);
                else ps.setDate(12, Date.valueOf(order.getRequestedDeliveryDate()));
                ps.setString(13, order.getDeliveryInstructions());
                ps.setString(14, order.getHoldReason());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No generated order ID");
                    orderId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(lineSql)) {
                for (SalesOrderLineModel line : order.getLines()) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, line.getProductId());
                    ps.setInt(3, line.getQuantity());
                    ps.setBigDecimal(4, line.getUnitPrice());
                    ps.setBigDecimal(5, line.getLineTotal());
                    ps.setInt(6, line.getAllocatedQty());
                    ps.setInt(7, line.getBackorderQty());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            order.setId(orderId);
            return orderId;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("SalesOrderDao.save: " + e.getMessage());
            return -1;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); }
                catch (SQLException ignored) {}
            }
        }
    }

    /** Moves an order between statuses, but only if it is currently in {@code from}. */
    public boolean updateStatus(int id, String from, String to, String holdReason) {
        final String sql = "UPDATE tblsalesorder SET Status = ?, HoldReason = ? WHERE ID = ? AND Status = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, to);
            ps.setString(2, holdReason);
            ps.setInt(3, id);
            ps.setString(4, from);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("SalesOrderDao.updateStatus: " + e.getMessage());
            return false;
        }
    }

    /**
     * Applies the allocation result and the next status in one transaction, deducting
     * stock only when the order becomes Confirmed. Only orders in Pending Approval or
     * Credit Hold can be confirmed.
     */
    public boolean confirm(SalesOrderModel order) {
        final String headSql =
            "UPDATE tblsalesorder SET Status = ?, HoldReason = ? " +
            "WHERE ID = ? AND Status IN ('Pending Approval','Credit Hold')";
        final String lineSql =
            "UPDATE tblsalesorderline SET AllocatedQty = ?, BackorderQty = ? " +
            "WHERE ID = ? AND SalesOrderID = ?";

        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(headSql)) {
                ps.setString(1, order.getStatus());
                ps.setString(2, order.getHoldReason());
                ps.setInt(3, order.getId());
                if (ps.executeUpdate() != 1) throw new SQLException("Order is not pending confirmation");
            }

            try (PreparedStatement ps = conn.prepareStatement(lineSql)) {
                for (SalesOrderLineModel line : order.getLines()) {
                    ps.setInt(1, line.getAllocatedQty());
                    ps.setInt(2, line.getBackorderQty());
                    ps.setInt(3, line.getId());
                    ps.setInt(4, order.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            if (SalesOrderModel.CONFIRMED.equals(order.getStatus())) {
                for (SalesOrderLineModel line : order.getLines()) {
                    if (line.getAllocatedQty() > 0
                            && productDao.reduceStock(conn, line.getProductId(), line.getAllocatedQty()) != 1) {
                        throw new SQLException("Stock changed for " + line.getProductCode());
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("SalesOrderDao.confirm: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); }
                catch (SQLException ignored) {}
            }
        }
    }

    /** Deletes an order (lines cascade), but only while it is in {@code from}. */
    public boolean delete(int id, String from) {
        final String sql = "DELETE FROM tblsalesorder WHERE ID = ? AND Status = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, from);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("SalesOrderDao.delete: " + e.getMessage());
            return false;
        }
    }

    /** Generates the next order number, e.g. SO-2026-00042. */
    public String nextOrderNo() {
        final String sql = "SELECT COALESCE(MAX(ID),0)+1 FROM tblsalesorder";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("SO-%d-%05d", java.time.Year.now().getValue(), rs.getInt(1));
        } catch (SQLException e) {
            return "SO-" + System.currentTimeMillis();
        }
    }

    /** Header rows only, joined to customer name. Newest first. */
    public List<SalesOrderModel> findAll() {
        final String sql =
            "SELECT so.ID, so.OrderNo, so.OrderDate, so.CustomerID, c.CustomerName, " +
            "       so.EmployeeID, so.Status, so.TotalAmount, so.Remarks, so.CustomerPONo " +
            "FROM tblsalesorder so " +
            "LEFT JOIN tblcustomer c ON c.ID = so.CustomerID " +
            "ORDER BY so.ID DESC";
        List<SalesOrderModel> out = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapHeader(rs));
        } catch (SQLException e) {
            System.err.println("SalesOrderDao.findAll: " + e.getMessage());
        }
        return out;
    }

    /** Header + all lines for one order. */
    public SalesOrderModel findById(int id) {
        final String headSql =
            "SELECT so.ID, so.OrderNo, so.OrderDate, so.CustomerID, c.CustomerName, " +
            "       so.EmployeeID, so.Status, so.TotalAmount, so.Remarks, so.CustomerPONo, " +
            "       so.SQRef, so.PaymentTerms, so.ShipToAddress, so.RequestedDeliveryDate, " +
            "       so.DeliveryInstructions, so.HoldReason " +
            "FROM tblsalesorder so " +
            "LEFT JOIN tblcustomer c ON c.ID = so.CustomerID " +
            "WHERE so.ID = ?";
        final String lineSql =
            "SELECT sol.ID, sol.ProductID, p.ProductCode, p.ProductName, sol.Quantity, " +
            "       sol.UnitPrice, sol.LineTotal, sol.AllocatedQty, sol.BackorderQty, p.UOM " +
            "FROM tblsalesorderline sol " +
            "LEFT JOIN tblproduct p ON p.ID = sol.ProductID " +
            "WHERE sol.SalesOrderID = ? ORDER BY sol.ID";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(headSql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SalesOrderModel order = mapHeader(rs);
                order.setSqRef(rs.getString("SQRef"));
                order.setPaymentTerms(rs.getString("PaymentTerms"));
                order.setShipToAddress(rs.getString("ShipToAddress"));
                Date d = rs.getDate("RequestedDeliveryDate");
                if (d != null) order.setRequestedDeliveryDate(d.toLocalDate());
                order.setDeliveryInstructions(rs.getString("DeliveryInstructions"));
                order.setHoldReason(rs.getString("HoldReason"));

                try (PreparedStatement ps2 = conn.prepareStatement(lineSql)) {
                    ps2.setInt(1, id);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            SalesOrderLineModel line = new SalesOrderLineModel();
                            line.setId(rs2.getInt("ID"));
                            line.setProductId(rs2.getInt("ProductID"));
                            line.setProductCode(rs2.getString("ProductCode"));
                            line.setProductName(rs2.getString("ProductName"));
                            line.setUom(rs2.getString("UOM"));
                            line.setQuantity(rs2.getInt("Quantity"));
                            BigDecimal price = rs2.getBigDecimal("UnitPrice");
                            line.setUnitPrice(price == null ? BigDecimal.ZERO : price);
                            line.setAllocatedQty(rs2.getInt("AllocatedQty"));
                            line.setBackorderQty(rs2.getInt("BackorderQty"));
                            order.addLine(line);
                        }
                    }
                }
                return order;
            }
        } catch (SQLException e) {
            System.err.println("SalesOrderDao.findById: " + e.getMessage());
            return null;
        }
    }

    /** Sum of open (not closed) order values for a customer, excluding one order (the one being checked). */
    public BigDecimal openOrderTotal(int customerId, int excludeOrderId) {
        final String sql =
            "SELECT COALESCE(SUM(TotalAmount), 0) FROM tblsalesorder " +
            "WHERE CustomerID = ? AND Status <> 'Closed' AND ID <> ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, excludeOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("SalesOrderDao.openOrderTotal: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private SalesOrderModel mapHeader(ResultSet rs) throws SQLException {
        SalesOrderModel o = new SalesOrderModel();
        o.setId(rs.getInt("ID"));
        o.setOrderNo(rs.getString("OrderNo"));
        Date d = rs.getDate("OrderDate");
        if (d != null) o.setOrderDate(d.toLocalDate());
        o.setCustomerId(rs.getInt("CustomerID"));
        o.setCustomerName(rs.getString("CustomerName"));
        o.setEmployeeId(rs.getInt("EmployeeID"));
        o.setStatus(rs.getString("Status"));
        o.setRemarks(rs.getString("Remarks"));
        o.setCustomerPoNo(rs.getString("CustomerPONo"));
        o.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        return o;
    }
}
