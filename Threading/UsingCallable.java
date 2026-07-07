import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class Task1 implements Callable<String>{
    public String call(){
        try{
            Thread.sleep(2000);
            return "task1 completed";
        }
        catch(InterruptedException e){
            e.getStackTrace();
             return "Task 1 interrupted";
        }

    }

}
class Task2 implements Callable<String> {
     public String call(){
          try{
               Thread.sleep(3000);
            return "Task 2 done";


        }
        catch(InterruptedException e){
            e.getStackTrace();
             return "Task 2 interrupted";
        }

        
    }

}
class Task3 implements Callable<String>{
     public String call(){
          try{
            Thread.sleep(4000);
            return "Task 3 done";

        }
        catch(InterruptedException e){
            e.getStackTrace();
             return "Task 3 interrupted";
        }

        
    }

}


public class UsingCallable {
    public  static void main(String [] str){
        Task1 task1 = new Task1();
        Task2 task2 = new Task2();
        Task3 task3 = new Task3();
        FutureTask<String> futureTask1 = new FutureTask<>(task3);
        FutureTask<String>futureTask2 = new FutureTask<>(task2);
        FutureTask<String> futureTask3 = new FutureTask<>(task1);
        Thread thread1 = new Thread(futureTask1);
        Thread thread2 = new Thread(futureTask2);
        Thread thread3 = new Thread(futureTask3);
        
       System.out.println("implementing the tasks");
       thread1.start();
       thread2.start();
       thread3.start();
      try {
    System.out.println(futureTask1.get());
    System.out.println(futureTask2.get());
    System.out.println(futureTask3.get());
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}

    }
    
}
