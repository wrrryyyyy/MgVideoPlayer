
package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by aa on 2018/10/6.
 */

public class CameraImageFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "CameraImageFragment";
    private Activity mActivity;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
//-----------------------UI---------------------------------------------------------------------------------
    private LinearLayout mLlBackgrount;
    private TextView mTvShow ;
    private TextView mTvFront;
    private TextView mTvColor;
    private Button mBtnClean;
    private Button mBtnPause;
    private Button[] mBtnTest = new Button[5];
    private Button[] mBtnSeletor = new Button[5];
    private ListView mListView ;
    private List<UartItem> mUartList;
    private UartAdapter mUartAdapter;
    private ImageSelector mImageSelector;
    private RawImage mRawImage ;
    private String mGetString;
    private String mShowString = "";
    private ImageView mIvUart;
    private byte[] mGetBuff;
    final int buffStreamLength = 1024*1024;
    private ByteArrayOutputStream mBuffStream;
    long dataFlashTime = 0;
    long dataDisconnectionTime = 300;
    private Map<String,Vector<UartItem>> mUartItemMap;
    private ConnectionManager mConnectionManager ;
//-------------------------UI end--------------------------------------------------------------------------------------
    //------------没啥用的变量---------------
    private boolean mFragmentUseable  = false;
    public CameraImageFragment() {
        // Required empty public constructor
    }

    public static CameraImageFragment newInstance(String param1, String param2) {
        return newInstance(param1,param2,null);
    }
    public static CameraImageFragment newInstance(int lineNum) {
        List<List<PointValue>> lists = new ArrayList<>();
        for(int i=0;i<lineNum;++i){
            lists.add(new ArrayList<PointValue>());
        }
        return newInstance(lists);
    }
    public static CameraImageFragment newInstance(List<List<PointValue>>list){
        return newInstance("","",list);
    }

    public static CameraImageFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
        CameraImageFragment fragment = new CameraImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        Log.d(TAG,"fragment start");
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_camera_image, container, false);
//        View v = inflater.inflate(R.layout.activity_uart, container, false);
        initView(v);
        return v;
    }
    private void initView(View v){

        initButtons(v);
        initSelector(v);
        initUI(v);
        initListView(v);
        initDatas();
        initDataDecode();
        initControls();
        testButtonInit(v);
        seletorInit(v);
    }

    private void initDatas(){
        mUartItemMap = new HashMap<>();
    }
    private void initDataDecode(){
        mRawImage = new RawImage(RawImage.IMAGE_COLOR,80,60,null);
        mBuffStream = new ByteArrayOutputStream(buffStreamLength);
    }
    private void initControls(){
        mConnectionManager = ConnectionManager.getConnectionManager(null);
    }
    private void initSelector(View v){
        mLlBackgrount = (LinearLayout)v.findViewById(R.id.ll_camera_backgrount);
        mIvUart= (ImageView)v.findViewById(R.id.iv_uart);
        mIvUart.setOnTouchListener(imageTouchListener);
        mImageSelector = new ImageSelector();
    }
    private void initListView(View v){
        mListView = (ListView)v.findViewById(R.id.list_view_uart);
        mUartList = new ArrayList<UartItem>();
        mUartAdapter = new UartAdapter(mActivity,R.layout.uart_item,mUartList);
        mListView.setAdapter(mUartAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UartItem item = mUartList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                Vector<UartItem> v = mUartItemMap.get(item.name);
                final List<UartItem> list = new ArrayList<UartItem>();
                for(UartItem ui:v){
                    list.add(ui);
                }
                builder.setAdapter(new UartAdapter(mActivity, R.layout.uart_item, list), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mActivity,list.get(which).getValue(),Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
    private void initUI(View v){
        mTvShow = (TextView)v.findViewById(R.id.tv_uart);
        mTvFront = mTvShow;
        mTvColor =(TextView)v.findViewById(R.id.tv_uart_color);
        mTvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    private void initButtons(View v){
        mBtnClean = (Button)v.findViewById(R.id.btn_clean);
        mBtnPause = (Button)v.findViewById(R.id.btn_pause);
        mBtnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvFront.setText("");
                mShowString = "";
            }
        });
        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener.getDecodeState()){
                    mBtnPause.setText(R.string.restart);
                    stopDecode();
                }else{
                    mBtnPause.setText(R.string.pause);
                    startDecode();
                }
            }
        });
    }
    private void seletorInit(View v){
        mBtnSeletor[0] = (Button)v.findViewById(R.id.btn_im_up);
        mBtnSeletor[1] = (Button)v.findViewById(R.id.btn_im_left);
        mBtnSeletor[2] = (Button)v.findViewById(R.id.btn_im_right);
        mBtnSeletor[3] = (Button)v.findViewById(R.id.btn_im_down);
        mBtnSeletor[4] = (Button)v.findViewById(R.id.btn_im_close);
        mBtnSeletor[0].setOnClickListener(selectorListener);
        mBtnSeletor[1].setOnClickListener(selectorListener);
        mBtnSeletor[2].setOnClickListener(selectorListener);
        mBtnSeletor[3].setOnClickListener(selectorListener);
        mBtnSeletor[4].setOnClickListener(selectorListener);
        updateSelectorUI();
    }
    private void testButtonInit(View v){
        mBtnTest[0] = (Button)v.findViewById(R.id.btn_uart_1);
        mBtnTest[1] = (Button)v.findViewById(R.id.btn_uart_2);
        mBtnTest[2] = (Button)v.findViewById(R.id.btn_uart_3);
        mBtnTest[3] = (Button)v.findViewById(R.id.btn_uart_4);
        mBtnTest[4] = (Button)v.findViewById(R.id.btn_uart_5);
        mBtnTest[0].setOnClickListener(testButtonListener);
        mBtnTest[1].setOnClickListener(testButtonListener);
        mBtnTest[2].setOnClickListener(testButtonListener);
        mBtnTest[3].setOnClickListener(testButtonListener);
        mBtnTest[4].setOnClickListener(testButtonListener);
        mBtnTest[0].setVisibility(View.INVISIBLE);
        mBtnTest[1].setVisibility(View.INVISIBLE);
        mBtnTest[2].setVisibility(View.INVISIBLE);
        mBtnTest[3].setVisibility(View.INVISIBLE);
        mBtnTest[4].setVisibility(View.INVISIBLE);
    }

    View.OnTouchListener imageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(mImageSelector.getVisible()){
                switch(event.getAction()){
                    case MotionEvent.ACTION_MOVE:{
                    }
                    case MotionEvent.ACTION_UP:{
                        double posX = event.getX();
                        double posY = event.getY();
                        int maxX = v.getWidth();
                        int maxY = v.getHeight();
                        double rateX = posX/maxX;
                        double rateY = posY/maxY;
                        mImageSelector.selectorMoveTo(rateX,rateY);
                        break;
                    }
                    default:{
                        Log.d(TAG,"unget touch event"+event.getAction());//down 0
                    }
                }
            }else{
                Log.d(TAG,"on touch but not visible");
            }
