import java.util.*;
class Task1 extends Thread{
    Task1(){
       
    }
    @Override
    public void run(){
        try{
            Thread.sleep(4000);
            System.out.println("task 1 done");

        }
        catch(InterruptedException e){
            e.getStackTrace();

        }
    }
}
class Task2 extends Thread{
    Task2 (){

    }
     @Override
    public void run(){
        try{
            Thread.sleep(2000);
             System.out.println("task 2 done");

        }
        catch(InterruptedException e){
            e.getStackTrace();
        }
    }
}
class Task3 extends Thread{
    Task3 (){

    }
     @Override
    public void run(){
        try{
            Thread.sleep(3000);
             System.out.println("task 3 done");

        }
        catch(InterruptedException e){
            e.getStackTrace();

        }
    }
}

public class ExtendingThread {
    public static  void main  (String [] str){
       Task1 task1 = new Task1();
       Task2 task2 = new Task2();
       Task3 task3 = new Task3();
       System.out.println("implementing the tasks");
       task1.start();
       task2.start();
       task3.start();
    }
    
}
