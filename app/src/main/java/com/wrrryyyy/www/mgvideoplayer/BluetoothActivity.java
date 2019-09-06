package com.wrrryyyy.www.mgvideoplayer;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BluetoothActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {
    String TAG = "btAct";
    private final String RgetCellValue = "0x([0-9A-Za-z]{2})";
    private final String RgetArray = "[{]([^{]*?)[}]";//"[{][^{]*?[}]";
    private final static int MSG_SENT_DATA = 0;
    private final static int MSG_RECEIVE_DATA = 1;
    private final static int MSG_UPDATE_UI = 2;
    private final static int MSG_START_MUSIC = 3;
    private final static int MSG_STOP_MUSIC = 4;

    private CameraBridgeViewBase mOpenCvCameraView;
    private int mwidth = 400;
    private int mheight = 400;
    private int mframeCount = 0;
    private MenuItem mConnectionMenuItem;
    private ListView mDeviceListView ;
    private ConnectionManager mConnectionManager;
    final  int RESULT_CODE_BTDEVICE =  15;
    final int REQUEST_CODE_SELECT_FILE = 16;
    private EditText mMessageEditor;
    private  Button mSendButton;
    private Button mChangeButton;
    private Button mAutoButton;
    private Button mCameraButton;
    private Button mOledButton;
    private Mat mRgb ;
    private int stage_camera = 0;//0对应后置摄像头 CAMERA_ID_FRONT 反之CAMERA_ID_BACK
    private Mat mCameraFrame ;
    private boolean autoSending = false;
    private boolean mOledInitColor = true;
    private boolean mColorCameraFrameSending = false;
    private MediaPlayer mMediaPlayer;
    private CameraSendTask mCameraSendTask ;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==RESULT_CODE_BTDEVICE&&resultCode ==RESULT_OK){//另一个activity的回答
            String deviceAddr = data.getStringExtra("DEVICE_ADDR");//传回来的地址
            mConnectionManager.connect(deviceAddr);
            Log.d(TAG,"get device success ");
        }else if(requestCode ==REQUEST_CODE_SELECT_FILE&&resultCode ==RESULT_OK){
            Uri uri = data.getData();
            String path = "";
            String[] searchKey = new String[] {
                    MediaStore.Images.Media.DATA
            };
            String [] keywords = null;
//        String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
            ContentResolver resolver = getContentResolver();
            try{
                Cursor cursor = resolver.query(uri, searchKey, null, keywords, null);
                if(cursor!=null) {
//                    int pos = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                    cursor.moveToFirst();
//                    path = cursor.getString(pos);
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
            i.setData(Uri.parse(path));
            startActivity(i);
            Log.d(TAG,"get back success "+path);
        }
        else{
            Log.d(TAG,"get device error ");
        }
    }
    private ConnectionManager.ConnectionListener mConnectionListener = new ConnectionManager.ConnectionListener() {
        @Override
        public void onConnectStateChange(int oldState, int state) {
            mHandler.obtainMessage(MSG_UPDATE_UI).sendToTarget();
        }

        @Override
        public void onListenStateChange(int oldState, int state) {
            mHandler.obtainMessage(MSG_UPDATE_UI).sendToTarget();
        }

        @Override
        public void onSendData(boolean suc, byte[] data) {

            mHandler.obtainMessage(MSG_SENT_DATA, suc?1:0, 0, data).sendToTarget();
        }
        @Override
        public void onReadDate(byte[] data) {
            mHandler.obtainMessage(MSG_RECEIVE_DATA,data).sendToTarget();
        }
    };
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_SENT_DATA:{
               //     Toast.makeText(BluetoothActivity.this,"msg",Toast.LENGTH_LONG).show();
                    mMessageEditor.setText("");
                }
                break;
                case MSG_UPDATE_UI:{
                    updateUI();
                }
                break;
                case MSG_RECEIVE_DATA: {
                    byte[] data = (byte[]) msg.obj;
                    if (data != null) {
                        String str = new String(data);
                        Toast.makeText(BluetoothActivity.this, str, Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"get data ");
                    }
                }
                break;
                case MSG_START_MUSIC: {
                    Log.d(TAG,"start play music ");
                    mMediaPlayer = MediaPlayer.create(BluetoothActivity.this,R.raw.bad_apple_sound_only);
                    mMediaPlayer.start();
                }
                break;
                case MSG_STOP_MUSIC: {
                    mMediaPlayer.stop();
                }
                break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_bluetooth);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDeviceListView = (ListView)findViewById(R.id.connectable_list);
        //opencv init
        staticLoadCVLibraries();
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_find_color);
        assert mOpenCvCameraView != null;
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.enableView();//
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);//前置摄像头 CameraBridgeViewBase.CAMERA_ID_BACK为后置摄像头
        mOpenCvCameraView.setOnTouchListener(BluetoothActivity.this);
        //       mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        mOpenCvCameraView.setMaxFrameSize(mwidth,mheight);

        mConnectionManager = ConnectionManager.getConnectionManager(mConnectionListener);
        if(mConnectionManager.isListening())mConnectionManager.startListen();
        mAutoButton = (Button)findViewById(R.id.auto_btn);
        mChangeButton = (Button)findViewById(R.id.change_btn);
        mCameraButton = (Button)findViewById(R.id.camera_send_btn);
        mOledButton = (Button)findViewById(R.id.oled_init_btn);
        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenCvCameraView.disableView();
                if(BluetoothActivity.this.stage_camera==0){
                    mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                    BluetoothActivity.this.stage_camera=1;
                }else{
                    mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                    BluetoothActivity.this.stage_camera=0;
                }
                mOpenCvCameraView.enableView();
            }
        });
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mColorCameraFrameSending == true){
                    mCameraButton.setText(R.string.send_camera_photo);
                    mColorCameraFrameSending = false;
                    if(mCameraSendTask!=null)mCameraSendTask.onCancelled();
                }else{
                    mCameraButton.setText(R.string.stop_send_camera_photo);
                    mColorCameraFrameSending = true;
                    mCameraSendTask = new CameraSendTask();
                    mCameraSendTask.execute();
                }
            }
        });
        mAutoButton . setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(autoSending ==true) {
                    autoSending = false;
                    updateUI();
                }else{
                    autoSending = true;
                    updateUI();
                    AutoSendTask ast = new AutoSendTask();
                    ast.execute();
                }

               // Toast.makeText(BluetoothActivity.this, cells[0]+" "+cells[1]+" "+cells.length+" "+frameArray.length, Toast.LENGTH_SHORT).show();
            }
        });
        mOledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOledInitColor==true){
                    mOledButton.setText(R.string.switch_oled_mono);
                    mOledInitColor = false;
                    char c1 = 38,c2=36;
                    String s[] = new String[2];
                    s[0] = c1+"";
                    s[1] = c2+"";
                    sendMessage(s);
                    Log.d(TAG,"Init oled color");

                }else{
                    mOledButton.setText(R.string.switch_oled_color);
                    mOledInitColor = true;
                    char c1 = 37,c2=36;
                    String s[] = new String[2];
                    s[0] = c1+"";
                    s[1] = c2+"";
                    sendMessage(s);
                    Log.d(TAG,"Init oled mono");
                }
            }
        });
        mSendButton = (Button)findViewById(R.id.send_btn);
        mMessageEditor = (EditText)findViewById(R.id.msg_editor);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
