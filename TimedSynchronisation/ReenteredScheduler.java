import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class SchedulerReentered{
    private final ReentrantLock lock  = new ReentrantLock();
    private final ScheduledExecutorService  scheduled = Executors.newSingleThreadScheduledExecutor();
    private Boolean locked = false;
    public void getLocked(String task){
         try{
            Boolean acquired = lock.tryLock();
        if(acquired){
            locked=true;
            System.out.println(task + " acquired");
              scheduled.schedule(()->{
                  if (locked) {
                  locked = false;
                  lock.unlock();
                  System.out.println("unlocked "+ task);
                  }
              }, 2, TimeUnit.SECONDS);

        }
      

    }
   
    finally{
        System.out.println("both task done");

    }

    }
   
}


public class ReenteredScheduler {
    public static void main (String [] str){
        SchedulerReentered s1 = new SchedulerReentered();
        Thread t1 = new Thread(()->{s1.getLocked("task1");});
        Thread t2 = new Thread(()->{s1.getLocked("task2");});
        t1.start();
        t2.start();

        


    }
    
}
