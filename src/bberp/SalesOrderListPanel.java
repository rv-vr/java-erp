package bberp;

import bberp.model.SalesOrderLineModel;
import bberp.model.SalesOrderModel;
import bberp.service.SalesOrderService;
import com.zaxxer.hikari.HikariDataSource;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

class SalesOrderListPanel extends JPanel {
    private final SalesOrderService service;
    private final OrderTableModel model = new OrderTableModel();
    private final JTable table = new JTable(model);
    private final JLabel count = App.label("", 11);
    private final JButton submitBtn = App.button("Submit", App.GREEN, Color.WHITE);
    private final JButton confirmBtn = App.button("Confirm", App.GREEN, Color.WHITE);
    private final JButton closeBtn = App.button("Close Order", App.FIELD, Color.BLACK);
    private final JButton deleteBtn = App.button("Delete", App.FIELD, Color.BLACK);
    private final JButton viewBtn = App.button("View", App.FIELD, Color.BLACK);

    SalesOrderListPanel(App app, HikariDataSource ds) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        service = new SalesOrderService(ds);

        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) showDetails();
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        submitBtn.addActionListener(e -> act("Submit"));
        confirmBtn.addActionListener(e -> act("Confirm"));
        closeBtn.addActionListener(e -> act("Close"));
        deleteBtn.addActionListener(e -> act("Delete"));
        viewBtn.addActionListener(e -> showDetails());

        JButton back = App.button("Back", App.FIELD, Color.BLACK);
        back.addActionListener(e -> app.go(new MenuPanel(app, ds)));
        JButton refresh = App.button("Refresh", App.FIELD, Color.BLACK);
        refresh.addActionListener(e -> refresh());
        JButton create = App.button("+ Create SO", App.GREEN, Color.WHITE);
        create.addActionListener(e -> app.go(new CreateSoPanel(app, ds)));

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topButtons.setBackground(Color.WHITE);
        topButtons.add(back);
        topButtons.add(refresh);
        topButtons.add(create);

        JPanel workflow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        workflow.setBackground(Color.WHITE);
        for (JButton b : new JButton[]{submitBtn, confirmBtn, closeBtn, viewBtn, deleteBtn}) {
            workflow.add(b);
        }
        workflow.add(count);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(Color.WHITE);
        top.add(App.label("Sales Orders", 18), BorderLayout.WEST);
        top.add(topButtons, BorderLayout.EAST);
        top.add(workflow, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        add(new Sidebar(false, 480), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        refresh();
    }

    private void refresh() {
        List<SalesOrderModel> orders = service.list();
        model.setOrders(orders);
        count.setText(orders.size() + " order(s)");
        updateButtons();
    }

    private SalesOrderModel selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : model.orderAt(table.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        SalesOrderModel o = selected();
        String status = o == null ? "" : o.getStatus();
        submitBtn.setEnabled(SalesOrderModel.DRAFT.equals(status));
        confirmBtn.setEnabled(SalesOrderModel.PENDING_APPROVAL.equals(status)
                || SalesOrderModel.CREDIT_HOLD.equals(status));
        closeBtn.setEnabled(SalesOrderModel.CONFIRMED.equals(status));
        deleteBtn.setEnabled(!status.isEmpty() && !SalesOrderModel.CONFIRMED.equals(status)
                && !SalesOrderModel.CLOSED.equals(status));
    }

    private void act(String action) {
        SalesOrderModel o = selected();
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
                    if (SalesOrderModel.CREDIT_HOLD.equals(o.getStatus())
                            && JOptionPane.showConfirmDialog(this,
                                "Override the credit hold and confirm " + o.getOrderNo() + "?",
                                "Credit Override", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                    SalesOrderService.Result r = service.confirm(o.getId());
                    String message = o.getOrderNo() + " is now " + r.order.getStatus() + ".\n"
                            + "Exposure: " + App.money(r.exposure) + " of " + App.money(r.creditLimit) + " limit"
                            + (r.backorderTotal > 0
                                ? "\nBackordered: " + r.backorderTotal + " unit(s)" : "\nFully allocated");
                    if (r.order.getHoldReason() != null) message += "\n" + r.order.getHoldReason();
                    info(message);
                    break;
                case "Close":
                    if (JOptionPane.showConfirmDialog(this, "Close " + o.getOrderNo() + "? This is final.",
                            "Close Order", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                    service.close(o.getId());
                    info("Order " + o.getOrderNo() + " closed.");
                    break;
                case "Delete":
                    if (JOptionPane.showConfirmDialog(this, "Delete " + o.getOrderNo() + "?",
                            "Delete Order", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
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
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            refresh();
        }
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Sales Order", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDetails() {
        SalesOrderModel selected = selected();
        if (selected == null) {
            info("Select an order first.");
            return;
        }
        SalesOrderModel o = service.find(selected.getId());
        if (o == null) {
            JOptionPane.showMessageDialog(this, "Order not found.", "Not Found",
                    JOptionPane.ERROR_MESSAGE);
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
          .append("Delivery   : ").append(o.getRequestedDeliveryDate() == null ? "-" : o.getRequestedDeliveryDate()).append('\n')
          .append("Instr.     : ").append(nz(o.getDeliveryInstructions())).append('\n')
          .append("Status     : ").append(o.getStatus()).append('\n');
        if (o.getHoldReason() != null) {
            sb.append("Note       : ").append(o.getHoldReason()).append('\n');
        }
        sb.append("\n")
          .append(String.format("%-14s %-28s %5s %5s %12s %10s%n",
                  "Item", "Description", "Qty", "UOM", "Unit", "Line Total"));
        for (SalesOrderLineModel l : o.getLines()) {
            sb.append(String.format("%-14s %-28s %5d %5s %12s %10s%n",
                    l.getProductCode(), l.getProductName(), l.getQuantity(),
                    nz(l.getUom()), l.getUnitPrice(), l.getLineTotal()));
            if (l.getBackorderQty() > 0) {
                sb.append("   allocated ").append(l.getAllocatedQty())
                  .append(", backorder ").append(l.getBackorderQty()).append('\n');
            }
        }
        sb.append("\nTOTAL: ").append(App.money(o.getTotalAmount()));

        JTextArea ta = new JTextArea(sb.toString(), 18, 80);
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
                "Sales Order " + o.getOrderNo(), JOptionPane.PLAIN_MESSAGE);
    }

    private static String nz(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    static class OrderTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "SO Number", "Date", "Customer PO", "Customer", "Total", "Status"};
        private final List<SalesOrderModel> rows = new ArrayList<>();

        void setOrders(List<SalesOrderModel> list) {
            rows.clear();
            if (list != null) rows.addAll(list);
            fireTableDataChanged();
        }

        SalesOrderModel orderAt(int r) {
            return (r < 0 || r >= rows.size()) ? null : rows.get(r);
        }

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            SalesOrderModel o = rows.get(r);
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
}
