# Chain of Responsibility Pattern — Notes & Q&A

Notes for [ChainOfResponse.java](ChainOfResponse.java): pass a request along a **chain of handlers** until one of them handles it. The sender doesn't know *which* handler will deal with it — each handler either **handles** the request or **forwards** it to the next. Example: an **approval hierarchy** — `Teacher` approves amounts < 1000, else it goes up to `Principal` (< 10000), else up to `Vp` (< 100000).

This is a **behavioural** pattern (about *how objects interact* — like Strategy, State & Command).

---

## Why we needed it — the problem

The sender knows only the **request** (an amount), not who's authorised to handle it. The naive version hardcodes the whole hierarchy into one place with an `if/else` that has to know every level:

```java
void approve(int amount){
    if (amount < 1000)        System.out.println("Teacher approves");
    else if (amount < 10000)  System.out.println("Principal approves");
    else if (amount < 100000) System.out.println("Vp approves");
    else                      System.out.println("cannot approve");
    // ❌ add a "Dean" tier, reorder limits, or change a threshold → edit this method every time
    // ❌ the caller is now coupled to the ENTIRE org hierarchy
}
```

One method owns every level's rule, and the caller is glued to the whole structure. Every change to the hierarchy reopens this code.

## How it solves the problem

Give each level its **own** handler object that knows only **its own rule** and a link to the **next** handler. The request walks the chain until someone handles it:

```java
teacher.setNextApproval(principal);      // build the chain once
principal.setNextApproval(vp);
teacher.approve(9999);                   // teacher can't → forwards → principal handles
```

Adding a "Dean" tier = one new class spliced into the chain — the caller and the other handlers don't change. The caller just fires `approve(amount)` and never learns who handled it.

---

## The idea

> **Decouple the sender from the receiver.** Line handlers up; each one decides *"is this mine?"* by its **own criterion** — if yes it handles, if no it delegates to the next. The sender fires the request without knowing which (if any) handler will process it.

**Escalation analogy:** you raise a support ticket. L1 tries it; if it's beyond them it goes to L2, then L3. You (the sender) don't pick the level — the chain routes it.

```
client ──approve(n)──▶ Teacher ──▶ Principal ──▶ Vp ──▶ (null)
                       <1000?      <10000?       <100000?   "cannot approve"
                     handle│forward  handle│forward  handle│forward
```

---

## The key distinction — criterion, not a label

The pattern only earns its place when a handler's decision is its **own condition** (a threshold/range), **not** a string that names the handler:

```java
if (n < 1000) { ... }          // ✅ Teacher's OWN rule — caller passes 9999, not "teacher"
if (type.equals("teacher"))    // ❌ the request names the handler → just a switch in disguise
```

> **The tell for a *good* Chain of Responsibility:** from the request alone you **cannot** name the handler — the chain *discovers* it. If the request could name its handler, a `switch` or `Map<Key,Handler>` would be simpler and faster; you wouldn't need this pattern.

This file passes an **amount** (`9999`) — the caller genuinely doesn't know Principal will handle it. That's the pattern working for the right reason.

---

## The 3 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Handler** (abstract) | `ApprovalList` | holds `nextApproval`, the `setNextApproval()` link, and abstract `approve(int)` |
| **Concrete Handlers** | `Teacher` (<1000), `Principal` (<10000), `Vp` (<100000) | each checks **its own** threshold; handle or forward |
| **Client** | `ChainOfResponse.main()` | builds the chain, sends requests |

```
ChainOfResponse.main()
   ├─ teacher.setNextApproval(principal)     // wire the chain
   ├─ principal.setNextApproval(vp)
   └─ teacher.approve(99999)
         └─ Teacher: 99999<1000?  no  → forward
              └─ Principal: 99999<10000? no → forward
                   └─ Vp: 99999<100000? yes → "Approved by Vp"
```

---

## UML class diagram

