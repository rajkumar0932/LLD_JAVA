# Flyweight Pattern — Notes & Q&A

Notes for [Flyweight.java](Flyweight.java): when you need a **huge number of similar objects**, store the **shared** part once and reuse it, so a million objects don't each carry their own copy of the same heavy data. Example: a **forest** of 100 trees that all share **one** `TreeType` (color/texture/name).

This is a **structural** pattern (like Adapter, Decorator, Facade, Composite, Proxy & Bridge — about *connecting* objects).

---

## Why we needed it — the problem

A forest has millions of trees. If every `Tree` stores its own `color`, `texture`, and `name`, you duplicate the **same** heavy strings a million times:

```java
class Tree {
    int x, y;
    String color, texture, name;   // ❌ 1,000,000 trees × the same "green/dense/mango"
}
// 1,000,000 Tree objects each holding identical color/texture/name → memory blows up
```

The position (`x`, `y`) genuinely differs per tree — but the `color`/`texture`/`name` are identical across thousands of them. Storing them per-object is pure waste.

## How it solves the problem

Split each object's state into two kinds and **share** the part that repeats:

- **Intrinsic** (shared, unchanging) → `color`, `texture`, `name` → stored **once** in a `TreeType`.
- **Extrinsic** (unique per object) → `x`, `y` → stays on each `Tree`.

A **factory** caches `TreeType`s and hands back the *same* instance for the same key:

```java
Tree t = new Tree(x, y, factoryTreeType.createTreeType("green","dense","mango"));
// all 100 mango trees point at ONE shared TreeType → memory stays flat
```

100 trees, but only **one** `TreeType` object exists — every tree references it.

---

## The idea

> **Share the repeated (intrinsic) state across many objects via a factory cache**, and keep only the unique (extrinsic) state on each object.

**Stamp analogy:** to print 1,000 letters you don't carve 1,000 stamps — you carve **one** stamp (the shared design) and press it at 1,000 different positions on the page. The stamp is the flyweight; the position is extrinsic.

```
   100 Tree objects (x,y each)      1 shared TreeType
        │  │  │  │  │  ...              (green/dense/mango)
        └──┴──┴──┴──┴────────────────────▶  ◀── all point here
```

---

## Intrinsic vs Extrinsic — the heart of the pattern

| | **Intrinsic** | **Extrinsic** |
|---|---|---|
| Meaning | shared, context-free, unchanging | unique per object, context-dependent |
| Here | `color`, `texture`, `name` | `x`, `y` |
| Lives in | `TreeType` (the flyweight, shared) | `Tree` (the context, one per object) |
| How many copies | **one**, cached & reused | one per object |

> The whole memory win comes from this split: pull the repeated data out into a shared flyweight, keep only what's genuinely unique on each instance.

---

## The 4 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Flyweight** (intrinsic, shared) | `TreeType` | holds `color`/`texture`/`name`; created once per unique combo |
| **Context** (extrinsic, unique) | `Tree` | holds `x`/`y` **and a reference** to a shared `TreeType` |
| **Flyweight Factory** | `factoryTreeType` | caches `TreeType`s in a `Map`; returns the existing one or makes a new one |
| **Client** | `Forest` / `Flyweight.main()` | plants trees; asks the factory for types |

```
Flyweight.main()  (plants 100 mango trees)
   └─▶ Forest.plantTree("green","dense","mango", x, y)
          └─ factoryTreeType.createTreeType("green","dense","mango")
                 ├─ key "green-dense-mango" in cache?  → return the SAME TreeType
                 └─ not cached?                        → new TreeType, store, return
```

---

## UML class diagram

