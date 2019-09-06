package com.wrrryyyy.www.mgvideoplayer;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class RemoteActivity extends AppCompatActivity {

    final int REQUEST_CODE_BTDEVICE = 15;
    private Button mButtonUp,mButtonRight,mButtonLeft,mButtonDown,mButtonCenter,mButtonA,mButtonB,mButtonLeftMenu;
    private DrawerLayout mLeftMenu;
    private ConnectionManager mConnectionManager;
    private final String TAG = "RemoteAct";
    private MenuItem mConnectionMenuItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        mConnectionManager = ConnectionManager.getConnectionManager(null);
        mLeftMenu = (DrawerLayout)findViewById(R.id.dl_remote_left_menu);
        mButtonUp = (Button)findViewById(R.id.remote_up_btn);
        mButtonDown = (Button)findViewById(R.id.remote_down_btn);
        mButtonLeft = (Button)findViewById(R.id.remote_left_btn);
        mButtonRight = (Button)findViewById(R.id.remote_right_btn);
        mButtonCenter = (Button)findViewById(R.id.remote_center_btn);
        mButtonA = (Button)findViewById(R.id.remote_A_btn);
        mButtonB = (Button)findViewById(R.id.remote_B_btn);
        mButtonLeftMenu = (Button)findViewById(R.id.btn_remote);
        mButtonUp.setOnTouchListener(btnTouchListener);
        mButtonDown.setOnTouchListener(btnTouchListener);
        mButtonLeft.setOnTouchListener(btnTouchListener);
        mButtonRight.setOnTouchListener(btnTouchListener);
        mButtonCenter.setOnTouchListener(btnTouchListener);
        mButtonA.setOnTouchListener(btnTouchListener);
        mButtonB.setOnTouchListener(btnTouchListener);
        mButtonLeftMenu.setOnTouchListener(btnTouchListener);

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
                case R.id.btn_remote:{

                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        mLeftMenu.openDrawer(Gravity.LEFT);
                    }
                    break;
                }
            }

            return false;
        }
    };
    View.OnClickListener btnClickLinstener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.remote_up_btn:{
                    sendMessage("1");
                }
                break;
                case R.id.remote_down_btn:{
                    sendMessage("2");
                }
                break;
                case R.id.remote_left_btn:{
                    sendMessage("3");
                }
                break;
                case R.id.remote_right_btn:{
                    sendMessage("4");
                }
                break;
                case R.id.remote_center_btn:{
                    sendMessage("5");
                }
                break;
                case R.id.remote_A_btn:{
                    sendMessage("6");
                }
                break;
                case R.id.remote_B_btn:{
                    sendMessage("7");
                }
                break;
            }
        }
    };
    private boolean sendMessage(String message){
        if(message == null){
            Log.d(TAG,"error:send message null");
            return false;
        }

        boolean ans = mConnectionManager.sendData(message.getBytes());
        //  if(!ans)Toast.makeText(BluetoothActivity.this,getString(R.string.send_fail),Toast.LENGTH_SHORT).show();
        if(!ans)Log.d(TAG,"send message error");
        return ans;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==REQUEST_CODE_BTDEVICE&&resultCode ==RESULT_OK){//另一个activity的回答
            String deviceAddr = data.getStringExtra("DEVICE_ADDR");//传回来的地址
            mConnectionManager.connect(deviceAddr);
            Log.d(TAG,"get device success ");
        }else{
            Log.d(TAG,"get device error ");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.connected_menu,menu);
        mConnectionMenuItem = menu.findItem(R.id.connect_menu);
        updateUI();
        return true;
    }
    private void updateUI() {
        Log.d(TAG, "update UI");
        if(mConnectionManager==null)return;
        if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTED){
            mConnectionMenuItem.setTitle(R.string.disconnect);
        }else if(mConnectionManager.getCurrentConnectState() ==ConnectionManager.CONNECT_STATE_CONNECTING){
            mConnectionMenuItem.setTitle(R.string.cancel);
        }else if(mConnectionManager.getCurrentConnectState() == ConnectionManager.CONNECT_STATE_IDLE){
            mConnectionMenuItem.setTitle(R.string.connect);
        }
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
                    Intent i = new Intent(RemoteActivity.this,ConnectActivity.class);
                    startActivityForResult(i,REQUEST_CODE_BTDEVICE);
                }
                updateUI();
            }
            return true;
            case R.id.about_menu:{
                Intent i = new Intent(RemoteActivity.this,AboutActivity.class);
                startActivity(i);
            }
            return true;
            default: return false;
        }
    }
}
