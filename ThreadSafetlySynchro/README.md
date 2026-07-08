# Thread Safety & Synchronization in Java — 4 Ways (with Pros & Cons)

This folder demonstrates the classic **lost-update** race condition and four ways to deal with it. Every class does the *same* job — expose `increment()` and `getValue()` around a shared counter — and `main` fires **two threads × 1000 increments** at each one. The correct answer is always **2000**; whether you actually get it tells you if the approach is thread-safe.

📄 [ThreadSafety.java](ThreadSafety.java)

The progression:

```
1. WithoutThreadSafety   ← baseline (BROKEN) — plain n++
2. withSynchonized       ← synchronized(this) block   ✅ correct
3. withValatile          ← volatile int               ❌ STILL broken (the trap)
4. AtomicVariables       ← AtomicInteger + CAS loop    ✅ correct, lock-free
```

## The one idea behind all of it: `n++` is not atomic

`n++` looks like a single step but the JVM runs it as **three**:

```
1. read  n      (load current value)
2. add   1      (compute n + 1)
3. write n      (store it back)
```

Two threads interleave those steps and clobber each other:

| Step | thread A | thread B | n |
|------|----------|----------|---|
| 1 | read n = 5 | | 5 |
| 2 | | read n = 5 | 5 |
| 3 | write 6 | | 6 |
| 4 | | write 6 | 6 |

Both incremented, but `n` went 5 → 6 instead of 7 — **one update was lost**. Do that thousands of times across two threads and the total lands *under* 2000.

There are actually **two** separate problems hiding here, and this is the whole point of the folder:

- **Atomicity** — the read-modify-write above can be interrupted mid-way.
- **Visibility** — without a memory barrier, one thread may not even *see* the other's latest write, because each thread can cache `n` in a register/CPU cache.

A fix has to solve **the one you actually have**. `n++` needs *both*; that's why `volatile` (which only fixes visibility) is not enough.

---

## 1. `WithoutThreadSafety` — the baseline (broken)

```java
private int n;
public void increment(){ n++; }        // race condition
```

No protection at all. This is here to *see the bug*, not to use.

| ✅ Pros | ❌ Cons |
|---|---|
| Simplest possible code | **Loses updates** under concurrency — result < 2000 |
| Fine for single-threaded use | Fails both atomicity *and* visibility |

**Result:** non-deterministic, usually **less than 2000**.

---

## 2. `withSynchonized` — `synchronized` block ✅
```java
private int n;
public void increment(){
    synchronized(this){
        n++;
    }
}
```

`synchronized` acquires the object's **monitor** (lock). Only one thread can be inside the block at a time, so the three steps of `n++` run as an indivisible unit. Releasing the monitor also **flushes changes to main memory** — so it fixes atomicity **and** visibility at once.

| ✅ Pros | ❌ Cons |
|---|---|
| Fixes **both** atomicity and visibility | Locking has overhead; threads block waiting for the monitor |
| Simple, obviously correct | Can deadlock if you nest locks carelessly |
| Can guard multi-line critical sections, not just one variable | Coarser than needed for a single counter |

> ⚠️ **You can't lock on a primitive.** `synchronized(n)` where `n` is an `int` won't compile — a monitor lives on an *object*. Lock on `this`, or better, a `private final Object lock = new Object();`. Never lock on the variable you're mutating (autoboxing creates a new lock each call).

**Result:** always **2000**.

---

## 3. `withValatile` — `volatile int` ❌ (the trap)
```java
private volatile int n;
public void increment(){ n++; }        // STILL a race!
```

`volatile` guarantees every read sees the latest write (**visibility**) and prevents reordering — but it does **nothing** for **atomicity**. `n++` is still read-modify-write, so two threads still clobber each other. This is the most common thread-safety misconception.

| ✅ Pros | ❌ Cons |
|---|---|
| Cheap — no locking | **Does NOT make `n++` atomic** → still loses updates |
| Correct for a **single** read *or* a single write (e.g. a `stop` flag) | Useless for any compound "read-then-write" operation |
| Guarantees visibility & ordering | Easy to reach for and be wrong |

