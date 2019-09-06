package com.wrrryyyy.www.mgvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by aa on 2018/10/6.
 */

public abstract class BaseFragment extends Fragment {
    private boolean isFragmentVisible = false;
    private boolean isViewCreated = false;
    private boolean isVisible = false;
    abstract void loadData();
    public boolean isViewVisible(){
        return isVisible;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated  = true;
        lazyLoad();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            isFragmentVisible = true;
            lazyLoad();
        }else{
            isFragmentVisible = false;
            lazyRelease();
        }
        isVisible = isVisibleToUser;
    }
    private void lazyLoad(){
        if(isViewCreated&&isFragmentVisible){
            loadData();
            isViewCreated = false;
            isFragmentVisible = false;
            Log.d("baseFragment","lazy load over");
        }
    }
    private void lazyRelease() {
        Log.d("baseFragment","lazy release");
    }



}





//public class CameraImageFragment extends BaseFragment {
//    // TODO: Rename parameter arguments, choose names that match
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//    private static final String TAG = "CameraImageFragment";
//    private Activity mActivity;
//    private String mParam1;
//    private String mParam2;
//    private OnFragmentInteractionListener mListener;
//
//    //------------没啥用的变量---------------
//    public CameraImageFragment() {
//        // Required empty public constructor
//    }
//
//    public static CameraImageFragment newInstance(String param1, String param2) {
//        return newInstance(param1,param2,null);
//    }
//    public static CameraImageFragment newInstance(int lineNum) {
//        List<List<PointValue>> lists = new ArrayList<>();
//        for(int i=0;i<lineNum;++i){
//            lists.add(new ArrayList<PointValue>());
//        }
//        return newInstance(lists);
//    }
//    public static CameraImageFragment newInstance(List<List<PointValue>>list){
//        return newInstance("","",list);
//    }
//
//    public static CameraImageFragment newInstance(String param1, String param2,List<List<PointValue>>list) {
//        CameraImageFragment fragment = new CameraImageFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
////        if(list!=null)fragment.mTempDataList = list;
//        return fragment;
//    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View v = inflater.inflate(R.layout.fragment_wave_player, container, false);
//        return v;
//    }
//
//    @Override
//    void loadData() {
//    }
//
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        }
//        mActivity = (Activity)context;
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//        mActivity = null;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
//
//}
