// i m implemeting three function / events running sequentially on one thread
import java.util.*;
class Task1 {
    Task1(){
        

    }
     public void implement(){
        try{
            Thread.sleep(3000);
             System.out.println("task1 completed");

        }
        catch(InterruptedException e){
            e.getStackTrace();
        }
    }
   
   
}
class Task2 {
    Task2() {
       

    }
     public void implement(){
        try{
            Thread.sleep(3000);
             System.out.println("task2 completed");

        }
        catch(InterruptedException e){
            e.getStackTrace();
        }
    }
}
class Task3{
    Task3(){
        
    }
    public void implement(){
        try{
            Thread.sleep(3000);
             System.out.println("task3 completed");

        }
        catch(InterruptedException e){
            e.getStackTrace();
        }
    }
}
public class WithoutConcurrency {
    public static void main(String [] str){
       Task1 task1 = new Task1();
       Task2 task2 = new Task2();
       Task3 task3 = new Task3();
       System.out.println("implementing the tasks");
       task1.implement();
       task2.implement();
       task3.implement();


    }
    
}
