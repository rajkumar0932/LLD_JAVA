// its like building a linked list where we decides
// how to order the nodes
// each nodes denotes the task
// we  can synchronise these tasks ourselfs
// it is useful were we have chain of responsibility
// like setup heirarchy  approval system
abstract class ApprovalList{
    protected ApprovalList nextApproval;
    public void setNextApproval(ApprovalList nextApproval){
        this.nextApproval = nextApproval;
    }
    abstract void approve(int n);

}
class Teacher extends ApprovalList{
  void approve(int n){
    if(n<1000){
        System.out.println("Approved by teacher");
    }
    else {
         if(nextApproval!=null){
            nextApproval.approve(n);
         }
         else{
             System.out.println("cannot approve");

         }

    }
  }

}

class Principal extends ApprovalList{
  void approve(int n){
    if(n<10000){
        System.out.println("Approved by principal");
    }
    else {
         if(nextApproval!=null){
            nextApproval.approve(n);
         }
         else{
             System.out.println("cannot approve");

         }

    }
  }

}

class Vp extends ApprovalList{
  void approve(int n){
    if(n<100000){
        System.out.println("Approved by Vp");
    }
    else {
         if(nextApproval!=null){
            nextApproval.approve(n);
         }
         else{
             System.out.println("cannot approve");

         }

    }
  }

}




public class ChainOfResponse {
    public static void main (String [] str){
        ApprovalList teacher = new Teacher();
        ApprovalList principal = new Principal();
        ApprovalList vp = new Vp();

        teacher.setNextApproval(principal);
        principal.setNextApproval(vp);
        teacher.approve(999);
        teacher.approve(9999);
        teacher.approve(99999);
    }

}
