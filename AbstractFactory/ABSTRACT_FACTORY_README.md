# Abstract Factory Pattern — Interview Notes

Notes for [AbstractFactory.java](AbstractFactory.java): a `CheckoutService` that creates a **matching family** of objects (payment gateway + invoice) per region, without hardcoding concrete classes.

---

## One-line idea

> **A factory that creates a *family* of related objects meant to work together** — without the client knowing the concrete classes.

Think **"factory of matching sets."**

---

## The analogy

Furnishing a room — everything must match:

- **Modern set** → modern chair + modern sofa
- **Victorian set** → Victorian chair + Victorian sofa

You never want a modern chair next to a Victorian sofa. Abstract Factory says *"give me the Modern set"* and you get a factory that **only** produces matching pieces.

In this example the "matching set" is **a gateway + its correct invoice**:

- **India set** → Razorpay/PayU gateway + GST invoice
- **US set** → PayPal/Stripe gateway + US invoice

---

## The problem it solves

Bad design hardcodes object creation inside business logic:

```java
public void checkOut(double amount) {
    if (gatewayType.equals("razorpay")) paymentGateway = new RazorpayGateway();
    else                                paymentGateway = new PayUGateway();
    paymentGateway.processPayment(amount);
    Invoice invoice = new GSTInvoice();   // always GST — hardcoded
    invoice.generateInvoice();
}
```

| Problem | Meaning |
|---|---|
| **Tight coupling** | `CheckoutService` knows exact classes (`RazorpayGateway`, `GSTInvoice`). |
| **Breaks Open/Closed** | Adding the US means editing `checkOut()` with more `if/else`. |
| **No matching guarantee** | Nothing stops a US gateway + GST invoice mismatch. |

Core sin: **business logic is doing object creation.** Those are two different jobs.

---

## The solution — extract creation into per-region factories

```java
// The abstract factory: promises a WHOLE SET (gateway + matching invoice)
interface RegionFactory {
    PaymentGateway createPaymentGateway(String gatewayType);
    Invoice createInvoice();
}

class IndiaFactory implements RegionFactory {
    public PaymentGateway createPaymentGateway(String type) { /* razorpay | payu */ }
    public Invoice createInvoice() { return new GSTInvoice(); }   // fixed → always matches India
}

class USFactory implements RegionFactory {
    public PaymentGateway createPaymentGateway(String type) { /* paypal | stripe */ }
    public Invoice createInvoice() { return new USInvoice(); }    // fixed → always matches US
}
```

`CheckoutService` now receives a factory and never knows which region:

```java
public CheckoutService(RegionFactory factory, String gatewayType) {
    this.paymentGateway = factory.createPaymentGateway(gatewayType);
    this.invoice        = factory.createInvoice();
}
public void completeOrder(double amount) {
    paymentGateway.processPayment(amount);   // no new, no if/else — just USES the objects
    invoice.generateInvoice();
}
```

---

## Structure

```
RegionFactory (abstract factory)         "I promise a gateway + an invoice"
   ├── IndiaFactory  → Razorpay/PayU + GSTInvoice   (matching India set)
   └── USFactory     → PayPal/Stripe + USInvoice    (matching US set)

CheckoutService  ← takes ANY RegionFactory, never knows which one
```

The invoice has **no parameter** (fixed per region), so you can choose the gateway freely but the invoice is locked to the correct region. A US gateway + GST invoice mismatch is **impossible**.

---

## This example combines TWO patterns

| Layer | Pattern | Picks |
|---|---|---|
| `RegionFactory` (India vs US) | **Abstract Factory** | which *family / region* |
| `createPaymentGateway(type)` (razorpay vs payu) | **Factory Method** | which *gateway within* that region |

The `if/else` inside `createPaymentGateway` is a contained Factory Method — messy creation logic stays **inside the factory**, not in `CheckoutService`.

---

## Factory Method vs Abstract Factory

| | Factory Method | Abstract Factory |
|---|---|---|
| **Makes** | **1** product | a **family** (2+ matching products) |
| **How many factories** | usually one | **one per family** (India, US...) |
| **Question answered** | "which gateway?" | "which whole **set** — gateway + invoice?" |
| **Guarantees matching?** | n/a | yes — India factory *can't* make a US invoice |
| **Methods on factory** | one `create()` | many — `createPaymentGateway()`, `createInvoice()` |

> **Factory Method** = vending machine → press a button, get **one** item.
> **Abstract Factory** = combo meal → order "India combo," get a **matching set** that all goes together.

---

## When to use Abstract Factory

- Multiple related objects must be created as a cohesive set (gateway **+** invoice).
- The objects to instantiate depend on a context (country, theme, platform).
- Client code must stay independent of concrete product classes.
- Consistency across a family must be guaranteed (US gateway paired with US invoice).

---

## Interview one-liners

- *What kind of pattern?* Creational.
- *What problem?* Creating families of matching objects without hardcoding concrete classes.
- *vs Factory Method?* Factory Method makes one product; Abstract Factory makes a matching family.
- *How does it keep business logic clean?* Object creation moves into factory classes; `CheckoutService` only uses the objects.
- *How does it follow Open/Closed?* Add a new region = add a new factory class; no existing code changes.

---

## Run it

```bash
cd AbstractFactory
javac AbstractFactory.java
java Main
```

Output:
```
Processing INR payment via Razorpay: 1999.0
Generating GST Invoice for India.
---
Processing USD payment via PayPal: 49.99
Generating Invoice as per US norms.
```
