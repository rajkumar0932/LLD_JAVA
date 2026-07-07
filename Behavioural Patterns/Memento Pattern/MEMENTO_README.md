# Memento Pattern — Notes & Q&A

Notes for [Memento.java](Memento.java): capture an object's state as a **snapshot** so you can **restore it later (undo)** — *without* exposing the object's internals. Example: a resume editor that can save a checkpoint and revert to it, while the saved state stays sealed from everyone except the resume itself.

This is a **behavioural** pattern (about *how objects behave / interact* — like Command, State & Mediator).

---

## The one-line idea

> **Memento = Ctrl+Z.** Save a snapshot of an object, keep it in a history, and restore it on undo — while the snapshot stays *sealed* so only the original object can read it back.

That "sealed" part is the whole reason it's a pattern and not just "copy the fields."

---

## Why we needed it — the problem

The naive "snapshot" exposes everything, so anything can read or corrupt it:

```java
class ResumeSnapshot {
    public String name;             // ❌ public — anyone can read/tamper with the snapshot
    public List<String> skills;
}
```

Two problems: (1) the snapshot's guts are wide open (**breaks encapsulation**), and (2) whoever manages history now depends on the resume's internal field layout — change a field, and the snapshot code breaks too.

## How it solves the problem

Make the snapshot a **sealed nested class** that only the originator can read, and hand it to a separate **history** that just stores it blindly:

```java
public MementoSanpshot save(){ ... }          // only ResumeEditor creates snapshots
history.save(resume);                          // History HOLDS it, but can't peek inside
history.undo(resume);                          // restore the last checkpoint
```

The history can carry snapshots around but **cannot open them** — encapsulation preserved, undo enabled.

---

## The 3 roles (with this file's classes)

Think of a **video game save**:

| Role | Plain meaning | In this code |
|---|---|---|
| **Originator** | the thing you save/restore | `ResumeEditor` |
| **Memento** | the sealed save file (snapshot) | `ResumeEditor.MementoSanpshot` (nested) |
| **Caretaker** | keeps the save files, never opens them | `History` (a `Stack` of mementos) |

```
Memento.main()
   ├─ history.save(resume)   → resume.save() makes a MementoSanpshot → pushed on the stack
   ├─ resume.setName(...)     (edit the live resume)
   └─ history.undo(resume)   → pop the last memento → resume.restore(it)
```

---

## The clever bit — "without breaking encapsulation"

This is the heart of the pattern. The memento is a **`static` nested class with `private` getters**:

```java
public static class MementoSanpshot {
    private String name;
    private String getName(){ return name; }   // getter is PRIVATE
}
```

Because `getName()` is `private`, the `History` caretaker can **hold** a `MementoSanpshot` but **can't call `getName()` on it** — it's locked. Only `ResumeEditor` (the enclosing class) can read those fields, and it does so inside `restore()`. Java lets an outer class reach into its own nested class's privates — that's exactly what makes this work:

```
ResumeEditor  ──can read──▶  MementoSanpshot's private getters   ✅ (enclosing class)
History       ──cannot────▶  MementoSanpshot's private getters   ❌ (just stores it)
```

So the snapshot is transparent to the resume and opaque to everyone else. That's "capture & restore state **without breaking encapsulation**."

---

## Two bugs this file hit on the way (and the lessons)

### 1. Shallow copy of the mutable field → snapshot gets corrupted
`save()` originally stored the **same `List` reference**, so editing the list later also changed the snapshot. `String` fields are safe (immutable), but a `List` is not.

```java
// ❌ shares the list — a later skills.add(...) corrupts the snapshot
new MementoSanpshot(name, college, cgpa, skills);
// ✅ defensive copy — the snapshot gets its own independent list
new MementoSanpshot(name, college, cgpa, new ArrayList<>(skills));
```

> **Lesson:** always **defensive-copy mutable state** into the memento (and ideally back out in `restore()` too). Immutable fields like `String` need no copy; mutable ones like `List` do.

### 2. Undo did nothing — the save/edit *order* was wrong
Saving the state *after* editing put the current state on top of the stack, so undo popped it and handed it right back — a no-op.

```java
// ❌ save AFTER edit → top of stack IS the current state → undo = no change
edit(); history.save(resume); history.undo(resume);

// ✅ save the checkpoint BEFORE editing → undo returns to it
history.save(resume); edit(); history.undo(resume);
```

> **Lesson — the golden rule:** **save the checkpoint *before* you edit; undo restores the last checkpoint.** The state you're currently looking at should be one edit *ahead* of the stack, never already on top of it. Nothing was wrong with `save`/`restore`/`undo` — only the order they were called in.

---

## UML class diagram

