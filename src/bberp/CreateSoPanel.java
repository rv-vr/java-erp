package bberp;

import bberp.service.LookupService;
import bberp.service.SalesOrderService;
import com.zaxxer.hikari.HikariDataSource;

public class CreateSoPanel extends javax.swing.JPanel {
    private App app;
    private HikariDataSource ds;
    private bberp.service.SalesOrderService service;
    private bberp.service.LookupService lookups;
    private final java.util.List<bberp.model.SalesOrderLineModel> lines = new java.util.ArrayList<>();
    private final javax.swing.table.DefaultTableModel lineTable = new javax.swing.table.DefaultTableModel(
            new String[]{"Code", "Description", "Qty", "UOM", "Unit Price", "Line Total", "Backorder"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    /** Required by the NetBeans GUI Builder. */
    public CreateSoPanel() {
        initComponents();
        spnQty.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1_000_000, 1));
        tblLines.setModel(lineTable);
        cboTerms.addItem("Net 30");
        cboTerms.addItem("Net 60");
        cboTerms.addItem("50% Advance");
        cboTerms.addItem("COD");
        cboTerms.addItem("Cash");
        txtOrderDate.setText(java.time.LocalDate.now().toString());
        txtDeliveryDate.setText(java.time.LocalDate.now().plusDays(7).toString());
        cboCustomer.addActionListener(e -> customerChanged());
    }

    public CreateSoPanel(App app, com.zaxxer.hikari.HikariDataSource ds) {
        this();
        connect(app, ds);
    }

    /** Wiring kept outside the generated code so the form can be edited safely. */
    public void connect(App app, com.zaxxer.hikari.HikariDataSource ds) {
        this.app = app;
        this.ds = ds;
        this.service = new bberp.service.SalesOrderService(ds);
        this.lookups = new bberp.service.LookupService(ds);
        for (bberp.model.CustomerModel c : lookups.customers()) {
            cboCustomer.addItem(c);
        }
        for (bberp.model.ProductModel p : lookups.products()) {
            cboProduct.addItem(p);
        }
        txtSO.setText(service.nextOrderNo());
        customerChanged();
    }


    private void customerChanged() {
        bberp.model.CustomerModel c = (bberp.model.CustomerModel) cboCustomer.getSelectedItem();
        if (c == null) {
            lblCredit.setText(" ");
            return;
        }
        if (txtShipTo.getText().isBlank() && c.getAddress() != null) {
            txtShipTo.setText(c.getAddress());
        }
        lblCredit.setText(String.format("Credit limit %s, current AR %s",
                App.money(c.getCreditLimit()), App.money(c.getArBalance())));
    }

    private void addLine() {
        bberp.model.ProductModel p = (bberp.model.ProductModel) cboProduct.getSelectedItem();
        if (p == null) {
            return;
        }
        int q = (Integer) spnQty.getValue();
        lines.add(new bberp.model.SalesOrderLineModel(p.getId(), p.getCode(), p.getName(), q,
                p.getUom(), p.getUnitPrice()));
        refreshLines();
    }

    private void removeLine() {
        int r = tblLines.getSelectedRow();
        if (r < 0) {
            r = lines.size() - 1;
        }
        if (r < 0) {
            return;
        }
        lines.remove(r);
        refreshLines();
    }

    private void refreshLines() {
        lineTable.setRowCount(0);
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (bberp.model.SalesOrderLineModel l : lines) {
            lineTable.addRow(new Object[]{l.getProductCode(), l.getProductName(), l.getQuantity(),
                l.getUom(), l.getUnitPrice(), l.getLineTotal(), ""});
            total = total.add(l.getLineTotal());
        }
        lblTotal.setText("Total: " + App.money(total));
    }

