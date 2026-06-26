# OOP Relationships & Builder Pattern — Interview Notes

A simplified, interview-ready reference covering UML class relationships and the Builder design pattern.

---

## Part 1 — UML Class Relationships

There are **six** relationships. They fall into two families by line style:

- **Solid line** → structural relationships (the object IS or HAS another).
- **Dashed line** → loose relationships (the object USES or IMPLEMENTS another).

### The six at a glance

| Relationship | Meaning | Line | Symbol at end | Points to |
|---|---|---|---|---|
| **Dependency** | USES-A | dashed | open arrow `→` | the class used (supplier) |
| **Association** | HAS-A (knows) | solid | open arrow or plain line | the known object |
| **Aggregation** | HAS-A (part-of, shared) | solid | hollow diamond ◇ | the **whole** |
| **Composition** | HAS-A (part-of, owned) | solid | filled diamond ◆ | the **whole** |
| **Inheritance / Generalization** | IS-A (`extends`) | solid | hollow triangle △ | the parent class |
| **Realization** | implements a contract (`implements`) | dashed | hollow triangle △ | the interface |

**One-line mnemonic:** *dependency uses, association knows, aggregation has, composition owns, inheritance is, realization promises.*

---

### Coupling spectrum (weakest → strongest)

```
Dependency  →  Association  →  Aggregation  →  Composition
 (uses)         (knows)         (has, shared)   (owns)
```

Two questions decide which one you have:

**Q1 — Is the other object stored as a FIELD?**
- No → **Dependency**
- Yes → it's a HAS-A (association / aggregation / composition)

**Q2 — (only if field) Who owns the lifecycle?**
- Neither owns the other → **Association**
- Whole holds parts it did NOT create → **Aggregation**
- Whole creates the parts and they die with it → **Composition**

---

### Q1 explained: "Is it stored as a field?"

A **field** (instance variable) is declared **inside the class but outside any method** — it's permanent state of the object. A reference that only lives **inside a method** (parameter, local variable, or `new`) is NOT a field.

```java
// FIELD → HAS-A (association/aggregation/composition)
class Car {
    private Engine engine;          // declared in class body = field, persists with Car
    void drive() { engine.start(); }
}

// PARAMETER / LOCAL → dependency
class Car {
    void refuel(FuelPump pump) {    // parameter — gone when method returns
        pump.pump();
    }
    void repair() {
        Mechanic m = new Mechanic(); // local — disappears after method
        m.fix();
    }
}
```

**Eye test:** look at the class's opening brace. Variable declarations sitting there before any method = fields. If the type only appears inside `( )` of a method or after a `new` inside a method body = dependency.

---

### Q2 explained: "Lifecycle" and "who owns it"

**Lifecycle** = the lifespan of an object, from `new` (birth) to garbage collection (death).
**Owning the lifecycle** = which object creates the part, and does the part die when the whole dies?

```java
// COMPOSITION — whole owns lifecycle
class House {
    private Room room = new Room();   // House CREATES Room; Room dies with House
}

// AGGREGATION — whole does NOT own lifecycle
class Team {
    private List<Player> players;
    Team(List<Player> players) {      // Players passed IN; they outlive the Team
        this.players = players;
    }
}

// ASSOCIATION — neither owns the other
class Student {
    private Course course;            // just references it; both independent
}
```

**The decisive test for any HAS-A field:** *"If I destroy the container, should this part also cease to exist?"*
- Yes, the part is meaningless alone → **Composition** (a `Room` with no `House`).
- No, it lived before and lives after → **Aggregation** (a `Player` with no `Team`).

---

### The confusing pair: Association vs Aggregation

This is the **most argued-about pair in UML** — even experts wave their hands at it, because aggregation is technically a *special case* of association.

| | Association | Aggregation |
|---|---|---|
| Meaning | two objects are simply **related / linked** | one object is specifically **PART OF** another (whole–part) |
| Hierarchy? | no — they're peers/equals | yes — clear whole and clear part |
| Natural phrase | "knows", "works with", "is linked to" | "is part of", "made up of", "consists of" |
| In **code** | `private Type field;` | `private Type field;` — **identical!** |
| UML symbol | plain solid line | solid line + hollow diamond ◇ |

```java
// ASSOCIATION — peers, no whole/part, often bidirectional
class Student { private List<Course> courses; }
class Course  { private List<Student> students; }
// A student is NOT "part of" a course; they're just enrolled.

// AGGREGATION — clear whole (Team) and part (Player)
class Team { private List<Player> players; }
// A player IS "part of" a team's makeup, but lives independently.
```

**Brutally honest interview note:** association and aggregation are **identical in code** — no keyword, nothing the compiler sees. The difference exists *only* in the UML diagram / your mental model. Many developers just call everything "association" unless lifecycle makes it composition. Don't burn energy trying to make this boundary crisp.

**The working test:** ask *"is X part of Y?"*
- Yes → **Aggregation** (if part is owned/dies with whole → **Composition**)
- No, just related → **Association**

---

### Full comparison table (the one to memorize)

| | Field? | Who creates the part? | Part survives the whole? | UML notation |
|---|---|---|---|---|
| **Dependency** | No (transient) | n/a | n/a | dashed line, open arrow |
| **Association** | Yes | someone else | yes | solid line |
| **Aggregation** | Yes | someone else (passed in) | yes | solid + hollow diamond ◇ |
| **Composition** | Yes | the whole itself | no | solid + filled diamond ◆ |

---

### Bonus: UML class box compartments (the horizontal line question)

A UML class/interface box has **three stacked compartments** separated by horizontal lines:

