// video playlist iteraator 
// in one word replace any iterator with a class which can iterate 

import java.util.*;

class Video {
    private String title;
    private String description;
    Video(String title, String description){
        this.title= title;
        this.description= description;

    } 
    void showVideoDetails(){
        System.out.println("Title is "+ title + "Description is "
            + description
        );
    }
}

class Videoplaylist{
    private List<Video> videoPlaylist = new ArrayList<>();
    public void addVideo(Video video){
        videoPlaylist.add(video);
    }
    public List<Video> getVideos(){
        return videoPlaylist;
    }

}
interface  VideoIterator{
    Video nextVideo();
}
class VideoplayLoopIterator  implements VideoIterator{
   private  int size=0;
   private List<Video> videoPlaylist = new ArrayList <>();
   VideoplayLoopIterator(  List<Video> videoPlaylist){
  
    this.videoPlaylist =videoPlaylist;
 }
 public Video nextVideo(){
    if(size == videoPlaylist.size()){
        return null;
    }
    return videoPlaylist.get(size++);
 }
}
public class Iterator {
    public static void main( String [] arg){
        Video video1= new Video("dsa", "lec1" );
        Video video2= new Video("dsa", "lec2" );
        Video video3= new Video("dsa", "lec3" );
        Videoplaylist videoPlaylist = new Videoplaylist();
        videoPlaylist.addVideo(video1);
        videoPlaylist.addVideo(video2);
        videoPlaylist.addVideo(video3);
        VideoplayLoopIterator iterator = new VideoplayLoopIterator(videoPlaylist.getVideos());
        Video v =iterator.nextVideo();
        while(v!=null){
             v.showVideoDetails();
             v=iterator.nextVideo();

 }
       






    }

    
}
