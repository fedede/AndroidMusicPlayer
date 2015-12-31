package com.exfume.mupl;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.exfume.mupl.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MusicPlayer extends FragmentStatePagerAdapter implements MediaPlayer.OnCompletionListener , ViewPager.OnPageChangeListener  {

    private MediaPlayer mediaPlayer;
    private int cursor = 0;
    private ArrayList<Song> dataset;
    private HashMap<Integer, player_fragment> cachedFragmentHashMap;

    private Handler loadHandler = new Handler();
    private Runnable UpdateSong = new Runnable() {
        @Override
        public void run() {
            load();
        }
    };

    //Control Music
    private boolean isPlaying = false;
    private repeat repeatState = repeat.no;


    public MusicPlayer(ArrayList<Song> songCollection , int position, FragmentManager fm){
        super(fm);
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.dataset = songCollection;
        this.cachedFragmentHashMap = new HashMap<>();
        this.cursor = position;
        this.mediaPlayer.setOnCompletionListener(this);
    }
    public MusicPlayer(int position, FragmentManager fm){
        super(fm);
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.dataset = new ArrayList<>();
        this.cursor = position;
        this.cachedFragmentHashMap = new HashMap<>();
        this.mediaPlayer.setOnCompletionListener(this);
    }
    public MusicPlayer(FragmentManager fm){
        super(fm);
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.dataset = new ArrayList<>();
        this.cachedFragmentHashMap = new HashMap<>();
        this.mediaPlayer.setOnCompletionListener(this);
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

        }
        return song;
    }
    public void play(){
        mediaPlayer.start();
        isPlaying = true;
    }
    public void pause(){
        mediaPlayer.pause();
        isPlaying = false;
    }
    public void stop(){
        mediaPlayer.seekTo(0);
        pause();
    }
    public void reset(){
        mediaPlayer.seekTo(0);
        if (isPlaying()){
            play();
        }else{
            pause();
        }
    }
    public Song next(){
        cursor++;
        if (cursor >= dataset.size()){
            if (repeatState == repeat.all){
                cursor = 0;
            }
            else{
                cursor--;
                stop();
                return null;
            }
        }
        return load();
    }
    public Song previous(){
        if(getTime() < 1000){
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
        /**
         * In seek of a good implementation
         */
    }
    public void repeat(){
        switch (repeatState){
            case no:
                repeatState = repeat.all;
                break;
            case all:
                repeatState = repeat.single;
                break;
            case single:
                repeatState = repeat.no;
                break;
        }
    }

    public boolean isPlaying(){
        return this.mediaPlayer.isPlaying();
    }
    public void seekTo(int msec){
        mediaPlayer.seekTo(msec);
    }
    public int getTime(){
        return mediaPlayer.getCurrentPosition();
    }
    public int getDuration(){
        return mediaPlayer.getDuration();
    }
    public Song getSong(){
        return dataset.get(cursor);
    }
    public Song getSong(int index){
        return dataset.get(index);
    }
    public int getCursor(){
        return cursor;
    }
    public int getTrackNumber(){
        return cursor+1;
    }
    public void setCursor(int position){
        cursor = position;
    }
    public int getTotalTracks(){
        return dataset.size();
    }
    public void addSong(Song song){
        this.dataset.add(song);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (repeatState){
            case all:
            case no:
                next();
                break;
            case single:
                reset();
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

    public HashMap<Integer, player_fragment> getCachedFragmentHashMap() {
        return cachedFragmentHashMap;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        mediaPlayer.stop();
        cursor = position;
        loadHandler.removeCallbacks(UpdateSong);
        loadHandler.postDelayed(UpdateSong, 300);
    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public enum repeat {
        no,all,single
    }

}
