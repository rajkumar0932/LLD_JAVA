# Visitor Pattern — Notes & Q&A

Notes for [Visitor.java](Visitor.java): add **new operations** to a set of classes **without editing those classes**, by moving each operation into its own *visitor* object. Example: hostels (`BoysHostle`, `GirlsHostle`) that need many services over time (`Laundry`, `Playground`, …) — each service is a visitor, and the hostel classes never change when you add one.

This is a **behavioural** pattern (about *how objects interact* — like Strategy, State, Command & Chain of Responsibility).

---

## Why we needed it — the problem

You have a stable set of classes (`BoysHostle`, `GirlsHostle`) and you keep needing **new operations** over them. The naive fix is to bolt each new operation onto every class:

```java
class BoysHostle {
    void doLaundry(){ ... }
    void managePlayground(){ ... }
    void runSecurity(){ ... }   // ❌ every new service = crack open EVERY hostel class
    void bill(){ ... }          //    and the class bloats with unrelated jobs
}
```

Two problems: (1) each new operation forces edits across **all** element classes, and (2) the hostel class becomes a junk drawer of unrelated responsibilities (laundry + playground + security + billing) — a straight **SRP** violation.

## How it solves the problem

Give each element a single `accept` hook **once**, then put every operation in its **own** class:

```java
boys.visitor(new Laundry());      // the Laundry operation lives in ONE class
boys.visitor(new Playground());   // adding Security = new class, hostels untouched
```

A new operation = one new visitor class. The hostel classes stay closed, and each operation's per-type logic sits together in one place.

---

## The idea

> **Pull operations *out* of the element classes and into separate visitor objects.** Each element only knows how to "accept" a visitor; the visitor carries the behaviour. Add behaviour by adding a visitor — not by editing the elements.

**Tax-inspector analogy:** houses don't each contain the tax rules. An inspector (visitor) walks in, looks at the house, and applies the rules. New rules = a new inspector; the houses never change.

```
element.accept(visitor)  ─▶  visitor.visit(element)
 (BoysHostle)  (Laundry)      the visitor does the work for THIS element type
```

---

## The 4 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Element** (interface) | `VisitorClass` | declares `visitor(VisitorFunction)` — the "accept" hook |
| **Concrete Elements** | `BoysHostle`, `GirlsHostle` | implement accept; call `vis.visitor(this)` |
| **Visitor** (interface) | `VisitorFunction` | one overload **per element type** — `visitor(BoysHostle)`, `visitor(GirlsHostle)` |
| **Concrete Visitors** | `Laundry`, `Playground` | implement each overload = one operation across all element types |

> Naming note: in this file everything is called `visitor`. The textbook names are **`accept(Visitor)`** on the element and **`visit(Element)`** on the visitor — worth renaming for readability, but the structure is correct as-is.

---

## The heart of it — double dispatch

The reason Visitor needs the `accept → visit` bounce is **double dispatch**: choosing behaviour from the runtime types of **two** objects (which hostel × which service), when Java only picks by **one** object per call.

The result must land in the right cell of this table:

|              | `Laundry` | `Playground` |
|--------------|-----------|--------------|
| **BoysHostle**  | Boys · Laundry | Boys · Playground |
| **GirlsHostle** | Girls · Laundry | Girls · Playground |

Neither type alone picks the cell — you need both, at runtime. Java can't do that in one call (the *argument's* type is resolved early, by its declared type, not its real type). So we bounce, and each hop nails down one object:

```java
class BoysHostle {
    public void visitor(VisitorFunction vis){
        vis.visitor(this);          // HOP 2
    }
}
```

Trace `boys.visitor(laundry)`:

```
boys.visitor(laundry)
  │  HOP 1 — dispatch on `boys` → real type BoysHostle → jump into BoysHostle.visitor()
  │          (now `this` is KNOWN to be a boys hostel)
  ▼
vis.visitor(this)
  │  HOP 2 — dispatch on `vis` → real type Laundry
  │          + `this` is BoysHostle → picks the visitor(BoysHostle) overload
  ▼
Laundry.visitor(BoysHostle)  →  "Boys Hostle Loundry"    ✅ BOTH types used
```

> **Two dispatches = double dispatch.** One call can only "see" one object clearly, so the element→visitor bounce recovers the second type. Visitor is essentially *"how to fake multiple dispatch in a single-dispatch language."*

---

## UML class diagram

