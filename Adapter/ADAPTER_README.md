# Adapter Pattern — Notes & Q&A

Notes for [Adaptor.java](Adaptor.java): make an incompatible class (`payPhone`) work with a client that expects a different interface (`paymentGateway`), without changing either one.

This is the first **structural** pattern (Builder/Factory/Prototype were *creational*, about *making* objects; structural patterns are about *connecting* them).

---

## The idea

> **A wrapper that translates one interface into another**, so two incompatible classes can work together — without modifying either.

**Plug-adapter analogy:** an Indian charger doesn't fit a European socket. You don't rewire the wall or buy a new charger — you use a **plug adapter** in between. The Adapter Pattern is that plug adapter, for code.

```
Indian charger → [plug adapter] → European socket
   (Adaptee)        (Adapter)        (what's expected = Target)
```

---

## The 4 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Target** | `paymentGateway` interface | the language the client speaks (`type()`, `sendMoney()`) |
| **Client** | `choosingPaymentGateway` | uses the Target; doesn't care who implements it |
| **Adaptee** | `payPhone` | the incompatible class — has `print()` & `deductMoney()` (different names) |
| **Adapter** | `payPhoneHandler` | the translator — implements Target, wraps Adaptee |

```
choosingPaymentGateway ──▶ paymentGateway ◀──implements── payPhoneHandler ──wraps──▶ payPhone
      (Client)                (Target)                       (Adapter)                 (Adaptee)
```

---

## The two key mechanics of an Adapter

```java
class payPhoneHandler implements paymentGateway {   // ① IMPLEMENTS the target (client accepts it)
    public payPhone payphone;                        // ② HAS the adaptee (wraps it)

    payPhoneHandler(int balance) {
        payphone = new payPhone(balance);
    }
    public void type() {
        payphone.print();                            // ③ translate type() -> print()
    }
    public int sendMoney(int money) {
        return payphone.deductMoney(money);          // ③ translate sendMoney() -> deductMoney()
    }
}
```

**"Implements one, wraps the other."** The client calls `sendMoney()`; the adapter quietly forwards it to the adaptee's `deductMoney()`. The client has no idea `payPhone` is behind the curtain.

---

## Why not just change `payPhone`?

- It's often a **third-party class** — you don't own the source.
- Changing it could **break other code** that already uses it.

So the adapter adds compatibility **without touching either side**.

---

## Problem it solves

- **Interface incompatibility** — same functionality, different method names/shapes.
- **Reuse existing classes** without modifying their source.
- Lets systems that otherwise couldn't (different method signatures) **talk to each other**.

---

## Common errors & lessons (hit while building this file)

### 1. `static` on a top-level class

```java
static class paytm implements paymentGateway { ... }   // ❌ illegal
```
`static` is only allowed on **nested** classes (inside another class). `paytm` is top-level → remove `static`.

| Class location | `static` allowed? |
|---|---|
| Top-level (directly in file) | ❌ no |
| Nested (inside another class) | ✅ yes |

### 2. Checked exception in `sendMoney()`

`throw new Exception(...)` is **checked** — the method/interface would have to declare `throws`. Use an **unchecked** exception instead:
```java
throw new RuntimeException("Balance deficit");
```

### 3. `main` floating outside any class

```java
public static void main(String[] str){ ... }   // ❌ not inside a class
```
Every method must live inside a class. Wrap it: `public class Adaptor { public static void main(...) { } }`.

### 4. Missing `return` in `sendMoney()`

```java
public int sendMoney(int money){
    payphone.deductMoney(money);     // ❌ returns int but nothing returned
}
```
A method declared `int` must return on every path:
```java
return payphone.deductMoney(money);  // ✅
```

### 5. Run button failed (duplicate class names)

All files here have **no `package` line** → they share the **default package**. Two classes were defined twice in it (`builder`, `Main`), so VS Code's Java extension (which builds the whole project) reported duplicate-class errors and refused to run anything — even unrelated files. Terminal worked because `javac Adaptor.java` compiles only one file.
**Fix:** keep every class name **unique across the whole repo**; avoid generic names like `Main`.

### 6. `java Adapter.Adaptor` → "Could not find or load main class"

```
wrong name: Adaptor
```
The file has **no `package` line**, so the class is just `Adaptor` — the **folder** name (`Adapter`) is irrelevant to Java. Run it as:
```bash
cd Adapter
javac Adaptor.java
java Adaptor            # no "Adapter." prefix
```
> `java <name>` must match `package + class`. No package line → just the class name; the folder doesn't count.

---

## Connection to earlier patterns

`choosingPaymentGateway` depends on the **`paymentGateway` interface**, not concrete classes — that's **"program to an interface"** / **Dependency Inversion (D)**. This is *why* the adapter slots in: the client only cares about the interface, so any implementation (including an adapter) works.

---

## Trick: association vs aggregation vs composition (UML)

The fastest way to tell the four "has/uses" relationships apart — **two questions, in order**:

```
Q1: Is it stored as a FIELD?
     NO  → Dependency      (just used inside a method)
     YES → it's a HAS-A, go to Q2

Q2: WHO created the object?  ("who typed new?")
     created inside the class (new X())  → COMPOSITION ◆
     passed in from outside (parameter)  → AGGREGATION ◇
```

### The code tell — look at the constructor

| What you see | Relationship | UML |
|---|---|---|
| `X x` only as a param/local, no field | **Dependency** (uses) | dashed → |
| field, two equal peers, no whole/part | **Association** (knows) | solid line |
| `this.x = paramX;` (received in) | **Aggregation** (has, shared) | solid + hollow ◇ |
| `this.x = new X();` (created inside) | **Composition** (owns) | solid + filled ◆ |

### Applied to this file

```java
// COMPOSITION ◆ — handler CREATES the payPhone (new inside)
payPhoneHandler(int balance) {
    payphone = new payPhone(balance);
}

// AGGREGATION ◇ — gateway is PASSED IN from outside
public choosingPaymentGateway(paymentGateway s) {
    this.method = s;
}
```

### The "death test" (aggregation vs composition, when unsure)

> "If I destroy the whole, does the part die too?"
- **Yes**, part is meaningless alone → **Composition ◆** (handler's `payPhone` exists only for it)
- **No**, part lived before/after → **Aggregation ◇** (the passed-in gateway is independent)

**Honest note:** association and aggregation are *identical in code* (`private X x;`). The difference is conceptual — aggregation has a clear whole/part; association is just two peers. Don't over-stress that boundary.

### Cheat sheet

```
Not a field?          → Dependency   (uses)        dashed →
Field, peers?         → Association  (knows)       solid line
Field, passed in?     → Aggregation  (has, shared) solid + hollow ◇
Field, created inside? → Composition  (owns)        solid + filled ◆
```

> **Killer trick: find the constructor.** `new` inside = **Composition ◆**. Passed as a parameter = **Aggregation ◇**. No field = **Dependency**.

---

## When to use Adapter

- You want to use an **existing/third-party class** whose interface doesn't match yours.
- You **can't modify** that class (no source, or risky).
- You need **two incompatible systems** to collaborate.

---

## Run it

```bash
cd Adapter
javac Adaptor.java
java Adaptor
```

Output:
```
This is payphone type
```

(`get()` calls `type()`, which the adapter translates to `payPhone.print()`.)
