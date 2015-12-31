package com.exfume.mupl;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.exfume.mupl.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Benjides on 31/12/2015.
 */
public class MusicPlayer extends FragmentStatePagerAdapter implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private int cursor;
    private ArrayList<Song> dataset;
    public HashMap<Integer, player_fragment> cachedFragmentHashMap;

    //Control Music
    boolean isPlaying;
    repeat repeatState;


    public MusicPlayer(ArrayList<Song> songCollection , int position, FragmentManager fm){
        super(fm);
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.dataset = songCollection;
        this.cachedFragmentHashMap = new HashMap<>();
        this.cursor = position;
        this.isPlaying = false;
        this.repeatState = repeat.no;
        this.mediaPlayer.setOnCompletionListener(this);
    }


    public HashMap<Integer, player_fragment> getCachedFragmentHashMap() {
        return cachedFragmentHashMap;
    }


    public Song load(){
        Song song = dataset.get(cursor);
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.path);
            mediaPlayer.prepare();
            if (isPlaying){
                play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return song;
    }
    public void play(){
        mediaPlayer.start();
        isPlaying = true;
    }
    public void pause(){
        mediaPlayer.stop();
        isPlaying = false;
    }
    public void stop(){
        mediaPlayer.seekTo(0);
        pause();
    }
    public void reset(){
        mediaPlayer.seekTo(0);
    }
    public Song next(){
        cursor++;
        if (cursor >= dataset.size()){
            if (repeatState == repeat.all){
                cursor = 0;
            }
            else{
                stop();
                return null;
            }
        }
        return load();
    }
    public Song previous(){
        if(getPosition() < 1000){
            cursor--;
            if (cursor < 0){
                if (repeatState == repeat.all){
                    cursor = dataset.size();
                    return load();
                }
                else{
                    cursor = 0;
                    reset();
                    return null;
                }
            }else{
                return load();
            }
        }else {
            reset();
            return null;
        }
    }
    public void shuffle(){

    }

    public boolean isPlaying(){
        return this.mediaPlayer.isPlaying();
    }
    public void seekTo(int msec){
        mediaPlayer.seekTo(msec);
        if (isPlaying)
            play();
    }
    public int getPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    public int getDuration(){
        return mediaPlayer.getDuration();
    }
    public Song getPlaying(){
        return dataset.get(cursor);
    }
    public int getCursor(){
        return cursor;
    }
    public int getTrack(){
        return cursor++;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (repeatState){
            case all:
            case no:
                next();
                break;
            case single:
                seekTo(0);
                break;
        }
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new player_fragment();
        Bundle args = new Bundle();
        args.putString("Path", dataset.get(position).path);
        fragment.setArguments(args);
        cachedFragmentHashMap.put(position, (player_fragment) fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return dataset.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        cachedFragmentHashMap.remove(position);
    }


    public enum repeat {
        no,single,all
    }

}
