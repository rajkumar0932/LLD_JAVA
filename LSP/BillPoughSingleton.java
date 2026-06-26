public class BillPoughSingleton {
    private BillPoughSingleton () {

    }
    private static class Holder{
        // class is always implemented locked by default in jvm 
        // so it complelty resolve issue of concurrnecy
        private static final BillPoughSingleton instance = new BillPoughSingleton();
    }
    public static BillPoughSingleton getInstance (){
        // inner class is not iniitlaised when referenced ..
        return Holder.instance;
    }
    
}
