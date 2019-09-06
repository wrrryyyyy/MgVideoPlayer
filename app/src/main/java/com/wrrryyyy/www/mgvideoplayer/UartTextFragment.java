package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.List;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by aa on 2018/10/7.
 */

public class UartTextFragment extends BaseFragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "UartTextFragment";
    private String mShowString = "";
    private Activity mActivity;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private ConnectionManager mConnectionManager;
    private TextView mTvFront;
    //------------没啥用的变量---------------
    private boolean isSendAble = true;
    public UartTextFragment() {
        // Required empty public constructor
    }

    public static UartTextFragment newInstance(String param1, String param2) {
        return newInstance(param1,param2,null);
    }
    public static UartTextFragment newInstance() {
        return newInstance(null);
    }
    public static UartTextFragment newInstance(int lineNum) {
        return newInstance(null);
    }
    public static UartTextFragment newInstance(List<List<PointValue>>list){
        return newInstance("","",list);
    }

    public static UartTextFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
        UartTextFragment fragment = new UartTextFragment();
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
        View view = inflater.inflate(R.layout.fragment_uart, container, false);
        mConnectionManager = ConnectionManager.getConnectionManager(null);
        final Button btnPause = (Button)view.findViewById(R.id.btn_pause);
        Button btnClean = (Button)view.findViewById(R.id.btn_clean);
        Button btnClose = (Button)view.findViewById(R.id.btn_uart_send);
        mTvFront = (TextView)view.findViewById(R.id.tv_uart_mega);
        mTvFront.setMovementMethod(ScrollingMovementMethod.getInstance());
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSendAble = !isSendAble;
                if(isSendAble){
                    btnPause.setText(R.string.reaching);//状态改变
                }else{
                    btnPause.setText(R.string.disreaching);//状态改变
                }
            }
        });
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvFront.setText("");
                mShowString = "";
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                final View sendMessageView = View.inflate(mActivity,R.layout.message_send_view,null);
                builder.setTitle(R.string.uart_send)
                        .setIcon(R.mipmap.uart_white)
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
                        mConnectionManager.sendData("F".getBytes());
//                        Toast.makeText(mActivity,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
                    }
                });
                btnTest5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConnectionManager.sendData("E".getBytes());
//                        Toast.makeText(mActivity,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();
                    }
                });
                btnTest4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConnectionManager.sendData("D".getBytes());
//                        sendParameter("lululu","nswsz".getBytes());
                    }
                });
                btnTest3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConnectionManager.sendData("C".getBytes());
//                        sendParameter("lololo","nswez".getBytes());
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
                        mConnectionManager.sendData("A".getBytes());
//                        Toast.makeText(mActivity,"还没想好这个按钮干啥",Toast.LENGTH_SHORT).show();

                    }
                });
                alertDialog.show();
            }
        });
        return view;
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

    private void updataUI(){
        mTvFront.setText(mShowString);
        int offset = mTvFront.getLineCount()*mTvFront.getLineHeight()-mTvFront.getHeight();//拿到文字的长度
        if(offset>0){
            mTvFront.scrollTo(0,offset>4?offset-4:offset);//这个函数是view的 用来滑动view里面的元素指定坐标
        }//Log.d(TAG,mShowString);
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
    public static final int MSG_UPDATE_TV = 3;
    public static final int MSG_ADD_STRING = 9;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_UPDATE_TV:{
                    updataUI();
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

            }
        }
    };
    public boolean isReachAble(){
        return isSendAble;
    }
}
