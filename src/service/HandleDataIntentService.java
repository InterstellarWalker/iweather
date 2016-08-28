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
		// ��ȡλ�ù�����
				location = (LocationManager) getSystemService(LOCATION_SERVICE);
				// ��ȡ�ṩ�߱�׼
				Criteria criteria = new Criteria();
				criteria.setCostAllowed(true);// �Ƿ�������
				criteria.setAccuracy(Criteria.ACCURACY_FINE);// ���þ�ȷ��
				// ��ȡ���ŵ�λ���ṩ��
				String bestProvider = location.getBestProvider(criteria, true);
				listener = new MyLocationListener();
				location.requestLocationUpdates(bestProvider, 0, 0, listener);
				Log.d(TAG,"info������λ");
		
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
		location.removeUpdates(listener);// ��Activity����ʱ�رշ����ʡ����
		Intent broadcast = new Intent("locateSuccessfull");
		broadcast.putExtra("locateCity","null");
		sendBroadcast(broadcast);
		Log.d(TAG,"��λʧ��");
	}

	class MyLocationListener implements LocationListener {
		// λ�÷����ı���м���
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.d(TAG, "���� :" + location.getLongitude());
			Log.d(TAG, "γ�� :" + location.getLatitude());
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
						Log.d(TAG,"��λ����"+c3);
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
					Log.d(TAG,"��λʧ��");
					//Toast.makeText(getApplicationContext(), "��λʧ��",
						//	Toast.LENGTH_SHORT).show();
				}
			});

		}

		// �û��ر�GPS
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onProviderDisabled");
		}

		// �û���GPS
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onProviderEnabled");
		}

		// λ���ṩ�߷����仯
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onStatusChanged");
		}

	}
	
}
