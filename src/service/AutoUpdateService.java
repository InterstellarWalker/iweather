package service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import modle.IWeatherDB;

import fragment.MainFragment;
import utils.HttpCallBackListener;
import utils.HttpUtil;
import utils.Url;
import activity.MyApplication;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class AutoUpdateService extends IntentService {

	public AutoUpdateService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	public AutoUpdateService() {
		super("AutoUpdateService");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		int anHour=intent.getIntExtra("updateTime", 0)*60*60*1000;
		AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent in=new Intent(this,UpdateBroadReceiver.class);
		//in.setAction("autoupdate");cCityList.size()
		Log.d("haha","ÎÒ±»¿ªÆô");
		PendingIntent sender=PendingIntent.getBroadcast(this, 01, in, PendingIntent.FLAG_UPDATE_CURRENT);
		//Calendar calendar=Calendar.getInstance();
		long triggerAtMillis=SystemClock.elapsedRealtime()+anHour;
		am.set(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, sender);
		List<String> cCityList=IWeatherDB.getInstance(MyApplication.getContext()).queryCityList();
		for (int i = 0; i < cCityList.size(); i++) {
			HttpUtil.getAqiResponse(cCityList.get(i));
			
		}
	}
	
}
