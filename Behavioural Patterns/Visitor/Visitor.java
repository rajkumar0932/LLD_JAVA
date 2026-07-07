
// when we onot want to add any functionality to an existing class rather we create a common
// functionality that can used by every class 
// just implements solid principle 
// i will take exaple of a service where i can add various functionality in all.
// when we want to add functionality in three different class and in different logic is involved in
// implementation in all three.
import java.util.*;

interface VisitorClass {
    void visitor(VisitorFunction vis);
}
class BoysHostle implements VisitorClass{
    
    public void visitor(VisitorFunction vis){
        vis.visitor(this);

    }


}
class GirlsHostle implements VisitorClass{

    public void visitor(VisitorFunction vis){
         vis.visitor(this);
        
    }
    
}
interface VisitorFunction{
    void visitor(BoysHostle bh);
    void visitor(GirlsHostle gh);
}
class Laundry implements VisitorFunction{
    public void visitor(BoysHostle bh){
        System.out.println("Boys Hostle Loundry");

    }
    public void visitor(GirlsHostle gh){
        System.out.println("Girls Hostle Loundry");

    }

}
class Playground implements VisitorFunction{
    public void visitor(BoysHostle bh){
        System.out.println("Boys Hostle Playground");

    }
    public void visitor(GirlsHostle gh){
        System.out.println("Girls Hostle PlayGround");

    }

}
public class Visitor {
    public static void main(String [] str){
        List<VisitorClass>list = new ArrayList<>();
        BoysHostle bh = new BoysHostle();
        GirlsHostle gh = new GirlsHostle();
        list.add(bh);
        list.add(gh);
        VisitorFunction Laundry = new Laundry();
        VisitorFunction Playground = new Playground();
        for(int i=0;i<list.size();i++){
            list.get(i).visitor(Laundry);
            list.get(i).visitor(Playground);
        }
    }
    
}
