package com.zhun.sununtouch.smart_sunrise.Information;

/**
 * Created by Sunny on 20.12.2015.
 * Model to represent a Song
 */

public class SongInformation {
    private final long id;
    private final String title;
    private final String artist;
    private final String album;
    private final String path;

    public SongInformation(long songID, String songTitle, String songArtist, String songAlbum, String songPath){

        id     = songID;
        title  = songTitle;
        artist = songArtist;
        album  = songAlbum;
        path   = songPath;
    }

    public long getID(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public String getArtist(){
        return artist;
    }
    public String getAlbum(){
        return album;
    }
    public String getPath(){
        return path;
    }
}
