package com.wrrryyyy.www.mgvideoplayer;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class MovicPlayerActivity extends AppCompatActivity {
    VideoView mVideoView;
    private int mLastPlayedTime ;
    private int mVideoWidth = 1;
    private int mVideoHeight = 1;
    private final String LAST_PLAYED_TIME = "LAST_TIME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        String path=uri.getPath();
        if(path ==null) {
            finish();
            Log.d("main","error path null");
        }
        setContentView(R.layout.activity_movic_player);
        if(this.getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();;
        }
        mVideoView = (VideoView) findViewById(R.id.movic_view);
        mVideoView.setVideoPath(path);
        MediaController controller = new MediaController(this);
        mVideoView.setMediaController(controller);
       /* mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                float scale = (float)mVideoWidth/(float)mVideoHeight;
                refreshPortraitScreen(mVideoWidth);
            }
        });*/
        mVideoView.start();
    }
    public void refreshPortraitScreen(int width){

    }
    static int countA = 0;
    static int countB = 0;
    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
        mLastPlayedTime = mVideoView.getCurrentPosition();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            int time = mVideoView.getCurrentPosition();
//            Log.d("main","output"+mVideoView.getDuration());
//            if(time+3000<mVideoView.getDuration()){
//                mVideoView.seekTo(mVideoView.getDuration());
//            }else{
                mVideoView.seekTo(mVideoView.getCurrentPosition()+3000);
//            }
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            int time = mVideoView.getCurrentPosition();
            Log.d("main","output"+mVideoView.getDuration());
            if(time<3000){
                mVideoView.seekTo(0);
                mVideoView.start();
            }else{
                mVideoView.seekTo(mVideoView.getCurrentPosition()-3000);
                mVideoView.start();
            }
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        countB++;
        Log.d("main","output:keyLongDown"+countB);

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
        if(mLastPlayedTime>0){
            mVideoView.seekTo(mLastPlayedTime);
        }
    }
    @Override
    protected  void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_PLAYED_TIME,mVideoView.getCurrentPosition());
    }
    @Override
    protected  void onRestoreInstanceState(Bundle saveInstanceState){
        super.onRestoreInstanceState(saveInstanceState);
        mLastPlayedTime = saveInstanceState.getInt(LAST_PLAYED_TIME);
    }


}
