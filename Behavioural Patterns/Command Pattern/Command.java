// implement a remote controller 
// which holds functionlaity
// onAc, onFan, offAc, offFan , undo method
import java.util.*;
class Fan {
    void on (){
        System.out.println("Fan is on");
    }
     void off (){
        System.out.println("Fan is off");
    }
}

class Ac {
    void on (){
        System.out.println("Ac is on");
    }
     void off (){
        System.out.println("Ac  is off");
    }
}
interface RemoteCommand {
    void execute();
    void undo();
}
class FanON implements RemoteCommand {
    Fan fan ;
    FanON(){
       this.fan =  new Fan();
    }
     
   public  void execute(){
     fan.on();

    }
   public  void undo(){
    fan.off();

    }
}
class FanOFF implements RemoteCommand {
    Fan fan ;
    FanOFF(){
       this.fan =  new Fan();
    }
     
   public  void execute(){
     fan.off();

    }
   public  void undo(){
    fan.on();

    }
} 
class AcON implements RemoteCommand {
    Ac ac ;
    AcON(){
       this.ac =  new Ac();
    }
     
   public  void execute(){
     ac.on();

    }
   public  void undo(){
    ac.off();

    }
}
class AcOFF implements RemoteCommand {
    Ac ac ;
    AcOFF(){
       this.ac =  new Ac();
    }
     
   public  void execute(){
     ac.off();

    }
   public  void undo(){
    ac.on();

    }
}
class RemoteController{
    private List<RemoteCommand>remote = new ArrayList<>();
    private Stack<RemoteCommand>sk = new Stack<>();
    public void addCommand(RemoteCommand com){
        remote.add(com);
        
    }
    public void useCommand(int index){
        if(remote.get(index)!=null){
            remote.get(index).execute();
            sk.push(remote.get(index));
        }
        else{
            System.out.println("command does not exist");
        }

    }
    public void undoButton(){
        if(!sk.isEmpty())
        sk.pop().undo();
    else System.out.println("no Last  method ");
    }
}


public class Command {
    public static void main(String [] str){
        RemoteController remote =  new  RemoteController();
        remote.addCommand(new FanON());
        remote.addCommand(new FanOFF());
        remote.addCommand(new AcON());
        remote.addCommand(new AcOFF());
        remote.useCommand(0);
        remote.useCommand(1);
        remote.useCommand(2);
        remote.useCommand(3);
        remote.undoButton();
        remote.undoButton();
        remote.undoButton();
        


    }
    
}
