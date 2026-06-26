import java.util.*;
interface TaxCalculator {
    double calculateTax(double income);
}
class IndianTaxCalculator implements TaxCalculator 
{
    @Override
    public double calculateTax(double income){
        return income*(0.18);
    }
}


class Main {
    public static void main (String[] arg){
        IndianTaxCalculator ind = new IndianTaxCalculator();
        System.out.println(ind.calculateTax(2000));


    }

}