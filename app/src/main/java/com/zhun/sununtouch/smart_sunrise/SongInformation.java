package com.zhun.sununtouch.smart_sunrise;

/**
 * Created by Sunny on 20.12.2015.
 */
public class SongInformation {
    private long id;
    private String title;
    private String artist;
    private String album;
    private String path;
    //private int year;

    public SongInformation(long songID, String songTitle, String songArtist, String songAlbum, String songPath){

        this.id     = songID;
        this.title  = songTitle;
        this.artist = songArtist;
        this.album  = songAlbum;
        this.path   = songPath;
    }

    public long getID(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getArtist(){
        return this.artist;
    }

    public String getAlbum(){
        return this.album;
    }

    public String getPath(){
        return this.path;
    }


}
