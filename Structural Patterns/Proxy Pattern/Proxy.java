// caching video downloading services
import java.util.*;
interface VideoDownloader{
     String download (String video);
}
class DownloadVideo implements VideoDownloader{
   public String download(String video){
        return "Downloaded " +video;
    } 
}
class DownloadVideoProxy implements VideoDownloader{
    private DownloadVideo downloaded;
    private Map<String,String>cached;
    DownloadVideoProxy(){
        downloaded= new DownloadVideo();
        cached = new HashMap<>();

    }
     public String download(String video){
       if(cached.get(video)!=null){
        System.out.println("Already present");
        return cached.get(video);
       }
       String result = downloaded.download(video);
       cached.put(video, result);
       return result;
    }
   
}

public class Proxy {
    public static void main (String [] str){
        DownloadVideoProxy down = new DownloadVideoProxy();
        System.out.println(down.download("www.jkdd.com"));
        System.out.println(down.download("www.jkdd.com"));
    }
    
}
