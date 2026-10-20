package bberp;

import bberp.service.SalesOrderService;
import com.zaxxer.hikari.HikariDataSource;

public class SalesOrderListPanel extends javax.swing.JPanel {
    private App app;
    private HikariDataSource ds;
    private bberp.service.SalesOrderService service;
    private final OrderTableModel model = new OrderTableModel();

    /** Required by the NetBeans GUI Builder. */
    public SalesOrderListPanel() {
        initComponents();
        tblOrders.setModel(model);
        tblOrders.getSelectionModel().addListSelectionListener(e -> updateButtons());
        tblOrders.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblOrders.getSelectedRow() >= 0) {
                    showDetails();
                }
            }
        });
        updateButtons();
    }

    public SalesOrderListPanel(App app, com.zaxxer.hikari.HikariDataSource ds) {
        this();
        connect(app, ds);
    }

    /** Wiring kept outside the generated code so the form can be edited safely. */
    public void connect(App app, com.zaxxer.hikari.HikariDataSource ds) {
        this.app = app;
        this.ds = ds;
        this.service = new bberp.service.SalesOrderService(ds);
        refresh();
    }


    private void refresh() {
        java.util.List<bberp.model.SalesOrderModel> orders = service.list();
        model.setOrders(orders);
        lblCount.setText(orders.size() + " order(s)");
        updateButtons();
    }

    private bberp.model.SalesOrderModel selected() {
        int row = tblOrders.getSelectedRow();
        return row < 0 ? null : model.orderAt(tblOrders.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        bberp.model.SalesOrderModel o = selected();
        String status = o == null ? "" : o.getStatus();
        btnSubmit.setEnabled(bberp.model.SalesOrderModel.DRAFT.equals(status));
        btnConfirm.setEnabled(bberp.model.SalesOrderModel.PENDING_APPROVAL.equals(status)
                || bberp.model.SalesOrderModel.CREDIT_HOLD.equals(status));
        btnClose.setEnabled(bberp.model.SalesOrderModel.CONFIRMED.equals(status));
        btnDelete.setEnabled(!status.isEmpty() && !bberp.model.SalesOrderModel.CONFIRMED.equals(status)
                && !bberp.model.SalesOrderModel.CLOSED.equals(status));
    }

    private void act(String action) {
        bberp.model.SalesOrderModel o = selected();
        if (o == null) {
            info("Select an order first.");
            return;
        }
        try {
            switch (action) {
                case "Submit":
                    service.submit(o.getId());
                    info("Order " + o.getOrderNo() + " submitted for approval.");
                    break;
                case "Confirm":
                    if (bberp.model.SalesOrderModel.CREDIT_HOLD.equals(o.getStatus())
                            && javax.swing.JOptionPane.showConfirmDialog(this,
                                "Override the credit hold and confirm " + o.getOrderNo() + "?",
                                "Credit Override",
                                javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
                        return;
                    }
                    bberp.service.SalesOrderService.Result r = service.confirm(o.getId());
                    String message = o.getOrderNo() + " is now " + r.order.getStatus() + ".\n"
                            + "Exposure: " + App.money(r.exposure) + " of " + App.money(r.creditLimit) + " limit"
                            + (r.backorderTotal > 0
                                ? "\nBackordered: " + r.backorderTotal + " unit(s)" : "\nFully allocated");
                    if (r.order.getHoldReason() != null) {
                        message += "\n" + r.order.getHoldReason();
                    }
                    info(message);
                    break;
                case "Close":
                    if (javax.swing.JOptionPane.showConfirmDialog(this,
                            "Close " + o.getOrderNo() + "? This is final.",
                            "Close Order",
                            javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
                        return;
                    }
                    service.close(o.getId());
                    info("Order " + o.getOrderNo() + " closed.");
                    break;
                case "Delete":
                    if (javax.swing.JOptionPane.showConfirmDialog(this,
                            "Delete " + o.getOrderNo() + "?",
                            "Delete Order",
                            javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
                        return;
                    }
                    service.delete(o.getId());
                    info("Order " + o.getOrderNo() + " deleted.");
                    break;
                default:
                    return;
            }
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Not Allowed",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            refresh();
        }
    }

    private void info(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, message, "Sales Order",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDetails() {
        bberp.model.SalesOrderModel selected = selected();
        if (selected == null) {
            info("Select an order first.");
            return;
        }
        bberp.model.SalesOrderModel o = service.find(selected.getId());
        if (o == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Order not found.", "Not Found",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Order No   : ").append(o.getOrderNo()).append('\n')
          .append("Date       : ").append(o.getOrderDate()).append('\n')
          .append("Customer   : ").append(o.getCustomerName()).append('\n')
          .append("Customer PO: ").append(nz(o.getCustomerPoNo())).append('\n')
          .append("SQ Ref     : ").append(nz(o.getSqRef())).append('\n')
          .append("Terms      : ").append(nz(o.getPaymentTerms())).append('\n')
          .append("Ship To    : ").append(nz(o.getShipToAddress())).append('\n')
          .append("Delivery   : ").append(o.getRequestedDeliveryDate() == null
                  ? "-" : o.getRequestedDeliveryDate()).append('\n')
          .append("Instr.     : ").append(nz(o.getDeliveryInstructions())).append('\n')
          .append("Status     : ").append(o.getStatus()).append('\n');
        if (o.getHoldReason() != null) {
            sb.append("Note       : ").append(o.getHoldReason()).append('\n');
        }
        sb.append("\n")
          .append(String.format("%-14s %-28s %5s %5s %12s %10s%n",
                  "Item", "Description", "Qty", "UOM", "Unit", "Line Total"));
        for (bberp.model.SalesOrderLineModel l : o.getLines()) {
            sb.append(String.format("%-14s %-28s %5d %5s %12s %10s%n",
                    l.getProductCode(), l.getProductName(), l.getQuantity(),
                    nz(l.getUom()), l.getUnitPrice(), l.getLineTotal()));
            if (l.getBackorderQty() > 0) {
                sb.append("   allocated ").append(l.getAllocatedQty())
                  .append(", backorder ").append(l.getBackorderQty()).append('\n');
            }
        }
        sb.append("\nTOTAL: ").append(App.money(o.getTotalAmount()));

        javax.swing.JTextArea ta = new javax.swing.JTextArea(sb.toString(), 18, 80);
        ta.setEditable(false);
        ta.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        javax.swing.JOptionPane.showMessageDialog(this, new javax.swing.JScrollPane(ta),
                "Sales Order " + o.getOrderNo(), javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private static String nz(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    static class OrderTableModel extends javax.swing.table.AbstractTableModel {
        private final String[] cols = {"ID", "SO Number", "Date", "Customer PO", "Customer", "Total", "Status"};
        private final java.util.List<bberp.model.SalesOrderModel> rows = new java.util.ArrayList<>();

        void setOrders(java.util.List<bberp.model.SalesOrderModel> list) {
            rows.clear();
            if (list != null) {
                rows.addAll(list);
            }
            fireTableDataChanged();
        }

        bberp.model.SalesOrderModel orderAt(int r) {
            return (r < 0 || r >= rows.size()) ? null : rows.get(r);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            bberp.model.SalesOrderModel o = rows.get(r);
            switch (c) {
                case 0: return o.getId();
                case 1: return o.getOrderNo();
                case 2: return o.getOrderDate();
                case 3: return o.getCustomerPoNo();
                case 4: return o.getCustomerName();
                case 5: return App.money(o.getTotalAmount());
                case 6: return o.getStatus();
                default: return null;
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidebar = new bberp.Sidebar();
        lblTitle = new javax.swing.JLabel();
        btnBack = new bberp.ui.FlatButton();
        btnRefresh = new bberp.ui.FlatButton();
        btnCreate = new bberp.ui.FlatButton();
        btnSubmit = new bberp.ui.FlatButton();
        btnConfirm = new bberp.ui.FlatButton();
        btnClose = new bberp.ui.FlatButton();
        btnView = new bberp.ui.FlatButton();
        btnDelete = new bberp.ui.FlatButton();
        lblCount = new javax.swing.JLabel();
        scrOrders = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());

        sidebar.setPreferredSize(new java.awt.Dimension(109, 480));
        add(sidebar, new java.awt.GridBagConstraints(0, 0, 1, 3, 0.0, 1.0, java.awt.GridBagConstraints.CENTER, java.awt.GridBagConstraints.VERTICAL, new java.awt.Insets(0, 0, 0, 0), 0, 0));

        lblTitle.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lblTitle.setText("Sales Orders");
        add(lblTitle, new java.awt.GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(24, 24, 8, 0), 0, 0));

        btnBack.setBackground(new java.awt.Color(243, 243, 243));
        btnBack.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(0, 0, 0));
        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnBackActionPerformed(evt);
                    }
                });
        add(btnBack, new java.awt.GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(24, 4, 8, 0), 0, 0));

        btnRefresh.setBackground(new java.awt.Color(243, 243, 243));
        btnRefresh.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(0, 0, 0));
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnRefreshActionPerformed(evt);
                    }
                });
        add(btnRefresh, new java.awt.GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(24, 4, 8, 0), 0, 0));

        btnCreate.setBackground(new java.awt.Color(15, 127, 75));
        btnCreate.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnCreate.setForeground(new java.awt.Color(255, 255, 255));
        btnCreate.setText("+ Create SO");
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnCreateActionPerformed(evt);
                    }
                });
        add(btnCreate, new java.awt.GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(24, 4, 8, 24), 0, 0));

        btnSubmit.setBackground(new java.awt.Color(15, 127, 75));
        btnSubmit.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnSubmit.setForeground(new java.awt.Color(255, 255, 255));
        btnSubmit.setText("Submit");
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSubmitActionPerformed(evt);
                    }
                });
        add(btnSubmit, new java.awt.GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 24, 8, 0), 0, 0));

        btnConfirm.setBackground(new java.awt.Color(15, 127, 75));
        btnConfirm.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnConfirm.setForeground(new java.awt.Color(255, 255, 255));
        btnConfirm.setText("Confirm");
        btnConfirm.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnConfirmActionPerformed(evt);
                    }
                });
        add(btnConfirm, new java.awt.GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 0), 0, 0));

        btnClose.setBackground(new java.awt.Color(243, 243, 243));
        btnClose.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnClose.setForeground(new java.awt.Color(0, 0, 0));
        btnClose.setText("Close Order");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnCloseActionPerformed(evt);
                    }
                });
        add(btnClose, new java.awt.GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 0), 0, 0));

        btnView.setBackground(new java.awt.Color(243, 243, 243));
        btnView.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnView.setForeground(new java.awt.Color(0, 0, 0));
        btnView.setText("View");
        btnView.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnViewActionPerformed(evt);
                    }
                });
        add(btnView, new java.awt.GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 0), 0, 0));

        btnDelete.setBackground(new java.awt.Color(243, 243, 243));
        btnDelete.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(0, 0, 0));
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnDeleteActionPerformed(evt);
                    }
                });
        add(btnDelete, new java.awt.GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 0), 0, 0));

        lblCount.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCount.setText("0 order(s)");
        add(lblCount, new java.awt.GridBagConstraints(6, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 24), 0, 0));

        tblOrders.setRowHeight(22);
        tblOrders.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
