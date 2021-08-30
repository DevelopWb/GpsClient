package com.mj.gpsclient.Activity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.ab.activity.AbActivity;
import com.ab.model.AbMenuItem;
import com.ab.view.sample.AbViewPager;
import com.ab.view.sliding.AbBottomTabView;
import com.ab.view.titlebar.AbTitleBar;
import com.mj.gpsclient.R;
import com.mj.gpsclient.adapter.ListPopAdapter;
import com.mj.gpsclient.global.DebugLog;

import java.util.ArrayList;
import java.util.List;

public class DevicesTrackActivity extends AbActivity {

    private List<Drawable> tabDrawables = null;
    private AbBottomTabView mBottomTabView;
    private String DeviceName;
    private AbTitleBar mAbTitleBar;
    private TrackRealtimeFragment trackRealtimeFragment;
    private TrackHistoryFragment trackHistoryFragment;
    private TrackFenceFragment trackFenceFragment;
    private TrackAlarmListFragment trackAlarmListFragment;
    private View popView;
    private TextView playProgress;
    private ImageView playOrPause;
    private int currentIndex;
    private Switch aSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatas();
        setAbContentView(R.layout.activity_devices_track);
        resetTileBar();
        mAbTitleBar = this.getTitleBar();
        mAbTitleBar.setTitleText("实时追踪");
        mAbTitleBar.setLogo(R.drawable.back_n);
        mAbTitleBar.setTitleBarBackground(R.drawable.tab_top_bg);
        mAbTitleBar.getLogoView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DevicesTrackActivity.this.finish();
            }
        });

        mAbTitleBar.setTitleTextMargin(0, 0, 0, 0);
        mAbTitleBar.getTitleTextLayout().setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        mAbTitleBar.setLogoLine(R.drawable.line);
        mAbTitleBar.getTitleTextButton().setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

        View rightViewtest = mInflater.inflate(R.layout.test_moni, null);
        aSwitch= (Switch) rightViewtest.findViewById(R.id.switch_test);
        aSwitch.setChecked(false);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               // if(!isChecked){
                    Message msg =Message.obtain();
                    msg.what =trackRealtimeFragment.MAP_AUTO_TRACK_SWITCH;
                    msg.arg1 =(isChecked?1:0);
                    trackRealtimeFragment.mHandler.sendMessage(msg);
              //  }

            }
        });
        mAbTitleBar.addRightView(rightViewtest);
        mAbTitleBar.setTitleBarGravity(Gravity.CENTER, Gravity.CENTER);
        initTitleRightLayout();
        //setTitleMargin();

        mBottomTabView = (AbBottomTabView) findViewById(R.id.device_track_BottomTabView);

        //如果里面的页面列表不能下载原因：
        //Fragment里面用的AbTaskQueue,由于有多个tab，顺序下载有延迟，还没下载好就被缓存了。改成用AbTaskPool，就ok了。
        //或者setOffscreenPageLimit(0)

        //缓存数量
        mBottomTabView.getViewPager().setOffscreenPageLimit(4);

        ((AbViewPager)mBottomTabView.getViewPager()).setPagingEnabled(false);
//        mBottomTabView.getViewPager().setEnabled(false);
        //禁止滑动
        mBottomTabView.getViewPager().setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }

        });

        mBottomTabView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                setBarTitle(i);
                //通知轨迹界面停止
                if(i!=1){
                    trackHistoryFragment.mHandler.sendEmptyMessageDelayed(6,0);
                    trackHistoryFragment.mHandler.sendEmptyMessageDelayed(9,0);
                }
                if(i!=0){
                    //暂停实时监控的界面
                    trackRealtimeFragment.mHandler.sendEmptyMessage(TrackRealtimeFragment.MAP_PAUSE);
                }
                if(i==0){
                    //获取一次数据
                    trackRealtimeFragment.mHandler.sendEmptyMessage(TrackRealtimeFragment.GET_DEVICE_DATA);
                }

                currentIndex=i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        trackRealtimeFragment = new TrackRealtimeFragment();
        trackHistoryFragment = new TrackHistoryFragment();
        trackFenceFragment = new TrackFenceFragment();
        trackAlarmListFragment = new TrackAlarmListFragment();

        List<Fragment> mFragments = new ArrayList<Fragment>();
        mFragments.add(trackRealtimeFragment);
        mFragments.add(trackHistoryFragment);
