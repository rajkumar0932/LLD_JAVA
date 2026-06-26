public class DoubleLayerThreadSafeSingleton {
    private static volatile DoubleLayerThreadSafeSingleton instance;
    // valtile ensures that variable is not read form cache of thread but from the main memory directly
    private DoubleLayerThreadSafeSingleton(){

    }
    public static  DoubleLayerThreadSafeSingleton getInstance(){
        if(instance==null){
            synchronized(DoubleLayerThreadSafeSingleton.class){
                if(instance==null){
                    instance = new DoubleLayerThreadSafeSingleton();
                }
            }
        }
        return instance;
    }
    
}
