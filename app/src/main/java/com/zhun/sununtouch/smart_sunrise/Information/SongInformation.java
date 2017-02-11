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
