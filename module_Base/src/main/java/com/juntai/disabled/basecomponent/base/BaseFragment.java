package com.juntai.disabled.basecomponent.base;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.juntai.disabled.basecomponent.app.BaseApplication;
import com.juntai.disabled.basecomponent.utils.DisplayUtil;
import com.squareup.leakcanary.RefWatcher;
import com.trello.rxlifecycle2.components.support.RxFragment;


public abstract class BaseFragment extends RxFragment {
    protected View mRootView = null;

    protected Context mContext;
    protected Toast toast;

    protected abstract int getLayoutRes();
    protected abstract void initView();
    protected abstract void initData();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        //        EventBus.getDefault().register(this);//订阅
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutRes(), null);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }
    /**
     * 查找viewid
     * @param viewId
     * @param <V>
     * @return
     */
    public <V extends View> V getView(int viewId){
        return mRootView.findViewById(viewId);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    public void onDestroyView() {
        //        EventBus.getDefault().unregister(this);//解除订阅
        initLeakCanary();
        super.onDestroyView();
        this.mContext = null;
        this.mRootView = null;
    }
    /**
     * 用来检测所有Fragment的内存泄漏
     */
    private void initLeakCanary() {
        RefWatcher refWatcher = BaseApplication.getRefWatcher(mContext);
        if (refWatcher != null){
            refWatcher.watch(this);
        }
    }


    /**
     * 获取activity
     * @return
     */
    public BaseActivity getBaseActivity() {
        return (BaseActivity)getActivity();
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    //    public void onBaseEvent(final Integer refresh){
    //
    //    }
    /**
     * 设置顶部图标
     * @param textView
     * @param drawableId
     */
    public void initViewTopDrawable(TextView textView, int drawableId, int width, int height) {
        Drawable drawable = getResources().getDrawable(drawableId);
        drawable.setBounds(0, 0, DisplayUtil.dp2px(mContext, width), DisplayUtil.dp2px(mContext, height));//第一个 0 是距左边距离，第二个 0
        // 是距上边距离，40 分别是长宽
        textView.setCompoundDrawables(null, drawable, null, null);//放顶部
    }
}
