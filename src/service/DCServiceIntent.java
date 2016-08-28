package service;

import utils.HttpCallBackListener;
import utils.HttpUtil;
import utils.Url;
import activity.MyApplication;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;




public class DCServiceIntent extends IntentService{
	
	private HttpUtil httpUtil;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	public DCServiceIntent() {
		super("DCServiceIntent");
		// TODO Auto-generated constructor stub
	}
	public DCServiceIntent(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		preferences=getSharedPreferences("iweather", MODE_PRIVATE);
		editor=preferences.edit();
		Log.d("开启服务","hah");
		//弹出通知显示进度
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		httpUtil=new HttpUtil();
		httpUtil.sendRequestHttp(Url.cityURL,
				new HttpCallBackListener() {

					@Override
					public void onFinish(String response) {
						// TODO Auto-generated method stub
						httpUtil.parseXMLData(response);
						editor.putBoolean("isDownCity", false);
						editor.commit();
						Log.d("开启服务","正在下载");
					}

					@Override
					public void onError(Exception e) {
						// TODO Auto-generated method stub
					}
				});
	}

	
}
