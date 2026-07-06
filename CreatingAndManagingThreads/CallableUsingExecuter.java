
import java.util.concurrent.*;

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




public class CallableUsingExecuter {
    public  static void main(String [] str) throws Exception{
        Task1 task1 = new Task1();
        Task2 task2 = new Task2();
        Task3 task3 = new Task3();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        Future<String> f1 = pool.submit(task1);
        Future<String> f2 = pool.submit(task2);
        Future<String> f3 = pool.submit(task3);
        System.out.println(f1.get());
        System.out.println(f2.get());
        System.out.println(f3.get());
        pool.shutdown();

        

    }
    
    
}
