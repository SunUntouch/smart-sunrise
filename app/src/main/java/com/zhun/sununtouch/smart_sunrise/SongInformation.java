package com.zhun.sununtouch.smart_sunrise;

/**
 * Created by Sunny on 20.12.2015.
 */
class SongInformation {
    private final long id;
    private final String title;
    private final String artist;
    private final String album;
    private final String path;

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
