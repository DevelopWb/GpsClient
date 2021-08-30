package com.mj.gpsclient.global;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.lang.Thread.UncaughtExceptionHandler;

public class CrashHandler implements UncaughtExceptionHandler {

	private Context mContext;
	private static CrashHandler INSTANCE = new CrashHandler();

	private CrashHandler() { }
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	public void init(Context con) {
		mContext = con;
		// 取代系统默认的错误处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable ex) {

		// 有网络直接发送错误信息到服务器. 没有网络保存错误信息到本地数据库
		try {
			new Thread(new Runnable() {
				public void run() {

//					if (CommonTools.checkNetWorkStatus(mContext)) {
//
//						String errMsg = "";
//						StringWriter sw = new StringWriter();
//			            ex.printStackTrace(new PrintWriter(sw, true));
//						if (sw != null && !("").equals(sw.toString())) errMsg = sw.toString().replaceAll("\n", "<br/>");
//
//						ExceptionEntity exEntity = new ExceptionEntity();
//						exEntity.setImei(Utils.getMobileIMEI(mContext));
//						exEntity.setBrand(Utils.getMoblieBrand());
//						exEntity.setModel(Utils.getMoblieModel());
//						exEntity.setAppVersion(Utils.getAppVersion(mContext));
//						exEntity.setSdkVerion(Build.VERSION.RELEASE);
//						exEntity.setSdkIntVersion(Build.VERSION.SDK_INT);
//						exEntity.setNetType(CommonTools.checkNetworkType(mContext));
//						exEntity.setOperator(Utils.getMoblieOperator(mContext));
//						exEntity.setResolution(Utils.getMobileWidth(mContext) + "*" + Utils.getMobileHeight(mContext));
//						exEntity.setExType(String.valueOf(ex.getCause()));
//						exEntity.setExMsg(errMsg);
//						exEntity.setExTime(System.currentTimeMillis());
//
//						StatisticsInterfaces.exception(exEntity);
//						Intent startIntent = new Intent(mContext,MusicPlayService.class);
//						mContext.stopService(startIntent);
//
//					} else {
//					}
				}
			}).start();
		} catch (Exception e) {
//			CommonTools.error("提交错误信息发生错误#####################################################");
			e.printStackTrace();
//			CommonTools.error("########################################################################");
		} finally {
//			CommonTools.error("完整错误信息#####################################################");
			ex.printStackTrace();
//			CommonTools.error("################################################################");
			
			// 使用Toast来显示异常信息
			new Thread() {
				@Override
				public void run() {
					Looper.prepare();
					Toast.makeText(mContext, "出现未知错误，请重新启动应用", Toast.LENGTH_LONG).show();
					Looper.loop();
				}
			}.start();

			// 等待 1.5 秒后退出程序
			try {
				Thread.sleep(1500);
				MyApplication.getInstance().exit();
			} catch (Exception e) { }
		}

		
	}

}
