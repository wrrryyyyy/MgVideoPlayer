package com.wrrryyyy.www.mgvideoplayer;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by aa on 2018/9/26.
 */

public class MessageDecoder {
    public interface MessageSolver{
        //这个用来处理和activity的交互
        void messageStart();
        void whenError(int errorType,int errorSite);
    }
    private static final String TAG ="messageDecoder";
    public static final int SUCCESS = 1;
    public static final int ERROR_DATA_TOO_LONG = 2;
    public static final int ERROR_NOT_GET_ANY_MARK = 3;
    public static final int ERROR_NOT_GET_FRONT_MARK = 4;
    public static final int ERROR_NOT_GET_BACK_MARK = 5;
    public static final int NEED_MORE_DATA = 6;//如果是这个 则弹掉第一个元素的
    public static final int DATA_TOO_SHORT = 7;
    public static final int DATA_GET_END = 8;
    public static final int SUCCESS_ONCE = 9;
    public static final int ERROR_OTHER = 0;

    /*这里有几种情况 首先数据够的情况(长度大于最大可能长度)1 找不到前后标志位之一 弹出没找到OK 2 找到前后标志位 但是长度过长 弹过长OK
                    3 本来就没有后标志位 那么需要一个最大可能长度。 4 找到后标志位 进下一层OK
    数据不够的情况（小于最大可能长度 大于最小可能长度） 1 没有后标志位 保持OK 2 有后标志位但是找不到 保持OK 3 有后标志位且找到了 进下一层
    数据不够的情况（小于最小可能长度） 保持OK
     1 找不到前标志位(返标志位) 2 前标志位后数据过长,找不到后标志位 或者前标志位+数据过长
    3 数据不够找不到后标志位

    * */
    public static int decodeBytesByBytesData(byte[]data,int start,BytesData bd){
        if(data.length<start+bd.getBytesDataSize()){//长度小于上次位置加个标志大小 说明后面东西不够了
            return NEED_MORE_DATA;//去掉小于最小的情况
        }
        int site = start+bd.getFrontMark().length;//
        if(bd.haveBackMark()){//如果有下一个数 搜索
            int backSite = VideoDecoder.getIndexOf(data,bd.getBackMark(),site,data.length);
            if(backSite!=-1){//神奇的找到了 如果超了抛出 没超就丢下一层
                if(backSite-site<=bd.getMaxDataLength()){//长度小于最大长度 那么丢下一层
                    bd.setData(data,site,backSite);
            //        start = backSite;
                    //-------------To next-------------------------------
                    return SUCCESS_ONCE;
                }else{
                    return ERROR_DATA_TOO_LONG;
                }
            }else{//果不其然的没有找到
                if(data.length<start+bd.getMaxSize()){//然后实际长度比最后长度+整个mark的长度小 数据不够再等等
                    return NEED_MORE_DATA;
                }else{//数据够了没找到就抛出了
                    return  ERROR_NOT_GET_BACK_MARK;
                }
            }
        }else{//没用后标志位的情况下 data大小在这里大等于datasize 那么直接抓就是了
            bd.setData(data,site,site+bd.getDataSize());
            return SUCCESS;
        }
    }
    public static  int decodeBytesByBytesData(byte[]data,Vector<BytesData> vin){
        if(vin.size()<1){
            return ERROR_OTHER;
        }
        BytesData bytesData = vin.elementAt(0);
        int markSize = bytesData.getMarkSize();
        if(data.length<=markSize){//虽然不大可能的数据为空
            return NEED_MORE_DATA;
        }
        int lastSite = VideoDecoder.getIndexOf(data,bytesData.getFrontMark());;
        if(lastSite == -1)return ERROR_NOT_GET_ANY_MARK;
        for(BytesData bd:vin){
            int stage = decodeBytesByBytesData(data,lastSite,bd);
            if(stage==SUCCESS_ONCE){
                lastSite += bd.getDataSize()+bd.getFrontMark().length;//位置往前面走一个data和一个头的长度
            }else{
                return stage;
            }
        }
        return SUCCESS;
    }


}
