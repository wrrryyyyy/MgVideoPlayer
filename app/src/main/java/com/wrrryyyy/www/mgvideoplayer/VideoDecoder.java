package com.wrrryyyy.www.mgvideoplayer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

/**
 * Created by aa on 2018/8/28.
 */

public class VideoDecoder {
    private static  final String TAG = "VideoDecoder";
    public VideoDecoder(){

    }
    //把byte[]转型成bitmap
    public static Bitmap creatBitmapByByte(int type,byte[] data,Size size,ImageSelector is){
        if(type ==RawImage.IMAGE_COLOR){
            return decodeColorByByte(data,size,is);
        }else if(type ==RawImage.IMAGE_GRAY){
            return decodeMonoByByte(data,size,is);
        }else if(type == RawImage.IMAGE_BINARY){
            return decodeBinaryByByte(data,size,is);
        }
        Log.d(TAG,"error:creatBitmap get a invalid type");
        return null;
    }
    public static Bitmap decodeColorByByte(byte[] data,Size size,ImageSelector is){//data 宽 高
        if(size.height*size.width*2!=data.length){
            Log.d(TAG,"error:color data size different that input size "+size.height*size.width);
            return null;
        }
        Bitmap bitmap =Bitmap.createBitmap((int)size.width,(int)size.height, Bitmap.Config.ARGB_4444) ;
        Mat mat = Mat.zeros(size,CV_8UC3);
        int count = 0;
        for(int i =0;i<mat.rows();i++){//y
            for(int j=0;j<mat.cols();j++){//x
                int rgb[] = rgb565to888((data[count]),data[count+1]);
                count+=2;
                byte[] brgb = new byte[3];
                brgb[0] = (byte)rgb[0];
                brgb[1] = (byte)rgb[1];
                brgb[2] = (byte)rgb[2];
                mat.put(i,j,brgb);
            }
        }
        if(is!=null){
            Imgproc.resize(mat,mat,size);
             is.writeSelector(mat);
        }
        Utils.matToBitmap(mat,bitmap);
        Log.d(TAG,"bitmap size:"+bitmap.getWidth()+" "+bitmap.getHeight());
        return bitmap;
    }
    public static Bitmap decodeMonoByByte(byte[] data,Size size,ImageSelector is){//data 宽 高
        if(size.height*size.width!=data.length){//80*60*1
            Log.d(TAG,"error:mono data size different that input size "+size.height*size.width);
            return null;
        }
        Bitmap bitmap =Bitmap.createBitmap((int)size.width,(int)size.height, Bitmap.Config.ARGB_4444) ;
        Mat mat = Mat.zeros(size,CV_8UC3);
        int count = 0;
        for(int i =0;i<mat.rows();i++){//y
            for(int j=0;j<mat.cols();j++){//x
                byte[] brgb = new byte[3];
                brgb[0] = data[count];
                brgb[1] = data[count];
                brgb[2] = data[count];
                count++;
                mat.put(i,j,brgb);
            }
        }
        if(is!=null){
            Imgproc.resize(mat,mat,size);
            is.writeSelectorTheme(mat);
        }
        Utils.matToBitmap(mat,bitmap);
        Log.d(TAG,"bitmap size:"+bitmap.getWidth()+" "+bitmap.getHeight());
        return bitmap;
    }
    public static Bitmap decodeBinaryByByte(byte[] data,Size size,ImageSelector is){//data 宽 高
        if(data==null||size.height*size.width!=data.length*8){//80*60/8
            Log.d(TAG,"error:binary data size different that input size "+size.height*size.width+" "+(data!=null?data.length:-1));
            return null;
        }
        Bitmap bitmap =Bitmap.createBitmap((int)size.width,(int)size.height, Bitmap.Config.ARGB_4444) ;
        Mat mat = Mat.zeros(size,CV_8UC3);
        int count = 0,countBit = 0;
        for(int i =0;i<mat.rows();i++){//y
            for(int j=0;j<mat.cols();j++){//x
                byte[] brgb = new byte[3];
                boolean black = false;
                int mark = 1<<countBit;
                black = (0==(data[count]&mark));
                if(black){
                    brgb[0] = 0;
                    brgb[1] = 0;
                    brgb[2] = 0;
                }else{
                    brgb[0] = -1;//255
                    brgb[1] = -1;
                    brgb[2] = -1;
                }
                countBit++;
                if(countBit>=8){
                    countBit = 0;
                    count++;
                }
                mat.put(i,j,brgb);
            }
        }
        if(is!=null){
            Imgproc.resize(mat,mat,size);
            is.writeSelectorTheme(mat);
        }
        Utils.matToBitmap(mat,bitmap);
        Log.d(TAG,"bitmap size:"+bitmap.getWidth()+" "+bitmap.getHeight());
        return bitmap;
    }
    public static String byte2HexString(byte[] data){
        String out = "";
        final char[] list= {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        for(byte b:data){
            char c = list[(b&0x0f)];
            char cc = list[((b>>4)&0x0f)];
            out +=cc;
            out+=c;
        }
        return out;
    }
    public static int[] getAntiColor(int[] data){
        int out[];
        if(data.length==3){
            out = new int[3];
            for(int i=0;i<3;i++){
                if(data[i]<=255){
                    out[i] = 255-data[0];
                }else{
                    out[i] = 0;
                    Log.d(TAG,"get anti color error:color out of range");
                }
            }
            return out;
        }
        return null;
    }
    public static Vector<Integer> decodeBinary(Bitmap bitmap, Size size){
        return decodeBinary(bitmap,size,null);
    }
    public static Vector<Integer> decodeBinary(Bitmap bitmap, Size size, Bitmap outMat){
        return decodeBinary(bitmap,size,120,null);
    }
    //threshold <100
    public static Vector<Integer> decodeBinary(Bitmap bitmap, Size size, int threshold,Bitmap outMat){
        Vector<Integer> v = new Vector<Integer>();
        long countTime2 = System.currentTimeMillis();
        Log.d(TAG,"time:D"+(System.currentTimeMillis()-countTime2));
        countTime2 = System.currentTimeMillis();
        Mat grayMat=new Mat();
        Mat rgbMat = new Mat();
        Mat mat = Mat.zeros(size,CV_8UC1);
        Utils.bitmapToMat(bitmap,rgbMat);
        Imgproc.cvtColor(rgbMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(grayMat,mat,size);
        Mat mat1 = Mat.zeros(mat.size(),CV_8UC1);
        if(threshold<0)threshold = 0;
        if(threshold>255)threshold = 255;
        int kmax = mat.rows()/8+(mat.rows()%8==0?0:1);
        int count = 0;
        int elem = 0;
         for(int j=0;j<mat.rows();j++){//y
             for(int i = 0;i<mat.cols();i++){//x
                int c =0;
                if(mat.get(j,i)[0]>threshold){
                    mat1.put(j,i,255);
                    c=1;
                }else{
                    mat1.put(j,i,0);
                }
                elem<<=1;
                elem +=c;
                if((count++)==7){
                    count = 0;
                    v.addElement(elem);
                    elem = 0;
                }
            }
        }
        if(outMat!=null){
            Mat outputMat = new Mat();
            Imgproc.resize(mat1,outputMat,rgbMat.size());
            Utils.matToBitmap(outputMat,outMat);//这个要求bitmap的大小和mat的一直 不然会报错
        }
        Log.d(TAG,"-----------kmax "+kmax + " |row "+mat.rows()+" |cols "+mat.cols()+"v size "+v.size()+"---------------");
        return v;
    }
    public static Vector<Integer> decodeBinary4Oled(Bitmap bitmap, Size size, Bitmap outMat){//这个的输出是按oled的来的 默认Y一定是8的倍数..
        Vector<Integer> v = new Vector<Integer>();
        long countTime2 = System.currentTimeMillis();
        Log.d(TAG,"time:D"+(System.currentTimeMillis()-countTime2));
        countTime2 = System.currentTimeMillis();
        Mat grayMat=new Mat();
        Mat rgbMat = new Mat();
        Mat mat = Mat.zeros(size,CV_8UC1);
        Utils.bitmapToMat(bitmap,rgbMat);
        Imgproc.cvtColor(rgbMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(grayMat,mat,size);
        Mat mat1 = Mat.zeros(mat.size(),CV_8UC1);
        int kmax = mat.rows()/8+(mat.rows()%8==0?0:1);
        for(int k=0;k<kmax;k++){
            for(int i=0;i<mat.cols();i++){
                int c = 0;
                for(int j=7;j>=0;j--){
                    int jj= k*8+j;
                    c = c<<1;
                    if(mat.get(jj,i)==null){//k有的时候除不正 给他填空
                        mat1.put(jj,i,255);
                        c|=0;
                        continue;
                    }
                    if(mat.get(jj,i)[0]>120){
                        mat1.put(jj,i,255);
                        c|=0;
                    }else{
                        mat1.put(jj,i,0);
                        c|=1;
                    }
                }
                v.addElement(new Integer(c));
            }
        }

        if(outMat!=null){
            Mat outputMat = new Mat();
            Imgproc.resize(mat1,outputMat,rgbMat.size());
            Utils.matToBitmap(outputMat,outMat);
        }
        Log.d(TAG,"-----------kmax "+kmax + " |row "+mat.rows()+" |cols "+mat.cols()+"v size "+v.size()+"---------------");
        return v;
    }
    public static Vector<Integer> decodeColor(Bitmap bitmap,Size size,Bitmap outMat){
        Vector<Integer> v = new Vector<Integer>();
        long countTime2 = System.currentTimeMillis();
        Log.d(TAG,"time:D"+(System.currentTimeMillis()-countTime2));
        countTime2 = System.currentTimeMillis();
        Mat grayMat=new Mat();
        Mat rgbMat = new Mat();
        Mat mat = Mat.zeros(size,CV_8UC3);
        Utils.bitmapToMat(bitmap,rgbMat);
  //      Imgproc.cvtColor(rgbMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(rgbMat,mat,size);
        Mat mat1 = Mat.zeros(mat.size(),CV_8UC3);
        int kmax = mat.rows()/8+(mat.rows()%8==0?0:1);
        for(int i =0;i<mat.rows();i++){
            for(int j=0;j<mat.cols();j++){
                int rgb[] = rgb888to565((int)mat.get(i,j)[0],(int)mat.get(i,j)[1],(int) mat.get(i,j)[2]);
                v.addElement(new Integer(rgb[0]));
                v.addElement(new Integer(rgb[1]));
            }
        }

//        if(outMat!=null){
//            Mat outputMat = new Mat();
//            Imgproc.resize(mat1,outputMat,rgbMat.size());
//            Utils.matToBitmap(outputMat,outMat);
//        }
        Log.d(TAG,"-----------kmax "+kmax + " |row "+mat.rows()+" |cols "+mat.cols()+"v size "+v.size()+"---------------");
        return v;
    }
    public static Vector<Integer> decodeColor(Mat mmat,Size size,Bitmap outMat){
        Vector<Integer> v = new Vector<Integer>();
        long countTime2 = System.currentTimeMillis();
        Log.d(TAG,"time:D"+(System.currentTimeMillis()-countTime2));
        countTime2 = System.currentTimeMillis();
        Mat grayMat=new Mat();
        Mat rgbMat = new Mat();
        Mat mat = Mat.zeros(size,CV_8UC3);
        rgbMat = mmat;
        if(rgbMat==null||rgbMat.empty()){
            Log.d(TAG,"error:get empty input");
        }
        //      Imgproc.cvtColor(rgbMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(rgbMat,mat,size);
        Mat mat1 = Mat.zeros(mat.size(),CV_8UC3);
        int kmax = mat.rows()/8+(mat.rows()%8==0?0:1);
        for(int i =0;i<mat.rows();i++){
            for(int j=0;j<mat.cols();j++){
                int rgb[] = rgb888to565((int)mat.get(i,j)[0],(int)mat.get(i,j)[1],(int) mat.get(i,j)[2]);
                v.addElement(new Integer(rgb[0]));
                v.addElement(new Integer(rgb[1]));
            }
        }

//        if(outMat!=null){
//            Mat outputMat = new Mat();
//            Imgproc.resize(mat1,outputMat,rgbMat.size());
//            Utils.matToBitmap(outputMat,outMat);
//        }
        Log.d(TAG,"-----------kmax "+kmax + " |row "+mat.rows()+" |cols "+mat.cols()+"v size "+v.size()+"---------------");
        return v;
    }
    public static Vector<Point> getPointOutSite(){
        return null;
    }
    public static Vector<Point> getPointInSite(){
        return null;
    }
    public boolean drawBitmapContour(Bitmap bm,int x,int y,Mat m){
        if(m==null)return false;

        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(Color.CYAN);
        paint.setStrokeWidth(5);
        canvas.drawCircle((float)x,(float)y,25,paint);
        Utils.bitmapToMat(bm,m);

        return true;
    }
    public void running(Mat rbgPhoto, Scalar low, Scalar high){
//        if(rbgPhoto==null||low==null||high==null)return;
//        Mat m = new Mat();
//        Mat hvsMat = new Mat();
//        Mat mMark = new Mat();
//        Mat mGray = new Mat();
//        Imgproc.pyrDown(rbgPhoto,m);
//        Imgproc.pyrDown(m,m);
//        Imgproc.cvtColor(m,hvsMat,Imgproc.COLOR_RGB2HSV_FULL);
//        Core.inRange(hvsMat,low,high,mMark);
//        Imgproc.morphologyEx(mMark,mMark,Imgproc.MORPH_OPEN,new Mat());
//        List<MatOfPoint>contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(mMark,contours,mHierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
//        double maxArea = 0;
//        Iterator<MatOfPoint> each = contours.iterator();
//        while(each.hasNext()){
//            MatOfPoint wrapper = each.next();
//            double area = Imgproc.contourArea(wrapper);
//            if(area>maxArea){
//                maxArea = area;
//            }
//        }
//        mContours.clear();
//        each = contours.iterator();
//        while(each.hasNext()){
//            MatOfPoint wrapper = each.next();
//            if(Imgproc.contourArea(wrapper)>mMinContourArea*maxArea){
//                Core.multiply(wrapper,new Scalar(4,4),wrapper);
//                mContours.add(wrapper);
//            }
//        }
    }
    public static Vector<Vector<Point>> getAllPoint(Bitmap bitmap,Bitmap outBitmap){
        //1 外圈轮廓点集
        //2 内圈轮廓点集
        Vector<Vector<Point>>vv = new Vector<>();
        Mat mat = new Mat();
        Mat hierarchy = new Mat();
        if(bitmap==null)return null;
        List<MatOfPoint> list = new ArrayList<>();
        Utils.bitmapToMat(bitmap,mat);
       // Imgproc.findContours(mat,list,);
     //   Imgproc.cvtColor(mat,mat,Imgproc.COLOR_BGR2GRAY);
     //  Core.inRange(mat,new Scalar(0,0,0),new Scalar(300,256,256),mat1);
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_BGR2GRAY);
        Mat mat1 = Mat.zeros(mat.size(),CV_8UC1);
        for(int j=0;j<mat.rows();j++){//y
            for(int i = 0;i<mat.cols();i++){//x
                int c =0;
                if(mat.get(j,i)[0]>120){
                    mat1.put(j,i,0);
                }else{
                    mat1.put(j,i,255);
                }
            }
        }
        Imgproc.findContours(mat1,list,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
//        CV_RETR_EXTERNAL - 只提取最外层的轮廓
//        CV_RETR_LIST - 提取所有轮廓，并且放置在 list 中
//        CV_RETR_CCOMP - 提取所有轮廓，并且将其组织为两层的 hierarchy: 顶层为连通域的外围边界，次层为洞的内层边界。
//        CV_RETR_TREE - 提取所有轮廓，并且重构嵌套轮廓的全部 hierarchy
//        method
//        逼近方法 (对所有节点, 不包括使用内部逼近的 CV_RETR_RUNS).
//                CV_CHAIN_CODE - Freeman 链码的输出轮廓. 其它方法输出多边形(定点序列).
//                CV_CHAIN_APPROX_NONE - 将所有点由链码形式翻译(转化）为点序列形式
//                CV_CHAIN_APPROX_SIMPLE - 压缩水平、垂直和对角分割，即函数只保留末端的象素点;
//        CV_CHAIN_APPROX_TC89_L1,
//                CV_CHAIN_APPROX_TC89_KCOS - 应用 Teh-Chin 链逼近算法. CV_LINK_RUNS - 通过连接为 1 的水平碎片使用完全不同的轮廓提取算法。
        Imgproc.drawContours(mat, list, -1, new Scalar(200,10,20),2);
        for(MatOfPoint mp:list){
            Vector<Point> v = new Vector<>();
            v.addAll(mp.toList());
            vv.addElement(v);
        }
       // Point[] p  = list.get(0).toArray();
        Utils.matToBitmap(mat,bitmap);
        return vv;
    }

    public static int[] rgb888to565(int colorR,int colorG,int colorB){
        int rgb565 = 0;
        rgb565+= (colorR&0b11111000)<<8;
        rgb565+= (colorG&0b11111100)<<3;
        rgb565+= (colorB&0b11111000)>>3;
        int out[] ={0,0};
        out[0] = (rgb565&0xff00)>>8;
        out[1] = rgb565&0x00ff;
     //   Log.d(TAG,"rgb565"+out[0]+" "+out[1]) ;
        return out;
    }
    public static int[] rgb565to888(int high,int low){
       // int rgb565 = high<<8+low;
        int R,G,B;
        R = high&0b11111000;
        G = ((high&0b111)<<5)+((low&0b11100000)>>3);
        B = (low&0b00011111)<<3;
        int out[] ={0,0,0};
        out[0] = R;
        out[1] = G;
        out[2] = B;
        return out;
    }
    public static int[] rgb8to888(int color){
        int out[] ={0,0,0};
        out[0] = color;
        out[1] = color;
        out[2] = color;
        return out;
    }
    //这个函数有bug 怎么写都偏黑 迷
    public static int[] rgb565to888(long  rgb565){//没有写补偿的
        long R,G,B;
        R =  (rgb565&0b1111100000000000)>>8;
        G =  (rgb565&0b0000011111100000)>>3;
        B =  (rgb565&0b0000000000011111)<<3;
        int out[] ={0,0,0};
        out[0] = (int)R;
        out[1] = (int)G;
        out[2] = (int)B;
        //   Log.d(TAG,"rgb565"+out[0]+" "+out[1]) ;
        return out;
    }
    @NonNull
    public static String getStringByIS(InputStream inputStream, int count){
        InputStreamReader inputStreamReader = null;
        int counter = 0;
        try {
            inputStreamReader = new InputStreamReader(inputStream,"gbk");
        }catch (Exception e){
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String str;
        try{
            while((str = reader.readLine())!=null&&count>=counter){
                if(count!=0)counter++;
                sb.append(str);
                sb.append("\n");
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        return sb.toString();
    }
    public static String[] getString(String sGet, String reg, int max) {
        long time = System.currentTimeMillis();
        String[] str = new String[max];
        Matcher matcher = Pattern.compile(reg).matcher(sGet);
        int i = 0;
        while (matcher.find()) {
            if ((matcher.group().equals(null)) || (i >= max))
                break;
            str[i] = matcher.group(1);
            i++;
        }
        if (System.currentTimeMillis() - time > 5L) Log.d("waring","getString:" + (System.currentTimeMillis() - time) + "       " + reg);
        //  System.out.println("getString:" + (System.currentTimeMillis() - time) + "       " + reg);
        return str;
    }
    public static String[] getStringTwice(String sGet, String reg) {
        long time = System.currentTimeMillis();
        String[] str ;//= new String[max];
        Vector<String> v = new Vector<String>();
        Matcher matcher = Pattern.compile(reg).matcher(sGet);
        while (matcher.find()) {
            if ((matcher.group().equals(null)) )
                break;
            //   str[i] = matcher.group(1);
            v.addElement(matcher.group(1));
        }
        str = new String[v.size()];
        for(int i=0;i<v.size();i++){
            str[i] = v.get(i);
        }
        if (System.currentTimeMillis() - time > 5L)Log.d("waring","getString:" + (System.currentTimeMillis() - time) + "       " + reg);
        return str;
    }
    public static int getIndexOf(byte[]data,byte[]sub){
        return getIndexOf(data,sub,0,data.length);
    }
    public static int getIndexOf(byte[]data ,byte[]sub,int start,int end){//从start开始 不到end
        //这边如果输入为0 还是返回的-1
        int subLength = sub.length;
        for(int i=start;i<end-sub.length;i++){
            if(data[i]==sub[0]){
                boolean same = true;
                for(int j=0;j<subLength;j++){
                    if(data[i+j]!=sub[j]){//如果不相同 说明不是这个子串
                        same = false;
                    }
                }
                if(same==true)return i;
            }
        }
        return -1;
    }
    public static int getLastIndexOf(byte[]data,byte[]sub){
        return getLastIndexOf(data,sub,0,data.length);
    }
    public static int getLastIndexOf(byte[]data ,byte[]sub,int start,int end){
        int subLength = sub.length;
        int index = -1;
        for(int i=start;i<end-sub.length;i++){
            if(data[i]==sub[0]){
                boolean same = true;
                for(int j=0;j<subLength;j++){
                    if(data[i+j]!=sub[j]){//如果不相同 说明不是这个子串
                        same = false;
                    }
                }
                if(same==true)index = i;
            }
        }
        return index;//默认-1 有匹配更新
    }

    public static int getColor(int num){
        int n = num%360;
        float hsv[] = new float[3];
        hsv[1] = 1;
        hsv[2] = 1;
        hsv[0] = n;
        return Color.HSVToColor(hsv);
    }
    public static int getColor16(int num){
        int n = num%16;
        if(n%2==0)n = (n+8)%16;
        n = n*360/16;
        return getColor(n);
    }
    public static  byte[] cutByte(byte[] buff,int start,int end){
        if(end>start&&start>=0&&buff!=null&&end<=buff.length){
            byte bb[] = new byte[end-start];
            for(int i=start;i<end;i++){
                bb[i-start] = buff[i];
            }
            return bb;
        }else{
            Log.d(TAG,"cutByte error:start "+start+" end "+end);
        }
        return null;
    }
    /*
* 切掉从start(包括)到end(不包括)中间的东西 返回剩下的
* 如切掉0位 cleanBuff(in,0,1);
* */
    public static byte[] cleanBuff(byte[] in,int start,int end){
        if(end>start&&start>=0&&in!=null&&end<=in.length){
            int count = 0;
            byte[] data = new byte[in.length-end+start];
            for(int i = 0;i<start;i++) {
                data[count++] = in[i];
            }
            for(int i=end;i<in.length;i++){
                data[count++] = in[i];
            }
            return data;
        }else{
                  Log.d(TAG,"cleanBuff error:start:"+start+" end "+end+" length ");
        }

        return in;
    }
    public static byte[] cutHead(byte[] data){
        if(data==null||data.length<4){
            return data;
        }
        byte bb[] = new byte[2];
        Integer it = Integer.valueOf("01", 16);
        bb[0] = (byte) it.intValue();
        it = Integer.valueOf("FE", 16);
        bb[1] = (byte) it.intValue();
        byte[] ans = new byte[data.length-4];
        if(data[0]==bb[0]&&data[1]==bb[1]&&data[data.length-1]==bb[1]&&data[data.length-2]==bb[0]){
            for(int i = 0;i<data.length-4;i++){
                ans[i] = data[i+2];
            }
            return ans;
        }
        return ans;
    }
    public static byte[] addupArray(byte[]a,byte[]b){
        byte[] dataA,dataB;
        if(a==null){
            if(b!=null)return b;
            dataA= new byte[0];
        }else{
            dataA = a;
        }
        if(b==null){
            return a;
        }else{
            dataB = b;
        }
        byte data[]= new byte[dataA.length+dataB.length];
        System.arraycopy(dataA,0,data,0,dataA.length);
        System.arraycopy(dataB,0,data,dataA.length,dataB.length);
        return data;
    }
    //java这边数据类型的长度和c有差异 int 32位 char 16位(甚至可以存中文)  byte 8 -128~127（这边没有255就很糟心） 然而(byte)129 = 0b10000001
    //然后对应到c这边                     32位       8位                       8 0~255
    //单纯的位运算 整数高位补0 负数高位补1 低位补0(不移动运算符) 然后对byte等的
    //有一种说法是转型会强制转成有符号的int 然后出问题 这里先不管
    //手动补个高位
    public static final int UINT8 =0;
    public static final int UINT16 =1;
    public static final int UINT32 =2;
    public static final int INT8 =3;
    public static final int INT16 =4;
    public static final int INT32 =5;
    public static final int INT64 =6;
    public static final int UINT64 =7;
    public static final int TYPE_STRING = 6;
    //public static final int TYPE_DOUBLE = 7;//可是我不想支持小数显示欸
    public static byte[] int2Bytes(long in,int inputType){//这边直接用int 最大16位也用不到顶
        byte[] out = new byte[4];
        long deal = in;
        int size = 0;
        boolean plus = true;
        switch(inputType){
            case UINT8:{
                size = 1;
                break;
            }
            case UINT16:{
                size = 2;
                break;
            }
            case UINT32:{
                size = 4;
                break;
            }
            case INT8:{
                size = 1;
                break;
            }
            case INT16:{
                size = 2;
                break;
            }
            case INT32:{
                size = 4;
                break;
            }
            case INT64:{
                size = 8;
                break;
            }
            case UINT64:{
                size = 8;
                break;
            }
            default:{
                Log.d(TAG,"change to byte error:"+in + " "+inputType);
                return null;
            }
        }
        out = new byte[size];
        for(int i=0;i<size;i++){
            out[size-i-1] =(byte) (deal&0xff);
            deal>>=8;
            Log.d(TAG,"int2Bytes"+out[size-i-1]);
        }
        return out;
    }
    public static int bytes2Int(byte[] data){
        int out = 0;
        for(int i = 0;i<data.length;i++){
            out<<=8;
            out |=data[i]&0xff;
        }
        Log.d(TAG,"byte2int"+out);
        return out;
    }
    public static int bytes2IntLittleEndian(byte[]data){

        int out = 0;
        for(int i = 0;i<data.length;i++){
            out<<=8;
            out |=data[data.length-1-i]&0xff;
        }
        Log.d(TAG,"byte2int"+out);
        return out;
    }
//    public static int bytes2Int(byte[] bytes)
//    {
//        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
//    }
    public static float bytes2Float(byte[] bytes)
{
    return Float.intBitsToFloat(bytes2Int(bytes));
}
    public static float bytes2FloatLittleEndian(byte[] bytes)
    {
        return Float.intBitsToFloat(bytes2IntLittleEndian(bytes));
    }

    public static byte[] int2Bytes(int data)
    {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }
    public static byte[] float2Bytes(float data)
    {
        int intBits = Float.floatToIntBits(data);
        return int2Bytes(intBits);
    }


}

