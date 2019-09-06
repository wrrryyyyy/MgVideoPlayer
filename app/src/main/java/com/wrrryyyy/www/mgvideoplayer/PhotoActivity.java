package com.wrrryyyy.www.mgvideoplayer;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Size;

import java.io.File;
import java.util.Vector;

public class PhotoActivity extends AppCompatActivity {

    final int REQUEST_CODE_BTDEVICE = 15;
    final int REQUEST_CODE_SELECT_FILE = 16;
    final int REQUEST_CODE_SELECT_FILE_COLOR = 17;
    final int TASK_COLOR = 1;
    final int TASK_MONO = 0;
    final String TAG = "PActivity";
    private Button mKiraButton;
    private Button mAddButton;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private MediaPlayer mMediaPlayer;
    private MediaMetadataRetriever mMedia;
    private ConnectionManager mConnectionManager;
    private MenuItem mConnectionMenuItem;
    private int frameCount = 0;
    private TextView mTv ;
    private int mAutoSendMode = 0;
    private VideoSendTask mVideoSendTask ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        mKiraButton = (Button)findViewById(R.id.kira_btn);
        mAddButton = (Button)findViewById(R.id.addup_btn);
        mImageView = (ImageView)findViewById(R.id.iv_photo);
        mTv = (TextView)findViewById(R.id.tv_decode);
        mConnectionManager = ConnectionManager.getConnectionManager(null);
        mMediaPlayer = MediaPlayer.create(PhotoActivity.this,R.raw.bad_apple_sound_only);
        mKiraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File("//storage//emulated//0//DCIM//");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");//"video/*"
                intent.setType("video/*");//"video/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if(null==f || !f.exists()){
                }else{
              //      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //    intent.setDataAndType(Uri.fromFile(f), "file/*");
                }
                startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
            }
        });
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File("//storage//emulated//0//DCIM//");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");//"video/*"
                intent.setType("video/*");//"video/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_SELECT_FILE_COLOR);
//                byte bb[] = new byte[2];
//                Integer it = Integer.valueOf("01",16);
//                bb[0] =(byte)it.intValue();
//                it = Integer.valueOf("FE",16);
//                bb[1] =(byte)it.intValue();
//                sendMessage(bb);
//
//                byte b[] = new byte[80*60*2];
//                for(int i = 0;i<80;i++){
//                    for(int j=0;j<60;j++){
//                        if(j%2==0){
//                            b[i*60*2+j*2] = 0;
//                            b[i*60*2+j*2+1] = 0;
//                        }else{
//                            b[i*60*2+j*2] = -127;
//                            b[i*60*2+j*2+1] = 1;
//                        }
//                    }
//                }
//                boolean ans =sendMessage(b);
//
//                it = Integer.valueOf("FE",16);
//                bb[0] =(byte)it.intValue();
//                it = Integer.valueOf("01",16);
//                bb[1] =(byte)it.intValue();
//                sendMessage(bb);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        if(mVideoSendTask!=null){
            mVideoSendTask.onCancelled();
        }
    }

    private final int MSG_CHANGE_IMG = 0;
    private final int MSG_CHANGE_UI = 1;
    private final int MSG_START_MUSIC = 2;
    private final int MSG_UPDATE_TV = 3;
    private final int MSG_STOP_MUSIC = 4;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_CHANGE_IMG:{
                    if(mBitmap!=null){
                        mImageView.setImageBitmap(mBitmap);
                    }
                    break;
                }
                case MSG_CHANGE_UI:{

                    break;
                }
                case MSG_START_MUSIC:{
                    Log.d(TAG,"start play music ");
                    mMediaPlayer.start();
                    break;
                }
                case MSG_UPDATE_TV:{
                    String str = (String) msg.obj;
                    mTv.setText(str);
                    break;
                }
                case MSG_STOP_MUSIC:{
                    mMediaPlayer.stop();
                    break;
                }
            }
        }
    };
    private void sendMessage(){
        String text = "123321";
        if(text!=null){
            text.trim();
            if(text.length()>0){
                boolean ans = mConnectionManager.sendData(text.getBytes());
            }
        }
    }
    private boolean sendMessage(byte [] message) {
        if(message == null){
            Log.d(TAG,"error:send message null");
            return false;
        }
        boolean ans = mConnectionManager.sendData(message);
        if(!ans)Log.d(TAG,"send message error");
        return ans;
    }
    private boolean sendMessage(String[] message){
        if(message == null){
            Log.d(TAG,"error:send message null");
            return false;
        }
        String s = "";
        for(int i=0;i<message.length;i++) {
            s = s+message[i];
        }
        boolean ans = mConnectionManager.sendData(s.getBytes());
        if(!ans)Log.d(TAG,"send message error");
        return ans;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==REQUEST_CODE_BTDEVICE&&resultCode ==RESULT_OK){//另一个activity的回答
            String deviceAddr = data.getStringExtra("DEVICE_ADDR");//传回来的地址
            mConnectionManager.connect(deviceAddr);
            Log.d(TAG,"get device success ");
        }else if((requestCode ==REQUEST_CODE_SELECT_FILE||requestCode ==REQUEST_CODE_SELECT_FILE_COLOR)&&resultCode ==RESULT_OK){
            Uri uri = data.getData();
            String path = "";
            String[] searchKey = new String[] {
                    MediaStore.Images.Media.DATA
            };
            String [] keywords = null;
            ContentResolver resolver = getContentResolver();
            try{
                Cursor cursor = resolver.query(uri, searchKey, null, keywords, null);
                if(cursor!=null) {
                    while(cursor.moveToNext()){
                        path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        Log.d(TAG,"get cursor "+path );
                    }
                    cursor.close();
                }
            }catch(Exception e) {
                Log.d(TAG,"error,need strog power");
            }
            Intent i = new Intent(this,MovicPlayerActivity.class);
            if(path!=null){
                i.setData(Uri.parse(path));
                //     startActivity(i);
                MediaMetadataRetriever media = new MediaMetadataRetriever();
                media.setDataSource(path);
                mMedia = media;
                mBitmap = media.getFrameAtTime(200,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                mImageView.setImageBitmap(mBitmap);

                VideoSendTask vst = new VideoSendTask();
                if(mVideoSendTask!=null){
                    mVideoSendTask.onCancelled();
                }
                mVideoSendTask = vst;
                if(requestCode ==REQUEST_CODE_SELECT_FILE){
                    vst.execute(TASK_MONO);
                }else{
                    vst.execute(TASK_COLOR);
                }
                Log.d(TAG,"get back success "+path);
            }else{
                Log.d(TAG,"get back error:get null pointer "+path);
            }
        }
        else{
            Log.d(TAG,"get device error ");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.connected_menu,menu);
        mConnectionMenuItem = menu.findItem(R.id.connect_menu);
        updateUI();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.connect_menu:{
                if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_CONNECTED){
                    mConnectionManager.disconnect();
                }else if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_CONNECTING){
                    mConnectionManager.disconnect();
                }else if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_IDLE){
                    Intent i = new Intent(PhotoActivity.this,ConnectActivity.class);
                    startActivityForResult(i,REQUEST_CODE_BTDEVICE);
                }
                updateUI();
            }
            return true;
            case R.id.about_menu:{
                Intent i = new Intent(PhotoActivity.this,AboutActivity.class);
                startActivity(i);
            }
            return true;
            default: return false;
        }
    }
    private void updateUI() {
        Log.d(TAG, "update UI");
        if(mConnectionManager==null)return;
        if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTED){
            mConnectionMenuItem.setTitle(R.string.disconnect);
            mAddButton.setEnabled(true);
            mKiraButton.setEnabled(true);
        }else if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTING){
            mConnectionMenuItem.setTitle(R.string.cancel);
            mAddButton.setEnabled(false);
            mKiraButton.setEnabled(false);
        }else if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_IDLE){
            mConnectionMenuItem.setTitle(R.string.connect);
            mAddButton.setEnabled(false);
            mKiraButton.setEnabled(false);
        }
    }
    private class VideoSendTask extends AsyncTask<Object,String,Void> {
        boolean taskSendable = true;
        @Override
        protected Void doInBackground(Object... params) {
            int sendMode = (int)params[0];
            Log.d(TAG,"start task"+sendMode);
            if(mMedia==null)return null;
            Log.d(TAG,"start task");
            String durationS = mMedia. extractMetadata( MediaMetadataRetriever. METADATA_KEY_DURATION);
            int duration =0;
            int frameRate = 30;
            try{
                duration =Integer.parseInt(durationS);
            }catch (Exception e){
                Log.d(TAG,"duration change error");
                duration = 0;
            }
            long time = 0;
            Bitmap bitmap = null;
            int count = 0;
            Vector<Vector<Integer>> vv = new Vector<Vector<Integer>>();
            if(sendMode ==TASK_MONO){
                while(time/1000</*3*1000*/duration&&taskSendable){//
                    bitmap = mMedia.getFrameAtTime(time,MediaMetadataRetriever.OPTION_CLOSEST);
                    Vector<Integer>v = new Vector<Integer>();
                    v = VideoDecoder.decodeBinary4Oled(bitmap,new Size(96,64),mBitmap);
                    String s=""+(time/100000)+"/"+(duration/100);
                    mHandler.obtainMessage(MSG_UPDATE_TV,s).sendToTarget();
                    if((count++)%2==1){
                        mHandler.obtainMessage(MSG_CHANGE_IMG).sendToTarget();
                    }
                    vv.add(v);
                    time +=(1000000/frameRate);//us单位 30帧
                }
            }else if(sendMode ==TASK_COLOR){
                while(time/1000</*1*1000*/duration&&taskSendable){
                    //while(time/1000<1*1000/*duration*/&&taskSendable){//
                    bitmap = mMedia.getFrameAtTime(time,MediaMetadataRetriever.OPTION_CLOSEST);
                    Vector<Integer>v = new Vector<Integer>();
                    v = VideoDecoder.decodeColor(bitmap,new Size(80,60),mBitmap);
                    String s=""+(time/100000)+"/"+(duration/100);
                    mHandler.obtainMessage(MSG_UPDATE_TV,s).sendToTarget();
                    if((count++)%2==1){
                        mHandler.obtainMessage(MSG_CHANGE_IMG).sendToTarget();
                    }
                    vv.add(v);
                    time +=(1000000/frameRate);//us单位 30帧
                }
            }
         //   mImageView.setImageBitmap(mBitmap);
            Log.d(TAG,"add "+frameCount);
