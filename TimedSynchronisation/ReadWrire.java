// in this type of lock  when write is called no other thread is allowed to do any thing
// when read is called only write is debarred to do anything..
// like stock market 

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ReadWriteLock{
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private String write;
    public void writeThings(String write){
        try{
      
        lock.writeLock().lock();
          System.out.println(Thread.currentThread().getName() + " access to write it ");
        Thread.sleep(6000);
        this.write= write;
        lock.writeLock().unlock();

        }
        catch(InterruptedException e){
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName() + "could not access to write it ");
        }
       
      
    }
    public void readThings(){
        try{
            
         lock.readLock().lock();
          System.out.println(Thread.currentThread().getName() + " access to read it ");
         Thread.sleep(1000);
         System.out.println(write);
         lock.readLock().unlock();

        }
        catch(InterruptedException e){
         System.out.println(Thread.currentThread().getName() + "could not access to read it ");
        }
        

    }


}
public class ReadWrire {
    public static void main(String [] str){
        ReadWriteLock readWriteLock  = new ReadWriteLock();
        Thread t1 =  new Thread(()->{readWriteLock.writeThings("raj kumar");});
       
        t1.start();
         try{
        Thread.sleep(1000);

        }
        catch(InterruptedException e){
        e.printStackTrace();
        }
         Thread t2 =  new Thread(()->{readWriteLock.readThings();});
        t2.start();
        try{
        Thread.sleep(4000);

        }
        catch(InterruptedException e){
        e.printStackTrace();
        }

         Thread t3 =  new Thread(()->{readWriteLock.readThings();});
         t3.start();

      

    }
    
}
