interface Medium {
    void send();
}
class Land implements Medium {
     public void send() {
        System.out.println("send through land");
    }
}
class Air implements Medium {
     public void send() {
        System.out.println("send through air");
    }
}
class Service {
     
    public static Medium getService (String  name) throws Exception{
        if(name.equalsIgnoreCase("Air")){
            return new Air();
        }
        else if(name.equalsIgnoreCase("Land")){
            return new Land();

        } 
        throw new Exception("not available mode");
    }
}
class DoServices{
    public static void main(String modeOfService ){
    try{
       Medium mode= Service.getService(modeOfService);
       mode.send();
        
    }
    catch(Exception e){
       System.out.println("error");
    }
  }
    
}
public class GoodPracticeFactoryMethod {
    public static void main( String [] str){

      DoServices.main("sir");


    }
    
}
