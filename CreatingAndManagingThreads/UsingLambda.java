import java.util.concurrent.*;


public class UsingLambda {
     public static void main( String [] str){
        Runnable task =()->{
           
                try{
                    Thread.sleep(2000);
                System.out.println("task done");

                }
                catch(InterruptedException e){
                    e.getStackTrace();
                }
             
            

        };
        Thread thread = new Thread(task);
        thread.start();

    }
    

    
}
