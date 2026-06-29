# Bridge Pattern — Notes & Q&A

Notes for [Bridge.java](Bridge.java): split **two things that vary independently** into two separate hierarchies and connect them with a reference, so you combine them instead of multiplying classes. Example: **devices** (`Mobile`/`Pc`/`Tablet`) × **video quality** (`HD`/`QHD`/`K4`) — any device plays at any quality.

This is a **structural** pattern (like Adapter, Decorator, Facade, Composite & Proxy — about *connecting* objects).

---

## Why we needed it — the problem

Two dimensions vary at once: **device** and **quality**. If you bake both into one class hierarchy, you need a class for **every combination**:

```
MobileHD, MobileQHD, Mobile4K,
PcHD,     PcQHD,     Pc4K,
TabletHD, TabletQHD, Tablet4K       ❌ 3 devices × 3 qualities = 9 classes
```

Add one device or one quality and the count jumps by a whole row/column. It **multiplies** — `n × m`.

## How it solves the problem

Put each dimension in its **own** hierarchy and give the device a **reference** to the quality (the "bridge"). Now you combine them at runtime instead of pre-building every pair:

```java
new Mobile(new HD()).play();   // Mobile + HD
new Mobile(new K4()).play();   // SAME Mobile class, now 4K
new Pc(new K4()).play();       // Pc + 4K
```

```
3 devices + 3 qualities = 6 classes      ✅ additive (n + m), not multiplicative
```

One `Mobile` class works with **every** quality, chosen by the caller — so the two dimensions vary independently.

---

## The idea

> **Decouple an abstraction from its implementation so the two can vary independently** — by having the abstraction *hold a reference* to the implementation rather than inherit from it.

**Two-islands analogy:** the device hierarchy is one island, the quality hierarchy is another. The reference field is the **bridge** between them — each island grows on its own, and the bridge connects any pair.

```
   Abstraction side              Implementor side
   DeviceQualityChecker          VideoQuality
    ├─ Mobile  ──[ vquality ]──▶  ├─ HD
    ├─ Pc          (bridge)       ├─ QHD
    └─ Tablet                     └─ K4
```

---

## The 4 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Abstraction** | `DeviceQualityChecker` (abstract) | holds the `VideoQuality` reference; declares `play()` |
| **Refined Abstraction** | `Mobile`, `Pc`, `Tablet` | extend the abstraction; implement `play()` by delegating to the quality |
| **Implementor** | `VideoQuality` interface | the contract for the other dimension (`qualityName()`) |
| **Concrete Implementor** | `HD`, `QHD`, `K4` | the actual quality behaviours |
| **Client** | `Bridge.main()` | mixes any device with any quality at runtime |

```
Bridge.main()
   └─▶ new Mobile(new HD()).play()
          └─ Mobile.play()  →  vquality.qualityName()   // delegates across the bridge
```

---

## UML class diagram

