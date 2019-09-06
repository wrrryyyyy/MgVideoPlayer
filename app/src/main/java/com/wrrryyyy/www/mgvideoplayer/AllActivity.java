package com.wrrryyyy.www.mgvideoplayer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;

public class AllActivity extends AppCompatActivity implements CameraImageFragment.OnFragmentInteractionListener{
    private final String TAG ="AllActivity";
    private final String TAG1 ="AllActivity_TEST";
    private final String DEF_BT_KEY = "def_bt_key";
    private final String DEF_BT_VALUE = "def_bt_value";
    private final String[] title = new String[]{"串口","图像","遥控","波形","示教器","表情包"};
 //   private final String[] title = new String[]{"串口","图像","遥控","波形","摄像头","表情包"};
    private final int[] TAB_ICON_ID = new int[]{
            R.drawable.ic_uart,
            R.drawable.ic_photo,
            R.drawable.ic_gamepad,
            R.drawable.ic_timeline,
            R.drawable.ic_camera,
            R.drawable.ic_huaji};
    private final int[] TAB_ICON_ID_THEME = new int[]{
            R.drawable.ic_uart_theme,
            R.drawable.ic_photo_theme,
            R.drawable.ic_gamepad_theme,
            R.drawable.ic_timeline_theme,
            R.drawable.ic_camera_theme,
            R.drawable.ic_huaji_theme};
    private NoRollViewPager mViewPager ;
    private TabLayout mTabLayout;
    private Button mBtnLeftMenu;
    private Button mBtnLeftAbout;
    private ImageButton mIbBluetooth;
    private TextView mTvBluetooth;
    private ListView mLeftListView;
    private LinearLayout mLeftLinearLayout;
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectionManager mConnectionManager;
    private ByteArrayOutputStream mBuffStream;
    private TimerAsyncTask mTimeTask;
    private SharedPreferences mSp;
    private DrawerLayout mDrawerLayout;
    //-------------sensor-----------
    private SensorManager mSensorManager;

    //------------------fragments---------------
    CameraImageFragment mCameraImageFragment;
    WavePlayerFragment mWavePlayerFragment;
    UartTextFragment mUartTextFragment;
    RemoteFragment mRemoteFragment;
    EmojiFragment mEmojiFragment;
    TeacherFragment mTeacherFragment;
    PhotoCameraFragment mPhotoCameraFragment;
    //----------------decode value---------------------

    private  byte[] mGetBuff;
    private String mDeviceName = "";
    //------------------------------------
    private int mSelectedItemNum = 0;
    private boolean mBluetoothDisconnected = true;//没有连接并且发送的话丢 一个toast
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        staticLoadCVLibraries();
        Mat testMat = new Mat();
        setContentView(R.layout.activity_all);
        initPageByFragment();//把各个Fragment放到下边栏里面
        initTabLayout();//初始化下边栏
        initBluetooth();//蓝牙初始化
        initLeftMenu();//初始化左拉框
        initTimerAsynTask();//开个计时器
        initSharedPreferences();
//        initSensor();
    }


    private void initTimerAsynTask(){
        mTimeTask = new TimerAsyncTask(25);
        mTimeTask.execute();
    }
    private void initSharedPreferences(){
        GetDeviceTask getDeviceTask = new GetDeviceTask();
        getDeviceTask.executeOnExecutor(Executors.newCachedThreadPool());;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mTimeTask!=null){
            mTimeTask.pause();
        }
