// implementing tree type flyweight
// will carry intrinsic property of class
import java.util.*;
class TreeType{
    private String color;
    private String texture;
    private String name;
    TreeType(String color, String texture, String name){
        this.color=color;
        this.texture=texture;
        this.name=name;
    }
    public void explainType(){
        System.out.println("has color "+color + " "+ " texture "+texture + " name "+ name );
    }
}
// tree carry both intrinsic as well as extrinsic
 
class Tree{
    private int x;
    private int y;
    private TreeType ls;
    Tree(int x , int y , TreeType ls){
        this.x=x;
        this.y=y;
        this.ls=ls;
    }
    void plan(){
        System.out.println("tree is planted at coordinate "+ x + " "+y+ " ");
        ls.explainType();
    }

}
// generate the tree class and memeorize it 
class factoryTreeType{
   private  static Map<String,TreeType>mp = new HashMap<>();
   public static TreeType createTreeType(String color, String texture, String name){
    String key = color +"-"+texture+"-"+name;
    if(mp.get(key)==null){
        mp.put(key,new TreeType(color,texture,name) );

    }
    return mp.get(key);

   }

}
class Forest{
    private List<Tree>forest  =new ArrayList<>();

    
   public void plantTree(String color, String texture, String name, int x ,int y){
    forest.add(new Tree(x,y,factoryTreeType.createTreeType(color,texture,name)));
   }
   public void plantedTree(){
    for(Tree t : forest){
        t.plan();
    }

   }
}

public class Flyweight {
    public static void main( String [] str){
      Forest amazon = new Forest();
      for(int i=0;i<100;i++)
      amazon.plantTree("green","dense", "mango", i, i);
    amazon.plantedTree();
    }
}
