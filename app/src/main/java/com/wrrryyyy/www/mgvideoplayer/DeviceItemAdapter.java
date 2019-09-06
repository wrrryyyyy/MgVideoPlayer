package com.wrrryyyy.www.mgvideoplayer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wrrryyyy.www.mgvideoplayer.R;

/**
 * Created by aa on 2018/8/13.
 */

public class DeviceItemAdapter extends ArrayAdapter<BluetoothDevice> {
    private final LayoutInflater mInflater;
    private int mResource;
    public DeviceItemAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
 //       return super.getView(position, convertView, parent);
        if(convertView == null){
            convertView = mInflater.inflate(mResource,parent,false);
        }
        TextView name = (TextView) convertView.findViewById(R.id.device_name);
        TextView info = (TextView) convertView.findViewById(R.id.device_info);
        BluetoothDevice device = getItem(position);
        name.setText(device.getName());
        info.setText(device.getAddress());
        return convertView;
    }

}
