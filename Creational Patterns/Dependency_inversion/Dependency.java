interface PaymentType{
    void pay();
}

class Paypal implements PaymentType{
    public void pay(){
        System.out.println("payment done using paypal");
    }
}

class Paytm implements PaymentType{
    public void pay(){
        System.out.println("payment done using paytm");
    }
}


class Service {
    PaymentType service ;
    Service(PaymentType service){
        this.service=service;
    }
    public void payment(){
        service.pay();
    }

}



public class Dependency {
    public static void main(String [] str){
        PaymentType paypal = new Paypal();
        Service service = new Service(paypal);
        service.payment();


    }
    
}
