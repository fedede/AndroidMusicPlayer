package com.exfume.mupl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Benjides on 16/11/2015.
 */
public class player_fragment extends Fragment {

    String songPath;
    ImageView songCover;
    MediaMetadataRetriever metaRetriver;


    public player_fragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_fragment,container,false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            songPath = bundle.getString("Path", null);
            Log.i("Player_Fragment", "Song : "+songPath);
        }
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(songPath);

        songCover = (ImageView) view.findViewById(R.id.mainCover);
        Glide.with(getContext()).load(metaRetriver.getEmbeddedPicture()).placeholder(R.drawable.placeholder).into(songCover);
        return view;

    }

}
