public class AbstractFactory {
    interface Country {
        void name();
    }

    interface CricketTeam {
        void team();
    }

    interface FootBallTeam {
        void team();
    }

    // indian contigent
    static class India implements Country {
        @Override
        public void name() {
            System.out.println("This is Indian Contigent");

        }
    }
    static class IndianCricket implements CricketTeam{
        @Override
        public void team(){
            System.out.println("This is the Indian Cricket Team");
        }

    }
     static class IndianFootball implements FootBallTeam{
        @Override
        public void team(){
            System.out.println("This is the Indian Football Team");
        }

    }
     // USA contigent
    static class USA implements Country {
        @Override
        public void name() {
            System.out.println("This is USA Contigent");

        }
    }
    static class USACricket implements CricketTeam{
        @Override
        public void team(){
            System.out.println("This is the USA Cricket Team");
        }

    }
     static class USAFootball implements FootBallTeam{
        @Override
        public void team(){
            System.out.println("This is the USA Football Team");
        }

    }
    // interface abstract factory
    interface Contigent {
        Country Name();
        CricketTeam cricketTeam();
        FootBallTeam FootballTeam();
}
// indian abstract factory
 static class IndianContigent implements Contigent {
     public Country Name(){
        return new India();
     }
      public CricketTeam cricketTeam(){
        return new IndianCricket();
     }
     public  FootBallTeam FootballTeam(){
        return new IndianFootball();
     }

 }
 // usa abstract factory
 static class USAContigent implements Contigent {
     public Country Name(){
        return new USA();
     }
      public CricketTeam cricketTeam(){
        return new USACricket();
     }
     public  FootBallTeam FootballTeam(){
        return new USAFootball();
     }

 }
 // service
 static class ContigentChoose {
    private Country Name;
    private  CricketTeam cricketTeam;
    private  FootBallTeam FootballTeam;
    public ContigentChoose(Contigent c){
        this.Name=c.Name();
        this.cricketTeam=c.cricketTeam();
        this.FootballTeam=c.FootballTeam();
     }
     public void showInfo(){
        Name.name();
        cricketTeam.team();
        FootballTeam.team();
     }


 }


    public static void main(String[] str) {
        ContigentChoose india = new ContigentChoose(new IndianContigent());
        india.showInfo();

        System.out.println("---");

        ContigentChoose usa = new ContigentChoose(new USAContigent());
        usa.showInfo();
    }

}