//            mHandler.obtainMessage(MSG_UPDATE_TV,"kuso").sendToTarget();
            while(taskSendable){
                long sendtime = System.currentTimeMillis();
                publishProgress(getString(R.string.start_send));
                if(vv!=null&&vv.size()!=0&&vv.get(0)!=null&&vv.get(0).size()!=0){
                    int btCount = 0;
                    mHandler.obtainMessage(MSG_START_MUSIC).sendToTarget();
                    long sendStartTime =  System.currentTimeMillis();
                    while(/*btCount< vv.size()&&*/taskSendable){
                        int sendFrame = (int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000;
                        if(sendFrame>=vv.size())break;
                        Log.d("btA","finish cell search");
                        char c1 = 35,c2=36;
                        if(sendMode ==TASK_MONO){
                            c1 = 35;c2 = 35;
                            String s[] = new String[2];
                            s[0] = c1+"";
                            s[1] = c2+"";
                            sendMessage(s);
                        }else if(sendMode==TASK_COLOR){
                            byte bb[] = new byte[2];
                            Integer it = Integer.valueOf("01",16);
                            bb[0] =(byte)it.intValue();
                            it = Integer.valueOf("FE",16);
                            bb[1] =(byte)it.intValue();
                            sendMessage(bb);
                        }
                   //     int vvSize = vv.get(sendFrame).size();
                        int vvSize = vv.get(btCount).size();
                        byte b[] = new byte[vvSize];
                        for(int i=0;i<vvSize;i++){
                            b[i] = (byte)vv.get(sendFrame).get(i).intValue();
                        }
                        boolean ans =sendMessage(b);
                        if(!ans){
                            taskSendable = false;
                        }
                        if(sendMode==TASK_COLOR){
                            byte bb[] = new byte[2];
                            Integer it = Integer.valueOf("FE",16);
                            bb[0] =(byte)it.intValue();
                            it = Integer.valueOf("01",16);
                            bb[1] =(byte)it.intValue();
                            sendMessage(bb);
                        }
                        Log.d("btA",btCount+"/"+vv.size()+"send  over once");
                        btCount++;
                        while(btCount>sendFrame){
                            Log.d("WAIT","wait");
                        }
                        if(btCount<sendFrame){
                            btCount = (sendFrame-btCount)/2;
                        }
                    }
                    mHandler.obtainMessage(MSG_STOP_MUSIC).sendToTarget();

                }
                Log.d("btA","send over"+(System.currentTimeMillis()-sendtime));
        //        break;
            }

         //   mHandler.obtainMessage(MSG_UPDATE_UI).sendToTarget();
            publishProgress(getString(R.string.finish_send));

            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG,values[0]);
            //  if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
            Toast.makeText(PhotoActivity.this,values[0],Toast.LENGTH_SHORT).show();
        }
        protected void onPostExecute(Void result) {
            Log.d("main","Task finished");
            updateResult();
        }
        protected void onCancelled(){
            Log.d("main","Task cancelled");
            taskSendable = false;
            updateResult();
        }
        private void updateResult(){

        }
    }
}
