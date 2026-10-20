**Business Case:** BB Bricks (Manufacturer of clay bricks, cement, and hollow blocks)   
**Submodule:** Sales Order (SO)

## **Example Business Scenario**

ABC Construction Corp. (a corporate customer) is building a commercial complex. They recently accepted a Sales Quotation (SQ) for 500 tons of cement at a locked-in price of \$100/ton. They send their Purchase Order (PO) to BB Bricks. The Sales Admin creates a Sales Order (SO) in the system, referencing the SQ and PO.

During the process, the system checks ABC Construction’s credit limit. They have a \$1,000,000 limit and currently owe \$800,000 in unpaid invoices. The new SO value is \$50,000. The system calculates their total exposure (\$850,000) and approves the credit. Next, the system checks inventory. BB Bricks only has 300 tons available in the silo. The system allocates the 300 tons and flags the remaining 200 tons as a **Backorder**. The SO is confirmed for the available stock, and the warehouse receives a fulfillment queue to begin generating Delivery Orders (DOs) for the first 300 tons.

---

### **1\. Sales Order Workflow (Process)**

* **Prerequisite & Order Initiation**

  The customer accepts a previously issued Sales Quotation (SQ) and issues a Purchase Order (PO). The Sales Admin creates a new Sales Order (SO) in the system, explicitly referencing the SQ\_Number and the Customer\_PO\_Number to maintain the audit trail.

* **Data Entry (Header & Lines)**

  The admin inputs the customer details, line-item quantities, agreed pricing (inherited from the SQ), and selects the Payment\_Terms\_Code from pre-set templates (e.g., Net 30, 50% Downpayment, COD). Specific delivery details (project site address, delivery instructions) are also entered.

* **System Validation (Credit & Inventory Check)**

  **Credit Check:** The system calculates the Total\_Exposure (Current AR Balance \+ New SO Value) and compares it to the customer's Credit\_Limit. If exceeded, the SO is placed on **"Credit Hold"**.

  * **Inventory Check:** The system checks Available\_Stock\_ATP (Available-to-Promise). If stock is insufficient, the system calculates the Allocated\_Quantity and flags the unfulfilled items as **"Backorder"**.  
* **Process 4: Order Confirmation:** If validations pass (or are overridden by a manager), the SO status changes to **"Confirmed"**. The system locks in the pricing, soft-allocates the inventory, and pushes the order to the warehouse/logistics queue.

---

### **2\. Assumed Inputs (Master Data Variables)**

*These are pulled automatically from other submodules (Customer Management, Item Master, Sales Quotation).*

**From Customer Management:**

* Customer\_ID (String) \- Unique identifier for the B2B account.  
* Customer\_Name (String) \- Legal business name (e.g., "ABC Construction Corp.").  
* Customer\_Type (Enum) \- Single Proprietorship, Partnership, or Corporation.  
* Billing\_Address (String) \- For invoicing.  
* Credit\_Limit (Decimal) \- Maximum allowed outstanding balance.  
* Current\_AR\_Balance (Decimal) \- The customer’s current unpaid invoice total.

**From Item Master:**

* Item\_ID (String) \- SKU (e.g., "CEM-OPC-50KG").  
* Item\_Description (String) \- E.g., "Ordinary Portland Cement 50kg".  
* Base\_UOM (String) \- Base Unit of Measure (e.g., "Tons", "Pieces").  
* Weight\_Per\_UOM (Decimal) \- Crucial for bulk logistics (e.g., 1 Pallet \= 1.5 Tons).  
* Default\_Unit\_Price (Decimal) \- Base price per UOM.

**From Sales Quotation (SQ):**

* SQ\_Number (String) \- Reference ID (e.g., "SQ-2023-001").  
* Quoted\_Unit\_Price (Decimal) \- The locked-in price for this specific customer.  
* SQ\_Validity\_Date (Date) \- To ensure the PO was issued before the quote expired.

---

### **3\. Transactional Inputs (User-Entered Variables)**

