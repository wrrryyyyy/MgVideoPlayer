package com.wrrryyyy.www.mgvideoplayer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by aa on 2018/8/14.
 */

public class ConnectionManager {
    public static final int CONNECT_STATE_IDLE = 0;
    public static final int CONNECT_STATE_CONNECTING = 1;
    public static final int CONNECT_STATE_CONNECTED = 2;
    private static final String TAG = "ConnectionManager";
    private static final int MAX_BUFF_SIZE = 1024;
    final int LISTEN_STATE_IDLE = 3;
    final int LISTEN_STATE_LISTENING = 4;
    private String lastConnectDeviceAddr = "";
    private final BluetoothAdapter mBluetoothAdapter;
    private static final String BT_NAME = "ann";
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static  ConnectionManager mConnectionManager;
    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;
    private int mListenState = LISTEN_STATE_IDLE;
    private int mConnectState = CONNECT_STATE_IDLE;
    private boolean mListening = false;
    public interface ConnectionListener{
        public void onConnectStateChange(int oldState,int state);
        public void onListenStateChange(int oldState,int state);
        public void onSendData(boolean suc,byte[]date);
        public void onReadDate(byte[] data);

    }
    private ConnectionListener mConnectionListener;
    private ConnectionManager(ConnectionListener cl){
        mConnectionListener = cl;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public  static  ConnectionManager getConnectionManager(ConnectionListener cl){
        if(mConnectionManager==null){
            mConnectionManager = new ConnectionManager(cl);
        }else if(cl!=null){
            mConnectionManager.setListener(cl);
        }
//        if(mConnectionManager!=null&&mConnectionManager.mConnectionListener==null&&cl!=null){
//            mConnectionManager = new ConnectionManager(cl);
//        }

        return mConnectionManager;
    }
    public void setListener(ConnectionListener cl){
        this.mConnectionListener = cl;
    }
    public void startListen(){
        if(mAcceptThread!=null){
            mAcceptThread.cancel();
        }
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
        mListening = true;
    }
    public boolean isListening(){
        return mListening;
    }
    public void stopListen(){
        if(mAcceptThread!=null){
            mAcceptThread.cancel();
        }
    }
    public boolean isConnectioned(){
        return mConnectState==CONNECT_STATE_CONNECTED;
    }
    public void connect(String deviceAddr){
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddr);
        try {
            Log.d(TAG,"connet start");
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BT_UUID);
            mConnectedThread = new ConnectedThread(socket,true);
            mConnectedThread.start();
            lastConnectDeviceAddr = deviceAddr;
        } catch (IOException e) {
            Log.d(TAG,"connet error,"+e);
           }
    }
    public void disconnect(){
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
        }
        Log.d(TAG,"disconnet");
    }
    public int getCurrentListenState(){
        return mListenState;
    }
    public int getCurrentConnectState(){
        return mConnectState;
    }
    public boolean sendData(byte[] data){
        if(mConnectedThread!=null&&mConnectState==CONNECT_STATE_CONNECTED){
            mConnectedThread.sendData(data);
            return true;
        }
        return false;
    }
    public String getLastConnectDeviceAddr(){
        return lastConnectDeviceAddr;
    }

    public String getState(int state){
        switch (state){
            case CONNECT_STATE_IDLE:
                return "CONNECT_STATE_IDLE";

            case CONNECT_STATE_CONNECTING:
                return "CONNECT_STATE_CONNECTING";

            case CONNECT_STATE_CONNECTED:
                return "CONNECT_STATE_CONNECTED";

            case LISTEN_STATE_IDLE:
                return "LISTEN_STATE_IDLE";

            case LISTEN_STATE_LISTENING:
                return "LISTEN_STATE_LISTENING";
        }
        return "UNKNOW";
    }
    private synchronized void setConnectState(int state) {

        if(mConnectState == state) {
            return;
        }

        int oldState = mConnectState;
        mConnectState = state;

        if(mConnectionListener != null) {

            Log.d(TAG, "BT state change: "+getState(oldState)+" -> "+ getState(mConnectState));
            mConnectionListener.onConnectStateChange(oldState, mConnectState);
        }
    }

    private synchronized void setListenState(int state) {

        if(mListenState == state) {
            return;
        }

        int oldState = mListenState;
        mListenState = state;

        if(mConnectionListener != null) {

            Log.d(TAG, "BT state change: "+getState(oldState)+" -> "+ getState(mListenState));
            mConnectionListener.onListenStateChange(oldState, mListenState);
        }
    }
    private synchronized void startConnectThread(BluetoothSocket socket, boolean needConnect) {

        mConnectedThread = new ConnectedThread(socket, needConnect);
        mConnectedThread.start();
    }
    private class AcceptThread extends  Thread{
        private final String TAG = "AcceptThread";
        private BluetoothServerSocket mServerSocket;
        private boolean mUserCancel;
        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            mUserCancel = false;
            try{
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(BT_NAME,BT_UUID);
            } catch (IOException e) {
                Log.e(TAG,"AcceptThread create error",e);
            }
            mServerSocket = tmp;
        }
        public void cancel(){
            try{
                mUserCancel = true;
                if(mServerSocket!=null){
                    mServerSocket.close();
                }
            }catch (IOException e){
                Log.e(TAG,"AcceptThread cancel fail",e);
            }
        }
        @Override
        public void run(){
            setName("AcceptThread");
            setListenState(LISTEN_STATE_LISTENING);
            BluetoothSocket socket = null;
            while(!mUserCancel){
                try{
                    Log.d(TAG,"AcceptThread waiting");
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.d(TAG,"AcceptThread exception"+e);
                    mServerSocket  = null;
                    break;
                }
                if(mConnectState == CONNECT_STATE_CONNECTED||mConnectState ==CONNECT_STATE_CONNECTING){
                    try{
                        socket.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }else if(mConnectState ==CONNECT_STATE_IDLE){
                    startConnectThread(socket,false);
                }
            }
            if(mServerSocket!=null){
                try{
                    mServerSocket.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                mServerSocket = null;
            }
            setListenState(LISTEN_STATE_IDLE);
            mAcceptThread = null;
            mListening = false;
            if(mUserCancel == true) {
                Log.d(TAG, "AcceptThread END since user cancel.");
            }
            else {
                Log.d(TAG, "AcceptThread END");
            }
        }
    }
    private class ConnectedThread extends Thread{
        private final int MAX_BUFFER_SIZE =1024;
        private BluetoothSocket mSocket;
        private InputStream mInStream;
        private OutputStream mOutStream;
        private boolean mUserCancel;
        private boolean mNeddConnect;

        String TAG = "connectedThread";
        public ConnectedThread(BluetoothSocket socket,boolean needConnect){
            setName("ConnectedThread");
            mNeddConnect = needConnect;
            mSocket = socket;
            mUserCancel = false;
        }

        @Override
        public void run(){
            Log.d(TAG,"ConnectedThread START");
            setConnectState(CONNECT_STATE_CONNECTING);
            if(mNeddConnect&&!mUserCancel){
                try{
                    mSocket.connect();
                } catch (IOException e) {
                    Log.d(TAG,"ConnectedThread END at connect(),"+e);
                    setConnectState(CONNECT_STATE_IDLE);
                    mSocket = null;
                    mConnectedThread = null;
                    return ;
                }
            }
            InputStream tmpIn = null;
            OutputStream tmpOut =null;
            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG,"ConnectedThread END at getStream(),"+e);
                setConnectState(CONNECT_STATE_IDLE);
                mSocket = null;
                mConnectedThread = null;
                return ;
            }
            mInStream = tmpIn;
            mOutStream = tmpOut;
            setConnectState(CONNECT_STATE_CONNECTED);
            byte[] buffer = new byte[MAX_BUFF_SIZE];
            int bytes = 1024;
            while(!mUserCancel){
                Log.d(TAG,"ConnectedThread start read");

                try {
                    bytes = mInStream.read(buffer);
                    if(mConnectionListener !=null&&bytes>0){
                        byte[] data = new byte[bytes];
                        System.arraycopy(buffer,0,data,0,bytes);
                        mConnectionListener.onReadDate(data);
                    }
                } catch (IOException e) {
                    Log.d(TAG,"ConnectedThread END at read data,"+e);
                    break;
                }
            }
            setConnectState(CONNECT_STATE_IDLE);
            mSocket = null;
            mConnectedThread = null;
            if(mUserCancel == true) {
                Log.d(TAG, "ConnectedThread END since user cancel.");
            }
            else {
                Log.d(TAG, "ConnectedThread END");
            }
        }
        public void cancel(){
            mUserCancel = true;
            if(mSocket!=null){
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "ConnectedThread cancel failed", e);
                }
            }
            Log.d(TAG, "ConnectedThread cancel END");
        }
        public void sendData(byte[] data){
            try {
                mOutStream.write(data);
                if(mConnectionListener!=null){
                    mConnectionListener.onSendData(true,data);
                }
            } catch (IOException e) {
                Log.d(TAG, "send data fail",e);
                if(mConnectionListener!=null){
                    mConnectionListener.onSendData(true,data);
                }

            }
        }
    }
    private class AutoSendThread extends Thread{

    }
}