    private void save() {
        java.time.LocalDate requested;
        try {
            requested = java.time.LocalDate.parse(txtDeliveryDate.getText().trim());
        } catch (java.time.format.DateTimeParseException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Requested Delivery Date must be yyyy-MM-dd.");
            return;
        }
        java.time.LocalDate ordered;
        try {
            ordered = java.time.LocalDate.parse(txtOrderDate.getText().trim());
        } catch (java.time.format.DateTimeParseException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Order Date must be yyyy-MM-dd.");
            return;
        }

        bberp.model.CustomerModel c = (bberp.model.CustomerModel) cboCustomer.getSelectedItem();
        bberp.model.SalesOrderModel order = new bberp.model.SalesOrderModel();
        order.setOrderNo(txtSO.getText());
        order.setOrderDate(ordered);
        if (c != null) {
            order.setCustomerId(c.getId());
            order.setCustomerName(c.getName());
            order.setCustomerCreditLimit(c.getCreditLimit());
            order.setCustomerArBalance(c.getArBalance());
        }
        order.setCustomerPoNo(txtPO.getText().trim());
        order.setSqRef(txtSQ.getText().trim());
        order.setPaymentTerms(String.valueOf(cboTerms.getSelectedItem()));
        order.setShipToAddress(txtShipTo.getText().trim());
        order.setRequestedDeliveryDate(requested);
        order.setDeliveryInstructions(txtInstructions.getText().trim());
        lines.forEach(order::addLine);

        try {
            bberp.model.SalesOrderModel saved = service.create(order);
            javax.swing.JOptionPane.showMessageDialog(this,
                    saved.getOrderNo() + " saved as Draft.\n"
                    + "Total: " + App.money(saved.getTotalAmount()) + "\n\n"
                    + "Submit it from the Sales Orders list to run the credit and stock checks.",
                    "Sales Order", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            app.go(new SalesOrderListPanel(app, app.ds()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Save",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidebar = new bberp.Sidebar();
        lblTitle = new javax.swing.JLabel();
        lblSO = new javax.swing.JLabel();
        txtSO = new javax.swing.JTextField();
        lblOrderDate = new javax.swing.JLabel();
        txtOrderDate = new javax.swing.JTextField();
        lblTerms = new javax.swing.JLabel();
        cboTerms = new javax.swing.JComboBox();
        lblCustomer = new javax.swing.JLabel();
        cboCustomer = new javax.swing.JComboBox();
        lblPO = new javax.swing.JLabel();
        txtPO = new javax.swing.JTextField();
        lblSQ = new javax.swing.JLabel();
        txtSQ = new javax.swing.JTextField();
        lblShipTo = new javax.swing.JLabel();
        txtShipTo = new javax.swing.JTextField();
        lblDelivery = new javax.swing.JLabel();
        txtDeliveryDate = new javax.swing.JTextField();
        lblInstructions = new javax.swing.JLabel();
        txtInstructions = new javax.swing.JTextField();
        lblLines = new javax.swing.JLabel();
        cboProduct = new javax.swing.JComboBox();
        spnQty = new javax.swing.JSpinner();
        btnAddLine = new bberp.ui.FlatButton();
        btnRemoveLine = new bberp.ui.FlatButton();
        scrLines = new javax.swing.JScrollPane();
        tblLines = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        lblCredit = new javax.swing.JLabel();
        btnCancel = new bberp.ui.FlatButton();
        btnSave = new bberp.ui.FlatButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());

        sidebar.setPreferredSize(new java.awt.Dimension(109, 480));
        add(sidebar, new java.awt.GridBagConstraints(0, 0, 1, 8, 0.0, 1.0, java.awt.GridBagConstraints.CENTER, java.awt.GridBagConstraints.VERTICAL, new java.awt.Insets(0, 0, 0, 0), 0, 0));

        lblTitle.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lblTitle.setText("Create Sales Order");
        add(lblTitle, new java.awt.GridBagConstraints(1, 0, 6, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(24, 24, 8, 24), 0, 0));

        lblSO.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblSO.setText("SO Number");
        add(lblSO, new java.awt.GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 24, 2, 4), 0, 0));

        txtSO.setBackground(new java.awt.Color(243, 243, 243));
        txtSO.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtSO.setPreferredSize(new java.awt.Dimension(160, 24));
        txtSO.setText("Auto-generated");
        txtSO.setEditable(false);
        add(txtSO, new java.awt.GridBagConstraints(2, 1, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 16), 0, 0));

        lblOrderDate.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblOrderDate.setText("Order Date");
        add(lblOrderDate, new java.awt.GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 8, 2, 4), 0, 0));

        txtOrderDate.setBackground(new java.awt.Color(243, 243, 243));
        txtOrderDate.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtOrderDate.setPreferredSize(new java.awt.Dimension(160, 24));
        add(txtOrderDate, new java.awt.GridBagConstraints(4, 1, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 16), 0, 0));

        lblTerms.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblTerms.setText("Payment Terms");
        add(lblTerms, new java.awt.GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 8, 2, 4), 0, 0));

        cboTerms.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cboTerms.setPreferredSize(new java.awt.Dimension(160, 24));
        add(cboTerms, new java.awt.GridBagConstraints(6, 1, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 24), 0, 0));

        lblCustomer.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCustomer.setText("Customer");
        add(lblCustomer, new java.awt.GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 24, 2, 4), 0, 0));

        cboCustomer.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cboCustomer.setPreferredSize(new java.awt.Dimension(160, 24));
        add(cboCustomer, new java.awt.GridBagConstraints(2, 2, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 16), 0, 0));

        lblPO.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblPO.setText("Customer PO Number");
        add(lblPO, new java.awt.GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 8, 2, 4), 0, 0));

        txtPO.setBackground(new java.awt.Color(243, 243, 243));
        txtPO.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtPO.setPreferredSize(new java.awt.Dimension(160, 24));
        add(txtPO, new java.awt.GridBagConstraints(4, 2, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 16), 0, 0));

        lblSQ.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblSQ.setText("Reference SQ Number");
        add(lblSQ, new java.awt.GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 8, 2, 4), 0, 0));

        txtSQ.setBackground(new java.awt.Color(243, 243, 243));
        txtSQ.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtSQ.setPreferredSize(new java.awt.Dimension(160, 24));
        add(txtSQ, new java.awt.GridBagConstraints(6, 2, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 24), 0, 0));

        lblShipTo.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblShipTo.setText("Ship-To Address");
        add(lblShipTo, new java.awt.GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 24, 2, 4), 0, 0));

        txtShipTo.setBackground(new java.awt.Color(243, 243, 243));
        txtShipTo.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtShipTo.setPreferredSize(new java.awt.Dimension(160, 24));
        add(txtShipTo, new java.awt.GridBagConstraints(2, 3, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 16), 0, 0));

        lblDelivery.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblDelivery.setText("Requested Delivery Date");
        add(lblDelivery, new java.awt.GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 8, 2, 4), 0, 0));

        txtDeliveryDate.setBackground(new java.awt.Color(243, 243, 243));
        txtDeliveryDate.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtDeliveryDate.setPreferredSize(new java.awt.Dimension(160, 24));
        add(txtDeliveryDate, new java.awt.GridBagConstraints(4, 3, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 16), 0, 0));

        lblInstructions.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblInstructions.setText("Delivery Instructions");
        add(lblInstructions, new java.awt.GridBagConstraints(5, 3, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 8, 2, 4), 0, 0));

        txtInstructions.setBackground(new java.awt.Color(243, 243, 243));
        txtInstructions.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtInstructions.setPreferredSize(new java.awt.Dimension(160, 24));
        add(txtInstructions, new java.awt.GridBagConstraints(6, 3, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 6, 24), 0, 0));

        lblLines.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblLines.setText("Line Items");
        add(lblLines, new java.awt.GridBagConstraints(1, 4, 6, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(8, 24, 4, 24), 0, 0));

        cboProduct.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cboProduct.setPreferredSize(new java.awt.Dimension(160, 24));
        add(cboProduct, new java.awt.GridBagConstraints(1, 5, 2, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 24, 8, 4), 0, 0));

        spnQty.setPreferredSize(new java.awt.Dimension(60, 24));
        add(spnQty, new java.awt.GridBagConstraints(3, 5, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 4), 0, 0));

        btnAddLine.setBackground(new java.awt.Color(15, 127, 75));
        btnAddLine.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnAddLine.setForeground(new java.awt.Color(255, 255, 255));
        btnAddLine.setText("+ Add Line");
        btnAddLine.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnAddLineActionPerformed(evt);
                    }
                });
        add(btnAddLine, new java.awt.GridBagConstraints(4, 5, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 4), 0, 0));

        btnRemoveLine.setBackground(new java.awt.Color(243, 243, 243));
        btnRemoveLine.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnRemoveLine.setForeground(new java.awt.Color(0, 0, 0));
        btnRemoveLine.setText("Remove Line");
        btnRemoveLine.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnRemoveLineActionPerformed(evt);
                    }
                });
        add(btnRemoveLine, new java.awt.GridBagConstraints(5, 5, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 8, 24), 0, 0));

        tblLines.setRowHeight(22);
        tblLines.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
