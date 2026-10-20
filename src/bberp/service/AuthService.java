package bberp.service;

import com.zaxxer.hikari.HikariDataSource;
import bberp.common.AppSession;
import bberp.dao.EmployeeDao;
import bberp.model.EmployeeModel;

public class AuthService {
    private final EmployeeDao employeeDao;

    public AuthService(HikariDataSource ds) {
        this.employeeDao = new EmployeeDao(ds);
    }

    /** Logs the user in, or throws IllegalArgumentException with a user-facing message. */
    public EmployeeModel login(String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Enter username and password.");
        }
        EmployeeModel user = employeeDao.findByCredentials(username, password);
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        AppSession.login(user);
        return user;
    }

    public void logout() {
        AppSession.logout();
    }
}