//                Intent intent = new Intent(BluetoothActivity.this,PhotoActivity.class);
//                startActivity(intent);
            }
        });
        mMessageEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND){
                    sendMessage();
                }
                return true;
            }
        });


        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(BluetoothActivity.this,R.raw.bad_apple_sound_only);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG,"music play end");
            }
        });
    //    mHandler.obtainMessage(MSG_START_MUSIC).sendToTarget();
     //   mMediaPlayer.start();
     //   mMediaPlayer.pause();

        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_UPDATE_UI);
        mHandler.removeMessages(MSG_SENT_DATA);
        mHandler.removeMessages(MSG_RECEIVE_DATA);
        if(mConnectionManager !=null){
//            mConnectionManager.disconnect();
//            mConnectionManager.stopListen();
        }
        if(mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if(mwidth==400||mheight==400){
            mwidth = height;
            mheight = width;
        }
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgb = inputFrame.rgba();
        if(mRgb==null)return null;
        if(stage_camera == 0){
            Core.flip(mRgb, mRgb, 1);//0左右1上下-1上下左右
        }else{
        }
        mCameraFrame = mRgb;
        return mRgb;
    }
    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i(TAG, "Open CV Libraries loaded...");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.connected_menu,menu);
        mConnectionMenuItem = menu.findItem(R.id.connect_menu);
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
                    Intent i = new Intent(BluetoothActivity.this,ConnectActivity.class);
                    startActivityForResult(i,RESULT_CODE_BTDEVICE);
                }
            }
            return true;
            case R.id.about_menu:{
                Intent i = new Intent(BluetoothActivity.this,AboutActivity.class);
                startActivity(i);
            }
            return true;
            case R.id.oled_menu:{
                if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_CONNECTED){
                    Intent i = new Intent(BluetoothActivity.this,PhotoActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(BluetoothActivity.this,"请先连接蓝牙",Toast.LENGTH_SHORT).show();
                }
            }
            return true;
            case R.id.remote_menu:{
//                if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_CONNECTED){
                    Intent i = new Intent(BluetoothActivity.this,RemoteActivity.class);
                    startActivity(i);
//                }else{
//                    Toast.makeText(BluetoothActivity.this,"请先连接蓝牙",Toast.LENGTH_SHORT).show();
//                }
            }
            return true;
            case R.id.uart_menu:{
                if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_CONNECTED){
                    Intent i = new Intent(BluetoothActivity.this,UartActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(BluetoothActivity.this,"请先连接蓝牙",Toast.LENGTH_SHORT).show();
                }
            }
            return true;
            case R.id.all_menu:{
                Intent i = new Intent(BluetoothActivity.this,AllActivity.class);
                startActivity(i);
            }
            return true;
            default: return false;
        }
    }
    private void sendMessage(){
        String text = mMessageEditor.getText().toString();
        if(text!=null){
            text.trim();
            if(text.length()>0){
                boolean ans = mConnectionManager.sendData(text.getBytes());
                if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean sendMessage(byte [] message) {
        if(message == null){
            Log.d(TAG,"error:send message null");
            return false;
        }

        boolean ans = mConnectionManager.sendData(message);
        //  if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
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
         //   boolean ans = mConnectionManager.sendData(message[i].getBytes());
        }

        boolean ans = mConnectionManager.sendData(s.getBytes());
      //  if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
        if(!ans)Log.d(TAG,"send message error");
        return ans;
    }
    private void updateUI() {
        Log.d(TAG, "update UI");

        if(mConnectionManager ==null||mConnectionMenuItem ==null){
            mMessageEditor.setEnabled(false);
            mSendButton.setEnabled(false);
            mOledButton.setEnabled(false);
            mAutoButton.setEnabled(false);
            mCameraButton.setEnabled(false);
            mChangeButton.setEnabled(true);
            return;
        }
        Log.d(TAG, "current BT ConnectState="+mConnectionManager.getState(mConnectionManager.getCurrentConnectState())
                +" ListenState="+mConnectionManager.getState(mConnectionManager.getCurrentListenState()));
        if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTED){
            mConnectionMenuItem.setTitle(R.string.disconnect);
            mMessageEditor.setEnabled(true);
            mSendButton.setEnabled(true);
            mOledButton.setEnabled(true);
            mAutoButton.setEnabled(true);
            mCameraButton.setEnabled(true);
            mChangeButton.setEnabled(false);

        }else if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTING){
            mConnectionMenuItem.setTitle(R.string.cancel);
            mMessageEditor.setEnabled(false);
            mSendButton.setEnabled(false);
            mOledButton.setEnabled(false);
            mAutoButton.setEnabled(false);
            mCameraButton.setEnabled(false);
            mChangeButton.setEnabled(true);
        }else if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_IDLE){
            mConnectionMenuItem.setTitle(R.string.connect);
            mMessageEditor.setEnabled(false);
            mSendButton.setEnabled(false);
            mOledButton.setEnabled(false);
            mAutoButton.setEnabled(false);
            mCameraButton.setEnabled(false);
            mChangeButton.setEnabled(true);
        }
        if(autoSending == true) {
            mAutoButton.setText("停止发送");
            mMessageEditor.setEnabled(false);
            mSendButton.setEnabled(false);
            mOledButton.setEnabled(false);
            mCameraButton.setEnabled(false);
        }else {
            mAutoButton.setText("自动发送");
        }
    }



    private class AutoSendTask extends AsyncTask<Object,String,Void> {
        @Override
        protected Void doInBackground(Object... params) {
            long sendtime = System.currentTimeMillis();
            int frameRate = 30;
            publishProgress(getString(R.string.read_file));
            InputStream inputStream = getResources().openRawResource(R.raw.bad_apple);
            String str =  VideoDecoder.getStringByIS(inputStream,0);
            mframeCount+=5;
            //       Log.d(TAG,"emm "+str);
            Log.d("btA","start autoSend");
            String frameArray[] = VideoDecoder.getStringTwice(str,RgetArray);
            Log.d("btA","finish array search");
            String cells[] =null;
            publishProgress(getString(R.string.start_send));
            mHandler.obtainMessage(MSG_START_MUSIC).sendToTarget();
            long showTimeOut = 0;
            if(frameArray!=null){
                int count = 0;
                int overCount = 0;
                long sendStartTime =  System.currentTimeMillis();
                while(count<frameArray.length&&autoSending==true){
                    int sendFrame = (int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000;
                    //sendFrame = (sendFrame<frameArray.length?sendFrame:frameArray.length-1);
                    if(sendFrame>=frameArray.length)break;
                    //   cells =new String[frameArray.length];
                    //cells = getStringTwice(frameArray[mframeCount<frameArray.length?mframeCount:(frameArray.length-1)],RgetCellValue);//nullable 而且会跳掉
                    cells =  VideoDecoder.getStringTwice(frameArray[count],RgetCellValue);
                    Log.d("btA","finish cell search");
                    char c1 = 35,c2=36;
                    String s[] = new String[2];
                    s[0] = c1+"";
                    s[1] = c2+"";
                    sendMessage(s);

                    Log.d("btA","send head over");
                    byte b[] = new byte[cells.length];
                    for(int i=0;i<cells.length;i++){
                        Integer it = Integer.valueOf(cells[i],16);
                        b[i] = (byte)it.intValue();
                    }
                    //     sendMessage(cells);
                    boolean bo =sendMessage(b);
                    if(bo)publishProgress("发送不出去");
                    Log.d("btA",count+"/"+frameArray.length+"send  over once");
                    //       Log.d(TAG,"send over "+(System.currentTimeMillis()-time));
                    count++;
                    while(count>(int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000){
                        Log.d("WAIT","wait");
                    }
                    if(count<(int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000){
                        count++;
                        overCount++;
                        publishProgress("漏帧："+overCount);
                        //count = (int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000;
                    }
                }
                showTimeOut = System.currentTimeMillis()-sendStartTime;
            }
            Log.d("btA","send over"+(System.currentTimeMillis()-sendtime));
            mHandler.obtainMessage(MSG_UPDATE_UI).sendToTarget();
            mHandler.obtainMessage(MSG_STOP_MUSIC).sendToTarget();
            publishProgress(getString(R.string.finish_send)+showTimeOut);

            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG,values[0]);
            //  if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
            Toast.makeText(BluetoothActivity.this,values[0],Toast.LENGTH_SHORT).show();
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

        }
    }
    private class CameraSendTask extends AsyncTask<Object,String,Void> {
        private boolean taskSendable = true;
        long mlastSendTime = 0;
        @Override
        protected Void doInBackground(Object... params) {
            Log.d(TAG,"start task");
            mlastSendTime = System.currentTimeMillis();
            while(/*btCount< vv.size()&&*/taskSendable) {
                byte bb[] = new byte[2];
                Integer it = Integer.valueOf("01", 16);
                bb[0] = (byte) it.intValue();
                it = Integer.valueOf("FE", 16);
                bb[1] = (byte) it.intValue();
                sendMessage(bb);
                Vector<Integer>v = new Vector<Integer>();
                v =VideoDecoder.decodeColor(mCameraFrame,new Size(80,60),null);
                int vvSize = v.size();
                byte b[] = new byte[vvSize];
                for (int i = 0; i < vvSize; i++) {
                    b[i] = (byte) v.get(i).intValue();
                }
                boolean ans = sendMessage(b);
                if (!ans) {
                    taskSendable = false;
                }
                it = Integer.valueOf("FE", 16);
                bb[0] = (byte) it.intValue();
                it = Integer.valueOf("01", 16);
                bb[1] = (byte) it.intValue();
                sendMessage(bb);
                while(mlastSendTime+1000>System.currentTimeMillis()){
                    Log.d("WAIT","wait photo send");
                }
                mlastSendTime = System.currentTimeMillis();
            }


            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG,values[0]);
            //  if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
            Toast.makeText(BluetoothActivity.this,values[0],Toast.LENGTH_SHORT).show();
        }
        protected void onPostExecute(Void result) {
            Log.d("main","Task finished");
            updateResult();
        }
        protected void onCancelled(){
            taskSendable = false;
            Log.d("main","Task cancelled");
            updateResult();
        }
        private void updateResult(){

        }
    }
}
