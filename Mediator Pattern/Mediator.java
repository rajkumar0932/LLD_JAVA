// Its like working as third man between communication of two guys 
// it acts as a wall between communication between two guys 
// assume two users in which one user to trying to communicate with the other 
//it can be messy in long run so use a Mediator  to work between them
import java.util.*;
interface MediatorGuy{
    void addUser(User user);
    void sendToAll(String message, User user);
}
class UserMediator implements MediatorGuy{
   private  List<User>ls = new ArrayList<>();
   
    public void addUser(User user){
        ls.add(user);
    }


    public void sendToAll(String message, User user){
        for(int i=0;i<ls.size();i++){
            if(ls.get(i)!=user){
                ls.get(i).receiveMessage(message, user);

            }
        }

    }

}
class User{
    private String user;
    private MediatorGuy userMediator;
    User(String user  ,MediatorGuy userMediator){
        this.userMediator=userMediator;
        this.user =user;
    }
    public void doChange(String message){
        userMediator.sendToAll(message,this);

    }
    public void receiveMessage(String message, User user){
        System.out.println("received "+message +"from  "+user.details());
    }
    public String  details(){
        return user;
    }
}
public class Mediator {
    public static void main(String [] str){
        UserMediator userMediator = new UserMediator();
        User user1 =new User("raj",userMediator);
        User user2 =new User("rajkumar",userMediator);
        User user3 =new User("sagar",userMediator);
        User user4 =new User("sagar gupta",userMediator);
        userMediator.addUser(user1);
        userMediator.addUser(user2);
        userMediator.addUser(user3);
        userMediator.addUser(user4);
        user1.doChange("hello everyone");
        user4.doChange("good night");



    }
    
}
