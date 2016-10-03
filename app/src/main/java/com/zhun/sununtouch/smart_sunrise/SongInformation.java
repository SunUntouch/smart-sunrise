package com.zhun.sununtouch.smart_sunrise;

/**
 * Created by Sunny on 20.12.2015.
 */
class SongInformation {
    private long id;
    private String title;
    private String artist;
    private String album;
    private String path;
    //private int year;

    SongInformation(long songID, String songTitle, String songArtist, String songAlbum, String songPath){

        this.id     = songID;
        this.title  = songTitle;
        this.artist = songArtist;
        this.album  = songAlbum;
        this.path   = songPath;
    }

    long getID(){
        return this.id;
    }

    String getTitle(){
        return this.title;
    }

    String getArtist(){
        return this.artist;
    }

    String getAlbum(){
        return this.album;
    }

    String getPath(){
        return this.path;
    }
}