```
   ┌──────────────────────────┐         ┌────────────────────────────┐
   │          Tree            │ ls      │         TreeType           │
   │ - x: int                 │ ◇──────▶│ - color: String            │
   │ - y: int                 │ (shared)│ - texture: String          │
   │ - ls: TreeType           │         │ - name: String             │
   │ + plan(): void           │         │ + explainType(): void      │
   └──────────────────────────┘         └──────────────▲─────────────┘
                                              «creates» │ + caches
                                    ┌───────────────────────────────────┐
                                    │          factoryTreeType          │
                                    │ - mp: Map<String,TreeType>        │
                                    │ + createTreeType(...): TreeType   │
                                    └───────────────────────────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Aggregation** | `Tree` ◇→ `TreeType` | hollow diamond ◇ on `Tree` | the `TreeType` is **passed in** and **shared** — it lives independently of any one tree (created outside, by the factory) → aggregation, never composition |
| **Dependency** («creates») | `factoryTreeType` ┄▶ `TreeType` | dashed open arrow | the factory `new`s and caches `TreeType`s |

> **Why aggregation, not composition:** the shared `TreeType` must **outlive** any single `Tree` (it's reused by thousands). If a tree *owned* its type (composition ◆), the type would die with that tree and couldn't be shared — which would defeat the entire pattern. Shared parts ⇒ aggregation ◇.

---

## The key mechanic — the factory cache

```java
class factoryTreeType {
    private static Map<String,TreeType> mp = new HashMap<>();

    public static TreeType createTreeType(String color, String texture, String name){
        String key = color + "-" + texture + "-" + name;   // identity of the shared state
        if (mp.get(key) == null){
            mp.put(key, new TreeType(color, texture, name)); // first time → create + cache
        }
        return mp.get(key);                                 // every other time → reuse
    }
}
```

Same key ⇒ same object handed back. Planting 100 mango trees calls this 100 times but only **one** `TreeType` is ever created — verified by reference identity (`==`) and a cache size of 1.

> **Optional polish:** the double lookup (`get` in the `if`, then `get` in `return`) collapses to one line:
> ```java
> return mp.computeIfAbsent(key, k -> new TreeType(color, texture, name));
> ```

---

## Common errors & lessons (hit while building this file)

### 1. A collection where a single reference belongs
```java
private List<TreeType> ls;   // ❌ a Tree has ONE type, not a list of types
```
The relationship is **many trees → one shared type**, not one tree → many types. Fix: `private TreeType ls;`. (`List` also needed an `import java.util.*;` and can't be `new`-ed directly.)

### 2. Map declared but never created → NullPointerException
```java
private static Map<String,TreeType> mp;    // ❌ defaults to null
...
mp.get(key)                                 // NPE: "mp is null"
```
A reference field is `null` until assigned. Initialize it: `= new HashMap<>();`. (Same trap for `Forest`'s `private List<Tree> forest;` → `= new ArrayList<>();`.)

### 3. Which null fires first — argument evaluation order
`forest.add(new Tree(x, y, factoryTreeType.createTreeType(...)))` — Java evaluates the **arguments first**, so `createTreeType` runs (and hits the null `mp`) *before* `forest.add` is even reached. The stack trace pointed at `createTreeType`, not `forest.add`, for exactly this reason.

### 4. Output doesn't prove sharing
Printing 100 trees looks the same whether they share a type or not. Real proof is **reference identity** (`t == first`) and **cache size == 1** — behaviour, not print output.

---

## When to use Flyweight

- You need a **very large number** of objects and memory is the bottleneck.
- Much of each object's state is **repeated/shareable** (intrinsic) and can be pulled out.
- The remaining per-object state (extrinsic) is small and can be passed in or stored separately.

> The win: N objects, but the heavy shared data exists **once**. It's a memory optimization — reach for it only when object count actually hurts.

---

## Run it

```bash
cd "Flyweight Pattern"
javac Flyweight.java
java Flyweight
```

Output (100 lines, one per tree — all sharing one `TreeType`):
```
tree is planted at coordinate 0 0
has color green   texture dense name mango
...
tree is planted at coordinate 99 99
has color green   texture dense name mango
```

All 100 lines describe the **same** shared `TreeType` — only the coordinates (extrinsic) change.
