# State Pattern — Notes & Q&A

Notes for [State.java](State.java): let an object **change its behaviour when its internal state changes**, so it *appears to change its class*. Each state is its own object, and the object delegates to whichever state it currently holds. Example: a student's **learning journey** through backend — `LearnJS → LearnNode → LearnCacheAndDB → BuildProject`, with a `Drop` state reachable from anywhere.

This is a **behavioural** pattern (about *how objects behave* — like Strategy, Template Method & Command).

---

## Why we needed it — the problem

Behaviour depends on "what phase am I in," so the naive version is a big `if/else`/`switch` on a state field that you must edit for every phase **and** every action:

```java
void next(){
    if (phase.equals("LearnJS"))            phase = "LearnNode";
    else if (phase.equals("LearnNode"))     phase = "LearnCacheAndDB";
    else if (phase.equals("LearnCacheAndDB")) phase = "BuildProject";
    else if (phase.equals("BuildProject"))  System.out.println("finished");
    // ❌ add a phase → crack open EVERY method (next/drop/name) and add a branch
}
```

The transition rules get scattered and duplicated across every action method, and a new phase means editing all of them — easy to get an order or a case wrong.

## How it solves the problem

Make each phase its own class behind a common interface, and let the context **delegate** to whichever one it currently holds. Each state knows **its own** next/drop behaviour:

```java
phase.nextPhase();   // internally: this.phase = new LearnNode();  (LearnJS decided that)
phase.drop();        // internally: this.phase = new Drop();
```

Adding a phase = writing **one new class**; the transition logic lives *inside* the states, not in a central switch.

---

## The idea

> **Encapsulate each state as an object**, give them a common interface, and let the context delegate to its current state — the state objects decide the transitions, so changing state = swapping the object the context points to.

**Traffic-light analogy:** Red, Green, Yellow are states; each knows which light comes *next*. The light fixture (context) doesn't branch on colour — it just asks the current state to advance.

```
client ──▶ CurrrentPhase ──delegates──▶ Phase (LearnJS | LearnNode | … | Drop)
           (the context)                the current state decides the next state
```

---

## The 3 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Context** | `CurrrentPhase` | holds the current `Phase`, delegates `next()/drop()/namePhase()` to it |
| **State** (interface) | `Phase` | the common contract every phase implements |
| **Concrete States** | `LearnJS`, `LearnNode`, `LearnCacheAndDB`, `BuildProject`, `Drop` | each phase's behaviour **and** its own transitions |

```
State.main()
   └─▶ new CurrrentPhase()            // starts in LearnJS (default)
          ├─ nameCurrentPhase()  →  LearnJS.namePhase()
          ├─ nextPhase()         →  LearnJS.next(this)   → setPhase(new LearnNode())
          ├─ nextPhase()         →  LearnNode.next(this) → setPhase(new LearnCacheAndDB())
          └─ drop()              →  LearnCacheAndDB.drop(this) → setPhase(new Drop())
```

> The distinctive move: **the state passes the context to itself** — `phase.next(this)` — so the current state can call `curr.setPhase(...)` and swap in its successor. That's what makes transitions *decentralized*.

---

## UML class diagram

```
                 ┌──────────────────────────┐
                 │     «interface» Phase     │
                 │ + next(CurrrentPhase)     │
                 │ + drop(CurrrentPhase)     │
                 │ + namePhase()             │
                 └─────────────▲────────────┘
        realizes  ╱   ╱    │    ╲    ╲  realizes
                 ╱   ╱     │     ╲    ╲
          ┌────────┐ ┌────────┐ ┌──────────────┐ ┌────────────┐ ┌──────┐
          │ LearnJS│ │LearnNode│ │LearnCacheAndDB│ │BuildProject│ │ Drop │
          └────────┘ └────────┘ └──────────────┘ └────────────┘ └──────┘
                         ▲ (holds one current Phase)
                         ◇ aggregation
                 ┌────────────────────────────┐
                 │        CurrrentPhase        │
                 │ - phase: Phase              │
                 │ + setPhase(Phase)           │
                 │ + nextPhase() / drop()      │
                 │ + nameCurrentPhase()        │
                 └────────────────────────────┘
```

**The relationships here** (exactly what we discussed):

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | each concrete state → `Phase` | dashed, hollow triangle ▷ | each `implements Phase` |
| **Aggregation** | `CurrrentPhase` ◇→ `Phase` | hollow diamond ◇ on the context | `phase` is a **field**, and it's **reassigned** over time (`setPhase`) — held, not owned-for-life → aggregation, not composition |
| **Dependency** («create») | each state ┈▷ its successor + `Drop` | dashed arrow ┈▷ | `LearnJS.next()` does `new LearnNode()`; every non-terminal state does `new Drop()` — transient *use*, not a stored field |

> **Field vs local use** is the tell: `CurrrentPhase` *stores* a `Phase` (aggregation ◇), while a state merely `new`s a successor inside a method and hands it off (dependency ┈▷).

