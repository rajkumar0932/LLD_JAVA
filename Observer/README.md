# Observer Pattern

This example demonstrates the **Observer design pattern** using a simple YouTube-style notification system.

The Observer pattern is useful when one object changes and many dependent objects should be notified automatically.

## Problem

If a `YouTubeChannel` directly handles every notification type, the class becomes tightly coupled to email, mobile, SMS, or any future notification logic.

That makes the code harder to extend because every new notification type requires changing the channel class.

## Solution

The Observer pattern separates the object being watched from the objects that react to it.

In this example:

| Pattern Role | Code |
| --- | --- |
| Subject | `YouTubeChannel` |
| Subject interface | `Channel` |
| Observer interface | `Subscriber` |
| Concrete observers | `EmailSubscriber`, `MobileSubscriber` |
| Event | `uploadVideo(String videoTitle)` |

## How It Works

1. Subscribers register with the channel using `subscribe()`.
2. The channel stores them in a list.
3. When `uploadVideo()` is called, the channel notifies all subscribers.
4. Each subscriber reacts in its own way through `update()`.

```java
public void uploadVideo(String videoTitle) {
    System.out.println(name + " uploaded: " + videoTitle);
    notifySubscribers(videoTitle);
}
```

The channel does not need to know how email or mobile notification works. It only calls the common observer method:

```java
subscriber.update(name, videoTitle);
```

## Run The Program

From the repository root:

```bash
javac "Observer/Observer.java"
java -cp "Observer" Observer
```

Or from inside the `Observer` folder:

```bash
javac Observer.java
java Observer
```

## Expected Output

```text
Prince Gaming uploaded: Observer Pattern in Java
Mobile notification for kumarraj0932: Prince Gaming uploaded Observer Pattern in Java
Email sent to kumarraj0932@gmail.com: Prince Gaming uploaded Observer Pattern in Java
Email sent to kumarraj09332@gmail.com: Prince Gaming uploaded Observer Pattern in Java
```

## Benefit

New subscriber types can be added without modifying `YouTubeChannel`.

For example, you could add `SmsSubscriber` later by implementing the `Subscriber` interface.
