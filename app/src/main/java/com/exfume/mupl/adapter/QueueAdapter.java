package com.exfume.mupl.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.exfume.mupl.R;
import com.exfume.mupl.interfaces.ItemTouchHelperAdapter;
import com.exfume.mupl.model.Song;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Benjides on 24/11/2015.
 */
public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList<Song> mDataset;


    public QueueAdapter(ArrayList<Song> mDataset) {
        this.mDataset = mDataset;
    }


    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.queue_item_view, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.Title.setText(mDataset.get(position).title);
        viewHolder.Artist.setText(mDataset.get(position).artist);
        viewHolder.Duration.setText(""+mDataset.get(position).duration);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    @Override
    public void onItemDismiss(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataset, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataset, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView Title;
        public TextView Artist;
        public TextView Duration;

        ViewHolder(View itemView) {
            super(itemView);
            Title = (TextView)itemView.findViewById(R.id.queue_SongTitle);
            Artist = (TextView)itemView.findViewById(R.id.queue_Artist);
            Duration = (TextView)itemView.findViewById(R.id.queue_Duration);
        }
    }
}
