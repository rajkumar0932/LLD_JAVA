public class singleton_design {
    private static final singleton_design instance = new singleton_design();
    private singleton_design (){

    }
    public static  singleton_design getInstance(){
        return instance;
    }
    
}
