package com.juntai.disabled.basecomponent.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.juntai.disabled.basecomponent.BuildConfig;
import com.juntai.disabled.basecomponent.R;
import com.juntai.disabled.basecomponent.utils.LogUtil;
import com.juntai.disabled.basecomponent.utils.NavigationBarInfo;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.interfaces.BetaPatchListener;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Locale;


public abstract class BaseApplication extends Application {
    public static int HEIGHT, width, statusBarH;
    public static int navigationBarH;
    public static BaseApplication app;
    private RefWatcher mRefWatcher;
    ArrayList<Activity> activities = new ArrayList<>();
    public static boolean isReLoadWarn = true;//登录被顶，是否提示

    /*app处于后台true,前台false*/
    public abstract void appBackground(boolean isBackground, Activity activity);

    public abstract String getTinkerId();

    //活动的activity数量
    int mActivityCount;
    //应用处于前台运行中
    public boolean isRun = true;
    //
    SoftReference<Activity> softReference;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        getScreen(this);
        if (BuildConfig.DEBUG) {
            //
            Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder().
                    tag(getString(R.string.app_name)).build()));
            LogUtil.logInit(true);
        }
        initLeakCanary();
//        registerActivityLifecycleCallbacks(mCallbacks);
//        hotFix();
        // 调试时，将第三个参数改为true


    }

    private void hotFix() {
        // 设置是否开启热更新能力，默认为true
        Beta.enableHotfix = true;
        // 设置是否自动下载补丁，默认为true
        Beta.canAutoDownloadPatch = true;
        // 设置是否自动合成补丁，默认为true
        Beta.canAutoPatch = true;
        // 设置是否提示用户重启，默认为false
        Beta.canNotifyUserRestart = true;
        // 补丁回调接口
        Beta.betaPatchListener = new BetaPatchListener() {
            @Override
            public void onPatchReceived(String patchFile) {
                Toast.makeText(getApplicationContext(), "补丁下载地址" + patchFile, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadReceived(long savedLength, long totalLength) {
                Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(), "%s %d%%", Beta.strNotificationDownloading, (int) (totalLength == 0 ? 0 : savedLength * 100 / totalLength)), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadSuccess(String msg) {
                Toast.makeText(getApplicationContext(), "补丁下载成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadFailure(String msg) {
                Toast.makeText(getApplicationContext(), "补丁下载失败", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onApplySuccess(String msg) {
                Toast.makeText(getApplicationContext(), "补丁应用成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApplyFailure(String msg) {
                Toast.makeText(getApplicationContext(), "补丁应用失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPatchRollback() {

            }
        };

        // 设置开发设备，默认为false，上传补丁如果下发范围指定为“开发设备”，需要调用此接口来标识开发设备
        Bugly.setIsDevelopmentDevice(getApplicationContext(), true);
        // 调试时，将第三个参数改为true
        Bugly.init(this, getTinkerId(), true);
    }




    /**
     * 记录当前正在活动的activity
     *
     * @return
     */
    public Activity getNowActivity() {
        if (softReference != null) {
            return softReference.get();
        }
       return null;
    }


    public void getScreen(Context aty) {
        //依赖于手机系统，获取到的是系统的屏幕信息；
        DisplayMetrics dm = aty.getResources().getDisplayMetrics();
        HEIGHT = dm.heightPixels;
        width = dm.widthPixels;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarH = getResources().getDimensionPixelSize(resourceId);
        }

        if (NavigationBarInfo.hasNavBar(aty)) {
            int navigationId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (navigationId > 0) {
                navigationBarH = getResources().getDimensionPixelSize(navigationId);
            }
        }
    }

    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        mRefWatcher = LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        return app.mRefWatcher;
    }

    /**
     * 下拉刷新
     * static 代码段可以防止内存泄露
     */
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(android.R.color.white, R.color.black);//全局设置主题颜色
                return new ClassicsHeader(context).setTextSizeTitle(13).setTextSizeTime(10).setDrawableArrowSize(15);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(15).setTextSizeTitle(13);
            }
        });
    }

//    private ActivityLifecycleCallbacks mCallbacks = new ActivityLifecycleCallbacks() {
//
//        @Override
//        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//            //LogUtil.d("Activity-onActivityCreated = " + activity.getClass().getName());
//            activities.add(activity);
//        }
//
//        @Override
//        public void onActivityStarted(Activity activity) {
//            mActivityCount++;
//            if (mActivityCount == 1) {
//                appBackground(false, activity);
//                isRun = true;
//            }
//            softReference = null;
//            softReference = new SoftReference<>(activity);
//        }
//
//        @Override
//        public void onActivityResumed(Activity activity) {
//        }
//
//        @Override
//        public void onActivityPaused(Activity activity) {
//        }
//
//        @Override
//        public void onActivityStopped(Activity activity) {
//            mActivityCount--;
//            if (mActivityCount == 0) {
//                appBackground(true, activity);
//                isRun = false;
//            }
//        }
//
//        @Override
//        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//        }
//
//        @Override
//        public void onActivityDestroyed(Activity activity) {
//            //LogUtil.d("Activity-onActivityDestroyed = " + activity.getClass().getName());
//            activities.remove(activity);
//        }
//    };


}
