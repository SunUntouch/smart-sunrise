package com.zhun.sununtouch.smart_sunrise.Information;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sunny on 02.10.2016.
 * Helper Class to get Song Information from Device
 */

@SuppressWarnings("unused")
public class SongDatabase {

    private LinkedHashMap<String, LinkedHashMap<String, HashSet<SongInformation>>> mDatabase = null;

    private int songCount = 0;
    private int albumCount = 0;
    private int artistCount = 0;

    public SongDatabase(){
        mDatabase = new LinkedHashMap<>();
    }
    public SongDatabase(SongInformation song){

        addSong(song);
    }
    public void addSong(SongInformation song){
        if(mDatabase == null)
            mDatabase = new LinkedHashMap<>();

        String artist = song.getArtist();
        String album = song.getAlbum();


        if(mDatabase.containsKey(artist))
        {
            if(mDatabase.get(artist).containsKey(album))
                mDatabase.get(artist).get(album).add(song);
            else
            {
                albumCount++;
                HashSet<SongInformation> songs = new HashSet<>();
                songs.add(song);
                mDatabase.get(artist).put(album, songs);
            }
        }

        else
        {
            artistCount++;
            albumCount++;

            HashSet<SongInformation> songs = new HashSet<>();
            songs.add(song);

            LinkedHashMap<String, HashSet<SongInformation>> albumEntry = new LinkedHashMap<>();
            albumEntry.put(album, songs);

            mDatabase.put(artist, albumEntry);
        }

        songCount++;
    }

    public String[] getArtistStrings(){
        return mDatabase.keySet().toArray(new String[mDatabase.size()]);
    }
    public String[] getAlbumStrings(String artist){
        return (mDatabase.containsKey(artist))? mDatabase.get(artist).keySet().toArray(new String[mDatabase.get(artist).size()]) : null;
    }
    public SongInformation[] getSongs(String artist, String album){

        if(!mDatabase.containsKey(artist))
            return null;
        return (mDatabase.get(artist).containsKey(album))? mDatabase.get(artist).get(album).toArray(new SongInformation[mDatabase.get(artist).get(album).size()]) : null;
    }

    public int getArtistCountCalculate(){
        return mDatabase.size();
    }
    public int getArtistCount(){
        return artistCount;
    }

    public int getAlbumCount(){
        return albumCount;
    }
    public int getAlbumCount(String artist){
        return (mDatabase.containsKey(artist)) ? mDatabase.get(artist).size() : 0;
    }

    public int getSongCount(){
        return songCount;
    }
    public int getSongCount(String artist){

        if(!mDatabase.containsKey(artist))
            return 0;

        int count = 0;
        Map<String, HashSet<SongInformation>> artistAlbums = mDatabase.get(artist);
        for( Map.Entry<String, HashSet<SongInformation>> album : artistAlbums.entrySet())
            count += album.getValue().size();
        return count;
    }
    public int getSongCount(String artist, String album){

        if(!mDatabase.containsKey(artist))
            return 0;

        return (mDatabase.get(artist).containsKey(album)) ? mDatabase.get(artist).get(album).size() : 0;
    }
}