//        closeSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTimeTask!=null){
            mTimeTask.restart();
        }
    }

    private void initBluetooth(){
        mBuffStream = new ByteArrayOutputStream(buffStreamLength);
        mConnectionManager = ConnectionManager.getConnectionManager(mConnectionListener);
        if(mConnectionManager.isListening())mConnectionManager.startListen();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver,filter);
    }
    private void initLeftMenu(){
        mDrawerLayout = (DrawerLayout)findViewById(R.id.dl_left_menu);
        mIbBluetooth = (ImageButton)findViewById(R.id.ib_head);
        mLeftListView = (ListView)findViewById(R.id.lv_left_menu);
        mLeftLinearLayout = (LinearLayout)findViewById(R.id.left_linear_layout);
        mTvBluetooth = (TextView) findViewById(R.id.tv_bluetooth_state);
        DeviceItemAdapter adapter = new DeviceItemAdapter(this,R.layout.device_item);
        mLeftListView.setAdapter(adapter);
        mLeftListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                ArrayAdapter adapter = (ArrayAdapter) mLeftListView.getAdapter();
                BluetoothDevice device  = (BluetoothDevice) adapter.getItem(position);
                String deviceAddr = device.getAddress();//传回来的地址
                String deviceName = device.getName();
                mDeviceName = deviceName;
                mConnectionManager.connect(deviceAddr);
                mSelectedItemNum = position;
            }
        });
        mIbBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(AllActivity.this,"get click",Toast.LENGTH_SHORT).show();
                if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTED){
                    mConnectionManager.disconnect();
                }
            }
        });
        mBtnLeftMenu = (Button)findViewById(R.id.btn_all_left_menu);
        mBtnLeftMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        mBtnLeftAbout = (Button)findViewById(R.id.btn_all_left_about);
        mBtnLeftAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AllActivity.this,VersionActivity.class);
            //    Intent i = new Intent(AllActivity.this,BluetoothActivity.class);
                startActivity(i);
            }
        });

        updateLeftList();
    }
    private void initTabLayout(){
        mTabLayout = (TabLayout)findViewById(R.id.all_tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);//要先关联起来才会产生实际的tab

        for(int i=0;i<title.length;i++){
            if(i!=0){
                mTabLayout.getTabAt(i).setCustomView(getTabViewUnselect(i));
            }else{
                mTabLayout.getTabAt(i).setCustomView(getTabViewSelect(i));
            }
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSelect(tab);
                Log.d(TAG+"TABLAYOUT","selected"+tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tabRelease(tab);
                Log.d(TAG+"TABLAYOUT","Unselected"+tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    private void tabSelect(TabLayout.Tab tab){
        View view = tab.getCustomView();
        TextView textView= (TextView)view.findViewById(R.id.tv_all_tab);
        ImageView imageView= (ImageView)view.findViewById(R.id.iv_all_tab);
        textView.setTextColor(ContextCompat.getColor(this,R.color.colorThemeYellow));
//        textView.setText(tab.getText());
        imageView.setImageResource(TAB_ICON_ID_THEME[tab.getPosition()]);
    }
    private void tabRelease(TabLayout.Tab tab){
        View view = tab.getCustomView();
        TextView textView= (TextView)view.findViewById(R.id.tv_all_tab);
        ImageView imageView= (ImageView)view.findViewById(R.id.iv_all_tab);
        textView.setTextColor(ContextCompat.getColor(this,R.color.colorBlack));
//        textView.setText(tab.getText());
        imageView.setImageResource(TAB_ICON_ID[tab.getPosition()]);
    }

    private View getTabViewUnselect(int position){
        View view = LayoutInflater.from(this).inflate(R.layout.view_all_tab,null);
        TextView textView= (TextView)view.findViewById(R.id.tv_all_tab);
        ImageView imageView= (ImageView)view.findViewById(R.id.iv_all_tab);
        imageView.setImageResource(TAB_ICON_ID[position]);
        textView.setText(title[position]);
        return view;
    }
    private View getTabViewSelect(int position){
        View view = LayoutInflater.from(this).inflate(R.layout.view_all_tab,null);
        TextView textView= (TextView)view.findViewById(R.id.tv_all_tab);
        textView.setTextColor(ContextCompat.getColor(this,R.color.colorThemeYellow));
        ImageView imageView= (ImageView)view.findViewById(R.id.iv_all_tab);
        imageView.setImageResource(TAB_ICON_ID_THEME[position]);
        textView.setText(title[position]);
        return view;
    }
    void initPageByFragment(){
        List<Fragment> list = new ArrayList<>();
        mWavePlayerFragment = WavePlayerFragment.newInstance(0);
        mCameraImageFragment = CameraImageFragment.newInstance(0);
        mUartTextFragment = UartTextFragment.newInstance();
        mRemoteFragment = RemoteFragment.newInstance();
        mPhotoCameraFragment = PhotoCameraFragment.newInstance();
        mEmojiFragment = EmojiFragment.newInstance();
        mTeacherFragment = TeacherFragment.newInstance();
//        fg2.addLine("one");
//        fg2.addLine("two");
//        fg2.addLine("three");
        List<String>titles = new ArrayList<>();
        mViewPager = (NoRollViewPager)findViewById(R.id.all_viewpager);
        mViewPager.setRollable(false);
        list.add(mUartTextFragment);
        list.add(mCameraImageFragment);
        list.add(mRemoteFragment);
        list.add(mWavePlayerFragment);
     //   list.add(mPhotoCameraFragment);  //这里先牺牲一下摄像头的东西
        list.add(mTeacherFragment);
        list.add(mEmojiFragment);
        titles.add(title[0]);
        titles.add(title[1]);
        titles.add(title[2]);
        titles.add(title[3]);
        titles.add(title[4]);
        titles.add(title[4]);
        mViewPager.setAdapter(new AllFragmentAdapter(getSupportFragmentManager(),list,titles));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        if(mTimeTask!=null)mTimeTask.stopTask();
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacksAndMessages(null);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"get result"+requestCode+" "+resultCode);
        if((requestCode ==EmojiFragment.REQUEST_CODE_SELECT_FILE||requestCode ==EmojiFragment.REQUEST_CODE_SELECT_FILE_COLOR)&&resultCode ==RESULT_OK){
            Uri uri = data.getData();
            if(mEmojiFragment!=null)mEmojiFragment.setUri(uri);
        }else if((requestCode ==TeacherFragment.REQUEST_CODE_SELECT_IMAGE)&&resultCode ==RESULT_OK){
            Uri uri = data.getData();
            if(mTeacherFragment!=null)mTeacherFragment.setUri(uri);

        }
        else{
            Log.d(TAG,"get device error ");
        }
    }

//opencv init
    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }






    //-----------------------------------fragment通信---------------------------------------------------------
    private boolean isFragmentShow(){
        if(mCameraImageFragment==null)return false;
        return isFragmentShow(mCameraImageFragment);
    }
    private boolean isFragmentShow(BaseFragment baseFragment){
        if(baseFragment==null)return false;
        return baseFragment.isViewVisible();
    }
    private void updateCameraImageFragment(UartItem ui) {
        if(isFragmentShow())mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_ADD_STRING,ui);//updateUI()
    }
    private void updateCameraImageFragment(int msg){
        if(isFragmentShow())mCameraImageFragment.messageInterface(msg);
    }
    private void updateCameraImageFragment() {
        if(isFragmentShow())mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_UPDATE_TV);//updateUI()
    }
    private void updateCameraImageFragment(int msg,Object obj) {
        if(isFragmentShow())mCameraImageFragment.messageInterface(msg,obj);

    }
    private void updateCameraImageFragment(RawImage rawImage) {
        if(isFragmentShow())mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_CHANGE_IMAGE_BY_RAW_IMAGE,rawImage);//updateImageByByte(cuted);
    }
    private void updateWaveFragment(float[] points){
        if(isFragmentShow(mWavePlayerFragment))mWavePlayerFragment.addListValueAddLine(points);
    }
    private void updateUartTextViewFragment(byte[] b){
        if(isFragmentShow()){
            addupShowStringMG(b);
        }
        if(isFragmentShow(mUartTextFragment)&&mUartTextFragment.isReachAble()){
            updateCameraImageFragment(new UartItem(0,"",b,0));
            mUartTextFragment.messageInterface(mUartTextFragment.MSG_ADD_STRING,new UartItem(0,"",b,0));
            mUartTextFragment.messageInterface(mUartTextFragment.MSG_UPDATE_TV);
        }
    }
    static int teacherFragmentCount = 0;
    private void updateTeacherFragment(TeacherCoder tc){

            if(isFragmentShow(mTeacherFragment)&&mTeacherFragment.isReachAble()){
//            updateCameraImageFragment(new UartItem(0,"",b,0));
                mTeacherFragment.messageInterface(mTeacherFragment.MSG_ADD_DATA,tc);
             //   mTeacherFragment.messageInterface(mTeacherFragment.MSG_UPDATE_TV);
            }

    }
