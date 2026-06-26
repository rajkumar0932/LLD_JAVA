
class Rectangle {
    int h,l;
    void setHeight(int h){
        this.h=h;


    }
    void  setWidth(int l){
        this.l=l;


    }
    int getArea(){
        return l*h;
    }
    
}
class Square extends Rectangle{
    @Override
    
    void setHeight(int h){
        this.h=h;
         this.l=h;


    }
    @Override
     void setWidth(int l){
        this.l=l;
           this.h=l;


    }
}

public class NotFollowing {
    public static void main (String [] args){

    Rectangle r1 = new Square();
    r1.setHeight(5);
    r1.setWidth(6);
    System.out.println(r1.getArea());
    }
}
