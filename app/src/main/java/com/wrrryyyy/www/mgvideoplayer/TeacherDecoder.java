package com.wrrryyyy.www.mgvideoplayer;

import android.content.res.Resources;

/**
 * Created by aa on 2019/4/23.
 */

public class TeacherDecoder {
    final static byte COMMAND_MARK[] = {0x15,0x77};//
    //
    //输入一组数据 从其中扣第一个包 然后抛出去
    //
    static int seachTeacherCoder(byte[] bytes,int startPos,TeacherCoder code){

        if(startPos>=0&&bytes!=null&&bytes.length>TeacherCoder.CODE_SIZE){//输入不为空且长度比一个包大
            int markSite = startPos;//0
            boolean getUseablePackage = false;
            while(true){
                markSite = VideoDecoder.getIndexOf(bytes,COMMAND_MARK,markSite,bytes.length);//找下一个包
                if(markSite!=-1){
                    if(code!=null){
                        code.init(bytes,markSite);
                    }else{

                        code = new TeacherCoder(bytes,markSite);

                    }
                    if(code.useable==true){//是一个正确的包
                        return markSite+TeacherCoder.CODE_SIZE;
                    }
                }else{//等于-1说明搜索到头都没有
                    break;
                }
                markSite+=1;//往下一格开始搜索
            }
        }
        return -1;
    }

    void findNextCode(){

    }
}
class TeacherCoder{
    static final int CODE_SIZE = 40;
    static final int COMMAND_BACK_NEED_NEXT = 135;
    byte[] headMark = new byte[2];//0
    byte[] nonMark = new byte[4];
    byte command[] = new byte[4];
    byte data[] = new byte[28];
    byte[] tailMark = new byte[2];
    boolean  useable = false;
    public TeacherCoder (byte[] bytes){
        this(bytes,0);
    }
    public TeacherCoder (){
        this(null,0);
    }
    public TeacherCoder(byte[]bytes,int start){
        init(bytes,start);
//        if(start>=0&&bytes!=null&&bytes.length >= start+CODE_SIZE){
//            headMark[0] = bytes[start+0];
//            headMark[1] = bytes[start+1];
//            for(int i=0;i<4;i++){
//                command[i] = bytes[start+i+4];//1和2是空的
//            }
//            for(int i=0;i<28;i++){
//                data[i] = bytes[start+i+8];
//            }//空2位
//            tailMark[0] = bytes[start+38];
//            tailMark [1] = bytes[start+39];
//            if(tailMark[0] == headMark[0]&&tailMark[1]==headMark[1]){
//                useable = true;
//            }
//        }
    }
    public int getCommand(){
        return VideoDecoder.bytes2IntLittleEndian(command);
    }
    public void init( byte[]bytes,int start ){

        if(start>=0&&bytes!=null&&bytes.length >= start+CODE_SIZE){
            headMark[0] = bytes[start+0];
            headMark[1] = bytes[start+1];
            for(int i=0;i<4;i++){
                command[i] = bytes[start+i+4];//1和2是空的
            }
            for(int i=0;i<28;i++){
                data[i] = bytes[start+i+8];
            }//空2位
            tailMark[0] = bytes[start+38];
            tailMark [1] = bytes[start+39];
            if(tailMark[0] == headMark[0]&&tailMark[1]==headMark[1]){
                useable = true;
            }
        }
    }
    public byte[] getData(){
        return data;
    }
    public float[] getDataFloat(){
        float[] f = new float[data.length/4];
        byte[] bytes = new byte[4];
        for(int i =0;i<data.length/4;i++){
            for(int j=0;j<4;j++){
                bytes[j] = data[i*4+j];
            }
            f[i] = VideoDecoder.bytes2FloatLittleEndian(bytes);
        }

        return f;
    }

     public byte[] getCommandByte(){
        return command;
    }

}
class ArmCoder{
    static final int COMMAND_UP = 29;
    static final int COMMAND_DOWN = 30;
    static final int COMMAND_LEFT = 31;
    static final int COMMAND_RIGHT = 32;

    static final int COMMAND_FORWARE= 33;
    static final int COMMAND_BACKWARE= 34;
    static final int COMMAND_SELECT= 35;
    static final int COMMAND_STOP= 40;
    static final int COMMAND_ZERO= 41;
    static final int COMMAND_G00[] ={ 00, 60, 70};
    static final int COMMAND_G01[] ={ 01, 61, 71};
    static final int COMMAND_G02[] ={ 02, 62, 72, 82};
    static final int COMMAND_G03[] ={ 03, 63, 73, 82};
    static final int COMMAND_G28 =28;
    static final int CODE_SIZE = 13;

