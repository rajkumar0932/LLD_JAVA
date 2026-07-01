# Java for C++ Devs — Quick Notes

Short notes from building the pattern examples. If you know C++, you already know 90% — this is just the 10% that's different.

---

## Start here: the 5 things that trip you up

1. **You can't `new` an interface.** `new List<>()` and `new Map<>()` fail. Use `new ArrayList<>()`, `new HashMap<>()`.
2. **No `[]` on lists.** Use `list.get(i)`, not `list[i]`. (`[]` works only on plain arrays.)
3. **Generics need objects.** `List<int>` is illegal — write `List<Integer>`.
4. **Imports are exact.** `import java.util.ArrayList;` — not a folder include like C++.
5. **Compile vs run:** `javac Foo.java` (with `.java`), then `java Foo` (without).

That's the whole learning curve. Details below when you need them.

---

## Lists (your `std::vector`)

Use `ArrayList`. (`Vector` exists but is old — ignore it.)

```java
import java.util.ArrayList;
import java.util.List;

List<Integer> v = new ArrayList<>();   // declare as List, build an ArrayList
v.add(7);                              // push_back
int x = v.get(0);                      // v[0]
v.set(0, 9);                           // v[0] = 9
int n = v.size();                      // size()
System.out.println(v);                 // [9]  — prints nicely
```

| C++ | Java |
|---|---|
| `v.push_back(7)` | `v.add(7)` |
| `v[i]` | `v.get(i)` |
| `v[i] = 9` | `v.set(i, 9)` |
| `v.size()` | `v.size()` |
| `v.empty()` | `v.isEmpty()` |

> Why `List` on the left, `ArrayList` on the right? `List` is the interface (the contract), `ArrayList` is one implementation. Same idea as your line-23 bug — you can't instantiate the interface itself.

---

## Looping a list

Most of the time, just use range-for (same as C++):

```java
for (int item : v) {
    System.out.println(item);
}
```

Need the index? Use a normal loop — but note `.size()` is a method:

```java
for (int i = 0; i < v.size(); i++) {
    System.out.println(v.get(i));
}
```

> **One trap:** don't add/remove items *while* a range-for is running — it crashes (`ConcurrentModificationException`). To delete while looping, use `v.removeIf(x -> x == 20);`.

---

## Maps (your `std::map` / `unordered_map`)

```java
import java.util.HashMap;
import java.util.Map;

Map<String,String> cache = new HashMap<>();   // unordered_map
cache.put("a", "apple");                       // m["a"] = "apple"
String val = cache.get("a");                   // "apple"
```

Pick the implementation by behaviour: `HashMap` (unordered, default), `TreeMap` (sorted keys), `LinkedHashMap` (keeps insertion order).

| C++ | Java |
|---|---|
| `m["a"] = "apple"` | `m.put("a", "apple")` |
| `m["a"]` | `m.get("a")` |
| `m.count("a")` | `m.containsKey("a")` |
| `m.erase("a")` | `m.remove("a")` |

> **The big difference:** `get()` on a missing key returns `null` and inserts nothing. (C++'s `m["x"]` silently creates an entry — Java never does.) So the cache check is:
> ```java
> String r = cache.get(key);
> if (r == null) {              // miss → compute and store
>     r = expensiveLoad(key);
>     cache.put(key, r);
> }
> ```

Looping a map:

```java
for (Map.Entry<String,String> e : cache.entrySet()) {
    System.out.println(e.getKey() + " -> " + e.getValue());
}
```

---

## Plain arrays (fixed size)

```java
int[] a = new int[5];        // int a[5];  (zeroed)
int[] b = {1, 2, 3};
int n = b.length;            // .length — a field, no () and no sizeof
```

Fixed size, can't grow — that's why you usually reach for `ArrayList` instead.

> Heads up: `length` (arrays) has no `()`, but `size()` (lists) does. Easy to mix up.

---

## When it won't compile — quick checklist

| Error / symptom | Fix |
|---|---|
| `new List<>()` / `new Map<>()` | use `new ArrayList<>()` / `new HashMap<>()` |
| `list[i]` | use `list.get(i)` |
| `List<int>` | use `List<Integer>` (wrapper types) |
| "cannot find symbol: List/Map" | add the `import java.util.*;` line |
| `int` method "missing return" | add `return ...;` at the end |
| statement in class body | move it inside `main` or a method |
| `javac Foo` error about class names | run `javac Foo.java` (with extension) |

---

That's it. Everything else in Java reads pretty much like C++ — start with the 5 gotchas at the top and come back here when something doesn't compile.
