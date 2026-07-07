# Creating & Managing Threads in Java — 7 Ways (with Pros & Cons)

This folder walks through **seven** ways to run work in Java — from no threads at all, up to a managed thread pool returning results. Each file does the "same" kind of job (a few tasks that sleep and print), so you can compare the *mechanism*, not the task.

The progression:

```
1. Sequential (no threads)        ← baseline
2. extends Thread                 ┐
3. implements Runnable            ├─ get a thread running (no result)
4. anonymous Runnable             │    (4 & 5 are just shorter ways to write 3)
5. lambda Runnable                ┘
6. Callable + FutureTask + Thread ┐
7. Callable + ExecutorService     ┘─ get a RESULT back from a thread
```

Two ideas run through everything:

- **`start()` vs `run()`** — `start()` launches a **new** thread and returns immediately; calling `run()`/`call()` directly just executes on the **current** thread (sequential). Forgetting `start()` = nothing runs in parallel.
- **Total time** — sequential = the **sum** of task times; concurrent = the **longest** task (they overlap).

---

## 1. Sequential — no concurrency
📄 [WithoutConcurrency.java](WithoutConcurrency.java)

Plain classes; `main` calls `task1.implement(); task2.implement(); task3.implement();` one after another on the **main thread**. No threads at all — the baseline to compare against.

```java
task1.implement();   // blocks 3s
task2.implement();   // then 3s
task3.implement();   // then 3s   → 9s total
```

| ✅ Pros | ❌ Cons |
|---|---|
| Dead simple, easy to read/debug | **No parallelism** — total time = sum of all tasks |
| Deterministic order | One slow/blocking task freezes everything |
| No thread-safety concerns at all | Wastes multi-core CPUs |

**Use when:** tasks are inherently ordered/dependent, or so fast that threading isn't worth it.

---

## 2. `extends Thread`
📄 [ExtendingThread.java](ExtendingThread.java)

Your task **is** a thread: subclass `Thread`, override `run()`, call `start()`.

```java
class Task1 extends Thread {
    public void run(){ ... }
}
new Task1().start();   // runs on a new thread
```

| ✅ Pros | ❌ Cons |
|---|---|
| Simplest way to *get* a thread | **Burns your one inheritance slot** — the class can't extend anything else |
| Task object has Thread methods directly | Tightly couples "what to do" with "how to run it" |
| | Task isn't reusable as a plain job (can't hand it to a pool) — and no return value |

**Use when:** rarely the right choice. Most guides say **prefer `Runnable`** (next) over this.

---

## 3. `implements Runnable`
📄 [UsingRunnable.java](UsingRunnable.java)

Define the task as a `Runnable`, then hand it to a `Thread`.

```java
class Task1 implements Runnable {
    public void run(){ ... }
}
new Thread(new Task1()).start();
```

| ✅ Pros | ❌ Cons |
|---|---|
| **Frees the inheritance slot** (composition over inheritance) | No return value from `run()` |
| Separates the **task** from the **thread** — reusable; same `Runnable` works with a `Thread` *or* an `ExecutorService` | `run()` **can't throw checked exceptions** (must `try/catch` inside) |
| The standard, preferred way to define a unit of work | Still creating/managing `Thread`s by hand |

**Use when:** the default for "fire-and-forget" tasks that don't return a result. **Preferred over `extends Thread`.**

---

## 4. Anonymous-class `Runnable`
📄 [AnonymsClassRunable.java](AnonymsClassRunable.java)

Same as #3, but the `Runnable` is an **unnamed class defined inline** — no separate named class.

```java
Runnable run = new Runnable(){
    public void run(){ ... }
};
new Thread(run).start();
```

| ✅ Pros | ❌ Cons |
|---|---|
| No separate named class for a one-off task | Verbose boilerplate (`new Runnable(){ public void run(){…} }`) |
| Logic sits right where it's used | Harder to read; not reusable |
| Can capture local variables (effectively final) | `this` refers to the **anonymous class**, not the outer class (gotcha) |