scrLines.setViewportView(tblLines);
        add(scrLines, new java.awt.GridBagConstraints(1, 6, 6, 1, 1.0, 1.0, java.awt.GridBagConstraints.CENTER, java.awt.GridBagConstraints.BOTH, new java.awt.Insets(0, 24, 8, 24), 0, 0));

        lblTotal.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblTotal.setText("Total: $0.00");
        add(lblTotal, new java.awt.GridBagConstraints(1, 7, 2, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 24, 24, 0), 0, 0));

        lblCredit.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        add(lblCredit, new java.awt.GridBagConstraints(3, 7, 2, 1, 0.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 24, 0), 0, 0));

        btnCancel.setBackground(new java.awt.Color(243, 243, 243));
        btnCancel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(0, 0, 0));
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnCancelActionPerformed(evt);
                    }
                });
        add(btnCancel, new java.awt.GridBagConstraints(5, 7, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.EAST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 4, 24, 12), 0, 0));

        btnSave.setBackground(new java.awt.Color(15, 127, 75));
        btnSave.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setText("Save as Draft");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSaveActionPerformed(evt);
                    }
                });
        add(btnSave, new java.awt.GridBagConstraints(6, 7, 1, 1, 0.0, 0.0, java.awt.GridBagConstraints.EAST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 0, 24, 24), 0, 0));
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddLineActionPerformed
        addLine();
    }//GEN-LAST:event_btnAddLineActionPerformed
    private void btnRemoveLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveLineActionPerformed
        removeLine();
    }//GEN-LAST:event_btnRemoveLineActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        app.go(new SalesOrderListPanel(app, ds));
    }//GEN-LAST:event_btnCancelActionPerformed
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        save();
    }//GEN-LAST:event_btnSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private bberp.Sidebar sidebar;
    private bberp.ui.FlatButton btnAddLine;
    private bberp.ui.FlatButton btnCancel;
    private bberp.ui.FlatButton btnRemoveLine;
    private bberp.ui.FlatButton btnSave;
    private javax.swing.JComboBox cboCustomer;
    private javax.swing.JComboBox cboProduct;
    private javax.swing.JComboBox cboTerms;
    private javax.swing.JLabel lblCredit;
    private javax.swing.JLabel lblCustomer;
    private javax.swing.JLabel lblDelivery;
    private javax.swing.JLabel lblInstructions;
    private javax.swing.JLabel lblLines;
    private javax.swing.JLabel lblOrderDate;
    private javax.swing.JLabel lblPO;
    private javax.swing.JLabel lblSO;
    private javax.swing.JLabel lblSQ;
    private javax.swing.JLabel lblShipTo;
    private javax.swing.JLabel lblTerms;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JScrollPane scrLines;
    private javax.swing.JSpinner spnQty;
    private javax.swing.JTable tblLines;
    private javax.swing.JTextField txtDeliveryDate;
    private javax.swing.JTextField txtInstructions;
    private javax.swing.JTextField txtOrderDate;
    private javax.swing.JTextField txtPO;
    private javax.swing.JTextField txtSO;
    private javax.swing.JTextField txtSQ;
    private javax.swing.JTextField txtShipTo;
    // End of variables declaration//GEN-END:variables
}
