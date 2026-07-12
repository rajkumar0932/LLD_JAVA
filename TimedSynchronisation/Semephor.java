import java.util.Scanner;
import java.util.concurrent.Semaphore;


class Sem{
    private  Semaphore sem;
     Sem(int n){
        sem = new Semaphore(n);

     }
     public void logIn(){
         if(sem.tryAcquire()){
            System.out.println(Thread.currentThread().getName() +" login successfuly");

         }
         else{
            System.out.println(Thread.currentThread().getName() +" Too many users try agains");
         }

     }
     public void logOut(){
      sem.release();
        System.out.println(Thread.currentThread().getName()+ " logged out");
     }

}


public class Semephor {
    public static void main(String [] str){
        Scanner sc = new Scanner(System.in);
        int n= sc.nextInt();
        Sem demo = new Sem(n);
        Thread  t1  = new Thread(()->{demo.logIn();});
        Thread  t2  = new Thread(()->{demo.logIn();});
        Thread  t3  = new Thread(()->{demo.logIn();});
        Thread  t4  = new Thread(()->{demo.logIn();});
        Thread  t5  = new Thread(()->{demo.logOut();});
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
      


    }
    
}
