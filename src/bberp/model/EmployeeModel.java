package bberp.model;

public class EmployeeModel {
    private final int id;
    private final String name;
    private final String username;

    public EmployeeModel(int id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
    }

    public int getId()         { return id; }
    public String getName()    { return name; }
    public String getUsername() { return username; }
}
