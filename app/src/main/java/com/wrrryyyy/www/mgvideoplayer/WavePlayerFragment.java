package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WavePlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WavePlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WavePlayerFragment extends BaseFragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String TAG = "waveFragment";
    private long mStartTime = 0;
    private Activity mActivity;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mNumberOfLines = 0;
    private List<List<PointValue>> mTempDataList;
    private List<Line>mLines = new ArrayList<>();
    private List<LineState>mLineState = new ArrayList<>();
    private LineChartView mLineChartView;
    private Button mBtnTest;
    private Button mBtnClean;
    private Button mBtnPause;
    private Button mBtnFront;
    private Button mBtnItem;
    private Spinner mSpinner;
    private LineChartData mLineChartData;
    private float mMaxValue = 0;
    private float mMinValue = 0;
    private float mFromPoint = 0;
    private float mLeftPoint = 0;
    private UIFleshAsyncTask mUITask ;
    private OnFragmentInteractionListener mListener;

    //------------没啥用的变量---------------
    private int mViewportMode = 0;
    private boolean mKeepFresh = true;
    private boolean mNeedFresh = true;
    public WavePlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WavePlayerFragment.
     */
    // TODO: Rename and change types and number of parameters

    public static WavePlayerFragment newInstance(String param1, String param2) {
        return newInstance(param1,param2,null);
    }
    public static WavePlayerFragment newInstance(int lineNum) {
        List<List<PointValue>> lists = new ArrayList<>();
        for(int i=0;i<lineNum;++i){
            lists.add(new ArrayList<PointValue>());
        }
        return newInstance(lists);
    }
    public static WavePlayerFragment newInstance(List<List<PointValue>>list){
        return newInstance("","",list);
    }

    public static WavePlayerFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
        WavePlayerFragment fragment = new WavePlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        if(list!=null)fragment.mTempDataList = list;
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_wave_player, container, false);
        initButtons(v);
        initLineChart(mTempDataList);
        return v;
    }

    @Override
    void loadData() {
        mUITask = new UIFleshAsyncTask();
     //   mUITask.execute();
        mUITask.executeOnExecutor(Executors.newCachedThreadPool());//不要用这个来写计时器..多了会进不去的
        mStartTime = System.currentTimeMillis();
        Log.d(TAG,"load data");
    }
    private void initButtons(View v){
        mLineChartView = (LineChartView) v.findViewById(R.id.line_chart_wave_player);
        mBtnTest = (Button)v.findViewById(R.id.btn_wave_player);
        mBtnClean = (Button)v.findViewById(R.id.btn_wave_player_clean);
        mBtnFront = (Button)v.findViewById(R.id.btn_wave_player_go_front);
        mBtnPause = (Button)v.findViewById(R.id.btn_wave_player_fresh);
        mBtnItem = (Button)v.findViewById(R.id.btn_wave_player_items);
        mBtnTest.setOnClickListener(mButtonListener);
        mBtnClean.setOnClickListener(mButtonListener);
        mBtnFront.setOnClickListener(mButtonListener);
        mBtnPause.setOnClickListener(mButtonListener);
        mBtnItem.setOnClickListener(mButtonListener);
    }
    private final int VIEWPORT_MODE_AUTO = 0;
    private final int VIEWPORT_MODE_LOCKED = 1;
    private final int VIEWPORT_MODE_FREE = 2;
    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_wave_player:{
                    updateLine(0,new PointValue[0]);
                    break;
                }
                case R.id.btn_wave_player_clean:{
                    cleanLines();
                    break;
                }
                case R.id.btn_wave_player_go_front:{
                    mViewportMode = (mViewportMode+1)%3;
                    if(mViewportMode==VIEWPORT_MODE_AUTO){
                        mBtnFront.setText(R.string.viewport_auto);
                    }else if(mViewportMode ==VIEWPORT_MODE_LOCKED){
                        mBtnFront.setText(R.string.lock_viewport);
                    }else{
                        mBtnFront.setText(R.string.free_viewport);
                    }
                    break;
                }
                case R.id.btn_wave_player_fresh:{
                    mKeepFresh = !mKeepFresh;
                    if(mKeepFresh){
                        mBtnPause.setText(R.string.keep_wave_fresh);
                    }else{
                        mBtnPause.setText(R.string.stop_fresh);
                    }
                    break;
                }
                case R.id.btn_wave_player_items:{
                    String head = "■";
                    List<WaveItem> list = new ArrayList<>();
                    String[] arrlist = new String[mLines.size()];
                    boolean[] bools = new boolean[mLines.size()];
                    for(int i =0;i<mLines.size();++i){
                        String name = head+getLineName(i);
                        arrlist[i] = name;
                        boolean b = getLineVisible(i);
                        bools[i] = b;
                        list.add(new WaveItem(VideoDecoder.getColor16(i),name,b));
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    View waveListView = View.inflate(mActivity,R.layout.wave_item_list_view,null);
                    ListView listView = (ListView)waveListView.findViewById(R.id.list_view_wave_item);
                    listView.setAdapter(new WaveAdapter(mActivity,R.layout.color_checkbox_item,list));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            changeLineVisible(position);
                            boolean b = getLineVisible(position);
                            ((CheckBox)view.findViewById(R.id.cb_wave_item)).setChecked(b);
                            Log.d(TAG,"being click"+b+" "+position);
                        }
                    });
                    builder.setTitle("显示线条菜单")
                            .setIcon(R.mipmap.wave_while)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setView(waveListView);
