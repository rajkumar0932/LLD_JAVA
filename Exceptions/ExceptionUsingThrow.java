import java.util.Scanner;

public class ExceptionUsingThrow {
    private static void checkAge(int age) throws Exception{
        if(age<18){
            throw new Exception("Age is below voting age");
        }
        else{
            System.out.println("Age is fine");
        }

    }    public static void main (String [] arg){
        Scanner sc= new Scanner(System.in);
        int n= sc.nextInt();
        try{

              checkAge(n);

        }
        catch(Exception e){
            System.out.println("Enter correct age");
        }
        sc.close();
      
      
    }
    
}
