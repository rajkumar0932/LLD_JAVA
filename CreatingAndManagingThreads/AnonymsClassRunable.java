



import java.util.concurrent.*;
public class AnonymsClassRunable {
    public static void main( String [] str){
        Runnable run =  new Runnable(){
            public void run(){
                try{
                    Thread.sleep(2000);
                System.out.println("task done");

                }
                catch(InterruptedException e){
                    e.getStackTrace();
                }
             
            }

        };
        Thread thread = new Thread(run);
        thread.start();

    }
    
}
