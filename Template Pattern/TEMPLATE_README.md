# Template Method Pattern — Notes & Q&A

Notes for [Template.java](Template.java): define the **fixed skeleton of an algorithm** in a base class, and let subclasses fill in **only the steps that vary**. Example: a role-based login flow where every user goes through the same sequence, but `Admin` and `NormalUser` differ in a couple of steps.

This is a **behavioural** pattern (about *how objects behave* — like Strategy & Command).

---

## Why we needed it — the problem

Every role (Admin, Normal user, …) follows the **same sequence** — validate → authenticate → authorize → services → review → logout — but a few steps differ. Without a shared skeleton you either duplicate the whole flow per role, or you branch:

```java
void login(String role){
    validate(); authenticate();
    if (role.equals("Admin"))       { /* admin authorize + services */ }
    else if (role.equals("Normal")) { /* normal authorize + services */ }  // ❌ if/else per role
    review(); logout();
    // add a role → edit this method; get the ORDER wrong → subtle bugs
}
```

The invariant order (validate before authenticate, logout last) gets copy-pasted and can drift, and every new role means editing the flow.

## How it solves the problem

Put the **fixed order in one template method** in the base class; expose only the varying steps as `abstract` (must override) or hooks (optional override):

```java
public void Backend(String email, String pw){   // the template method — the skeleton
    validation(email, pw);      // fixed
    authentication(email, pw);  // fixed
    authorization();            // varies → abstract
    services();                 // varies → abstract
    review();                   // optional → hook
    logout();                   // fixed
}
```

A new role = a new subclass supplying `authorization()`/`services()`. The **order can never drift**, and shared steps live in exactly one place.

---

## The idea

> **The base class owns the algorithm's *structure*; subclasses own the *details*.** The template method calls the steps in a fixed order — some concrete, some abstract, some overridable hooks.

**Recipe analogy:** a "bake" recipe fixes the sequence — prep → mix → *bake at X°* → cool → *decorate*. Every cake follows it; a specific cake only customizes the italicized steps. The recipe (template) guarantees the order.

---

## The 3 kinds of steps (the heart of the pattern)

| Step type | Modifier | In this code | Rule |
|---|---|---|---|
| **Invariant** | `private` | `validation`, `authentication`, `logout` | same for all; **can't** be overridden |
| **Abstract** | `abstract` | `authorization`, `services` | subclass **must** implement |
| **Hook** | `protected` + default | `review` | subclass **may** override (optional) |

> Getting all three right is what makes this a *complete* Template Method. The **hook** (`review`) is the subtle one: `Admin` keeps the default (`"good experience"`), `NormalUser` overrides it (`"bad experience"`).

---

## The 2 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Abstract Class** | `UserRole` | holds the template method `Backend()` + fixed/abstract/hook steps |
| **Concrete Classes** | `Admin`, `NormalUser` | implement the abstract steps (and optionally the hook) |
| **Client** | `Template.main()` | picks a subclass, calls the template method |

```
Template.main()
   └─▶ new Admin().Backend("raj","123457")
          ├─ validation()      (fixed)
          ├─ authentication()  (fixed)
          ├─ authorization()   → Admin's version
          ├─ services()        → Admin's version
          ├─ review()          → default (Admin didn't override the hook)
          └─ logout()          (fixed)
```

---

## UML class diagram

```
   ┌──────────────────────────────────────┐
   │            UserRole (abstract)        │   ← italic name = abstract
   │ + Backend(email, pw): void           │   ← template method (should be final)
   │ - validation(...) / authentication() │   ← private, invariant
   │ - logout()                           │
   │ # review(): void                     │   ← hook (default, overridable)
   │ + authorization(): void {abstract}   │   ← must override
   │ + services(): void {abstract}        │
   └───────────────────▲──────────────────┘
              extends   │   extends
            ┌───────────┴───────────┐
    ┌───────────────┐        ┌───────────────┐
    │     Admin     │        │  NormalUser   │
    │ + authorization│        │ + authorization│
    │ + services     │        │ + services     │
    │ (keeps hook)   │        │ # review (override)│
    └───────────────┘        └───────────────┘
```

**Relationship:** `Admin`/`NormalUser` → `UserRole` is **inheritance** (solid line, hollow triangle △) — Template Method is inheritance-based (that's central to the SOLID discussion below).

---

## SOLID trade-off — Template Method strains SRP (and DIP)

Template Method is correct here, but it concentrates behaviour in the base class. By the strict SRP test — *"how many actors would request a change?"* — `UserRole` carries several:

| Step | Owner / reason to change |
|---|---|
| `validation`, `authentication`, `authorization` | security team |
| `services` | product team |
| `review` | UX / analytics |
| `logout` | session team |

Many reasons to change one class ⇒ **SRP is strained.** And because the base **hardcodes** concrete `validation`/`authentication`, the high-level flow depends on low-level details ⇒ **DIP is strained** too.

**Is that a bug?** No — it's a **deliberate trade-off.** Template Method's job is to centralize a fixed skeleton; you accept some SRP compromise to guarantee the sequence. Fine when steps are simple and truly shared.

**If the steps grow independent — refactor toward composition** (extract each step into its own class, inject as an abstraction):

```java
class LoginFlow {                          // one responsibility: orchestrate
    private final Validator validator;     // each concern its own class (SRP)
    private final Authenticator auth;      // injected as abstractions (DIP)
    public final void run(String email, String pw){
        validator.validate(email, pw);
        auth.authenticate(email, pw);
        // ...
    }
}
```

> That's the **Template Method → Strategy/composition** shift. Contrast: **Strategy** (composition-based) respects SRP/OCP/DIP cleanly; **Template Method** (inheritance-based) leans on subclassing and pays for it here. Same "vary some steps" goal, different SOLID profile.

---

## Common notes & lessons

### 1. Make the template method `final`
```java
public void Backend(...) { ... }          // a subclass could override & break the skeleton
public final void Backend(...) { ... }    // ✅ locks the sequence — the key convention
```
The steps vary; the **order** must not. `final` enforces that.

### 2. `private` steps are intentionally locked
`validation`/`authentication`/`logout` are `private` — subclasses can't touch them. That's the point: they're invariant. Only `abstract`/`protected` steps are meant to vary.

### 3. Hooks give optional customization
`review` has a default body and is `protected` → subclasses override *only if they want*. `Admin` doesn't; `NormalUser` does. That's a hook, not an abstract step.

### 4. Naming (style)
`Backend` reads like a class name; a verb like `process()` or `handleLogin()` is conventional for a template method. Not a correctness issue.

---

## When to use Template Method

- Several variants share the **same overall algorithm** but differ in a **few steps**.
- You want to **lock the sequence** and prevent step-reordering.
- The shared steps are simple/stable enough that centralizing them is worth the SRP trade-off.

> If the steps become complex, independently-owned, or need runtime swapping — prefer **Strategy/composition** instead.

---

## Run it

```bash
cd "Template Pattern"
javac Template.java
java Template
```

Output:
```
user validated
user authenticated
authorize to do Admin stuffs
do stuffs monitor, adding items to website 
good experience              ← Admin keeps the default hook
user logged out
user validated
user authenticated
authorize to do normal user stuffs
do stuffs like buying , cancelling items , adding items to cart 
bad experience               ← NormalUser overrode the hook
user logged out
```

Both users run the **identical skeleton**; only the abstract steps and the `review` hook differ.
