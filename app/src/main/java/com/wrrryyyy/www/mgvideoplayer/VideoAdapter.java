package com.wrrryyyy.www.mgvideoplayer;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by aa on 2018/8/4.
 */

public class VideoAdapter extends ArrayAdapter<VideoItem> {
    private final LayoutInflater mInflater;
    private final int mResource;
    public VideoAdapter(Context context, int resource,List<VideoItem> object) {
        super(context, resource,object);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }
    public View getView(int position,View convertView,ViewGroup parent){
        if(convertView ==null) {
            convertView = mInflater.inflate(mResource,parent,false);
        }
        VideoItem item = getItem(position);
        TextView title = (TextView)convertView.findViewById(R.id.video_title);
        title.setText(item.name);
        TextView createdTime = (TextView)convertView.findViewById(R.id.video_data);
        createdTime.setText(item.createdTime);
        ImageView thumb = (ImageView)convertView.findViewById(R.id.video_thumb);
        thumb.setImageBitmap(item.thumb);
        return convertView;
    }
}
