# Iterator Pattern

This example demonstrates the **Iterator design pattern** using a simple video playlist.

The Iterator pattern lets client code access elements of a collection one by one without directly depending on how that collection is stored internally.

## Problem

If client code directly works with a `List<Video>`, it becomes tied to that exact data structure.

The Iterator pattern solves this by giving the client a separate iterator object that controls traversal.

## Classes In This Example

| Class / Interface | Responsibility |
| --- | --- |
| `Video` | Represents one video with a title and description. |
| `Videoplaylist` | Stores videos in a private list and exposes methods to add or access them. |
| `VideoIterator` | Defines the iterator contract with `nextVideo()`. |
| `VideoplayLoopIterator` | Implements the traversal logic for the playlist. |
| `Iterator` | Contains the `main` method and demonstrates how the iterator is used. |

## How It Works

1. Create multiple `Video` objects.
2. Add them to a `Videoplaylist`.
3. Pass the playlist videos to `VideoplayLoopIterator`.
4. Keep calling `nextVideo()` until it returns `null`.

```java
Video v = iterator.nextVideo();

while (v != null) {
    v.showVideoDetails();
    v = iterator.nextVideo();
}
```

## Important Note

Do not call `nextVideo()` twice in the same loop condition/body unless you intentionally want to skip items.

This is incorrect:

```java
while (iterator.nextVideo() != null) {
    iterator.nextVideo().showVideoDetails();
}
```

The first call moves the iterator forward, and the second call moves it forward again. At the end of the playlist, the second call can return `null`, causing a `NullPointerException`.

## Run The Program

From the repository root:

```bash
javac "Iterator patteren/Iterator.java"
java -cp "Iterator patteren" Iterator
```

Or from inside the `Iterator patteren` folder:

```bash
javac Iterator.java
java Iterator
```

## Expected Output

```text
Title is dsaDescription is lec1
Title is dsaDescription is lec2
Title is dsaDescription is lec3
```

## Benefit

The traversal logic stays inside `VideoplayLoopIterator`, so the client code does not need to know how the playlist is stored internally.
