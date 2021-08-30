package com.mj.gpsclient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ab.activity.AbActivity;
import com.ab.fragment.AbProgressDialogFragment;
import com.ab.soap.AbSoapListener;
import com.ab.soap.AbSoapParams;
import com.ab.soap.AbSoapUtil;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbToastUtil;
import com.ab.view.titlebar.AbTitleBar;
import com.mj.gpsclient.AppHttpUtil;
import com.mj.gpsclient.R;
import com.mj.gpsclient.global.DebugLog;
import com.mj.gpsclient.global.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;


public class ResetPwdActivity extends AbActivity implements View.OnClickListener{
    private EditText mEdOldPwd;
    private EditText mEdNewPwdOne;
    private EditText mEdNewPwdTwo;
    private Button mBtRest;
    private AbSoapUtil mAbSoapUtil ;
    private AbTitleBar mAbTitleBar = null;
    private MyApplication application;
    private RelativeLayout loadingLayout;
    private RelativeLayout loginLayout;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int result = msg.what;
            switch (result) {
                case 0: //重新登录
                    application.clearLoginParams();
                    Intent intent = new Intent(ResetPwdActivity.this,LoginActivity.class);
                    startActivity(intent);
                    ResetPwdActivity.this.finish();
                break;
                default:
                    break;
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAbTitleBar = this.getTitleBar();
        mAbTitleBar.setTitleText("修改密码");
        mAbTitleBar.setLogo(R.drawable.back_n);
        mAbTitleBar.setTitleBarBackground(R.drawable.tab_top_bg);
        mAbTitleBar.getLogoView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPwdActivity.this.finish();
            }
        });
        mAbTitleBar.setTitleBarGravity(Gravity.CENTER, Gravity.CENTER);
        setAbContentView(R.layout.activity_resetpwd);
        application =(MyApplication)abApplication;
        initView();

    }

    private void initView(){
        mEdOldPwd = (EditText) findViewById(R.id.editText_old_pwd);
        mEdNewPwdOne = (EditText) findViewById(R.id.editText_pwd_one);
        mEdNewPwdTwo = (EditText) findViewById(R.id.editText_pwd_two);
        mBtRest = (Button) findViewById(R.id.btn_reset);
        mBtRest.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_reset:
                goResetPwd();
                break;
            default:
                break;
        }
    }

    private void goResetPwd(){
        String oldPwd =mEdOldPwd.getText().toString();
        String newPwdOne =mEdNewPwdOne.getText().toString().trim();
        String newPwdTwd =mEdNewPwdTwo.getText().toString().trim();
        if(TextUtils.isEmpty(oldPwd)||TextUtils.isEmpty(newPwdOne)||TextUtils.isEmpty(newPwdTwd)){
            Toast.makeText(this,"信息输入不完整，请重新输入！",Toast.LENGTH_SHORT).show();
            return;
        }else if(!newPwdOne.equals(newPwdTwd)){
            Toast.makeText(this,"2次新密码输入不一致，请重新输入",Toast.LENGTH_SHORT).show();
            return;
        }else if(oldPwd.equals(newPwdOne)){
            Toast.makeText(this,"新旧密码完全一样，请重新输入！",Toast.LENGTH_SHORT).show();
            return;
        }
        resetPwd(oldPwd,newPwdOne);
        AbProgressDialogFragment dialogFragment=AbDialogUtil.showProgressDialog(ResetPwdActivity.this, 0,
                "设置中...");
        dialogFragment.setCancelable(true);
    }

    private void resetPwd(final String oldPwd, final String newPwd){

        String nameSpace="http://tempuri.org/";
        String methodName = "ChangePassword";
        AbSoapParams params = new AbSoapParams();
        params.put("name",application.mUser.getUserName());
        params.put("Oldpassword",oldPwd);
        params.put("newPassword",newPwd);
        mAbSoapUtil= AbSoapUtil.getInstance(this);
        mAbSoapUtil.setTimeout(10000);
        mAbSoapUtil.call(AppHttpUtil.BASE_URL,nameSpace,methodName,params, new AbSoapListener() {

            //获取数据成功会调用这里
            @Override
            public void onSuccess(int statusCode, SoapObject object) {

                String LoginResult = object.getPrimitivePropertyAsString("ChangePasswordResult");
                DebugLog.e("statusCode=" + statusCode + "SoapObject=" + LoginResult);
                AbDialogUtil.removeDialog(ResetPwdActivity.this);
                JSONObject jobj=null;
                try {
                    jobj=new JSONObject(LoginResult);
                } catch (JSONException e) {
                    AbToastUtil.showToast(ResetPwdActivity.this, "系统返回异常！");
                    e.printStackTrace();
                }
                String result =jobj.optString("Result");
                if(result.equals("ok")){
                    Toast.makeText(ResetPwdActivity.this,"密码修改成功，稍后请重新登录！",Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessageDelayed(0,600);

                }else {
                    AbToastUtil.showToast(ResetPwdActivity.this, "修改失败！");
                }

            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, String content,
                                  Throwable error) {
                AbToastUtil.showToast(ResetPwdActivity.this, "修改失败！");
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, SoapFault fault) {
                AbToastUtil.showToast(ResetPwdActivity.this, "修改失败！");
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
