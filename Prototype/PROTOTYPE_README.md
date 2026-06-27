# Prototype Pattern — Notes & Q&A

Notes for [Prototype.java](Prototype.java): clone a ready-made object instead of building a new one each time. Includes every question worked through while writing it.

---

## The idea

> **Instead of building a new object from scratch, copy (clone) an existing one and tweak it.**

**Photocopy analogy:** to send 10 offer letters, you don't retype the letter 10 times — you write it once, **photocopy** it, and change only the name. Prototype is the photocopy machine for objects.

A **creational** pattern. Use it when:
- Object creation is **expensive/complex** (heavy constructor: disk/DB/network).
- You need **many similar objects** with small differences.
- You want to avoid repeating initialization logic.

---

## Structure (this file)

```
EmailTemplate (interface, extends Cloneable)   "I can copy myself" + permission
   └── CloneEmail (concrete)                    real copy code: (CloneEmail) super.clone()
giveCloneEmail (registry)                       builds prototype ONCE, hands out copies
main                                            getClone("welcome") → showEmail()
```

```java
interface EmailTemplate extends Cloneable {
    EmailTemplate emailClone();
    void header(String h);
    void content(String c);
}
```

---

## Q&A — everything worked through

### Q1. Is `Cloneable` built into Java? What power does it give?

Yes — `Cloneable` is built into `java.lang`. It's an **empty marker interface** (no methods). It does NOT contain `clone()`.

- **`clone()` lives in `Object`** (every object inherits it) — it does the actual field-by-field copy.
- **`Cloneable` is just permission.** `Object.clone()` checks "is this class `Cloneable`?" → if yes it copies, if no it throws `CloneNotSupportedException`.

So the power: `Cloneable` + `super.clone()` duplicates an object **without manually copying each field** — the JVM copies them for you (fast, `native`).

| Thing | Lives in | Role |
|---|---|---|
| `Cloneable` | `java.lang` (empty) | **permission** to be cloned |
| `clone()` | `Object` | **does** the copy |
| `CloneNotSupportedException` | `java.lang` | thrown if you clone without `Cloneable` |

### Q2. `extends Cloneable` on the interface — what does it do, where's clone inside?

`extends Cloneable` propagates the **permission down to every class** that implements the interface (the interface itself is never cloned — interfaces have no objects). The line `EmailTemplate emailClone();` is just the **contract** (forces implementers to provide it, and makes it public/typed). The **actual copy code** lives only in the concrete class via `super.clone()`.

### Q3. What does `super.clone()` actually do?

```
super.clone()
   └─ super = Object (parent)
        └─ Object.clone()  ← the built-in copy method
              └─ checks Cloneable → allowed (because EmailTemplate extends Cloneable) → copies
```

It copies a `CloneEmail` object; the result is typed `Object`, so you **cast** it back: `(CloneEmail) super.clone()`. `Cloneable` does NOT contain `clone()` — it only grants permission.

### Q4. How is `clone()` more efficient than `new`?

`new` = allocate memory **+ run the constructor**. `clone()` = allocate memory **+ copy fields** (skips the constructor). The win is **skipping an expensive constructor**:

```java
public CloneEmail() {
    loadFromDisk(); fetchFromDB(); callAPI();   // ~200ms each time with new
}
```

Build the prototype **once** (pay 200ms once), then every `clone()` skips all that → near-instant. For **1000 emails**: `new` ≈ 1000×200ms; clone ≈ 200ms once + cheap copies.

**Honest caveat:** if the constructor is trivial (`this.x = 5`), clone gives ~no benefit — just use `new`. Prototype only wins when creation is genuinely expensive.

### Q5. If clone is "cheap," where is the copy stored?

`clone()` **does** create a real, separate object in memory (on the heap) — same as `new`. `original == copy` is `false` (different objects). It does NOT skip memory allocation; it only skips **running the constructor**, filling fields by copying instead.

### Q6. Does putting `clone()` in the interface do a deep copy?

No. The interface only **declares** the method — it copies nothing. The comment "recommended deep copy" is **advice to the developer**. `super.clone()` does a **shallow copy**. Deep copy is **your job to code** if needed.