```
   ┌──────────────────────────┐             ┌──────────────────────────────┐
   │  «interface»              │             │  «interface» VisitorFunction  │
   │  VisitorClass (Element)   │             │  + visitor(BoysHostle)        │
   │  + visitor(VisitorFunction)│────uses───▶│  + visitor(GirlsHostle)       │
   └────────────▲─────────────┘             └───────────────▲──────────────┘
   realizes ╱        ╲ realizes               realizes ╱          ╲ realizes
           ╱          ╲                              ╱            ╲
   ┌────────────┐ ┌────────────┐            ┌────────────┐  ┌──────────────┐
   │ BoysHostle │ │ GirlsHostle│            │  Laundry   │  │  Playground  │
   │ +visitor() │ │ +visitor() │            │ +visitor×2 │  │ +visitor×2   │
   └────────────┘ └────────────┘            └────────────┘  └──────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | elements → `VisitorClass`, visitors → `VisitorFunction` | dashed, hollow triangle ▷ | each `implements` its interface |
| **Dependency** («use») | `VisitorClass` ┈▷ `VisitorFunction` | dashed arrow ┈▷ | `accept` *uses* a visitor as a parameter (transient, not stored) |

> Note the dependency is a **parameter**, not a field — the element doesn't *keep* the visitor, it just receives one for the call. That's a `«use»` dependency (the weakest link), same category as the `new Successor()` calls in your State/Chain notes.

---

## The core trade-off — operations vs. element types

Visitor is **not** a free win. It optimises one axis and pessimises the other:

| | Add a new **operation** (e.g. `Security`) | Add a new **element type** (e.g. `StaffHostle`) |
|---|---|---|
| **Visitor** | ✅ easy — one new visitor class, elements untouched | ❌ hard — edit `VisitorFunction` **and every** visitor |
| Methods-on-the-class | ❌ hard — edit every element class | ✅ easy — one new class |

> **Rule:** use Visitor when the set of **element types is stable** but **operations keep growing.** If you keep adding element types instead, Visitor turns painful — and a plain method-on-the-class is better.

---

## SOLID

> Note: **Singleton is *not* SOLID** — it's a separate creational pattern. The **S** in SOLID is **Single Responsibility**.

| SOLID | Followed? | Why, in this code |
|---|---|---|
| **S** — Single Responsibility | ✅ | each visitor owns one operation; hostels aren't bloated with laundry+playground+security |
| **O** — Open/Closed | ⚠️ **one-directional** | ✅ for **operations** (new visitor, no edits) — ❌ for **element types** (adding `StaffHostle` edits every visitor) |
| **L** — Liskov Substitution | ✅ | any element substitutes for `VisitorClass`, any visitor for `VisitorFunction` |
| **I** — Interface Segregation | ⚠️ | `VisitorFunction` forces **every** visitor to implement an overload for **every** element type, even irrelevant ones |
| **D** — Dependency Inversion | ✅ | elements depend on the `VisitorFunction` **abstraction**, not on concrete `Laundry`/`Playground` |

> The headline: Visitor gives you **Open/Closed for operations** at the cost of **breaking it for element types**. That directional trade-off *is* the pattern.

---

## Common notes & lessons

### 1. `main` is currently empty
The classes are wired correctly, but `main` has no calls — so the file compiles and runs but prints nothing. See **Run it** below for a `main` that exercises all four combinations.

### 2. Naming (`accept`/`visit`)
Every method here is named `visitor`, which is hard to read. Convention:
```java
interface Element  { void accept(Visitor v); }   // element side
interface Visitor  { void visit(BoysHostel b); void visit(GirlsHostel g); }  // visitor side
```
Same structure, far clearer intent. (Functionally identical — purely readability.)

### 3. Typos
"Hostle" → **Hostel**, "Loundry" → **Laundry**. Cosmetic; the output strings currently print with the typos.

### 4. Why `accept` must call `vis.visitor(this)` — not the client
The bounce **must** happen inside the element, because only there is `this` known to be the concrete type (`BoysHostle`). If the client tried `laundry.visitor(someElement)` directly, Java couldn't pick the overload from the element's declared type. The `accept` hook is what makes double dispatch possible.

---

## When to use Visitor

- You have a **stable set of element classes** and **keep adding operations** over them.
- An operation's logic **differs per element type**, and you want it **centralised in one place** instead of scattered across the classes.
- You want to keep the element classes **focused** (SRP) rather than piling unrelated methods onto them.
- Real-world: compilers/AST traversal (type-check, optimise, generate code — each a visitor over the same node types), document exporters (to-XML, to-PDF), UI element operations, reporting over an object structure.

> The win: add an operation by adding a class (Open/Closed for operations, SRP for elements). The cost: adding a new **element type** touches every visitor — so only reach for it when element types are settled.

---

## Run it

The file's `main` is currently empty. Add this to see the double dispatch work across all four (element × visitor) combinations:

```java
public static void main(String[] str){
    VisitorClass boys  = new BoysHostle();
    VisitorClass girls = new GirlsHostle();
    VisitorFunction laundry    = new Laundry();
    VisitorFunction playground = new Playground();

    boys.visitor(laundry);       // (BoysHostle  x Laundry)
    girls.visitor(laundry);      // (GirlsHostle x Laundry)
    boys.visitor(playground);    // (BoysHostle  x Playground)
    girls.visitor(playground);   // (GirlsHostle x Playground)
}
```

```bash
cd Visitor
javac Visitor.java
java Visitor
```

Output:
```
Boys Hostle Loundry
Girls Hostle Loundry
Boys Hostle Playground
Girls Hostle PlayGround
```

Each line is a different cell of the (element × visitor) table — selected at runtime by **both** types via double dispatch.
