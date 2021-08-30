package com.mj.gpsclient.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ab.fragment.AbFragment;
import com.ab.soap.AbSoapListener;
import com.ab.soap.AbSoapParams;
import com.ab.soap.AbSoapUtil;
import com.ab.util.AbToastUtil;
import com.ab.view.titlebar.AbTitleBar;
import com.mj.gpsclient.AppHttpUtil;
import com.mj.gpsclient.R;
import com.mj.gpsclient.adapter.DevicesListAdapter;
import com.mj.gpsclient.global.DebugLog;
import com.mj.gpsclient.global.MyApplication;
import com.mj.gpsclient.model.Devices;
import com.mj.gpsclient.view.SearchBarView;
import com.mj.gpsclient.view.segmentSelectView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by majin on 15/5/26.
 */
public class DevicesListFragment extends AbFragment {

    private MyApplication application;
    private Activity mActivity = null;
    private ListView listView;
    private segmentSelectView segmentSelectView;
    private SearchBarView searchBarView;
    private DevicesListAdapter devicesAdapter;
    private List<Devices> list;
    private List<Devices> allDeviceList;
    private List<Devices> onLineDeviceList;
    private List<Devices> offLineDeviceList;
    private AbSoapUtil mAbSoapUtil ;
    private AbTitleBar mAbTitleBar = null;
    private int ydix, hight;
    private int segmentIndex;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int result = msg.what;
            switch (result) {
                case 0: //搜索点击
                    showSearchView();
                  break;
                case 1:
//                    mj_showLoadView();
//                    mHandler.sendEmptyMessageDelayed(100,1000);
//                    queryOnlineDevices();
                    sendEmptyMessage(101);
                    break;
                case 100:
//                    AbToastUtil.showToast(mActivity, "数据获取成功！");
//                    mj_hideLoadView();
                    break;
                case 101:
//                    onLineDeviceList.clear();
//                    offLineDeviceList.clear();
//                    allDeviceList.clear();
                    queryOnlineDevices();
                    break;
                case 102:
                    queryOfflineDevices();
                    break;
                case 103:
                    refreshListView(segmentIndex,true);
                    break;
                default:
                    break;
            }
        }

    };


    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = this.getActivity();
        application = (MyApplication) mActivity.getApplication();
        View v =inflater.inflate(R.layout.fragment_devices_list, container, false);
        initViews(v);
        initSearchView();
        initDatas();
        devicesAdapter =new DevicesListAdapter(list,mActivity,mHandler);
        listView.setAdapter(devicesAdapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(mActivity, DevicesTrackingActivity.class);
//                intent.putExtra("memberName",list.get(i).getName());
                Intent intent = new Intent(mActivity, DevicesTrackActivity.class);
                intent.putExtra("memberName", ((Devices)devicesAdapter.getItem(i)).getName());
                startActivity(intent);
            }
        });
        devicesAdapter.notifyDataSetChanged();
        setSegmentSelectView();
        segmentSelectView.setCallback(new segmentSelectView.SegmentSelectCallback() {

            @Override
            public void onSelect(int index) {
                refreshListView(index,false);
                segmentIndex=index;
            }
        });
        searchBarView.setCallback(new SearchBarView.SearchBarCallback(){

            @Override
            public void onchange(String text) {
                Filter filter = ((Filterable) devicesAdapter).getFilter();
                DebugLog.e("onchange---"+text);
                if (TextUtils.isEmpty(text)) {
                    filter.filter(" ");
                } else {
                    filter.filter(text);
                }
            }

            @Override
            public void onCancel() {
                hideSearchView();
                Filter filter = ((Filterable) devicesAdapter).getFilter();
                filter.filter(" ");
            }
        });
        return v;
    }

    private void initDatas(){
        list =new ArrayList<Devices>();
        list.clear();
        list.addAll(((MainActivity)mActivity).getDevicesAll());//new ArrayList<Devices>();


        allDeviceList =new ArrayList<Devices>();
        allDeviceList.clear();
        allDeviceList.addAll(((MainActivity)mActivity).getDevicesAll());

        onLineDeviceList =new ArrayList<Devices>();
        onLineDeviceList.clear();
        onLineDeviceList.addAll(((MainActivity)mActivity).getDevicesesOnline());

        offLineDeviceList =new ArrayList<Devices>();
        offLineDeviceList.clear();
        offLineDeviceList.addAll(((MainActivity)mActivity).getDevicesesOffline());

        segmentIndex=0;
    }


    @Override
    public void setResource() {
        //设置加载的资源
        this.setLoadDrawable(R.drawable.ic_load);
        this.setLoadMessage("正在加载,请稍候...");
        this.setTextColor(getResources().getColor(R.color.moji_black_text));

        this.setRefreshDrawable(R.drawable.ic_refresh_f);
        this.setRefreshMessage("请求出错，请重试");
    }

    private void refreshListView(int index,boolean isRefresh){
        if(isRefresh){
            allDeviceList.clear();
            allDeviceList.addAll(onLineDeviceList);
            allDeviceList.addAll(offLineDeviceList);
            ((MainActivity)mActivity).refreshDeviceList(allDeviceList,onLineDeviceList,offLineDeviceList);
        }
        switch (index){
            case 0:
                list.clear();
                list.addAll(allDeviceList);
                devicesAdapter.setDate(list);
                devicesAdapter.notifyDataSetChanged();
                break;
            case 1:
                list.clear();
                list.addAll(onLineDeviceList);
                devicesAdapter.setDate(list);
                devicesAdapter.notifyDataSetChanged();
                break;
            case 2:
                list.clear();
                list.addAll(offLineDeviceList);
                devicesAdapter.setDate(list);
                devicesAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void setSegmentSelectView(){
        int segmentOne =0;
        int segmentTwo =0;
        int segmentThree =0;
        if(((MainActivity)mActivity).getDevicesAll()!=null&&((MainActivity)mActivity).getDevicesAll().size()>0){
            segmentOne=((MainActivity)mActivity).getDevicesAll().size();
        }
        if(((MainActivity)mActivity).getDevicesesOnline()!=null&&((MainActivity)mActivity).getDevicesesOnline().size()>0){
            segmentTwo=((MainActivity)mActivity).getDevicesesOnline().size();
        }
        if(((MainActivity)mActivity).getDevicesesOffline()!=null&&((MainActivity)mActivity).getDevicesesOffline().size()>0){
            segmentThree=((MainActivity)mActivity).getDevicesesOffline().size();
        }

        segmentSelectView.setData(""+segmentOne,""+segmentTwo,""+segmentThree);
    }

    private void initViews(View view){
        listView = (ListView) view.findViewById(R.id.devices_listView);
        segmentSelectView = (segmentSelectView) view.findViewById(R.id.segmentation_view);
        searchBarView =(SearchBarView)view.findViewById(R.id.search_view);
    }


    private void hideSearchView(){
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                searchBarView.getLayoutParams());
//        params.setMargins(0,hight,0, 0);
//        searchBarView.setLayoutParams(params);
//        Animation animation1 =new TranslateAnimation(0,0,-hight,0);
//        animation1.setInterpolator(new DecelerateInterpolator());
//        animation1.setDuration(800);
//        searchBarView.startAnimation(animation1);
//
//        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
//                segmentSelectView.getLayoutParams());
//        params2.setMargins(0,0,0, 0);
//        segmentSelectView.setLayoutParams(params2);
//        Animation animation2 =new TranslateAnimation(0,0,-hight,0);
//        animation2.setInterpolator(new DecelerateInterpolator());
//        animation2.setDuration(800);
//        segmentSelectView.startAnimation(animation2);
        toggleMode(segmentSelectView,searchBarView,1);
        refreshListView(segmentIndex,false);
    }

    private void showSearchView(){
//        hight =segmentSelectView.getHeight()+10;
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                segmentSelectView.getLayoutParams());
//        params.setMargins(0, -hight, 0, 0);
//        segmentSelectView.setLayoutParams(params);
//        Animation animation1 =new TranslateAnimation(0,0,hight,0);
//        animation1.setDuration(800);
//        animation1.setInterpolator(new DecelerateInterpolator());
//        segmentSelectView.startAnimation(animation1);
//
//        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
//                searchBarView.getLayoutParams());
//        params2.setMargins(0,0,0, 0);
//        searchBarView.setLayoutParams(params2);
//        Animation animation2 =new TranslateAnimation(0,0,hight,0);
//        animation2.setDuration(800);
//        animation2.setInterpolator(new DecelerateInterpolator());
//        searchBarView.startAnimation(animation2);
//        searchBarView.layout();
        refreshListView(0,false);
        toggleMode(searchBarView, segmentSelectView, -1);
    }

    private void toggleMode(View showView,View hideView,int direction){
        hight =segmentSelectView.getHeight()+10;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                hideView.getLayoutParams());
        params.setMargins(0, direction*hight, 0, 0);
        hideView.setLayoutParams(params);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setDuration(500);
        animationSet.setInterpolator(new DecelerateInterpolator());
        Animation animation1 =new TranslateAnimation(0,0,-direction*hight,0);
        animationSet.addAnimation(animation1);
        AlphaAnimation animAlpha = new AlphaAnimation(1.0f, 0.7f);
        animationSet.addAnimation(animAlpha);
        hideView.startAnimation(animationSet);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                showView.getLayoutParams());
        params2.setMargins(0,0,0, 0);
        showView.setLayoutParams(params2);
        Animation animation2 =new TranslateAnimation(0,0,-direction*hight,0);
        animation2.setDuration(500);
        animation2.setInterpolator(new DecelerateInterpolator());
        showView.startAnimation(animation2);
    }


    private void initSearchView(){
        DebugLog.e("initSearchView----");
        int location[] =new int[2] ;
        searchBarView.getLocationInWindow(location);
        DebugLog.e(location[0] + "--" + location[1]);
        hight =segmentSelectView.getHeight();
        ydix =-location[1]-hight;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                searchBarView.getLayoutParams());
        params.setMargins(0,400,0, 0);
        searchBarView.setLayoutParams(params);
        searchBarView.setVisibility(View.VISIBLE);

    }


    @Override
    public void onResume(){
        super.onResume();
        showContentView();
        Log.d("majin", "-------onResume");
    }



    private void queryOnlineDevices(){
        DebugLog.e("---queryOnlineDevices----");
        String nameSpace="http://tempuri.org/";
        String methodName = "MemberOnLine";
        AbSoapParams params = new AbSoapParams();
        params.put("name",application.mUser.getUserName());
        mAbSoapUtil= AbSoapUtil.getInstance(mActivity);
        mAbSoapUtil.setTimeout(10000);
        mj_showLoadView();
        mAbSoapUtil.call(AppHttpUtil.BASE_URL,nameSpace,methodName,params, new AbSoapListener() {

            //获取数据成功会调用这里
            @Override
            public void onSuccess(int statusCode, SoapObject object) {
                DebugLog.e(object.toString());
                String MemberOnLineResult = object.getPrimitivePropertyAsString("MemberOnLineResult");
                JSONObject jobj=null;
                try {
                    jobj=new JSONObject(MemberOnLineResult);
                } catch (JSONException e) {
                    AbToastUtil.showToast(mActivity, "系统返回异常！");
                    e.printStackTrace();
                }
                String result =jobj.optString("Result");
                if(result.equals("ok")){
                    AbToastUtil.showToast(mActivity, "获取数据成功！");
                    try {
                        ArrayList<Devices> data =new ArrayList<Devices>();
                        parseJsonOnline(jobj.getJSONArray("Model"),data);
                        DebugLog.d("data.size ="+data.size());
                        onLineDeviceList.clear();
                        onLineDeviceList.addAll(data);
                        mHandler.sendEmptyMessage(102);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    AbToastUtil.showToast(mActivity, "获取数据异常！");
                }

                mj_hideLoadView();
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, String content,
                                  Throwable error) {
                DebugLog.e("onFailure-----"+error.toString()+content);
                AbToastUtil.showToast(mActivity, "获取数据失败！");
                mj_hideLoadView();
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, SoapFault fault) {
                DebugLog.e("onFailure-----");
                AbToastUtil.showToast(mActivity, "获取数据失败！");
                mj_hideLoadView();
            }

            // 开始执行前
            @Override
            public void onStart() {

            }

            // 完成后调用，失败，成功
            @Override
            public void onFinish() {
                Log.d("majin","onFinish");
            };

        });

    }

    /**
     * 解析在线刷新的数据
     */
    public void parseJsonOnline(JSONArray jsonArray,List<Devices> devicesList){
        if (null==devicesList){
            devicesList =new ArrayList<Devices>();
        }
        devicesList.clear();
        for (int i=0;i<jsonArray.length();i++){
            JSONObject jb =jsonArray.optJSONObject(i);
            Devices device =new Devices();
            device.setIMEI(jb.optString("IMEI"));
            device.setIMSI(jb.optString("IMSI"));
            device.setName(jb.optString("Name"));
            device.setOnTime(jb.optString("OnTime"));
            device.setLineStatus(jb.optString("LineStatus"));
            device.setFromDate(jb.optString("FromDate"));
            device.setWarnNo(jb.optString("WarnNo"));
            device.setSIMNo(jb.optString("SIMNo"));
            device.setUserName(jb.optString("UserName"));
            devicesList.add(device);
        }

    }
    private void queryOfflineDevices(){
        DebugLog.e("---queryOnlineDevices----");
        mj_showLoadView();
        String nameSpace="http://tempuri.org/";
        String methodName = "MemberOffLine";
        AbSoapParams params = new AbSoapParams();
        params.put("name",application.mUser.getUserName());
        mAbSoapUtil= AbSoapUtil.getInstance(mActivity);
        mAbSoapUtil.setTimeout(10000);
        mAbSoapUtil.call(AppHttpUtil.BASE_URL,nameSpace,methodName,params, new AbSoapListener() {

            //获取数据成功会调用这里
            @Override
            public void onSuccess(int statusCode, SoapObject object) {
                DebugLog.e(object.toString());
                String MemberOnLineResult = object.getPrimitivePropertyAsString("MemberOffLineResult");
                JSONObject jobj=null;
                try {
                    jobj=new JSONObject(MemberOnLineResult);
                } catch (JSONException e) {
                    AbToastUtil.showToast(mActivity, "系统返回异常！");
                    e.printStackTrace();
                }
                String result =jobj.optString("Result");
                if(result.equals("ok")){
                    AbToastUtil.showToast(mActivity, "获取数据成功！");
                    try {
                        ArrayList<Devices> data =new ArrayList<Devices>();
                        parseJsonOnline(jobj.getJSONArray("Model"),data);
                        DebugLog.d("data.size ="+data.size());
                        offLineDeviceList.clear();
                        offLineDeviceList.addAll(data);
                        mHandler.sendEmptyMessage(103);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    AbToastUtil.showToast(mActivity, "获取数据异常！");
                }

                mj_hideLoadView();
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, String content,
                                  Throwable error) {
                DebugLog.e("onFailure-----"+error.toString()+content);
                AbToastUtil.showToast(mActivity, "获取数据失败！");
                mj_hideLoadView();
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, SoapFault fault) {
                DebugLog.e("onFailure-----");
                AbToastUtil.showToast(mActivity, "获取数据失败！");
                mj_hideLoadView();
            }

            // 开始执行前
            @Override
            public void onStart() {

            }

            // 完成后调用，失败，成功
            @Override
            public void onFinish() {
                Log.d("majin","onFinish");
            };

        });

    }





}
