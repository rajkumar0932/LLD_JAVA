import java.util.*;
class Notification {
    void giveNotification(){
        System.out.println("The Notification is sent");
    }
}
class EmailNotification extends Notification{
    @Override 
     void giveNotification(){
        System.out.println("The email Notification is sent");
    }

}
class SmsNotification extends Notification{
    @Override 
     void giveNotification(){
        System.out.println("The sms Notification is sent");
    }

}
class Following {
    public static void main(String [] arg){
        Notification p = new SmsNotification();
        p.giveNotification();


    } 

}