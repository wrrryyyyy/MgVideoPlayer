package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by aa on 2018/10/7.
 */

public class RemoteFragment extends BaseFragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "RemoteFragment";
    private Activity mActivity;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private TextView mTvGryo;
    private CheckBox mCheckBox;
    private Button mButtonUp,mButtonRight,mButtonLeft,mButtonDown,mButtonCenter,mButtonA,mButtonB,mButtonShowMore;
    private ConnectionManager mConnectionManager;
    private SensorManager mSensorManager;
    //------------没啥用的变量---------------
    private boolean mGryoState = true;
    public RemoteFragment() {
        // Required empty public constructor
    }

    public static RemoteFragment newInstance(String param1, String param2) {
        return newInstance(param1,param2,null);
    }
    public static RemoteFragment newInstance() {
        return newInstance(null);
    }
    public static RemoteFragment newInstance(int lineNum) {
        return newInstance(null);
    }
    public static RemoteFragment newInstance(List<List<PointValue>>list){
        return newInstance("","",list);
    }

    public static RemoteFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
        RemoteFragment fragment = new RemoteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
//        if(list!=null)fragment.mTempDataList = list;
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
        View view = inflater.inflate(R.layout.fragment_remote, container, false);
        mConnectionManager = ConnectionManager.getConnectionManager(null);
        initUI(view);
        return view;
    }
    private void initUI(View view){
        mTvGryo = (TextView)view.findViewById(R.id.tv_remote);
        mCheckBox = (CheckBox)view.findViewById(R.id.cb_remote);
        mCheckBox.setChecked(mGryoState);
        mButtonUp = (Button)view.findViewById(R.id.remote_up_btn);
        mButtonDown = (Button)view.findViewById(R.id.remote_down_btn);
        mButtonLeft = (Button)view.findViewById(R.id.remote_left_btn);
        mButtonRight = (Button)view.findViewById(R.id.remote_right_btn);
        mButtonCenter = (Button)view.findViewById(R.id.remote_center_btn);
        mButtonA = (Button)view.findViewById(R.id.remote_A_btn);
        mButtonB = (Button)view.findViewById(R.id.remote_B_btn);
        mButtonShowMore = (Button)view.findViewById(R.id.btn_remote_show_more);
        mButtonUp.setOnTouchListener(btnTouchListener);
        mButtonDown.setOnTouchListener(btnTouchListener);
        mButtonLeft.setOnTouchListener(btnTouchListener);
        mButtonRight.setOnTouchListener(btnTouchListener);
        mButtonCenter.setOnTouchListener(btnTouchListener);
        mButtonA.setOnTouchListener(btnTouchListener);
        mButtonB.setOnTouchListener(btnTouchListener);
        mButtonShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage("up      :'1'/'a'\ndown  :'2'/'b'\nleft     :'3'/'c'\nright   :'4'/'d'" +
                        "\ncenter:'5'/'e'\nO         :'6'/'f'\nX         :'7'/'g'")
                        .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setTitle(R.string.button_key_values);

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGryoState = isChecked;
                updateButtons();
                setSensorListener();
            }
        });
        updateButtons();
    }

    View.OnTouchListener btnTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(v.getId()){
                case R.id.remote_up_btn:{
                    if(event.getAction() == MotionEvent.ACTION_DOWN)sendMessage("1");
                    if(event.getAction() == MotionEvent.ACTION_UP)sendMessage("a");
                }
                break;
                case R.id.remote_down_btn:{
                    if(event.getAction() == MotionEvent.ACTION_DOWN)sendMessage("2");
                    if(event.getAction() == MotionEvent.ACTION_UP)sendMessage("b");
                }
                break;
                case R.id.remote_left_btn:{
                    if(event.getAction() == MotionEvent.ACTION_DOWN)sendMessage("3");
                    if(event.getAction() == MotionEvent.ACTION_UP)sendMessage("c");
                }
                break;
                case R.id.remote_right_btn:{
                    if(event.getAction() == MotionEvent.ACTION_DOWN)sendMessage("4");
                    if(event.getAction() == MotionEvent.ACTION_UP)sendMessage("d");
                }
                break;
                case R.id.remote_center_btn:{
                    if(event.getAction() == MotionEvent.ACTION_DOWN)sendMessage("5");
                    if(event.getAction() == MotionEvent.ACTION_UP)sendMessage("e");
                }
                break;
                case R.id.remote_A_btn:{
                    if(event.getAction() == MotionEvent.ACTION_DOWN)sendMessage("6");
                    if(event.getAction() == MotionEvent.ACTION_UP)sendMessage("f");
                }
                break;
                case R.id.remote_B_btn:{
                    if(event.getAction() == MotionEvent.ACTION_DOWN)sendMessage("7");
                    if(event.getAction() == MotionEvent.ACTION_UP)sendMessage("g");
                }
                break;
            }

            return false;
        }
    };
    private void updateButtons(){
        if(mGryoState||!mConnectionManager.isConnectioned()){//陀螺仪发送模式
            mButtonA.setEnabled(false);
            mButtonB.setEnabled(false);
            mButtonCenter.setEnabled(false);
            mButtonUp.setEnabled(false);
            mButtonDown.setEnabled(false);
            mButtonLeft.setEnabled(false);
            mButtonRight.setEnabled(false);
        }else{
            mTvGryo.setText("");
            mButtonA.setEnabled(true);
            mButtonB.setEnabled(true);
            mButtonCenter.setEnabled(true);
            mButtonUp.setEnabled(true);
            mButtonDown.setEnabled(true);
            mButtonLeft.setEnabled(true);
            mButtonRight.setEnabled(true);
        }
    }
    private void updateUI(){
        int x,y,z,x1,y1,z1,x2,y2,z2,x3,y3,z3;
        x = (int)(xGryo*10000);//角速度
        y = (int)(yGryo*10000);
        z = (int)(zGryo*10000);
        x1 = (int)(xAccl*10);//加速度
        y1 = (int)(yAccl*10);
        z1 = (int)(zAccl*10);
        x2 = (int)(xRate*180/Math.PI);//角度
        y2 = (int)(yRate*180/Math.PI);
        z2 = (int)(zRate*180/Math.PI);
        x3 = (int)(xAcclRate*10);//角速度累加
        y3 = (int)(yAcclRate*10);
        z3 = (int)(zAcclRate*10);
        Log.d(TAG+"gyro","1 x "+x+" y "+y+" z " +z);
        Log.d(TAG+"gyro","2 x "+x1+" y "+y1+" z " +z1);
        Log.d(TAG+"gyro","3 x "+x2+" y "+y2+" z " +z2);
        Log.d(TAG+"gyro","4 x "+x3+" y "+y3+" z " +z3 + "4 x "+(int)(xAcclRate/Math.PI*180/mSameleRate)+" y "+(int)(yAcclRate/Math.PI*180/mSameleRate)+" z " +(int)(zAcclRate/Math.PI*180/mSameleRate));
        mTvGryo.setText(" x "+x2+" y "+y2+" z "+z2 );
//        mTvGryo.setText(" x "+x3+" y "+y3+" z "+z3+" x "+x1+" y "+y1+" z "+z1+" x "+x2+" y "+y2+" z "+z2 + " zo "+(int)(zOut*180/Math.PI));
//        mTvGryo.setText(" x "+x3+" y "+y3+" z "+z3+" x "+x1+" y "+y1+" z "+z1+" x "+x2+" y "+y2+" z "+z2 + " zo "+(int)(zOut*180/Math.PI));
    }
    private final int MSG_UPDATE_GRYO = 55;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_UPDATE_GRYO:{//这个是后期自己家的 控制的蓝牙侧边栏的
                    updateUI();
                    sendMessageGryo();
                    break;
                }
            }
        }
    };
    private float xGryo,yGryo,zGryo,xAccl,yAccl,zAccl;
    private float xRate,yRate,zRate;
    private float xOut,yOut,zOut;
    private float xAcclRate,yAcclRate,zAcclRate;
    private float xGryoLast,yGryoLast,zGryoLast,xAcclLast,yAcclLast,zAcclLast;
    private long mSampleTime  = 0;
    private float mSameleRate = 1;
    //姿态解算
    //实际测试了一下 NOMAL模式下的陀螺仪返回加速度有个很大的滞后性 换到faster就没有 而且积分起来挺准的 实时性差半秒左右 可能是我反应的问题
    //然后普通速度加速度计条件下的姿态好像也还行..有一定波动 感觉可以下位机做处理啥的
    private void attitudeAlgorithm(){
        long now = System.currentTimeMillis();
        long time = now -mSampleTime;
        mSameleRate = 1000f/time;
        xRate = (float)Math.atan2(zAccl,yAccl);
        yRate = (float)Math.atan2(xAccl,zAccl);
        zRate = (float)Math.atan2(yAccl,xAccl);
        xAcclRate +=xGryo;
        yAcclRate +=yGryo;
        zAcclRate +=zGryo;
        xGryoLast = xGryo;
        yGryoLast = yGryo;
        zGryoLast = zGryo;
        mSampleTime = now;
    }
    private void attitudeAlgorithmZ(float x,float y,float zG,long sampleTime){//xy平面 z的角速度
        float rate = 0.5f;
        float gravitation = (float)Math.atan2(x,y);
        float adjust = (zOut -gravitation)*rate;//上次和现在的重力角间的角度差
//        zOut += (zG) /*- adjust*/;//输出为采到的角加速度减去偏差然后除于采样次数 =1000/(单次采样时间)
//        zOut += (zG)*sampleTime/(1000) /*- adjust*/;//输出为采到的角加速度减去偏差然后除于采样次数 =1000/(单次采样时间)
        zOut += (zG - adjust)*sampleTime/(1000);//输出为采到的角加速度减去偏差然后除于采样次数 =1000/(单次采样时间)

        Log.d(TAG+"gyroAccl",""+zOut*180/Math.PI);
    }
    private void attitudeAlgorithmInit(){
        xOut = (float)Math.atan2(zAccl,yAccl);
        yOut = (float)Math.atan2(xAccl,zAccl);
        zOut = (float)Math.atan2(yAccl,xAccl);
    }
    //---------------------其他一些测试用的东西-----------------------------------
    private void initSensor(){
        if(mActivity!=null){

            mSensorManager = (SensorManager)(mActivity.getSystemService(Context.SENSOR_SERVICE));
//        mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_FASTEST);
            setSensorListener();
        }
        mStartTime = System.currentTimeMillis();
    }
    private void setSensorListener(){
        if(mGryoState){
            mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            closeSensor();
        }
    }
    private void closeSensor(){
        if(mSensorManager!=null){
            mSensorManager.unregisterListener(mSensorEventListener);
        }
    }
    long mStartTime = 0;
    SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                xAccl = x;
                yAccl= y;
                zAccl =z;
                Log.d(TAG,"sensor value:"+x+" "+y+" "+z);
            }
            if(event.sensor.getType() ==Sensor.TYPE_GYROSCOPE){
                if(mSampleTime==0){
                    mSampleTime = System.currentTimeMillis();
                    attitudeAlgorithmInit();
                }else{

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    xGryo = x;
                    yGryo= y;
                    zGryo =z;
                    Log.d(TAG,"sensor value gyro:"+x+" "+y+" "+z);
                    attitudeAlgorithm();
                    mHandler.obtainMessage(MSG_UPDATE_GRYO).sendToTarget();

                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private boolean sendMessage(String message){
        if(message == null){
            Log.d(TAG,"error:send message null");
            return false;
        }
        boolean ans = mConnectionManager.sendData(message.getBytes());
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
    private boolean sendMessageGryo(){
        String s[] = new String[2];
        char c1 = 38,c2=36;
        s[0] = c1+"";
        s[1] = c2+"";
        sendMessage(s);
        byte[] x = new byte[4],y = new byte[4], z = new byte[4];
        x =VideoDecoder.float2Bytes(xRate);
        y =VideoDecoder.float2Bytes(yRate);
        z = VideoDecoder.float2Bytes(zRate);
//        Log.d(TAG,"x"+x[0]+" "+x[1])+" "+x[2]+" "+x[3]+" "+xRate);
        boolean ans = true;
        if(! mConnectionManager.sendData(x))ans= false;
        if(! mConnectionManager.sendData(y))ans= false;
        if(! mConnectionManager.sendData(z))ans= false;
        if(!ans)Log.d(TAG,"send message error"+x+" ");
        return ans;
    }

    @Override
    void loadData() {
        //initSensor();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
        mActivity = (Activity)context;
        if(mActivity==null){
            Log.d(TAG+"mActi","mActivity get null");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
        Log.d(TAG+"mActi","view close");
    }

    @Override
    public void onResume() {
        super.onResume();
//        setSensorListener();

    }

    @Override
    public void onPause() {
        super.onPause();
        closeSensor();
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(!isVisibleToUser){
            closeSensor();
        }else{
            initSensor();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
