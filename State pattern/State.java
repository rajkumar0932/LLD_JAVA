// we use this pattern when we have certain task which changes state over time 
// such pattern is called state pattern.
// i will implement phase of student leanring backend and how he chqanges state to go to next the concept

class CurrrentPhase{
    Phase phase ;
    CurrrentPhase(){
        this.phase= new LearnJS(); // by deafult

    }
    public void setPhase(Phase phase){
        this.phase=phase;
    }
    public void nextPhase (){
        phase.next(this);
    }
    public void drop(){
        phase.drop(this);
    }
    public void nameCurrentPhase(){
        phase.namePhase();
    }
}
interface Phase{
    void next(CurrrentPhase curr);
    void drop(CurrrentPhase curr);
    void namePhase();

}
class LearnJS implements Phase{
   public  void next(CurrrentPhase curr){
        curr.setPhase(new LearnNode());

    }
    public void drop(CurrrentPhase curr){
         curr.setPhase(new Drop());

    }
    public void namePhase (){
        System.out.println("this phase about learning java script basics and strengthen it more");
    }

}
class LearnNode implements Phase{
   public  void next(CurrrentPhase curr){
        curr.setPhase(new LearnCacheAndDB());

    }
    public void drop(CurrrentPhase curr){
         curr.setPhase(new Drop());

    }
    public void namePhase (){
        System.out.println("this phase about learning node.js basics and strengthen it more");
    }

}
class LearnCacheAndDB implements Phase{
   public  void next(CurrrentPhase curr){
       curr.setPhase(new BuildProject());

    }
    public void drop(CurrrentPhase curr){
        curr.setPhase(new Drop());

    }
    public void namePhase (){
        System.out.println("this phase about learningcache basics and strengthen it more");
    }

}
class BuildProject implements Phase{
   public  void next(CurrrentPhase curr){
       System.out.println(" congratulation!!! you finished the journey");

    }
    public void drop(CurrrentPhase curr){
        curr.setPhase(new Drop());

    }
    public void namePhase (){
        System.out.println("this phase about learning cache basics and strengthen it more");
    }

}
class Drop implements Phase{
   public  void next(CurrrentPhase curr){
       System.out.println("rest ");

    }
    public void drop(CurrrentPhase curr){
        System.out.println("already dropped");
    }
    public void namePhase (){
        System.out.println("this phase about taking rest for a while");
    }

}


public class State {
    public static void main (String [] str){
        CurrrentPhase phase = new CurrrentPhase();
        phase.nameCurrentPhase();
        phase.nextPhase();
        phase.nameCurrentPhase();
        phase.nextPhase();
        phase.nameCurrentPhase();
        phase.drop();
         phase.nameCurrentPhase();


    }
    

    
}
