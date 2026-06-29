// video quality 
interface VideoQuality{
    void qualityName();
}

class HD implements VideoQuality{
    public void qualityName(){
        System.out.println("Video quality is HD");
    }
}
class QHD implements VideoQuality{
    public void qualityName(){
        System.out.println("Video quality is QHD");
    }
}
class K4 implements VideoQuality{
    public void qualityName(){
        System.out.println("Video quality is 4k");
    }
}
 class Mobile extends DeviceQualityChecker{
   Mobile(VideoQuality vd){
            super(vd);
        }
    public void play(){
        System.out.print("Mobile: ");
        vquality.qualityName();
    }
 }
 class Pc extends DeviceQualityChecker{
    Pc(VideoQuality vd){
            super(vd);
        }
    public void play(){
        System.out.print("Pc: ");
        vquality.qualityName();
    }
  
 }

  class Tablet extends DeviceQualityChecker{
        Tablet(VideoQuality vd){
            super(vd);
        }
    public void play(){
        System.out.print("Tablet: ");
        vquality.qualityName();
    }
 }
 abstract class DeviceQualityChecker{
  
    VideoQuality vquality;
    DeviceQualityChecker(VideoQuality vquality){
       
        this.vquality=vquality;

    }
    abstract public void play();
       

    



}

public class Bridge {
    public static void main(String[] str){
        // mix any device with any quality — 3 + 3 classes, not 3 x 3
        new Mobile(new HD()).play();
        new Pc(new K4()).play();
        new Tablet(new QHD()).play();

        // same device class, different quality — the bridge in action
        DeviceQualityChecker mobile4k = new Mobile(new K4());
        mobile4k.play();
    }
}
