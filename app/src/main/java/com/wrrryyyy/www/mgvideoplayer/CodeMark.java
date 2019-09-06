package com.wrrryyyy.www.mgvideoplayer;

/**
 * Created by aa on 2018/10/7.
 */

public class CodeMark {
    static final int MT_PARAMETER = 0;
    static final int MT_PARAMETER_A = 4;
    static final int MT_VIDEO = 1;
    static final int MT_VIDEO_A = 5;
    static final int MT_WAVE = 2;
    static final int MT_WAVE_A = 8;
    static final int MT_VIDEO_MONO = 6;//这两个不要用
    static final int MT_VIDEO_MONO_A = 7;
    static final int MT_VIDEO_GRAY = 9;
    static final int MT_VIDEO_GRAY_A = 10;
    static final int MT_VIDEO_BINARY = 11;
    static final int MT_VIDEO_BINARY_A = 12;
    static final int MT_DEF = 3;
    static final int MT_TEACHER = 15;
    private boolean mDecodeMode = true;
    public static byte[] getCodeMark(int markType){
        byte[] b = new byte[4];
        switch(markType){//你有没有想过这么瞎取标志位之后怎么维护 //2019年4月22日 维护不了不只是瞎取标志位的问题..
            case MT_VIDEO:{//#123456
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x34;
                b[3] = 0x56;
                break;
            }
            case MT_PARAMETER:{//#234567
                b[0] = '#';
                b[1] = 0x23;
                b[2] = 0x45;
                b[3] = 0x67;
                break;
            }
            case MT_WAVE:{//#122334
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x23;
                b[3] = 0x34;
                break;
            }
            case MT_WAVE_A:{//#122345
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x23;
                b[3] = 0x45;
                break;
            }
            case MT_PARAMETER_A:{//123457
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x34;
                b[3] = 0x57;
                break;
            }
            case MT_VIDEO_A:{//234568
                b[0] = '#';
                b[1] = 0x23;
                b[2] = 0x45;
                b[3] = 0x68;
                break;
            }
            case MT_VIDEO_MONO:{//133456
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x34;
                b[3] = 0x56;
                break;
            }
            case MT_VIDEO_MONO_A:{//133457
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x34;
                b[3] = 0x57;
                break;
            }
            case MT_VIDEO_GRAY:{//133458
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x34;
                b[3] = 0x58;
                break;
            }
            case MT_VIDEO_GRAY_A:{//133459
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x34;
                b[3] = 0x59;
                break;
            }
            case MT_VIDEO_BINARY:{//133556
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x35;
                b[3] = 0x56;
                break;
            }
            case MT_VIDEO_BINARY_A:{//133557
                b[0] = '#';
                b[1] = 0x13;
                b[2] = 0x35;
                b[3] = 0x57;
                break;
            }
            case MT_TEACHER :{
                return TeacherDecoder.COMMAND_MARK;
            }
            case MT_DEF:{}
            default:{
                b[0] = '#';
                b[1] = 0x12;
                b[2] = 0x21;
                b[3] = 0x31;
            }
        }

        return b;
    }

}
