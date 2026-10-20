# BB Bricks ERP — Sales Order module

Java 11 + Swing desktop app, layered **UI → Service → DAO → Database** with
HikariCP over MySQL/MariaDB.

## Layout

| Layer | Package | Files |
|---|---|---|
| UI | `bberp` | `App`, `LoginPanel`, `MenuPanel`, `SalesOrderListPanel`, `CreateSoPanel`, `Sidebar` |
| Service | `bberp.service` | `AuthService`, `LookupService`, `SalesOrderService` |
| DAO | `bberp.dao` | `EmployeeDao`, `CustomerDao`, `ProductDao`, `SalesOrderDao` |
| Model | `bberp.model` | `EmployeeModel`, `CustomerModel`, `ProductModel`, `SalesOrderModel`, `SalesOrderLineModel` |
| Infra | `bberp.common` | `DB` (HikariCP pool), `AppSession` |

The 1-to-many: `tblsalesorder` → `tblsalesorderline` (cascade delete), saved in one
transaction by `SalesOrderDao.save`.

## Business rules (`SalesOrderService`)

- Customer PO Number and at least one line are required.
- Credit check: exposure = customer AR + open orders + this order, against the
  credit limit. Over the limit → status `Credit Hold`, no stock committed.
- Inventory check: each line is allocated against live stock; the rest is
  stored as `BackorderQty`. Stock is deducted only when the order is `Confirmed`.

## Database

Requires MySQL 8.0.29+ or MariaDB 10.0.2+.

```bash
mariadb -u root -e "CREATE DATABASE IF NOT EXISTS dbemployee CHARACTER SET utf8mb4"
mariadb -u root dbemployee < Employee/dbemployee.sql   # base tables + seed
mariadb -u root dbemployee < db-bberp.sql              # spec fields + BB Bricks seed
```

Connection settings are read from system properties, then environment
variables, then the defaults in `bberp/common/DB.java`
(`jdbc:mysql://localhost:3306/dbemployee`, `root` / `root`). Override with:

```bash
DB_URL=jdbc:mysql://localhost:3306/dbemployee DB_USER=root DB_PASSWORD=secret java -jar dist/java-erp.jar
```

## Build and run

Needs JDK 11 (the project targets `--release` 11; newer JDKs cannot compile for 11).

```bash
ant clean jar
java -jar dist/java-erp.jar
```

NetBeans: open the folder as a project. If your NetBeans runs on a JDK newer
than 11, add JDK 11 as a Java Platform and select it for the project, or raise
`javac.source` / `javac.target` to match.

Login: `admin` / `admin`.
