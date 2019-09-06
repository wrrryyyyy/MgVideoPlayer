package com.wrrryyyy.www.mgvideoplayer;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private ListView mVideoListView;
    private List<VideoItem> mVideoList;
    private VideoAdapter mVideoAdapter;
    private AsyncTask mVideoUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player_layout);
        mVideoListView = (ListView) findViewById(R.id.list_view);
        mVideoList = new ArrayList<VideoItem>();
        mVideoAdapter = new VideoAdapter(this,R.layout.video_item,mVideoList);
        mVideoListView.setAdapter(mVideoAdapter);
        mVideoListView.setOnItemClickListener(this);
        mVideoUpdateTask = new VideoUpdateTask();
        mVideoUpdateTask.execute();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VideoItem item = mVideoList.get(position);
        Intent i = new Intent(this,MovicPlayerActivity.class);
        i.setData(Uri.parse(item.path));
        startActivity(i);
    }
private class VideoUpdateTask extends  AsyncTask<Object,VideoItem,Void>{
    List<VideoItem>mDataList = new ArrayList<VideoItem>();
    @Override
    protected Void doInBackground(Object... params) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] searchKey = new String[] {
                MediaStore.Video.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String where = MediaStore.Video.Media.DATA + " like \"%"+""+"%\"";
        String [] keywords = null;
//        String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
        String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

        ContentResolver resolver = getContentResolver();
        try{
            Cursor cursor = resolver.query(uri, searchKey, where, keywords, sortOrder);

            if(cursor!=null) {
                int count = cursor.getCount();
                while(cursor.moveToNext()&&!isCancelled()){
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    VideoItem item = new VideoItem(path,name,createdTime);
                    if(mVideoList.contains(item)==false) {
                        item.creatThumb();
                        publishProgress(item);
                    }
                    mDataList.add(item);
                }
                cursor.close();
            }
        }catch(Exception e) {
         Log.d("main","error,need strog power");
        }

        return null;
    }
    @Override
    protected void onProgressUpdate(VideoItem... values) {
        VideoItem data = values[0];
        mVideoList.add(data);
        VideoAdapter adapter = (VideoAdapter) mVideoListView.getAdapter();
        adapter.notifyDataSetChanged();
    }
    protected void onPostExecute(Void result) {
        Log.d("main","Task finished");
        updateResult();
    }
    protected void onCancelled(){
        Log.d("main","Task cancelled");
        updateResult();
    }
    private void updateResult(){
        for(int i=0;i<mVideoList.size();i++){
            if(!mDataList.contains(mVideoList.get(i))){
                mVideoList.get(i).releaseThumb();
                mVideoList.remove(i);
                i--;
            }
        }
        mDataList.clear();
        VideoAdapter adapter =(VideoAdapter) mVideoListView.getAdapter();
        adapter.notifyDataSetChanged();

    }
}
}