//------------------------------------------------------------------------------------------------------------
    final int MSG_UPDATE_LEFT_MENU = 5;
    final int MSG_UPDATE_STATE_CHANGE = 6;
    final int MSG_SHOW_TOAST_NO_CONNECTED = 7;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_UPDATE_LEFT_MENU:{// 控制的蓝牙侧边栏的
                    updateLeftMenuUI();
                    break;
                }
                case MSG_UPDATE_STATE_CHANGE:{
                    Toast.makeText(AllActivity.this,"连接已断开( •ω＜ )ρ⌒☆",Toast.LENGTH_SHORT).show();
                    break;
                }
                case MSG_SHOW_TOAST_NO_CONNECTED:{
                    Toast.makeText(AllActivity.this,"请连接蓝牙",Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    };

    private void onTimerDeal(){
        mGetBuff = VideoDecoder.addupArray(mGetBuff,mBuffStream.toByteArray());
        mBuffStream.reset();
        mGetBuff = decodeDataByteTeacher(mGetBuff);
        if(isFragmentShow(mTeacherFragment)){
            mTeacherFragment.messageInterface(mTeacherFragment.MSG_UPDATE_TV);
            mTeacherFragment.messageInterface(mTeacherFragment.MSG_TIMMER);
        }
       // mGetBuff = decodeDataByte(mGetBuff);
//        Log.d(TAG,"timer deal");
        updateCameraImageFragment(CameraImageFragment.MSG_TIMER);//刷新cameraimagefragment的ui
//        mCameraImageFragment.messageInterface(CameraImageFragment.MSG_TIMER);
      //  sendWave();
    }
    //--------------------------------蓝牙连接处理开始-----------------------------------
    final int BT_SEARCH_STATE_SEARCHING = 1;
    final int BT_SEARCH_STATE_IDLE = 2;
    private int mSearchingStage = BT_SEARCH_STATE_IDLE;
    private int mLastConnectionState = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG,"BT device found:"+device.getName());
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                    DeviceItemAdapter adapter = (DeviceItemAdapter)mLeftListView.getAdapter();
                    adapter.add(device);
                    adapter.notifyDataSetChanged();
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Log.d(TAG, "BT device discover started");
                mSearchingStage = BT_SEARCH_STATE_SEARCHING;
                updateLeftMenuUI();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d(TAG, "BT device discover final");
                mSearchingStage = BT_SEARCH_STATE_IDLE;
                updateLeftMenuUI();
            }else{
                Log.d(TAG, "BT device discover  unknow");
            }
        }
    };
    private void updateLeftListState(){

    }

    private void updateLeftList(){//更新左边的列表子项
        DeviceItemAdapter adapter = (DeviceItemAdapter) mLeftListView.getAdapter();
        adapter.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            for(BluetoothDevice device:pairedDevices){
                adapter.add(device);
            }
        }
        adapter.notifyDataSetChanged();
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        int hasPermission = ActivityCompat.checkSelfPermission(AllActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AllActivity.this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},0);
        } else {
            boolean ret = mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "BT device discover about to start: ret=" + ret);
        }
        //获取未配对设备
    }
    private void updateSharedPreferences(String addr){
        if(mSp!=null&&addr!=null&&!addr.equals(DEF_BT_VALUE)){
            SharedPreferences.Editor editor = mSp.edit();
            editor.putString(DEF_BT_KEY,addr);
            editor.commit();//apply()异步写
        }
        Log.d(TAG,"write to sp:"+addr);
    }

    private void updateLeftMenuUI() {

        if(mConnectionManager ==null||mTvBluetooth ==null||mLeftListView==null){
            return;
        }

        Log.d(TAG, "current BT ConnectState="+mConnectionManager.getState(mConnectionManager.getCurrentConnectState())
                +" ListenState="+mConnectionManager.getState(mConnectionManager.getCurrentListenState()));
        if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTED){
            mTvBluetooth.setText(R.string.connected);
//            mLeftListView.getAdapter().getView(mSelectedItemNum,null,mLeftListView).findViewById(R.id.device_name).setBackgroundColor(Color.rgb(100,100,0));
            mLeftListView.setEnabled(false);
            mLeftListView.setBackgroundResource(R.color.colorGrayWhite);
            mBtnLeftAbout.setBackgroundResource(R.color.colorGrayWhite);
            mTvBluetooth.setBackgroundResource(R.color.colorGrayWhite);
            mLeftLinearLayout.setBackgroundResource(R.color.colorGrayWhite);
            updateSharedPreferences(mConnectionManager.getLastConnectDeviceAddr());//往地址里写个东西
        }else if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTING){
            mTvBluetooth.setText(R.string.connecting);
            mLeftListView.setEnabled(true);
            mLeftListView.setBackgroundResource(R.color.colorDarkWhite);
            mBtnLeftAbout.setBackgroundResource(R.color.colorDarkWhite);
            mTvBluetooth.setBackgroundResource(R.color.colorDarkWhite);
            mLeftLinearLayout.setBackgroundResource(R.color.colorDarkWhite);
            if(mLastConnectionState==ConnectionManager.CONNECT_STATE_CONNECTED){
                mHandler.obtainMessage(MSG_UPDATE_STATE_CHANGE).sendToTarget();
            }
        }else if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_IDLE){
            mTvBluetooth.setText(R.string.idle);
            mLeftListView.setBackgroundResource(R.color.colorDarkWhite);
            mBtnLeftAbout.setBackgroundResource(R.color.colorDarkWhite);
            mTvBluetooth.setBackgroundResource(R.color.colorDarkWhite);
            mLeftLinearLayout.setBackgroundResource(R.color.colorDarkWhite);
            mLeftListView.setEnabled(true);
            if(mLastConnectionState==ConnectionManager.CONNECT_STATE_CONNECTED){
                mHandler.obtainMessage(MSG_UPDATE_STATE_CHANGE).sendToTarget();
            }
        }
        mLastConnectionState = mConnectionManager.getCurrentConnectState();
    }
    private void connectBluetooth(String deviceAddr){
        if(deviceAddr==null||deviceAddr.equals("")||deviceAddr.equals(DEF_BT_VALUE)){
            Log.d(TAG,"connect error:get invaild device addr"+deviceAddr);
            return;
        }
        if(mConnectionManager.getCurrentConnectState()==ConnectionManager.CONNECT_STATE_IDLE){
            Log.d(TAG,"connect to Bluetooth");
            mConnectionManager.connect(deviceAddr);
            mDeviceName = deviceAddr;
        }
    }
    //-----------------------------------蓝牙连接处理结束-------------------------------------------------------
    //----------------------------蓝牙数据处理部分---------------------------------------------
    int mCMReadDataCount = 0;
    final int BUFF_STREAM_LENGTH = 1024*1024;
    final int buffStreamLength = BUFF_STREAM_LENGTH;
    ConnectionManager.ConnectionListener mConnectionListener= new ConnectionManager.ConnectionListener(){
        @Override
        public void onConnectStateChange(int oldState, int state) {
            mHandler.obtainMessage(MSG_UPDATE_LEFT_MENU).sendToTarget();
            Log.d(TAG,"connect state change");
        }
        @Override
        public void onListenStateChange(int oldState, int state) {
            mHandler.obtainMessage(MSG_UPDATE_LEFT_MENU).sendToTarget();
            Log.d(TAG,"listen state change");
        }
        @Override
        public void onSendData(boolean suc, byte[] data) {
            if(!mConnectionManager.isConnectioned()&&mBluetoothDisconnected){
                mHandler.obtainMessage(MSG_SHOW_TOAST_NO_CONNECTED).sendToTarget();
                mBluetoothDisconnected = false;
            }
        }
        @Override
        public void onReadDate(byte[] data) {
            try {
                mBuffStream.write(data);
                mCMReadDataCount+= data.length;
                if(mCMReadDataCount>BUFF_STREAM_LENGTH/2){
                    byteFreshData();
                }
            } catch (IOException e) {
                Log.d(TAG,"get data error");
            }
        }
    } ;





    //---------------解码控制-----------------

    private boolean mDecodeMode = true;
