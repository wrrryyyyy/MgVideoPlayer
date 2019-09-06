package com.wrrryyyy.www.mgvideoplayer;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Created by aa on 2018/10/4.
 */

public class NoRollViewPager extends ViewPager {
    private boolean rollable = true;
    public NoRollViewPager(Context context) {
        super(context);
    }
    public void setRollable(boolean b){
        rollable = b;
    }
    public NoRollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(rollable){
            return super.onTouchEvent(ev);
        }else{
            return false;
        }
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);//取消滑动动画
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(rollable){
            return super.onInterceptTouchEvent(ev);
        }else{
            return false;
        }
    }

}