    byte[] defMark = {0x77,0x15};
    byte[] headMark = new byte[2];
    byte command = 0;
    byte data[] = new byte[8];
    byte[] tailMark = new byte[2];
    private byte[] all = new byte[CODE_SIZE];
    boolean  useable = false;
    public ArmCoder (byte[] bytes){
        this(bytes,0);
    }
    public ArmCoder (){
        this(null,0);
    }
    public ArmCoder(byte command){
        headMark[0] = defMark[0];
        headMark[1] = defMark[1];
        tailMark[0] = defMark[1];
        tailMark[1] = defMark[0];
        this.command = command;
    }
    public ArmCoder(byte command,float A){
        this(command);
        byte[] bytes = VideoDecoder.float2Bytes(A);
        for(int i = 0 ;i<4;i++){
            data[i] = bytes[i];
        }
    }
    public ArmCoder(byte command,float A,float B){
        this(command);
        byte[] bytes = VideoDecoder.float2Bytes(A);
        for(int i = 0 ;i<4;i++){
            data[i] = bytes[i];
        }
        bytes = VideoDecoder.float2Bytes(B);
        for(int i = 0 ;i<4;i++){
            data[i+4] = bytes[i];
        }
    }
    public ArmCoder(byte command,int A,int B){
        this(command);
        byte[] bytes = VideoDecoder.int2Bytes(A);
        for(int i = 0 ;i<4;i++){
            data[i] = bytes[i];
        }
        bytes = VideoDecoder.int2Bytes(B);
        for(int i = 0 ;i<4;i++){
            data[i+4] = bytes[i];
        }
    }
    public ArmCoder(byte[]bytes,int start){

        if(start>=0&&bytes!=null&&bytes.length >= start+CODE_SIZE){
            headMark[0] = bytes[start+0];
            headMark[1] = bytes[start+1];
            command = bytes[start+2];
            for(int i=0;i<8;i++){
                data[i] = bytes[start+i+3];
            }//空2位
            tailMark[0] = bytes[start+11];
            tailMark [1] = bytes[start+12];
            useable = true;
        }
    }
    public void init( byte[]bytes,int start ){
        if(start>=0&&bytes!=null&&bytes.length >= start+CODE_SIZE){
            headMark[0] = bytes[start+0];
            headMark[1] = bytes[start+1];
            command = bytes[start+2];
            for(int i=0;i<8;i++){
                data[i] = bytes[start+i+3];
            }//空2位
            tailMark[0] = bytes[start+11];
            tailMark [1] = bytes[start+12];
            useable = true;
        }
    }
    public byte[] getData(){
        return data;
    }

