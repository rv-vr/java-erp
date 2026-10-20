package bberp;

import bberp.common.AppSession;
import bberp.service.AuthService;
import com.zaxxer.hikari.HikariDataSource;

public class MenuPanel extends javax.swing.JPanel {

    private App app;
    private HikariDataSource ds;
    private AuthService auth;

    /** Required by the NetBeans GUI Builder. */
    public MenuPanel() {
        initComponents();
        lblSignedIn.setText("Signed in as "
                + (AppSession.user() == null ? "guest" : AppSession.user().getName()));
    }

    public MenuPanel(App app, HikariDataSource ds) {
        this();
        connect(app, ds);
    }

    /** Wiring kept outside the generated code so the form can be edited safely. */
    public void connect(App app, HikariDataSource ds) {
        this.app = app;
        this.ds = ds;
        this.auth = new AuthService(ds);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidebar = new bberp.Sidebar();
        pnlContent = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSignedIn = new javax.swing.JLabel();
        btnSalesOrders = new bberp.ui.FlatButton();
        btnLogout = new bberp.ui.FlatButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());

        sidebar.setPreferredSize(new java.awt.Dimension(109, 480));
        add(sidebar, new java.awt.GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, java.awt.GridBagConstraints.CENTER, java.awt.GridBagConstraints.VERTICAL, new java.awt.Insets(0, 0, 0, 0), 0, 0));

        pnlContent.setBackground(new java.awt.Color(255, 255, 255));
        pnlContent.setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lblTitle.setText("Main Menu");
        pnlContent.add(lblTitle, new java.awt.GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 0, 4, 0), 0, 0));

        lblSignedIn.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblSignedIn.setText("Signed in as guest");
        pnlContent.add(lblSignedIn, new java.awt.GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 0, 16, 0), 0, 0));

        btnSalesOrders.setBackground(new java.awt.Color(15, 127, 75));
        btnSalesOrders.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnSalesOrders.setForeground(new java.awt.Color(255, 255, 255));
        btnSalesOrders.setPreferredSize(new java.awt.Dimension(200, 32));
        btnSalesOrders.setText("Sales Orders");
        btnSalesOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalesOrdersActionPerformed(evt);
            }
        });
        pnlContent.add(btnSalesOrders, new java.awt.GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 0, 8, 0), 0, 0));

        btnLogout.setBackground(new java.awt.Color(243, 243, 243));
        btnLogout.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(0, 0, 0));
        btnLogout.setPreferredSize(new java.awt.Dimension(200, 32));
        btnLogout.setText("Log out");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        pnlContent.add(btnLogout, new java.awt.GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.WEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 0, 0));

        add(pnlContent, new java.awt.GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, java.awt.GridBagConstraints.NORTHWEST, java.awt.GridBagConstraints.NONE, new java.awt.Insets(24, 24, 24, 24), 0, 0));
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalesOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalesOrdersActionPerformed
        app.go(new SalesOrderListPanel(app, ds));
    }//GEN-LAST:event_btnSalesOrdersActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        auth.logout();
        app.go(new LoginPanel(app, ds));
    }//GEN-LAST:event_btnLogoutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private bberp.ui.FlatButton btnLogout;
    private bberp.ui.FlatButton btnSalesOrders;
    private javax.swing.JLabel lblSignedIn;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlContent;
    private bberp.Sidebar sidebar;
    // End of variables declaration//GEN-END:variables
}
