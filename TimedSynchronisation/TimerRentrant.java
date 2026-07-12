import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;

class TimedRentrant{
    private final  ReentrantLock lock = new ReentrantLock();
    private Boolean locked = false;
    public void tryLock(String task){
      
        
            try{
             Boolean acquired = lock.tryLock(5,TimeUnit.SECONDS);
                if(acquired){

                 locked = true;
            System.out.println(task + " assigned");
            Thread.sleep(4000);
               System.out.println(task + " done");
            locked=false;
            lock.unlock();

            }
        }
            catch(InterruptedException e) {
                e.printStackTrace();

            }
           

        

    }
}
public class TimerRentrant {
    public static void main (String [] str){
        TimedRentrant t1 = new TimedRentrant();
        Thread th1 = new Thread(()->{t1.tryLock("task1");});
        Thread th2 = new Thread(()->{t1.tryLock("task2");});
        th1.start();
        th2.start();


        

    }
    
}