### Q7. Shallow vs deep copy, and mutable vs immutable types

- **Shallow copy** = original and clone share the **same** referenced objects.
- **Safe when** the fields are **primitives or immutable** (can't be changed, so sharing is fine).
- **Dangerous when** a field is a **mutable object** (a `List`, array, etc.) — changing it via the clone corrupts the original.

| Category | Examples | Shallow safe? |
|---|---|---|
| Primitives | `int`, `double`, `boolean`, `char` | ✅ |
| Immutable objects | `String`, `Integer`/wrappers, `LocalDate`, `enum`, `BigDecimal` | ✅ |
| Mutable objects | `ArrayList`, `HashMap`, arrays, `StringBuilder`, most custom classes | ❌ need deep copy |

`CloneEmail` has only `String` fields → shallow copy is fine here.

### Q8. Is there a direct/built-in way to deep copy in Java?

No single native one-liner. Options:

| Method | Built-in? | Auto deep? | Notes |
|---|---|---|---|
| Manual copy inside `clone()` | ✅ | ❌ (you code it) | fast, full control |
| Copy constructor | ✅ | ❌ (you code it) | many prefer over `clone()` |
| Serialization | ✅ | ✅ | slow; needs `Serializable` |
| Library (Apache Commons `SerializationUtils.clone`, Gson) | ❌ external | ✅ | true one-liner |

For learning: use **manual deep copy** or a **copy constructor**.

### Q9. When does the registry's `static` block run?

It runs **once**, the **first time the class is used** (first static-method/field access or `new`) — lazy loading. In this file, the first `giveCloneEmail.getClone("welcome")` triggers it. It always finishes **before** that call's body runs, so the map is never empty. It never runs at compile time, program start, or "definition."

### Q10. What is the registry doing?

```java
static class giveCloneEmail {
    private static HashMap<String, CloneEmail> mp = new HashMap<>();
    static { mp.put("welcome", new CloneEmail()); }   // build ONCE, store
    public static CloneEmail getClone(String str) {
        return mp.get(str).emailClone();              // return a COPY, not the original
    }
}
```

A `Map` (key → prototype) built once. `getClone()` returns a **clone** so callers get their own copy and the stored original stays pristine. Returning the original directly would let one caller's edits corrupt everyone else's (same shared-state bug as `static` fields).

### Q11. Why doesn't `throw (e);` work in the catch?

`CloneNotSupportedException` is a **checked exception** — re-throwing it would force `emailClone()` to declare `throws ...`, but the interface didn't declare it, so an implementer can't add it. Fix: wrap in an **unchecked** exception:

```java
catch (CloneNotSupportedException e) {
    throw new RuntimeException(e);   // unchecked — no 'throws' needed
}
```

| | Checked (`CloneNotSupportedException`) | Unchecked (`RuntimeException`) |
|---|---|---|
| Must declare/handle? | ✅ yes | ❌ no |
| Re-throw freely? | only with `throws` | ✅ anywhere |

### Q12. Why can't the registry call `mp.get(str).clone()` directly?

`Object.clone()` is **`protected`** — callable only from inside the class hierarchy, not from an unrelated class like `giveCloneEmail`. That's exactly why `CloneEmail` exposes a **public** `emailClone()` wrapper:

```
giveCloneEmail ──▶ emailClone() (public) ──▶ super.clone() (protected, inside CloneEmail)
```

Call the **public** `emailClone()`, not the protected `clone()`.

---

## How Prototype compares to the other creational patterns

| Pattern | How it makes objects |
|---|---|
| Builder | assemble step by step (parts → object) |
| Factory / Abstract Factory | a factory decides which class to `new` |
| **Prototype** | **copy an existing object** instead of building new |

> Builder = assemble from parts. Factory = ask a factory. Prototype = photocopy one you already have.

---

## Run it

This file declares `package Prototype;`, so run with the full name from the parent folder:

```bash
javac Prototype/Prototype.java
java Prototype.Prototype
```

Output:
```
welcome to team!!!
welcome
```

(If the IDE Run button fails with "could not find main class", it's the package/folder mismatch — run from the terminal as above, or remove the `package` line.)