```
                 ┌────────────────────────────────┐
                 │      ApprovalList (abstract)     │◀────┐
                 │ # nextApproval: ApprovalList     │     │ association
                 │ + setNextApproval(ApprovalList)  │─────┘ (points to next handler)
                 │ + approve(int) {abstract}        │
                 └───────────────▲─────────────────┘
             extends  ╱          │          ╲  extends
                     ╱           │           ╲
            ┌──────────┐  ┌────────────┐  ┌──────────┐
            │  Teacher │  │  Principal │  │    Vp    │
            │ +approve │  │ +approve   │  │ +approve │
            │ (<1000)  │  │ (<10000)   │  │ (<100000)│
            └──────────┘  └────────────┘  └──────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Inheritance** | `Teacher`/`Principal`/`Vp` → `ApprovalList` | solid line, hollow triangle △ | each `extends ApprovalList` |
| **Self-association** | `ApprovalList` → `ApprovalList` | plain arrow → (to itself) | the `nextApproval` **field** links each handler to the next — a "has-a next" that forms the chain |

> The self-reference (`ApprovalList nextApproval`) is what makes this a **linked list of handlers** — exactly the comment at the top of your file. It's a *field* (association), not a transient `new` (dependency): the link persists for the object's life.

---

## SOLID — Chain of Responsibility scores well

> Note: **Singleton is *not* SOLID** — it's a separate creational pattern. The **S** in SOLID is **Single Responsibility**.

| SOLID | Followed? | Why, in this code |
|---|---|---|
| **S** — Single Responsibility | ✅ | each handler owns exactly one level's rule; no class knows the whole hierarchy |
| **O** — Open/Closed | ✅ **headline** | add a `Dean` tier → new class, splice into the chain, **no existing handler edited** |
| **L** — Liskov Substitution | ✅ | any `ApprovalList` substitutes for the base; the client and the `nextApproval` link work with all |
| **I** — Interface Segregation | ✅ | the handler contract is tiny (`approve`) — nothing implements what it doesn't use |
| **D** — Dependency Inversion | ✅ | handlers depend on the `ApprovalList` **abstraction** for their `next`, not on concrete successors |

**Open/Closed — the reason it beats the big `if/else`:**
```java
class Dean extends ApprovalList {                 // NEW tier...
    void approve(int n){
        if (n < 500000) System.out.println("Approved by Dean");
        else if (nextApproval != null) nextApproval.approve(n);
        else System.out.println("cannot approve");
    }
}
vp.setNextApproval(new Dean());                   // ...splice in — nothing else changes
```

> Contrast with **State** (your other note), where transitions live *inside* the states and DIP is strained by `new Successor()`. Here the successor is **injected** via `setNextApproval` and typed as the abstraction — so CoR keeps DIP clean where State bends it.

---

## Common notes & lessons

### 1. Strict `<` — mind the boundaries
Thresholds use `<`, so the edges are exclusive: exactly `1000` is **not** approved by Teacher (needs `< 1000`) and flows up to Principal, and any amount `≥ 100000` reaches the end of the chain → `"cannot approve"`. Switch to `<=` if the limits should be inclusive. Intentional either way — just know where the edge falls.

### 2. Always give the chain a tail
Every handler's `else` ends in either forward-or-`"cannot approve"`, so a request that nobody handles still gets a clear answer instead of silently vanishing. That terminal fallback is what makes the chain safe — never leave the last handler able to drop a request with no output.

### 3. The chain order is a runtime decision
The hierarchy isn't baked into the classes — it's built in `main` with `setNextApproval`. Reorder the wiring and you reorder the escalation, without touching any handler. That flexibility is a core selling point of the pattern.

### 4. Naming (fixed)
The field/setter are now consistently `nextApproval` / `setNextApproval` (previously `nextAproval` / `nextAprroval`), the message reads "principal", and the client's local variable is `teacher` (was `Teacher`, which shadowed the class name). Cosmetic — the logic was already correct.

---

## Chain of Responsibility vs a plain `switch`/`Map`

| | **Chain of Responsibility** (this) | **`switch` / `Map<Key,Handler>`** |
|---|---|---|
| Request carries | data to *evaluate* (an amount) | a key that *names* the handler |
| Who decides | the handlers, by their own rules | the caller, up front |
| Multiple handlers? | yes — each can act then forward | no — one lookup, one handler |
| Best when | criterion is a range/condition; sender shouldn't know the receiver | exact key → exact handler |

> If your matching key *is* the handler's name, prefer a `switch`/`Map`. Reach for CoR when the decision is a **condition** and the sender must stay **decoupled** from the receiver — like this amount-threshold approval.

---

## When to use Chain of Responsibility

- A request could be handled by **one of several** handlers and the sender **shouldn't know which**.
- Handlers decide by their **own criteria** (thresholds, ranges, conditions) — not by a label naming them.
- You want to **add/reorder handlers** without touching the sender or the other handlers.
- Real-world: approval/escalation chains, logging by severity, web **middleware** (auth → rate-limit → route), ATM cash dispensing, UI event bubbling, and `try/catch` (the language's built-in CoR).

> The win: each handler is one small class owning one rule (SRP), a new handler is a new class spliced into the chain (Open/Closed), and the sender stays decoupled from every receiver.

---

## Run it

```bash
cd "Chain Of Responsibility"
javac ChainOfResponse.java
java ChainOfResponse
```

Output:
```
Approved by teacher     ← 999   < 1000
Approved by principal   ← 9999  < 10000   (teacher forwarded it up)
Approved by Vp          ← 99999 < 100000  (teacher → principal → vp)
```

Each request enters at `teacher` and climbs the chain until a handler's threshold covers it — the client never names the approver.