```
   ┌──────────────────────────────┐   creates   ┌──────────────────────────────┐
   │        ResumeEditor          │────────────▶│  MementoSanpshot (static)    │
   │  (Originator)                │             │  - name, college, cgpa       │
   │  - name, college, cgpa, skills│             │  - skills                    │
   │  + save(): Memento           │◀────────────│  - getName()  (PRIVATE)      │
   │  + restore(Memento)          │  reads its  │  - getCollege() (PRIVATE)    │
   └──────────────────────────────┘  own nested └──────────────────────────────┘
                 ▲                     privates              ▲
                 │ uses                                      │ holds (can't read)
   ┌──────────────────────────────┐                          │
   │           History            │  ◇───────────────────────┘
   │  (Caretaker)                 │      Stack<Memento>
   │  - sk: Stack<...Memento>     │
   │  + save(ResumeEditor)        │
   │  + undo(ResumeEditor)        │
   └──────────────────────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Nested / creates** | `ResumeEditor` → `MementoSanpshot` | nesting + «create» | the memento lives inside the originator, which makes it in `save()` |
| **Aggregation** | `History` ◇→ `MementoSanpshot` | hollow diamond ◇ | the caretaker **holds a stack** of mementos (created outside it, so aggregation not composition) |
| **Dependency** | `History` ┈▷ `ResumeEditor` | dashed arrow ┈▷ | `save`/`undo` take a `ResumeEditor` as a parameter (transient use) |

> Note the deliberate asymmetry: `History` aggregates mementos but **cannot read them** — it depends on the private-getter wall for encapsulation.

---

## SOLID

> Note: **Singleton is *not* SOLID** — it's a separate creational pattern. The **S** in SOLID is **Single Responsibility**.

| SOLID | Followed? | Why, in this code |
|---|---|---|
| **S** — Single Responsibility | ✅ | `ResumeEditor` owns its state; `History` owns the undo stack — snapshot storage isn't smeared into the editor |
| **O** — Open/Closed | ✅ | add redo, or a bounded history, by extending `History` — the editor's snapshot logic is untouched |
| **L** — Liskov Substitution | ➖ | no hierarchy to substitute here |
| **I** — Interface Segregation | ✅ | the caretaker only sees the memento as an opaque token — the *narrowest* possible surface |
| **D** — Dependency Inversion | ➖ | fine for this size; could inject a `Memento` interface if you wanted the caretaker fully abstract |

> The standout is really **encapsulation** (the private-getter wall) — the whole reason Memento exists over a naive public-field copy.

---

## Common notes & lessons

### 1. Why `String` reverts but `List` didn't
`String` is **immutable** — `setName("sagar")` swaps in a *new* string, so the snapshot's old string is never touched. `List` is **mutable** — without a copy, the snapshot and editor share one object. That's why the defensive copy matters only for the mutable field.

### 2. Residual: copy on the way *out* too
`save()` copies the list in ✅. `restore()` still does `this.skills = prevResume.getSkills()` — sharing the memento's list back to the editor. For full independence, copy there as well: `this.skills = new ArrayList<>(prevResume.getSkills())`.

### 3. Redo is a second stack
This file supports **undo** (one stack). Redo is the mirror image — on undo, push the popped state onto a *redo* stack; on redo, pop it back. Same mechanism, opposite direction.

### 4. Cosmetic
Typo `MementoSanpshot` → `MementoSnapshot`; and `showResume` prints `"skills"+skills` with no space/colon. Neither affects behaviour.

---

## Memento vs Command — both do undo, differently

| | **Memento** (this) | **Command** |
|---|---|---|
| Stores | a **snapshot of state** | the **action** (and how to reverse it) |
| Undo by | restoring the whole saved state | running the inverse operation |
| Best when | state is small/simple to snapshot | actions are discrete and individually reversible |

> Rule of thumb: **Memento** = "remember what it *was*." **Command** = "remember what was *done* (so I can undo the doing)."

---

## When to use Memento

- You need **undo/redo, rollback, or checkpoints** (editors, games, transactions, form wizards).
- You want to save/restore an object's state **without exposing its internals** to whoever manages the history.
- The state is reasonable to snapshot (not gigantic — otherwise snapshots get expensive).

> The win: full undo capability, with the snapshot **sealed** so only the originator can read it — the caretaker stores history blindly.

---

## Run it

```bash
cd "Memento Pattern"
javac Memento.java
java Memento
```

Output:
```
Resume
 name: raj
 college : nit agartala
 cgpa : 8.41
skills[html, css, java]
Resume
 name: sagar
 college : nit delhi
 cgpa : 8.41
skills[html, css, java]
Resume
 name: raj
 college : nit agartala
 cgpa : 8.41
skills[html, css, java]
```

`save()` checkpoints **raj** before editing; the resume is edited to **sagar / nit delhi**; then `undo()` restores the checkpoint — the third printout is back to **raj / nit agartala**. Undo works because the checkpoint was taken *before* the edit.
