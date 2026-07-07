
interface paymentGateway{
    void type();
    int sendMoney(int debit);
}
 class paytm implements paymentGateway{
    private int balance;
   public paytm(int balance){
        this.balance=balance;
    }
    public void type(){
      
    }
    public int sendMoney(int debit){
        if(balance>=debit)
        balance-=debit;
    else{
        throw new RuntimeException("Balance deficit");
        }

        return debit;
    }
}
class payPhone {
    private int balance;
    public payPhone(int balance){
        this.balance=balance;
    }
  
    public void print(){
          System.out.println("This is payphone type");

    }
    public int deductMoney (int money){
          if(balance>=money)
        balance-=money;
    else{
        throw new RuntimeException("Balance deficit");
        }

        return money;

    }
}
class payPhoneHandler implements paymentGateway{
    public payPhone payphone;
    payPhoneHandler(int balance){
     payphone = new payPhone(balance);

    }
    public void type(){
        payphone.print();

    }
    public int sendMoney(int money){
        return payphone.deductMoney(money);
    }
     

}

 class choosingPaymentGateway{
    private paymentGateway method ;
    public choosingPaymentGateway(paymentGateway s){
        this.method = s;
    }
    public void get(){
        method.type();

    }


 }
 public class Adaptor {
    public static void main(String[] str){
        choosingPaymentGateway chosse = new choosingPaymentGateway(new payPhoneHandler(1000));
        chosse.get();
    }
 }


