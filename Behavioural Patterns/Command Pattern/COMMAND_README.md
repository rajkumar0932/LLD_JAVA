# Command Pattern — Notes & Q&A

Notes for [Command.java](Command.java): wrap each **request as an object**, so you can store it, queue it, pass it around, and **undo** it — instead of calling the receiver directly. Example: a **remote controller** whose buttons are command objects (`FanON`, `FanOFF`, `AcON`, `AcOFF`) with an undo button.

This is a **behavioural** pattern (about *how objects interact* — like Strategy).

---

## Why we needed it — the problem

A remote's button needs to trigger an action on some device. If the button calls the device **directly**, the invoker is welded to every device's API, and there's **no way to undo or replay**:

```java
void pressButton(int i){
    if (i == 0) fan.on();          // ❌ invoker hardcodes every device call
    else if (i == 1) fan.off();
    else if (i == 2) ac.on();
    // ...and how do you UNDO the last press? you can't — nothing was recorded
}
```

Every new device = editing this method, and "undo" is impossible because the action wasn't captured as anything reusable.

## How it solves the problem

Turn each action into a **command object** with `execute()` and `undo()`. The invoker just holds commands and calls `execute()` — it never knows which device is inside. Executed commands are pushed on a **stack**, so undo is "pop and call `undo()`":

```java
remote.addCommand(new FanON());   // a request, as an object
remote.useCommand(0);             // invoker calls execute(), records it
remote.undoButton();              // pops last command, calls undo()
```

Add a device → add a command class. Undo → free, because every action is a stored object.

---

## The idea

> **Package a request (receiver + action) as a standalone object** implementing a common `execute()`/`undo()` interface, so the invoker is decoupled from the receiver and actions become storable, queueable, and reversible.

**Restaurant analogy:** a waiter writes your order on a ticket (the command). The waiter doesn't cook — they carry the ticket to the kitchen (receiver). The ticket can be queued, re-fired, or voided (undone). The waiter (invoker) never needs to know how to cook.

```
client ──sets up──▶ RemoteController ──execute()──▶ RemoteCommand ──▶ Fan / Ac
 (main)              (invoker + undo stack)          (the request)     (receiver)
```

---

## The 5 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Command** (interface) | `RemoteCommand` | the contract: `execute()` + `undo()` |
| **Concrete Commands** | `FanON`, `FanOFF`, `AcON`, `AcOFF` | bind an action to a receiver; forward `execute()`/`undo()` |
| **Receiver** | `Fan`, `Ac` | does the real work (`on()` / `off()`) |
| **Invoker** | `RemoteController` | holds commands, calls `execute()`, records them on an undo stack |
| **Client** | `Command.main()` | creates commands, wires them to the invoker |

```
Command.main()
   └─▶ RemoteController.useCommand(0)
          ├─ remote.get(0).execute()   →  FanON.execute()  →  fan.on()
          └─ sk.push(that command)         (recorded for undo)
   └─▶ RemoteController.undoButton()
          └─ sk.pop().undo()           →  FanON.undo()     →  fan.off()
```

---

## UML class diagram