scrOrders.setViewportView(tblOrders);
        add(scrOrders, new java.awt.GridBagConstraints(1, 2, 6, 1, 1.0, 1.0, java.awt.GridBagConstraints.CENTER, java.awt.GridBagConstraints.BOTH, new java.awt.Insets(0, 24, 24, 24), 0, 0));
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        app.go(new MenuPanel(app, ds));
    }//GEN-LAST:event_btnBackActionPerformed
    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed
    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        app.go(new CreateSoPanel(app, ds));
    }//GEN-LAST:event_btnCreateActionPerformed
    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        act("Submit");
    }//GEN-LAST:event_btnSubmitActionPerformed
    private void btnConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmActionPerformed
        act("Confirm");
    }//GEN-LAST:event_btnConfirmActionPerformed
    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        act("Close");
    }//GEN-LAST:event_btnCloseActionPerformed
    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        showDetails();
    }//GEN-LAST:event_btnViewActionPerformed
    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        act("Delete");
    }//GEN-LAST:event_btnDeleteActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private bberp.Sidebar sidebar;
    private bberp.ui.FlatButton btnBack;
    private bberp.ui.FlatButton btnClose;
    private bberp.ui.FlatButton btnConfirm;
    private bberp.ui.FlatButton btnCreate;
    private bberp.ui.FlatButton btnDelete;
    private bberp.ui.FlatButton btnRefresh;
    private bberp.ui.FlatButton btnSubmit;
    private bberp.ui.FlatButton btnView;
    private javax.swing.JLabel lblCount;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrOrders;
    private javax.swing.JTable tblOrders;
    // End of variables declaration//GEN-END:variables
}
