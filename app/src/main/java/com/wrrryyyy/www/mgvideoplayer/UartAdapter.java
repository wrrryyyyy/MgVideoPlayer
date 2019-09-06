package com.wrrryyyy.www.mgvideoplayer;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by aa on 2018/9/13.
 */

public class UartAdapter extends ArrayAdapter<UartItem> {
    private final LayoutInflater mInflater;
    private final int mResource;

    public UartAdapter(@NonNull Context context, @LayoutRes int resource, List<UartItem> object) {
        super(context, resource, object);
        mInflater = LayoutInflater.from(context);
        mResource = resource;

    }
    //简单创建一个adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }
        UartItem item = getItem(position);//拿到指定的item
        TextView name = (TextView)convertView.findViewById(R.id.tv_uart_name);
        TextView data = (TextView)convertView.findViewById(R.id.tv_uart_data);
        TextView creatTime = (TextView)convertView.findViewById(R.id.tv_uart_creat_time);
        name.setText(item.name);
        data.setText(item.getValue());
        creatTime.setText(item.getCreatTime());
        return convertView;
    }
}
class UartItem{
    private int type;
    String name ;
    byte[] value;
    long creatTime;
    public UartItem(int type,String name,byte[]value,long creatTime){
        this.type = type;
        this.name = name;
        this.value = value;
        this.creatTime = creatTime;
    }
    private int decodeInt(){
        int out = VideoDecoder.bytes2Int(value);
        return out;
    }
    /**/
    public String getValue(){
        String out = "";
        switch(type){
            case VideoDecoder.UINT8:{
                out = ""+decodeInt();
                break;
            }

            case VideoDecoder.UINT16:{
                out = ""+decodeInt();
                break;
            }

            case VideoDecoder.UINT32:{
                out = ""+decodeInt();
                break;
            }
            case VideoDecoder.INT8:{
                out = ""+decodeInt();
                break;
            }
            case VideoDecoder.INT16:{
                out = ""+decodeInt();
                break;
            }
            case VideoDecoder.INT32:{
                out = ""+decodeInt();
                break;
            }
            case VideoDecoder.TYPE_STRING:{
                try {
                    out = new String(value,"GBK");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            default:{
                Log.d("UartItem","error get not fit type");
            }
        }
        return out;
    }
    public String getCreatTime(){
        String out = new SimpleDateFormat("mm:ss:SS").format(creatTime);
        return out;
    }

}