```
                 ┌──────────────────────────┐
                 │ «interface» RemoteCommand │
                 │ + execute(): void         │
                 │ + undo(): void            │
                 └────────────▲──────────────┘
          realizes ╱   ╱   ╲   ╲ realizes
              ┌────────┐ ┌────────┐ ┌───────┐ ┌───────┐
              │ FanON  │ │FanOFF  │ │ AcON  │ │ AcOFF │
              │ ◆ Fan  │ │ ◆ Fan  │ │ ◆ Ac  │ │ ◆ Ac  │   ← each creates its receiver
              └────────┘ └────────┘ └───────┘ └───────┘
                   │           │          │        │
                   ◇ aggregation (commands added in)
                 ┌──────────────────────────────────┐
                 │         RemoteController          │
                 │ - remote: List<RemoteCommand>     │
                 │ - sk: Stack<RemoteCommand>        │
                 │ + addCommand(RemoteCommand)       │
                 │ + useCommand(int) / undoButton()  │
                 └──────────────────────────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | `FanON`/… → `RemoteCommand` | dashed, hollow triangle ▷ | each `implements RemoteCommand` |
| **Composition** | `FanON` ◆→ `Fan`, `AcON` ◆→ `Ac` | filled diamond ◆ | each command `new`s its receiver **inside** itself (see design note) |
| **Aggregation** | `RemoteController` ◇→ `RemoteCommand` | hollow diamond ◇ | commands are **passed in** via `addCommand` (created in `main`) |

---

## The key mechanic — undo via a LIFO stack

```java
public void useCommand(int index){
    if (remote.get(index) != null){
        remote.get(index).execute();     // do it
        sk.push(remote.get(index));      // remember it
    }
}
public void undoButton(){
    if (!sk.isEmpty())  sk.pop().undo(); // undo the MOST RECENT
    else System.out.println("no Last  method ");
}
```

Because executed commands are pushed on a `Stack`, undo pops them in **reverse order** — the last action done is the first undone. Each concrete command defines `undo()` as the inverse of `execute()` (`FanON.execute()` = `fan.on()`, `FanON.undo()` = `fan.off()`).

Run trace: execute FanON, FanOFF, AcON, AcOFF → stack top is AcOFF → three undos fire `AcOFF.undo()` (ac.on), `AcON.undo()` (ac.off), `FanOFF.undo()` (fan.on).

---

## Common errors & lessons (hit while building this file)

### 1. Two type names differing only in case → Windows collision
`interface command` (lowercase) + `public class Command` (uppercase) compiled to `command.class` and `Command.class` — the **same filename** on Windows' case-insensitive filesystem. One overwrote the other, leaving only `command.class`, so `java Command` failed with:
```
NoClassDefFoundError: command (wrong name: Command)
```
Fix: rename the interface to a distinct name (`command` → **`RemoteCommand`**). Lesson: never let two types differ only by case — and follow PascalCase for all type names. (This would have compiled on Linux/macOS, hiding the bug.)

### 2. Receiver created inside the command vs injected (design choice)
Each command builds its own receiver:
```java
FanON(){ this.fan = new Fan(); }    // FanON and FanOFF hold DIFFERENT Fan objects
```
Fine here because `Fan`/`Ac` are stateless (just print). But if the receiver had **state** (a dimmer level, an on/off flag), the commands must share **one** receiver — inject it instead:
```java
FanON(Fan fan){ this.fan = fan; }   // pass the SAME fan to FanON and FanOFF
```
That also flips `FanON ◆→ Fan` (composition) into `FanON ◇→ Fan` (aggregation).

---

## Command vs Strategy (both behavioural, both inject an object)

| | **Command** | **Strategy** |
|---|---|---|
| Encapsulates | a **request/action** (do + undo) | an **algorithm/behaviour** |
| Extra powers | undo, queue, log, replay | just swap the behaviour |
| Invoker knows | only `execute()` | only the strategy method |

> Both hold an injected interface and delegate. Command adds the idea of a *reified request* you can store and reverse; Strategy is purely "which algorithm runs."

---

## When to use Command

- You want **undo/redo**, **queues**, **macros**, or **logging/replay** of actions.
- You want to **decouple** the thing that triggers an action (button, menu) from the thing that performs it (device, service).
- You want to parameterize objects **with actions** (pass a command around like a value).

> The win: actions become first-class objects — storable, reversible, and swappable — and the invoker stays ignorant of the receivers.

---

## Run it

```bash
cd "Command Pattern"
javac Command.java
java Command
```

Output:
```
Fan is on
Fan is off
Ac is on
Ac  is off
Ac is on
Ac  is off
Fan is on
```

The first four lines are the executed commands; the last three are the **undos** firing in reverse (LIFO) order.
