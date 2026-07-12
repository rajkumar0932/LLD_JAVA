// in reneterant thread i will create a booking system that will terminate after few sec automatically  

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;

class Reenterant{
    private final ReentrantLock lock = new ReentrantLock();
    public void bookSeat(String task){
        lock.lock();
        try{
             System.out.println(task +" locked");
            Thread.sleep(5002);
             System.out.println(task+ " done");
           

        }
        catch (InterruptedException e){
             e.getStackTrace();
        }

    
        finally{
            lock.unlock();
            System.out.println("task unlocked");

        }
    }


}
public class ReenterantThread {
    public static void main(String [] str){
    Reenterant re = new Reenterant();
    Thread thread1 =  new Thread(()->{re.bookSeat("task1");});
    Thread thread2 =  new Thread(()->{re.bookSeat("task2");});
    thread1.start();
    thread2.start();
    try{
        thread1.join();
        thread2.join();

    }
    catch(InterruptedException e ){
        e.printStackTrace();


    }
    }
     
}
