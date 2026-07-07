
interface Pizza{
    String name();
    int  price();
}
class VeggiesPizza  implements Pizza{
    public String name(){
        return "This is veggies pizza";

    }
    public int price (){
        return 200;
    }

}
class PlainPizza  implements Pizza{
    public String name(){
        return "This is plain pizza";

    }
    public int price (){
        return 150;
    }

}
class NonvegPizza  implements Pizza{
    public String name(){
        return "This is non veg pizza";

    }
    public int price (){
        return 250;
    }

}
abstract class PizzaDecorator implements Pizza {
   protected Pizza name;
   PizzaDecorator(Pizza name){
    this.name=name;
   }
}
class ExtraButter extends PizzaDecorator {
    public ExtraButter(Pizza name){
        super(name);

    }
    public String name(){
       return  name.name() + " extra  butter";
    }
    public int price (){
        return name.price()+50;
    }

}
class ExtraChesse extends PizzaDecorator {
    public ExtraChesse(Pizza name){
        super(name);

    }
    public String name(){
         return  name.name() + " extra  cheese";
    }
    public int price (){
        return name.price()+40;
    }

}


public class Decorator {
    public static void main (String [] arg){

        // layering it one over the other
        Pizza pizza = new PlainPizza();
        System.out.println(pizza.name());
        Pizza pizzaWithButter = new ExtraButter(pizza);
         System.out.println(pizzaWithButter.name());
          Pizza pizzaWithButterExtraCheese = new ExtraChesse(pizzaWithButter);
            System.out.println(pizzaWithButterExtraCheese.name());
        
       



    }

    
}