//                            .setAdapter(new WaveAdapter(builder.getContext(), R.layout.color_checkbox_item, list),
//                            new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            changeLineVisible(which);
//                            Log.d(TAG,"get one click");
//                        }
//                    });
//                            .setMultiChoiceItems(arrlist, bools, new DialogInterface.OnMultiChoiceClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                                    Log.d(TAG,"be click"+which+" "+isChecked);
//                                    changeLineVisible(which);
//                                }
//                            });

                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    break;
                }

            }
        }
    };
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
        mActivity = (Activity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mUITask!=null)mUITask.stopTask();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public void addListValueAddLine(float[] y){
        if(y!=null&y.length>0){
            int lineNum = y.length;
            if(mLines.size()<lineNum){
                for(int i = mLines.size();i<lineNum;i++){
                    addLine("");
                }
            }
            for(int i=0;i<y.length;++i){
                addListValue(i,y[i]);
            }
        }
    }
    //以当前时间添加值
    public void addListValue(int index,float y){
        addListValue(System.currentTimeMillis(),index,y);
    }

    //以某一时间添加值
    public void addListValue(long time,int index,float y){
        if(mStartTime==0||time<mStartTime){
            Log.d(TAG,"add value error:init didn't finish");
        }else{
            float now =  (time -mStartTime);
            addListValue(index,now/1000,y);
        }
    }
    public void addListValue(int index,float x,float y){
        if(mLines==null||index>mLines.size()){
            Log.d(TAG,"addListValue error:invalid index");
            return ;
        }
        PointValue pv = new PointValue(x,y);
        PointValue pvs[] = new PointValue[1];
        pvs[0] = pv;
//        List<PointValue> list =mTempDataList.get(index);
//        list.add(pv);
        updateLineList(index,pvs);

    }
    private void resetViewport() {
        // Reset viewport height range to (0,100)
        setViewPort(100,0,0,100);
    }
    private void setViewPort(int top,int bottom,int left,int right){
        final Viewport v = new Viewport(mLineChartView.getMaximumViewport());
        Log.d(TAG,"v.size:top...right"+v.top+" "+v.bottom+" "+v.left+" "+v.right);
        v.bottom = bottom;
        v.top = top;
        v.left = left;
        v.right = right;
   //     mLineChartView.setMaximumViewport(v);
        mLineChartView.setCurrentViewport(v);
    }
    private void setViewportAuto(){

        final Viewport vNow = mLineChartView.getCurrentViewport();
        final Viewport v = new Viewport(mLineChartView.getMaximumViewport());
        final Viewport vMax = new Viewport(mLineChartView.getMaximumViewport());
        float vx = vNow.right - vNow.left;
        vMax .left = mLeftPoint;
        vMax.right = mFromPoint*1.2f;
        vMax.top = mMaxValue*1.2f;
        vMax.bottom = mMinValue*1.2f;
        mLineChartView.setMaximumViewport(vMax);
        if(mViewportMode==VIEWPORT_MODE_LOCKED){
            v.bottom = vNow.bottom;
            v.top = vNow.top;
            v.right = mFromPoint+vx*0.2f;
            v.left = v.right-vx;
            mLineChartView.setCurrentViewport(v);
        }else if(mViewportMode ==VIEWPORT_MODE_AUTO){
            v.bottom = vMax.bottom;
            v.top = vMax.top;
            v.right = mFromPoint+vx*0.2f;
            v.left = v.right-vx;
            mLineChartView.setCurrentViewport(v);
        }
    }
    private void setViewPortToFrom(){
        final Viewport v = new Viewport(mLineChartView.getMaximumViewport());
        Log.d(TAG,"v.size:top...right"+v.top+" "+v.bottom+" "+v.left+" "+v.right);
        v.bottom = mMinValue;
        v.top = mMaxValue;
        v.left = mLeftPoint;
        if(mFromPoint<20){
            v.right = 20;
        }else{
            v.right = mFromPoint*1.2f;
        }
        mLineChartView.setMaximumViewport(v);
        mLineChartView.setCurrentViewport(v);
    }
    private void setViewPortMax(){
        final Viewport vNow = mLineChartView.getCurrentViewport();
        final Viewport v = new Viewport(mLineChartView.getMaximumViewport());
        Log.d(TAG,"v.size:top...right"+v.top+" "+v.bottom+" "+v.left+" "+v.right+" || "+vNow.top+" "+vNow.bottom+" "+vNow.left+" "+vNow.right);
        v.bottom = mMinValue*1.2f;
        v.top = mMaxValue*1.2f;
        v.left = mLeftPoint;
        v.right = mFromPoint*1.2f;
        mLineChartView.setMaximumViewport(v);
    }
    private float[][] generateValues(int maxNumberOfLines,int numberOfPoints) {
        float[][]randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];
        for (int i = 0; i < maxNumberOfLines; ++i) {
            for (int j = 0; j < numberOfPoints; ++j) {
                randomNumbersTab[i][j] = (float) Math.random() * 100f;
            }
        }
        return randomNumbersTab;
    }
    private void initLineChart(List<List<PointValue>>list){
        if(list==null){
            mNumberOfLines = 5;
            float[][] numtable = generateValues(mNumberOfLines,100);
            generateData(numtable);
        }else{
            mNumberOfLines = list.size();
            generateData(list);
        }

        setAxis();
        mLineChartData.setBaseValue(Float.NEGATIVE_INFINITY);
        mLineChartView.setLineChartData(mLineChartData);
        mLineChartView.setViewportCalculationEnabled(false);
        mLineChartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        mLineChartView.setInteractive(true);//滑动支持
        mLineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        setViewPortToFrom();
        mLineChartView.setOnValueTouchListener(new ValueTouchListener());


    }
    private void setAxis(){
        Axis axisX = new Axis();
        Axis axisY = new Axis();
        axisY.setHasLines(true);
        mLineChartData.setAxisXBottom(axisX);//底下x轴
        mLineChartData.setAxisYLeft(axisY);
    }
    private void setLineType(int index,Line line){

        line.setStrokeWidth(2);
        line.setColor(VideoDecoder.getColor16(index));
        line.setShape(ValueShape.CIRCLE);
        line.setHasLines(true);
        line.setCubic(false);
        line.setHasPoints(false);
    }
    private void generateData(List<List<PointValue>>list){
        float max =0,min = 0;
        if(list.size()>0&&list.get(0)!=null&&list.get(0).size()!=0){
            max = list.get(0).get(0).getX();
            min = list.get(0).get(0).getY();
            int i = 0;
            for(List<PointValue> value:list){
                Line line = new Line(value);
                setLineType(i,line);
                for(PointValue pv:value){
                    if(pv.getX()>max)max = pv.getX();
                    if(pv.getY()<min)min = pv.getY();
                }
                //省略一堆设置
                mLines.add(line);
                mLineState.add(new LineState(""));
                mFromPoint = value.size();
                i++;
            }
            mMaxValue = max;
            mMinValue = min;
        }else{
            Log.d(TAG,"generateData  input error");
        }
        mLineChartData = new LineChartData(mLines);

    }
    private void generateData(float[][] numtable) {
        float max = 0,min = 0;

        max = numtable[0][0];
        min = numtable[0][0];
        for(int i=0;i<mNumberOfLines;i++){
            List<PointValue> value = new ArrayList<>();
            for(int j=0;j<100;j++){
                value.add(new PointValue(j,numtable[i][j]));
                if(max<numtable[i][j])max = numtable[i][j];
                if(min>numtable[i][j])min = numtable[i][j];
            }
            Line line = new Line(value);
//            line.setStrokeWidth(2);
//            line.setColor(VideoDecoder.getColor16(i));
//            line.setShape(ValueShape.CIRCLE);
//            line.setHasLines(true);
//            line.setCubic(false);
//            line.setHasPoints(true);
//            line.setPointRadius(3);
            setLineType(i,line);
            //省略一堆设置
            mLines.add(line);
            mLineState.add(new LineState(""));
            mFromPoint = value.size();

        }
        mMaxValue = max;
        mMinValue = min;
        mLineChartData = new LineChartData(mLines);
    }
    public void addLine(String name){
        List<PointValue> list=new ArrayList<>();
        Line line = new Line(list);
        setLineType(mLines.size(),line);
        mLines.add(line);
        mLineState.add(new LineState(name));
    }
    private void setLineName(String[]names){
        if(names!=null&&names.length>0){
            for(int i=0;i< names.length;++i){
                setLineName(i,names[i]);
            }
        }
    }
    private void setLineName(int index,String names){
        if(index>=0&&index<mLineState.size()){
            mLineState.get(index).name = names;
        }
    }
    private String getLineName(int index){
        if(index>=0&&index<mLineState.size()){
            return mLineState.get(index).name ;
        }
        return "";
    }
    private boolean getLineVisible(int index){
        if(index>=0&&index<mLineState.size()){
            return mLineState.get(index).visible;
        }
        return true;
    }
    private void changeLineVisible(int index){
        if(index>=0&&index<mLineState.size()){
             mLineState.get(index).visible = ! mLineState.get(index).visible ;
            Log.d(TAG,"change line visible"+mLineState.get(index).visible);
        }else{
            Log.d(TAG,"change line disvisible"+mLineState.get(index).visible);
        }
        updateLineList(index , new PointValue[]{});
    }
    private void updateLineList(int index,PointValue[] points){//最底下加点的地方
        if(index>=mLines.size()||!mKeepFresh){
            Log.d(TAG,"updateLine error :get invalid value");
            return;
        }
        mNeedFresh = true;
        Line line = mLines.get(index);
        List<PointValue> list=line.getValues();
        for(PointValue point:points){
            float xValue = point.getX();
            if(xValue>mFromPoint)mFromPoint = xValue;
            if(xValue<mLeftPoint)mLeftPoint = xValue;
            float yValue = point.getY();
            if(yValue>mMaxValue)mMaxValue = yValue;
            if(yValue<mMinValue)mMinValue = yValue;
            list.add(point);
         //   Log.d(TAG,"add point"+points.length);
        }
        line.setHasLines(getLineVisible(index));
        line.setValues(list);
     //   line.setColor(Color.rgb(255,0,0));
    }
    private void cleanLines(){
        for(Line line:mLines){
            List<PointValue> list=line.getValues();
            list.clear();
            line.setValues(list);
        }
        mLeftPoint = mFromPoint;
        mFromPoint+=20;

        setViewPortToFrom();
    }
    private void updateUI(){
        if(mNeedFresh){
            mNeedFresh = false;
            mLineChartData.setLines(mLines);
            //       mLineChartData = new LineChartData(mLines);
            mLineChartView.setLineChartData(mLineChartData);
            //   setViewPortToFrom();
            setViewportAuto();
        }
    }
    private void updateButtons(){

    }
    private void updateLine(int index,PointValue[] points){

        updateLineList(index,points);
//        mFromPoint = list.size();
        Log.d(TAG,"updateLine");
        updateUI();
    }
    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(getActivity(), "选择点: " + value+" "+lineIndex+" "+pointIndex+" ", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"getTouch");
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub
            Log.d(TAG,"Touch relese");

        }

    }
    private class UIFleshAsyncTask extends AsyncTask<Void,Void,Void>{
        private boolean running = true;
        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG,"task start");
            while(running){
//                Log.d(TAG,"timmer running");
                try {
                    publishProgress();
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    Log.d(TAG,"task error");
                    e.printStackTrace();
                }
            }
            Log.d(TAG,"task compeletion");
            return null;
        }
        public void stopTask(){
            Log.d(TAG,"stop thread");
            running = false;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Log.d(TAG,"update timmer");
            updateUI();
        }
    }
    private class LineState{
        boolean visible = true;
        String name = "";
        LineState(String s){
            name = s;
        }
    }

    class WaveItem {
        private int color;
        String name;
        boolean selected = true;

        public WaveItem(int color, String name, boolean selected) {
            this.color = color;
            this.name = name;
            this.selected = selected;
        }
    }
    class WaveAdapter extends ArrayAdapter<WaveItem> {
        private final LayoutInflater mInflater;
        private final int mResource;

        public WaveAdapter(@NonNull Context context, @LayoutRes int resource, List<WaveItem> object) {
            super(context, resource, object);
            mInflater = LayoutInflater.from(context);
            mResource = resource;

        }
        //简单创建一个adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(mResource, parent, false);
            }
            WaveItem item = getItem(position);//拿到指定的item
            TextView name = (TextView)convertView.findViewById(R.id.tv_wave_item_name);
            CheckBox select = (CheckBox)convertView.findViewById(R.id.cb_wave_item);
//            ImageView color = (ImageView)convertView.findViewById(R.id.iv_wave_item);
//            CheckedTextView tcv = (CheckedTextView) convertView.findViewById(R.id.ct_wave_item);
            name.setText(item.name);
           // name.setBackgroundColor(item.color);
            name.setTextColor(item.color);
           // color.setBackgroundColor(item.color);
//            color.setColorFilter(item.color);
//            select.setText(item.name);
//            select.setTextColor(item.color);
            select.setChecked(item.selected);
            return convertView;
        }
    }
}
