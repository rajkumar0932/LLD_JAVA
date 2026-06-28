# Decorator Pattern — Notes & Q&A

Notes for [Decorator.java](Decorator.java): add behaviour to an object **at runtime** by wrapping it in another object that shares the same interface — without changing the original class or exploding into subclasses.

This is a **structural** pattern (like Adapter — about *connecting* objects, not *making* them). Where **Adapter** changes an interface so two things fit, **Decorator** keeps the same interface and **adds extra behaviour**.

---

## The idea

> **A wrapper that has the same type as the thing it wraps**, so you can stack wrappers to keep adding behaviour — each layer adds a bit and forwards the rest.

**Pizza analogy:** start with a plain pizza, then wrap it with "extra butter", then wrap *that* with "extra cheese". Each wrapper is still a `Pizza`, so the next wrapper doesn't care whether it's holding a plain pizza or an already-decorated one.

```
PlainPizza → [ExtraButter] → [ExtraChesse]
  (base)      (decorator)     (decorator)
   150          +50             +40          = 240
```

---

## The 4 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Component** | `Pizza` interface | the common type (`name()`, `price()`) — base *and* decorators implement it |
| **Concrete Component** | `PlainPizza`, `VeggiesPizza`, `NonvegPizza` | the real object being decorated |
| **Base Decorator** | `PizzaDecorator` (abstract) | implements `Pizza` **and holds a `Pizza`** — the wrapped object |
| **Concrete Decorator** | `ExtraButter`, `ExtraChesse` | add behaviour, then call through to the wrapped object |

```
ExtraChesse ──wraps──▶ ExtraButter ──wraps──▶ PlainPizza
   (all three are Pizza — that's why they stack)
```

---

## UML class diagram

```
                          ┌──────────────────┐
                          │   «interface»    │
                          │      Pizza       │
                          │ + name(): String │
                          │ + price(): int   │
                          └──────────────────┘
              realize ╱╲        ▲   ▲        ╱╲ realize
          ┌╌╌╌╌╌╌╌╌╌╌    ╌╌╌╌╌╌╌┘   └╌╌╌╌╌╌╌    ╌╌╌╌╌╌╌╌╌┐
          ┆ (implements)                       (implements) ┆
 ┌────────────────┐  ┌────────────┐  ┌───────────┐         ┆
 │  VeggiesPizza  │  │ NonvegPizza│  │ PlainPizza│         ┆
 └────────────────┘  └────────────┘  └───────────┘         ┆
                                                            ┆ realize
                          ┌──────────────────────┐          ┆ (implements)
                          │   «abstract class»   │◇╌╌╌╌╌╌╌╌╌╌┘
            aggregation ◇─┤    PizzaDecorator    │
          (has-a Pizza,   │  - name: Pizza       │
           passed in)     │  PizzaDecorator(Pizza)│
                          └──────────────────────┘
                              ▲              ▲
                    inherit  ╱ ╲   (extends) ╱ ╲
                  ┌──────────────┐      ┌──────────────┐
                  │  ExtraButter │      │ ExtraChesse  │
                  │ + name():Str │      │ + name():Str │
                  │ + price():int│      │ + price():int│
                  └──────────────┘      └──────────────┘
```

