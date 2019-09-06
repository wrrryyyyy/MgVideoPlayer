package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;

import lecho.lib.hellocharts.model.PointValue;

import static com.wrrryyyy.www.mgvideoplayer.Gcode.*;


public class TeacherFragment extends BaseFragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "TeacherFragment";
    private static  String[] Gcode ;
    private String mShowString = "";
    private Activity mActivity;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private ConnectionManager mConnectionManager;
    private Spinner mSpinner;
    private Button mBtnBeginPos;
    private Button mBtnEndPos;
    private Button mBtnInsert;
    private Button mBtnDelect;
    private Button mBtnStep;
    private Button mBtnAuto;
    private Button mBtnZero;
    private Button mBtnStop;
    private EditText mEditText;
    private ListView mListView;
    private TextView mTvTeacherShow;
    private Button[] mBtnMove = new Button[8];
    private int mSpinnerNum = 0;


    private MediaMetadataRetriever mMedia;
    private Bitmap mBitmapBuff;
    private Bitmap mBitmap;
    private boolean bitmapInit = false;
    private GcodeSendingHandler sendingTask ;
    //------------没啥用的变量---------------
    private Gcode mEditingGcode ;
    private float mArmData[] = new float[7];
    private int mArmCommand  = 0;
    private boolean isSendAble = true;
    private Vector<Gcode> mGcodeVector = new Vector<>() ;
    private GcodeEditer mGcodeEditer;

    public static final int REQUEST_CODE_SELECT_IMAGE = 65;
    public static final float[] DEFAULT_PHOTO_GCODE_OFFSET = {0,180,30};
    public static final float PHOTO_GCODE_RATE = 0.2f;


    public TeacherFragment() {
        // Required empty public constructor
    }

    public static TeacherFragment newInstance(String param1, String param2) {
        return newInstance(param1,param2,null);
    }
    public static TeacherFragment newInstance() {
        return newInstance(null);
    }
    public static TeacherFragment newInstance(int lineNum) {
        return newInstance(null);
    }
    public static TeacherFragment newInstance(List<List<PointValue>>list){
        return newInstance("","",list);
    }

    public static TeacherFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
        TeacherFragment fragment = new TeacherFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher, container, false);
        mConnectionManager = ConnectionManager.getConnectionManager(null);
        Resources res = getResources();
        Gcode = res.getStringArray(R.array.Gcode);
        initButton(view);
        initWidget(view);
        initVar(view);
        sendingTask = new GcodeSendingHandler();
        updataUI();
        return view;
    }
    View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {
           switch(v.getId()){
               case R.id.btn_gcode_step:{
                   sendGcode(mGcodeEditer.getGcodeNow());
                   sendingTask.sendOver();
                   break;
               }
               case R.id.btn_var_begin:{
                   posInputViewCreat(true);
                   break;
               }
               case R.id.btn_var_end:{
                   posInputViewCreat(false);
                   break;
               }
               case R.id.btn_gcode_insert:{
                   mEditingGcode.code = mSpinnerNum;
                   gcodeInsert(mEditingGcode);
                   break;
               }
               case R.id.btn_gcode_delet:{
                   mGcodeEditer.gcodeDelet();
                   mHandler.obtainMessage(MSG_UPDATE_LV).sendToTarget();
                   break;
               }
               case R.id.btn_teacher_up:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_UP);
                   sendMessage(code.getAll());
                   break;
               }
               case R.id.btn_teacher_down:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_DOWN);
                   sendMessage(code.getAll());
                   break;
               }
               case R.id.btn_teacher_left:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_LEFT);
                   sendMessage(code.getAll());
                   break;
               }
               case R.id.btn_teacher_right:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_RIGHT);
                   sendMessage(code.getAll());
                   break;
               }
               case R.id.btn_teacher_forware:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_FORWARE);
                   sendMessage(code.getAll());
                   break;
               }
               case R.id.btn_teacher_backware:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_BACKWARE);
                   sendMessage(code.getAll());
                   break;
               }
               case R.id.btn_emergency_stop:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_STOP);
                   sendMessage(code.getAll());
                   sendingTask.sendOver();
                   break;
               }
               case R.id.btn_teacher_select:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_SELECT);
                   sendMessage(code.getAll());
                   break;
               }
               case R.id.btn_back_to_zero:{
                   ArmCoder code = new ArmCoder((byte)ArmCoder.COMMAND_ZERO);
                   sendMessage(code.getAll());
                   sendingTask.sendOver();
                   break;
               }


               case R.id.btn_gcode_run:{
//                   Gcode[] gcode = mGcodeEditer.getGcodeAll();
//                   if(gcode!=null&&gcode.length>0){
//                       for(Gcode code:gcode){
//                           sendGcode(code);
//                       }
//                   }
                   if(sendingTask!=null){
                       sendingTask.sendOver();
                       sendingTask.startSending();

                       Toast.makeText(mActivity,"send start",Toast.LENGTH_SHORT).show();
                   }
                   break;
               }
               case R.id.btn_teacher_more:{
                   gcodePhotoViewCreat();
                   break;
               }
           }
        }
    };
    void gcodePhotoViewCreat(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final View photo2GcodeView = View.inflate(mActivity,R.layout.photo_gcode_view,null);
        final ImageView iv_photo = (ImageView)photo2GcodeView.findViewById(R.id.iv_photo_gcode);
        Button btn_test = (Button)photo2GcodeView.findViewById(R.id.btn_photo_gcode);
        Button btn_photo = (Button)photo2GcodeView.findViewById(R.id.btn_photo_gcode_open);
        EditText et_test = (EditText)photo2GcodeView.findViewById(R.id.et_photo_gcode);
        SeekBar sb_photo = (SeekBar)photo2GcodeView.findViewById(R.id.sb_photo_gcode);
        final int[] seekBarPos = {0};
        sb_photo.setMax(254);
        if(bitmapInit&&mBitmapBuff!=null){
            iv_photo.setImageBitmap(mBitmapBuff);
         //   mBitmap = Bitmap.createBitmap(mBitmapBuff,0,0,mBitmapBuff.getWidth()-1,mBitmapBuff.getHeight()-1);
        }
        builder.setTitle("图片转Gcode")
                .setIcon(R.mipmap.video_blue)
                .setView(photo2GcodeView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //点确定的时候刷新值到界面上
                        dialog.dismiss();
                    }
                });
        sb_photo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarPos[0] = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mBitmapBuff!=null){
                    VideoDecoder.decodeBinary(mBitmapBuff,new Size(300,240),seekBarPos[0],mBitmap);
                    if(mBitmap!=null)iv_photo.setImageBitmap(mBitmap);
                }
            }
        });

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f= new File("/");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mActivity.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            }
        });
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //     Imgproc.findContours();
                Vector<Vector<Point>> vv= VideoDecoder.getAllPoint(mBitmap,null);
                if(vv!=null&&vv.size()>0){// add gcodes to listview
                    for(Vector<Point>vector:vv){
                        if(vector==null||vector.size()==0)continue;
                        Point firstPoint = vector.get(0);

                        mHandler.obtainMessage(MSG_ADD_GCODE,photoPixelChange(new Gcode(_G00,new float[]{0,0,0},new float[]{(float)firstPoint.x,(float)firstPoint.y,20},0.0f))).sendToTarget();;//移动
                        mHandler.obtainMessage(MSG_ADD_GCODE,photoPixelChange(new Gcode(_G01,new float[]{0,0,0},new float[]{(float)firstPoint.x,(float)firstPoint.y,10},0.0f))).sendToTarget();;//下
                        for(Point point:vector){
                            mHandler.obtainMessage(MSG_ADD_GCODE,photoPixelChange(new Gcode(_G01,(float)point.x,(float)point.y,0.0f))).sendToTarget();;
                        }
                        Point lastPoint = vector.get(vector.size()-1);//
                        mHandler.obtainMessage(MSG_ADD_GCODE,photoPixelChange(new Gcode(_G01,new float[]{0,0,0},new float[]{(float)lastPoint.x,(float)lastPoint.y,20},0.0f))).sendToTarget();;//抬起

                    }
                }
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    Gcode photoPixelChange(Gcode gcode){
        gcode.endPos[0] *= PHOTO_GCODE_RATE;
        gcode.endPos[1] *=PHOTO_GCODE_RATE;
//        gcode.endPos[2] *=PHOTO_GCODE_RATE;
        gcode.endPos[0]+= DEFAULT_PHOTO_GCODE_OFFSET[0];
        gcode.endPos[1]+= DEFAULT_PHOTO_GCODE_OFFSET[1];
        gcode.endPos[2]+= DEFAULT_PHOTO_GCODE_OFFSET[2];
        return gcode;
    }
    void gcodeInsert(Gcode gcode){
        Gcode addGcode = new Gcode(gcode);
        mGcodeEditer.mGcodeAdapter.add(addGcode);
        mGcodeEditer.mGcodeAdapter.notifyDataSetChanged();
    }
    void posInputViewCreat(boolean start){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final View setPointView = View.inflate(mActivity,R.layout.pos_select_view,null);
        final boolean statue = start;
        final Button btnNowX = (Button)setPointView.findViewById(R.id.btn_pos_x);
        Button btnNowY = (Button)setPointView.findViewById(R.id.btn_pos_y);
        Button btnNowZ = (Button)setPointView.findViewById(R.id.btn_pos_z);
        final EditText etX = (EditText)setPointView.findViewById(R.id.ed_pos_x);
        final EditText etY = (EditText)setPointView.findViewById(R.id.ed_pos_y);
        final EditText etZ = (EditText)setPointView.findViewById(R.id.ed_pos_z);
        if(start==true){
            etX.setText(""+mEditingGcode.startPos[0]);
            etY.setText(""+mEditingGcode.startPos[1]);
            etZ.setText(""+mEditingGcode.startPos[2]);
        }else{
            etX.setText(""+mEditingGcode.endPos[0]);
            etY.setText(""+mEditingGcode.endPos[1]);
            etZ.setText(""+mEditingGcode.endPos[2]);
        }
        builder.setTitle("点坐标编辑")
                .setIcon(R.mipmap.wave_while)
                .setView(setPointView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //点确定的时候刷新值到界面上
                        if(statue){
                            mEditingGcode.startPos[0] = Float.valueOf(etX.getText().toString());
                            mEditingGcode.startPos[1] = Float.valueOf(etY.getText().toString());
                            mEditingGcode.startPos[2] = Float.valueOf(etZ.getText().toString());
                        }else{

                            mEditingGcode.endPos[0] = Float.valueOf(etX.getText().toString());
                            mEditingGcode.endPos[1] = Float.valueOf(etY.getText().toString());
                            mEditingGcode.endPos[2] = Float.valueOf(etZ.getText().toString());
                        }
                        dialog.dismiss();
                        updataUI();
                    }
                });
        final AlertDialog alertDialog = builder.create();
        btnNowX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statue){
                    mEditingGcode.startPos[0] = mArmData[0];
                    etX.setText(""+mEditingGcode.startPos[0]);
                }
                else {
                    mEditingGcode.endPos[0] = mArmData[3];
                    etX.setText(""+mEditingGcode.endPos[0]);
                }
            }
        });

        btnNowY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statue){
                    mEditingGcode.startPos[1] = mArmData[1];
                    etY.setText(""+mEditingGcode.startPos[1]);
                }
                else {
                    mEditingGcode.endPos[1] = mArmData[4];
                    etY.setText(""+mEditingGcode.endPos[1]);
                }
            }
        });

        btnNowZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statue){
                    mEditingGcode.startPos[2] = mArmData[2];
                    etZ.setText(""+mEditingGcode.startPos[2]);
                }
                else {
                    mEditingGcode.endPos[2] = mArmData[5];
                    etZ.setText(""+mEditingGcode.endPos[2]);
                }
            }
        });

        alertDialog.show();
    }
    void initVar(View view){
        mEditingGcode = new Gcode(0,new float[]{10,10,10},new float[]{20,20,20},0);
    }
    void initWidget(View view){
        mSpinner = (Spinner)view.findViewById(R.id.spinner_gcode);
        mEditText = (EditText)view.findViewById(R.id.edit_var_num);
        mListView = (ListView)view.findViewById(R.id.list_view_teacher_gcode);
        //mSpinner.setDropDownWidth(100);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mActivity,R.layout.g_code_item,Gcode);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSpinnerNum = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mTvTeacherShow = (TextView)view.findViewById(R.id.tv_teacher_show);

        mGcodeEditer = new GcodeEditer();
        mGcodeEditer.mGcodeList = new ArrayList<Gcode>();
        mGcodeEditer.mGcodeAdapter = new GcodeAdapter(mActivity,R.layout.teacher_gcode_item,mGcodeEditer.mGcodeList);
        mListView.setAdapter(mGcodeEditer.mGcodeAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Gcode item = mGcodeEditer.mGcodeList.get(position);
                mGcodeEditer.gcodeSelect(position);
                Toast.makeText(mActivity,"select gcode"+position,Toast.LENGTH_SHORT);
                mHandler.obtainMessage(MSG_UPDATE_LV).sendToTarget();;
            }
        });
    }
    void initButton(View view){
        mBtnBeginPos = (Button)view.findViewById(R.id.btn_var_begin);
        mBtnEndPos = (Button)view.findViewById(R.id.btn_var_end);
        mBtnInsert = (Button)view.findViewById(R.id.btn_gcode_insert);
        mBtnDelect = (Button)view.findViewById(R.id.btn_gcode_delet);
        mBtnStep = (Button)view.findViewById(R.id.btn_gcode_step);
        mBtnAuto = (Button)view.findViewById(R.id.btn_gcode_run);
        mBtnZero = (Button)view.findViewById(R.id.btn_back_to_zero);
        mBtnStop = (Button)view.findViewById(R.id.btn_emergency_stop);
        mBtnMove[0] = (Button)view.findViewById(R.id.btn_teacher_up);
        mBtnMove[1] = (Button)view.findViewById(R.id.btn_teacher_forware);
        mBtnMove[2] = (Button)view.findViewById(R.id.btn_teacher_more);
        mBtnMove[3] = (Button)view.findViewById(R.id.btn_teacher_left);
        mBtnMove[4] = (Button)view.findViewById(R.id.btn_teacher_select);
        mBtnMove[5] = (Button)view.findViewById(R.id.btn_teacher_right);
        mBtnMove[6] = (Button)view.findViewById(R.id.btn_teacher_backware);
        mBtnMove[7] = (Button)view.findViewById(R.id.btn_teacher_down);
        mBtnMove[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        for(int i = 0;i<mBtnMove.length;i++){
            mBtnMove[i].setOnClickListener(buttonListener);
        }
        mBtnBeginPos.setOnClickListener(buttonListener);
        mBtnEndPos.setOnClickListener(buttonListener);
        mBtnInsert.setOnClickListener(buttonListener);
        mBtnDelect.setOnClickListener(buttonListener);
        mBtnStep.setOnClickListener(buttonListener);
        mBtnAuto.setOnClickListener(buttonListener);
        mBtnZero.setOnClickListener(buttonListener);
        mBtnStep.setOnClickListener(buttonListener);
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
    public void messageInterface(int msg){
        mHandler.obtainMessage(msg).sendToTarget();
        Log.d(TAG+"All","get message obj "+" "+msg);
    }
    public void messageInterface(int msg,Object obj){
        if(obj!=null)mHandler.obtainMessage(msg,obj).sendToTarget();
        Log.d(TAG+"All","get message obj "+" "+msg);
    }
    private boolean sendGcode(Gcode gcode){
        if(gcode==null){
            Toast.makeText(mActivity,"最少需要编写一行代码",Toast.LENGTH_SHORT);
            Log.d(TAG,"send message error:sending gcode null");
            return false;
        }
        boolean ans = mConnectionManager.sendData(gcode.getArmCoder());
        if(!ans)Log.d(TAG,"send message error");
        return ans;
    }
    private boolean sendMessage(byte[] bytes){//往外输出的函数
        if(bytes == null||bytes.length==0){
            Log.d(TAG,"error:send message null");
            return false;
        }
        boolean ans = mConnectionManager.sendData(bytes);
        if(!ans)Log.d(TAG,"send message error");
        return ans;
    }
    private void updataListViewUI(){
        mGcodeEditer.mGcodeAdapter.notifyDataSetChanged();
    }
    private void updataUI(){
        mBtnBeginPos.setText(mEditingGcode.getStartPosString());
        mBtnEndPos.setText(mEditingGcode.getEndPosString());
        mShowString = "命令："+mArmCommand+"\n起点坐标：\nx:"+mArmData[0]+"\ny:"+mArmData[1]+"\nz:"+mArmData[2]+"\n终点坐标：\nx:"+mArmData[3]
                +"\ny:"+mArmData[4]+"\nz:"+mArmData[5]+"\n\nR:"+mArmData[6];
        mTvTeacherShow.setText(mShowString);
//        int offset = mTvFront.getLineCount()*mTvFront.getLineHeight()-mTvFront.getHeight();//拿到文字的长度
//        if(offset>0){
//            mTvFront.scrollTo(0,offset>4?offset-4:offset);//这个函数是view的 用来滑动view里面的元素指定坐标
//        }//Log.d(TAG,mShowString);
    }
    private void addupShowString(String s){
        mShowString  = s;
    }
    private void addupShowString(byte[] data,String encode){
        try {
            addupShowString(new String(data,encode));
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG,"error encode");
        }
    }

    public void setUri(Uri uri){
        mHandler.obtainMessage(MSG_GET_URI,uri).sendToTarget();
    }

    public static final int MSG_UPDATE_TV = 3;
    public static final int MSG_UPDATE_LV = 6;
    public static final int MSG_ADD_DATA = 4;
    public static final int MSG_ADD_COMMAND = 5;
    public static final int MSG_ADD_STRING = 9;
    public static final int MSG_GET_URI = 11;
    public static final int MSG_ADD_GCODE = 12;
    public static final int MSG_TIMMER = 13;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_UPDATE_TV:{
                    updataUI();
                    break;
                }
                case MSG_UPDATE_LV:{
                    updataListViewUI();
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
                case MSG_ADD_COMMAND:{
                    break;
                }
                case MSG_ADD_DATA:{
                    TeacherCoder tc = (TeacherCoder) msg.obj;
                    teacherDataDeal(tc);
                    break;
                }

                case MSG_GET_URI:{
                    Uri uri = (Uri)msg.obj;
                    Log.d(TAG,"get uri");
                    if(uri==null)break;
                    Log.d(TAG,"uri:"+uri.getPath());
                    Toast.makeText(mActivity,uri.getPath(),Toast.LENGTH_SHORT).show();;
                    //           mTvFile.setText(uri.getPath());
                    dealUri(uri);
                    break;
                }
                case MSG_ADD_GCODE:{
                    Gcode gcode= (Gcode)msg.obj;
                    Log.d(TAG,"get gcode");
                    if(gcode==null)break;
                    gcodeInsert(gcode);
                    break;
                }
                case MSG_TIMMER:{
                    sendingTask.TimmerSending();
                    break;
                }
            }
        }
    };
    private void teacherDataDeal(TeacherCoder tc){
        if(tc!=null){
            byte[] b = tc.data;
            int command = tc.getCommand();
            if(command == TeacherCoder.COMMAND_BACK_NEED_NEXT){//只有这种包捕获下来 其他的直接显示
                sendingTask.sendNext();
                Toast.makeText(mActivity,"get next",Toast.LENGTH_SHORT).show();;
            }else{
                mArmCommand = tc.getCommand();
                mArmData = tc.getDataFloat();
                addupShowString(b,"GBK");
            }
        }
    }
    private void onGcodeSending(int count){
        int sendNum = 20;
        Gcode[] gcode = mGcodeEditer.getGcodeAt(count * sendNum,sendNum);
        if(gcode!=null&&gcode.length>0){
            for(Gcode code:gcode){
                sendGcode(code);
            }
            if(gcode.length<sendNum) {
                sendingTask.sendOver();
              //  Toast.makeText(mActivity,"send over",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mActivity,"sending:"+count,Toast.LENGTH_SHORT).show();
            }
        }else{
            sendingTask.sendOver();
         //   Toast.makeText(mActivity,"send over",Toast.LENGTH_SHORT).show();
        }
    }
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
        if(path!=null&&type!=null&&type.indexOf("image")!=-1){//get a file by uri
            //image型
                try{
                    mBitmapBuff =  EmojiFragment.getBitmapFormUri(mActivity,uri);
                    mBitmap =  EmojiFragment.getBitmapFormUri(mActivity,uri);
                    bitmapInit = true;
                }catch(Exception e){
                    Log.d(TAG,"creat bitmap error");
                }

        }else{
            Log.d(TAG,"get back error:get null pointer "+path);
        }
    }
    public boolean isReachAble(){
        return isSendAble;
    }
    private class GcodeSendingHandler{

        private boolean needSendNext = false;
        private boolean needSend = false;
        private int sendCount = 0;
        private int  counter = 0;
        private final static int counterMax = 100;
        public void  sendNext(){
            needSendNext = true;
        }
        public void sendOver(){
            needSend = false;
            if(sendCount!=0)  Toast.makeText(mActivity,"send over",Toast.LENGTH_SHORT).show();
            sendCount = 0;
        }
        public void startSending(){
            needSend = true;
        }
        public void TimmerSending(){
            if(needSend){
                counter++;
                if(counter>=counterMax||needSendNext){
                    onGcodeSending(sendCount++);
                    counter = 0;
                    needSendNext = false;
                }
            }
        }
    }
