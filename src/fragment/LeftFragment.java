package fragment;

import java.util.List;
import modle.IWeatherDB;
import utils.QueryCallBackListener;
import com.example.iweather.R;
import com.lidroid.xutils.ViewUtils;
import activity.About;
import activity.Setting;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LeftFragment extends Fragment implements QueryCallBackListener {
	protected static final String TAG = "LeftFragment";
	public Activity mActivity;
	private ClickCityListener listener;
	private LeftOnClickListener leftListener;
	// ������
	FBaseAdapter fAdapter;
	// ��ʼ���໬������
	private DrawerLayout drawerLayout;
	// ����
	private List<String> cCityList;
	private IWeatherDB iWDb;

	// �ؼ�
	ListView fListView;
	private View view;// ��ʾ����ͼ
	public RelativeLayout topBar;
	public TextView barTemRange;// �����¶ȷ�Χ
	public TextView barAirQulity;// ������������
	public TextView barCity;// ��������
	public ImageView barIcon;// ����ͼ��
	// Ԥ���ؼ�
	private TextView alarmTitle;
	private TextView alarmContent;
	private TextView alarmTime;
	private ImageView alarmIcon;
	public String showCity;// չʾ�ĳ���
	private LinearLayout setting;// ����
	private LinearLayout about;// ����
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	// Ԥ��
	private TextView alarm;
	private String alTime;
	private String nowTime;
	private BroadcastReceiver mRBroadcastReceiver = new BroadcastReceiver() {
		// ���ܸ������й㲥��
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals("action.refreshShowCity")) {
				initData(intent.getStringExtra("showCity"));
				// Log.d(TAG,"���ܵ���"+intent.getStringExtra("showCity"));
			}
		}
	};
	private BroadcastReceiver isAlarmBCReceiver = new BroadcastReceiver() {
		// ���ܹرվ���㲥��
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals("action.isAlarm")) {
				if (intent.getBooleanExtra("topBar", true)) {
					topBar.setVisibility(View.VISIBLE);
					Log.d(TAG, "�ɼ�");
				} else {
					Log.d(TAG, "���ɼ�");
					topBar.setVisibility(View.INVISIBLE);
				}
			}
		}
	};

	// ������ӳ����¼��Ļص��ӿ�
	public interface ClickCityListener {
		void returnCity(int position);

		void changeCity(int position);
	}

	@Override
	public void onAttach(Context context) {
		// TODO Auto-generated method stub
		super.onAttach(context);
		try {
			listener = (ClickCityListener) getActivity();
		} catch (Exception e) {
			// TODO: handle exception
			throw new ClassCastException(getActivity().toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	// ������Ƭ
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
		preferences = mActivity.getSharedPreferences("iweather",
				Context.MODE_PRIVATE);
		editor = preferences.edit();
		iWDb = IWeatherDB.getInstance(mActivity);

		// ��ʼ���ؼ�
		if (listener == null) {
			listener = (ClickCityListener) new MainFragment();
		}// ע��չʾ���й㲥
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("action.refreshShowCity");
		mActivity.registerReceiver(mRBroadcastReceiver, intentFilter);

		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction("action.isAlarm");
		mActivity.registerReceiver(isAlarmBCReceiver, intentFilter2);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		cCityList = (List<String>) iWDb.queryCityList();// �����Ѿ�ѡ��ĳ���
		// android.app.FragmentManager manger=mActivity.getFragmentManager();

		initView();// ��ʼ����ͼ
		initData(showCity);// ��ʼ�����ݲ�ƥ����ͼ;
		// ���ü�����
		setting.setOnClickListener((OnClickListener) leftListener);
		about.setOnClickListener((OnClickListener) leftListener);

		fListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				ImageView image = (ImageView) view
						.findViewById(R.id.lv_sliding_delete);
				TextView temperature = (TextView) view
						.findViewWithTag("temperature");
				if (image.getVisibility() == View.VISIBLE) {
					image.setBackgroundColor(Color.WHITE);
					image.setVisibility(View.INVISIBLE);
					temperature.setVisibility(View.VISIBLE);
				} else {
					drawerLayout.closeDrawer(Gravity.LEFT);
					listener.returnCity(position);
				}

			}
		});
 
		//ɾ�����м���
		fListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					final int arg2, long arg3) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				fListView.setClickable(false);
				final String deleteCity = cCityList.get(arg2);
				final ImageView image = (ImageView) view
						.findViewWithTag("delete");
				final TextView temperature = (TextView) view
						.findViewWithTag("temperature");
				image.setBackgroundColor(Color.LTGRAY);
				image.setVisibility(View.VISIBLE);
				temperature.setVisibility(View.INVISIBLE);
				Log.d(TAG, "VIEW" + view.isFocusable());
				image.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (deleteCity.equals("����")) {
							Toast.makeText(mActivity, "������ӭ���´ι���",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(mActivity, "��ɾ��" + deleteCity,
									Toast.LENGTH_SHORT).show();
						}
						cCityList.remove(deleteCity);
						iWDb.db.delete("CitySet", "city_saveName=?",
								new String[] { deleteCity });
						// cCityList = iWDb.queryCityList();
						fAdapter.notifyDataSetChanged();
						listener.changeCity(arg2);
						// fListView.setClickable(false);
						Log.d(TAG, "3333");
					}
				});

				return true;
			}

		});

		drawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDrawerOpened(View arg0) {
				// TODO Auto-generated method stub

				/*
				 * if (alarmTime==) {
				 * 
				 * }
				 */
			}

			@Override
			public void onDrawerClosed(View arg0) {
				// TODO Auto-generated method stub

			}
		});
		return view;
	}

	/*
	 * private void showCity(String city){ //showCity=city; initData(city); }
	 */
	public void initData(String showCity) {
		// TODO Auto-generated method stub
		// ȡֵ

		// �ж��Ƿ��Ѿ�ѡ�������
		if (cCityList.size() > 0) {
			// ȡ��������Ϣ�ļ���
			if (preferences.getBoolean("topBar", false)) {
				topBar.setVisibility(View.VISIBLE);
			} else {
				topBar.setVisibility(View.INVISIBLE);
			}
			// Log.d(TAG,"�洢�ĳ���"+preferences.getString("scCity", null));
			if (preferences.getString("scCity", null) == null) {
				showCity = cCityList.get(0);
			} else {
				showCity = preferences.getString("scCity", null);
			}

		}
		barAirQulity.setText("����������"
				+ preferences.getString(showCity + "_aqi_city_qlty", null));
		barTemRange.setText(preferences.getString(showCity
				+ "__forecast_zero_tmp_min", null)
				+ "��~"
				+ preferences.getString(showCity + "__forecast_zero_tmp_max",
						null) + "��");
		barCity.setText(showCity);
		barIcon.setImageResource(MainFragment.getBigDrawableId(preferences
				.getString(showCity + "_now_cond_txt", null)));
		Log.d(TAG,"showCity"+showCity);
		String alcCity=preferences.getString(showCity + "_aqi_alarm_city", null);
		alTime = preferences.getString(showCity + "_aqi_alarm_issueTime", null);
		// nowTime != null &&;
		nowTime = preferences
				.getString(showCity + "__forecast_zero_date", null);
		Log.d(TAG, "Ԥ��ʱ��" + alTime + "\nԤ��ʱ��"
				+ "nowTime" + nowTime+"\n"+alcCity);
	//	Log.d(TAG, "Ԥ��ʱ���Ƿ����"+alTime.subSequence(0, 10).equals(nowTime));
		if (alTime != null && alTime.subSequence(0, 10).equals(nowTime)) {
			alarmIcon.setVisibility(View.VISIBLE);
			alarmTitle.setVisibility(View.VISIBLE);
			alarmTime.setVisibility(View.VISIBLE);
			editor.putBoolean("alarmIcon", true);
			/*editor.putBoolean("alarmTitle", true);
			editor.putBoolean("alarmTime", true);*/

			alarmContent.setText(preferences.getString(showCity
					+ "_aqi_alarm_issueContent", null));
			alarmTitle.setText(preferences.getString(showCity
					+ "_aqi_alarm_signalLevel", null)+"-"+preferences.getString(showCity
							+ "_aqi_alarm_signalType",null));
			alarmTime.setText(preferences.getString(showCity
					+ "_aqi_alarm_issueTime", null).subSequence(5, 11));

		}

	}

	public void updateTopBar() {

	}

	private View initView() {
		// TODO Auto-generated method stub

		view = View.inflate(mActivity, R.layout.layout_sliding, null);
		topBar = (RelativeLayout) view.findViewById(R.id.layout_topbar);
		leftListener = new LeftOnClickListener();
		// ��ʼ���ؼ�
		fListView = (ListView) view.findViewById(R.id.lv_layout_sliding_one);

		setting = (LinearLayout) view.findViewById(R.id.setting);
		about = (LinearLayout) view.findViewById(R.id.about);
		fAdapter = new FBaseAdapter();
		fListView.setAdapter(fAdapter);
		drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer);
		barTemRange = (TextView) view.findViewById(R.id.temrange);
		barAirQulity = (TextView) view.findViewById(R.id.air_quality);
		barCity = (TextView) view.findViewById(R.id.currentCity_name);
		barIcon = (ImageView) view.findViewById(R.id.icon_weather);
		alarmContent = (TextView) view.findViewById(R.id.alarm_content);
		alarmIcon = (ImageView) view.findViewById(R.id.alarm_icon);
		alarmTitle = (TextView) view.findViewById(R.id.alarm_title);
		alarmTime = (TextView) view.findViewById(R.id.alarm_time);
		// alarmContent=(TextView) view.findViewById(R.id.alarm_content);
		if (preferences.getBoolean("alarmIcon", false)) {
			alarmIcon.setVisibility(View.VISIBLE);
			alarmTitle.setVisibility(View.VISIBLE);
			alarmTime.setVisibility(View.VISIBLE);
		}

		return view;
	}

	public void initTopBar() {

	}

	// ����viewHolder
	class ViewHolder {
		public TextView temperature;
		public TextView weather;
		public TextView city;
		public ImageView deleteImage;
	}

	// �����ڲ���FBaseAdapter
	class FBaseAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cCityList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			View view = View.inflate(mActivity, R.layout.lv_sliding, null);
			TextView city = (TextView) view.findViewById(R.id.lv_sliding_city);
			TextView weather = (TextView) view
					.findViewById(R.id.lv_sliding_weather);
			TextView temperature = (TextView) view
					.findViewById(R.id.lv_sliding_temperature);

			ImageView deleteImage = (ImageView) view
					.findViewById(R.id.lv_sliding_delete);
			// ȡֵ
			String turnCity = cCityList.get(position);

			// ������ǩ
			temperature.setTag("temperature");
			deleteImage.setTag("delete");
			// ��ֵ
			city.setText(turnCity);

			weather.setText(preferences.getString(turnCity + "_now_cond_txt",
					null));
			temperature.setText(preferences.getString(turnCity + "_now_tmp",
					null) + "��");
			return view;
		}
	}

	@Override
	public void notifyAdapter(String saveCity) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyAdapter������");
	}

	class LeftOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.about:
				startActivity(new Intent(mActivity, About.class));
				break;
			case R.id.setting:
				Intent in = new Intent(mActivity, Setting.class);
				in.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivity(in);
				break;

			default:
				break;
			}
		}

	}

	public void addCityNum() {
		cCityList = iWDb.queryCityList();
		fAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mRBroadcastReceiver != null) {
			mActivity.unregisterReceiver(mRBroadcastReceiver);
		}
		if (isAlarmBCReceiver != null) {
			mActivity.unregisterReceiver(isAlarmBCReceiver);
		}
		super.onDestroy();
	}
}