//            //注：这两行写不写好像问题不大
//            android:focusable="true"
//            android:focusableInTouchMode="true"
            return true;//这边false会进入ontoucheven的
        }
    };
    View.OnClickListener selectorListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG,"get click");
            switch(v.getId()){
                case R.id.btn_im_up:{
                    mImageSelector.selectorMove(ImageSelector.MOVE_UP,10);
                    break;
                }
                case R.id.btn_im_down:{
                    mImageSelector.selectorMove(ImageSelector.MOVE_DOWN,10);
                    break;
                }
                case R.id.btn_im_left:{
                    mImageSelector.selectorMove(ImageSelector.MOVE_LEFT,10);
                    break;
                }
                case R.id.btn_im_right:{
                    mImageSelector.selectorMove(ImageSelector.MOVE_RIGHT,10);
                    break;
                }
                case R.id.btn_im_close:{
                    mImageSelector.visibleChange();
                    updateSelectorUI();
                    break;
                }
            }
        }
    };
    View.OnClickListener testButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_uart_1:{
                    sendBitMap();
                    break;
                }
                case R.id.btn_uart_2:{
                    try {
                        mConnectionManager.sendData(getString(R.string.text_string).getBytes("GBK"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case R.id.btn_uart_3:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    View view = View.inflate(mActivity,R.layout.pure_uart_view,null);
                    builder.setView(view)
                            .setTitle(R.string.uart_data);
                    final Button btnPause = (Button)view.findViewById(R.id.btn_pause);
                    Button btnClean = (Button)view.findViewById(R.id.btn_clean);
                    Button btnClose = (Button)view.findViewById(R.id.btn_close);
                    if(!mListener.getDecodeState()){
                        btnPause.setText(R.string.restart);
                    }
                    mTvFront = (TextView)view.findViewById(R.id.tv_uart_mega);
                    mTvFront.setMovementMethod(ScrollingMovementMethod.getInstance());
                    final AlertDialog alertDialog = builder.create();
                    btnPause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //这两个待实现
                            if(mListener.getDecodeState()==true){
                                stopDecode();
                                btnPause.setText(R.string.restart);//状态改变
                            }else{
                                startDecode();
                                btnPause.setText(R.string.pause);
                            }
                        }
                    });
                    btnClean.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mTvFront.setText("");
                        }
                    });
                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mTvFront = mTvShow;//结束的时候把更新的textView 换回去
                            updateViewUI();
                        }
                    });
                    alertDialog.show();
                    break;
                }
                case R.id.btn_uart_4:{
//                    sendParameter("lalala","wsnyy".getBytes());
//                    sendWave();
                    sendMonoBitMap();
                    break;
                }
                case R.id.btn_uart_5:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    View sendMessageView = View.inflate(mActivity,R.layout.message_send_view,null);
                    builder.setTitle(R.string.uart_send)
                            .setIcon(R.mipmap.ic_launcher_round)
                            .setView(sendMessageView);
                    final EditText messageEditText = (EditText) sendMessageView.findViewById(R.id.msg_editor);
                    Button btnTest1 = (Button)sendMessageView.findViewById(R.id.btn_message_test_1);
                    Button btnTest2 = (Button)sendMessageView.findViewById(R.id.btn_message_test_2);
                    Button btnTest3 = (Button)sendMessageView.findViewById(R.id.btn_message_test_3);
                    Button btnTest4 = (Button)sendMessageView.findViewById(R.id.btn_message_test_4);
                    Button btnTest5 = (Button)sendMessageView.findViewById(R.id.btn_message_test_5);
                    Button btnTest6 = (Button)sendMessageView.findViewById(R.id.btn_message_test_6);
                    Button btnSend = (Button)sendMessageView.findViewById(R.id.btn_send);
                    btnTest2.setText("X");
                    final AlertDialog alertDialog = builder.create();
                    btnSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String s = messageEditText.getText().toString();
                            messageEditText.setText("");
                            mConnectionManager.sendData(s.getBytes());
                        }
                    });
                    btnTest6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mActivity,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
                        }
                    });
                    btnTest5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mActivity,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
                        }
                    });
                    btnTest4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendParameter("lululu","nswsz".getBytes());
                        }
                    });
                    btnTest3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendParameter("lololo","nswez".getBytes());
                        }
                    });
                    btnTest2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    btnTest1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mActivity,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
                        }
                    });
                    alertDialog.show();
                    //Toast.makeText(UartActivity.this,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    };


    @Override
    void loadData() {
        mFragmentUseable = true;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }else{
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mActivity = (Activity)context;
    }
    private void stopDecode(){
        mListener.onDecodeStateChange(false);
    }
    private void startDecode(){
        mListener.onDecodeStateChange(true);
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
        void onDecodeStateChange(boolean b);
        boolean getDecodeState();
    }