The successor dependencies:
```
LearnJS         ┈▷ LearnNode,       Drop
LearnNode       ┈▷ LearnCacheAndDB, Drop
LearnCacheAndDB ┈▷ BuildProject,    Drop
BuildProject    ┈▷ Drop
Drop            ┈▷ (none — terminal, just prints)
```

---

## Composition, not inheritance — why the pattern works

`CurrrentPhase` **has-a** `Phase` (composition/aggregation); it does **not** inherit from it. That's what lets behaviour change **at runtime**:

```java
phase.nextPhase();   // this.phase = new LearnNode();  — behaviour swapped while running
```

If the context *inherited* its behaviour, the "state" would be fixed at compile time. Composition is what gives State its flexibility — the classic **"favour composition over inheritance."**

---

## SOLID — where this implementation stands

> Note: **Singleton is *not* SOLID** — it's a separate creational pattern. The **S** in SOLID is **Single Responsibility**.

| SOLID | Followed? | Why, in this code |
|---|---|---|
| **S** — Single Responsibility | ✅ | each state owns one phase's behaviour; `CurrrentPhase`'s one job is "hold current state + delegate" |
| **O** — Open/Closed | ⚠️ mostly | adding a phase = a new class, but **inserting one mid-sequence** means editing the predecessor's `next()`; the default state is hardcoded in the constructor |
| **L** — Liskov Substitution | ✅ | any concrete state substitutes for `Phase`; the context works with all of them. (**N/A to `CurrrentPhase`** — it has no subclasses) |
| **I** — Interface Segregation | ✅ | `Phase` is small (3 related methods); every state uses all of them |
| **D** — Dependency Inversion | ❌ **the weak spot** | concrete classes are instantiated with `new` — both in the context **and** in the states |

**The DIP violation — spelled out.** The field types are good abstractions (`Phase phase`), but concretions get `new`ed in two places:

```java
CurrrentPhase(){ this.phase = new LearnJS(); }   // context depends on a CONCRETE state
...
public void next(CurrrentPhase curr){ curr.setPhase(new LearnNode()); }  // state → concrete successor
```

`CurrrentPhase` is the *clearest* example (`new LearnJS()`), but the concrete states share the **same** DIP wrinkle via their `new Successor()` calls — so it isn't "context bad, states clean." They *do* honour **LSP** and **SRP**; it's **DIP** that's imperfect across the board.

**Closing the DIP gap** (optional): inject the initial state instead of hardcoding it,
```java
CurrrentPhase(Phase initial){ this.phase = initial; }   // depends only on the abstraction
```
and replace the `new`s with injected **singletons** (the states are stateless) or a **transition table/map**. That trades the pattern's readable, decentralized transitions for looser coupling — for a learning example the current version is the clearest way to *see* State, so keep it as-is.

---

## State vs Strategy — same shape, different intent

Both are "a context holding an interface reference and delegating," so the code looks identical. The tell is **who drives the change**:

| | **State** (this) | **Strategy** |
|---|---|---|
| Swaps | the **whole state**, changing many behaviours at once | one **algorithm/behaviour** |
| Who transitions | the **states themselves** (`curr.setPhase(...)`) | the **client** picks/injects |
| States know each other? | **yes** — each knows its successor | no — strategies are independent |
| Intent | model a **lifecycle/finite-state machine** | make one behaviour interchangeable |

> Rule of thumb: if the objects **decide the next object** (a lifecycle), it's **State**; if the **client** chooses an interchangeable behaviour, it's **Strategy**.

---

## Common notes & lessons

### 1. Terminal states
`BuildProject.next()` and `Drop`'s methods just **print** and don't transition — legitimate *terminal/absorbing* states. `Drop.drop()` printing `"already dropped"` is a nice self-loop guard.

### 2. Typo — `CurrrentPhase` (three r's)
Compiles fine because it's consistent, but rename to `CurrentPhase` for clarity.

### 3. Copy-paste in `namePhase()` messages
`LearnCacheAndDB` and `BuildProject` both say *"learning cache basics"* — the `BuildProject` message should describe building a project.

### 4. States are stateless → singleton candidates
Every concrete state has no fields, so `new LearnNode()` on each transition allocates needlessly. Making each a singleton removes the allocation (the `«create»` dependency stays, but you stop churning objects).

---

## When to use State

- An object's behaviour depends on **which of several modes/phases** it's in.
- You have a **lifecycle / finite-state machine** with defined transitions (workflow steps, order status, connection states, game phases).
- You're replacing a growing **`if/else`/`switch` on a state field** — spread across many methods — with polymorphism.

> The win: each state is one class that owns its own behaviour *and* its transitions, so the machine is readable and a new state is a new class rather than edits to every method.

---

## Run it

```bash
cd "State pattern"
javac State.java
java State
```

Output:
```
this phase about learning java script basics and strengthen it more
this phase about learning node.js basics and strengthen it more
this phase about learningcache basics and strengthen it more
this phase about taking rest for a while
```

The context starts in `LearnJS`, advances twice (`LearnNode` → `LearnCacheAndDB`), then `drop()` switches it to `Drop` — behaviour changing as the state object changes.
