# Composite Pattern — Notes & Q&A

Notes for [Composite.java](Composite.java): compose objects into **tree structures** (part-whole hierarchies) and let clients treat a **single object** and a **whole tree of objects** exactly the same way.

This is a **structural** pattern (like Adapter, Decorator, Facade — about *connecting* objects). Example: an **e-commerce cart** holding individual products *and* bundles — where a bundle can itself contain other bundles.

---

## The idea

> **One interface for both the leaf and the container**, so the client never asks "is this a single thing or a group?" — it just calls `getPrice()` / `display()` and the tree handles itself.

**Folder analogy:** a folder can hold files *and* other folders. "Get total size" works the same whether you point it at one file or the whole nested tree. The Composite Pattern is that folder/file uniformity, for code.

```
Cart
 ├─ Product: Atomic Habits            (leaf)
 ├─ Bundle: iPhone Combo              (composite)
 │   ├─ Product: iPhone 15
 │   └─ Product: AirPods
 └─ Bundle: Festive Mega Pack         (composite holding composites!)
     ├─ Bundle: iPhone Combo
     ├─ Product: Atomic Habits
     └─ Bundle: School Kit
```

---

## The 3 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Component** | `CartItem` interface | the shared type (`getPrice()`, `display()`) — leaf *and* composite implement it |
| **Leaf** | `Product` | an atomic item, **no children** — does the real work itself |
| **Composite** | `ProductBundle` | **holds a `List<CartItem>`**, delegates each call to its children (which may be leaves *or* other composites) |
| **Client** | `Composite.main()` | iterates a `List<CartItem>` — **no `instanceof`, no casts** |

```
                 CartItem (Component)
                 ╱              ╲
           Product            ProductBundle ──holds many──▶ CartItem
            (Leaf)             (Composite)        (back to the interface = recursion)
```

---

## The key mechanic — the recursive "has-a back to the interface"

```java
class ProductBundle implements CartItem {        // ① IS-A CartItem
    private List<CartItem> items = new ArrayList<>(); // ② HAS-MANY CartItem (leaf OR composite)

    public void addItem(CartItem item){ items.add(item); }  // accepts anything CartItem

    public double getPrice(){
        double total = 0;
        for (CartItem item : items) total += item.getPrice(); // ③ delegate — no type check
        return total;
    }
}
```

**Because the list is typed to the *interface*, a bundle can hold another bundle.** That self-reference (`Composite` contains `Component`, which can be another `Composite`) is what builds the tree and makes `getPrice()`/`display()` recurse for free.

---

## Why the naive version was worse (the problem it solves)

Without a shared interface, `Product` and `ProductBundle` are unrelated types, forcing:

```java
List<Object> cart = ...;                 // ❌ unsafe, loses abstraction
for (Object item : cart) {
    if (item instanceof Product) { ((Product) item).display(...); }       // ❌ type checks
    else if (item instanceof ProductBundle) { ((ProductBundle) item)... } // ❌ breaks polymorphism
}
```

- `instanceof` + casts everywhere → polymorphism broken
- `List<Object>` → unsafe, no abstraction
- a bundle **couldn't contain another bundle** → no recursion
- display/price logic **duplicated** per type

Composite replaces all of that with **one `List<CartItem>` and one loop**.

---

## Leaf vs Composite — the distinction

| | **Leaf** (`Product`) | **Composite** (`ProductBundle`) |
|---|---|---|
| children? | none | holds many `CartItem` |
| `getPrice()` | returns its own price | **sums** children's prices |
| `display()` | prints itself | prints itself **then recurses** into children |
| can nest? | no | yes — including other composites |

---

## Composite vs the other structural patterns

| Pattern | Shape | Goal |
|---|---|---|
| **Adapter** | wraps **one** class | change its interface to fit |
| **Decorator** | wraps **one** object (same type), stackable | add behaviour |
| **Facade** | wraps **a subsystem** (many classes) | simplify to one door |
| **Composite** | a **tree** of same-typed objects | treat single & group **uniformly**, recursively |

> Decorator and Composite look similar (both "is-a + has-a the interface"). Difference: a **Decorator wraps exactly one** component to *add* to it; a **Composite holds many** to *aggregate* them into a tree.

---

## Lessons / gotchas

### 1. Type the collection to the interface, not the concrete class
`List<CartItem>` (not `List<Product>`) is what lets a bundle hold both leaves and other bundles. Typing to the interface **is** the pattern.

### 2. Unique class names (default package)
This repo has no `package` lines, so every class shares the default package. Generic names like `Main`/`Product` can collide and break VS Code's Run button — `main` lives inside the `Composite` class here to stay unique across the repo.

### 3. Currency symbol & encoding
Used `Rs ` instead of `₹` — on Windows, `javac`/console default encoding can garble non-ASCII source/output.

---

## When to use Composite

- Your objects form a **tree / part-whole hierarchy** (folders, menus, org charts, bundles).
- You want operations on a **single item and a group** to look identical (total price, render, count).
- You want to **keep client code free of `instanceof`** and let polymorphism handle leaf-vs-group.

**Pros:** uniform treatment, easy to extend (OCP), clean client code, natural recursion.
**Cons:** can over-generalize (a leaf may not meaningfully support `addItem`); overkill for flat lists; can blur important type distinctions.

---

## Run it

```bash
cd "Composite Pattern"
javac Composite.java
java Composite
```

Output:
```
Your Amazon Cart:
  Product: Atomic Habits - Rs 499.0
  Bundle: iPhone Essentials Combo
    Product: iPhone 15 - Rs 79999.0
    Product: AirPods - Rs 15999.0
    Product: 20W Charger - Rs 1999.0
  Bundle: Festive Mega Pack
    Bundle: iPhone Essentials Combo
      Product: iPhone 15 - Rs 79999.0
      Product: AirPods - Rs 15999.0
      Product: 20W Charger - Rs 1999.0
    Product: Atomic Habits - Rs 499.0
    Bundle: Back to School Kit
      Product: Notebook Pack - Rs 249.0
      Product: Pen Set - Rs 99.0

Total: Rs 197340.0
```

Notice the **deeper indentation** on the nested bundle — that's `display()` recursing through the tree with zero extra code.
