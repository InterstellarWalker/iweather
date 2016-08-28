package service;

import org.json.JSONObject;

import utils.HttpCallBackListener;
import utils.HttpUtil;
import utils.Url;
import android.app.IntentService;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class HandleDataIntentService extends IntentService {

	private static final String TAG = "handleDataIntentService";
	private MyLocationListener listener;
	LocationManager location;

	public HandleDataIntentService() {
		super("HandleDataIntentService");
	}

	public HandleDataIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
		super.onCreate();
		Log.d(TAG, "onCreate");
		// String info=intent.getStringExtra("info");
		
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		// 获取位置管理器
				location = (LocationManager) getSystemService(LOCATION_SERVICE);
				// 获取提供者标准
				Criteria criteria = new Criteria();
				criteria.setCostAllowed(true);// 是否允许花费
				criteria.setAccuracy(Criteria.ACCURACY_FINE);// 设置精确度
				// 获取最优的位置提供者
				String bestProvider = location.getBestProvider(criteria, true);
				listener = new MyLocationListener();
				location.requestLocationUpdates(bestProvider, 0, 0, listener);
				Log.d(TAG,"info开启定位");
		
		Log.d(TAG, "onStart");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		location.removeUpdates(listener);// 当Activity结束时关闭服务节省电量
		Intent broadcast = new Intent("locateSuccessfull");
		broadcast.putExtra("locateCity","null");
		sendBroadcast(broadcast);
		Log.d(TAG,"定位失败");
	}

	class MyLocationListener implements LocationListener {
		// 位置发生改变进行监听
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.d(TAG, "经度 :" + location.getLongitude());
			Log.d(TAG, "纬度 :" + location.getLatitude());
			Double longitude = location.getLongitude();
			Double latitude = location.getLatitude();
			String address = Url.getLocationURL(longitude, latitude);
			final Intent broadcast = new Intent("locateSuccessfull");
			final HttpUtil httpUtil = new HttpUtil();
			httpUtil.sendRequestHttp(address, new HttpCallBackListener() {

				@Override
				public void onFinish(String response) {
					// TODO Auto-generated method stub
					// httpUtil.parseJSONData(response,null);
					Log.d(TAG, response);
					try {
						JSONObject jsonObject = new JSONObject(response);
						JSONObject showapi_res_body = jsonObject
								.getJSONObject("showapi_res_body");
						JSONObject cityInfo = showapi_res_body
								.getJSONObject("cityInfo");

						String c3 = cityInfo.getString("c3");
						// Intent in=new Intent();
						Log.d(TAG,"定位城市"+c3);
						if (c3 == null) {
							broadcast.putExtra("locateCity", c3);
							sendBroadcast(broadcast);
							
						} else {
							broadcast.putExtra("locateCity", c3);
							sendBroadcast(broadcast);
							//
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}

				@Override
				public void onError(Exception e) {
					// TODO Auto-generated method stub
					broadcast.putExtra("locateCity",null+"");
					sendBroadcast(broadcast);
					Log.d(TAG,"定位失败");
					//Toast.makeText(getApplicationContext(), "定位失败",
						//	Toast.LENGTH_SHORT).show();
				}
			});

		}

		// 用户关闭GPS
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onProviderDisabled");
		}

		// 用户打开GPS
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onProviderEnabled");
		}

		// 位置提供者发生变化
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onStatusChanged");
		}

	}
	
}
