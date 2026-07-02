# Strategy Pattern — Notes & Q&A

Notes for [Strategy.java](Strategy.java): swap an object's **behaviour at runtime** by handing it a different *strategy* object, instead of branching with `if/else`. Example: a **subscription** whose feature access is decided by which plan object you inject — `Free`, `Pro`, or `Max`.

This is a **behavioural** pattern (about *how objects interact / choose behaviour*, unlike the structural patterns Adapter/Decorator/Bridge/etc.).

---

## Why we needed it — the problem

Behaviour depends on some choice (the plan), so the naive version is an `if/else` (or `switch`) that you must **edit every time a plan is added**:

```java
void featuresAccess(String plan){
    if (plan.equals("Free"))      System.out.println("basic access");
    else if (plan.equals("Pro"))  System.out.println("basic + extra");
    else if (plan.equals("Max"))  System.out.println("all features");
    // ❌ add "Enterprise" → crack open this method AGAIN and add a branch
}
```

Every new plan means modifying existing, tested code — and the branching logic gets copy-pasted wherever the decision is needed.

## How it solves the problem

Make each behaviour its own class behind a common interface, and let the context **delegate** to whichever one it's given:

```java
new SubscriptionDecider(new Max()).featuresAccess();   // behaves as Max
new SubscriptionDecider(new Pro()).featuresAccess();   // behaves as Pro
```

Adding `Enterprise` = writing **one new class** — no existing code touched. The `if/else` is replaced by **polymorphism**.

---

## The idea

> **Encapsulate each behaviour as an interchangeable object**, and let a context hold one and delegate to it — so the behaviour can be chosen (and swapped) at runtime.

**Payment analogy:** a checkout says "pay" — the *how* (card, UPI, wallet) is a strategy object you plug in. Checkout doesn't care which; it just calls `pay()`.

```
client ──picks──▶ SubscriptionDecider ──delegates──▶ Subscription (Free | Pro | Max)
 (new Max())        (the context)                      (the chosen strategy)
```

---

## The 4 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Strategy** (interface) | `Subscription` | the common contract (`features()`) |
| **Concrete Strategies** | `Free`, `Pro`, `Max` | interchangeable behaviours |
| **Context** | `SubscriptionDecider` | holds a `Subscription`, delegates to it |
| **Client** | `Strategy.main()` | chooses which strategy to inject |

```
Strategy.main()
   └─▶ new SubscriptionDecider(new Max())
          └─ featuresAccess()  →  access.features()   // delegates to the injected strategy
```

---

## UML class diagram

```
                 ┌────────────────────────┐
                 │ «interface» Subscription│
                 │ + features(): void      │
                 └───────────▲─────────────┘
              realizes ╱      │      ╲ realizes
                      ╱       │       ╲
              ┌───────┐  ┌───────┐  ┌───────┐
              │ Free  │  │  Pro  │  │  Max  │
              │+feat()│  │+feat()│  │+feat()│
              └───────┘  └───────┘  └───────┘
                         ▲ (holds one)
                         ◇ aggregation
                 ┌────────────────────────────┐
                 │     SubscriptionDecider     │
                 │ - access: Subscription      │
                 │ + featuresAccess(): void    │
                 └────────────────────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | `Free`/`Pro`/`Max` → `Subscription` | dashed, hollow triangle ▷ | each `implements Subscription` |
| **Aggregation** | `SubscriptionDecider` ◇→ `Subscription` | hollow diamond ◇ on the context side | the strategy is **passed in** via the constructor (created in `main`, not inside the decider) → aggregation, not composition |

> **Why aggregation:** the strategy (`new Max()`) is created **outside** and handed in. Destroy the `SubscriptionDecider` and the `Max` object can still live on → the part outlives the whole → aggregation ◇, never composition ◆.

---

## SOLID — Strategy is a poster child

> Note: **Singleton is *not* SOLID** — it's a separate creational pattern. The **S** in SOLID is **Single Responsibility**.

| SOLID | Followed? | Why, in this code |
|---|---|---|
| **S** — Single Responsibility | ✅ | each `Free`/`Pro`/`Max` owns one behaviour; the decider only delegates |
| **O** — Open/Closed | ✅ **headline** | add an `Enterprise` class → **no existing code edited** |
| **L** — Liskov Substitution | ✅ | any `Subscription` substitutes for the interface; the decider works with all |
| **I** — Interface Segregation | ✅ | `Subscription` has one method — nothing implements what it doesn't use |
| **D** — Dependency Inversion | ✅ **headline** | the decider depends on the `Subscription` **abstraction**, and concretes are **injected** |

**Open/Closed — the reason it beats `if/else`:**
```java
class Enterprise implements Subscription {          // NEW class...
    public void features(){ System.out.println("Dedicated support + SLA"); }
}
new SubscriptionDecider(new Enterprise()).featuresAccess();   // ...nothing else changed
```

**Dependency Inversion — who depends on whom:**
```java
Subscription access;   // context depends on the INTERFACE, not Free/Pro/Max
```
High-level (`SubscriptionDecider`) and low-level (`Max`) both depend on the **abstraction** — that's the inversion.

---

## Strategy vs Bridge — identical shape, different intent

Both are "a context holding an interface reference and delegating," so they look the same in code. The tell is **why**:

| | **Strategy** (this) | **Bridge** |
|---|---|---|
| Family | behavioural | structural |
| Swaps | **one algorithm/behaviour** | an **implementation** under an abstraction |
| Intent | choose *what it does* at runtime | let two dimensions (abstraction × implementation) vary independently |
| Typically set | swapped at runtime | once, at construction |

> Same `has-a + delegate` mechanic → people say "Strategy and Bridge have the same UML." The difference is purpose, not structure.

---

## Common notes & lessons

### 1. Set-at-construction vs swap-at-runtime (optional enhancement)
The strategy is currently set only in the constructor. Textbook Strategy often adds a **setter** so you can change behaviour on the *same* context:
```java
void setAccess(Subscription access){ this.access = access; }
```
```java
SubscriptionDecider d = new SubscriptionDecider(new Free());
d.featuresAccess();          // Free
d.setAccess(new Max());      // swap at runtime, same object
d.featuresAccess();          // Max
```
Still valid Strategy either way — this just makes the "changeable at runtime" aspect explicit.

### 2. Depend on the interface, inject the concrete
The whole benefit comes from `SubscriptionDecider` referencing `Subscription` (not `Max`) and receiving the concrete from outside. Hardcoding `this.access = new Max()` inside would throw away OCP/DIP and the runtime flexibility.

---

## When to use Strategy

- You have **several interchangeable behaviours/algorithms** for one job (sorting, pricing, payment, compression…).
- You want to **pick or swap** the behaviour at runtime.
- You're replacing a growing **`if/else`/`switch`** on a "type" with polymorphism.

> The win: add a behaviour by adding a class (Open/Closed), and choose it at runtime by injecting it (Dependency Inversion).

---

## Run it

```bash
cd "Strategy Pattern"
javac Strategy.java
java Strategy
```

Output:
```
User get all features  access to platform
User get basic + some additoonal  access to platform
```

The first line is `Max`'s behaviour, the second is `Pro`'s — same `SubscriptionDecider`, different injected strategy.
