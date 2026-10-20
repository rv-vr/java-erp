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

The four screens are NetBeans form pairs (`XPanel.java` + `XPanel.form`) laid out
with `GridBagLayout`, so they open in the GUI Builder's Design view. Non-generated
wiring lives in `connect(App, HikariDataSource)` and outside the `GEN` blocks.

## Workflow and business rules (`SalesOrderService`)

`Draft → Pending Approval → Confirmed | Credit Hold → Closed`

- **Draft** — Save Order. Requires a customer, Customer PO Number, and at least
  one line with a positive quantity. No stock is committed.
- **Submit** — Draft → Pending Approval.
- **Confirm** — runs the checks. Exposure = customer AR + other open orders +
  this order, against the credit limit. Over the limit → `Credit Hold`, with no
  stock committed. Otherwise `Confirmed`: each line is allocated against live
  stock, the remainder is stored as `BackorderQty`, and allocated stock is
  deducted.
- **Override** — confirming a `Credit Hold` order approves it despite the credit
  failure; the override is recorded in `HoldReason` as an audit note.
- **Close** — Confirmed → Closed. Final; the order can no longer change.
- **Delete** — allowed for Draft, Pending Approval, and Credit Hold only.
  Confirmed and closed orders are kept for audit. Lines cascade.

The 1-to-many: `tblsalesorder` → `tblsalesorderline` (cascade delete), saved in one
transaction by `SalesOrderDao.save`; confirmation updates the header, all lines,
and stock in one transaction in `SalesOrderDao.confirm`.

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