//------------------接口-------------------------------------
    public void messageInterface(int i){
//        Log.d(TAG+"All","get message "+mFragmentUseable);
        if(mFragmentUseable)mHandler.obtainMessage(i).sendToTarget();//很多东西没初始化之前瞎送message会出bug
    }
    public void messageInterface(int i, Object obj){
        if(mFragmentUseable&&obj!=null)mHandler.obtainMessage(i,obj).sendToTarget();
        Log.d(TAG+"All","get message obj "+mFragmentUseable+" "+i);
    }
//------------------UI update-------------------------
    public static final int MSG_TIMER = 0;
    public static final int MSG_CHANGE_DATA = 1;
    public static final int MSG_CHANGE_LIST = 2;
    public static final int MSG_UPDATE_TV = 3;
    public static final int MSG_UPDATE_IV = 4;
    public static final int MSG_ADD_STRING = 9;
    public static final int MSG_CHANGE_IMAGE_BY_BYTE = 12;
    public static final int MSG_CHANGE_IMAGE_BY_RAW_IMAGE = 13;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_CHANGE_LIST:{
                    UartItem data = (UartItem) msg.obj;
                    if(data==null)break;
                    dealParameter(data);
                    Log.d(TAG,"uartlist size"+mUartList.size());
                    UartAdapter adapter = (UartAdapter) mListView.getAdapter();//这边原因不明的发生过一次bug
                    if(adapter==null)Log.d(TAG,"uartlist size"+mUartList.size());
                    adapter.notifyDataSetChanged();
                    break;
                }
                case MSG_UPDATE_TV:{
                    updataUI();
                    break;
                }
                case MSG_TIMER:{
                    onTimerUI();
                    break;
                }
                case MSG_UPDATE_IV:{
                    updateImageView();
                    updateColorTV();
                    break;
                }
                case MSG_ADD_STRING:{
                    UartItem data = (UartItem) msg.obj;
                    if(data!=null){
                        byte[] b = data.value;
                        addupShowString(b,"GBK");
                    }
                    break;
                }
                case MSG_CHANGE_IMAGE_BY_BYTE:{
                    UartItem data = (UartItem) msg.obj;
                    if(data!=null) {
                        byte[] b = data.value;
                        updateImageByByte(b);
                    }
                    break;
                }
                case MSG_CHANGE_IMAGE_BY_RAW_IMAGE:{
                    Log.d(TAG,"handle get raw image");
                    RawImage data = (RawImage) msg.obj;
                    if(data!=null&&data.getRawData()!=null) {
                        updateImageByRawImage(data);
                    }else{
                        Log.d(TAG,"handle get raw image,data is null"+(data==null));
                    }
                    break;
                }
            }
        }
    };
    private void onTimerUI(){
//        Log.d(TAG,"TIMER RUN ONCE");
        if(mImageSelector.needFresh()||mImageSelector.needFreshColor()){
            mHandler.obtainMessage(MSG_UPDATE_IV).sendToTarget();//刷新光标
        }
    }


    private void updataUI(){
        mTvFront.setText(mShowString);
        int offset = mTvFront.getLineCount()*mTvFront.getLineHeight()-mTvFront.getHeight();//拿到文字的长度
        if(offset>0){
            mTvFront.scrollTo(0,offset>4?offset-4:offset);//这个函数是view的 用来滑动view里面的元素指定坐标
        }//Log.d(TAG,mShowString);
    }

    private void updateViewUI(){
        if(mListener.getDecodeState()){
            mBtnPause.setText(R.string.pause);//状态不改变
            stopDecode();
        }else{
            mBtnPause.setText(R.string.restart);
            startDecode();
        }
        updataUI();
    }
    private void updateImageView(){
        if(mRawImage.useable){
//            Bitmap bm =VideoDecoder.decodeColorByByte(mRawImage.getRawData(),new Size(80,60),mImageSelector);
            Bitmap bm =VideoDecoder.creatBitmapByByte(mRawImage.getType(),mRawImage.getRawData(),new Size(mRawImage.col,mRawImage.row),mImageSelector);
            mIvUart.setImageBitmap(bm);
            Bitmap bbm = ((BitmapDrawable)(mIvUart.getDrawable())).getBitmap();
            Log.d(TAG,"bbm size:"+bbm.getHeight()+" "+bbm.getWidth()+" bm size "+bm.getHeight()+" "+bm.getWidth());
        }
    }
    private void updateSelectorUI(){
        if(mImageSelector.getVisible()){
            for(int i =0;i<4;i++){
                mBtnSeletor[i] .setEnabled(true);
            }
            updateSelectorButton(true);
//            mBtnSeletor[4].setText("On");
//            mBtnSeletor[4].setBackgroundColor(getResources().getColor(R.color.colorGray));
        }else{
            for(int i =0;i<4;i++){
                mBtnSeletor[i] .setEnabled(false);
                updateSelectorButton(false);
            }
        }
    }
    private void updateSelectorButton(boolean open){
        if(open){
            mBtnSeletor[4].setBackgroundResource(R.drawable.ic_selector_black);//这个是旧版的 用ON off判断

        }else{
            mBtnSeletor[4].setBackgroundResource(R.drawable.ic_selector);//这个是旧版的 用ON off判断

        }
//        mBtnSeletor[4].setText("Off");//这个是旧版的 用ON off判断
//        mBtnSeletor[4].setBackgroundColor(getResources().getColor(R.color.colorWhite));
    }
    private void updateColorTV(){
        if(mRawImage.useable){
            double pos[] = mImageSelector.getSelectorPos();
            int posX = (int)(mRawImage.col*pos[0]);
            int posY = (int)(mRawImage.row*pos[1]);
            byte[] data = mRawImage.getPointData(posX,posY);
            int[] dataColor  = new int[]{0,0,0} ;
            if(mRawImage.getType()==RawImage.IMAGE_COLOR){
                dataColor= VideoDecoder.rgb565to888(data[0],data[1]);
            }
            else if(mRawImage.getType()==RawImage.IMAGE_BINARY){
                dataColor= VideoDecoder.rgb8to888(data[0]);
            }
            else if(mRawImage.getType()==RawImage.IMAGE_GRAY){
                dataColor= VideoDecoder.rgb8to888(data[0]);

            }

            String rawData = "";
            Log.d(TAG,"data size"+data.length+" "/*+data[0]+" "+data[1]*/);
            rawData = VideoDecoder.byte2HexString(data);
            int colorR = mImageSelector.getColor(0);
            int colorG = mImageSelector.getColor(1);
            int colorB = mImageSelector.getColor(2);
            mTvColor.setText("位置："+posX+","+posY+" 源数据 "+rawData+" 颜色值 "+colorR+","+colorG+","+colorB);
            mTvColor.setBackgroundColor(Color.rgb(colorR,colorG,colorB));
            mTvColor.setTextColor(Color.rgb(255-colorR,255-colorG,255-colorB));
            mImageSelector.colorRefresh();
//            mTvFront.setBackgroundColor(Color.rgb(dataColor[0],dataColor[1],dataColor[2]));
            mTvFront.setBackgroundColor(Color.rgb(colorR,colorG,colorB));
            mLlBackgrount.setBackgroundColor(Color.rgb(colorR,colorG,colorB));
            mTvFront.setTextColor(Color.rgb(255-colorR,255-colorG,255-colorB));

        }
    }
    private void asynUpdataUI(){
        mHandler.obtainMessage(MSG_UPDATE_TV).sendToTarget();
    }
    private void addupShowString(String s){
        mShowString  += s;
    }
    private void addupShowString(byte[] data,String encode){
        try {
            addupShowString(new String(data,encode));
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG,"error encode");
        }
    }
    private void dealParameter(UartItem uit){
        if(mUartItemMap.containsKey(uit.name)){
            Vector<UartItem>vector = mUartItemMap.get(uit.name);
            vector.addElement(uit);
        }else{//往里头加东西
            Vector<UartItem>vector = new Vector<>();
            vector.addElement(uit);
            mUartItemMap.put(uit.name,vector);
        }
        boolean flag = true;
        for(int i=0;i<mUartList.size();i++){
            if(uit.name.equals(mUartList.get(i).name)){
                flag = false;
                mUartList.set(i,uit);//替换
                Log.d(TAG,"dealParameter swift uartItem");
                break;
            }
        }
        if(flag ==true){//列表里没有
            mUartList.add(uit);
        }
        Toast.makeText(mActivity,uit.name,Toast.LENGTH_SHORT).show();
        //mUartList.add(uit);
    }
    private void removeUartItem(UartItem uit){
        if(mUartItemMap.containsKey(uit.name)){
            mUartItemMap.remove(uit.name);
        }
    }
    //2018年10月10日01点08分 加个更新不一样的数据的
    private void updateImageByRawImage(RawImage rawImage){
        Log.d(TAG,"decode raw image start");
        mRawImage.imageChange(rawImage.getType(),rawImage.col,rawImage.row);
        mRawImage.setNewSize(rawImage.col,rawImage.row);
        mRawImage.setData(rawImage.getRawData());
        Bitmap bm =VideoDecoder.creatBitmapByByte(rawImage.getType(),rawImage.getRawData(),new Size(rawImage.col,rawImage.row),mImageSelector);//那么这里为什么size要反过来定义呢..
        mIvUart.setImageBitmap(bm);
        Log.d(TAG,"decode bitmap success");
    }
    private void updateImageByByte(byte[] cuted){
        mRawImage.setNewSize(80,60);
        mRawImage.setData(cuted);
        Bitmap bm =VideoDecoder.decodeColorByByte(cuted,new Size(80,60),mImageSelector);
        mIvUart.setImageBitmap(bm);
        Log.d(TAG,"decode bitmap success");
    }


    private boolean mBitMapMark = true;
    private void sendParameter(){
        sendParameter("name","wsnbb".getBytes());
    }
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
    int sendBitMapStep = 0;
    private void sendBitMapStep(){
        sendBitMapStep ++;
        InputStream is = null ;
        Bitmap bm = null;
        if(mBitMapMark){
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70473577);
        }else{
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70258811);
        }
        byte[] headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO);
        byte[] bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_A);
        Vector<Integer>v = new Vector<Integer>();
        v =VideoDecoder.decodeColor(bm,new Size(80,60),null);
        int vvSize = v.size();
        byte b[] = new byte[vvSize];
        for (int i = 0; i < vvSize; i++) {
            b[i] = (byte) v.get(i).intValue();
        }
        byte bb[] = new byte[2];
        Integer it = Integer.valueOf("01", 16);
        if(sendBitMapStep==1){
            mConnectionManager.sendData(headMark);
            mConnectionManager.sendData(new String(""+b.length).getBytes());
            mConnectionManager.sendData(bodyMark);
        }
        if(sendBitMapStep==2){
            it = Integer.valueOf("01", 16);
            bb[0] = (byte) it.intValue();
            it = Integer.valueOf("FE", 16);
            bb[1] = (byte) it.intValue();
            //    sendMessage(bb);
            mConnectionManager.sendData(bb);
            boolean ans = mConnectionManager.sendData(b);
        }
        if(sendBitMapStep == 3){
            it = Integer.valueOf("FE", 16);
            bb[0] = (byte) it.intValue();
            it = Integer.valueOf("01", 16);
            bb[1] = (byte) it.intValue();
            mConnectionManager.sendData(bb);

            mBitMapMark = !mBitMapMark;
            Log.d(TAG,"Photo chhange");
            sendBitMapStep = 0;
        }

    }
    private void sendMonoBitMap(){//测试
        InputStream is = null ;
        Bitmap bm = null;
        if(mBitMapMark){
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70473577);
        }else{
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70258811);
        }

        byte[] headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_BINARY);
        byte[] bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_BINARY_A);
