# Mediator Pattern — Notes & Q&A

Notes for [Mediator.java](Mediator.java): route all communication between a set of objects through **one central hub** (the *mediator*), so the objects never talk to each other directly. Example: chat users who don't hold references to each other — each one sends to a `UserMediator`, and the mediator fans the message out to everyone else.

This is a **behavioural** pattern (about *how objects interact* — like Observer, Strategy, State & Command).

---

## Why we needed it — the problem

When many objects talk **directly** to each other, you get an N-to-N mesh: every user needs a reference to every other user, and adding one means wiring it to all the rest.

```java
class User {
    User a, b, c, d;                 // ❌ each user references every other user
    void send(String m){ a.receive(m); b.receive(m); c.receive(m); ... }
}
// 4 users → up to 12 directed links; 10 users → 90. Adding a user touches everyone.
```

The connections explode (**O(N²)**), every object knows about every other, and changing the interaction rules means editing all of them. That tight coupling is exactly what gets "messy in the long run" (your top comment).

## How it solves the problem

Put **one** object in the middle. Everyone talks only to the mediator; the mediator knows the group and coordinates:

```java
user1.doChange("hello everyone");   // user1 tells the mediator ONLY
// UserMediator.sendToAll → delivers to user2, user3, user4
```

Now each user has **one** link (to the mediator), not N-1 links to peers. The N-to-N mesh becomes a **hub-and-spoke**. Add a user → register it once with the mediator; nobody else changes.

---

## The idea

> **Replace many-to-many object references with a central coordinator.** Objects (colleagues) don't reference each other — they reference the mediator, and the mediator owns the interaction logic.

**Air-traffic-control analogy:** planes don't negotiate landings with each other directly (chaos). They all talk to the control tower (mediator); the tower coordinates. Add a plane → it just tunes into the tower.

```
   user1 ─┐                       ┌─ user2
          ├──▶  UserMediator  ◀──┤
   user4 ─┘     (the hub)         └─ user3
   each user ↔ mediator only; never user ↔ user
```

---

## The 3 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Mediator** (interface) | `MediatorGuy` | declares `addUser`, `sendToAll` |
| **Concrete Mediator** | `UserMediator` | holds the list of users, coordinates who-hears-what |
| **Colleague** | `User` | talks **only through** the mediator; never to another `User` directly |

```
Mediator.main()
   ├─ userMediator.addUser(user1..4)     // register colleagues with the hub
   └─ user1.doChange("hello everyone")
         └─ userMediator.sendToAll("hello everyone", user1)
               ├─ user2.receiveMessage(..., sender=user1)
               ├─ user3.receiveMessage(..., sender=user1)
               └─ user4.receiveMessage(..., sender=user1)
               (skips user1 via  if (ls.get(i) != user) )
```

> The defining property: **no `User` holds a reference to another `User`.** `user1` only knows the mediator. That's what turns the O(N²) mesh into O(N) spokes.

---

## UML class diagram

```
      ┌───────────────────────────┐          ┌──────────────────────────┐
      │  «interface» MediatorGuy  │◀────────│          User            │
      │  + addUser(User)          │  depends │ - user: String           │
      │  + sendToAll(String,User) │  on      │ - userMediator: MediatorGuy│
      └─────────────▲─────────────┘          │ + doChange(String)       │
              realizes │                      │ + receiveMessage(String,User)│
      ┌─────────────────────────┐             └──────────────────────────┘
      │      UserMediator       │  ◇───────────────▶ holds many Users
      │ - ls: List<User>        │      aggregation
      │ + addUser / sendToAll   │
      └─────────────────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | `UserMediator` → `MediatorGuy` | dashed, hollow triangle ▷ | `implements MediatorGuy` |
| **Aggregation** | `UserMediator` ◇→ `User` | hollow diamond ◇ | `List<User> ls` — the mediator **holds** users; they're created outside and outlive it → aggregation, not composition |
| **Association / dependency** | `User` → `MediatorGuy` | line / arrow | each `User` keeps a `MediatorGuy` reference (field) and calls through it |

> Note the two-way knowledge: the mediator holds the users (to broadcast), and each user holds the mediator (to send). But **users never hold each other** — that's the coupling the pattern removes.

---

## SOLID

> Note: **Singleton is *not* SOLID** — it's a separate creational pattern. The **S** in SOLID is **Single Responsibility**.

| SOLID | Followed? | Why, in this code |
|---|---|---|
| **S** — Single Responsibility | ✅ | `User` handles being a user; the **coordination logic** lives in `UserMediator`, not smeared across users |
| **O** — Open/Closed | ✅ | add a `User` → register it; change how broadcasting works → edit only `UserMediator`, not the users |
| **L** — Liskov Substitution | ✅ | any `MediatorGuy` substitutes for the interface; `User` works with any mediator |
| **I** — Interface Segregation | ✅ | `MediatorGuy` is small (`addUser`, `sendToAll`) |
| **D** — Dependency Inversion | ✅ **(just fixed)** | `User` now depends on the **`MediatorGuy` abstraction**, not the concrete `UserMediator` |

**The DIP fix that was applied:**
```java
private UserMediator userMediator;                 // ❌ before — concrete
User(String user, UserMediator userMediator){...}  //    depended on the implementation

