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
        java.awt.GridBagConstraints gridBagConstraints;

        sidebar = new bberp.Sidebar();
        pnlContent = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSignedIn = new javax.swing.JLabel();
        btnSalesOrders = new bberp.ui.FlatButton();
        btnLogout = new bberp.ui.FlatButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(sidebar, gridBagConstraints);

        pnlContent.setBackground(new java.awt.Color(255, 255, 255));
        pnlContent.setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lblTitle.setText("Main Menu");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlContent.add(lblTitle, gridBagConstraints);

        lblSignedIn.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblSignedIn.setText("Signed in as guest");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        pnlContent.add(lblSignedIn, gridBagConstraints);

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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlContent.add(btnSalesOrders, gridBagConstraints);

        btnLogout.setBackground(new java.awt.Color(243, 243, 243));
        btnLogout.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnLogout.setPreferredSize(new java.awt.Dimension(200, 32));
        btnLogout.setText("Log out");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        pnlContent.add(btnLogout, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 24, 24, 24);
        add(pnlContent, gridBagConstraints);
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
