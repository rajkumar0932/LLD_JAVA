import java.util.*;

// The subject keeps observers and notifies them when its state changes.
interface Subscriber {
    void update(String channelName, String videoTitle);
}

class MobileSubscriber implements Subscriber {
    private String userName;

    MobileSubscriber(String userName) {
        this.userName = userName;
    }

    public void update(String channelName, String videoTitle) {
        System.out.println("Mobile notification for " + userName + ": " + channelName
            + " uploaded " + videoTitle);
    }
}

class EmailSubscriber implements Subscriber {
    private String email;

    EmailSubscriber(String email) {
        this.email = email;
    }

    public void update(String channelName, String videoTitle) {
        System.out.println("Email sent to " + email + ": " + channelName
            + " uploaded " + videoTitle);
    }
}

interface Channel {
    void subscribe(Subscriber subscriber);
    void unsubscribe(Subscriber subscriber);
    void notifySubscribers(String videoTitle);
}

class YouTubeChannel implements Channel {
    private List<Subscriber> subscribers = new ArrayList<>();
    private String name;

    YouTubeChannel(String name) {
        this.name = name;
    }

    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void notifySubscribers(String videoTitle) {
        for (Subscriber subscriber : subscribers) {
            subscriber.update(name, videoTitle);
        }
    }

    public void uploadVideo(String videoTitle) {
        System.out.println(name + " uploaded: " + videoTitle);
        notifySubscribers(videoTitle);
    }
}

public class Observer {
    public static void main(String[] args) {
        Subscriber emailSubscriber1 = new EmailSubscriber("kumarraj0932@gmail.com");
        Subscriber emailSubscriber2 = new EmailSubscriber("kumarraj09332@gmail.com");
        Subscriber mobileSubscriber = new MobileSubscriber("kumarraj0932");

        YouTubeChannel princeGaming = new YouTubeChannel("Prince Gaming");
        princeGaming.subscribe(mobileSubscriber);
        princeGaming.subscribe(emailSubscriber1);
        princeGaming.subscribe(emailSubscriber2);

        princeGaming.uploadVideo("Observer Pattern in Java");
    }
}
