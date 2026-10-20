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
        java.awt.GridBagConstraints gridBagConstraints;

        pnlContent = new javax.swing.JPanel();
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

        pnlContent.setBackground(new java.awt.Color(255, 255, 255));
        pnlContent.setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lblTitle.setText("Create Sales Order");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 24, 8, 24);
        pnlContent.add(lblTitle, gridBagConstraints);

        lblSO.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblSO.setText("SO Number");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 2, 4);
        pnlContent.add(lblSO, gridBagConstraints);

        txtSO.setBackground(new java.awt.Color(243, 243, 243));
        txtSO.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtSO.setPreferredSize(new java.awt.Dimension(160, 24));
        txtSO.setText("Auto-generated");
        txtSO.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        pnlContent.add(txtSO, gridBagConstraints);

        lblOrderDate.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblOrderDate.setText("Order Date");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 2, 4);
        pnlContent.add(lblOrderDate, gridBagConstraints);

        txtOrderDate.setBackground(new java.awt.Color(243, 243, 243));
        txtOrderDate.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtOrderDate.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        pnlContent.add(txtOrderDate, gridBagConstraints);

        lblTerms.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblTerms.setText("Payment Terms");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 2, 4);
        pnlContent.add(lblTerms, gridBagConstraints);

        cboTerms.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cboTerms.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 24);
        pnlContent.add(cboTerms, gridBagConstraints);

        lblCustomer.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCustomer.setText("Customer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 2, 4);
        pnlContent.add(lblCustomer, gridBagConstraints);

        cboCustomer.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cboCustomer.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        pnlContent.add(cboCustomer, gridBagConstraints);

        lblPO.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblPO.setText("Customer PO Number");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 2, 4);
        pnlContent.add(lblPO, gridBagConstraints);

        txtPO.setBackground(new java.awt.Color(243, 243, 243));
        txtPO.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtPO.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        pnlContent.add(txtPO, gridBagConstraints);

        lblSQ.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblSQ.setText("Reference SQ Number");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 2, 4);
        pnlContent.add(lblSQ, gridBagConstraints);

        txtSQ.setBackground(new java.awt.Color(243, 243, 243));
        txtSQ.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtSQ.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 24);
        pnlContent.add(txtSQ, gridBagConstraints);

        lblShipTo.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblShipTo.setText("Ship-To Address");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 2, 4);
        pnlContent.add(lblShipTo, gridBagConstraints);

        txtShipTo.setBackground(new java.awt.Color(243, 243, 243));
        txtShipTo.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtShipTo.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        pnlContent.add(txtShipTo, gridBagConstraints);

        lblDelivery.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblDelivery.setText("Requested Delivery Date");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 2, 4);
        pnlContent.add(lblDelivery, gridBagConstraints);

        txtDeliveryDate.setBackground(new java.awt.Color(243, 243, 243));
        txtDeliveryDate.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtDeliveryDate.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        pnlContent.add(txtDeliveryDate, gridBagConstraints);

        lblInstructions.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblInstructions.setText("Delivery Instructions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 2, 4);
        pnlContent.add(lblInstructions, gridBagConstraints);

        txtInstructions.setBackground(new java.awt.Color(243, 243, 243));
        txtInstructions.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtInstructions.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 24);
        pnlContent.add(txtInstructions, gridBagConstraints);

        lblLines.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblLines.setText("Line Items");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 24, 4, 24);
        pnlContent.add(lblLines, gridBagConstraints);

        cboProduct.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cboProduct.setPreferredSize(new java.awt.Dimension(160, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 8, 4);
        pnlContent.add(cboProduct, gridBagConstraints);

        spnQty.setPreferredSize(new java.awt.Dimension(60, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 4);
        pnlContent.add(spnQty, gridBagConstraints);

        btnAddLine.setBackground(new java.awt.Color(15, 127, 75));
        btnAddLine.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnAddLine.setForeground(new java.awt.Color(255, 255, 255));
        btnAddLine.setText("+ Add Line");
        btnAddLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddLineActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 4);
        pnlContent.add(btnAddLine, gridBagConstraints);

        btnRemoveLine.setBackground(new java.awt.Color(243, 243, 243));
        btnRemoveLine.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnRemoveLine.setText("Remove Line");
        btnRemoveLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveLineActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 24);
        pnlContent.add(btnRemoveLine, gridBagConstraints);

        tblLines.setRowHeight(22);
        tblLines.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrLines.setViewportView(tblLines);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 8, 24);
        pnlContent.add(scrLines, gridBagConstraints);

        lblTotal.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblTotal.setText("Total: $0.00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 24, 0);
        pnlContent.add(lblTotal, gridBagConstraints);

        lblCredit.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 24, 0);
        pnlContent.add(lblCredit, gridBagConstraints);

        btnCancel.setBackground(new java.awt.Color(243, 243, 243));
        btnCancel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 24, 12);
        pnlContent.add(btnCancel, gridBagConstraints);

        btnSave.setBackground(new java.awt.Color(15, 127, 75));
        btnSave.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setText("Save as Draft");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 24);
        pnlContent.add(btnSave, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlContent, gridBagConstraints);
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
    private javax.swing.JPanel pnlContent;
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
