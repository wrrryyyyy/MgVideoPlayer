package com.wrrryyyy.www.mgvideoplayer;

import android.util.Log;

import org.opencv.core.Mat;

/**
 * Created by aa on 2018/9/22.
 */

public class ImageSelector{
    private static final String TAG = "imageSelector";
    private static final int DEF_CURSOR_SIZE = 5;
    private static final int CANVAS_SIZE_X = 1000;
    private static final int CANVAS_SIZE_Y = 1000;
    public static final int MOVE_UP = 0;
    public static final int MOVE_DOWN = 1;
    public static final int MOVE_LEFT = 2;
    public static final int MOVE_RIGHT = 3;
    private int[] centerColor = {-1,-1,-1};//这边如果是灰度就三个都一样的
    private boolean colorChange = false;
    private int selectorX = 0;
    private int selectorY = 0;
    private int canvasX ;
    private int canvasY ;
    private boolean posChange = true;
    private boolean visible = true;
    public int getColor(int i){
        if(i<centerColor.length&&i>=0){
            return centerColor[i];
        }
        return -1;
    }
    public void visibleChange(){
        if(visible == true){
            disappear();
        }else{
            appear();
        }
    }
    public void disappear(){
        if(visible!=false)posChanging();//有跳变 丢个改位置的
        visible = false;
    }
    public void appear(){
        if(visible!=true)posChanging();//有跳变 丢个改位置的
        visible = true;
    }
    public boolean getVisible(){
        return visible;
    }
    public void refresh(){
        posChange = false;
    }
    public void colorRefresh(){
        colorChange = false;
    }
    public boolean needFreshColor(){return colorChange;}

    public boolean needFresh(){return posChange;}

    private void posChanging(){
        posChange  = true;
    }
    public ImageSelector(int canvasX,int canvasY){
        this.canvasX = canvasX;
        this.canvasY = canvasY;
        if(canvasX<=0)canvasX = CANVAS_SIZE_X;
        if(canvasY<=0)canvasY = CANVAS_SIZE_Y;
    }
    public ImageSelector(){
        this(CANVAS_SIZE_X,CANVAS_SIZE_Y);
    }
    public void selectorMoveTo(double perX,double perY){
        double x = perX ,y=perY;
        if(perX<0)x = 0;
        if(perX>1)x = 1;
        if(perY<0)y = 0;
        if(perY>1)y = 1;
        selectorX = (int)(x*canvasX);
        selectorY = (int)(y*canvasY);
        posChanging();
    }
    /*
    * move 移动方向
    * step 移动步数
    * */
    public void selectorMove(int move,int step){
        int moveX=0,moveY=0;
        switch(move){
            case MOVE_UP:{
                moveY = -step;
                break;
            }
            case MOVE_DOWN:{
                moveY = step;
                break;
            }
            case MOVE_LEFT:{
                moveX = -step;
                break;
            }
            case MOVE_RIGHT:{
                moveX = step;
                break;
            }
            default:{
                Log.d(TAG,"selectorMove error:unknow input");
                return ;
            }
        }
        selectorX += moveX;
        selectorY += moveY;
        if(selectorX>=canvasX)selectorX = canvasX-1;
        if(selectorY>=canvasY)selectorY = canvasY-1;
        if(selectorX<0)selectorX = 0;
        if(selectorY<0)selectorY = 0;
        posChanging();
    }
    public void selectorMove(int move,double step){
        if(step>0&&step<=1){
            selectorMove(move,(int)(step*canvasX));
        }
    }
    public boolean writeSelector(Mat mat){
        return wirteSelector(mat,new int[]{-1,-1,-1},DEF_CURSOR_SIZE);
    }

