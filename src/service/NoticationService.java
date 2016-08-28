package service;

import java.util.List;

import modle.IWeatherDB;

import com.example.iweather.R;

import fragment.MainFragment;

import activity.MainActivity;
import activity.MyApplication;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.Preference;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class NoticationService extends IntentService {

	private static final String TAG = "NoticationService ";

	public NoticationService() {
		super("NoticationService");
		// TODO Auto-generated constructor stub
	}

	public NoticationService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		super.onCreate();

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		// =new
		// Notification(R.drawable.ic_launcher,"我是天气通知栏",System.currentTimeMillis());
		Intent ntIntent = new Intent(this, MainActivity.class);// 建立一个与该通知互动的一个Activity意图
		PendingIntent pi = PendingIntent.getActivity(this, 0, ntIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);// 响应通知栏的点击事件

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder build = new Notification.Builder(this);
		Log.d(TAG,intent.getStringExtra("currentCity"));
		Log.d(TAG,intent.getStringExtra("currentWeather")+"11");
		Log.d(TAG,intent.getStringExtra("currentRange"));
		Log.d(TAG,intent.getIntExtra("currentIcon", 0)+"");
		build.setContentText(intent.getStringExtra("currentCity"));
		build.setContentTitle(intent.getStringExtra("currentWeather"));
		build.setSubText(intent.getStringExtra("currentRange"));
		build.setSmallIcon(intent.getIntExtra("currentIcon", 0));

		/*
		 * RemoteViews view=new RemoteViews(getPackageName(),
		 * R.layout.service_layout); view.setTextViewText(R.id.text, "我是内容");
		 * view.setImageViewResource(R.id.image, R.drawable.ic_launcher);
		 * view.setOnClickPendingIntent(R.id.btn, pi); build.setContent(view);
		 */// 自定义view无法显示
		Log.d(TAG, "通知栏");
		build.setAutoCancel(false);
		//build.setNumber(100);
		build.setContentIntent(pi);
		build.setOngoing(true);

		Notification notification = build.build();
		startForeground(3, notification);
		nm.notify(2, notification);
		final Handler handler=new Handler();
		Runnable runnable=new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.d(TAG,"哈哈");
				handler.postDelayed(this,10);
			}
		};
		handler.postDelayed(runnable, 3);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		SharedPreferences preferences=getSharedPreferences("iweather", MODE_PRIVATE);
		SharedPreferences.Editor edit=preferences.edit();
		edit.putBoolean("notification", false);
		edit.commit();
	}

}
