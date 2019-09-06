package com.wrrryyyy.www.mgvideoplayer;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.opencv.core.CvType.CV_8UC3;

public class UartActivity extends AppCompatActivity {
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
    String mGetString = "";
    String mShowString = "";
    ImageView mIvUart;
    byte[] mGetBuff;
    final int buffStreamLength = 1024*1024;
    ByteArrayOutputStream mBuffStream;
    long dataFlashTime = 0;
    long dataDisconnectionTime = 300;
    final String TAG = "UART";
    final String TAG1 = "TIMER_UAR";
    boolean mDecodeMode = true;
    private Map<String,Vector<UartItem>> mUartItemMap;
    ConnectionManager mConnectionManager ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);
        initButtons();
        initSelector();
        initUI();
        initListView();
        initDatas();
        initDataDecode();
        initControls();
        testButtonInit();
        seletorInit();
        mHandler.sendEmptyMessage(MSG_TIMER);
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
        mConnectionManager.setListener(cl);
    }
    private void initSelector(){
        mIvUart= (ImageView)findViewById(R.id.iv_uart);
        mIvUart.setOnTouchListener(imageTouchListener);
        mImageSelector = new ImageSelector();
    }
    private void initListView(){
        mListView = (ListView)findViewById(R.id.list_view_uart);
        mUartList = new ArrayList<UartItem>();
        mUartAdapter = new UartAdapter(this,R.layout.uart_item,mUartList);
        mListView.setAdapter(mUartAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UartItem item = mUartList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(UartActivity.this);
                Vector<UartItem> v = mUartItemMap.get(item.name);
                final List<UartItem> list = new ArrayList<UartItem>();
                for(UartItem ui:v){
                    list.add(ui);
                }
                builder.setAdapter(new UartAdapter(UartActivity.this, R.layout.uart_item, list), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(UartActivity.this,list.get(which).getValue(),Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
    private void initUI(){
        mTvShow = (TextView)findViewById(R.id.tv_uart);
        mTvFront = mTvShow;
        mTvColor =(TextView)findViewById(R.id.tv_uart_color);
        mTvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    private void initButtons(){
        mBtnClean = (Button)findViewById(R.id.btn_clean);
        mBtnPause = (Button)findViewById(R.id.btn_pause);
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
                if(mDecodeMode){
                    mBtnPause.setText(R.string.restart);
                    stopDecode();
                }else{
                    mBtnPause.setText(R.string.pause);
                    startDecode();
                }
            }
        });
    }
    private void seletorInit(){
        mBtnSeletor[0] = (Button)findViewById(R.id.btn_im_up);
        mBtnSeletor[1] = (Button)findViewById(R.id.btn_im_left);
        mBtnSeletor[2] = (Button)findViewById(R.id.btn_im_right);
        mBtnSeletor[3] = (Button)findViewById(R.id.btn_im_down);
        mBtnSeletor[4] = (Button)findViewById(R.id.btn_im_close);
        mBtnSeletor[0].setOnClickListener(selectorListener);
        mBtnSeletor[1].setOnClickListener(selectorListener);
        mBtnSeletor[2].setOnClickListener(selectorListener);
        mBtnSeletor[3].setOnClickListener(selectorListener);
        mBtnSeletor[4].setOnClickListener(selectorListener);
        updateSelectorUI();
    }
    private void testButtonInit(){
        mBtnTest[0] = (Button)findViewById(R.id.btn_uart_1);
        mBtnTest[1] = (Button)findViewById(R.id.btn_uart_2);
        mBtnTest[2] = (Button)findViewById(R.id.btn_uart_3);
        mBtnTest[3] = (Button)findViewById(R.id.btn_uart_4);
        mBtnTest[4] = (Button)findViewById(R.id.btn_uart_5);
        mBtnTest[0].setOnClickListener(testButtonListener);
        mBtnTest[1].setOnClickListener(testButtonListener);
        mBtnTest[2].setOnClickListener(testButtonListener);
        mBtnTest[3].setOnClickListener(testButtonListener);
        mBtnTest[4].setOnClickListener(testButtonListener);
    }
    View.OnTouchListener imageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG,"get touch event"+event.getAction());//down 0
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
                        mConnectionManager.sendData(getResources().getString(R.string.text_string).getBytes("GBK"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case R.id.btn_uart_3:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(UartActivity.this);
                    View view = View.inflate(UartActivity.this,R.layout.pure_uart_view,null);
                    builder.setView(view)
                            .setTitle(R.string.uart_data);
                    final Button btnPause = (Button)view.findViewById(R.id.btn_pause);
                    Button btnClean = (Button)view.findViewById(R.id.btn_clean);
                    Button btnClose = (Button)view.findViewById(R.id.btn_close);
                    if(!mDecodeMode){
                        btnPause.setText(R.string.restart);
                    }
                    mTvFront = (TextView)view.findViewById(R.id.tv_uart_mega);
                    mTvFront.setMovementMethod(ScrollingMovementMethod.getInstance());
                    final AlertDialog alertDialog = builder.create();
                    btnPause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //这两个待实现
                            if(mDecodeMode==true){
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
                    sendParameter("lalala","wsnyy".getBytes());
                    break;
                }
                case R.id.btn_uart_5:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(UartActivity.this);
                    View sendMessageView = View.inflate(UartActivity.this,R.layout.message_send_view,null);
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
                            Toast.makeText(UartActivity.this,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
                        }
                    });
                    btnTest5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(UartActivity.this,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UartActivity.this,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
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
    private void updataUI(){
        mTvFront.setText(mShowString);
        int offset = mTvFront.getLineCount()*mTvFront.getLineHeight()-mTvFront.getHeight();//拿到文字的长度
        if(offset>0){
            mTvFront.scrollTo(0,offset);//这个函数是view的 用来滑动view里面的元素指定坐标
        }//Log.d(TAG,mShowString);
    }
    private void updateViewUI(){
        if(mDecodeMode){
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
            Bitmap bm =VideoDecoder.decodeColorByByte(mRawImage.getRawData(),new Size(80,60),mImageSelector);
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
            mBtnSeletor[4].setText("On");
            mBtnSeletor[4].setBackgroundColor(getResources().getColor(R.color.colorGray));
        }else{
            for(int i =0;i<4;i++){
                mBtnSeletor[i] .setEnabled(false);
            }
            mBtnSeletor[4].setText("Off");
            mBtnSeletor[4].setBackgroundColor(getResources().getColor(R.color.colorWhite));
        }
    }
    private void updateColorTV(){
        if(mRawImage.useable){
            double pos[] = mImageSelector.getSelectorPos();
            int posX = (int)(mRawImage.col*pos[0]);
            int posY = (int)(mRawImage.row*pos[1]);
            byte[] data = mRawImage.getPointData(posX,posY);
            int[] dataColor = VideoDecoder.rgb565to888(data[0],data[1]);
            mTvFront.setBackgroundColor(Color.rgb(dataColor[0],dataColor[1],dataColor[2]));
            String rawData = "";
            Log.d(TAG,"data size"+data.length+" "+data[0]+" "+data[1]);
            rawData = VideoDecoder.byte2HexString(data);
            int colorR = mImageSelector.getColor(0);
            int colorG = mImageSelector.getColor(1);
            int colorB = mImageSelector.getColor(2);
            mTvColor.setText("位置："+posX+","+posY+" 源数据 "+rawData+" 颜色值 "+colorR+","+colorG+","+colorB);
            mTvColor.setBackgroundColor(Color.rgb(colorR,colorG,colorB));
            mTvColor.setTextColor(Color.rgb(255-colorR,255-colorG,255-colorB));
            mImageSelector.colorRefresh();

        }
    }
    private void asynUpdataUI(){
        mHandler.obtainMessage(MSG_UPDATE_TV).sendToTarget();
    }
    private void updateImageByByte(byte[] cuted){
        mRawImage.setNewSize(80,60);
        mRawImage.setData(cuted);
        Bitmap bm =VideoDecoder.decodeColorByByte(cuted,new Size(80,60),mImageSelector);
        mIvUart.setImageBitmap(bm);
        Log.d(TAG,"decode bitmap success");
    }

    final int MSG_TIMER = 0;
    final int MSG_CHANGE_DATA = 1;
    final int MSG_CHANGE_LIST = 2;
    final int MSG_UPDATE_TV = 3;
    final int MSG_UPDATE_IV = 4;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_CHANGE_LIST:{
                    UartItem data = (UartItem) msg.obj;
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
                    Log.d(TAG,"in timer");
                    onTimerDeal();
                    mHandler.sendEmptyMessageDelayed(MSG_TIMER,25);//姑且先定200ms
                    break;
                }
                case MSG_UPDATE_IV:{
                    updateImageView();
                    updateColorTV();
                    break;
                }
            }
        }
    };
    private void onTimerDeal(){
        mGetBuff = VideoDecoder.addupArray(mGetBuff,mBuffStream.toByteArray());
        mBuffStream.reset();
        mGetBuff = decodeDataByte(mGetBuff);
        onTimerUI();
    }
    private void onTimerUI(){
        Log.d(TAG1,"TIMER RUN ONCE");
        if(mImageSelector.needFresh()||mImageSelector.needFreshColor()){
            mHandler.obtainMessage(MSG_UPDATE_IV).sendToTarget();//刷新光标
        }
    }
    private boolean mBitMapMark = true;
    private void sendParameter(){
        sendParameter("name","wsnbb".getBytes());
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
        byte[] headMark = getCodeMark(MT_VIDEO);
        byte[] bodyMark = getCodeMark(MT_VIDEO_A);
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
    private void sendBitMap(){//测试
        InputStream is = null ;
        Bitmap bm = null;
        if(mBitMapMark){
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70473577);
        }else{
            bm = BitmapFactory.decodeResource(this.getResources(),R.raw.p70258811);
        }

        byte[] headMark = getCodeMark(MT_VIDEO);
        byte[] bodyMark = getCodeMark(MT_VIDEO_A);
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
        byte[] b = getCodeMark(MT_PARAMETER);
        byte[] bodyMark = getCodeMark(MT_PARAMETER_A);
        mConnectionManager.sendData(b);
        mConnectionManager.sendData("1".getBytes());//目前没奇怪的解码 于是这个属性没啥用
        mConnectionManager.sendData(bodyMark);
        mConnectionManager.sendData(name.getBytes());
        mConnectionManager.sendData(bodyMark);
        mConnectionManager.sendData(new String(""+data.length).getBytes());
        mConnectionManager.sendData(bodyMark);
        mConnectionManager.sendData(data);
    }



    private void removeUartItem(UartItem uit){
        if(mUartItemMap.containsKey(uit.name)){
            mUartItemMap.remove(uit.name);
        }
    }
    private byte[] cutByteShow(byte[] buff,int start,int end){//这个有一半decode的 一半刷新的 mmp
        byte[]b = VideoDecoder.cutByte(buff,start,end);//可为空
        byte[]bb = VideoDecoder.cleanBuff(buff,start,end);
        if(b!=null&&b.length>0){
            addupShowString(b,"GBK");
            asynUpdataUI();
        }
        return bb;
    }

    /*
    * data 输入 mark 标志位 perSize 找几个框在里面的  只是找第一个的话填0 out标志位中间的东西 site 标志位位置
    * */
    private int decodeBytesByMarks(byte[]data,byte[]mark,int perSize,Vector<byte[]>out,Vector<Integer>site){
        return decodeBytesByMarks(data,mark,mark,perSize,out,site);
    }
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
    static final int MT_PARAMETER = 0;
    static final int MT_PARAMETER_A = 4;
    static final int MT_VIDEO = 1;
    static final int MT_VIDEO_A = 5;
    static final int MT_WAVE = 2;
    static final int MT_VIDEO_MONO = 6;
    static final int MT_VIDEO_MONO_A = 7;
    static final int MT_DEF = 3;
    private byte[] getCodeMark(int markType){
        byte[] b = new byte[4];
        switch(markType){//你有没有想过这么瞎取标志位之后怎么维护
            case MT_VIDEO:{//#123456
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x34;
                b[3] = 0x56;
                break;
            }
            case MT_PARAMETER:{//#234567
                b[0] = '#';
                b[1] = 0x23;
                b[2] = 0x45;
                b[3] = 0x67;
                break;
            }
            case MT_WAVE:{//#122334
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x23;
                b[3] = 0x34;
                break;
            }
            case MT_PARAMETER_A:{//123457
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x34;
                b[3] = 0x57;
                break;
            }
            case MT_VIDEO_A:{//234568
                b[0] = '#';
                b[1] = 0x23;
                b[2] = 0x45;
                b[3] = 0x68;
                break;
            }
            case MT_VIDEO_MONO:{//133456
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x34;
                b[3] = 0x56;
                break;
            }
            case MT_VIDEO_MONO_A:{//133457
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x34;
                b[3] = 0x57;
                break;
            }
            case MT_DEF:{}
            default:{
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x21;
                b[3] = 0x31;
            }
        }

        return b;
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
        Toast.makeText(UartActivity.this,uit.name,Toast.LENGTH_SHORT).show();
        //mUartList.add(uit);
    }
    private byte[] decodeParameter(byte[]in,BooleanFlag bool){
        byte[] data = in;
        byte[]headMark = getCodeMark(MT_PARAMETER);
        byte[]bodyMark = getCodeMark(MT_PARAMETER_A);
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
                mHandler.obtainMessage(MSG_CHANGE_LIST,ui).sendToTarget();
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
    private byte[] decodeMountainOutVideo(){
        return null;
    }
    private byte[] decodeVideoMono(byte[]in,BooleanFlag bool){
        byte[] data = in;
        byte[]headMark = getCodeMark(MT_PARAMETER);
        byte[]bodyMark = getCodeMark(MT_PARAMETER_A);
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
                // Toast.makeText(this,(new String(par))+" "+(new String(parameter.elementAt(0)))+" "+new String(parameter.elementAt(1))+" "+new String(parameter.elementAt(2)),Toast.LENGTH_SHORT);
                Log.d(TAG,"par size "+par.length+" paramenter size"+parameter.size());
                UartItem ui = new UartItem(VideoDecoder.TYPE_STRING,new String(parameter.elementAt(1),"GBK"),par,System.currentTimeMillis());//这儿有点问题

                mHandler.obtainMessage(MSG_CHANGE_LIST,ui).sendToTarget();
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
    /*
    * 有检测到或者处于这个状态 返true 否则返false 成功返回false
    * 状态是这样  每个方法去处理字串 如果没检查到东西 返回false 下一个处理 处理到最底下 如果有# 那么卡着不显示
    * 如果有检查到东西 如果满足给定的可能出现的状态 比如mark1 length 然后没到mark2这样 就卡着 返回true 期间可以去掉一部分字符抛出显示
    * 否则把不满足的都显示了 然后抛个false
    * 2018年9月13日 没有写只到一半卡着的
    * 逻辑是这样 第一次检测到 标志位前面的部分都抛了 第一次没有检测到 返回false
    * 第二次之后没有检测到 直接抛出检测位前面的部分 返回false
    * 全部数据通过 返回false
    * 每次检查到标志位之后小于一定长度之内没有检测到 返回true
    * */
    private byte[] decodeVideo(byte[]in,BooleanFlag bool){
        final int mountain_out_mark_size = 2;//山外标志位的大小 神经病啊 mountain out have mountain
        byte[]data = in;
        byte[]headMark = getCodeMark(MT_VIDEO);
        byte[]bodyMark = getCodeMark(MT_VIDEO_A);
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
                        updateImageByByte(cuted);
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
                    addupShowString(data,"GBK");
                    data = VideoDecoder.cleanBuff(data,0,data.length);
                    updataUI();
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
                    if(cuted!=null){
                        addupShowString(cuted,"GBK");
                        asynUpdataUI();
                        Log.d(TAG,"decode video break out "+cuted.length +" "+ data.length);
                    }
                }
            }
        }else{//第一层 没有找到标志位
            addupShowString(data,"GBK");
            data = VideoDecoder.cleanBuff(data,0,data.length);
            updataUI();
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
    private byte[] decodeDataByte(byte[] data){
        Log.d(TAG1,"decode data byte length "+data.length);
        long timeStart = System.currentTimeMillis();
//        messageDecoderTest(data);
        byte[] bytes = data;
        int marks[] = new int[3];
        marks[0] = MT_PARAMETER;
        marks[1] = MT_VIDEO;
        marks[2] = MT_WAVE;
      if(data!=null&&data.length>0){
          BooleanFlag b = new BooleanFlag(false);
          if(mDecodeMode==false){
            b.setFlag(true);//不进下面的while
              bytes = cutByteShow(bytes,0,bytes.length);//全抛出去显示
            //  bytes = null;//全抛出去
              Log.d(TAG,"decode mode close");
          }
          while(b.getFlag()==false){
              int minMarkSite = VideoDecoder.getIndexOf(bytes,getCodeMark(marks[0]));
              int minType = MT_PARAMETER;
              for(int i=1;i<marks.length;i++){
                  int markSite = VideoDecoder.getIndexOf(bytes,getCodeMark(marks[i]));
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
                      case MT_PARAMETER:{
                          returnb = decodeParameter(bytes,b);
                          Log.d(TAG,"decode param "+returnb.length);
                          break;
                      }
                      case MT_VIDEO:{
                          returnb = decodeVideo(bytes,b);
                          Log.d(TAG,"decode video "+returnb.length);
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
    private void byteFreshData(){
        byte[] buff = mBuffStream.toByteArray();
        mGetBuff = buff;
        mBuffStream.reset();
//        mHandler.obtainMessage(MSG_CHANGE_DATA).sendToTarget();
        mGetBuff = decodeDataByte(mGetBuff);

        Log.d(TAG,"refresh data cause by buffStream has half data");
    }
    /*
    * 2018年9月6日
    * wrrryyyyy
    * 把下位机的各种东西弄出来
    * 视频的目标信号： #123456length#234567山外标志位data(贼长)山外标志位 第一阶段读length 第二阶段一直读到快满足length的长度，
    *
    * */
//    protected void decodeString(){
//        final String videoMark = "#123456";
//        final String videoMarkA = "#234567";
//        final String MARK = "#";
//        final int MAX_VIDEO_LENGTH = 11;
//      //  final String markVideo = new String((byte[])(0x123456));
//        String s = mGetString;
//        int videoFromPos = s.indexOf(videoMark);
//        if(videoFromPos!=-1)mVideoMessage.levelUp();
//        if(mVideoMessage.getLevel() ==1){//取个length
//            //1 不到一定长度 保持 2 一定长没收到 爆掉 3 收到 进下一级
//            if(videoFromPos+videoMarkA.length()+videoMark.length()<s.length()){//保持
//                Log.d(TAG,"A keep");
//            }else if(videoFromPos+videoMark.length()*2+MAX_VIDEO_LENGTH<s.length()){//length太长
//                Log.d(TAG,"A");
//                mVideoMessage.relife();
//            }else{//等待收到
//                int videoLengthPos = s.indexOf(videoMarkA);
//                if(videoLengthPos !=-1){
//                    String length = s.substring(videoFromPos+videoMark.length(),videoLengthPos);
//                    Log.d(TAG,"video data size"+length);
//                    int videoLength = -1;
//                    try{
//                        videoLength = Integer.parseInt(length);
//                        mVideoMessage.setLength(videoLength);
//                        mVideoMessage.videoLengthEnd = videoLengthPos;
//                        mVideoMessage.levelUp();
//                    }catch (Exception e){
//                        Log.d(TAG,e.toString());
//                        videoLength = -1;
//                        mVideoMessage.relife();
//                    }
//                    if(videoLength<10)mVideoMessage.relife();
//                }
//
//            }
//        }
//        if(mVideoMessage.getLevel() ==2){//
//            //跳出条件：一定长度没收到标志位
//            if(mVideoMessage.videoLength == -1){
//                mVideoMessage.relife();
//            }else{
//                int length = mVideoMessage.videoLengthEnd+videoMarkA.length()+mVideoMessage.getLength()+2;//这个4是山外的标志位 再说了
//                if(s.length()<length){
//                    //保留
//                }else{
//                    byte bb[] = new byte[2];
//                    Integer it = Integer.valueOf("01", 16);
//                    bb[1] = (byte) it.intValue();
//                    it = Integer.valueOf("FE", 16);
//                    bb[0] = (byte) it.intValue();
//                    String mark = new String(bb);
//                    if(s.indexOf(mark,length)!=-1){
//                        mVideoMessage.levelUp();
//                        String data = s.substring(length-mVideoMessage.getLength(),length);
//                        dataString2Photo(data);
//                        Log.d(TAG,"level 2 success");
//                    }else{
//                        mVideoMessage.relife();
//                        Log.d(TAG,"level 2 can't found end mark");
//                    }
//                }
//            }
//
//        }
//        if(mVideoMessage.getLevel()==3){
//            mVideoMessage.relife();
//        }
//        if(mVideoMessage.getLevel() ==4){
//
//        }
//        if(mVideoMessage.getLevel()==0){
//            //正常情况送出
//            mShowString = mShowString + s;
//            mGetString = "";//感觉线程会出问题
//        }
//
//    }
    /*
    * 2018年9月2日
    * me
    * 这段是这样子 首先兼容山外的那两个标识符 第一位 1 ~1 data ~1 1 然后在前面加我们自己的标识符 0x1234567 lentgth 0x1234567 这样
    * 抓三次标志位 如果中间不满足任意条件则跳出 1 #后面没有跟着标识符 2 8位内没有找到第二个标识符
    * 3 标识符后面没有跟着长度（转换不出来） 4 一定长度之后找不到第四个标识符 5 1秒内没有继续收到数据 6 连接断开
    * 如果找到#而且没有跳出 那么先扣着不显示
    * 如果四个都满足了 那么把这段剪掉。
    * 然后跳出之后把之前攒下来的字符丢去显示
    * */
    int mCMReadDataCount = 0;
    ConnectionManager.ConnectionListener cl= new ConnectionManager.ConnectionListener(){

        @Override
        public void onConnectStateChange(int oldState, int state) {

        }

        @Override
        public void onListenStateChange(int oldState, int state) {

        }

        @Override
        public void onSendData(boolean suc, byte[] date) {
     //       mHandler.obtainMessage(MSG_SENT_DATA, suc?1:0, 0, data).sendToTarget();
        }

        @Override
        public void onReadDate(byte[] data) {
            try {
                mBuffStream.write(data);
                mCMReadDataCount+= data.length;
                if(mCMReadDataCount>buffStreamLength/2){
                    byteFreshData();
                }
            } catch (IOException e) {
                Log.d(TAG,"get data error");
            }
        }
    } ;
    public  class VideoMessage{
        final String MARK = "#";
        final String VIDEOMARK = "#123456";
        int videoStart,videoEnd,videoLengthEnd,videoLength;
        private int  level;
        public  VideoMessage(){
            videoStart = -1;
            videoEnd = -1;
            videoLength = -1;
            videoLengthEnd = -1;
            level = 0;
        }
        public int getLength(){
            return videoLength;
        }
        public void setLength(int length){
            videoLength = length;
        }
        public void  levelUp (){
            level++;
        }
        public int getLevel (){
            return level;
        }
        /*2018年9月6日
        *
        * 这个是拿来重置level的 那么为什么不叫resetLevel要叫复活呢
        * */
        public void relife(){
            level = 0;
        }

    }




}

