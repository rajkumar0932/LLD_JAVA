# Locks in Java — Quick Guide

Small demos of the locking tools in `java.util.concurrent`, from the basic lock up to
read-write locks, semaphores, and a deadlock (plus its fix). Each file is one idea. The running
theme: **how a thread waits for a shared resource, and who is allowed to release it.**

## The whole folder in one table

| Tool | File | In one line |
|---|---|---|
| **ReentrantLock** | [ReenterantThread.java](ReenterantThread.java) | Basic lock — one thread at a time, others **wait forever**. |
| **tryLock(timeout)** | [TimedBooking.java](TimedBooking.java) | Wait only N seconds, then **give up** (stay responsive). |
| **tryLock(timeout)** | [TimerRentrant.java](TimerRentrant.java) | Same idea — shows a lock **serializes** threads, doesn't reject them. |
| **Lock + Scheduler** | [ReenteredScheduler.java](ReenteredScheduler.java) | ⚠️ Learning trap: only the owner thread may unlock a `ReentrantLock`. |
| **ReadWriteLock** | [ReadWrire.java](ReadWrire.java) | **Many readers together**, but a writer needs **exclusive** access. |
| **Semaphore** | [Semephor.java](Semephor.java) | **N permits** — a counter with no owner; any thread can release. |
| **Deadlock + fix** | [DeadLock.java](DeadLock.java) | Two locks in opposite order **hang forever**; lock **ordering** fixes it. |

---

## 1. `ReentrantLock` — the basic lock
📄 [ReenterantThread.java](ReenterantThread.java)

```java
lock.lock();
try { /* critical section */ }
finally { lock.unlock(); }   // always release in finally
```
- Only **one thread** holds it at a time; everyone else **waits** (forever).
- `lock()` gives you no timeout — the thread blocks until it wins the lock.
- **Rule:** only the thread that called `lock()` may `unlock()`.

**Demo:** two threads book a seat; the second waits ~5s until the first releases.

---

## 2. `tryLock(timeout)` — a lock that gives up
📄 [TimedBooking.java](TimedBooking.java) · [TimerRentrant.java](TimerRentrant.java)

```java
if (lock.tryLock(3, TimeUnit.SECONDS)) {   // wait at most 3s
    try { /* got it */ } finally { lock.unlock(); }
} else {
    /* couldn't get it → tell the user, move on */
}
```
- The fix for `synchronized`/`lock()` blocking **forever**. Wait a bounded time, then **give up**.
- Three flavors: `lock()` = wait forever · `tryLock()` = give up instantly · `tryLock(t, unit)` = wait up to `t`.
- **Note:** a lock **serializes** threads (they take turns) — it does not *reject* the second one. A timeout only fires if the hold time is **longer** than the wait time.

**Demo:** Alice holds the seat 5s; Bob waits 3s, then gives up ("seat busy").

---

## 3. ⚠️ Lock + Scheduler — the ownership trap
📄 [ReenteredScheduler.java](ReenteredScheduler.java)

An attempt to auto-release a lock after 2s using a background scheduler. **It doesn't work** — and that's the lesson:

- A `ReentrantLock` can only be unlocked by the **same thread** that locked it.
- A scheduler runs on its **own** thread, so `lock.unlock()` there throws `IllegalMonitorStateException` (and the scheduler **swallows it silently**).
- **Takeaway:** for a timed auto-release, don't use `ReentrantLock` — use a **`Semaphore`** (whose permits aren't tied to a thread) or let the owner thread release itself.

---

## 4. `ReentrantReadWriteLock` — many readers OR one writer
📄 [ReadWrire.java](ReadWrire.java)

```java
lock.readLock().lock();    // SHARED   — many readers at once
lock.writeLock().lock();   // EXCLUSIVE — one writer, blocks everyone
```

| While held by… | another **reader**? | a **writer**? |
|---|:---:|:---:|
| **read lock** | ✅ yes (share) | ❌ no |
| **write lock** | ❌ no | ❌ no |

- Only **read + read** can overlap. Anything involving a writer is exclusive.
- Great when reads are frequent and writes are rare (**"stock market"**: everyone reads the price, an update freezes all).

**Demo:** a writer holds for 6s; readers must wait, then multiple readers run together.

---

## 5. `Semaphore` — N permits, no owner
📄 [Semephor.java](Semephor.java)

```java
Semaphore sem = new Semaphore(2);   // 2 slots
if (sem.tryAcquire()) { ... }       // take a slot  (count − 1)
sem.release();                      // free a slot  (count + 1)
```
- Just a **counter of permits** (like a parking lot's "spaces free" sign).
- `release()` doesn't log out any *specific* user — it just **adds a slot** for the next login.
- **No ownership:** *any* thread can `release()` (unlike `ReentrantLock`). Powerful, but you must only release what you actually acquired, or the count inflates.

**Demo:** N login slots; extra users are refused until someone logs out.

---

## 6. Deadlock — and how lock ordering fixes it
📄 [DeadLock.java](DeadLock.java)

Two threads each grab two locks in **opposite** order and freeze forever:

```
T1 holds a1, waits for a2  ─┐
                            │  ← a cycle → deadlock
T2 holds a2, waits for a1  ─┘
```

A deadlock needs **4 conditions** at once: mutual exclusion, hold-and-wait, no preemption, and
**circular wait**. Break any one and it can't happen.

**The fix — lock ordering:** give every lock a rank (here, `Account.id()`) and *always* acquire
**lower rank first**. Then no thread ever holds a high lock while waiting for a low one, so the
wait chain can't loop back → circular wait is impossible.

```java
Account first  = from.id() < to.id() ? from : to;   // always lock lower id first
Account second = from.id() < to.id() ? to   : from;
synchronized (first) { synchronized (second) { /* transfer */ } }
```

**Demo:** `runTransfers(false)` deadlocks (hangs); `runTransfers(true)` uses ordering and
completes. Toggle the flag in `main`. *(The money math uses `from`/`to`; only the **locks** are
reordered.)*

---

## Which one do I use?

```
One thread at a time?                → ReentrantLock          (lock/unlock)
...but I don't want to wait forever? → tryLock(timeout)
Lots of reads, few writes?           → ReentrantReadWriteLock
Allow up to N at once?               → Semaphore
```

## Three things that bite everyone here
1. **Always `unlock()` in a `finally`** — otherwise an exception leaks the lock forever.
2. **`tryLock(t, unit)`, `sleep()`, and `join()` throw checked `InterruptedException`** — you must `try/catch` or `throws` it (the compiler forces you).
3. **Grabbing 2+ locks? Always acquire them in the same global order** — opposite orders across threads is how deadlocks are born.
