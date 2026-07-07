import java.util.Scanner;
;

public class ExceptionInjava1 {
    public static void main (String []  args){
        Scanner sc = new Scanner(System.in);
        int num1=sc.nextInt();
        int num2=sc.nextInt();
      
        try {
            System.out.println(num1/num2);


        }
        catch(Exception e){
         System.out.println("Divison by zero not allowed");

    }
     finally {
          System.out.println("Program ended");

     }
     sc.close();
    

    
}
}
