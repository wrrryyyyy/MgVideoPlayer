package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;
import java.util.Vector;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by aa on 2018/10/7.
 */

public class PhotoCameraFragment extends BaseFragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "CameraFragment";
    private Activity mActivity;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private TextView mTvFront;
    private  Button mSendButton;
    private Button mChangeButton;
    private Button mAutoButton;
    private Button mCameraButton;
    private ConnectionManager mConnectionManager;
    //------------opencv---------------------

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgb ;
    private int stage_camera = 0;//0对应后置摄像头 CAMERA_ID_FRONT 反之CAMERA_ID_BACK
    private Mat mCameraFrame ;
    private boolean mColorCameraFrameSending = false;
    private CameraSendTask mCameraSendTask ;

    //------------没啥用的变量---------------
    private int mwidth = 400;
    private int mheight = 400;
    public PhotoCameraFragment() {
        // Required empty public constructor
    }

    public static PhotoCameraFragment newInstance(String param1, String param2) {
        return newInstance(param1,param2,null);
    }
    public static PhotoCameraFragment newInstance() {
        return newInstance(null);
    }
    public static PhotoCameraFragment newInstance(int lineNum) {
        return newInstance(null);
    }
    public static PhotoCameraFragment newInstance(List<List<PointValue>>list){
        return newInstance("","",list);
    }

    public static PhotoCameraFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
        PhotoCameraFragment fragment = new PhotoCameraFragment();
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
        View view = inflater.inflate(R.layout.fragment_phone_camera, container, false);
        initUI(view);
        initControl();
        return view;
    }
    private  void initControl(){
        mConnectionManager = ConnectionManager.getConnectionManager(null);
    }
    private void initUI(View v){
        //opencv init
        openCvInit(v);
        initButton(v);
    }
    private void initButton(View v){
        mChangeButton = (Button)v.findViewById(R.id.change_btn);
        mCameraButton = (Button)v.findViewById(R.id.camera_send_btn);
        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenCvCameraView.disableView();
                if(PhotoCameraFragment.this.stage_camera==0){
                    mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                    PhotoCameraFragment.this.stage_camera=1;
                }else{
                    mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                    PhotoCameraFragment.this.stage_camera=0;
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
    }
    private void openCvInit(View v){
        staticLoadCVLibraries();
        mOpenCvCameraView = (CameraBridgeViewBase) v.findViewById(R.id.camera_find_color);
        assert mOpenCvCameraView != null;
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.enableView();//
        mOpenCvCameraView.setCvCameraViewListener(mOpencvListener);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);//前置摄像头 CameraBridgeViewBase.CAMERA_ID_BACK为后置摄像头
   //     mOpenCvCameraView.setOnTouchListener(BluetoothActivity.this);
        mOpenCvCameraView.setMaxFrameSize(mwidth,mheight);
    }
    private CameraBridgeViewBase.CvCameraViewListener2 mOpencvListener = new CameraBridgeViewBase.CvCameraViewListener2() {
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

    };
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

    private final static int MSG_UPDATE_UI = 2;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_UPDATE_UI:{
                 ///   updateUI();
                }
                break;
            }
        }
    };

    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i(TAG, "Open CV Libraries loaded...");
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
                Vector<Integer> v = new Vector<Integer>();
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
            Toast.makeText(mActivity,values[0],Toast.LENGTH_SHORT).show();
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
}


