package bberp;

import bberp.model.CustomerModel;
import bberp.model.ProductModel;
import bberp.model.SalesOrderLineModel;
import bberp.model.SalesOrderModel;
import bberp.service.LookupService;
import bberp.service.SalesOrderService;
import com.zaxxer.hikari.HikariDataSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

class CreateSoPanel extends JPanel {
    private final SalesOrderService service;
    private final LookupService lookups;
    private final List<SalesOrderLineModel> lines = new ArrayList<>();
    private final DefaultTableModel lineTable = new DefaultTableModel(
            new String[]{"Code", "Description", "Qty", "UOM", "Unit Price", "Line Total", "Backorder"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JLabel totalLabel = App.label("Total: $0.00", 13);
    private final JLabel creditNote = App.label(" ", 10);
    private final JTable table = new JTable(lineTable);

    private final JComboBox<CustomerModel> customer = new JComboBox<>();
    private final JComboBox<ProductModel> product = new JComboBox<>();
    private final JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
    private final JTextField poNo = App.style(new JTextField());
    private final JTextField sqRef = App.style(new JTextField());
    private final JComboBox<String> terms = new JComboBox<>(
            new String[]{"Net 30", "Net 60", "50% Advance", "COD", "Cash"});
    private final JTextField shipTo = App.style(new JTextField());
    private final JTextField deliveryDate = App.style(new JTextField(LocalDate.now().plusDays(7).toString()));
    private final JTextField orderDate = App.style(new JTextField(LocalDate.now().toString()));
    private final JTextField instructions = App.style(new JTextField());
    private final JTextField soNumber = App.style(new JTextField());

    CreateSoPanel(App app, HikariDataSource ds) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        service = new SalesOrderService(ds);
        lookups = new LookupService(ds);

        soNumber.setEditable(false);
        soNumber.setText(service.nextOrderNo());
        terms.setBackground(App.FIELD);
        for (CustomerModel c : lookups.customers()) customer.addItem(c);
        for (ProductModel p : lookups.products()) product.addItem(p);
        product.setPreferredSize(new Dimension(320, 24));
        customer.addActionListener(e -> customerChanged());

        JButton addLine = App.button("+ Add Line", App.GREEN, Color.WHITE);
        addLine.addActionListener(e -> addLine());
        JButton removeLine = App.button("Remove Line", App.FIELD, Color.BLACK);
        removeLine.addActionListener(e -> removeLine());

        JPanel lineButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        lineButtons.setBackground(Color.WHITE);
        lineButtons.add(new JLabel("Product:"));
        lineButtons.add(product);
        lineButtons.add(new JLabel("Qty:"));
        lineButtons.add(qty);
        lineButtons.add(addLine);
        lineButtons.add(removeLine);

        JPanel lineSection = new JPanel(new BorderLayout(0, 8));
        lineSection.setBackground(Color.WHITE);
        lineSection.add(App.label("Line Items", 13), BorderLayout.NORTH);
        lineSection.add(new JScrollPane(table), BorderLayout.CENTER);
        lineSection.add(lineButtons, BorderLayout.SOUTH);

        JButton cancel = App.button("Cancel", App.FIELD, Color.BLACK);
        cancel.addActionListener(e -> app.go(new SalesOrderListPanel(app, ds)));
        JButton save = App.button("Save Order", App.GREEN, Color.WHITE);
        save.addActionListener(e -> save(app));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        JPanel totals = new JPanel();
        totals.setLayout(new BoxLayout(totals, BoxLayout.Y_AXIS));
        totals.setBackground(Color.WHITE);
        totals.add(totalLabel);
        totals.add(creditNote);
        footer.add(totals, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);
        actions.add(cancel);
        actions.add(save);
        footer.add(actions, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        content.add(App.label("Create Sales Order", 18), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(Color.WHITE);
        body.add(header(), BorderLayout.NORTH);
        body.add(lineSection, BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        add(new Sidebar(false, 480), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        customerChanged();
    }

    private JPanel header() {
        JPanel grid = new JPanel(new GridLayout(0, 3, 16, 8));
        grid.setBackground(Color.WHITE);
        grid.add(labeled("SO Number", soNumber));
        grid.add(labeled("Order Date", orderDate));
        grid.add(labeled("Payment Terms", terms));
        grid.add(labeled("Customer", customer));
        grid.add(labeled("Customer PO Number", poNo));
        grid.add(labeled("Reference SQ Number", sqRef));
        grid.add(labeled("Ship-To Address", shipTo));
        grid.add(labeled("Requested Delivery Date", deliveryDate));
        grid.add(labeled("Delivery Instructions", instructions));
        return grid;
    }

    private void customerChanged() {
        CustomerModel c = (CustomerModel) customer.getSelectedItem();
        if (c == null) {
            creditNote.setText(" ");
            return;
        }
        if (shipTo.getText().isBlank() && c.getAddress() != null) {
            shipTo.setText(c.getAddress());
        }
        creditNote.setText(String.format("Credit limit %s, current AR %s",
                App.money(c.getCreditLimit()), App.money(c.getArBalance())));
    }

    private void addLine() {
        ProductModel p = (ProductModel) product.getSelectedItem();
        if (p == null) return;
        int q = (Integer) qty.getValue();
        lines.add(new SalesOrderLineModel(p.getId(), p.getCode(), p.getName(), q, p.getUom(), p.getUnitPrice()));
        refreshLines();
    }

    private void removeLine() {
        int r = table.getSelectedRow();
        if (r < 0) r = lines.size() - 1;
        if (r < 0) return;
        lines.remove(r);
        refreshLines();
    }

    private void refreshLines() {
        lineTable.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrderLineModel l : lines) {
            lineTable.addRow(new Object[]{l.getProductCode(), l.getProductName(), l.getQuantity(),
                    l.getUom(), l.getUnitPrice(), l.getLineTotal(), ""});
            total = total.add(l.getLineTotal());
        }
        totalLabel.setText("Total: " + App.money(total));
    }

    private void save(App app) {
        LocalDate requested;
        try {
            requested = LocalDate.parse(deliveryDate.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Requested Delivery Date must be yyyy-MM-dd.");
            return;
        }
        LocalDate ordered;
        try {
            ordered = LocalDate.parse(orderDate.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Order Date must be yyyy-MM-dd.");
            return;
        }

        CustomerModel c = (CustomerModel) customer.getSelectedItem();
        SalesOrderModel order = new SalesOrderModel();
        order.setOrderNo(soNumber.getText());
        order.setOrderDate(ordered);
        if (c != null) {
            order.setCustomerId(c.getId());
            order.setCustomerName(c.getName());
            order.setCustomerCreditLimit(c.getCreditLimit());
            order.setCustomerArBalance(c.getArBalance());
        }
        order.setCustomerPoNo(poNo.getText().trim());
        order.setSqRef(sqRef.getText().trim());
        order.setPaymentTerms(String.valueOf(terms.getSelectedItem()));
        order.setShipToAddress(shipTo.getText().trim());
        order.setRequestedDeliveryDate(requested);
        order.setDeliveryInstructions(instructions.getText().trim());
        lines.forEach(order::addLine);

        try {
            SalesOrderModel saved = service.create(order);
            JOptionPane.showMessageDialog(this,
                    saved.getOrderNo() + " saved as Draft.\n"
                    + "Total: " + App.money(saved.getTotalAmount()) + "\n\n"
                    + "Submit it from the Sales Orders list to run the credit and stock checks.",
                    "Sales Order", JOptionPane.INFORMATION_MESSAGE);
            app.go(new SalesOrderListPanel(app, app.ds()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Save",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private static JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(Color.WHITE);
        p.add(App.label(text, 11), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}