    public byte[] getAll(){
        all[0] = headMark[0];
        all[1] = headMark[1];
        all[2] = command;
        for(int i=0;i<8;i++){
            all[i+3] = data[i];
        }//空2位
        all[11] = tailMark[0];
        all[12] = tailMark[1];
        return all;
    }
    public byte[] setAll(byte[] source, int begin){
        if(source ==null||source.length<begin+CODE_SIZE||begin<0)return source;
        getAll();
        for(int i=0;i<all.length;i++){
            source[i+begin] = all[i];
        }
        return source;
    }
}
class Gcode{
    public static final String gcode[] = {"G00","G01","G02","G03","G28"};
    int code;
    float startPos[] = new float[3];
    float endPos[]= new float[3];
    float var ;
    boolean select = false;
    public Gcode(Gcode otherGcode){
        this.code = otherGcode.code;
        for(int i = 0 ;i<3;i++){
            this.startPos[i] = otherGcode.startPos[i];
            this.endPos[i] = otherGcode.endPos[i];
        }
        this.var = otherGcode.var;
    }
    public Gcode(int code,float start[],float end[],float var){
        this.code = code;
        if(start!=null&&start.length>=3&&end!=null&&end.length>=3){
            for(int i=0;i<3;i++){
                this.startPos[i] = start[i];
                this.endPos[i] = end[i];
            }
        }
        this.var = var;

    }
    public Gcode(int code,float endx,float endy,float var){
        this.code = code;

        this.endPos[0] = endx;
        this.endPos[1] = endy;
        this.var = var;

    }
public String toString(){
    String s = ""+gcode[code]+" "+"("+startPos[0]+","+startPos[1]+","+startPos[2]+"),("
            +endPos[0]+","+endPos[1]+","+endPos[2]+")";
    if(code ==2||code==3){
        s+= " R "+var;
    }
    if(code==4){
        s = ""+gcode[code];
    }
    return s;
}
public String getVar(){
    String s = "("+startPos[0]+","+startPos[1]+","+startPos[2]+"),("
            +endPos[0]+","+endPos[1]+","+endPos[2]+")";
    if(code ==2||code==3){
        s+= " R "+var;
    }
    if(code==4){
        s = ""+gcode[code];
    }
    return s;
}
    public String getStartPosString(){
        return "("+startPos[0]+", \n "+startPos[1]+" ,\n "+startPos[2]+")";
    }
    public String getEndPosString(){
        return "("+endPos[0]+", \n "+endPos[1]+" ,\n "+endPos[2]+")";
    }
    public byte[] getArmCoder(){
        switch(code){
            case 0:{//G00
                byte[] b= new byte[ArmCoder.CODE_SIZE*3];
                ArmCoder ac1 = new ArmCoder((byte)ArmCoder.COMMAND_G00[0],startPos[0],startPos[1]);
                ArmCoder ac2 = new ArmCoder((byte)ArmCoder.COMMAND_G00[1],startPos[2],endPos[0]);
                ArmCoder ac3 = new ArmCoder((byte)ArmCoder.COMMAND_G00[2],endPos[1],endPos[2]);
                ac1.setAll(b,0);
                ac2.setAll(b,ArmCoder.CODE_SIZE);
                ac3.setAll(b,ArmCoder.CODE_SIZE*2);
                return b;
            }
            case 1:{//G01
                byte[] b= new byte[ArmCoder.CODE_SIZE*3];
                ArmCoder ac1 = new ArmCoder((byte)ArmCoder.COMMAND_G01[0],startPos[0],startPos[1]);
                ArmCoder ac2 = new ArmCoder((byte)ArmCoder.COMMAND_G01[1],startPos[2],endPos[0]);
                ArmCoder ac3 = new ArmCoder((byte)ArmCoder.COMMAND_G01[2],endPos[1],endPos[2]);
                ac1.setAll(b,0);
                ac2.setAll(b,ArmCoder.CODE_SIZE);
                ac3.setAll(b,ArmCoder.CODE_SIZE*2);
                return b;
            }
            case 2:{//G02
                byte[] b= new byte[ArmCoder.CODE_SIZE*4];
                ArmCoder ac1 = new ArmCoder((byte)ArmCoder.COMMAND_G02[0],startPos[0],startPos[1]);
                ArmCoder ac2 = new ArmCoder((byte)ArmCoder.COMMAND_G02[1],startPos[2],endPos[0]);
                ArmCoder ac3 = new ArmCoder((byte)ArmCoder.COMMAND_G02[2],endPos[1],endPos[2]);
                ArmCoder ac4 = new ArmCoder((byte)ArmCoder.COMMAND_G02[3],var);
                ac1.setAll(b,0);
                ac2.setAll(b,ArmCoder.CODE_SIZE);
                ac3.setAll(b,ArmCoder.CODE_SIZE*2);
                ac4.setAll(b,ArmCoder.CODE_SIZE*3);
                return b;
            }
            case 3:{//G03
                byte[] b= new byte[ArmCoder.CODE_SIZE*4];
                ArmCoder ac1 = new ArmCoder((byte)ArmCoder.COMMAND_G03[0],startPos[0],startPos[1]);
                ArmCoder ac2 = new ArmCoder((byte)ArmCoder.COMMAND_G03[1],startPos[2],endPos[0]);
                ArmCoder ac3 = new ArmCoder((byte)ArmCoder.COMMAND_G03[2],endPos[1],endPos[2]);
                ArmCoder ac4 = new ArmCoder((byte)ArmCoder.COMMAND_G03[3],var);
                ac1.setAll(b,0);
                ac2.setAll(b,ArmCoder.CODE_SIZE);
                ac3.setAll(b,ArmCoder.CODE_SIZE*2);
                ac4.setAll(b,ArmCoder.CODE_SIZE*3);
                return b;
            }
        }
        return new ArmCoder((byte)code).getAll();
    }
    public static final int _G00 = 0;
    public static final int _G01 = 1;
    public static final int _G02 = 2;
    public static final int _G03 = 3;
    public static final int _G04 = 4;
    public static final int _G28 = 28;
}