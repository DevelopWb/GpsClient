package com.mj.gpsclient.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ab.activity.AbActivity;
import com.ab.fragment.AbProgressDialogFragment;
import com.ab.fragment.AbSampleDialogFragment;
import com.ab.soap.AbSoapListener;
import com.ab.soap.AbSoapParams;
import com.ab.soap.AbSoapUtil;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbToastUtil;
import com.ab.view.titlebar.AbTitleBar;
import com.ab.view.wheel.AbNumericWheelAdapter;
import com.ab.view.wheel.AbWheelUtil;
import com.ab.view.wheel.AbWheelView;
import com.mj.gpsclient.AppHttpUtil;
import com.mj.gpsclient.R;
import com.mj.gpsclient.global.MyApplication;
import com.mj.gpsclient.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;


public class LoginActivity extends AbActivity implements View.OnClickListener{
    private EditText mEdName;
    private EditText mEdPass;
    private CheckBox mCb;
    private Button mBtLogin;
    private AbSoapUtil mAbSoapUtil ;
    private AbTitleBar mAbTitleBar = null;
    private MyApplication application;
    private CheckBox checkBox;
    private RelativeLayout loadingLayout;
    private RelativeLayout loginLayout;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int result = msg.what;
            switch (result) {
                case 0: //搜索点击
                   login();
                break;
                default:
                    break;
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAbContentView(R.layout.activity_login);
        application =(MyApplication)abApplication;
        initView();
        if(null!=application.mUser){
            mEdName.setText(application.mUser.getUserName());
            mEdPass.setText(application.mUser.getPassword());
        }

        if(application.mUser !=null&&!TextUtils.isEmpty(application.mUser.getUserName())&&
                !TextUtils.isEmpty(application.mUser.getPassword())){
//            loginLayout.setVisibility(View.GONE);
//            loadingLayout.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(0,100);
        }else{
//            loadingLayout.setVisibility(View.GONE);
//            loginLayout.setVisibility(View.VISIBLE);
        }

    }

    private void initView(){
        mEdName = (EditText) findViewById(R.id.editText_user_name);
        mEdPass = (EditText) findViewById(R.id.editText_user_pass);
        mCb = (CheckBox) findViewById(R.id.user_sava_pass);
        mBtLogin = (Button) findViewById(R.id.btn_login);
        checkBox = (CheckBox) findViewById(R.id.user_sava_pass);
        checkBox.setChecked(true);
        loadingLayout = (RelativeLayout) findViewById(R.id.login_bg);
        loginLayout = (RelativeLayout) findViewById(R.id.login_content);
        mBtLogin.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                login();
                break;
            default:
                break;
        }
    }

    private void login(){
        hideKeyboardFromView(mBtLogin);
        String name =mEdName.getText().toString();
        String pass =mEdPass.getText().toString();
        if(TextUtils.isEmpty(name)||TextUtils.isEmpty(pass)) {
            return;
        }
        loginWithName(name,pass);
        AbProgressDialogFragment dialogFragment=AbDialogUtil.showProgressDialog(LoginActivity.this, 0,
                "登录中...");
        dialogFragment.setCancelable(true);
    }

    private void loginWithName(final String name, final String pass){

        String nameSpace="http://tempuri.org/";
        String methodName = "Login";
        AbSoapParams params = new AbSoapParams();
        params.put("name",name);
        params.put("password",pass);
        mAbSoapUtil= AbSoapUtil.getInstance(this);
        mAbSoapUtil.setTimeout(10000);
        mAbSoapUtil.call(AppHttpUtil.BASE_URL,nameSpace,methodName,params, new AbSoapListener() {

            //获取数据成功会调用这里
            @Override
            public void onSuccess(int statusCode, SoapObject object) {

                String LoginResult = object.getPrimitivePropertyAsString("LoginResult");
               // Log.d("majin", "statusCode=" + statusCode + "SoapObject=" + LoginResult);
                AbDialogUtil.removeDialog(LoginActivity.this);
                JSONObject jobj=null;
                try {
                    jobj=new JSONObject(LoginResult);
                } catch (JSONException e) {
                    AbToastUtil.showToast(LoginActivity.this, "系统返回异常！");
                    e.printStackTrace();
                }
                String result =jobj.optString("Result");
                if(result.equals("ok")){
                    AbToastUtil.showToast(LoginActivity.this, "登录成功！");
                    JSONArray jmode =  jobj.optJSONArray("Model");
                    User user =new User();
                    user.setUserName(name);
                    user.setPassword(pass);
//                    if(checkBox.isChecked()){
//                        application.updateLoginParams(user);
//                    }
                    application.userPasswordRemember =checkBox.isChecked();
                    application.updateLoginParams(user);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("model",jmode.toString());
                    startActivity(intent);
                    finish();
                }else {
                    AbToastUtil.showToast(LoginActivity.this, "用户名或密码错误！");
                }

            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, String content,
                                  Throwable error) {
                AbToastUtil.showToast(LoginActivity.this, content);
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, SoapFault fault) {

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

    private void testWheel(){

        View mTimeView2=null;
        TextView timeTextView2 =new TextView(application);
        mTimeView2 = mInflater.inflate(R.layout.choose_three, null);
        initWheelTime(mTimeView2,timeTextView2);

        final AbWheelView mWheelView1 = (AbWheelView)mTimeView2.findViewById(R.id.wheelView0);
        mWheelView1.setAdapter(new AbNumericWheelAdapter(2000, 2020));
        // 可循环滚动
        mWheelView1.setCyclic(true);
        // 添加文字
        mWheelView1.setLabel("      年");
        // 初始化时显示的数据
        mWheelView1.setCurrentItem(15);
        mWheelView1.setValueTextSize(35);
        mWheelView1.setLabelTextSize(35);
        mWheelView1.setLabelTextColor(0x80000000);
        mWheelView1.setCenterSelectDrawable(this.getResources().getDrawable(R.drawable.wheel_select));

        AbSampleDialogFragment abs = AbDialogUtil.showDialog(mTimeView2, Gravity.BOTTOM);
        abs.setOnDismissListener(new DialogInterface.OnDismissListener(){

            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                int index = mWheelView1.getCurrentItem();
                String val = mWheelView1.getAdapter().getItem(index);
                Log.d("majin","val =" +val+"---");
            }
        });

    }

    public void initWheelTime(View mTimeView,TextView mText){
        final AbWheelView mWheelViewMD = (AbWheelView)mTimeView.findViewById(R.id.wheelView1);
        final AbWheelView mWheelViewMM = (AbWheelView)mTimeView.findViewById(R.id.wheelView2);
        final AbWheelView mWheelViewHH = (AbWheelView)mTimeView.findViewById(R.id.wheelView3);
        Button okBtn = (Button)mTimeView.findViewById(R.id.okBtn);
        Button cancelBtn = (Button)mTimeView.findViewById(R.id.cancelBtn);
        mWheelViewMD.setCenterSelectDrawable(this.getResources().getDrawable(R.drawable.wheel_select));
        mWheelViewMM.setCenterSelectDrawable(this.getResources().getDrawable(R.drawable.wheel_select));
        mWheelViewHH.setCenterSelectDrawable(this.getResources().getDrawable(R.drawable.wheel_select));
        AbWheelUtil.initWheelTimePicker(this, mText, mWheelViewMD, mWheelViewMM, mWheelViewHH, okBtn, cancelBtn, 2013, 1, 1, 10, 0, true);
    }

}