private MediatorGuy userMediator;                  // ✅ after — abstraction
User(String user, MediatorGuy userMediator){...}   //    depends on the interface
```
`main` still creates the concrete `new UserMediator()` (the composition root) — but `User` no longer knows or cares which mediator it got. High-level and low-level both depend on `MediatorGuy`. That's the inversion.

---

## Mediator vs Observer — often confused

Both decouple "who tells whom," so they get mixed up. The difference is the **topology**:

| | **Mediator** (this) | **Observer** |
|---|---|---|
| Shape | **hub** — many colleagues ↔ one coordinator | **broadcast** — one subject → many subscribers |
| Direction | many-to-many, routed through the middle | one-to-many, one source of events |
| Who has the logic | the mediator owns *interaction rules* | subject just notifies; observers react |
| Colleagues know… | only the mediator | observers know the subject they follow |

> Rule of thumb: **Mediator** untangles a *mesh* of peers (chat, UI form fields that affect each other). **Observer** pushes events from *one* source to many listeners.

---

## Common notes & lessons

### 1. Skip-the-sender guard
`if (ls.get(i) != user)` in `sendToAll` stops the message echoing back to whoever sent it — reference comparison, correct here. Without it, `user1` would receive its own broadcast.

### 2. Sender vs recipient in the message
`receiveMessage(message, user)` prints `user.details()` — and `user` is the **sender** (passed down from `doChange` → `sendToAll`). So a broadcast from raj prints "from raj" on every recipient's line — the sender's identity, which is what you want.

### 3. Register before you broadcast
Users only receive if they've been `addUser`-ed into the mediator's list. (An earlier version left this out and printed nothing — the mediator was talking to an empty room.) Auto-registering in the `User` constructor (`mediator.addUser(this)`) is a tidier alternative to calling it in `main`.

### 4. Cosmetic
The output strings are missing a space (`everyonefrom  raj`) — pure formatting in `receiveMessage`'s `println`; and `doChange` reads oddly for "send" (`send()` would be clearer). Neither affects correctness.

---

## When to use Mediator

- Several objects communicate in a **tangled many-to-many** way and the wiring is getting unmanageable.
- You want to **centralise interaction rules** in one place instead of spreading them across the participants.
- You want colleagues **reusable/decoupled** — able to work without knowing about each other.
- Real-world: chat rooms, air-traffic control, UI dialogs where fields enable/disable each other, event/message buses, workflow coordinators.

> The win: N objects need **one** link each (to the mediator) instead of N-1 links to peers — the O(N²) mesh collapses to O(N) spokes, and the coordination logic lives in exactly one class.

---

## Run it

```bash
cd "Mediator Pattern"
javac Mediator.java
java Mediator
```

Output:
```
received hello everyonefrom  raj
received hello everyonefrom  raj
received hello everyonefrom  raj
received good nightfrom  sagar gupta
received good nightfrom  sagar gupta
received good nightfrom  sagar gupta
```

`user1` (raj) broadcasts to the three others → three "from raj" lines; then `user4` (sagar gupta) broadcasts → three "from sagar gupta" lines. No user ever called another user — every message went through the mediator.
