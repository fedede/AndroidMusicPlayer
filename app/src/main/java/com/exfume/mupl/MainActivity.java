package com.exfume.mupl;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.exfume.mupl.adapter.QueueAdapter;
import com.exfume.mupl.model.Song;
import com.exfume.mupl.transformer.DepthPageTransformer;
import com.exfume.mupl.transformer.PileTransformer;
import com.exfume.mupl.transformer.ZoomOutPageTransformer;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    ArrayList<Song> mSongData;
    ArrayList<Song> mDataBackup;

    PagerAdapter mPageAdapter;
    ViewPager mViewPager;

    QueueAdapter mQueueAdapter;
    RecyclerView mRecyclerView;


    MediaPlayer mediaPlayer;

    int accentColor;

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

    FloatingActionButton mFab;

    SeekBar seekBar;


    int currentPosition = 0;
    long actualTime = 0;
    long finalTime = 0;

    int forwardTime = 5000;
    int backwardTime = 5000;

    int isRepeat = 0;

    boolean isShuffle = false;

    final Handler mHandler = new Handler();
    final Handler loadHandler = new Handler();

    boolean isPlaying = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.main_layout);
        RelativeLayout mLinearLayout = (RelativeLayout) findViewById(R.id.mPagerWraper);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        mLinearLayout.measure(screenWidth, screenWidth);
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.height = screenWidth;
        params.width = screenWidth;
        mLinearLayout.setLayoutParams(params);


        mSongData = new ArrayList<>();
        mDataBackup = new ArrayList<>();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        initLayout();

        mViewPager = (ViewPager) findViewById(R.id.mPager);
        mPageAdapter = new PagerAdapter(getSupportFragmentManager(), mSongData);
        mViewPager.setAdapter(mPageAdapter);


        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layout);
        mQueueAdapter = new QueueAdapter(mSongData);
        mRecyclerView.setAdapter(mQueueAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mQueueAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        accentColor = getResources().getColor(R.color.colorAccent);

        //Tags
        songName = (TextView) findViewById(R.id.mupl_songName);
        songArtistAlbum = (TextView) findViewById(R.id.mupl_songArtistAlbum);

        songNumber = (TextView) findViewById(R.id.mupl_songNumber);
        songTotal = (TextView) findViewById(R.id.mupl_songTotal);

        songTotal.setText("" + mSongData.size());


        songProgress = (TextView) findViewById(R.id.mupl_songProgress);
        songDuration = (TextView) findViewById(R.id.mupl_songDuration);


        mFab = (FloatingActionButton) findViewById(R.id.mFab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mRecyclerView.getVisibility()){
                    case View.GONE:
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mViewPager.setVisibility(View.GONE);
                        break;
                    case View.VISIBLE:
                        mRecyclerView.setVisibility(View.GONE);
                        mViewPager.setVisibility(View.VISIBLE);
                        break;
                }


            }
        });

        //Control Buttons
        songPlay = (ImageButton) findViewById(R.id.ab_Play);
        songPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    songPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    isPlaying = false;
                } else {
                    mediaPlayer.start();
                    songPlay.setImageResource(R.drawable.ic_pause_white_24dp);
                    isPlaying = true;
                }
            }
        });

        songNext = (ImageButton) findViewById(R.id.ab_Next);
        songNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });


        songPrevious = (ImageButton) findViewById(R.id.ab_Previous);
        songPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition--;
                mViewPager.setCurrentItem(currentPosition, true);
            }
        });


        songShuffle = (ImageButton) findViewById(R.id.ab_Shuffle);
        songShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffle();
            }
        });

        songRepeat = (ImageButton) findViewById(R.id.ab_Repeat);
        songRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (isRepeat) {
                    case 0:
                        songRepeat.setColorFilter(accentColor, PorterDuff.Mode.MULTIPLY);
                        isRepeat++;
                        break;
                    case 1:
                        songRepeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                        isRepeat++;
                        break;
                    case 2:
                        songRepeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                        songRepeat.clearColorFilter();
                        isRepeat = 0;
                        break;
                }

            }
        });


        //SeekBar
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        mHandler.postDelayed(UpdateSongTime, 100);

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
                mediaPlayer.seekTo(currentPosition);
                mHandler.postDelayed(UpdateSongTime, 100);
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Manage all things here
                currentPosition = position;
                mediaPlayer.stop();
                Log.i("MusicPLayer", "Selected: " + mSongData.get(position).title);
                setFields();
                initSong();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isRepeat == 2) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                } else {
                    nextSong();
                }
            }
        });

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
            mSongData.add(songObject);

        }
    }

    private void loadMusic() {
        String dataSource = mSongData.get(currentPosition).path;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(dataSource);
            mediaPlayer.prepare();
            if (isPlaying) {
                mediaPlayer.start();
            }
            mHandler.postDelayed(UpdateSongTime, 100);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Exception : " + e.getMessage(), Toast.LENGTH_SHORT);
        }


    }

    private void setFields() {

        finalTime = mSongData.get(currentPosition).duration;

        mHandler.removeCallbacks(UpdateSongTime);
        songNumber.setText("" + (currentPosition + 1));
        seekBar.setProgress(0);
        seekBar.setMax((int) finalTime);
        songProgress.setText(msTo(0));
        songDuration.setText(msTo(finalTime));


        songName.setText(mSongData.get(currentPosition).title);
        String ArtistAlbum = mSongData.get(currentPosition).artist + " - " + mSongData.get(currentPosition).album;
        songArtistAlbum.setText(ArtistAlbum);

    }

    private void nextSong() {

        switch (isRepeat) {
            case 2:
            case 0:
                currentPosition++;
                mViewPager.setCurrentItem(currentPosition);
                mRecyclerView.scrollToPosition(currentPosition);
                break;
            case 1:
                currentPosition = currentPosition >= mSongData.size() ? currentPosition = 0 : currentPosition + 1;
                mViewPager.setCurrentItem(currentPosition);
                break;
        }
    }

    private void previousSong() {

    }

    private void shuffle() {
        if (!isShuffle) {
            mDataBackup = mSongData;
            mSongData.remove(currentPosition);
            Collections.shuffle(mSongData);
            mSongData.add(currentPosition, mDataBackup.get(currentPosition));
            songShuffle.setColorFilter(accentColor, PorterDuff.Mode.MULTIPLY);
            Toast.makeText(this,"Shuffling all songs", Toast.LENGTH_LONG);
        } else {
            mSongData = mDataBackup;
            mDataBackup.clear();
            songShuffle.clearColorFilter();
            Toast.makeText(this,"Normal Reproduction", Toast.LENGTH_LONG);
        }
        mPageAdapter.notifyDataSetChanged();
        mQueueAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(currentPosition);
        isShuffle = !isShuffle;
    }


    private Runnable UpdateSongTime = new Runnable() {

        @Override
        public void run() {
            actualTime = mediaPlayer.getCurrentPosition();
            seekBar.setProgress((int) actualTime);
            songProgress.setText(msTo(actualTime));
            mHandler.postDelayed(this, 100);

        }

    };

    private Runnable UpdateSong = new Runnable() {
        @Override
        public void run() {
            loadMusic();
        }
    };


    private String msTo(long duration) {
        duration = duration / 1000;
        int seconds = (int) duration % 60;
        duration /= 60;
        int minutes = (int) duration % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }


    private class PagerAdapter extends FragmentStatePagerAdapter {

        public ArrayList<String> mDatatList;
        public HashMap<Integer, player_fragment> cachedFragmentHashMap;

        public PagerAdapter(FragmentManager fm, ArrayList<Song> mList) {
            super(fm);
            cachedFragmentHashMap = new HashMap<>();
            mDatatList = new ArrayList<>();
            for (int ii = 0; ii < mList.size(); ii++) {
                mDatatList.add(mList.get(ii).path);
            }
        }

        public HashMap<Integer, player_fragment> getCachedFragmentHashMap() {
            return cachedFragmentHashMap;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new player_fragment();
            Bundle args = new Bundle();
            args.putString("Path", mDatatList.get(position));
            fragment.setArguments(args);
            cachedFragmentHashMap.put(position, (player_fragment) fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return mDatatList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            cachedFragmentHashMap.remove(position);
        }
    }


    public void initSong() {
        loadHandler.removeCallbacks(UpdateSong);
        loadHandler.postDelayed(UpdateSong, 500);
    }


}