*These are the variables the Sales Admin enters or selects when creating the Sales Order.*

**Header Level (General Order Info):**

* SO\_Number (String) \- System-generated unique ID (e.g., "SO-2023-1050").  
* Order\_Date (Date) \- Date the SO is created.  
* Customer\_PO\_Number (String) \- The customer's official PO reference.  
* Reference\_SQ\_Number (String) \- Linked to the assumed SQ.  
* Payment\_Terms\_Code (Enum/Dropdown) \- Set templates: *Net 30, Net 60, 50% Advance, COD, Cash*.  
* Ship\_To\_Address\_ID (String/Dropdown) \- Select the specific project site (e.g., "Project Site A \- Quezon City").  
* Requested\_Delivery\_Date (Date) \- When the customer wants the first batch.  
* Delivery\_Instructions (Text) \- Free text (e.g., "Deliver via Gate 3", "Needs crane for offloading").

**Line-Item Level (Per Item Ordered):**

* Line\_Number (Integer) \- 10, 20, 30...  
* Item\_ID (String) \- Select from Item Master.  
* Order\_Quantity (Decimal) \- E.g., 500\.  
* Order\_UOM (Enum) \- E.g., "Tons", "Pieces".  
* Unit\_Price (Decimal) \- Auto-populated from SQ, but overridable by authorized users.  
* Line\_Total (Decimal) \- Order\_Quantity \* Unit\_Price.

**Payment Terms (Payment\_Terms\_Code):**

The dropdown offers these five templates. "Net N" means the full invoice is due N days after the invoice date; the others are paid before or on delivery.

| Code | Meaning |
| --- | --- |
| **Net 30** | Invoice is due in full 30 days after the invoice date. Default term when none is selected. |
| **Net 60** | Invoice is due in full 60 days after the invoice date. |
| **50% Advance** | Customer pays 50% of the SO value before delivery; the remaining 50% is billed after delivery. |
| **COD (Cash on Delivery)** | Full payment is collected when the goods are delivered, before the truck is released. |
| **Cash** | Full payment is made up front, before the order is prepared or shipped. |

Payment\_Terms\_Code is free of calculation in this module: it is stored on the SO header (`tblsalesorder.PaymentTerms`) and carried into the Delivery Order and invoice. It does not affect the credit check, which uses Total\_Exposure regardless of terms.

---

### **4\. System/Process Variables**

*These are calculated by the ERP system in the background to execute the validations.*

* Total\_SO\_Value (Decimal) \- The sum of all Line Totals \+ Taxes/Delivery Fees.  
* Total\_Exposure (Decimal) \- Current\_AR\_Balance \+ Total\_SO\_Value.  
* Credit\_Check\_Status (Enum) \- **Passed**, **Failed (On Hold)**, or **Override Approved**.  
* Available\_Stock\_ATP (Decimal) \- The system's live Available-to-Promise inventory.  
* Allocated\_Quantity (Decimal) \- The amount successfully reserved for this SO.  
* Backorder\_Quantity (Decimal) \- Order\_Quantity minus Allocated\_Quantity.  
* SO\_Status (Enum) \- *Draft, Pending Approval, Credit Hold, Confirmed, Closed*.

---

### **5\. Submodule Outputs**

* **Confirmed Sales Order (SO) Document:** A formal system-generated document with a unique SO number, serving as the internal authorization for fulfillment.  
* **Inventory Allocation:** The requested items are reserved in the system (Available stock decreases; Committed stock increases).  
* **Credit Hold Alert:** If Credit\_Check\_Status fails, an alert is sent to the Finance/AR team to review or collect payment before proceeding.  
* **Backorder Notification:** If Backorder\_Quantity \> 0, an alert is routed to Procurement/Production to replenish stock.  
* **Fulfillment Queue Trigger:** The confirmed SO is pushed to the Logistics/Warehouse dashboard to begin generating Delivery Orders (DOs) and scheduling truck loading.