    public boolean writeSelectorBlack(Mat mat){
        return wirteSelector(mat,new int[]{0,0,0},DEF_CURSOR_SIZE);
    }
    public boolean writeSelectorTheme(Mat mat){
        return wirteSelector(mat,new int[]{0xff,0xc1,0x09},DEF_CURSOR_SIZE);
    }
    public boolean writeSelectorWhite(Mat mat){
        return wirteSelector(mat,new int[]{255,255,255},DEF_CURSOR_SIZE);
    }
    public double[] getSelectorPos(){
        double out[] = new double[2];
        out[0] = (double)selectorX/canvasX;
        out[1] = (double)selectorY/canvasY;
        return out;
    }

    public boolean wirteSelector(Mat mat,int[]color,int squareSize){
        if(mat!=null&&color.length==3){
            if(visible){
                if(mat.channels()==1){//灰度只有一个通道
                    Log.d(TAG,"get mono image");
                }else if(mat.channels()==3){//彩色的有三个通道
                    Log.d(TAG,"get color image");
                }
                int matX = mat.cols()*selectorX/canvasX;
                int matY = mat.rows()*selectorY/canvasY;
                matX = matX>=mat.cols()?mat.cols()-1:matX;
                matY = matY>=mat.rows()?mat.rows()-1:matY;
                Log.d(TAG,"c&r"+matX+" "+matY);
                int square = squareSize>1?squareSize/2:1;//最小3*3
                for(int i = 0;i<mat.rows();i++){//行 y
                    int posX = matX;
                    int posY = i;
                    if(i<matY-square||i>matY+square){
                        wirtePoint(mat,posX,posY,color);
                    }
                }
                for(int i = 0;i<mat.cols();i++){//列 x
                    int posX = i;
                    int posY = matY;
                    if(i<matX-square||i>matX+square){
                        wirtePoint(mat,posX,posY,color);
                    }
                }
                for(int i = -square;i<=square;i++){//画个框
                    for(int j = -square;j <= square;j++){
                        if((i!=-square&&i!=square)&&(j!=-square&&j!=square))continue;
                        int posX = matX + j;
                        int posY = matY + i;
                        if(posX<0||posY<0||posX>=mat.cols()||posY>=mat.rows())continue;;
//                    if(posX<0)posX = 0;
//                    if(posX>=mat.cols())posX = mat.cols()-1;
//                    if(posY <0)posY = 0;
//                    if(posY>=mat.rows())posY = mat.rows()-1;
                        wirtePoint(mat,posX,posY,color);
                    }
                }
                checkSelectColor(mat,matX,matY);//查看是否变色
//            for(int i=0;i<centerColor.length;i++){
//                int temp = (int)mat.get(matY,matX)[i];
//                if(temp!=centerColor[i]){
//                    centerColor[i] = temp;
//                    colorChange = true;
//                }
//            }
//            if(temp!=centerColor[0])colorChange = true;
//            centerColor[0] = temp;
//            centerColor[1] = (int)mat.get(matY,matX)[1];
//            centerColor[2] = (int)mat.get(matY,matX)[2];
            }
            refresh();
            Log.d(TAG,"color:R "+centerColor[0]+" g "+centerColor[1]+" b "+centerColor[2]);
        }
        return true;
    }
    private void checkSelectColor(Mat mat,int matX,int matY){
        for(int i=0;i<centerColor.length;i++){
            int temp = (int)mat.get(matY,matX)[i];
            if(temp!=centerColor[i]){
                centerColor[i] = temp;
                colorChange = true;
            }
        }
    }
    private void wirtePoint(Mat mat,int posX,int posY,int[]color){
        byte[] brgb = new byte[3];
        if(color[0] <0||color[0]>255){//反色
            brgb[0] = (byte)(255-(int)mat.get(posY,posX)[0]);
            brgb[1] = (byte)(255-(int)mat.get(posY,posX)[1]);
            brgb[2] = (byte)(255-(int)mat.get(posY,posX)[2]);
        }else{
            brgb[0] = (byte)color[0];
            brgb[1] = (byte)color[1];
            brgb[2] = (byte)color[2];
        }
        mat.put(posY,posX,brgb);
    }
}