**Result:** still **less than 2000** — and this run actually caught it: `1955`. Proof that `volatile` alone does not fix `n++`.

> **When `volatile` *is* right:** a flag written by one thread and read by others, like `private volatile boolean running;`. One writer, simple assignment, no read-modify-write.

---

## 4. `AtomicVariables` — `AtomicInteger` + compare-and-swap ✅
```java
private AtomicInteger n = new AtomicInteger(0);
public void increment(){
    int curr, next;
    do {
        curr = n.get();
        next = curr + 1;
    } while (!n.compareAndSet(curr, next));   // retry if someone changed it
}
```

`compareAndSet(expected, new)` is a single **atomic CPU instruction (CAS)**: "if `n` is still `curr`, set it to `next`; otherwise fail." If another thread snuck in, `curr` is stale, the CAS fails, and the loop retries with the fresh value. No locks, no blocking — **lock-free**.

| ✅ Pros | ❌ Cons |
|---|---|
| Atomic **and** visible, **without locking** | The CAS-loop idiom is more code / less obvious |
| No blocking → scales well under contention | Under *very* high contention, retries can spin |
| Rich API (`getAndIncrement`, `addAndGet`, …) | Only atomic on a **single** variable — can't span two fields |

> 💡 **You can delete the whole loop** — `AtomicInteger` already ships this exact CAS loop as `n.incrementAndGet()`. The manual version here is written out to *show what `incrementAndGet` does under the hood*.

**Result:** always **2000**.

---

## Side-by-side summary

| # | Approach | Fixes atomicity? | Fixes visibility? | Locking? | Correct? |
|---|----------|:---:|:---:|:---:|:---:|
| 1 | `WithoutThreadSafety` (`n++`) | ❌ | ❌ | no | ❌ < 2000 |
| 2 | `synchronized(this)` | ✅ | ✅ | yes (blocking) | ✅ 2000 |
| 3 | `volatile int` | ❌ | ✅ | no | ❌ < 2000 |
| 4 | `AtomicInteger` + CAS | ✅ | ✅ | no (lock-free) | ✅ 2000 |

---

## Cross-cutting lessons (the things that actually bite)

1. **`n++` is three operations, not one.** Read, add, write — interruptible in the middle. This single fact is the whole race condition.
2. **Atomicity ≠ visibility.** They're separate guarantees. Know which one your bug needs before picking a fix.
3. **`volatile` fixes visibility only.** Great for a simple flag; **useless** for `n++`, `count += x`, or any read-then-write. The most common wrong answer.
4. **`synchronized` fixes both** — at the cost of blocking. Lock on an object (`this` or a private lock), never on a primitive or the field you mutate.
5. **`AtomicInteger` fixes both, lock-free** via CAS. Prefer the built-in `incrementAndGet()` over hand-rolling the retry loop.
6. **The bug is non-deterministic.** A broken version may print 2000 sometimes (see run 1 below). "It worked once" proves nothing — run it repeatedly.
7. **`getValue()` here is safe unsynchronized only because of `join()`.** `main` joins both threads before reading, and `join()` establishes a *happens-before* edge, so the read sees the final value. Read the counter *while* threads run and you'd need synchronization on the read too.

---

## Run it

```bash
cd ThreadSafetlySynchro
javac ThreadSafety.java && java ThreadSafety
```

`main` runs the four approaches in order and prints four numbers:

```
WithoutThreadSafety   synchronized   volatile   Atomic
```

Run it a **few times** — the unsafe ones (columns 1 and 3) vary run to run:

```
2000  2000  2000  2000     ← got lucky: even the broken ones hit 2000
1779  2000  2000  2000     ← WithoutThreadSafety lost updates
1977  2000  1955  2000     ← both WithoutThreadSafety AND volatile lost updates
```

Columns 2 (`synchronized`) and 4 (`Atomic`) are **always 2000**. Columns 1 and 3 are the race condition — and the fact that `volatile` (column 3) sometimes drops below 2000 is the headline takeaway of this folder.