**The three relationships (don't mix them up):**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | concrete pizzas → `Pizza` | dashed line + hollow ▷ | they `implements Pizza` |
| **Realization** | `PizzaDecorator` → `Pizza` | dashed line + hollow ▷ | the decorator **also** `implements Pizza` |
| **Aggregation** | `PizzaDecorator` ◇→ `Pizza` | hollow diamond ◇ on decorator side | holds a `Pizza` field, **passed in** (not `new`-ed) → aggregation, not composition |
| **Inheritance** | `ExtraButter`/`ExtraChesse` → `PizzaDecorator` | solid line + hollow ▷ | they `extends PizzaDecorator` |

> **The key subtlety:** `PizzaDecorator` has **two** links to `Pizza` — it **is-a** `Pizza` (realization, so it can be wrapped again) **and has-a** `Pizza` (aggregation, the thing it wraps). That double relationship is the whole reason decorators stack. A diagram that draws only the has-a is incomplete.

---

## The key mechanic — "is-a AND has-a"

A decorator is special because it does **both** at once:

```java
abstract class PizzaDecorator implements Pizza {  // ① IS-A Pizza  (so it can be wrapped again)
   protected Pizza name;                          // ② HAS-A Pizza (the thing it wraps)
   PizzaDecorator(Pizza name){
    this.name = name;
   }
}

class ExtraButter extends PizzaDecorator {
    public ExtraButter(Pizza name){ super(name); }
    public String name(){
       return name.name() + " extra butter";      // ③ add my bit, then forward to inner
    }
    public int price(){
        return name.price() + 50;                  // ③ inner price + my surcharge
    }
}
```

**"Implements the type, and holds the type."** Because a decorator *is* a `Pizza`, you can feed a decorated pizza into another decorator — that's what makes them stack infinitely.

---

## How the layering works (the `main`)

```java
Pizza pizza = new PlainPizza();                                 // 150
Pizza pizzaWithButter = new ExtraButter(pizza);                 // 150 + 50
Pizza pizzaWithButterExtraCheese = new ExtraChesse(pizzaWithButter); // + 40
```

Calling `name()` on the outermost wrapper triggers a **chain of forwarded calls** inward, and the strings build up on the way back out:

```
ExtraChesse.name()
  → ExtraButter.name()
      → PlainPizza.name()  → "This is plain pizza"
      ← "This is plain pizza extra butter"
  ← "This is plain pizza extra butter extra cheese"
```

---

## Why not just use inheritance / subclasses?

Without Decorator you'd need a subclass for **every combination**: `PlainPizza`, `PlainPizzaButter`, `PlainPizzaButterCheese`, `VeggiesPizzaButter`, … — a **class explosion**. Decorator gives you the combinations at **runtime** by composing small wrappers, instead of one frozen class per combination.

| Inheritance | Decorator |
|---|---|
| behaviour fixed at **compile time** | behaviour chosen at **runtime** |
| N features → up to 2ⁿ subclasses | N features → N small wrappers |
| rigid | mix & match, any order |

---

## Common errors & lessons (hit while building this file)

### 1. `private static void main` → runs nothing

```java
private static void main(String[] arg){ ... }   // ❌ compiles, but JVM can't see it
```
The JVM entry point **must be `public`**. With `private` you get at runtime:
```
Error: Main method not found in class Decorator, please define the main method as:
   public static void main(String[] args)
```
Fix: `public static void main(String[] arg)`.

> It **compiles fine** — the error only shows at *run* time, because the JVM looks specifically for a `public static void main(String[])`.

### 2. Wrong wrapper — name says cheese, code adds butter

```java
Pizza pizzaWithButterExtraCheese = new ExtraButter(pizzaWithButter);  // ❌ butter twice
```
The variable is named `...ExtraCheese` but it wrapped with `ExtraButter` again, so cheese was never added. Silent **logic bug** — no compiler complaint, just wrong output. Fix:
```java
Pizza pizzaWithButterExtraCheese = new ExtraChesse(pizzaWithButter);  // ✅ adds cheese
```

### 3. `price()` is computed but never printed

The decorators correctly add surcharges (`+50`, `+40`), but `main` only prints `name()`. To see the cumulative cost, print it too:
```java
System.out.println(pizzaWithButterExtraCheese.price());   // 240
```

### 4. Duplicate class names break the Run button

All files in this repo share the **default package** (no `package` line). If two classes have the same name anywhere in the project, VS Code's Java extension refuses to run. Keep every class name **unique across the repo**.

---

## Connection to earlier patterns

- **Adapter** (previous structural pattern): also wraps an object — but to **change** its interface so it fits. Decorator **keeps** the interface and **adds** behaviour.
- The base decorator depends on the **`Pizza` interface**, not concrete pizzas — "program to an interface" / **Dependency Inversion (D)**. That's *why* any pizza (plain, veggie, or already-decorated) slots in.

---

## When to use Decorator

- You want to add responsibilities to objects **dynamically and transparently** (without affecting other objects).
- You want to **avoid a subclass explosion** from many independent, combinable features.
- You need to **stack** features in any combination/order at runtime.

Real-world: Java I/O streams (`new BufferedReader(new InputStreamReader(System.in))`) are the classic Decorator chain.

---

## Run it

```bash
cd Decorator
javac Decorator.java
java Decorator
```

Output:
```
This is plain pizza
This is plain pizza extra  butter
This is plain pizza extra  butter extra  cheese
```
