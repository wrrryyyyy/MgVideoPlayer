package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Size;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by aa on 2018/10/7.
 */

public class EmojiFragment extends BaseFragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "EmojiFragment";
    private Activity mActivity;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public static final int REQUEST_CODE_SELECT_FILE = 16;
    public static final int REQUEST_CODE_SELECT_FILE_COLOR = 17;
    final int TASK_COLOR = 1;
    final int TASK_BINARY = 0;
    final int TASK_BITMAP_COLOR = 2;
    final int TASK_BITMAP_BINARY = 3;
    private Button mBtnSend;
    private Button mBtnOled;
    private Button mBtnCopy;
    private Button mBtnOpenFile;
    private TextView mTvFile;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private MediaPlayer mMediaPlayer;
    private MediaMetadataRetriever mMedia;
    private Bitmap mBitmapSend;
    private ConnectionManager mConnectionManager;
    private int frameCount = 0;
    private TextView mTv ;
    private int mAutoSendMode = 0;
    private VideoSendTask mVideoSendTask ;


    //------------没啥用的变量---------------
    private final int SEND_MODE_SEND = 0;
    private final int SEND_MODE_CANCLE = 1;
    private final int OLED_MODE_BINARY = 0;
    private final int OLED_MODE_COLOR = 1;
    private int  mSendMode =SEND_MODE_SEND;
    private int mOledMode = OLED_MODE_BINARY;
    private Vector<Integer> mLastFrameDataVector;

    public EmojiFragment() {
        // Required empty public constructor
    }

    public static EmojiFragment newInstance(String param1, String param2) {
        return newInstance(param1,param2,null);
    }
    public static EmojiFragment newInstance() {
        return newInstance(null);
    }
    public static EmojiFragment newInstance(int lineNum) {
        return newInstance(null);
    }
    public static EmojiFragment newInstance(List<List<PointValue>>list){
        return newInstance("","",list);
    }

    public static EmojiFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
        EmojiFragment fragment = new EmojiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.view_construction, container, false);
        View view = inflater.inflate(R.layout.fragment_emoji, container, false);
        mConnectionManager = ConnectionManager.getConnectionManager(null);
        initUI(view);

        return view;
    }
    private void initUI(View v){
        mTvFile = (TextView)v.findViewById(R.id.tv_emoji_file_name);
        mTv = (TextView)v.findViewById(R.id.tv_emoji_decode);
        mImageView = (ImageView)v.findViewById(R.id.iv_emoji_photo);
        mBtnSend = (Button)v.findViewById(R.id.btn_emoji_send);
        mBtnOled = (Button)v.findViewById(R.id.btn_emoji_oled_mode);
        mBtnCopy = (Button)v.findViewById(R.id.btn_emoji_text);
        mBtnOpenFile = (Button)v.findViewById(R.id.btn_emoji_file);
        mBtnSend.setOnClickListener(mButtonListener);
        mBtnOled.setOnClickListener(mButtonListener);
        mBtnCopy.setOnClickListener(mButtonListener);
        mBtnOpenFile.setOnClickListener(mButtonListener);
        updateUI();
    }
    View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_emoji_send:{
                    if(mSendMode ==SEND_MODE_CANCLE){
                        if(mVideoSendTask!=null)mVideoSendTask.onCancelled();
                        mSendMode = SEND_MODE_SEND;
                        updateUI();
                    }else{

                    }
                    break;
                }
                case R.id.btn_emoji_oled_mode:{
                    if(mOledMode ==OLED_MODE_COLOR){
                        mOledMode = OLED_MODE_BINARY;
                        String[] s = getSendingMarkByChar((char)37,(char)36);
                        sendMessage(s);
                    }
                    else{
                        mOledMode = OLED_MODE_COLOR;
                        String[] s = getSendingMarkByChar((char)38,(char)36);
                        sendMessage(s);
                    }
                    updateUI();
                    break;
                }
                case R.id.btn_emoji_text:{
                    //String get = getDecodingImage();
                    String get = data2String();
                    ClipboardManager clipboard = (ClipboardManager)mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText(null,get);
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(mActivity,"已粘贴到剪切板",Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.btn_emoji_file:{
//                    Intent i = new Intent(mActivity,);
                    File f= new File("/");
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    Uri uri = FileProvider.getUriForFile(mActivity,"org.diql.fileprovider",f);
//                    intent.setType("*/*");
          //          intent.setDataAndType(Uri.fromFile(f),"*/*");
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    mActivity.startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
                    break;
                }
            }
        }
    };

    @NonNull
    public static String[] getSendingMarkByChar(char a,char b) {
        String s[] = new String[2];
        s[0] = a+"";
        s[1] = b+"";
        return s;
    }

    private String data2String(){
        String def = "kira";
        if(mLastFrameDataVector==null||mLastFrameDataVector.size()==0){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(
                "byte frame[][768] = {");
        stringBuilder .append("\n"+"{");
        int count = 0;
        for(Integer i:mLastFrameDataVector){
            String s ="0x"+ String.format("%02x", i)+",";
            stringBuilder.append(s);
            count++;
            if(count%5==0){
                count = 0;
                stringBuilder.append("\n");
            }
        }
        stringBuilder .append("}");
        stringBuilder .append("\n"+"}");
        def = stringBuilder.toString();
        return def;
    }
    @Override
    void loadData() {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
        mActivity = (Activity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


//---------------------------ui-------------------------------------------------------------------
    private void updateUI() {
        Log.d(TAG, "update UI");

        if(mConnectionManager ==null){
            mBtnOpenFile.setEnabled(false);
//            mBtnOpenFile.setBackgroundResource(R.color.colorDarkYellow);
            mBtnSend.setEnabled(false);
//            mBtnSend.setBackgroundResource(R.color.colorDarkYellow);
            mBtnOled.setEnabled(false);
//            mBtnOled.setBackgroundResource(R.color.colorThemeYellow);
            mBtnCopy.setEnabled(false);
//            mBtnCopy.setBackgroundResource(R.color.colorDarkYellow);
            mBtnOpenFile.setTextColor(Color.BLACK);
            mBtnSend.setTextColor(Color.BLACK);
            mBtnOled.setTextColor(Color.BLACK);
            mBtnCopy.setTextColor(Color.BLACK);
            return;
        }
        if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTED){
            mBtnOpenFile.setEnabled(true);
            mBtnSend.setEnabled(true);
            mBtnOpenFile.setTextColor(Color.WHITE);
            mBtnSend.setTextColor(Color.WHITE);

        }else if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTING||
                mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_IDLE){
            mBtnOpenFile.setEnabled(false);
            mBtnSend.setEnabled(false);
            mBtnOpenFile.setTextColor(Color.BLACK);
            mBtnSend.setTextColor(Color.BLACK);
        }
        if(mSendMode == SEND_MODE_CANCLE) {
            mBtnSend.setText(getString(R.string.cancel));//发送中都不能按
            mBtnOpenFile.setEnabled(false);
            mBtnOled.setEnabled(false);
            mBtnCopy.setEnabled(false);
            mBtnSend.setEnabled(true);
            mBtnOpenFile.setTextColor(Color.BLACK);
            mBtnOled.setTextColor(Color.BLACK);
            mBtnCopy.setTextColor(Color.BLACK);
            mBtnCopy.setTextColor(Color.WHITE);
        }else {
            mBtnSend.setText(getString(R.string.sendd));//发送中都不能按
            mBtnOpenFile.setEnabled(true);
            mBtnOled.setEnabled(true);
            mBtnCopy.setEnabled(true);
            mBtnOpenFile.setTextColor(Color.WHITE);
            mBtnSend.setTextColor(Color.WHITE);
            mBtnOled.setTextColor(Color.WHITE);
            mBtnCopy.setTextColor(Color.WHITE);
        }

        if(mOledMode ==OLED_MODE_COLOR){
            mBtnOled.setText(getResources().getString(R.string.image_color));
        }
        else{
            mBtnOled.setText(getResources().getString(R.string.image_binary));
        }
    }
    private String getDecodingImage(){
        return "";
    }
    public void setUri(Uri uri){
        mHandler.obtainMessage(MSG_GET_URI,uri).sendToTarget();
    }
    //---------------------------------------------------------------------------------------------------------------
    private final int MSG_CHANGE_IMG = 0;
    private final int MSG_CHANGE_UI = 1;
    private final int MSG_START_MUSIC = 2;
    private final int MSG_UPDATE_TV = 3;
    private final int MSG_STOP_MUSIC = 4;
    private final int MSG_GET_URI = 5;
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
                    updateUI();
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
                case MSG_GET_URI:{
                    Uri uri = (Uri)msg.obj;

                    Log.d(TAG,"get uri");
                    if(uri==null)break;
                    Log.d(TAG,"uri:"+uri.getPath());
                    Toast.makeText(mActivity,uri.getPath(),Toast.LENGTH_SHORT).show();;
                    mTvFile.setText(uri.getPath());
                    dealUri(uri);
                    break;
                }

            }
        }
    };

    private void dealUri(Uri uri){
        Log.d(TAG,"get uri page"+uri.getPath()+" "+uri.getAuthority()+" "+uri.getLastPathSegment()+" "+uri.getQuery());
        String path = "";
        String type = "";
        String[] searchKey = new String[] {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE
        };
        String [] keywords = null;
        ContentResolver resolver = mActivity.getContentResolver();
        try{
            Cursor cursor = resolver.query(uri, searchKey, null, keywords, null);
            if(cursor!=null) {
                while(cursor.moveToNext()){
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    type = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    //-------------
                    int length = cursor.getColumnCount();
                    String cursorData= "";
                    for(int i=0;i<length;i++){
                        cursorData+="|"+cursor.getColumnName(i);
                        cursorData +=" "+ cursor.getString(i);
                    }
                    Log.d(TAG,"get cursor "+path +"|"+cursorData);
                    //--------------
                }
                cursor.close();
            }
        }catch(Exception e) {
            Log.d(TAG,"error,need strog power");
        }
        if(path!=null&&type!=null){
            mSendMode = SEND_MODE_CANCLE;
            updateUI();
            if(type.indexOf("image")==-1){
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
                if(mOledMode==OLED_MODE_BINARY){
                    vst.executeOnExecutor(Executors.newCachedThreadPool(),(TASK_BINARY));;
                }else{
                    vst.executeOnExecutor(Executors.newCachedThreadPool(),(TASK_COLOR));;
                }
                Log.d(TAG,"get back success "+path);
            }else{//image型
                try{
                   mBitmap =  getBitmapFormUri(mActivity,uri);
                    mImageView.setImageBitmap(mBitmap);
                    VideoSendTask vst = new VideoSendTask();
                    if(mVideoSendTask!=null){
                        mVideoSendTask.onCancelled();
                    }
                    mVideoSendTask = vst;
                    if(mOledMode==OLED_MODE_BINARY){
                        vst.executeOnExecutor(Executors.newCachedThreadPool(),(TASK_BITMAP_BINARY));;
                    }else{
                        vst.executeOnExecutor(Executors.newCachedThreadPool(),(TASK_BITMAP_COLOR));;
                    }
                    Log.d(TAG,"get back success "+path);
                }catch(Exception e){
                    Log.d(TAG,"creat bitmap error");
                }
            }
        }else{
            Log.d(TAG,"get back error:get null pointer "+path);
        }
    }
    //代码来源https://blog.csdn.net/jdsjlzx/article/details/51181229
    public static  Bitmap getBitmapFormUri(Activity activity, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = activity.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = activity.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
    private void dealPhotoByUri(Uri uri){

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

    private class VideoSendTask extends AsyncTask<Object,String,Void> {
        boolean taskSendable = true;
        @Override
        protected Void doInBackground(Object... params) {
            int sendMode = (int)params[0];
            Log.d(TAG,"start task"+sendMode);
            int duration =0;
            int frameRate = 30;
            if(sendMode==TASK_BINARY||sendMode==TASK_COLOR){
                if(mMedia==null)return null;
                String durationS = mMedia. extractMetadata( MediaMetadataRetriever. METADATA_KEY_DURATION);
                try{
                    duration =Integer.parseInt(durationS);
                }catch (Exception e){
                    Log.d(TAG,"duration change error");
                    duration = 0;
                }
            }
            long time = 0;
            Bitmap bitmap = null;
            int count = 0;
            Vector<Vector<Integer>> vv = new Vector<Vector<Integer>>();
            if(sendMode == TASK_BINARY){
                Log.d(TAG,"binary task start");
                while(time/1000</*3*1000*/duration&&taskSendable){//
                    bitmap = mMedia.getFrameAtTime(time,MediaMetadataRetriever.OPTION_CLOSEST);
                    Vector<Integer>v = VideoDecoder.decodeBinary4Oled(bitmap,new Size(96,64),mBitmap);
                    String s=""+(time/100000)+"/"+(duration/100);
                    mHandler.obtainMessage(MSG_UPDATE_TV,s).sendToTarget();
                    if((count++)%2==1){
                        mHandler.obtainMessage(MSG_CHANGE_IMG).sendToTarget();
                    }
                    vv.add(v);
                    time +=(1000000/frameRate);//us单位 30帧
                }
            }else if(sendMode ==TASK_COLOR){
                Log.d(TAG,"color task start");
                while(time/1000</*1*1000*/duration&&taskSendable){
                    bitmap = mMedia.getFrameAtTime(time,MediaMetadataRetriever.OPTION_CLOSEST);
                    Vector<Integer>v =  VideoDecoder.decodeColor(bitmap,new Size(80,60),mBitmap);
                    String s=""+(time/100000)+"/"+(duration/100);
                    mHandler.obtainMessage(MSG_UPDATE_TV,s).sendToTarget();
                    if((count++)%2==1){
                        mHandler.obtainMessage(MSG_CHANGE_IMG).sendToTarget();
                    }
                    vv.add(v);
                    time +=(1000000/frameRate);//us单位 30帧
                }
            }else if(sendMode ==TASK_BITMAP_COLOR){
                Log.d(TAG,"color bitmap start");
                bitmap = mBitmap;
                Vector<Integer>v =  VideoDecoder.decodeColor(bitmap,new Size(80,60),mBitmap);
                mHandler.obtainMessage(MSG_UPDATE_TV,"1/1").sendToTarget();
                mHandler.obtainMessage(MSG_CHANGE_IMG).sendToTarget();
                vv.add(v);
            } else if(sendMode == TASK_BITMAP_BINARY){
                Log.d(TAG,"binary bitmap start");
                bitmap = mBitmap;
                Vector<Integer>v = VideoDecoder.decodeBinary4Oled(bitmap,new Size(96,64),mBitmap);
                mHandler.obtainMessage(MSG_UPDATE_TV,"1/1").sendToTarget();
                mHandler.obtainMessage(MSG_CHANGE_IMG).sendToTarget();
                vv.add(v);
            }
            if(vv!=null&&vv.size()!=0)mLastFrameDataVector = vv.lastElement();
            //   mImageView.setImageBitmap(mBitmap);
            Log.d(TAG,"add "+frameCount);
//            mHandler.obtainMessage(MSG_UPDATE_TV,"kuso").sendToTarget();
            while(taskSendable){
                long sendtime = System.currentTimeMillis();
                if(vv!=null&&vv.size()!=0&&vv.get(0)!=null&&vv.get(0).size()!=0){
                    publishProgress(getString(R.string.start_send));
                    int btCount = 0;
//                    mHandler.obtainMessage(MSG_START_MUSIC).sendToTarget();
                    long sendStartTime =  System.currentTimeMillis();
                    while(/*btCount< vv.size()&&*/taskSendable){
                        int sendFrame = (int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000;
                        if(sendFrame>=vv.size()&&sendMode==TASK_COLOR||sendMode==TASK_BINARY){
                            taskSendable = false;
                            break;
                        }
                        Log.d("btA","finish cell search");
                        char c1 ,c2;
                        if(sendMode == TASK_BINARY||sendMode==TASK_BITMAP_BINARY){
                            c1 = 35;c2 = 36;
                            String s[] = new String[2];
                            s[0] = c1+"";
                            s[1] = c2+"";
                            sendMessage(s);
                        }else if(sendMode==TASK_COLOR||sendMode==TASK_BITMAP_COLOR){
                            byte bb[] = new byte[2];
                            Integer it = Integer.valueOf("01",16);
                            bb[0] =(byte)it.intValue();
                            it = Integer.valueOf("FE",16);
                            bb[1] =(byte)it.intValue();
                            sendMessage(bb);
                        }
                        int vvSize = vv.get(btCount).size();
                        byte b[] = new byte[vvSize];
                        for(int i=0;i<vvSize;i++){
                            b[i] = (byte)vv.get(sendFrame).get(i).intValue();
                        }
                        boolean ans =sendMessage(b);
                        if(!ans){
                            taskSendable = false;
                        }
                        if(sendMode==TASK_COLOR||sendMode==TASK_BITMAP_COLOR){
                            byte bb[] = new byte[2];
                            Integer it = Integer.valueOf("FE",16);
                            bb[0] =(byte)it.intValue();
                            it = Integer.valueOf("01",16);
                            bb[1] =(byte)it.intValue();
                            sendMessage(bb);
                        }
                        Log.d("btA",btCount+"/"+vv.size()+"send  over once");
                        btCount++;
                        if(sendMode==TASK_BITMAP_BINARY||sendMode==TASK_BITMAP_COLOR){
                            taskSendable=false;
                            break;
                        }
                        while(btCount>sendFrame){
                            sendFrame = (int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000;
                            Log.d("WAIT","wait");
                        }
                        if(btCount<sendFrame){
                            sendFrame = (int)(System.currentTimeMillis()-sendStartTime)*frameRate/1000;
                            btCount = (sendFrame-btCount)/2;
                        }
                        if(btCount>=vv.size())break;
                    }
//                    mHandler.obtainMessage(MSG_STOP_MUSIC).sendToTarget();
                }else{
                    taskSendable = false;
                    Log.d(TAG,"cannot get vv");
                }
                Log.d("btA","send over"+(System.currentTimeMillis()-sendtime));
            }

            publishProgress(getString(R.string.finish_send));
            mSendMode = SEND_MODE_SEND;
            mHandler.obtainMessage(MSG_CHANGE_UI).sendToTarget();
            return null;
        }
        private void getVectorColorBitmap(Bitmap bitmap){
            if(bitmap!=null){

            }
        }
        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG,values[0]);
            //  if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
            Toast.makeText(mActivity,values[0],Toast.LENGTH_SHORT).show();
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