//        byte[] headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_MONO);
//        byte[] bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_MONO_A);
        Vector<Integer>v = new Vector<Integer>();
        v =VideoDecoder.decodeBinary(bm,new Size(80,60),null);
        int vvSize = v.size();
        byte b[] = new byte[vvSize];
        for (int i = 0; i < vvSize; i++) {
            b[i] = (byte) v.get(i).intValue();
        }
        mConnectionManager.sendData(headMark);
        mConnectionManager.sendData(new String(""+b.length).getBytes());
        mConnectionManager.sendData(bodyMark);
        byte bb[] = new byte[2];
        Integer it = Integer.valueOf("01", 16);
        bb[0] = (byte) it.intValue();
        it = Integer.valueOf("FE", 16);
        bb[1] = (byte) it.intValue();
        //    sendMessage(bb);
        mConnectionManager.sendData(bb);
        boolean ans = mConnectionManager.sendData(b);;
        it = Integer.valueOf("FE", 16);
        bb[0] = (byte) it.intValue();
        it = Integer.valueOf("01", 16);
        bb[1] = (byte) it.intValue();
        mConnectionManager.sendData(bb);
        mBitMapMark = !mBitMapMark;
    }
    private void sendBitMap(){//测试
        InputStream is = null ;
        Bitmap bm = null;
        if(mBitMapMark){
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70473577);
        }else{
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70258811);
        }

        byte[] headMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO);
        byte[] bodyMark = CodeMark.getCodeMark(CodeMark.MT_VIDEO_A);
