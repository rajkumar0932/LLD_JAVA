// main thing is one subject is observed and other are the observer 
// when observed subject changes the entire observer should get notification about it 
// that is basically observer pattern
import java.util.*;
interface Subscriber{
    void send(String name );
}
class MobileSubscriber implements Subscriber{
    private String userName;
     MobileSubscriber(String userName){
        this.userName= userName;

     }
    public void send(String name){
        System.out.println("this is send to MobileSubscriber with username " +userName + "video :: "+name );


    }
}
class EmailSubscriber implements Subscriber{
      private String email;
    EmailSubscriber(String userName){
        this.email= userName;

     }
    public void send(String name ){
        System.out.println("this is send to EmailSubscriber with username " +email+ "video :: "+name );


    }
}
interface channel{
    void subscribe(Subscriber s);
    void unsubscribe(Subscriber s);
    void notifyUser(String s);
}
class YouTubeChannel implements channel{
   private  List<Subscriber>totalSub = new ArrayList<>();
   String name ;
   YouTubeChannel(String name )
{
    this.name=name;
}
    public void subscribe(Subscriber s){
        totalSub.add(s);

    }
    public void unsubscribe(Subscriber s){
         totalSub.remove(s);


    }
    public void notifyUser(String video){
        for(Subscriber s : totalSub){
            s.send(video);
        }
    }
    public void addVideo(String video){
        notifyUser(video);

    }

}
public class Observer {
    public static void main(String [] str){
        Subscriber sub1 = new  EmailSubscriber("kumarraj0932@gmail.com");
        Subscriber sub2 = new  EmailSubscriber("kumarraj09332@gmail.com");
        Subscriber sub3 = new  MobileSubscriber("kumarraj0932");
        YouTubeChannel  princeGaming = new YouTubeChannel("Prince Gaming");
        princeGaming.subscribe(sub3);
        princeGaming.subscribe(sub1);
        princeGaming.subscribe(sub2);
        princeGaming.addVideo("new video");




    }
    
}