```
┌─────────────────────┐
│   «interface»       │   ← 1. NAME (+ stereotype)
│   Logistics         │
├─────────────────────┤   ← separating line
│   (attributes)      │   ← 2. ATTRIBUTES / fields
├─────────────────────┤   ← separating line
│   +send(): void     │   ← 3. OPERATIONS / methods
└─────────────────────┘
```

- Order top → bottom: **Name → Attributes → Operations**.
- An **empty compartment can be dropped**. If a class/interface has no attributes, the attribute compartment is collapsed, leaving only **one** line (between name and methods).
- That's why interfaces (and field-less classes like `Road`, `Air`) show only one horizontal line — the attribute box is empty and omitted.

---

## Part 2 — Builder Pattern

### What it is

A **creational** design pattern that **separates the construction of a complex object from its representation**, so the same step-by-step process can build different versions of the object.

> Analogy: ordering a custom burger/pizza — you pick bun, patty, toppings, sauce one at a time, and the chef assembles it. Same process, many different results.

### The problem it solves

Objects with a **few required parts and many optional parts**, built with plain constructors, create the **"telescoping constructor"** mess:

```java
new BurgerMeal("wheat", "veg", null, null, false);   // what do null, null, false mean??
```

Problems:
1. **Unreadable** — you must remember exact order and types of every parameter.
2. **Forced nulls** — don't want a topping? Still must pass `null` to fill the slot.
3. **NullPointerException risk** — all those nulls can blow up later.
4. **Constructor explosion** — one overloaded constructor per combination doesn't scale.

### The solution

Set parts **one at a time, by name**, then call `build()`:

```java
BurgerMeal meal = new BurgerMeal.BurgerBuilder("wheat", "veg")  // required
                       .withCheese(true)                       // optional, named
                       .withSide("fries")                      // optional, named
                       .build();                               // finalize
```

Readable, no forced nulls, one process for all combinations.

---

### Anatomy (the 5 key parts)

```java
class BurgerMeal {
    private final String bunType;     // all final → immutable
    private final String patty;
    private final boolean hasCheese;

    // ① PRIVATE constructor — only the Builder can call this
    private BurgerMeal(BurgerBuilder b) {
        this.bunType  = b.bunType;
        this.patty    = b.patty;
        this.hasCheese = b.hasCheese;
    }

    // ② STATIC NESTED builder class
    public static class BurgerBuilder {
        private final String bunType;   // required — final
        private final String patty;
        private boolean hasCheese;      // optional — not final (mutable scratchpad)

        // ④ REQUIRED fields go in the builder's constructor
        public BurgerBuilder(String bunType, String patty) {
            this.bunType = bunType;
            this.patty = patty;
        }

        // ③ FLUENT methods — set optional field, return this
        public BurgerBuilder withCheese(boolean hasCheese) {
            this.hasCheese = hasCheese;
            return this;                // enables chaining
        }

        // ⑤ build() — hands itself to the private constructor
        public BurgerMeal build() {
            return new BurgerMeal(this);
        }
    }
}
```

| # | Element | Why |
|---|---|---|
| ① | **Private constructor** | Forces everyone through the Builder; no half-built objects via `new`. |
| ② | **Static nested class** | Nested = bonded to `BurgerMeal`. `static` = can exist *before* a `BurgerMeal` does (else chicken-and-egg). |
| ③ | **Fluent `withX()` + `return this`** | Enables the readable `.a().b().build()` chain. |
| ④ | **Required args in builder constructor** | Compiler forces bun + patty; optional fields opt-in via methods. |
| ⑤ | **`build()`** | The "I'm done" button; passes the filled builder to the private constructor → returns the finished, frozen object. |

---

### Why `return this`?

`this` = the current builder object. Returning it lets the **next method in the chain** have something to be called on.

```java
public BurgerBuilder withCheese(boolean c) {
    this.hasCheese = c;
    return this;          // pass the same builder along
}
```

Without it (returning `void`), the chain breaks:

```java
new BurgerBuilder("wheat","veg")
    .withCheese(true)     // returns void → nothing left
    .withSide("fries");   // ❌ ERROR: can't call a method on "nothing"
```

**Mental picture:** a notepad passed down a line of people. Each method writes one thing and passes the *same notepad* to the next (`return this`). At the end, `build()` reads the completed notepad. This style is called a **fluent interface** / **method chaining** (same trick `StringBuilder.append()` uses).

---

### Why a separate nested Builder? Why not put `withX()` on `BurgerMeal` directly?

Because of one hard conflict: **`final` fields vs. setter-style methods.**

- `final` field = assigned **once**, never changed → gives **immutability**.
- A `withCheese()` method changes a field **after** the object exists → **can't compile** against a `final` field.

You can't have both on one class. The Builder resolves this by **splitting responsibilities**:

```
Builder (mutable scratchpad)  ──build()──▶  BurgerMeal (immutable result)
 fields NOT final, withX() mutate freely    fields final, created once, frozen
```

| Putting `withX()` directly on BurgerMeal | Why it fails |
|---|---|
| Fields can't be `final` | Lose immutability — object can be mutated anytime |
| Object exists before fully configured | A half-built, usable, invalid object floats around |
| Required + optional mix | Falls back to telescoping-constructor hell |

**Honest caveat:** if you **don't need immutability**, you *can* skip the Builder and just use chainable setters on the class directly ("fluent setter" object). The Builder earns its extra complexity specifically when you want **immutable objects with many optional fields**.

---

### Builder — interview one-liners

- *What problem?* Telescoping constructors + forced nulls for objects with many optional fields.
- *What kind of pattern?* Creational.
- *Why static nested class?* So the builder can exist before the target object does.
- *Why private constructor?* Funnel all creation through the builder; guarantees complete objects.
- *Why `return this`?* Enables fluent method chaining.
- *Why does it give immutability?* All mutation happens on the builder; the final object is built once with `final` fields and never changed.