//        mFragments.add(trackFenceFragment);
//        mFragments.add(trackAlarmListFragment);

        List<String> tabTexts = new ArrayList<String>();
        tabTexts.add("实时追踪");
        tabTexts.add("历史回放");
//        tabTexts.add("电子围栏");
//        tabTexts.add("车辆报警");

        //设置样式
        mBottomTabView.setTabTextColor(Color.rgb(255, 255,255));
        mBottomTabView.setTabSelectColor(Color.rgb(234, 110, 9));
        mBottomTabView.setTabTextSize(20);
//        mBottomTabView.setTabBackgroundResource(R.drawable.tab_bg2);
        mBottomTabView.setTabBackgroundResource(R.drawable.tab_home_bg);
        mBottomTabView.setTabLayoutBackgroundResource(R.drawable.tab_home_bg);

        //注意图片的顺序
        tabDrawables = new ArrayList<Drawable>();
        tabDrawables.add(this.getResources().getDrawable(R.drawable.tracking_older));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.tracking_old_press));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.history_old));
        tabDrawables.add(this.getResources().getDrawable(R.drawable.history_old_press));
//        tabDrawables.add(this.getResources().getDrawable(R.drawable.geofence_old));
//        tabDrawables.add(this.getResources().getDrawable(R.drawable.geofence_old_press));
//        tabDrawables.add(this.getResources().getDrawable(R.drawable.more_old));
//        tabDrawables.add(this.getResources().getDrawable(R.drawable.more_old_press));
        mBottomTabView.setTabCompoundDrawablesBounds(0, 0, 50, 50);
        //演示增加一组
        mBottomTabView.addItemViews(tabTexts, mFragments, tabDrawables);

        mBottomTabView.setTabPadding(10, 10, 10, 10);

    }

  private void initDatas(){
      Intent in =this.getIntent();
      DeviceName = in.getStringExtra("memberName");
      currentIndex=0;
  }

  public String getDeviceName(){
      return DeviceName;
  }

    public void setBarTitle(int i) {
        mAbTitleBar.clearRightView();
        mAbTitleBar.setTitleTextSize(20);
        Drawable d = null;//getApplication().getResources().getDrawable(R.color.photo_tab_red);
        switch (i) {
            case 0:
                resetTileBar();

                mAbTitleBar.setTitleTextBackgroundDrawable(d);
                mAbTitleBar.setTitleText("实时追踪");
                mAbTitleBar.setTitleTextOnClickListener(null);
                trackRealtimeFragment.mHandler.sendEmptyMessageDelayed(3,500);
                View rightViewtest = mInflater.inflate(R.layout.test_moni, null);
                aSwitch= (Switch) rightViewtest.findViewById(R.id.switch_test);
                aSwitch.setChecked(false);
                aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(!isChecked){
                            trackRealtimeFragment.mHandler.sendEmptyMessage(100);
                        }

                    }
                });
                mAbTitleBar.addRightView(rightViewtest);
                mAbTitleBar.setTitleBarGravity(Gravity.CENTER, Gravity.CENTER);
                //setTitleMargin();

                break;
            case 1:
                resetTileBar();
                mAbTitleBar.setTitleText("请选择");
                mAbTitleBar.setTitleTextSize(20);
                setListTitle();

                View rightViewProgress = mInflater.inflate(R.layout.step_progress, null);
                playProgress = (TextView) rightViewProgress.findViewById(R.id.play_progress);
                mAbTitleBar.addRightView(rightViewProgress);
                mAbTitleBar.setTitleBarGravity(Gravity.CENTER, Gravity.CENTER);

                View rightViewApp = mInflater.inflate(R.layout.more_btn, null);
                playOrPause = (ImageView) rightViewApp.findViewById(R.id.play_pause);
                playOrPause.setTag(R.drawable.pause_binoculars);
                playOrPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //trackHistoryFragment.mHandler.sendEmptyMessage(9);
                        if((Integer)v.getTag()==R.drawable.pause_binoculars){
                            playOrPause.setImageDrawable(abApplication.getResources().getDrawable(R.drawable.play_binoculars));
                            playOrPause.setTag(R.drawable.play_binoculars);
                        }else{
                            playOrPause.setImageDrawable(abApplication.getResources().getDrawable(R.drawable.pause_binoculars));
                            playOrPause.setTag(R.drawable.pause_binoculars);
                            trackHistoryFragment.mHandler.sendEmptyMessage(8);
                        }


                    }
                });
                mAbTitleBar.addRightView(rightViewApp);
                mAbTitleBar.setTitleBarGravity(Gravity.LEFT, Gravity.CENTER);



                //trackHistoryFragment.mHandler.sendEmptyMessageDelayed(3,500);
                break;
            case 2:

                resetTileBar();
                mAbTitleBar.setTitleTextBackgroundDrawable(d);
                mAbTitleBar.setTitleText("电子围墙");
                mAbTitleBar.setTitleTextBackgroundDrawable(d);
                mAbTitleBar.setTitleTextOnClickListener(null);
                View rightViewApp3 = mInflater.inflate(R.layout.refresh_btn, null);
                ImageView img2 = (ImageView) rightViewApp3.findViewById(R.id.bu_refresh);
                img2.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                    }
                });
                mAbTitleBar.addRightView(rightViewApp3);
                mAbTitleBar.setTitleBarGravity(Gravity.CENTER, Gravity.CENTER);
                rightViewApp3.setVisibility(View.INVISIBLE);
               // setTitleMargin();

                break;
            case 3:
                resetTileBar();
                mAbTitleBar.setTitleText("车辆报警");
                mAbTitleBar.setTitleTextBackgroundDrawable(d);
                mAbTitleBar.setTitleTextOnClickListener(null);
                View rightViewApp2 = mInflater.inflate(R.layout.refresh_btn, null);
                ImageView img = (ImageView) rightViewApp2.findViewById(R.id.bu_refresh);
                img.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                    }
                });
                mAbTitleBar.addRightView(rightViewApp2);
                mAbTitleBar.setTitleBarGravity(Gravity.CENTER, Gravity.CENTER);
               // setTitleMargin();
                break;
            default:
                break;
        }



    }
    private void resetTileBar(){
//        AbTitleBar mAbTitleBar = this.getTitleBar();
//        mAbTitleBar.clearRightView();
//        mAbTitleBar.setLogo(R.drawable.back_n);
//        mAbTitleBar.setLogoLine(R.drawable.line);
//        mAbTitleBar.setLogoOnClickListener(null);
//        mAbTitleBar.setPadding(0, 0, 0, 0);
//        mAbTitleBar.setTitleTextMargin(0,0,0,0);
//        mAbTitleBar.setTitleBarBackground(R.drawable.tab_top_bg);
//        mAbTitleBar.setTitleBarGravity(Gravity.CENTER,Gravity.CENTER);
//        mAbTitleBar.setLogoLine(R.drawable.line);
//        mAbTitleBar.getTitleTextLayout().setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAbTitleBar.getTitleTextLayout().getLayoutParams();
//        params.setMargins(0,0,0,0);
//        params.leftMargin=0;
//        params.rightMargin=0;
//        params.gravity =Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
//        mAbTitleBar.getTitleTextLayout().setLayoutParams(params);
//        mAbTitleBar.getTitleTextButton().setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

//        mAbTitleBar.getTitleTextLayout().setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

    }
    private void setTitleMargin(){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAbTitleBar.getTitleTextLayout().getLayoutParams();
        int myLeft = mAbTitleBar.getTitleTextLayout().getLeft();
        int myWidth =mAbTitleBar.getTitleTextLayout().getWidth();
        int cLeft =mAbTitleBar.getWidth()/2;
        int dd =cLeft-myLeft;
        if (dd>0){
            params.leftMargin+=Math.abs(dd);
        }else{
            params.rightMargin+=Math.abs(dd);
        }
        params.gravity =Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
        DebugLog.e("cLeft=" + cLeft + " myLeft =" + myLeft + "   dd =" + dd);
//        mAbTitleBar.getTitleTextLayout().setLeft(cLeft);
        mAbTitleBar.getTitleTextLayout().setLayoutParams(params);
    }



    public void setListTitle(){
        mAbTitleBar.setTitleTextBackgroundResource(R.drawable.drop_down_title_btn);
        popView = mInflater.inflate(R.layout.list_pop, null);
        ListView popListView = (ListView) popView.findViewById(R.id.pop_list);
        List<AbMenuItem> list = new ArrayList<AbMenuItem>();
        list.add(new AbMenuItem("当天"));
        list.add(new AbMenuItem("过去3天"));
        list.add(new AbMenuItem("过去一周"));
        list.add(new AbMenuItem("自定义"));
        ListPopAdapter mListPopAdapter = new ListPopAdapter(DevicesTrackActivity.this, list,R.layout.item_list_pop);
        popListView.setAdapter(mListPopAdapter);
        mAbTitleBar.setTitleTextDropDown(popView);
        popListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        mAbTitleBar.setTitleText("当天");
                        break;
                    case 1:
                        mAbTitleBar.setTitleText("过去3天");
                        break;
                    case 2:
                        mAbTitleBar.setTitleText("过去一周");
                        break;
                    case 3:
                        mAbTitleBar.setTitleText("自定义");
                        break;
                    default:
                        break;
                }
                mAbTitleBar.hideWindow();
