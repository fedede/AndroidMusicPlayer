package com.exfume.mupl.model;

/**
 * Created by Benjides on 20/11/2015.
 */
public class Song {

    public String path;
    public String title;
    public String artist;
    public String album;
    public long duration;
    public int track;

    public Song(String path, String title, String artist, String album, long duration) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }
}
