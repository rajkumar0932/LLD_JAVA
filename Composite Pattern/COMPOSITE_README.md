# Composite Pattern — Notes & Q&A

Notes for [Composite.java](Composite.java): treat **a single object** and **a group of objects** the **same way**, so a "bundle of products" is used exactly like one "product".

This is a **structural** pattern (like Adapter, Decorator & Facade — about *connecting* objects). Example: a **shopping cart** where a `Product` and a `ProductBundles` (which can hold products *and other bundles*) both answer `price()` and `name()`. The client never asks "is this one item or many?"

---

## Why we needed it — the problem

A cart total has to add up **single products** *and* **bundles of products** — and a bundle can itself contain other bundles ("combo of combos"). Without a shared type, the client is forced to keep asking *"is this one item or a group?"*:

```java
int total = 0;
for (Object item : cart) {
    if (item instanceof Product) {
        total += ((Product) item).price();
    } else if (item instanceof ProductBundles) {
        for (Product p : ((ProductBundles) item).getItems())   // and what if THOSE are bundles too?
            total += p.price();                                 // ...you'd recurse by hand, forever
    }
}
```

Every new level of nesting means more `instanceof` branches and hand-written recursion. The client is tightly coupled to the tree's shape.

## How it solves the problem

Give the **leaf** (`Product`) and the **container** (`ProductBundles`) one common interface (`Itemcart`), and let the container hold a `List<Itemcart>` — the *same* interface it implements. Now:

```java
int total = item.price();   // works whether item is a single product or a bundle of bundles
```

The composite's `price()` just loops over its children calling `price()` — it doesn't know or care if each child is a leaf or another composite. **The `if/instanceof` ladder disappears, and the recursion happens automatically** to any depth.

---

## The idea

> **A common interface for both the "leaf" and the "container", where the container holds a list of that same interface** — so trees of objects are treated like single objects.

**Folder analogy:** a file has a size; a folder also has a size — the **sum of everything inside it**, including sub-folders. You ask any of them `size()` the same way. The recursion is invisible to you.

```
       Library  (composite)
      /    |    \
 books1  books2  book8   ← book8 is a leaf, books1/books2 are composites
   /|\     /|\
 leaves   leaves
```

---

## The 3 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Component** (common interface) | `Itemcart` | declares `name()` and `price()` — the shared contract |
| **Leaf** (no children) | `Product` | a single item; implements the contract directly |
| **Composite** (has children) | `ProductBundles` | holds `List<Itemcart>` + `add()`; implements the contract by **looping over children** |
| **Client** | `Composite.main()` | treats `Product` and `ProductBundles` identically through `Itemcart` |

```
Composite.main()
   ├─ book4.price()            // leaf → returns 350
   ├─ books1.price()           // composite → 400+200+500 = 1100
   └─ Library.price()          // composite of composites → 1100+1100+350 = 2550
          └─ recurses into books1, books2, then book8
```

---

## The one line that makes it Composite

```java
List<Itemcart> Products = new ArrayList<>();   // ✓ Itemcart, NOT Product
```

Because the list holds the **interface type** (`Itemcart`), a bundle can contain **leaves and other bundles**. That recursive containment *is* the pattern. Declare it as `List<Product>` instead and you'd lose the ability to nest bundles — it would just be a flat list wrapper.

```java
public int price(){
    int cost = 0;
    for (int i = 0; i < Products.size(); i++)
        cost += Products.get(i).price();   // doesn't care if child is leaf or composite
    return cost;
}

public void name(){
    System.out.println("this productBundle is " + name);
    for (Itemcart item : Products)
        item.name();                       // recurses the whole tree
}
```

---

## UML class diagram

