# Factory Pattern — Notes & Q&A

Notes for [GoodPracticeFactoryMethod.java](GoodPracticeFactoryMethod.java) and [FactoryMethod.java](FactoryMethod.java): hand object creation to a **factory** so the client asks for *what* it wants (a name/string) and never calls `new` on a concrete class itself.

This is a **creational** pattern (like Builder, Prototype & Abstract Factory — about *making* objects, not *connecting* them).

---

## Why we needed it — the problem

The client needs a `Medium` (or a `Pizza`), but the *concrete* type depends on some input. If the client `new`s it directly, it gets **coupled to every concrete class** and repeats the selection logic everywhere:

```java
Medium mode;
if (name.equalsIgnoreCase("Air"))  mode = new Air();    // ❌ client knows Air, Land, ...
else if (name.equalsIgnoreCase("Land")) mode = new Land();
// ...copy-paste this if/else into every place that needs a Medium
mode.send();
```

Add a new `Sea` medium and you must hunt down **every** such `if/else` and edit it. The client is welded to the concrete classes it shouldn't care about.

## How it solves the problem

Move the `new` + selection into **one factory method**. The client passes a name and gets back the interface — never naming a concrete class:

```java
Medium mode = Service.getService(modeOfService);   // client only knows the Medium interface
mode.send();
```

Now the concrete classes live in **one place** (`Service`). Adding `Sea` means editing the factory only; every client keeps working unchanged.

---

## The idea

> **A method whose only job is to build and return an object**, choosing the concrete class internally, so callers depend on an interface instead of `new ConcreteClass()`.

**Restaurant analogy:** you order "a pizza" from the counter (the factory). You don't walk into the kitchen and assemble the `CheesePizza` yourself — you name what you want and the counter hands back *a* `Pizza`.

```
client ──"Air"──▶ Service.getService() ──▶ new Air()  ──▶ returns Medium
 (a name)            (the factory)          (hidden)       (the interface)
```

---

## The 3 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Product** (interface) | `Medium` (`Pizza`) | the common type the client depends on (`send()` / `type()`) |
| **Concrete Products** | `Air`, `Land` (`CheesePizza`, `ChickenPizza`) | the real classes the factory hides |
| **Factory** | `Service.getService(...)` (`PizzaFactory.orderType(...)`) | picks the concrete class and returns it as the Product |
| **Client** | `DoServices.main(...)` | asks the factory by name; never calls `new` on a product |

```
DoServices.main("Air")
   └─▶ Service.getService("Air")
          ├─ "Air"  → new Air()      returned as Medium
          ├─ "Land" → new Land()     returned as Medium
          └─ else   → throw Exception("not available mode")
```

---

## UML class diagram

```
                 ┌────────────────────┐
                 │ «interface» Medium │
                 │ + send(): void     │
                 └─────────▲──────────┘
              realizes ╱       ╲ realizes
                      ╱         ╲
              ┌──────────┐  ┌──────────┐
              │   Air    │  │   Land   │
              │ + send() │  │ + send() │
              └────▲─────┘  └────▲─────┘
                   │  «creates»  │
                   └──────┬──────┘
                   ┌──────────────────────────────┐
                   │           Service            │
                   │ + getService(String): Medium │   ← the factory
                   └──────────────────────────────┘
                              ▲ «uses»
                   ┌──────────────────────┐
                   │      DoServices      │   ← client
                   └──────────────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | `Air`/`Land` → `Medium` | dashed, hollow triangle ▷ | both `implements Medium` |
| **Dependency** («creates») | `Service` ┄▶ `Air`/`Land` | dashed open arrow | the factory `new`s them **inside a method** (not stored as a field) → dependency, not a HAS-A |
| **Dependency** («uses») | `DoServices` ┄▶ `Service` and `Medium` | dashed open arrow | the client calls the factory and uses the returned `Medium` locally |

> **Why dependency, not composition:** `Service` creates `new Air()` **inside** `getService()` and returns it — it doesn't keep it in a field. A reference that lives only inside a method = **dependency** (uses-a), not aggregation/composition (has-a).

---

## The key mechanic — one method hides the `new`

```java
class Service {
    public static Medium getService(String name) throws Exception {
        if (name.equalsIgnoreCase("Air"))  return new Air();
        if (name.equalsIgnoreCase("Land")) return new Land();
        throw new Exception("not available mode");   // unknown type = fail loudly
    }
}
```

- **`static`** → call it as `Service.getService(...)` without creating a `Service` first.
- **Returns the interface** (`Medium`), never a concrete type → the client stays decoupled.
- **Throws on unknown input** → the "good practice" bit: an unmatched name fails clearly instead of returning `null` and causing a `NullPointerException` later.

---

## Simple Factory vs Factory Method vs Abstract Factory

These files show a **Simple Factory** (one class with a parameterized creation method). Worth knowing how it differs from its GoF cousins:

| | What it is | Here |
|---|---|---|
| **Simple Factory** | one factory method picks the class from a parameter (`if/else`) | ✅ this file (`getService`, `orderType`) |
| **Factory Method** | an **abstract creator** with a `createX()` that **subclasses override** to decide the class | a creator hierarchy, no `if/else` |
| **Abstract Factory** | a factory that builds a **whole matching family** of objects | see [Abstract Factory](../AbstractFactoryPattern/ABSTRACT_FACTORY_README.md) |

> The file is named `FactoryMethod`, but the code is really the **Simple Factory** idiom (a single parameterized method). True Factory Method replaces the `if/else` with polymorphism — subclasses each return their own product.

---

## Common errors & lessons (hit while building these files)

### 1. Passing an unknown type → the exception path
```java
DoServices.main("sir");   // "sir" matches neither Air nor Land
```
`getService("sir")` throws `"not available mode"`, the `catch` runs, and the program prints **`error`**. That's the factory correctly rejecting an unknown mode — pass `"Air"` or `"Land"` to see a real send.

### 2. Returning a default instead of failing (design choice)
In [FactoryMethod.java](FactoryMethod.java), `orderType` returns `ChickenPizza` for **anything** that isn't `"Cheese"`:
```java
if (name.equalsIgnoreCase("Cheese")) return new CheesePizza();
else return new ChickenPizza();          // silent default for typos like "Chees"
```
Convenient, but a typo silently gets a ChickenPizza. The `Service` version's *throw-on-unknown* is safer — prefer failing loudly over a surprising default.

### 3. Empty `main` / nothing runs
`FactoryMethod`'s `public class` has an **empty** body — running it does nothing. To demo it, add a `main` that calls `new PizzaFactory().orderType("Cheese").type();`.

### 4. Unusual `main` signature
`DoServices.main(String modeOfService)` takes a single `String`, not `String[]` — so it's just a normal method here (called as `DoServices.main("sir")`), **not** a JVM entry point. Fine as a method; just don't expect `java DoServices` to launch it.

---

## When to use Factory

- The exact class to create depends on **input/config** and you don't want that `if/else` scattered across clients.
- You want clients to depend on an **interface**, not concrete classes (easy to add new types later).
- Object creation has a bit of logic you'd rather centralize in one place.

> The win: one place owns `new`. Add a product, edit the factory, and every client keeps working.

---

## Run it

```bash
cd Factory
javac GoodPracticeFactoryMethod.java
java GoodPracticeFactoryMethod
```

Current output (because `"sir"` is passed):
```
error
```

Change the call to `DoServices.main("Air")` and it prints:
```
send through air
```
