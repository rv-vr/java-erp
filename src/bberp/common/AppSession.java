package bberp.common;

import bberp.model.EmployeeModel;

public final class AppSession {
    private static EmployeeModel user;

    private AppSession() {}

    public static void login(EmployeeModel u) { user = u; }
    public static EmployeeModel user()        { return user; }
    public static int userId()                { return user == null ? 0 : user.getId(); }
    public static void logout()               { user = null; }
}