//这边添加新元素要注意配置marks和switch力的东西
    //不变长的解码方法
    private byte[] decodeDataByteTeacher(byte[]data){
        TeacherCoder tc = new TeacherCoder();
        int count = 0;
        byte[] bytes = data;
        int startSite = 0;
        do{
            if(count++>100)break;
            startSite =TeacherDecoder.seachTeacherCoder(bytes,startSite,tc);
            if(tc!=null&&tc.useable){
                updateTeacherFragment(tc);
             //   startSite++;
            }
        }while(tc.useable);//
        bytes = cutByteShow(bytes,startSite,bytes.length);//清除一段 但是删不干净

        Log.d(TAG1,"teacher remain data size "+(data!=null?data.length:-1));
        return bytes;
    }
    private void teacherDeal(TeacherCoder tc){
        if(tc!=null&&tc.useable){
            int command = tc.getCommand();
            switch(tc.getCommand()){
                case 1:{

                    break;
                }
            }
        }
    }
    //变长的解码方法，

    private byte[] decodeDataByte(byte[] data){
//        if(data!=null)Log.d(TAG1,"decode data byte length "+data.length);
        long timeStart = System.currentTimeMillis();
        byte[] bytes = data;
        int marks[] = new int[6];
        marks[0] = CodeMark.MT_PARAMETER;
        marks[1] = CodeMark.MT_VIDEO;
        marks[2] = CodeMark.MT_WAVE;
        marks[3] = CodeMark.MT_VIDEO_MONO;
        marks[4] = CodeMark.MT_VIDEO_BINARY;
        marks[5] = CodeMark.MT_VIDEO_GRAY;
       // marks[6] = CodeMark.MT_TEACHER;//add
        if(data!=null&&data.length>0){
            BooleanFlag b = new BooleanFlag(false);
            if(mDecodeMode==false){
                b.setFlag(true);//不进下面的while
                bytes = cutByteShow(bytes,0,bytes.length);//全抛出去显示
                //  bytes = null;//全抛出去
                Log.d(TAG,"decode mode close");
            }
            while(b.getFlag()==false){
                int minMarkSite = VideoDecoder.getIndexOf(bytes,CodeMark.getCodeMark(marks[0]));
                int minType = CodeMark.MT_PARAMETER;
                for(int i=1;i<marks.length;i++){
                    int markSite = VideoDecoder.getIndexOf(bytes,CodeMark.getCodeMark(marks[i]));
                    if(markSite!=-1&&markSite<minMarkSite||minMarkSite==-1){
                        minMarkSite = markSite;//取个最小值
                        minType = marks[i];
                    }
                }
                Log.d(TAG,"decode start "+minMarkSite+" "+minType+ " data length"+bytes.length);
                if(minMarkSite==-1){//这里面没有完整的mark
                    int markSite =VideoDecoder.getLastIndexOf(bytes,new byte[]{'#'});
                    if(markSite!=-1&&markSite+marks.length>data.length){//#12 0 4 3  #123 0 4 4 1#12 1 4 4 1#123 1 4 5 //可能跟着一个不完整的mark
                        bytes = cutByteShow(bytes,0,markSite);
                        Log.d(TAG,"decode only get a single mark ");
                    }else{
                        Log.d(TAG,"decode did't get minMarkSite "+bytes.length);
                        bytes = cutByteShow(bytes,0,bytes.length);
                    }
                    break;
                }else{//有可选的标志位 丢处理
                    byte[] returnb ; //卡住返回true 采不到扣掉返回false 成功返回false
                    switch(minType){
                        case CodeMark.MT_PARAMETER:{
                            returnb = decodeParameter(bytes,b);
                            Log.d(TAG,"get decode param mark"+returnb.length);
                            break;
                        }
                        case CodeMark.MT_VIDEO:{
                            returnb = decodeVideo(bytes,b);
                            Log.d(TAG,"get decode video mark "+returnb.length);
                            break;
                        }
                        case CodeMark.MT_WAVE:{
                            returnb = decodeWave(bytes,b);
                            Log.d(TAG,"get decode wave mark"+returnb.length);
                            break;
                        }
                        case CodeMark.MT_VIDEO_BINARY:{
                            returnb = decodeVideoAll(bytes,b,RawImage.IMAGE_BINARY);
                            Log.d(TAG,"get decode wave mark"+returnb.length);
                            break;
                        }

                        case CodeMark.MT_VIDEO_GRAY:{
                            returnb = decodeVideoAll(bytes,b,RawImage.IMAGE_GRAY);
                            Log.d(TAG,"get decode wave mark"+returnb.length);
                            break;
                        }

                        default:{
                            returnb = bytes;
                        }
                    }
                    bytes = returnb;
                }
            }
            Log.d(TAG,"decode end "+(System.currentTimeMillis()-timeStart));
        }
        return bytes;
    }

    private byte[] cutByteShow(byte[] buff,int start,int end){
        byte[]b = VideoDecoder.cutByte(buff,start,end);//可为空
        byte[]bb = VideoDecoder.cleanBuff(buff,start,end);
        if(b!=null&&b.length>0){
            Log.d(TAG,"this part didn't finish");//下面这块是更新uart activity里的那个textView的
            //这边两行调用对面的handler传个数
            updateUartTextViewFragment(b);
            }
        return bb;
    }
    private void addupShowStringMG(byte[] b){//特殊实现
        if(b==null)Log.d(TAG,"error:get null b");
        updateCameraImageFragment(new UartItem(0,"",b,0));
       // mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_ADD_STRING,new UartItem(0,"",b,0));//addupShowString(b,"GBK");
    //    mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_UPDATE_TV);//mHandler.obtainMessage(MSG_UPDATE_TV).sendToTarget();//asynUpdataUI();

        updateCameraImageFragment();
    }

    /*
    * data 输入 mark 标志位 perSize 找几个框在里面的  只是找第一个的话填0 out标志位中间的东西 site 标志位位置
    * */
    private int decodeBytesByMarks(byte[]data,byte[]headMark,byte[]bodyMark,int perSize,Vector<byte[]>out,Vector<Integer>site){
        int start = VideoDecoder.getIndexOf(data,headMark) ;
        boolean getFlag = true;
        if(start!=-1){
            site.addElement(new Integer(start));
            start = start+headMark.length;
            for(int i = 0;i<perSize;i++){
                int markGetSite =  VideoDecoder.getIndexOf(data,bodyMark,start,data.length);
                if(markGetSite==-1){
                    return i;//把找到的跳出 返回值比数组大小小一维
                }else{
                    site.addElement(new Integer(markGetSite));
                    byte[] getData = VideoDecoder.cutByte(data,start,markGetSite);
                    out.addElement(getData);
                    start = markGetSite+ bodyMark.length;//更新开始位置
                }

            }
        }else{
            getFlag = false;
        }
        if(getFlag == true){
            return -1;//都找到
        }
        return -2;
    }
    private byte[] decodeParameter(byte[]in,BooleanFlag bool){
        byte[] data = in;
        byte[]headMark = CodeMark.getCodeMark(CodeMark.MT_PARAMETER);
        byte[]bodyMark = CodeMark.getCodeMark(CodeMark.MT_PARAMETER_A);
        int markSite = VideoDecoder.getIndexOf(data,headMark);
        Vector<byte[]>parameter = new Vector<>();
        Vector<Integer>site = new Vector<>();
        int markNum =  decodeBytesByMarks(data,headMark,bodyMark,3,parameter,site);
        if(markNum>-1) {//卡着
            //这里判断一下长度有没有问题 有的话跳掉
            //这边每一级都有可能要跳
            Log.d(TAG,"decode parameter waiting");
            bool.setFlag(true);
        }else if(markNum==-1){
            int dataLength = 0;
            try{
                dataLength = Integer.parseInt(new String(parameter.lastElement()));
                byte[] par = VideoDecoder.cutByte(data,site.lastElement()+headMark.length,site.lastElement()+headMark.length+dataLength);
                data = VideoDecoder.cleanBuff(data,0,site.lastElement()+headMark.length+dataLength);
                Log.d(TAG,"par size "+par.length+" paramenter size"+parameter.size());
                UartItem ui = new UartItem(VideoDecoder.TYPE_STRING,new String(parameter.elementAt(1),"GBK"),par,System.currentTimeMillis());//这儿有点问题
               // mHandler.obtainMessage(MSG_CHANGE_LIST,ui).sendToTarget();
                updateCameraImageFragment(ui);
//                mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_CHANGE_LIST,ui);
                bool .setFlag(false);
            }catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                data = VideoDecoder.cleanBuff(data,0,site.lastElement()+headMark.length);
            }catch (Exception e){
                if(parameter.size()>0)Log.d(TAG,"decode parameter error :change length error"+parameter.size()+"  "+new String(parameter.lastElement()));
                data = VideoDecoder.cleanBuff(data,0,site.lastElement()+headMark.length);
            }
        }else{// = -2
            //     Log.d(TAG,"decode parameter not fount");
            bool.setFlag(false);
        }
        return data;
    }
    private byte[] decodeWave(byte[]in,BooleanFlag bool){
        byte[] data = in;
        byte[]headMark = CodeMark.getCodeMark(CodeMark.MT_WAVE);
        byte[]bodyMark = CodeMark.getCodeMark(CodeMark.MT_WAVE_A);
        Vector<byte[]>parameter = new Vector<>();
        Vector<Integer>site = new Vector<>();
        //head type body length body mark data kram
        int markNum =  decodeBytesByMarks(data,headMark,bodyMark,2,parameter,site);
        if(markNum>-1) {//卡着
            //这里判断一下长度有没有问题 有的话跳掉
            //这边每一级都有可能要跳
            Log.d(TAG,"decode wave waiting");
            bool.setFlag(true);
        }else if(markNum==-1){
            int dataLength = 0;
            int itemLength = 0;
            try{
                itemLength = VideoDecoder.bytes2Int(parameter.elementAt(0));
                dataLength = VideoDecoder.bytes2Int(parameter.lastElement());
                dataLength +=4;
//                dataLength = Integer.parseInt(new String(parameter.lastElement()));
                byte[] par = VideoDecoder.cutByte(data,site.lastElement()+headMark.length,site.lastElement()+headMark.length+dataLength);
                data = VideoDecoder.cleanBuff(data,0,site.lastElement()+headMark.length+dataLength);
                Log.d(TAG,"par size "+par.length+" wave size"+parameter.size() + " itemlength "+itemLength);
                if(itemLength>0&&dataLength>4){
                    int lineNum = (dataLength-4)/itemLength;
                    float points[] = new float[lineNum];
                    int count = 0;
                    for(int i=2;i<dataLength-2;i= i+itemLength){
                        byte[] temp = new byte[itemLength];
                        for(int j=0;j<itemLength;++j){
                            temp[j] = par[i+j];
                        }
                        points[count++] = VideoDecoder.bytes2Int(temp);
                        Log.d(TAG,"points value"+points[count-1]);
                    }
                    if(count>0){
                       // mWavePlayerFragment.addListValueAddLine(points);
                        updateWaveFragment(points);
                    }

                }
//                UartItem ui = new UartItem(VideoDecoder.TYPE_STRING,new String(parameter.elementAt(1),"GBK"),par,System.currentTimeMillis());//这儿有点问题

          //      mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_CHANGE_LIST,ui);
                bool .setFlag(false);
            }/*catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                data = VideoDecoder.cleanBuff(data,0,site.lastElement()+headMark.length);
            }*/catch (Exception e){
                if(parameter.size()>0)Log.d(TAG,"decode parameter error :change length error"+parameter.size()+"  "+new String(parameter.lastElement()));
                data = VideoDecoder.cleanBuff(data,0,site.lastElement()+headMark.length);
            }
        }else{// = -2
            //     Log.d(TAG,"decode parameter not fount");
            bool.setFlag(false);
        }
        return data;
    }
    private byte[] decodeVideo(byte[]in,BooleanFlag bool){
        final int mountain_out_mark_size = 2;//山外标志位的大小 神经病啊 mountain out have mountain
        byte[]data = in;
        byte[]headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO);
        byte[]bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_A);
        int markSite = VideoDecoder.getIndexOf(data,headMark);
        Log.d(TAG,"getVideoPositon "+markSite);
        if(markSite!=-1){//卡着
            Log.d(TAG,"getMark"+markSite);
            data = cutByteShow(data,0,markSite);//这把前面部分先抛出去
            markSite = 0;//抛出去之后首地址变了
            int markEndSite =VideoDecoder.getIndexOf(data,bodyMark,markSite+headMark.length,data.length);
            if(markEndSite!=-1){//第二层找到了标志位
                byte[] cuted = VideoDecoder.cutByte(data,markSite+headMark.length,markEndSite);//没有#号
                String strlen = "";
                try {
                    strlen = new String(cuted,"GBK");
                    int length = Integer.parseInt(strlen);
                    Log.d(TAG,"get length"+length);
                    int dataLength = length+headMark.length+bodyMark.length+cuted.length+mountain_out_mark_size*2;
                    if(data.length>=dataLength){
                        Log.d(TAG,"getImage");
                        cuted = VideoDecoder.cutByte(data,markEndSite+bodyMark.length+mountain_out_mark_size,markEndSite+bodyMark.length+mountain_out_mark_size+length);

                         RawImage rawImage = new RawImage(RawImage.IMAGE_COLOR,80,60,cuted);
//                        mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_CHANGE_IMAGE_BY_RAW_IMAGE,rawImage);//updateImageByByte(cuted);
                        updateCameraImageFragment(rawImage);
                        Log.d(TAG,"decode bitmap success"+(cuted!=null));
                        Log.d(TAG,"decode video data length before clean"+data.length);
                        data = VideoDecoder.cleanBuff(data,0,markEndSite+bodyMark.length+mountain_out_mark_size*2+length);
                        Log.d(TAG,"decode video data length"+data.length);
                        if(cuted!=null&&cuted.length>0)Log.d(TAG,"getImage"+(int)cuted[0]+" "+(int)cuted[cuted.length-1]);
                    }else{
                        bool.setFlag(true);
                        Log.d(TAG,"waiting data mark"+" "+dataLength+" "+data.length);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }catch(Exception e){
                    Log.d(TAG,"integer change error"+strlen);
                    e.printStackTrace();
                    //如果没拿到 显示跳出
                    if(data!=null&&data.length>0)addupShowStringMG(data);
                    data = VideoDecoder.cleanBuff(data,0,data.length);
//                    mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_UPDATE_TV);//updateUI()
                    updateCameraImageFragment();
                }
            }else{//第二层 没有检测到第二个标志位 长度不够的话卡着 超出放掉
                if(markSite+headMark.length+10>data.length){//markSite+markLength + x = datalength 要求datalength-()<10
                    bool.setFlag(true);
                    Log.d(TAG,"waiting mark");
                }else{//过长的处理
                    bool.setFlag(false);
                    byte[] cuted =VideoDecoder.cutByte(data,0,markSite+headMark.length);
                    data=VideoDecoder.cleanBuff(data,0,markSite+headMark.length);//这个肯定会漏一点东西出去
//                    data=VideoDecoder.cleanBuff(data,0,data.length);//这个说不定会漏特别多的东西出去
                    if(cuted!=null&&cuted.length>0){
                        addupShowStringMG(cuted);
//                        addupShowString(cuted,"GBK");
//                        asynUpdataUI();
                        Log.d(TAG,"decode video break out "+cuted.length +" "+ data.length);
                    }
                }
            }
        }else{//第一层 没有找到标志位
//            addupShowString(data,"GBK");
            if(data!=null&data.length>0)addupShowStringMG(data);
            data = VideoDecoder.cleanBuff(data,0,data.length);
//            updataUI();
            bool.setFlag(false);
        }
        return data;
    }
    private byte[] decodeVideoAll(byte[]in,BooleanFlag bool,int type){
        final int mountain_out_mark_size = 2;//山外标志位的大小  mountain out have mountain
        byte[]headMark ;
        byte[]bodyMark ;
        switch(type){
            case RawImage.IMAGE_COLOR :{
                headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO);
                bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_A);
                break;
            }
            case RawImage.IMAGE_GRAY:{
                headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_GRAY);
                bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_GRAY_A);
                break;
            }
            case RawImage.IMAGE_BINARY :{
                headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_BINARY);
                bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_BINARY_A);
                break;
            }
            default:{
                Log.d(TAG,"decodeVideoAll error, invalid type"+type);
                return null;
            }
        }
        byte[]data = in;
        int markSite = VideoDecoder.getIndexOf(data,headMark);
        Log.d(TAG,"decodeVideoAll start");
        Log.d(TAG,"getVideoPositon "+markSite);
        if(markSite!=-1){//卡着
            Log.d(TAG,"getMark"+markSite);
            data = cutByteShow(data,0,markSite);//这把前面部分先抛出去
            markSite = 0;//抛出去之后首地址变了
            int markEndSite =VideoDecoder.getIndexOf(data,bodyMark,markSite+headMark.length,data.length);
            if(markEndSite!=-1){//第二层找到了标志位
                byte[] cuted = VideoDecoder.cutByte(data,markSite+headMark.length,markEndSite);//没有#号
                String strlen = "";
                try {
                    strlen = new String(cuted,"GBK");
                    int length = Integer.parseInt(strlen);
                    Log.d(TAG,"get length"+length);
                    int dataLength = length+headMark.length+bodyMark.length+cuted.length+mountain_out_mark_size*2;
                    if(data.length>=dataLength){
                        cuted = VideoDecoder.cutByte(data,markEndSite+bodyMark.length+mountain_out_mark_size,markEndSite+bodyMark.length+mountain_out_mark_size+length);//切掉山外的标志位
                        Log.d(TAG,"getImage "+(cuted!=null));
//                        mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_CHANGE_IMAGE_BY_BYTE,new UartItem(0,"",cuted,0));//updateImageByByte(cuted);
                        RawImage rawImage = new RawImage(type,80,60,cuted);
//                        mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_CHANGE_IMAGE_BY_RAW_IMAGE,rawImage);//updateImageByByte(cuted);
                        updateCameraImageFragment(rawImage);
                        Log.d(TAG,"decode bitmap success "+(cuted!=null?cuted.length:-1));
                        Log.d(TAG,"decode video data length before clean"+data.length);
                        data = VideoDecoder.cleanBuff(data,0,markEndSite+bodyMark.length+mountain_out_mark_size*2+length);
                        Log.d(TAG,"decode video data length"+data.length);
                        if(cuted!=null&&cuted.length>0)Log.d(TAG,"getImage"+(int)cuted[0]+" "+(int)cuted[cuted.length-1]);
                    }else{
                        bool.setFlag(true);
                        Log.d(TAG,"waiting data mark"+" "+dataLength+" "+data.length);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }catch(Exception e){
                    Log.d(TAG,"integer change error"+strlen);
                    e.printStackTrace();
                    //如果没拿到 显示跳出
                    if(data!=null&&data.length>0)addupShowStringMG(data);
                    data = VideoDecoder.cleanBuff(data,0,data.length);
//                    mCameraImageFragment.messageInterface(mCameraImageFragment.MSG_UPDATE_TV);//updateUI()
                    updateCameraImageFragment();
                }
            }else{//第二层 没有检测到第二个标志位 长度不够的话卡着 超出放掉
                if(markSite+headMark.length+10>data.length){//markSite+markLength + x = datalength 要求datalength-()<10
                    bool.setFlag(true);
                    Log.d(TAG,"waiting mark");
                }else{//过长的处理
                    bool.setFlag(false);
                    byte[] cuted =VideoDecoder.cutByte(data,0,markSite+headMark.length);
                    data=VideoDecoder.cleanBuff(data,0,markSite+headMark.length);//这个肯定会漏一点东西出去
//                    data=VideoDecoder.cleanBuff(data,0,data.length);//这个说不定会漏特别多的东西出去
                    if(cuted!=null&&cuted.length>0){
                        addupShowStringMG(cuted);
//                        addupShowString(cuted,"GBK");
//                        asynUpdataUI();
                        Log.d(TAG,"decode video break out "+cuted.length +" "+ data.length);
                    }
                }
            }
        }else{//第一层 没有找到标志位
//            addupShowString(data,"GBK");
            if(data!=null&data.length>0)addupShowStringMG(data);
            data = VideoDecoder.cleanBuff(data,0,data.length);
//            updataUI();
            bool.setFlag(false);
        }
        return data;
    }
    private void stopDecode(){
        mDecodeMode = false;
        Log.d(TAG,"stop decode"+mDecodeMode);
    }
    private void startDecode(){
        mDecodeMode = true;
        Log.d(TAG,"start decode "+mDecodeMode);
    }
    private void stopGetData(){

    }
    private void startGetData(){

    }
    private void byteFreshData(){//把数据丢到缓冲区 顺便decode
        byte[] buff = mBuffStream.toByteArray();
        mGetBuff = buff;
        mBuffStream.reset();
        //mHandler.obtainMessage(MSG_CHANGE_DATA).sendToTarget();
  //      mGetBuff = decodeDataByte(mGetBuff);
        Log.d(TAG,"refresh data cause by buffStream has half data");
    }

    //----------------------------蓝牙数据处理结束---------------------------------------------
    //---------------------------解码----------------------------------------------------------

    //----------------------------解码结束------------------------------------------------------
