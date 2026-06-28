# Facade Pattern — Notes & Q&A

Notes for [Facade.java](Facade.java): hide a **complicated multi-step subsystem** behind **one simple method**, so the client makes a single call instead of juggling five classes in the right order.

This is a **structural** pattern (like Adapter & Decorator — about *connecting* objects). Example: a **food-delivery MVP** where ordering food actually means *authenticate → place order → record address → take payment → issue receipt*. The client shouldn't have to know that.

---

## The idea

> **A single entry-point class that wires together a messy subsystem**, so callers deal with one friendly method instead of many objects and a fragile call order.

**Restaurant analogy:** you tell the waiter "one Paneer Tikka." You don't walk into the kitchen and coordinate the chef, the cashier, and the delivery rider yourself. The **waiter is the facade** — one simple interface over a complex kitchen.

```
client ──▶ FoodDeliveryFacade ──▶ { Authentication, FoodOrder, Address, Payment, Receipt }
 (one call)      (the waiter)              (the complex kitchen)
```

---

## The 2 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Facade** | `FoodDeliveryFacade` | exposes one method `FoodOrderDetails(...)`; orchestrates the subsystem in the right order |
| **Subsystem classes** | `Authentication`, `FoodOrder`, `Address`, `Payment`, `Receipt` | do the real work; each unaware of the others |
| **Client** | `Facade.main()` | calls **only** the facade — never touches the subsystem directly |

```
Facade.main()
   └─▶ FoodDeliveryFacade.FoodOrderDetails()
          ├─ auth.AuthenticationRequired() + info()   // 1. log in
          ├─ foodOrder.FoodOrderName() + info()        // 2. order
          ├─ new Address().info()                      // 3. address
          ├─ new Payment().info()                      // 4. pay
          └─ receipt.info()                            // 5. receipt
```

---

## UML class diagram

```
 ┌────────────────────┐ ┌──────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌──────────────┐
 │  Authentication    │ │    FoodOrder     │ │    Address     │ │    Payment     │ │   Receipt    │
 │ - email: String    │ │ - food: String   │ │ - street:String│ │ - cost: int    │ │              │
 │ - password: String │ │                  │ │                │ │                │ │              │
 │ + AuthReq(): void  │ │ + FoodOrderName  │ │ + Address(str) │ │ + Payment(int) │ │ + info():void│
 │ + info(): void     │ │ + info(): void   │ │ + info(): void │ │ + info(): void │ │              │
 └─────────┬──────────┘ └────────┬─────────┘ └───────┬────────┘ └───────┬────────┘ └──────┬───────┘
           │                     │                   │                  │                 │
           ◆ composition         ◆                   ◆                  ◆                 ◆
           │  (facade `new`s     │                   │                  │                 │
           │   each part)        │                   │                  │                 │
           └─────────────────────┴─────────┬─────────┴──────────────────┴─────────────────┘
                                            │
                              ┌─────────────────────────────┐
                              │      FoodDeliveryFacade      │
                              │ - auth: Authentication       │
                              │ - foodOrder: FoodOrder       │
                              │ - address: Address           │
                              │ - payment: Payment           │
                              │ - receipt: Receipt           │
                              │ + FoodOrderDetails(...): void │
                              └─────────────────────────────┘
```

**The one relationship type here — Composition ◆:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Composition** | `FoodDeliveryFacade` ◆→ each subsystem | filled diamond ◆ on the **facade** side | the facade `new`s every part **inside itself** (none passed in) → created inside = composition |

> **The "who typed `new`?" test:** every subsystem object is built with `new` *inside* the facade (`new Authentication()`, `new Address(street)`, …). Created inside → **Composition ◆** (filled diamond), not aggregation (hollow ◇, which is for parts *passed in* from outside). Death test: destroy the facade → all five parts die with it. ✔
>
> Unlike Decorator (which had **two** links to its interface — is-a + has-a), the Facade has **no** inheritance/realization — it just **owns** the subsystem objects. One arrow type, five times.

---

## The key mechanic — one method hides the sequence

```java
class FoodDeliveryFacade {
    private Authentication auth;
    private FoodOrder foodOrder;
    private Address address;
    private Payment payment;
    private Receipt receipt;

    // the client calls THIS and nothing else
    public void FoodOrderDetails(String email, String password,
                                 String food, String street, int cost){
        auth.AuthenticationRequired(email, password);  auth.info();   // 1
        foodOrder.FoodOrderName(food);                 foodOrder.info(); // 2
        this.address = new Address(street);            address.info(); // 3
        this.payment = new Payment(cost);              payment.info(); // 4
        receipt.info();                                                // 5
    }
}
```

**The whole 5-step dance lives inside the facade.** The client just writes:
```java
new FoodDeliveryFacade().FoodOrderDetails("raj@gmail.com","secret123","Paneer Tikka","MG Road",350);
```

---

## Facade vs Adapter vs Decorator (the structural trio so far)

| Pattern | Wraps… | Goal |
|---|---|---|
| **Adapter** | one class | **change** its interface so it fits a client |
| **Decorator** | one object (same type) | **add** behaviour, stackable |
| **Facade** | a **whole subsystem** (many classes) | **simplify** — one door over many |

> Key tell: Adapter & Decorator wrap **one** thing; **Facade wraps many** and gives you a simpler combined API.

---

## Common errors & lessons (hit while building this file)

### 1. Calling a constructor that doesn't exist

```java
this.foodOrder = new FoodOrder("");   // ❌ FoodOrder has no String constructor
```
`FoodOrder` only has a default constructor plus a **method** `FoodOrderName(String)`. Either add a matching constructor, or do it in two steps:
```java
this.foodOrder = new FoodOrder();     // ✅
foodOrder.FoodOrderName(food);        // set the value via the method
```

### 2. Declared a field but never created it

`private Address address;` was declared but `address` was never `new`-ed, so calling `address.info()` would throw `NullPointerException`. Fix: create it where you have the data —
```java
this.address = new Address(street);
```

### 3. Method cut off / missing parameter

`FoodOrderDetails` was unfinished and had **no `street` parameter**, so `Address` could never receive data. Added `String street` to the signature so the facade can build every subsystem object.

### 4. No `main` / empty `Facade` class

The public `Facade` class was empty — nothing ran. Added a `public static void main` that calls only the facade (proving the client never touches the subsystem).

### 5. Missing space in output

```java
System.out.println(street + "your address is recorded");  // "MG Roadyour address..."
```
String concatenation doesn't add spaces — fixed to `street + " — your address is recorded"`.

---

## When to use Facade

- A subsystem is **complex** and clients only need a **common, simple** slice of it.
- You want to **decouple** clients from subsystem classes (change internals freely without breaking callers).
- You want a clear **entry point** / layering (e.g. service layer over many helpers).

> Facade doesn't *hide* the subsystem (you can still use the classes directly) — it just offers an **easier default door**.

---

## Run it

```bash
cd Facade
javac Facade.java
java Facade
```

Output:
```
User logged in
Paneer Tikka is ordered.
MG Road — your address is recorded
Payment done
Receipt is here
```
