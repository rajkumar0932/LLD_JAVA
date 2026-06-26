# `builder.java` — Builder Pattern + `static` vs `final` Deep Dive

Notes specific to [builder.java](builder.java): a `Maggie` object built via an inner `IngredientsRequired` builder, plus the field-modifier gotchas that trip people up.

---

## The structure at a glance

```
IngredientsRequired (mutable scratchpad)  ──execute()──▶  Maggie (immutable result)
   fields NOT final                                        fields final
   you fill it step by step via chaining                   set once, frozen forever
```

| | Inner builder (`IngredientsRequired`) | Outer object (`Maggie`) |
|---|---|---|
| Fields | **NOT final** | **final** |
| Why | must change as you chain `.addWater().addSpices()` | frozen once built |
| Role | mutable scratchpad | immutable result |

Usage:

```java
Maggie meal = new Maggie.IngredientsRequired(true, true)  // required: ingredient, oil
                  .addWater(true)                         // optional, chained
                  .addSpices(true)                        // optional, chained
                  .execute();                             // finalize → immutable Maggie
```

---

## Why `final` on `Maggie`'s fields — and NOT `static`

### What `static` actually means

A field **without** `static` is an **instance field** — every object gets its **own personal copy**.

A field **with** `static` is a **class field** — there is **exactly ONE copy**, shared by the entire class. It belongs to the blueprint, not to any object.

```java
class Maggie {
    private static Boolean spices;   // ONE box, shared by ALL Maggie objects
    private Boolean oil;             // each Maggie gets its OWN box
}
```

Analogy:
- **instance field** = each student has their own notebook
- **static field** = there's one whiteboard the whole class shares

---

### Problem 1 — `static` makes all your objects share state (corruption)

The Builder pattern exists to create **many different objects**. `static` would make them all share the same ingredient boxes:

```java
Maggie spicy = new IngredientsRequired(true, true).addSpices(true).execute();
Maggie plain = new IngredientsRequired(true, true).execute();   // no spices
```

With `static spices`:

```
spicy built  → writes spices = true   onto the shared whiteboard
plain built  → writes spices = null   onto the SAME whiteboard (overwrites)

Now spicy.spices → null too!  The second meal erased the first meal's data.
```

Your two "different" Maggies become the same data. That defeats the entire purpose of making objects.

---

### Problem 2 — `static final` won't even compile

`final` means **"assign exactly once, then frozen forever."**

A `static final` field is frozen when the **class** is first loaded — before any object exists. But the constructor runs **every time** you make a new Maggie:

```java
private Maggie(IngredientsRequired builder) {
    this.spices = builder.spices;   // ← assigning a "set-once-ever" value on EVERY object
}
```

Contradiction:
> "set only one time in the program's entire life" (`static final`)
> vs.
> "set once per object" (constructor runs many times)

Compiler error: *"cannot assign a value to final variable spices."*

---

## So what's correct? `final` YES, `static` NO

```java
class Maggie {
    // compulsory
    private final Boolean ingredient;   // each Maggie = its own copy
    private final Boolean oil;
    // optional
    private final Boolean water;
    private final Boolean vegees;
    private final Boolean butter;
    private final Boolean spices;
}
```

- **`final`** → immutability: set once in the constructor, never changed. This is the *point* of the result object.
- **No `static`** → each `Maggie` owns its own ingredients, so different meals stay independent.

### The catch `final` introduces

A `final` field **must** be assigned by the time the constructor finishes — no exceptions. The constructor already assigns all 6 fields, so it compiles. Even if the user never calls `addSpices()`, the builder's `spices` is still `null`, and `this.spices = builder.spices` assigns `null`. A `final Boolean` is allowed to be `null` — `final` means *can't be reassigned*, not *can't be null*.

---

## Why the inner builder's fields must NOT be `final`

The builder is a **mutable scratchpad**. Its fields change as you chain:

```java
public IngredientsRequired addSpices(Boolean spices) {
    this.spices = spices;   // reassigns — impossible if the field were final
    return this;            // return this → enables chaining
}
```

If the builder's fields were `final`, `addSpices()` couldn't reassign them and chaining would break. Each side needs the **opposite** treatment:

- Builder fields **non-final** → so you can fill them step by step.
- Maggie fields **final** → so the result is frozen.

---

## One-line summaries

- **`static`?** No — one shared copy for the whole class → all your meals become the same meal.
- **`static final`?** No — won't compile; constructor tries to set a "set-once-ever" value on every object.
- **`final`?** Yes — gives immutability, and the constructor already assigns every field.
- **Builder fields `final`?** No — they must stay mutable so `.addX()` chaining works.
- **Why `return this`?** So the next method in the chain has the same builder to act on.
- **Why a private `Maggie` constructor?** Funnel all creation through the builder; guarantees complete, valid objects.
