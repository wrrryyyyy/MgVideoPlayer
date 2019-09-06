package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity implements WavePlayerFragment.OnFragmentInteractionListener,SensorEventListener {
    private final String[] title = new String[]{
            "k","i","r","a","☆"
    };
    private final String[] data = new String[]{
            "k","i","r","a","☆"
    };
    private final String TAG = "AboutActivity";
    private View[] mvs= new View[5];
    private SensorManager mSensorManager;
    private WavePlayerFragment mWavePlayerFragment;
    private long mStartTime = 0;
    TabLayout mTabLayout;
    NoRollViewPager mVp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
   //     initPageByFragment();
        initPageByAboutFragment();
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }
    void initPageByAboutFragment(){
        List<Fragment> list = new ArrayList<>();
        List<String>titles = new ArrayList<>();
        mVp = (NoRollViewPager)findViewById(R.id.about_viewpager);
        mVp.setRollable(false);
        list.add(AboutFragment.getInstance("kira"));
        titles.add(title[0]);
        mVp.setAdapter(new AboutFragmentAdapter(getSupportFragmentManager(),list,titles));
        mTabLayout = (TabLayout)findViewById(R.id.about_tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText(title[0]));
        mTabLayout.setupWithViewPager(mVp);
        mTabLayout.setVisibility(View.INVISIBLE);
    }
    void initPageByFragment(){
        List<Fragment> list = new ArrayList<>();
        WavePlayerFragment fg1 = WavePlayerFragment.newInstance("kira","☆");
        WavePlayerFragment fg2 = WavePlayerFragment.newInstance(0);
        fg2.addLine("one");
        fg2.addLine("two");
        fg2.addLine("three");

        mWavePlayerFragment = fg2;
        mStartTime = System.currentTimeMillis();
        List<String>titles = new ArrayList<>();
        mVp = (NoRollViewPager)findViewById(R.id.about_viewpager);
        mVp.setRollable(false);
        list.add(AboutFragment.getInstance("kira"));
//        list.add(AboutFragment.getInstance("k"));
//        list.add(AboutFragment.getInstance("i"));
//        list.add(AboutFragment.getInstance("r"));
//        list.add(AboutFragment.getInstance("a"));
        list.add(fg1);
        list.add(AboutFragment.getInstance("i"));
        list.add(fg2);
        list.add(AboutFragment.getInstance("a"));
        titles.add(title[0]);
        titles.add(title[1]);
        titles.add(title[2]);
        titles.add(title[3]);
        titles.add(title[4]);
        mVp.setAdapter(new AboutFragmentAdapter(getSupportFragmentManager(),list,titles));
        initTabLayout();
    }
    void initPagerByView(){

        for(int i=0;i<mvs.length;i++) {
            if (i == 0) {
                View view1 = getLayoutInflater().inflate(R.layout.message_send_view, null);
                mvs[i] = view1;
            } else {
                View view2 = getLayoutInflater().inflate(R.layout.pure_uart_view, null);
                mvs[i] = view2;
            }
        }
        //     View view2 = getLayoutInflater().inflate(R.layout.message_send_view,null);
        AboutViewPagerAdapter mAVPA = new AboutViewPagerAdapter(mvs);

        mVp = (NoRollViewPager)findViewById(R.id.about_viewpager);
        mVp.setAdapter(mAVPA);
        initTabLayout();
    }
    void initTabLayout(){
        mTabLayout = (TabLayout)findViewById(R.id.about_tab_layout);
        for(int i=0;i<title.length;i++){
            mTabLayout.addTab(mTabLayout.newTab().setText(title[i]));
        }
        mTabLayout.setupWithViewPager(mVp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
//---------------------------sensor-------------------------------
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if(mWavePlayerFragment!=null){
                float time = (System.currentTimeMillis()-mStartTime);
                time/=1000;
                mWavePlayerFragment.addListValue(0,time,x);
                mWavePlayerFragment.addListValue(1,time,y);
                mWavePlayerFragment.addListValue(2,time,z);
            }
            Log.d(TAG,"sensor value:"+x+" "+y+" "+z);
        }
    }

//---------------------------sensor end-------------------------------
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    class AboutViewPagerAdapter extends PagerAdapter {

        View mvs[];
        public AboutViewPagerAdapter(View view1,View view2){
            View v[] = new View[2];
            v[0] = view1;
            v[1] = view2;
            mvs = v;

        }
        public AboutViewPagerAdapter(View[] view){
            mvs = view;
        }


        @Override
        public int getCount() {
            return mvs.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            super.getPageTitle(position);
            return title[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mvs[position]);
            return mvs[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
          //  super.destroyItem(container, position, object);
            container.removeView(mvs[position]);
        }
    }
    class AboutFragmentAdapter extends FragmentStatePagerAdapter {
        List<String> titleList;
        List<Fragment> fragmentList;

        public AboutFragmentAdapter(FragmentManager fm,List<Fragment>fragmentList,List<String>titleList) {
            super(fm);
            this.fragmentList = fragmentList;
            this.titleList = titleList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }


}
