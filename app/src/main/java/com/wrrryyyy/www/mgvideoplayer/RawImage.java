package com.wrrryyyy.www.mgvideoplayer;

import android.util.Log;

/**
 * Created by aa on 2018/10/6.
 */

public  class RawImage{
    private  final String TAG ="Utils";
    public static final int IMAGE_COLOR = 0;//我们这里统一规定一下
    public static final int IMAGE_GRAY = 1;
    public static final int IMAGE_BINARY = 2;//二值化
    private int type =IMAGE_COLOR;
    public int col;//列数 x
    public int row;//行数 y
    private byte[] rawData = null;
    public boolean useable = false;
    RawImage(int type,int col,int row,byte[] data){
        imageChange(type,col,row);
        setData(data);
        Log.d(TAG+"RawImage","type:"+type+"data "+getDataSize()+" "+" size"+col+" "+row);
    }
    public void imageChange(int type,int col,int row){
        this.type = type;
        this.row = row;
        this.col = col;
        useable = false;
    }
    public void setNewSize(int col,int row){
        imageChange(type,col,row);
    }
    private double getDataSize(){
        double size = 1;
        switch (type){
            case IMAGE_COLOR:{
                size = 2;
                break;
            }
            case IMAGE_GRAY:{
                size = 1;
                break;
            }
            case IMAGE_BINARY:{
                size = 1.0/8;
                break;
            }
        }
        return size;
    }

    public int getType() {
        return type;
    }

    /**
     * 改变了数据的类型之后需要调用这个函数 不然这个类不可用。
     */
    public void cleanData(byte[] data){
        if(data!=null&&data.length>4){

        }
    }
    public void setData(byte[] data){
        if(data!=null){
            int dataLength = (int)(getDataSize()*row*col);
            if(data.length==dataLength){
                Log.d(TAG,"getData");
                rawData = data;
                useable = true;
            }else{
                Log.d(TAG+"RawImage",""+getDataSize()+"send data to raw image  error: data not fit"+col+" "+row+" "+data.length+" "+dataLength);
                rawData = data;
            }
        }
    }
    /*
    * 返回某个点的原始数据
    * */
    public byte[] getPointData(int x,int y){
        double dataLength = getDataSize();
        int dataPos = (int)((y*col+x)*dataLength);//拿到以后强制转型
        Log.d(TAG,"get point data pos"+dataPos+","+x+","+y);
        byte b[];
        if(dataLength>=1){
            b = new byte[(int)dataLength];
        }else{
            //dataLength = 1;
            b = new byte[1];//最少取一格
        }
        if(!useable||dataPos+dataLength>=rawData.length)return b;
        for(int i=0;i<b.length;i++){
            b[i] = rawData[dataPos+i];
        }
        return b;
    }
    public byte[] getRawData(){
        return rawData;
    }
}
class BooleanFlag{
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