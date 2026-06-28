// mvp of food delivery system 
class Authentication{
    private String email;
     private String password;
   public void AuthenticationRequired (String email , String password){
        this.email=email;
        this.password=password;
    }
    public void info(){
        System.out.println("User logged in");
    }
}

class FoodOrder{
    private String food;
     public void FoodOrderName (String food){
        this.food=food;
    }
    public void info(){
        System.out.println(food +" is ordered.");
    }
}
class Address{
    private String   street;
    Address(String street){
        this.street=street;
    }
    public void info(){
        System.out.println(street + " — your address is recorded");
    }
}

class Payment{
    private int  cost;
    Payment(int cost){
        this.cost=cost;
    }
    public void info(){
        System.out.println("Payment done");
    }
}

class Receipt{
   
   
    public void info(){
        System.out.println("Receipt is here ");
    }
}

class FoodDeliveryFacade{
    private Authentication auth;
    private FoodOrder foodOrder;
    private Address address;
    private Payment payment;
    private Receipt receipt;

    FoodDeliveryFacade(){
        this.auth      = new Authentication();
        this.foodOrder = new FoodOrder();
        this.receipt   = new Receipt();
    }

    // ONE call hides the whole multi-step subsystem from the client
    public void FoodOrderDetails(String email, String password, String food, String street, int cost){
        // 1. log the user in
        auth.AuthenticationRequired(email, password);
        auth.info();

        // 2. place the order
        foodOrder.FoodOrderName(food);
        foodOrder.info();

        // 3. record the delivery address
        this.address = new Address(street);
        address.info();

        // 4. take the payment
        this.payment = new Payment(cost);
        payment.info();

        // 5. hand over the receipt
        receipt.info();
    }
}

public class Facade {
    public static void main(String[] args){
        // the client touches ONLY the facade — not the 5 subsystem classes
        FoodDeliveryFacade order = new FoodDeliveryFacade();
        order.FoodOrderDetails("raj@gmail.com", "secret123", "Paneer Tikka", "MG Road", 350);
    }
}
