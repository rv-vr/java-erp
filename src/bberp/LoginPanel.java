package bberp;

import bberp.service.AuthService;
import com.zaxxer.hikari.HikariDataSource;

public class LoginPanel extends javax.swing.JPanel {

    private App app;
    private HikariDataSource ds;
    private AuthService auth;

    /** Required by the NetBeans GUI Builder. */
    public LoginPanel() {
        initComponents();
    }

    public LoginPanel(App app, HikariDataSource ds) {
        this();
        connect(app, ds);
    }

    /** Wiring kept outside the generated code so the form can be edited safely. */
    public void connect(App app, HikariDataSource ds) {
        this.app = app;
        this.ds = ds;
        this.auth = new AuthService(ds);
    }

    private void submit() {
        lblError.setText("");
        try {
            auth.login(txtUsername.getText().trim(), new String(txtPassword.getPassword()));
            app.go(new MenuPanel(app, ds));
        } catch (IllegalArgumentException ex) {
            lblError.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            lblError.setText("Database unavailable.");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        btnLogin = new bberp.ui.FlatButton();
        sidebar = new bberp.Sidebar();
        lblWelcome = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblError = new javax.swing.JLabel();
        lblVersion = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());

        btnLogin.setBackground(new java.awt.Color(15, 127, 75));
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("Log in");
        btnLogin.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 12);
        add(btnLogin, gridBagConstraints);

        sidebar.setCentered(true);
        sidebar.setPreferredSize(new java.awt.Dimension(109, 240));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(sidebar, gridBagConstraints);

        lblWelcome.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblWelcome.setText("Welcome User!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 8, 12);
        add(lblWelcome, gridBagConstraints);

        lblUsername.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblUsername.setText("Username");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 2, 12);
        add(lblUsername, gridBagConstraints);

        txtUsername.setBackground(new java.awt.Color(243, 243, 243));
        txtUsername.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtUsername.setPreferredSize(new java.awt.Dimension(283, 24));
        txtUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsernameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 12);
        add(txtUsername, gridBagConstraints);

        lblPassword.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblPassword.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 2, 12);
        add(lblPassword, gridBagConstraints);

        txtPassword.setBackground(new java.awt.Color(243, 243, 243));
        txtPassword.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtPassword.setPreferredSize(new java.awt.Dimension(283, 24));
        txtPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPasswordActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 16, 12);
        add(txtPassword, gridBagConstraints);

        lblError.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblError.setForeground(new java.awt.Color(255, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 12);
        add(lblError, gridBagConstraints);

        lblVersion.setFont(new java.awt.Font("SansSerif", 0, 9)); // NOI18N
        lblVersion.setText("BB Bricks ERP v0.0.1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(lblVersion, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        submit();
    }//GEN-LAST:event_btnLoginActionPerformed

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        submit();
    }//GEN-LAST:event_txtUsernameActionPerformed

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPasswordActionPerformed
        submit();
    }//GEN-LAST:event_txtPasswordActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private bberp.ui.FlatButton btnLogin;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JLabel lblWelcome;
    private bberp.Sidebar sidebar;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
