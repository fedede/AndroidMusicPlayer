package com.exfume.mupl;

import android.database.Cursor;

import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;




import com.exfume.mupl.model.Song;




public class MainActivity extends AppCompatActivity {

    ViewPager mViewPager;
    MusicPlayer musicPlayer;
    int cursor;


    TextView songName;
    TextView songArtistAlbum;
    TextView songProgress;
    TextView songDuration;
    TextView songNumber;
    TextView songTotal;


    ImageButton songPlay;
    ImageButton songNext;
    ImageButton songPrevious;
    ImageButton songShuffle;
    ImageButton songRepeat;

    SeekBar seekBar;

    final Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);
        RelativeLayout mLinearLayout = (RelativeLayout) findViewById(R.id.mPagerWraper);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        mLinearLayout.measure(screenWidth, screenWidth);
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.height = screenWidth;
        params.width = screenWidth;
        mLinearLayout.setLayoutParams(params);

        musicPlayer = new MusicPlayer(getSupportFragmentManager());
        initLayout();

        mViewPager = (ViewPager) findViewById(R.id.mPager);
        mViewPager.setAdapter(musicPlayer);
        musicPlayer.load();

        //Tags
        songName = (TextView) findViewById(R.id.mupl_songName);
        songArtistAlbum = (TextView) findViewById(R.id.mupl_songArtistAlbum);
        songNumber = (TextView) findViewById(R.id.mupl_songNumber);
        songTotal = (TextView) findViewById(R.id.mupl_songTotal);
        songTotal.setText(""+musicPlayer.getTotalTracks());
        songProgress = (TextView) findViewById(R.id.mupl_songProgress);
        songDuration = (TextView) findViewById(R.id.mupl_songDuration);

        //Control Buttons
        songPlay = (ImageButton) findViewById(R.id.ab_Play);
        songPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicPlayer.isPlaying()){
                    musicPlayer.pause();
                }else{
                    musicPlayer.play();
                }
            }
        });
        songNext = (ImageButton) findViewById(R.id.ab_Next);
        songNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer.next();
                mViewPager.setCurrentItem(musicPlayer.getCursor(), true);
            }
        });
        songPrevious = (ImageButton) findViewById(R.id.ab_Previous);
        songPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer.previous();
                mViewPager.setCurrentItem(musicPlayer.getCursor(), true);
            }
        });

        songShuffle = (ImageButton) findViewById(R.id.ab_Shuffle);
        songShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer.shuffle();
            }
        });
        songRepeat = (ImageButton) findViewById(R.id.ab_Repeat);
        songRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer.repeat();
            }
        });


        //SeekBar
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                songProgress.setText(msTo(seekBar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(UpdateSongTime);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(UpdateSongTime);
                int currentPosition = seekBar.getProgress();
                musicPlayer.seekTo(currentPosition);
                mHandler.postDelayed(UpdateSongTime, 100);
            }
        });
        mViewPager.addOnPageChangeListener(musicPlayer);
        setFields();


    }


    private void initLayout() {
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        final String[] cursor_cols = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
        };

        final String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        final Cursor cursor = getContentResolver().query(uri, cursor_cols, selection, null, sortOrder);

        while (cursor.moveToNext()) {
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            Long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

            Song songObject = new Song(data, title, artist, album, duration);
            musicPlayer.addSong(songObject);

        }
    }



    private void setFields() {
        cursor = musicPlayer.getCursor();
        Song song = musicPlayer.getSong();
        float finalTime = song.duration;
        mHandler.removeCallbacks(UpdateSongTime);
        songNumber.setText(""+musicPlayer.getTrackNumber());
        seekBar.setProgress(0);
        seekBar.setMax((int) finalTime);
        songProgress.setText(msTo(0));
        songDuration.setText(msTo((int) finalTime));
        songName.setText(song.title);
        String ArtistAlbum = song.artist + " - " + song.album;
        songArtistAlbum.setText(ArtistAlbum);
        UpdateSongTime.run();
        mViewPager.setCurrentItem(cursor,true);
    }




    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            if(musicPlayer.getCursor() != cursor){
                setFields();
            }
            int actualTime = musicPlayer.getTime();
            seekBar.setProgress(actualTime);
            songProgress.setText(msTo(actualTime));
            mHandler.postDelayed(this, 100);
        }
    };


    private String msTo(long duration) {
        duration = duration / 1000;
        int seconds = (int) duration % 60;
        duration /= 60;
        int minutes = (int) duration % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }


}
