
// instaed of manually creating thread pool for every task 
// we predefine the no of thread pool we are going to use in my 
// application which enhabnces the effficiecny
// we do it using executor service

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

class EmailService{
    public static final ExecutorService pool = Executors.newFixedThreadPool(10);
    public void sendEmail(String receipt){
        pool.execute(()->{
             System.out.println(receipt+" by" +Thread.currentThread().getName());
            try{
                Thread.sleep(2000);
                System.out.println("email send to receipt "+ receipt);

            }
            catch (InterruptedException e){
               Thread.currentThread().interrupt();
            }
        });
    }

}

public class ExecuterThreadPoll {
    public static void main(String [] str){
        EmailService email = new EmailService();
        for(int i=0;i<25;i++){
            email.sendEmail("emails"+i);

        }
        EmailService.pool.shutdown();

    }
    
}
