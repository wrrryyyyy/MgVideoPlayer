package com.wrrryyyy.www.mgvideoplayer;

/**
 * Created by aa on 2018/9/26.
 */
class BytesData{
    private int maxDataLength;
    private byte[] frontMark;
    private byte[] data;
    private byte[] backMark;
    private int dataSize = 0;
    private boolean haveBack = true;
    public BytesData(byte[]frontMark,byte[]backMark,byte[]data,int maxDataLength){
        this.frontMark = frontMark;
        this.backMark = backMark;
        this.data = data;
        this.maxDataLength = maxDataLength;
    }public BytesData(byte[]frontMark,byte[]backMark,int maxDataLength){
        this(frontMark,backMark,null,maxDataLength);
    }
    //声明一个没用后缀的数据 就像图片数据的最后一块
    public BytesData(byte[]frontMark,int maxDataLength){
        this(frontMark,null,null,maxDataLength);
        haveBack = false;
    }public BytesData(byte[]frontMark){
        this(frontMark,null,null,-1);
        haveBack = false;
    }
    public boolean haveBackMark(){
        return haveBack;
    }
    public void setData(byte[] data){
        this.data= data;
    }
    public void setData(byte[]dataSorce,int start,int end){
        setData(VideoDecoder.cutByte(dataSorce,start,end));
    }

    public int getMaxDataLength() {
        return maxDataLength;
    }

    public void setDataSize(int dataSize){
        this.dataSize = dataSize;
    }
    public byte[] getFrontMark(){return frontMark;}

    public byte[] getData() {
        return data;
    }
    public int getDataSize(){
        return dataSize;
    }
    public byte[] getBackMark() {
        return backMark;
    }
    public boolean useable(){
        if(haveBack){
            if(frontMark!=null&&data!=null&&backMark!=null){
                return true;
            }
        }else{
            if(frontMark!=null&&data!=null){
                return true;
            }
        }
        return false;

    }
    public int getMarkSize(){
        if(haveBack){
            return frontMark.length+backMark.length;
        }else{
            return frontMark.length;
        }
    }
    /*
    *获取整个的大小
    * */
    public int getBytesDataSize(){
//        if(data!=null){
//            return getMarkSize()+data.length;
//        }
        return getMarkSize()+dataSize;
    }
    //空返回-1 否则返回实际
    public int getRealDataSize(){
        if(data!=null)return data.length;
        return -1;
    }
    /*如果有限制长度返回长度 没有的话返回-1*/
    public int getMaxSize(){
        if(backMark==null&&dataSize==0){
            return -1;
        }
        if(backMark==null&&dataSize!=0){
            return getMarkSize()+maxDataLength;
        }
        return getMarkSize()+maxDataLength;
    }

}