package Prototype;

import java.util.HashMap;

public class Prototype {
 interface EmailTemplate extends Cloneable {
       EmailTemplate  emailClone();
       void header(String headerEmail );
       void content(String contentEmail);
}
 static class CloneEmail implements EmailTemplate {
    private String headerEmail;
    private String contentEmail;
     public CloneEmail() {
        headerEmail="welcome to team!!!";
        contentEmail="welcome";
     }
    public CloneEmail emailClone()  {
        try{
            return  (CloneEmail) super.clone();

        }
        catch(CloneNotSupportedException e){
            throw new RuntimeException(e);

        }

    }
    @Override
    public void header (String headerEmail){
        this.headerEmail=headerEmail;

    }
     @Override
      public void content (String contentEmail){
        this.contentEmail=contentEmail;
    }
    public void showEmail(){
        System.out.println(headerEmail);
         System.out.println(contentEmail);

    }

 }
 static class giveCloneEmail{
   private static  HashMap<String,CloneEmail>mp= new HashMap<>();
   static {
     mp.put("welcome" , new CloneEmail());
   }
   public static CloneEmail getClone( String str){
    return (CloneEmail)mp.get(str).emailClone();
   }

 }
 public static void main(String [] str){
    CloneEmail s=giveCloneEmail.getClone("welcome");
    s.showEmail();


 }

}
