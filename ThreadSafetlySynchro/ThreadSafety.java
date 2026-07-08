// in this practice i will be programaming various method which deal with thread safety and synchronization
// three method
// without thread safety
// using synchronized keyword on class or block
// using volatile
// using atomic vatibale int, bool

import java.util.concurrent.atomic.AtomicInteger;

class withSynchonized{
    private int  n;
    public  void increment(){
        synchronized(this){
               n++;

        }
     
    }
    public int  getValue(){
        return n;
    }
}
class withValatile{
    private volatile int  n;
    public  void increment(){
      
               n++;

        
     
    }
    public int  getValue(){
        return n;
    }
}
class AtomicVariables{
    private AtomicInteger n  = new AtomicInteger(0);
    public void increment(){
        int curr, next;
        do{
            curr=n.get();
            next=curr+1;

        }
        while(!n.compareAndSet(curr,next));

        
        
    }
    public int  getValue(){
        return n.get();
    }
}
class WithoutThreadSafety{
    private int  n;
    public void increment(){
        n++;
    }
    public int  getValue(){
        return n;
    }

}
class ThreadSafety{
    public static void main(String [] str) throws InterruptedException{
    WithoutThreadSafety w = new WithoutThreadSafety();
    withSynchonized v = new withSynchonized(); 
    AtomicVariables at = new AtomicVariables();
    withValatile u = new withValatile();
    Runnable r =()->{
        for(int i=0;i<1000;i++){
            w.increment();

        }

    };
    Runnable r2 = ()->{
        for(int i=0;i<1000;i++){
            v.increment();
        }
    };
      Runnable r3 = ()->{
        for(int i=0;i<1000;i++){
            u.increment();
        }
    };
     Runnable r4 = ()->{
        for(int i=0;i<1000;i++){
            at.increment();
        }
    };
    Thread thread1 = new Thread(r);
    Thread thread2 =new Thread (r);
    Thread thread3 = new Thread(r2);
    Thread thread4 =new Thread (r2);
    Thread thread5 = new Thread(r3);
    Thread thread6 =new Thread (r3);
    Thread thread7 = new Thread(r4);
    Thread thread8 =new Thread (r4);
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();
    System.out.println(w.getValue());
    thread3.start();
    thread4.start();
    thread3.join();
    thread4.join();
     System.out.println(v.getValue());
    thread5.start();
    thread6.start();
    thread5.join();
    thread6.join();
      System.out.println(u.getValue());
    thread7.start();
    thread8.start();
    thread7.join();
    thread8.join();
    System.out.println(at.getValue());


}

}

