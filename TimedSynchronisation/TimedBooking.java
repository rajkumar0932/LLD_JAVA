import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

// BookMyShow: two users try to book the SAME seat.
// One gets it; the other WAITS UP TO 3 SECONDS, then gives up (stays responsive).
public class TimedBooking {

    private static final ReentrantLock seat = new ReentrantLock();

    static void book(String user) {
        try {
            // try to grab the seat, but wait no longer than 3 seconds
            if (seat.tryLock(3, TimeUnit.SECONDS)) {
                try {
                    System.out.println(user + " got the seat, booking...");
                    Thread.sleep(5000);                 // booking takes 5 seconds
                    System.out.println(user + " finished booking");
                } finally {
                    seat.unlock();                      // same thread releases the seat
                }
            } else {
                System.out.println(user + " gave up - seat was busy");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        new Thread(() -> book("Alice")).start();
        new Thread(() -> book("Bob")).start();
    }
}
