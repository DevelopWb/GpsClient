package com.mj.gpsclient.Activity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.ab.activity.AbActivity;
import com.ab.util.AbToastUtil;
import com.ab.view.sample.AbViewPager;
import com.ab.view.sliding.AbBottomTabView;
import com.ab.view.titlebar.AbTitleBar;
import com.mj.gpsclient.R;
import com.mj.gpsclient.model.Devices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AbActivity {

    private AbBottomTabView mBottomTabView;
    private List<Drawable> tabDrawables = null;
    private List<Devices> devices;
    private List<Devices> devicesesOnline;
    private List<Devices> devicesesOffline;
    private Boolean isExit = false;
    private DevicesListFragment devicesListFragment;
    private DevicesMonitorFragment devicesMonitorFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAbContentView(R.layout.activity_main);
        initData();
        AbTitleBar mAbTitleBar = this.getTitleBar();
        mAbTitleBar.setTitleText("设备列表");
        //mAbTitleBar.setLogo(R.drawable.ic_launcher);
        mAbTitleBar.setTitleBarBackground(R.drawable.tab_top_bg);
        mAbTitleBar.setTitleBarGravity(Gravity.CENTER,Gravity.CENTER);
        mAbTitleBar.setTitleTextMargin(0, 0, 0, 0);
        mAbTitleBar.setLogoLine(R.drawable.line);
        initTitleRightLayout();
        setBarTitle(0);
        mAbTitleBar.setTitleTextSize(20);
        mBottomTabView = (AbBottomTabView) findViewById(R.id.mBottomTabView);
        //如果里面的页面列表不能下载原因：
        //Fragment里面用的AbTaskQueue,由于有多个tab，顺序下载有延迟，还没下载好就被缓存了。改成用AbTaskPool，就ok了。
        //或者setOffscreenPageLimit(0)

        //缓存数量
        mBottomTabView.getViewPager().setOffscreenPageLimit(5);
        ((AbViewPager)mBottomTabView.getViewPager()).setPagingEnabled(false);

        mBottomTabView.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){


            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                setBarTitle(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        devicesListFragment = new DevicesListFragment();
        devicesMonitorFragment = new DevicesMonitorFragment();
        Fragment page3 = new DevicesAlarmFragment();
        Fragment page4 = new MoreFragment();

        List<Fragment> mFragments = new ArrayList<Fragment>();
        mFragments.add(devicesListFragment);
        mFragments.add(devicesMonitorFragment);
//        mFragments.add(page3);
        mFragments.add(page4);

        List<String> tabTexts = new ArrayList<String>();
        tabTexts.add("车辆列表");
        tabTexts.add("多车监控");
//        tabTexts.add("车辆报警");
        tabTexts.add("更多");

        //设置样式
        mBottomTabView.setTabTextColor(Color.rgb(255, 255,255));
        mBottomTabView.setTabSelectColor(Color.rgb(234, 110, 9));
        mBottomTabView.setTabTextSize(20);
        mBottomTabView.setTabBackgroundResource(R.drawable.tab_home_bg);
        mBottomTabView.setTabLayoutBackgroundResource(R.drawable.tab_home_bg);

        //注意图片的顺序
        tabDrawables = new ArrayList<Drawable>();
        tabDrawables.add(this.getResources().getDrawable(R.drawable.home_old));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.home_old_press));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.carsmonitor_old));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.carsmonitor_old_press));