//    private class GcodeSendingAsyncTask extends AsyncTask<Void,Void,Void> {
//        private boolean running = true;
//        private boolean pause = false;
//        private long delayTime = 100;
//        public void pause(){
//            pause = true;
//        }
//        public void restart(){
//            pause  =false;
//        }
//        public GcodeSendingAsyncTask (long time){
//            if(time>0)delayTime = time;
//        }
//
//        public GcodeSendingAsyncTask(){
//            super();
//        }
//        @Override
//        protected Void doInBackground(Void... params) {
//            int counter = 0;
//            while(running){
//                try {
//                    if(!pause){
//
//                    }
//                    Thread.sleep(delayTime);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            return null;
//        }
//        public void stopTask(){
//            running = false;
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//            boolean b = false;
//
//            if(b){
//                stopTask();
//            }else{
//
//            }
//        }
//    }
}

class ArmTeacher{

    private float mArmData[] = new float[7];
    private int mArmCommand  = 0;
    private float start[] = new float[3];
    private float end[] = new float[3];
    private int gcode = 0;


}
class GcodeEditer{
    int pos;
    int lastPos;
    ArrayList<Gcode> mGcodeList;
    GcodeAdapter mGcodeAdapter;
    GcodeEditer(){
        pos = 0;
        lastPos = 0;
    }
    void gcodeSelect(int num){
        if(mGcodeList!=null&&mGcodeList.size()>=num){
            lastPos = pos;
            pos = num;
            mGcodeAdapter.selected = pos;
            mGcodeAdapter.lastSelected = lastPos;
        }
    }
    void selectNext(){
        if(mGcodeList!=null){
            if(pos == mGcodeList.size()-1){
                pos = 0;
            }else{
                pos++;
            }
            gcodeSelect(pos);
        }


    }
    void gcodeDelet(int num){
        if(mGcodeList!=null&&mGcodeList.size()>num){
            mGcodeList.remove(num);
            if(pos>=num){
                pos--;
                gcodeSelect(pos);
            }
        }
    }
    void gcodeDelet(){
        gcodeDelet(pos);
    }
    Gcode getGcodeNow(){
        if(mGcodeList!=null&&mGcodeList.size()>pos){
            return mGcodeList.get(pos);
        }
        return null;
    }
    Gcode[] getGcodeAll(){
        if(mGcodeList!=null&&mGcodeList.size()>0){
            Gcode gcode[] = new Gcode[mGcodeList.size()];
            mGcodeList.toArray(gcode);
            return gcode;

        }

        return null;
    }
    Gcode[] getGcodeAt(int start,int length){
        if(mGcodeList!=null&&mGcodeList.size()>=start+length-1){
            int all = mGcodeList.size()-start>=length?length:mGcodeList.size()-start;
            Gcode gcode[] = new Gcode[all];
            for(int i=0;i<all;i++){
                gcode[i] = mGcodeList.get(start+i);
            }

            return gcode;

        }

        return null;
    }
}
class GcodeAdapter extends ArrayAdapter<Gcode>{

    private final LayoutInflater mInflater;
    private final int mResource;
    int selected = 0;
    int lastSelected  = 0;
    public GcodeAdapter(@NonNull Context context, @LayoutRes int resource, List<Gcode> object) {
        super(context, resource, object);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }
        Gcode item = getItem(position);//拿到指定的item
        TextView name = (TextView)convertView.findViewById(R.id.tv_gcode);
        TextView data = (TextView)convertView.findViewById(R.id.tv_gcode_var);
        name.setText(item.gcode[item.code]);
        if(selected == position) {//改颜色
            name.setBackgroundColor(convertView.getResources().getColor(R.color.colorGrayWhite));
        }else{
            name.setBackgroundColor(convertView.getResources().getColor(R.color.colorWhite));

        }
        data.setText(item.toString());
        return convertView;
    }


}
