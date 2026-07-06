interface Pizza {
    public void type();
}

class CheesePizza implements Pizza {
    @Override
    public void type() {
        System.out.println("ChessePizza");

    }

}
class ChickenPizza implements Pizza {
    @Override
    public void type(){
        System.out.println("Chicken pizza");
    }
}
class PizzaFactory{
    public Pizza orderType(String name){
        if(name.equalsIgnoreCase("Cheese")){

            return new CheesePizza();
        }
        else{
            return new ChickenPizza();
        }
    }
}
public class FactoryMethodBadMethod {

}