```
   ┌─────────────────────────────┐          ┌──────────────────────┐
   │  DeviceQualityChecker (abs) │  vquality│ «interface»          │
   │ - vquality: VideoQuality  ◇─┼─────────▶│   VideoQuality       │
   │ + play(): void {abstract}   │          │ + qualityName(): void│
   └──────────────▲──────────────┘          └──────────▲───────────┘
       extends    │   extends                  realizes │
      ┌───────────┼───────────┐          ┌──────────────┼──────────────┐
 ┌─────────┐ ┌─────────┐ ┌─────────┐  ┌──────┐     ┌──────┐      ┌──────┐
 │ Mobile  │ │   Pc    │ │ Tablet  │  │  HD  │     │ QHD  │      │  K4  │
 │ + play()│ │ + play()│ │ + play()│  └──────┘     └──────┘      └──────┘
 └─────────┘ └─────────┘ └─────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Inheritance** | `Mobile`/`Pc`/`Tablet` → `DeviceQualityChecker` | solid line, hollow triangle △ | `extends` the abstraction |
| **Realization** | `HD`/`QHD`/`K4` → `VideoQuality` | dashed, hollow triangle ▷ | `implements` the implementor |
| **Aggregation** | `DeviceQualityChecker` ◇→ `VideoQuality` | hollow diamond ◇ on the abstraction side | the quality is **passed in** via the constructor (created outside) → aggregation, not composition |

> **The bridge is the aggregation arrow** (`DeviceQualityChecker ◇→ VideoQuality`). That single reference is what lets the two hierarchies vary independently — it's the namesake of the pattern.

---

## The key insight — "the bridge is the field, not the base class"

The bridge is **holding the implementor as an interface reference and delegating to it**:

```java
abstract class DeviceQualityChecker {
    VideoQuality vquality;                       // ⬅️ THE BRIDGE
    DeviceQualityChecker(VideoQuality q){ this.vquality = q; }
    abstract public void play();
}
class Mobile extends DeviceQualityChecker {
    Mobile(VideoQuality q){ super(q); }
    public void play(){
        System.out.print("Mobile: ");
        vquality.qualityName();                  // ⬅️ delegate across the bridge
    }
}
```

What would **throw the bridge away** is hardcoding the implementor instead of referencing the interface:

```java
public void play(){
    new HD().qualityName();   // ❌ Mobile welded to HD → need Mobile4K, MobileQHD… → n×m is back
}
```

| Code shape | Bridge? | Device polymorphism? |
|---|---|---|
| device holds `VideoQuality` field **+** shared abstract base | ✅ full Bridge | ✅ yes |
| device holds `VideoQuality` field, **no** base class | ✅ still bridging | ❌ no (devices share no type) |
| device does `new HD()` inside `play()` | ❌ no bridge | ❌ no |

> The abstract base (`DeviceQualityChecker`) is a **separate** decision from the bridge. It earns its keep by (1) holding the `vquality` field + constructor **once**, and (2) giving every device a **common type** so the client can handle "any device" via `List<DeviceQualityChecker>`. Drop it only if you never need that polymorphism.

---

## Bridge vs Decorator (they look almost identical)

Both have "a class that holds a reference to an interface." The tell:

| | **Bridge** | **Decorator** |
|---|---|---|
| Hierarchies | **two** different types (`Device` holds `Quality`) | **one** type (`Pizza` holds `Pizza`) |
| Wrapper type vs held type | **different** → can't stack | **same** → can stack |
| Combination shape | pick **one** from each axis (`n × m`) | stack **many** from one set (`2ⁿ`) |
| Apply two at once? | No — one quality per device | Yes — butter **and** cheese both apply |
| Goal | **avoid n×m class explosion** | **add behaviour**, stackable |

> Both fight a class explosion — but Bridge fights the **n×m** (one-from-each-axis) kind, while Decorator fights the **2ⁿ** (stack-any-subset) kind. Decorator's wrapper *is the same type* as what it holds (so it stacks); Bridge's abstraction holds a *different* type (so it doesn't).

---

## Common errors & lessons (hit while building this file)

### 1. "This is just composition" — when it's *not* a Bridge yet
An earlier version had a single concrete `DeviceQualityChecker` holding **both** `Device` and `VideoQuality` and just calling each. That's **composition / two strategies**, not Bridge — there was no abstraction *hierarchy* and no abstraction→implementation delegation. Bridge needs one side to be an **abstraction with refined subclasses** that **delegates** to the implementor.

### 2. `abstract` class with nothing extending it
A class should be `abstract` **only if something extends it**. An abstract `DeviceQualityChecker` with no subclasses can't be instantiated and does nothing — either give it refined abstractions (`Mobile`/`Pc`/`Tablet`) or make it concrete.

### 3. Storing the implementor but never using it
`play()` printed only `"Mobile"` and never called `vquality.qualityName()` — so the bridge field looked pointless. The abstraction must **delegate** to the implementor for the pattern to mean anything.

### 4. Using the constructor parameter outside the constructor
```java
Pc(VideoQuality vd){ vquality = vd; }
public void play(){ vd.qualityName(); }   // ❌ vd is out of scope here
```
`vd` exists only inside the constructor; in `play()` use the **field**: `vquality.qualityName()`.

### 5. Stale `main` after refactor
After changing the constructor to take one `VideoQuality` and `play()` to take no args, the old `new DeviceQualityChecker(new Mobile(), new HD()).play("...")` no longer compiled (abstract instantiation + wrong arg counts). Updated to `new Mobile(new HD()).play()`.

---

## When to use Bridge

- You have **two (or more) dimensions** that each vary — and you don't want a class per combination.
- You want to choose the combination **at runtime**, not bake it into the class hierarchy.
- You want the abstraction and its implementation to **evolve separately**.

> The win is **n + m instead of n × m** classes, plus runtime mixing — bought entirely by holding the implementor as an interface reference (composition over inheritance).

---

## Run it

```bash
cd "Bridge Pattern"
javac Bridge.java
java Bridge
```

Output:
```
Mobile: Video quality is HD
Pc: Video quality is 4k
Tablet: Video quality is QHD
Mobile: Video quality is 4k
```

The last line is the proof: the **same** `Mobile` class plays at 4K just by handing it a different `VideoQuality` — the two dimensions vary independently.
