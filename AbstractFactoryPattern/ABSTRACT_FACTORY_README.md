# Abstract Factory — Contingent Example (Notes)

Notes for [AbstractFactory.java](AbstractFactory.java): each **country** fields a matching *family* of teams (Country + CricketTeam + FootballTeam), built through a per-country factory. Covers the real bugs hit while writing it.

---

## The idea

> **A factory that produces a whole matching family of objects** — without the caller knowing the concrete classes.

Here the "family" is one country's set:

- **India set** → India + IndianCricket + IndianFootball
- **USA set** → USA + USACricket + USAFootball

`IndianContigent` can only build the India set, `USAContigent` only the USA set — so a mismatched family (India country + USA cricket team) is impossible.

---

## Structure

```
Contigent (abstract factory)            "I promise a Country + Cricket + Football team"
   ├── IndianContigent → India + IndianCricket + IndianFootball
   └── USAContigent    → USA   + USACricket   + USAFootball

ContigentChoose (service)  ← takes ANY Contigent, never knows which country
```

```java
interface Contigent {
    Country Name();
    CricketTeam cricketTeam();
    FootBallTeam FootballTeam();
}
```

---

## The service: take the factory, don't create it

The service receives a factory and just **uses** the products — no `new`, no `if/else`:

```java
static class ContigentChoose {
    private Country Name;
    private CricketTeam cricketTeam;
    private FootBallTeam FootballTeam;

    public ContigentChoose(Contigent c) {       // factory passed IN
        this.Name        = c.Name();
        this.cricketTeam = c.cricketTeam();
        this.FootballTeam= c.FootballTeam();
    }
    public void showInfo() {
        Name.name();            // USES the objects (calls methods)
        cricketTeam.team();
        FootballTeam.team();
    }
}
```

**Mistake to avoid:** creating the factory *inside* the service (`Contigent name = new ...`). Don't — pass it in through the constructor. That keeps the service independent of India vs USA.

---

## Common errors & lessons (hit while building this file)

### 1. `static` on every nested class — the big one

All nested classes (`India`, `IndianCricket`, `IndianContigent`, `ContigentChoose`...) **must be `static`**. Without it:

```
error: No enclosing instance of type AbstractFactory is accessible
```

**Why (the mechanics):** a *non-static* inner class secretly carries a hidden reference to an outer `AbstractFactory` object (Java adds a field called `this$0`). When you write `new IndianContigent()`, Java must fill that hidden field with an outer object. But `main` is `static` → no outer object, no `this` → nothing to fill it with → error.

```java
// non-static inner class — Java secretly adds:
class India {
    // AbstractFactory this$0;   ← hidden; needs an outer object to fill
}
```

`static` **removes** that hidden reference, so the class stands alone and can be created from `static main`.

| | non-static inner class | `static` nested class |
|---|---|---|
| Hidden outer reference | ✅ yes (`this$0`) | ❌ no |
| Needs an outer object to create | ✅ yes | ❌ no |
| Works from `static main` | ❌ no | ✅ yes |

> Mental model: non-static inner class = a child that must **hold its parent's hand**. In `static main` there's no parent standing there → can't create it. `static` lets it stand alone.

(Same fix used in `Builder/builder.java` → `static class Maggie`.)

### 2. Passing a class name instead of an object

```java
new ContigentChoose(IndianContigent);        // ❌ that's a TYPE, not a value
new ContigentChoose(new IndianContigent());  // ✅ create the object with new
```

A constructor parameter wants an **object**. `IndianContigent` alone is just the class name; `new IndianContigent()` produces an actual object. The chain has two `new`s — one for the factory, one for the service.

### 3. `showInfo()` printed objects, not text

```java
System.out.println(Name);     // ❌ prints AbstractFactory$India@1b6d3586
Name.name();                  // ✅ calls the method → readable text
```

Printing an object uses its default `toString()` (class name + hash). Call the actual method to get meaningful output.

### 4. Junk imports from the same file

The IDE auto-added `import AbstractFactoryPattern.AbstractFactory.Country;` etc. — importing nested classes **from the same file**. Unnecessary; deleted them all.

### 5. `package` line broke the Run button

`package AbstractFactoryPattern;` made the real class name `AbstractFactoryPattern.AbstractFactory`, but the Run button executed `java AbstractFactory` (no package) →
`Could not find or load main class`. **Fix:** removed the `package` line so it runs like the other folders. (The IDE still shows a harmless "package does not match folder" warning — safe to ignore.)

### 6. `javac AbstractFactory` → "Class names ... only accepted if annotation processing is requested"

`javac` compiles a **file**, so it needs the `.java` extension.

| Step | Command | Extension |
|---|---|---|
| Compile | `javac AbstractFactory.java` | ✅ `.java` |
| Run | `java AbstractFactory` | ❌ none |

> `javac` = compile a **file** → needs `.java`. `java` = run a **class** → no extension.

---

## Choosing the factory: if/else vs Map

If something external (string/config) picks the country, **keep that choice OUT of the service** — put it in `main` or a small provider.

- **2–3 countries, demo** → a simple `if/else` in `main` is fine.
- **Many countries / production** → use a `Map<String, Contigent>` (or enum): O(1) lookup, and adding a country is **one new line** instead of editing an if/else (respects Open/Closed).

The if/else isn't slow — it just **doesn't scale**. Either way the service still receives a finished factory; it never selects one.

---

## Run it

```bash
cd AbstractFactoryPattern
javac AbstractFactory.java
java AbstractFactory
```

Output:
```
This is Indian Contigent
This is the Indian Cricket Team
This is the Indian Football Team
---
This is USA Contigent
This is the USA Cricket Team
This is the USA Football Team
```
