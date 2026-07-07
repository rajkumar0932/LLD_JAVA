import java.util.*;
class Task1 implements Runnable{
    public void run(){
        try{
            Thread.sleep(2000);
            System.out.println("Task 1 done");

        }
        catch(InterruptedException e){
            e.getStackTrace();
        }

    }

}
class Task2 implements Runnable {
     public void run(){
          try{
               Thread.sleep(3000);
            System.out.println("Task 2 done");

        }
        catch(InterruptedException e){
            e.getStackTrace();
        }

        
    }

}
class Task3 implements Runnable{
     public void run(){
          try{
            Thread.sleep(4000);
            System.out.println("Task 4 done");

        }
        catch(InterruptedException e){
            e.getStackTrace();
        }

        
    }

}

public class UsingRunnable {
    public static void main(String [] str){
       Task1 task1 = new Task1();
       Task2 task2 = new Task2();
       Task3 task3 = new Task3();
     
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        Thread thread3 = new Thread(task3);
        System.out.println("implementing the tasks");
        thread1.start();
        thread2.start();
        thread3.start();



    }
    
}