//                mAbTitleBar.setTitleTextBackgroundDrawable(null);
//                mAbTitleBar.setTitleTextOnClickListener(null);
                changeHistoryTime(i);
            }
        });

        mAbTitleBar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message msg =Message.obtain();
                msg.what =5;
                msg.arg1 =-1;
                trackHistoryFragment.mHandler.sendEmptyMessage(5);
            }
        });
        mAbTitleBar.setTitleTextOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!popView.isShown()){
                    mAbTitleBar.showWindow(view,popView,true);
                }else{
                    mAbTitleBar.hideWindow();
                }
            }
        });

    }

    public void setStepProgress(String text){
        if (null!=playProgress){
            playProgress.setText(text);
        }

    }

    public void setPlayOrPauseEnable(boolean isEnable){
        if (null!=playOrPause){
            //playProgress.setText(text);
            playOrPause.setEnabled(isEnable);
        }

    }

    public void setPause(boolean isEnd){
        if(isEnd){
            playOrPause.setImageDrawable(abApplication.getResources().getDrawable(R.drawable.play_binoculars));
            playOrPause.setTag(R.drawable.play_binoculars);
        }
    }

    public boolean isPauseStep(){
        return (playOrPause!=null&&(Integer)playOrPause.getTag()!=R.drawable.pause_binoculars);
    }


    public int  getCurrentIndexFragmentIndex(){

        return currentIndex;
    }


    public boolean isRealtimeMode(){
        return aSwitch!=null&&aSwitch.isChecked();
    }

    private void  togglePopView(){
        if(popView.isShown()){

        }else{

        }
    }



    private void changeHistoryTime(int index ){
        Message msg =Message.obtain();
        msg.what=4;
        msg.arg1 =index;
        trackHistoryFragment.mHandler.sendMessage(msg);

    }

    private void initData() {
        Intent in = this.getIntent();
        String model = in.getStringExtra("model");
        if (TextUtils.isEmpty(model)) {
            return;
        }

    }

    /**
     * 实时追踪中的导航模式
     */
    public void toNavigationMode(String title){
        aSwitch.setChecked(false);
        //隐藏那个右侧的实时导航开关
        mAbTitleBar.getRightLayout().setVisibility(View.INVISIBLE);
        mAbTitleBar.setTitleText(title);


    }

    /**
     * 退出导航模式
     */
    public  void exitNavigationMode(){
        mAbTitleBar.setTitleText("实时追踪");
        mAbTitleBar.getRightLayout().setVisibility(View.VISIBLE);
    }

    public boolean  isRealTimeModle(){
        return mAbTitleBar.getTitleTextButton().getText().equals("实时追踪");

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


   protected void onPause(){
        super.onPause();
       if(trackHistoryFragment.mHandler!=null){
           trackHistoryFragment.mHandler.sendEmptyMessage(9);
       }

    }

    protected void onStop() {
        super.onPause();
        if(trackHistoryFragment.mHandler!=null){
            trackHistoryFragment.mHandler.sendEmptyMessage(9);
        }
    }

    private void initTitleRightLayout() {

    }
}