```
                 ┌────────────────────────┐
                 │   «interface» Itemcart │
                 │  + name(): void        │
                 │  + price(): int        │
                 └───────────▲────────────┘
                realizes ╱   │   ╲ realizes
                       ╱     │     ╲
        ┌─────────────────┐ │  ┌──────────────────────────────┐
        │     Product     │ │  │       ProductBundles         │
        │ - product:String│ │  │ - name: String               │
        │ - cost: int     │ │  │ - Products: List<Itemcart> ◆─┼──┐
        │ + name(): void  │ │  │ + add(Itemcart): void        │  │ aggregation
        │ + price(): int  │ │  │ + name(): void               │  │ (children
        └─────────────────┘ │  │ + price(): int               │◀─┘  passed in)
                            │  └──────────────────────────────┘
                            └── Product also realizes Itemcart
```

**The two relationship types here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | `Product`/`ProductBundles` → `Itemcart` | dashed arrow, hollow triangle ▷ | both `implements Itemcart` |
| **Aggregation** | `ProductBundles` ◇→ `Itemcart` | hollow diamond ◇ on the bundle side | children are **passed in** via `add()`, created **outside** the bundle → aggregation, not composition |

> **The "who typed `new`?" test:** the products are `new`-ed in `main` and *handed to* the bundle via `add(book1)`. Created **outside** → **Aggregation ◇** (hollow). If the bundle built its own children internally, it'd be Composition ◆ (filled). Death test: destroy a bundle and the products could still live on elsewhere. ✔
>
> The self-pointing aggregation (`ProductBundles` → `Itemcart`, and `ProductBundles` *is* an `Itemcart`) is the recursive loop that lets bundles nest inside bundles.

---

## Common errors & lessons (hit while building this file)

### 1. Trying to instantiate the `List` interface

```java
List<Product> product = new List<Product>();   // ❌ List is an interface
```
`List` can't be `new`-ed — it's an interface (like an abstract class with only pure-virtuals in C++). Use a concrete implementation:
```java
List<Itemcart> Products = new ArrayList<>();    // ✅
```

### 2. C++ array-indexing on a List

```java
cost += Products[i].price();    // ❌ [] doesn't work on collections in Java
```
`[]` only works on plain arrays. For a `List`, use `.get(i)`:
```java
cost += Products.get(i).price();   // ✅
```

### 3. `int` method with no `return`

`public int price()` looped and summed into `cost` but never returned it → won't compile. Added `return cost;`.

### 4. Executable statement in the class body

```java
public class Composite {
    System.out.println(book4.price());   // ❌ statements can't float in a class body
}
```
A class body holds only **declarations** (fields, methods, constructors). Statements must live **inside** a method. Moved it into `public static void main`.

### 5. `javac Composite` without `.java`

```
error: Class names, 'Composite', are only accepted if annotation processing is explicitly requested
```
`javac` compiles **source files** → needs the extension: `javac Composite.java`. `java` runs a **class** → no extension: `java Composite`.

### 6. `name()` didn't recurse

`ProductBundles.name()` originally printed only its own name. Added a loop so it calls `name()` on every child — now it prints the whole tree, matching how `price()` already recursed.

---

## When to use Composite

- You have a **part-whole hierarchy** (tree) — menus, file systems, org charts, UI components, carts of carts.
- You want clients to **treat individual objects and compositions uniformly** (no `if (isGroup)` branching).
- Operations naturally **recurse** down the tree (sum, render, count, search).

> Key tell: whenever you'd otherwise write "if it's a single item do X, if it's a group loop and do X to each" — Composite removes that branch by making both answer the same method.

---

## Composite vs the other structural patterns

| Pattern | Shape | Goal |
|---|---|---|
| **Adapter** | wraps one class | **change** its interface |
| **Decorator** | wraps one object (same type) | **add** behaviour, stackable |
| **Facade** | wraps a whole subsystem | **simplify** — one door over many |
| **Composite** | a **tree** of same-typed objects | treat **one and many** uniformly |

---

## Run it

```bash
cd "Composite Pattern"
javac Composite.java
java Composite
```

Output:
```
350
1100
2550
```

`2550` is the proof: `Library.price()` recursed two levels deep (Library → books1/books2 → leaf products) without any caller knowing how deep the tree goes.
