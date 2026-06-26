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

---

## Common errors & lessons (learned while building this file)

These are the real mistakes hit while writing `builder.java`, with the takeaway from each.

### 1. "No enclosing instance of type `builder`" on `new Maggie(this)`

- **Cause:** `Maggie` was a *non-static* inner class. Inner classes need an instance of the outer class to exist — but `execute()` lives in the `static` builder, where no outer instance exists.
- **Fix:** make `Maggie` `static` too → `static class Maggie`.
- **Lesson:** in the Builder pattern, **both nested classes must be `static`** so they can be created independently of the outer class.

### 2. Fields show a "never used / never read" warning (yellow squiggle)

- **Cause:** the fields are *assigned* in the constructor but never *read* anywhere. Writing a value ≠ using it; something has to read it back.
- **Fix:** add a `toString()` (or getters) that reads every field.
- **Lesson:** **yellow = warning** (code still runs), **red = error** (won't compile). Assigning a field does not count as "using" it.

### 3. `new IngredientsRequired(...)` undefined from `main`

- **Cause:** two issues — missing `new`, and the class is nested inside `Maggie`, so its real name is `Maggie.IngredientsRequired`.
- **Fix:** `new Maggie.IngredientsRequired(true, true)`.
- **Lesson:** `static` removes the need for an outer **instance**, but not the need to **name the path** to the class.

### 4. When to use `new`

- **Constructor → use `new`** (`new Maggie.IngredientsRequired(...)`) — it creates a brand-new object.
- **Method → no `new`** (`.addWater(true)`, `.execute()`) — it acts on an object that already exists.
- **Lesson:** a whole builder chain has exactly **one `new`** (at the start); everything after is method calls.

### 5. `Maggie.toString()` doesn't print the meal

- **Cause:** `toString()` was called on the **class**, and the built object was thrown away (not stored).
- **Fix:** store the result, then print it:
  ```java
  Maggie meal = new Maggie.IngredientsRequired(true, true).addWater(true).execute();
  System.out.println(meal);   // println auto-calls toString()
  ```
- **Lesson:** `toString()` describes a **specific object**, not the class. Catch what `execute()` returns in a variable first.

### 6. `meal` never prints when running the program

- **Cause:** `main` was `public void main(...)` — not `static`. The JVM only runs `public static void main(String[] args)`.
- **Fix:** add `static` → `public static void main(String[] str)`.
- **Lesson:** at program start **no object exists yet**, so `main` must be `static` (the exact entry-point signature) or it never runs.

---

## `toString()` — the auto-called method

```java
Maggie meal = new Maggie.IngredientsRequired(true, true).addWater(true).execute();
System.out.println(meal);   // prints field values — but we never wrote .toString()?
```

It still prints because **`println` calls `toString()` for you, automatically**:

```java
System.out.println(meal);
// Java secretly runs:
System.out.println(meal.toString());
```

### Why it works

Every object inherits `toString()` from the `Object` class, so it always exists:

- **No `toString()` overridden** → default → `Maggie@1b6d3586` (class name + memory hash).
- **`toString()` overridden** → your version → `Maggie{ingredient=true, ...}`.

You don't *create* `toString()` — you **override** (replace) the one already there. `@Override` marks that.

### Where Java auto-calls it (and where it does NOT)

Java auto-calls `toString()` only inside a few **specific operations** — it is not a general "convert anything to text" rule.

| Situation | Auto-calls `toString()`? | Why |
|---|---|---|
| `"text" + meal` | ✅ yes | the `+` operator is defined to convert objects to text |
| `System.out.println(meal)` | ✅ yes | `println(Object)` is built to call it |
| `String.valueOf(meal)` | ✅ yes | this method's whole job is conversion |
| `String s = meal;` | ❌ **no** — compile error | plain assignment needs matching types; no conversion rule |
| `meal.toString()` elsewhere (logging, `.contains()`, etc.) | ❌ you call it yourself | not a text context |

### Why `String s = meal;` fails but `println(meal)` works

Java is **statically typed** — assignment demands the types match (`Maggie` ≠ `String`), and Java never silently converts. So you must be explicit:

```java
String s = meal;                 // ❌ incompatible types
String s = meal.toString();      // ✅ explicit conversion
String s = "" + meal;            // ✅ the "+" rule triggers the auto-call
```

The auto-call is a special rule **baked into `+`, `println`, `valueOf`** — not into assignment.

### Other "special" methods like this

`toString()` is one of a small family every object inherits, that Java auto-calls in specific situations. Override them to change the behavior:

| Method | Auto-called when... |
|---|---|
| `toString()` | object used as text (`println`, `+`) |
| `equals()` | comparing objects with `.equals()` |
| `hashCode()` | object used as a `HashMap` / `HashSet` key |

---

## Real-world Builder: Lombok's `@Builder`

The Builder pattern is so common that **Lombok** (a Java library that removes boilerplate) generates the whole thing from one annotation. Everything written by hand in `builder.java` becomes:

```java
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class Maggie {
    private final Boolean ingredient;
    private final Boolean oil;
    private final Boolean water;
    private final Boolean vegees;
    private final Boolean butter;
    private final Boolean spices;
}
```

Usage:

```java
Maggie meal = Maggie.builder()
        .ingredient(true)
        .oil(true)
        .water(true)
        .build();
System.out.println(meal);
```

### What each annotation replaces

| Hand-written code | Generated by |
|---|---|
| nested builder class (`IngredientsRequired`) | `@Builder` |
| `addWater()` / `addX()` methods + `return this` | `@Builder` |
| builder constructor + private `Maggie(...)` constructor | `@Builder` |
| `execute()` (Lombok names it `build()`) | `@Builder` |
| hand-written `toString()` | `@ToString` |

**~75 lines → ~15 lines.**

### Gotchas

- **`@Builder` does NOT generate `toString()`** — it only builds. Add `@ToString` separately for readable printing, or Lombok prints the default `Maggie@1b6d3586`.
- **Don't keep both** a manual `toString()` and `@ToString`: Lombok sees your method and backs off, so `@ToString` does nothing. Pick one.
- **Required fields:** plain `@Builder` makes every field optional. Use `@NonNull` on a field to force it to be set before `build()`.
- **Not plain Java:** Lombok needs a dependency (Maven/Gradle) + IDE plugin. The hand-written `Maggie` runs with **zero dependencies** — that's the trade-off.