**Use when:** a genuinely one-off task, *or* you need instance fields / multiple methods (things a lambda can't do).

---

## 5. Lambda `Runnable`
📄 [UsingLambda.java](UsingLambda.java)

The **shorthand** for #4 — because `Runnable` is a *functional interface* (one method), Java infers everything but the body.

```java
Runnable task = () -> { ... };
new Thread(task).start();
```

| ✅ Pros | ❌ Cons |
|---|---|
| Most concise & readable — zero boilerplate | Only works for **functional interfaces** (one abstract method) |
| Modern idiom (Java 8+) | Can't hold fields/state or implement multiple methods |
| `this` refers to the **enclosing** class (intuitive) | Stack traces slightly less descriptive |

**Use when:** the default for short, stateless one-off tasks in modern Java.

> #3, #4, #5 all produce the *same* `Runnable` — they differ only in **how much boilerplate** you write: named class → anonymous class → lambda.

---

## 6. `Callable` + `FutureTask` + `Thread` (manual, returns a result)
📄 [UsingCallable.java](UsingCallable.java)

`Runnable` can't return anything. **`Callable<T>`** can — it returns a value *and* may throw checked exceptions. Wrap it in a `FutureTask` (which is **both** a `Runnable` and a `Future`), run it on a `Thread`, and collect the result with `.get()`.

```java
FutureTask<String> ft = new FutureTask<>(new Task1());  // Task1 implements Callable<String>
new Thread(ft).start();
String result = ft.get();   // blocks until done, returns the value
```

| ✅ Pros | ❌ Cons |
|---|---|
| **Returns a result** (`Future.get()`) | Verbose — `FutureTask` + `Thread` wiring |
| `call()` **can throw checked exceptions** | Still creating threads by hand (no pooling) |
| `FutureTask` bridges `Callable` → `Thread` and gives you `.get()` | `get()` **blocks** and throws checked exceptions (`InterruptedException`, `ExecutionException`) |

**Use when:** you need a result from a thread but aren't using a pool. Great for *seeing the machinery*; not how you'd scale.

---

## 7. `Callable` + `ExecutorService` (pooled, returns results) ⭐
📄 [CallableUsingExecuter.java](CallableUsingExecuter.java)

Let a **thread pool** own and reuse the threads. You just submit work and collect `Future`s.

```java
ExecutorService pool = Executors.newFixedThreadPool(3);
Future<String> f1 = pool.submit(new Task1());   // submit → Future, returns instantly
System.out.println(f1.get());                    // collect the result
pool.shutdown();                                 // ← REQUIRED, or the JVM hangs
```

| ✅ Pros | ❌ Cons |
|---|---|
| **Thread pooling & reuse** — no `new Thread` per task; scales to many tasks | **Must call `shutdown()`** or the program never exits |
| Returns results via `Future`; `invokeAll` for batches; `CompletionService` for "results as they finish" | `get()` still blocks (unless you use `CompletableFuture`) |
| Manages the whole thread lifecycle for you | Choosing a good pool size takes thought |

**Use when:** **the default for real applications** — anything beyond a trivial one-off. This is the idiomatic modern way.

---

## Side-by-side summary

| # | Approach | Returns a value? | Checked exceptions in body? | Thread management | Boilerplate | Verdict |
|---|----------|:---:|:---:|---|---|---|
| 1 | Sequential | n/a | n/a | none | tiny | baseline only |
| 2 | `extends Thread` | ❌ | ❌ | manual | low | ⚠️ avoid (eats inheritance) |
| 3 | `implements Runnable` | ❌ | ❌ | manual | low | ✅ good for no-result tasks |
| 4 | anonymous `Runnable` | ❌ | ❌ | manual | high | ok for one-offs (pre-8 / needs state) |
| 5 | lambda `Runnable` | ❌ | ❌ | manual | tiny | ✅ concise one-off tasks |
| 6 | `Callable`+`FutureTask` | ✅ | ✅ | manual | high | ok when you need a result, no pool |
| 7 | `Callable`+`ExecutorService` | ✅ | ✅ | **pooled** | medium | ⭐ **default for real apps** |

---

## Cross-cutting lessons (the things that actually bite)

1. **`start()`, not `run()`.** `start()` spawns a new thread; `run()`/`call()` called directly run on the current thread — sequential. The #1 "why isn't it parallel?" mistake.
2. **`Runnable` vs `Callable`.** `run()` → returns `void`, no checked exceptions. `call()` → returns a value, **can** throw checked exceptions. That's the whole reason `Callable` exists.
3. **`Thread.sleep()` throws checked `InterruptedException`.** Inside `run()` you *must* `try/catch` it; inside `call()` you may instead `throws Exception`.
4. **`future.get()` throws checked exceptions** (`InterruptedException`, `ExecutionException`) — the caller must catch or declare them.
5. **`ExecutorService` must be `shutdown()`.** Its threads are non-daemon and idle-wait, so without shutdown the JVM never exits.
6. **Output order is non-deterministic** for all concurrent approaches — the OS scheduler decides who prints first. Don't rely on it.
7. **`get()` blocks only the calling thread** — the workers keep running. Calling `get()` in a fixed order delays *collection*, not *execution*.

---

## Decision guide

```
Need a result back from the work?
├── NO  → just running a task
│        ├── one-off, short        → lambda Runnable            (#5)
│        ├── reusable named task   → implements Runnable        (#3)
│        └── (avoid extends Thread — it eats your inheritance)  (#2)
└── YES → need Callable
         ├── many tasks / real app → Callable + ExecutorService (#7) ⭐
         └── just learning / one   → Callable + FutureTask      (#6)
```

---

## Run them

Each file is standalone (they each define their own `Task1/2/3`), so compile and run one at a time:

```bash
cd CreatingAndManagingThreads
javac UsingRunnable.java   &&  java UsingRunnable
javac CallableUsingExecuter.java  &&  java CallableUsingExecuter
# ...same pattern for the others
```

What you'll observe:
- **WithoutConcurrency** — tasks finish strictly in order; total time = the **sum** (~9s).
- **All concurrent versions** — tasks overlap; total time = the **longest** task; print order may vary run to run.
- **CallableUsingExecuter / UsingCallable** — you also get the returned strings back via `Future.get()`.
