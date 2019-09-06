package com.wrrryyyy.www.mgvideoplayer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;

public class ConnectActivity extends AppCompatActivity {
    private ListView mDeviceListView ;
    final String TAG = "connectActivity";
    final int BT_SEARCH_STATE_SEARCHING = 1;
    final int BT_SEARCH_STATE_IDLE = 2;
    private int mSearchingStage = BT_SEARCH_STATE_IDLE;
    private BluetoothAdapter mBluetoothAdapter;
    MenuItem mSearchMenuItem;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG,"BT device found:"+device.getName());
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                    DeviceItemAdapter adapter = (DeviceItemAdapter)mDeviceListView.getAdapter();
                    adapter.add(device);
                    adapter.notifyDataSetChanged();
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Log.d(TAG, "BT device discover started");
                mSearchingStage = BT_SEARCH_STATE_SEARCHING;
                updateUI();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d(TAG, "BT device discover final");
                mSearchingStage = BT_SEARCH_STATE_IDLE;
                updateUI();
            }else{
                Log.d(TAG, "BT device discover  unknow");
            }
        }
    };
    private void updateUI() {

        switch (mSearchingStage)
        {
            case BT_SEARCH_STATE_IDLE:
            {
                if(mSearchMenuItem != null)
                {
                    mSearchMenuItem.setTitle(R.string.search);
                }
            }
            break;

            case BT_SEARCH_STATE_SEARCHING:
            {

                if(mSearchMenuItem != null)
                {
                    mSearchMenuItem.setTitle(R.string.cancel);
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        DeviceItemAdapter adapter = new DeviceItemAdapter(this,R.layout.device_item);
        mDeviceListView = (ListView)findViewById(R.id.connectable_list);
        mDeviceListView.setAdapter(adapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                ArrayAdapter adapter = (ArrayAdapter) mDeviceListView.getAdapter();
                BluetoothDevice device  = (BluetoothDevice) adapter.getItem(position);
                Intent i = new Intent();
                i.putExtra("DEVICE_ADDR",device.getAddress());
                setResult(RESULT_OK,i);
                Log.d(TAG,"device selete");
                finish();
            }
        });

    //    mSearchMenuItem = (MenuItem)findViewById(R.id.device_list);
        //蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver,filter);
        updateDeviceList();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_menu,menu);
        mSearchMenuItem = menu.findItem(R.id.device_search_menu);
        updateUI();
        return true;
    }
    private void updateDeviceList(){
        DeviceItemAdapter adapter = (DeviceItemAdapter) mDeviceListView.getAdapter();
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
        int hasPermission = ActivityCompat.checkSelfPermission(ConnectActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ConnectActivity.this,
                    new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        } else {
            boolean ret = mBluetoothAdapter.startDiscovery();

            Log.d(TAG, "BT device discover about to start: ret=" + ret);
        }
        //获取未配对设备
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.device_search_menu:{
                if(mSearchingStage ==BT_SEARCH_STATE_IDLE){
                    if(mBluetoothAdapter.isDiscovering()){
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    updateDeviceList();

                }else if(mSearchingStage == BT_SEARCH_STATE_SEARCHING){
                    if(mBluetoothAdapter.isDiscovering()){
                        mBluetoothAdapter.cancelDiscovery();
                    }
                }
            }
            break;
            case android.R.id.home:{
                this.finish();
                break;
            }
        }
        return true;
    }
}
