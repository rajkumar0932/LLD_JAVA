import java.util.concurrent.*;
import java.util.*;

class Service {
    public static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public Future<String> sendEmail(String receipt) {          // return the Future, no get() here
        return pool.submit(() -> {
            try {
                Thread.sleep(2000);
                return "sent to " + receipt + " by " + Thread.currentThread().getName();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "interrupted: " + receipt;
            }
        });
    }
}

public class UsingSubmit {
    public static void main(String[] str) throws Exception {
        Service ser = new Service();
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 1; i < 25; i++) {
            futures.add(ser.sendEmail("reader " + i));   // submit all 24 — fast, no blocking
        }
        for (Future<String> f : futures) {
            System.out.println(f.get());                 // NOW collect & print each result
        }
        Service.pool.shutdown();
    }
}
