
// template for two different function let say Role based Authentication  for ecommerce
abstract class UserRole{
    public final void Backend(String email, String password ){
        validation(email, password);
        authentication(email, password);
        authorization();
         services();
         review();
         logout();
     }
    private void validation(String email, String password){
           System.out.println("user validated");

     }
    private void authentication(String email, String password){
           System.out.println("user authenticated");
        
     }
     abstract  void authorization();
     abstract void services();
     protected void review(){
        System.out.println("good experience");
     }
     private void logout(){
           System.out.println("user logged out");
     }
}
class NormalUser extends UserRole{
    @Override
     void authorization(){
        System.out.println("authorize to do normal user stuffs");
     }
     @Override
     void services()
     {
          System.out.println("do stuffs like buying , cancelling items , adding items to cart ");

     }
  
     @Override
     protected void review(){
        System.out.println("bad experience ");
     }

}
class Admin extends UserRole{
    @Override
     void authorization(){
        System.out.println("authorize to do Admin stuffs");
     }
     @Override
     void services()
     {
          System.out.println("do stuffs monitor, adding items to website ");

     }
  
    

}

public class Template {
    public static void main(String [] str){
        UserRole userrole = new Admin();
        userrole.Backend("raj","123457");
    
        UserRole userrole2 = new NormalUser();
        userrole2.Backend("sagar","1234567");
      
       
    }
    
}
