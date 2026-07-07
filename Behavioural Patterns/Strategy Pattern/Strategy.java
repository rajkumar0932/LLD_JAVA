// strategy pattern is used when we want not to use "if else" statement instead of it 
// we use classes which is assigned at runtime dynamically
interface Subscription{
    void features();
}
class Free implements Subscription{
   public void features(){
        System.out.println("User get basic access to platform");
    }
}
class Pro implements Subscription{
   public void features(){
        System.out.println("User get basic + some additoonal  access to platform");
    }
}
class Max implements Subscription{
   public void features(){
        System.out.println("User get all features  access to platform");
    }
}
class SubscriptionDecider{
    Subscription access ;
    SubscriptionDecider(Subscription access){
        this.access= access;
    }
   public  void featuresAccess (){
           access.features();

    }
 

}

public class Strategy {
    public static void main (String [] str){
        SubscriptionDecider subDecider1 = new SubscriptionDecider(new Max());
        subDecider1.featuresAccess();
         SubscriptionDecider subDecider2 = new SubscriptionDecider(new Pro());
        subDecider2.featuresAccess();



    }
    
}
