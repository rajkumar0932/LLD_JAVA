public class LazySingleton {
    private static  LazySingleton instance ;
    private LazySingleton (){

    }
    public static LazySingleton getReference(){
        if(instance==null){
            return instance = new LazySingleton();
        }
        return instance;

    }
    
}
