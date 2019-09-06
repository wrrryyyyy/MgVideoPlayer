package com.wrrryyyy.www.mgvideoplayer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_OPEN_BT_CODE = 0;
    private int mPermission=0;
    private PermissionAsyncTask mAT ;
    private boolean hasDialog = false;
    private TextView mTvLoading ;
    private int mAnimationState = 0;
    private int mAnimationStateRandom = 0;
    private final String ss[] = new String[]{"_(:з」∠❁_","_(:з」∠)_","_(:з」∠)","(:з」∠)","(:з∠)","(:з     )","(:з  )","(:  )","(:)","(   )","(  )"};

    private final String changed[] = new String[]{"_(O.O」∠) _","∠( ᐛ 」∠)＿","_(:зゝ∠)_","_(┐「ε:)_","_(·ω·\"∠)_","_(:з」∠❁_"};
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_OPEN_BT_CODE){
            hasDialog = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if(mPermission ==1){
//            finish();
//            return ;
//        }
        if(checkPremissionOnly()){
            Log.d("Main","go oncreat");
            toOthers();
        }else{
            mAT = new PermissionAsyncTask();
            mAT.execute();
        }
        mTvLoading = (TextView)findViewById(R.id.tv_main_loading);
        mAnimationState = ss.length-1;

        mTvLoading.setText(ss[mAnimationState]);
        Animation testAnim = AnimationUtils.loadAnimation(this,R.anim.rollll);
        mTvLoading.setAnimation(testAnim);
        testAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //      animation.setStartOffset(2000);
                if(mAnimationState< 0){
                    mAnimationStateRandom ++;
                    if(mAnimationStateRandom%10==0){
                        int random = (int)(Math.random()*(changed.length-1));
                        mTvLoading.setText(changed[random]);
                //        mTvLoading.setBackgroundColor(Color.BLUE);
                    }else if(mAnimationStateRandom%10==1){
                      //  mTvLoading.setText(ss[1]);
                    }else{
                          mTvLoading.setText(ss[1]);
                    }
                }else{
                    mTvLoading.setText(ss[mAnimationState--]);
                }
                mTvLoading.startAnimation(animation);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void toOthers(){
        mPermission = 1;
        Intent i = new Intent(this, AllActivity.class);
    //    Intent i = new Intent(this, BluetoothActivity.class);
        if(mAT!=null)mAT.stopTask();
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//  /*      if(hasPermission != PackageManager.PERMISSION_GRANTED){
//            Intent i = new Intent(this, VideoPlayerActivity.class);
//            startActivity(i);
//        }*/
//        if(hasPermission == PackageManager.PERMISSION_GRANTED){
////            finish();//主的让他死掉
//            if(mPremission==1){
//                finish();
//            }else{
//                Intent i = new Intent(this, VideoPlayerActivity.class);
//        //        startActivity(i);
//                mPremission = 1;
//            }
//        }
        if(mAT!=null)mAT.restart();
        if(mPermission==1){
            finish();
            Log.d("Main","finish resume");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mAT!=null)mAT.pause();
    }

    /*
        * 检查权限 如果有一个不正确的弹窗 返回false
        * 都过返回true
        * 分个几步 打开所有权限 打开蓝牙 允许被搜索
        *
        * */
    private boolean checkPremissionOnly(){
        String[] premissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,

        };
        int premissionGet = 0;
        boolean allClear = true;
        for(String premission:premissions){
            premissionGet = ActivityCompat.checkSelfPermission(this,premission);
            if(premissionGet!=PackageManager.PERMISSION_GRANTED){
                allClear = false;
            //    ActivityCompat.requestPermissions(this,new String[]{premission},0);
                continue;
            }
        }
        //检查蓝牙

        BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!BTAdapter.isEnabled()){
            allClear = false;
        }else if(BTAdapter.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE){
            allClear = false;
        }

        if(allClear){
            return true;
        }else{
            return false;
        }
    }
    private boolean checkPremission(){
        List<String> premissionList = new ArrayList<>();
        String[] premissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,

        };
        int premissionGet = 0;
        int count = 0;
        boolean allClear = true;
        for(String premission:premissions){
            count++;
            premissionGet = ActivityCompat.checkSelfPermission(this,premission);
            if(premissionGet!=PackageManager.PERMISSION_GRANTED){
                allClear = false;
                ActivityCompat.requestPermissions(this,new String[]{premission},0);
                continue;
            }
        }
        Log.d("eihei","pass"+count);
        if(!allClear)return allClear;

        BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!BTAdapter.isEnabled()){
            allClear = false;
            Log.d("eihei","dead is enable");
            if(hasDialog==false){
                hasDialog = true;
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, REQUEST_OPEN_BT_CODE);
            }
        }/*else if(BTAdapter.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE){
            allClear = false;
            Log.d("eihei","dead scan mode");
            if(hasDialog==false){
                hasDialog = true;
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
                startActivityForResult(i, REQUEST_OPEN_BT_CODE);
            }
        }*/

        if(allClear){
            Log.d("eihei","all clear");
            return true;
        }else{
            return false;
        }
    }
    private class PermissionAsyncTask extends AsyncTask<Void,Void,Void> {
        private boolean running = true;
        private boolean pause = false;
        public void pause(){
            pause = true;
        }
        public void restart(){
            pause  =false;
        }
        @Override
        protected Void doInBackground(Void... params) {

            while(running){
                try {
                    if(!pause){
                        publishProgress();
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        public void stopTask(){
            running = false;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            boolean b = checkPremission();
            if(b){
                stopTask();
                toOthers();
            }else{
            }
            Log.d("eihei","premission"+b);
        }
    }
}