//        mark[0] = '#';
//        mark[1] = 0x12;
//        mark[2] = 0x34;
//        mark[3] = 0x56;

        Vector<Integer>v = new Vector<Integer>();
        v =VideoDecoder.decodeColor(bm,new Size(80,60),null);
        int vvSize = v.size();
        byte b[] = new byte[vvSize];
        for (int i = 0; i < vvSize; i++) {
            b[i] = (byte) v.get(i).intValue();
        }
        mConnectionManager.sendData(headMark);
        mConnectionManager.sendData(new String(""+b.length).getBytes());
        mConnectionManager.sendData(bodyMark);
        byte bb[] = new byte[2];
        Integer it = Integer.valueOf("01", 16);
        bb[0] = (byte) it.intValue();
        it = Integer.valueOf("FE", 16);
        bb[1] = (byte) it.intValue();
        //    sendMessage(bb);
        mConnectionManager.sendData(bb);
        boolean ans = mConnectionManager.sendData(b);;
        it = Integer.valueOf("FE", 16);
        bb[0] = (byte) it.intValue();
        it = Integer.valueOf("01", 16);
        bb[1] = (byte) it.intValue();
        mConnectionManager.sendData(bb);
        mBitMapMark = !mBitMapMark;
    }
    private void sendParameter(String name,byte[] data){
        //mark name mark type mark length mark
        byte[] b = CodeMark.getCodeMark(CodeMark.MT_PARAMETER);
        byte[] bodyMark = CodeMark.getCodeMark(CodeMark.MT_PARAMETER_A);
        mConnectionManager.sendData(b);
        mConnectionManager.sendData("1".getBytes());//目前没奇怪的解码 于是这个属性没啥用
        mConnectionManager.sendData(bodyMark);
        mConnectionManager.sendData(name.getBytes());
        mConnectionManager.sendData(bodyMark);
        mConnectionManager.sendData(new String(""+data.length).getBytes());
        mConnectionManager.sendData(bodyMark);
        mConnectionManager.sendData(data);
    }




    private class BooleanFlag{
        private Boolean flag ;
        BooleanFlag(Boolean flag){
            this.flag = flag;
        }

        public void setFlag(Boolean b) {
            this.flag = b;
        }

        public Boolean getFlag() {
            return flag;
        }
    }
}