//----------------------------------隔壁接口的实现------------------------------------
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDecodeStateChange(boolean b) {

    }

    @Override
    public boolean getDecodeState() {
        return false;
    }


//---------------------其他一些测试用的东西-----------------------------------
private void initSensor(){

        mWavePlayerFragment.addLine("0");//不添加测试线条
        mWavePlayerFragment.addLine("1");
        mWavePlayerFragment.addLine("2");
    mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    mStartTime = System.currentTimeMillis();
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
                if(mWavePlayerFragment!=null){
                    float time = (System.currentTimeMillis()-mStartTime);
                    time/=1000;
                    mWavePlayerFragment.addListValue(0,time,x);
                    mWavePlayerFragment.addListValue(1,time,y);
                    mWavePlayerFragment.addListValue(2,time,z);
                }
                Log.d(TAG,"sensor value:"+x+" "+y+" "+z);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    private void sendWave(){
        byte[] headMark = CodeMark.getCodeMark(CodeMark.MT_WAVE);
        byte[] bodyMark = CodeMark.getCodeMark(CodeMark.MT_WAVE_A);
        byte[] data = new byte[]{0,1,2,3,4,5,6,7,8,9,10};
        Integer it;
        byte[] bb= new byte[2];
        int a = 64;
//        data = VideoDecoder.int2Bytes(a,VideoDecoder.INT16);
        mConnectionManager.sendData(headMark);
        mConnectionManager.sendData(VideoDecoder.int2Bytes(1,VideoDecoder.UINT8));
        mConnectionManager.sendData(bodyMark);
        mConnectionManager.sendData(VideoDecoder.int2Bytes(data.length,VideoDecoder.UINT32));
        mConnectionManager.sendData(bodyMark);
//        mConnectionManager.sendData(bb);
//        mConnectionManager.sendData(data);

        it = Integer.valueOf("03", 16);
        bb[0] = (byte) it.intValue();
        it = Integer.valueOf("FC", 16);
        bb[1] = (byte) it.intValue();
        mConnectionManager.sendData(bb);
        mConnectionManager.sendData(data);
        it = Integer.valueOf("FC", 16);
        bb[0] = (byte) it.intValue();
        it = Integer.valueOf("03", 16);
        bb[1] = (byte) it.intValue();
        mConnectionManager.sendData(bb);
    }

    private class GetDeviceTask extends AsyncTask<Void,Void,Void>{
        private String deviceId = "";
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG,"device task start");
            mSp = getSharedPreferences("kira",Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = mSp.edit();
            deviceId = mSp.getString(DEF_BT_KEY,DEF_BT_VALUE);
            Log.d(TAG,"device task get id:"+deviceId);
            publishProgress();
            return null;

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
//            mConnectionManager.connect(deviceId);
            Log.d(TAG,"get device id:"+deviceId);
            connectBluetooth(deviceId);
        }
    }
    private class TimerAsyncTask extends AsyncTask<Void,Void,Void> {
        private boolean running = true;
        private boolean pause = false;
        private long delayTime = 100;
        public void pause(){
            pause = true;
        }
        public void restart(){
            pause  =false;
        }
        public TimerAsyncTask (long time){
            if(time>0)delayTime = time;
        }
        public TimerAsyncTask(){
            super();
        }
        @Override
        protected Void doInBackground(Void... params) {

            while(running){
                try {
                    if(!pause){
                        publishProgress();
                    }
                    Thread.sleep(delayTime);
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
            boolean b = false;
            onTimerDeal();
            if(b){
                stopTask();
            }else{

            }
        }
    }
}




class AllFragmentAdapter extends FragmentStatePagerAdapter {
    List<String> titleList;
    List<Fragment> fragmentList;
    List<Bitmap> icnoList;
    public AllFragmentAdapter(FragmentManager fm, List<Fragment>fragmentList, List<String>titleList) {
        super(fm);
        this.fragmentList = fragmentList;
        this.titleList = titleList;
    }
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
    @Override
    public int getCount() {
        return fragmentList.size();
    }

}