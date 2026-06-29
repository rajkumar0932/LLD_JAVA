# Proxy Pattern — Notes & Q&A

Notes for [Proxy.java](Proxy.java): put a **stand-in** in front of a real object that shares its interface, so the stand-in can add control around each call. Example: a **caching video downloader** — the proxy serves repeat requests from a cache instead of downloading again.

This is a **structural** pattern (like Adapter, Decorator, Facade & Composite — about *connecting* objects).

---

## Why we needed it — the problem

Downloading a video is **expensive** (network, time). If the client calls the real downloader directly, every request — even for a video already fetched — pays the full cost again:

```java
VideoDownloader d = new DownloadVideo();
d.download("A.com");   // ❌ downloads
d.download("A.com");   // ❌ downloads the SAME thing again — wasted work
```

You could add caching *inside* `DownloadVideo`, but then the download class is also doing cache bookkeeping — two jobs in one class, and you'd repeat that logic for every other expensive service.

## How it solves the problem

Put a **proxy** (`DownloadVideoProxy`) in front that implements the **same interface** (`VideoDownloader`). The client can't tell it apart from the real thing, but the proxy checks a cache first and only delegates to the real downloader on a miss:

```java
VideoDownloader d = new DownloadVideoProxy();
d.download("A.com");   // miss → real download, then cached
d.download("A.com");   // hit  → returned from cache, no download
```

The real `DownloadVideo` stays focused on downloading; the proxy owns the caching. Same interface = **drop-in replacement**, so no client code changes.

---

## The idea

> **A class that stands in for another object of the same type**, intercepting calls to add control — caching, lazy loading, access checks, logging — before (or instead of) reaching the real object.

**ID-card analogy:** a receptionist (proxy) has the same "answer questions about the boss" interface as the boss. For routine questions they answer from memory (cache); only for new ones do they actually interrupt the boss (the real object).

```
client ──▶ DownloadVideoProxy ──(only on cache miss)──▶ DownloadVideo
 (one call)   (checks cache first)                       (real, expensive work)
```

---

## The 3 roles (with this file's classes)

| Role | In this code | Job |
|---|---|---|
| **Subject** (common interface) | `VideoDownloader` | the contract both share (`download(String)`) |
| **Real Subject** | `DownloadVideo` | does the actual, expensive work |
| **Proxy** | `DownloadVideoProxy` | same interface; adds caching, delegates on miss |
| **Client** | `Proxy.main()` | talks to the interface — can't tell proxy from real |

```
Proxy.main()
   └─▶ DownloadVideoProxy.download("A.com")
          ├─ cache hit?  → return cached value         (no real call)
          └─ cache miss? → downloaded.download(...)     (real call)
                           then store in cache
```

---

## UML class diagram

```
                 ┌──────────────────────────┐
                 │ «interface» VideoDownloader │
                 │ + download(String): String  │
                 └─────────────▲────────────▲─┘
                  realizes ╱                  ╲ realizes
                          ╱                    ╲
        ┌──────────────────────────┐   ┌──────────────────────┐
        │   DownloadVideoProxy      │   │    DownloadVideo     │
        │ - downloaded: DownloadVideo ◆─┼──▶ (the real subject)│
        │ - cached: Map<String,String> │   │ + download(): String│
        │ + download(String): String   │   └──────────────────────┘
        └──────────────────────────┘
```

**The relationships here:**

| Relationship | Between | UML | Why |
|---|---|---|---|
| **Realization** | `DownloadVideoProxy` & `DownloadVideo` → `VideoDownloader` | dashed, hollow triangle ▷ | both `implements VideoDownloader` |
| **Composition** | `DownloadVideoProxy` ◆→ `DownloadVideo` | filled diamond ◆ | the proxy `new`s the real subject **inside itself** (in its constructor) → created inside = composition |

> **The "who typed `new`?" test:** the proxy builds its own real subject (`downloaded = new DownloadVideo()` in the constructor) → **Composition ◆**. Destroy the proxy and its real downloader goes with it.
>
> Like Decorator, the proxy both **is-a** and **has-a** the subject type. The difference is *intent*: a Decorator **adds behaviour** to enrich the object; a Proxy **controls access** to it (cache, lazy-load, permissions) while keeping the same behaviour.

---

## The key mechanic — check, then delegate

```java
public String download(String video){
    if (cached.get(video) != null) {        // 1. cache HIT
        System.out.println("Already present");
        return cached.get(video);           //    serve without the real call
    }
    String result = downloaded.download(video); // 2. cache MISS → do real work
    cached.put(video, result);                  //    remember it
    return result;
}
```

The real download runs **only on a miss**. Every later request for the same URL is served from the `Map`.

> **Note on the `null` check:** `cached.get(video)` returns `null` when the key is absent (Java maps don't auto-insert like C++'s `m[key]`), which is exactly why `!= null` cleanly means "cache hit."

---

## Common errors & lessons (hit while building this file)

### 1. `Map` not imported / instantiated
```java
private Map<String,String> cached;   // ❌ "cannot find symbol" without an import
```
`Map` lives in `java.util` → add `import java.util.*;` (or `java.util.Map` + `java.util.HashMap`). And `Map` is an **interface** — you can't `new Map<>()`; create a `new HashMap<>()`.

### 2. Forgetting to initialize the map → NullPointerException
A declared-but-null `cached` blows up on the first `cached.get(...)`. Initialize it in the constructor: `cached = new HashMap<>();`.

### 3. Redundant lookup after `put`
```java
cached.put(video, downloaded.download(video));
return cached.get(video);          // looks the value up AGAIN
```
Store the result once and return it directly:
```java
String result = downloaded.download(video);
cached.put(video, result);
return result;
```

### 4. A `main` that doesn't prove the cache works
Calling `download` **once** produces output identical to having no proxy. Call it **twice with the same URL** so the second call visibly hits the cache.

---

## Proxy vs the other structural patterns

| Pattern | Wraps… | Intent |
|---|---|---|
| **Adapter** | one class | **change** its interface so it fits |
| **Decorator** | one object (same type) | **add** behaviour, stackable |
| **Facade** | a whole subsystem | **simplify** — one door over many |
| **Composite** | a tree of same-typed objects | treat **one and many** uniformly |
| **Proxy** | one object (same type) | **control access** — cache / lazy-load / guard |

> Decorator and Proxy look identical in structure (is-a + has-a the subject). The tell is intent: Decorator *enhances*, Proxy *gatekeeps*.

**Common Proxy flavors:** caching (this file), virtual (lazy-create a heavy object on first use), protection (access/permission checks), remote (stand-in for an object on another machine).

---

## When to use Proxy

- Calls to the real object are **expensive** and repeatable → **caching proxy**.
- The real object is **heavy to create** and may not be needed → **virtual/lazy proxy**.
- You need **access control / logging** around an object without changing it → **protection proxy**.

> The win: all that control logic lives in the proxy, and because it shares the subject's interface, it drops in with **zero client changes**.

---

## Run it

```bash
cd "Proxy Pattern"
javac Proxy.java
java Proxy
```

Output:
```
Downloaded www.jkdd.com
Already present
Downloaded www.jkdd.com
```

First call downloads; the second prints **"Already present"** and returns the cached value — proof the proxy served it without a real download.
