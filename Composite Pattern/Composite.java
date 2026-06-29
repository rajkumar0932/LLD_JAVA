import java.util.ArrayList;
import java.util.List;

interface Itemcart{
    void name ();
     int price();
}
class Product implements Itemcart {
    String  product ;
    int cost;

    Product(String  product , int cost){
        this.product=product;
        this.cost =cost;

    }
    public void name (){
        System.out.println("this product is " +product);
    }
    public int price(){
        return cost;
    }

}
class ProductBundles implements Itemcart {
   private  String name;
    List<Itemcart>Products = new ArrayList<>() ;
        
  

    ProductBundles( String name ){
    this.name=name;

    }
    public  void add (Itemcart product){
        Products.add(product);

    }
    public void name (){
        System.out.println("this productBundle  is " +name);
        for(Itemcart item : Products){
            item.name();
        }
    }
    public int price(){
        int cost=0;
       for(int i=0;i<Products.size();i++){
        cost+=Products.get(i).price();

       }
       return cost;
    }

}
public class Composite {
    public  static void main(String [] str){
    Itemcart book1 = new Product("Book1", 400);
    Itemcart book2 = new Product("Book2", 200);
    Itemcart book3 = new Product("Book3", 500);
    Itemcart book4 = new Product("Book4", 350);
     Itemcart book5 = new Product("Book1", 400);
    Itemcart book6 = new Product("Book2", 200);
    Itemcart book7 = new Product("Book3", 500);
    Itemcart book8 = new Product("Book4", 350);
   System.out.println(book4.price());
    ProductBundles books1 = new ProductBundles("Books");
    ProductBundles books2 = new ProductBundles("Books");
    books1.add(book1);
    books1.add(book2);
    books1.add(book3);
    books2.add(book5);
    books2.add(book6);
    books2.add(book7);
 
    System.out.println(books1.price());
    ProductBundles Library = new ProductBundles("Library");
    Library.add(books1);
    Library.add(books2);
    Library.add(book8);
    System.out.println(Library.price());



    }
   
    


    
}