//        tabDrawables.add(this.getResources().getDrawable(R.drawable.warn_old));
//        tabDrawables.add(this.getResources().getDrawable(R.drawable.warn_old_press));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.more_old));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.more_old_press));
        mBottomTabView.setTabCompoundDrawablesBounds(0, 0, 50, 50);
        //演示增加一组
        mBottomTabView.addItemViews(tabTexts,mFragments,tabDrawables);

        mBottomTabView.setTabPadding(10,10, 10, 10);

    }

    public void setBarTitle(int i){
        AbTitleBar mAbTitleBar = this.getTitleBar();
       switch (i){
           case 0:
               resetTileBar();
               mAbTitleBar.setTitleText("设备列表");
               View rightViewApp = mInflater.inflate(R.layout.refresh_btn, null);
               ImageView img = (ImageView) rightViewApp.findViewById(R.id.bu_refresh);
               img.setOnClickListener(new View.OnClickListener(){
                   @Override
                   public void onClick(View v) {
                       devicesListFragment.mHandler.sendEmptyMessage(1);
                    }
                });
               mAbTitleBar.addRightView(rightViewApp);
               mAbTitleBar.setLogo(R.drawable.title_search);
               mAbTitleBar.setLogoLine(null);
               mAbTitleBar.setLogoOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       devicesListFragment.mHandler.sendEmptyMessage(0);
                   }
               });
               mAbTitleBar.setPadding(100,0,0,0);
               break;
           case 1:
               resetTileBar();
               mAbTitleBar.setTitleText("多车监控");
               devicesMonitorFragment.mHandler.sendEmptyMessage(0);
               break;
           case 2:
               resetTileBar();
               mAbTitleBar.setTitleText("设备报警");
               break;
           case 3:
               resetTileBar();
               mAbTitleBar.setTitleText("更多");
               break;
           default:
               break;
       }

   }


   private void resetTileBar(){
       AbTitleBar mAbTitleBar = this.getTitleBar();
       mAbTitleBar.clearRightView();
       mAbTitleBar.setLogo(null);
       mAbTitleBar.setLogoOnClickListener(null);
       mAbTitleBar.setPadding(0,0,0,0);

   }


   private  void initData(){
       Intent in = this.getIntent();
       String model =in.getStringExtra("model");
       if(TextUtils.isEmpty(model)){
           return;
       }
       devices =new ArrayList<Devices>();
       devicesesOnline =new ArrayList<Devices>();
       devicesesOffline =new ArrayList<Devices>();

       devices.clear();
       devicesesOnline.clear();
       devicesesOffline.clear();

       JSONArray jsonArray =null;
       try {
           jsonArray = new JSONArray(model);
       } catch (JSONException e) {
           e.printStackTrace();
       }
       for (int i=0;i<jsonArray.length();i++){
           JSONObject jb =jsonArray.optJSONObject(i);
           Devices device =new Devices();
           device.setName(jb.optString("name"));
           device.setLineStatus(jb.optString("linestatus"));
           if(device.getLineStatus().equals("离线")){
               devicesesOffline.add(device);
           }else{
               devicesesOnline.add(device);
           }
           devices.add(device);
       }

   }

    public List<Devices> getDevicesAll(){
        return devices;
    }
    public List<Devices> getDevicesesOnline(){
        return devicesesOnline;
    }
    public List<Devices> getDevicesesOffline(){
        return devicesesOffline;
    }


    public void refreshDeviceList(List<Devices> all,List<Devices> online,List<Devices> offline){
        devices.clear();
        devices.addAll(all);
        devicesesOnline.clear();
        devicesesOnline.addAll(online);
        devicesesOffline.clear();
        devicesesOffline.addAll(offline);
    }

    /**
     * 描述：返回.
     */
    @Override
    public void onBackPressed() {

            if (isExit == false) {
                isExit = true;
                AbToastUtil.showToast(MainActivity.this, "再按一次退出程序");
                new Handler().postDelayed(new Runnable(){

                    @Override
                    public void run() {
                        isExit = false;
                    }

                }, 2000);
            } else {
                super.onBackPressed();
            }
        }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initTitleRightLayout() {

    }


    public boolean isOnLine(String name){
        boolean isOnline  =false;
        for (Devices d:devicesesOnline){
            if (d.getName().equals(name)){
                isOnline = true;
                break;
            }
        }
        return isOnline;
    }


}
