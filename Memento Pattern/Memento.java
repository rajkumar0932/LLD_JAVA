// entrie focus of this pattern is doing a cntr Z without breaking encapsulation
// we create a nested static class to store the snapshot when save button is clicked 
// the saved resume is store inside the history class in a stack which can not modifify
// or peek the resume in it that is all getter and setter are kept private 
// responsible for making iniitaliing reumse resume 
import java.util.*;

class ResumeEditor{
    private String name;
    private String college;
    private String cgpa ;
    private List<String>skills;
    ResumeEditor(String name, String college, String cgpa, List<String>skills){
        this.name=name;
        this.college=college;
        this.cgpa=cgpa;
        this.skills=skills;
    }
     public void setName(String name){
         this.name=name;
       }
        public void setCollege(String college){
        this.college= college;
       }
        public void setCgpa(String cgpa){
        this.cgpa=cgpa;
       }
       public void setSkills(List<String> skills){
        this.skills= skills;
       }
    
    public MementoSanpshot save(){
        return new MementoSanpshot(this.name,this.college,this.cgpa,new ArrayList<>(skills));
    }
    public void restore(MementoSanpshot prevResume){
        this.name=prevResume.getName();
        this.college=prevResume.getCollege();
        this.cgpa=prevResume.getCgpa();
        this.skills=prevResume.getSkills();

    }
    public void showResume(){
        System.out.printf("Resume\n name: %s%n college : %s%n cgpa : %s%n",name,college,cgpa);
        System.out.println("skills" + skills);
    }
    public static  class MementoSanpshot{
    private String name;
    private String college;
    private String cgpa ;
    private List<String>skills;
    MementoSanpshot(String name, String college, String cgpa, List<String>skills){
        this.name=name;
        this.college=college;
        this.cgpa=cgpa;
        this.skills=skills;
       }
       private String getName(){
        return name;
       }
        private String getCollege(){
        return college;
       }
        private String getCgpa(){
        return cgpa;
       }
        private List<String> getSkills(){
        return skills;
       }
    

    }
    


}



// caretaker of resumes history

class History{
    Stack<ResumeEditor.MementoSanpshot>sk = new Stack<>();
    public void save(ResumeEditor currentVersion){
        sk.push( currentVersion.save());

    }
    public void undo(ResumeEditor currentVersion){
        if(!sk.isEmpty()){
            currentVersion.restore(sk.pop());

            

        }
    }


}

public class Memento {
    public static void main(String [] str){
        List<String>ls=new ArrayList<>(Arrays.asList("html","css","java"));
        ResumeEditor resume= new ResumeEditor("raj","nit agartala" ,"8.41",ls);
        History history = new History();
        history.save(resume);
        resume.showResume();
        resume.setCollege("nit delhi");
        resume.setName("sagar");
     
        resume.showResume();
        history.undo(resume);
        resume.showResume();



    }
    
}
