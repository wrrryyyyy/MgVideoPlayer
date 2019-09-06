package com.wrrryyyy.www.mgvideoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by aa on 2018/9/25.
 */

@SuppressLint("ValidFragment")
public class AboutFragment extends Fragment {
    private String ARG_PARAM1 = "arg_param1";
    private String mParam1;
    private Activity mActivity;
    private TextView mTvAbout;
    private LineChartView mLcv;
    private LineChartData mLcd;
    private Button mBtn;
    private List<Line> lines;
    public AboutFragment(String param1) {
        super();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        this.setArguments(args);
    }
    public AboutFragment() {
    }
    public static AboutFragment getInstance(String param1){
        return new AboutFragment(param1);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        mTvAbout = (TextView)v.findViewById(R.id.tv_about);
        mLcv = (LineChartView)v.findViewById(R.id.line_chart);
        mBtn = (Button)v.findViewById(R.id.btn_about_test);
        initChart(mActivity);
        Animation testAnim = AnimationUtils.loadAnimation(mActivity,R.anim.rollinggg);
        mTvAbout.setAnimation(testAnim);
        testAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //      animation.setStartOffset(2000);
                mTvAbout.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Line line = lines.get(0);
                List<PointValue> list=line.getValues();
                PointValue pv=list.get(list.size()-1);
                list.add(new PointValue(pv.getX()+0.5f,pv.getY()+0.3f));

                line.setValues(list);
                line.setColor(Color.rgb(255,0,0));
                mLcd.setLines(lines);
//                mLcd = new LineChartData(lines);
                mLcv.setLineChartData(mLcd);
                final Viewport vv = new Viewport(mLcv.getMaximumViewport());
                vv.bottom = 0;
                vv.top = 100;
                vv.left = 0;
                vv.right = 400;
                mLcv.setMaximumViewport(vv);
                mLcv.setCurrentViewport(vv);
            }
        });
    //    v.setBackgroundColor(Color.rgb(255,0,0));
   //     View v = inflater.inflate(R.layout.view_line,container,false);
        return v;
    }
    private void setLinesType(Line line){

    }
    public void initChart(Context context){
        float[][] numtable = getNumTable(10,6000);
        lines = new ArrayList<>();
        for(int i=0;i<10;i++){
            List<PointValue> value = new ArrayList<PointValue>();
            for(int j=0;j<60;j++){
                value.add(new PointValue(j,numtable[i][j]));
            }
            Line line = new Line(value);
            line.setStrokeWidth(3);
            line.setColor(Color.rgb(10*i%255,20*i%255,30*i%255));
            line.setShape(ValueShape.CIRCLE);
            line.setHasLines(true);
            line.setCubic(false);
            line.setHasPoints(false);
            //省略一堆设置
            lines.add(line);

        }
        mLcd = new LineChartData(lines);
        Axis axisX = new Axis();
        Axis axisY = new Axis();
        //
        mLcd.setAxisXBottom(axisX);

        mLcd.setAxisYLeft(axisY);
        mLcd.setBaseValue(Float.NEGATIVE_INFINITY);
        mLcv.setLineChartData(mLcd);
        mLcv.setViewportCalculationEnabled(false);
        mLcv.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        mLcv.setInteractive(true);//滑动支持
        mLcv.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

        final Viewport v = new Viewport(mLcv.getMaximumViewport());
        v.bottom = -20;
        v.top = 20;
        v.left = 0;
        v.right = 200;
        mLcv.setMaximumViewport(v);
        mLcv.setCurrentViewport(v);





    }
    public float[][] getNumTable(int x,int y){
        float[][] ans = new float[x][y];
        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++){
                ans[i][j] = i*j%13;
            }
        }
        return ans;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity)context;
    }

}