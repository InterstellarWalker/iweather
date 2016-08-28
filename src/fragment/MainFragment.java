package fragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.AutoUpdateService;
import service.UpdateBroadReceiver;
import utils.DensityUtils;
import utils.HttpUtil;
import utils.Url;
import modle.IWeatherDB;
import com.example.iweather.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import activity.ChooseCity;
import activity.MyApplication;
import activity.Setting.ShowCityListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author quhuainan
 * 
 */
public class MainFragment extends Fragment {
	public Activity mActivity;
	public LeftFragment leftFragment;
	public ViewPager viewPager;
	private List<View> vpList;
	private DrawerLayout drawerLayout;// ���������
	protected static final int UPTEXT_DATA = 10;
	protected static final String TAG = "MainFragment";

	public IWeatherDB iWDb;// ���ݿ�ʵ��
	protected static final int REQUEST_CODE = 101;// ������
	public MyPagerAdapter vpAdapter;
	protected HttpUtil httpUtil;
	public ImageButton btSliding; // Log.d(TAG, "��ʼ��ҳ��ViewPager����");

	public PullToRefreshScrollView scrollView;
	private Long lastLoadTime;
	private boolean isRefresh;// �жϴ������ȡ����
	private int mPointWidth;// Բ��֮��ļ�ࣻ
	private View viewBlackPoint;// С�ڵ�
	private LinearLayout lLayout;
	private List<String> beforeCityList;
	private View view;
	private String currentCity;// ��ǰλ�õĳ�������
	private CityChangedListener listener;// ���иı����
	private TextView publishTime;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private int flag = 0;

	// index ��Ϣ
	public String[] weeks = { "��", "һ", "��", "��", "��", "��", "��" };

	public TextView cityTitleName;
	private ShowCityListener scListener;

	public interface CityChangedListener {
		void notifyAdapter(String changedCity);
	}

	@Override
	public void onAttach(Context context) {
		// TODO Auto-generated method stub
		super.onAttach(context);
		try {
			listener = (CityChangedListener) getActivity();
			scListener = (ShowCityListener) getActivity();

		} catch (Exception e) {
			// TODO: handle exception
			throw new ClassCastException(getActivity().toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
		httpUtil = new HttpUtil();
		preferences = mActivity.getSharedPreferences("iweather",
				Context.MODE_PRIVATE);
		iWDb = IWeatherDB.getInstance(mActivity);
		vpList = new ArrayList<View>();
		vpAdapter = new MyPagerAdapter();

	}

	@SuppressLint({ "CommitPrefEdits", "NewApi" })
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// ��ѡ����н�����ҳ��
		beforeCityList = (List<String>) iWDb.queryCityList();// �����Ѿ�ѡ��ĳ���
		if (beforeCityList.size() == 0) {
			cityNumNull(0);
		} else {
			initView();
		}
		return view;
	}

	@SuppressWarnings("deprecation")
	private View initView() {// ��ʼ����ͼ

		// TODO Auto-generated method stub
		view = View.inflate(mActivity, R.layout.main_fragment, null);
		publishTime=(TextView) view.findViewById(R.id.weather_publish_time);
		
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer);
		lLayout = (LinearLayout) view.findViewById(R.id.fg_linearLayout);
		viewBlackPoint = view.findViewById(R.id.the_black_point);
		viewPager.setOnPageChangeListener(new WeatherPagerChanged());
		// ��ʼ��СԲ��
		for (int i = 0; i < beforeCityList.size(); i++) {
			vpList.add(View.inflate(mActivity, R.layout.layout_weather_detail,
					null));// װ����ͼ����
			addIndicatePoint(i);// ����ָʾ��
		}
		// ����СԲ��֮����
		getPointDis(beforeCityList.size());
		viewPager.setAdapter(vpAdapter);
		return view;
	}

	private void getPointDis(final int num) {
		// TODO Auto-generated method stub
		lLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					public void onGlobalLayout() {
						// TODO Auto-generated method stub
						lLayout.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
						if (num > 1) {// ����������Բ��ʱ�׵�Ż��м��
							mPointWidth = lLayout.getChildAt(1).getLeft()
									- lLayout.getChildAt(0).getLeft();
						} else {
							mPointWidth = 0;
						}
					}
				});
	}

	public class MyPagerAdapter extends PagerAdapter {
		private ImageButton addNew;

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return POSITION_NONE;
			// return super.getItemPosition(object);
		}

		@Override
		public int getCount() {
			// Log.d(TAG,"ҳǩ����"+vpList.size());

			return vpList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {

			container.removeView(vpList.get(position));

			Log.d(TAG, "�Ƴ�" + position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {

			container.addView(vpList.get(position));// ����Ӧλ�õ�view��ӽ�viewpager��

			// ��ʼ������
			cityTitleName = (TextView) container
					.findViewById(R.id.tx_main_title);
			// cityTitleName.setTag("title");
			Log.d(TAG,"scrollView�³�ʼ��");
			scrollView = (PullToRefreshScrollView) view
					.findViewById(R.id.layout_weathwer_detail_scrollview);
			// ÿ�λ���ʱ�����notifyDataSetChanged,ʹcityTitleName.getText()Ϊ������ÿ�ζ��Ử����������첽����

			// �����ⱻ��ֵ�ı��Ժ�ÿ�λ����Ͳ�������첽������
			if (TextUtils.isEmpty(cityTitleName.getText())) {
				currentCity = beforeCityList.get(position);// ��ʼ����Ӧ�İ�ť,����֤��ǰ����Ϊ��ǰҳ����У�����ᱻviewpagerԤ���ػ��Ƹ���
				initScrollView(currentCity, scrollView);// �������µ�scrollview����
				initTextView(container);// ��ʼ���ı��ؼ�
				cityTitleName.setText(currentCity);
				// �ж��Ƿ���Ҫ�������ȡ���ݣ����Ǵ�sharepreferences�л�ȡ����
				//publishTime.setText(preferences.getString(currentCity + "_basic_update_loc",null));//����ʱ��
				setTextValues(currentCity);
				if (isFromDB(currentCity)) {
					Log.d(TAG, 2 + "������������");
					new UpdateUIAsyncTask().execute(currentCity);
					// scrollView.onRefreshComplete();
					// ���ˢ�¼������10���Ӿ����·�������ݿ���ȡ������(���������ҲӦ�ô����ݿ��м�������);
				} 
				
				Log.d(TAG, "��ǰ����1" + currentCity);
			}

			// ��ʼ����ť
			btSliding = (ImageButton) container.findViewById(R.id.bt_sliding);
			addNew = (ImageButton) container.findViewById(R.id.bt_addCity);
			btSliding.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					 drawerLayout.openDrawer(Gravity.LEFT);
					// lLayout.removeViewAt(1);
					//scrollView.onRefreshComplete();
					//
				}
			});
			addNew.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (beforeCityList.size() >= 8) {
						addNew.setEnabled(false);
						Toast.makeText(mActivity, "��ӳ��й��࣬����ɾ�����к�����ӣ������������������", Toast.LENGTH_SHORT).show();
					} else {
						startActivityForResult(new Intent(mActivity,
								ChooseCity.class), REQUEST_CODE);
					}
				}
			});
			return vpList.get(position);

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
			beforeCityList = iWDb.queryCityList();
			addCityNum();
			
			viewPager.setCurrentItem(beforeCityList.size() - 1);
		} else if (resultCode == 110 && requestCode == REQUEST_CODE) {// �������������city���ڵ�ҳ��
			// Log.d(TAG, "ѡ��ĳ���" + saveCity);
			viewPager.setCurrentItem(beforeCityList.indexOf(data
					.getStringExtra("saveCity")));
		}
	}

	// ���ӳ�������
	public void addCityNum() {
		int cityNum = beforeCityList.size();
		vpList.add(View
				.inflate(mActivity, R.layout.layout_weather_detail, null));// ���������ӵĳ���
		addIndicatePoint(cityNum);// ����ָʾ��
		getPointDis(cityNum);// �����ʱС�ҵ�ľ���
		vpAdapter.notifyDataSetChanged();

		countPoitnWidth(0, cityNum);
		// Log.d(TAG, "��Ӧѡ����к��������=" + vpList.size());

	}

	// ���ٳ�������
	public void reduceCityNum(int position) {
		// beforeCityList =iWDb.queryCityList();//��ȡ���³����б�
		// ����ʹ��set�����ж��Ƿ��ظ���list���ϱ���

		// viewPager.removeViewAt(0);
		iWDb.db.delete("CitySet", "_id=?", new String[] { position + "" });
		beforeCityList = iWDb.queryCityList();
		lLayout.removeViewAt(beforeCityList.size());

		// lLayout.removeAllViews();
		// reduceIndicatePoint(beforeCityList.size());
		viewPager.setAdapter(null);// viewPagerû�м���ҳǩ�ķ���ֻ�ܽ�viewPagerȫ�����ٵ�����������adapter
		vpList.remove(position);
		viewPager.setAdapter(vpAdapter);
		vpAdapter.notifyDataSetChanged();
		if (beforeCityList.size() == 0) {
			startActivityForResult(new Intent(mActivity, ChooseCity.class),
					REQUEST_CODE);
		}

	}

	// �жϳ����Ƿ�Ϊ��
	public View cityNumNull(int cityNum) {
		// �ж��Ƿ񱣴������
		startActivity(new Intent(getActivity(), ChooseCity.class));
		Log.d(TAG, "��ǰû�г���");
		startActivity(new Intent(mActivity, ChooseCity.class));
		mActivity.finish();
		return null;

	}

	// ����ָʾ��
	private void addIndicatePoint(int i) {
		View point = new View(mActivity);
		point.setBackgroundResource(R.drawable.the_gray_point);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				DensityUtils.dp2px(mActivity, 5), DensityUtils.dp2px(mActivity,
						5));
		if (i > 0) {
			params.leftMargin = DensityUtils.dp2px(mActivity, 8);// ����ÿ��СԲ��֮��ļ��
		}
		// params.setLayoutDirection(layoutDirection);
		point.setLayoutParams(params);
		lLayout.addView(point);
	}

	

	// ����ڵ���ƶ�����
	private void countPoitnWidth(float positionOffset, int position) {
		int len = (int) (mPointWidth * positionOffset) + position * mPointWidth;
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewBlackPoint
				.getLayoutParams();
		params.leftMargin = len;
		viewBlackPoint.setLayoutParams(params);
	}

	class WeatherPagerChanged implements OnPageChangeListener {
		@Override
		// ��������״̬�ı仯
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			Log.d(TAG, state + "onPage");
		}

		@Override
		// ������������
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// TODO Auto-generated method stub
			countPoitnWidth(positionOffset, position);
		}

		@Override
		// ������һҳ��ѡ��
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			// ��һʱ�䱻���ã��ڻ�ͼ֮ǰ��
			// Log.d(TAG, "ѡ�е�λ��=" + position);

			if (position > 0) {
				vpAdapter.notifyDataSetChanged();
			}
			

		}
	}

	// �첽��������
	 class UpdateUIAsyncTask extends AsyncTask<String, Void, String> {
		// private ViewHolder viewHolder;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(final String... params) {
			// TODO Auto-generated method stub
			// params �Ǵ���������������
			HttpUtil.getAqiResponse(params[0]);
			HttpUtil.getWeatherResponse(params[0]);
			return params[0];

		}

		@Override
		protected void onPostExecute(String parseCity) {
			// TODO Auto-generated method stub
			super.onPostExecute(parseCity);
			// initScrollView(parseCity);
			//scrollView.onRefreshComplete();
			Log.d(TAG, "ˢ����ɺ����onRefreshComplete()");
			Boolean isHasData = preferences.getBoolean(
					parseCity + "_isHasData", false);

			if (isHasData) {
				vpAdapter.notifyDataSetChanged();
				scrollView.onRefreshComplete();
				setTextValues(parseCity);
				Toast.makeText(mActivity, "ˢ�³ɹ�", Toast.LENGTH_SHORT).show();

			} else {
				vpAdapter.notifyDataSetChanged();
				scrollView.onRefreshComplete();
				
				if (iWDb.queryCityNameInfo(parseCity)
						.get("city_code")==null) {
					Toast.makeText(mActivity, "�������ظó�����Ϣ���Ժ���", Toast.LENGTH_SHORT).show();
					
				}else{
				Toast.makeText(mActivity, "������", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	private void initScrollView(final String currentCity,
			PullToRefreshScrollView scrollView) {
		// TODO Auto-generated method stub
		
		ILoadingLayout startLabels = scrollView.getLoadingLayoutProxy();
		startLabels.setPullLabel("����ˢ��");
		startLabels.setRefreshingLabel("����ˢ��");
		startLabels.setReleaseLabel("�ɿ�ˢ��");

		scrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				String label = DateUtils.formatDateTime(
						MyApplication.getContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				Log.d(TAG, "1111������������");

				new UpdateUIAsyncTask().execute(currentCity);
			}
		});
		// scrollView.onRefreshComplete();
	}

	private Boolean isFromDB(String parseCity) {
		// �ж�Ӧ�ô����￪ʼ����
		editor = preferences.edit();
		lastLoadTime = preferences.getLong(parseCity + "_refresh_time", 0L);
		isRefresh = ((System.currentTimeMillis() - lastLoadTime) >= 1000 * 60 * 10);
		Log.d(TAG, "isRefresh" + isRefresh + "\nlastLoadTime" + lastLoadTime
				+ "\nSystem.currentTimeMillis() " + System.currentTimeMillis());
		editor.putLong(parseCity + "_refresh_time", System.currentTimeMillis());
		editor.commit();
		return isRefresh;

	}

	public void initTextView(ViewGroup container) {
		// TODO Auto-generated method stub

		// ע��now����
		TextView nowAirQuality = (TextView) viewPager
				.findViewById(R.id.now_air_quality);
		TextView nowAirPM = (TextView) viewPager
				.findViewById(R.id.now_air_qulity_pm);
		TextView nowCond = (TextView) viewPager
				.findViewById(R.id.now_weather_cond);

		TextView nowTemRange = (TextView) viewPager
				.findViewById(R.id.now_weather_range);
		TextView nowTem = (TextView) viewPager
				.findViewById(R.id.now_temperature);
		// now�������
		nowAirQuality.setTag("nowAirQuality");
		nowAirPM.setTag("nowAirPM");
		nowCond.setTag("nowCond");
		nowTemRange.setTag(" nowTemRange");
		nowTem.setTag("nowTem");
		// sugesstion����
		TextView clIndex_title = (TextView) viewPager
				.findViewById(R.id.title_detail_clothes_index);
		TextView clIndex_txt = (TextView) viewPager
				.findViewById(R.id.text_detail_clothes_index);

		TextView clodIndex_title = (TextView) viewPager
				.findViewById(R.id.title_detail_clod_index);
		TextView clodIndex_txt = (TextView) viewPager
				.findViewById(R.id.text_detail_cold_index);

		TextView rayIndex_txt = (TextView) viewPager
				.findViewById(R.id.text_detail_rays_index);
		TextView rayIndex_title = (TextView) viewPager
				.findViewById(R.id.title_detail_rays_index);

		TextView spIndex_title = (TextView) viewPager
				.findViewById(R.id.title_detail_sports_index);
		TextView spIndex_txt = (TextView) viewPager
				.findViewById(R.id.text_detail_sports_index);

		TextView clcIndex_title = (TextView) viewPager
				.findViewById(R.id.title_detail_cleancar_index);
		TextView clcIndex_txt = (TextView) viewPager
				.findViewById(R.id.text_detail_cleancar_index);

		clIndex_title.setTag("clIndex_title");
		clIndex_txt.setTag("clIndex_txt");
		clodIndex_title.setTag("clodIndex_title");
		clodIndex_txt.setTag("clodIndex_txt");
		rayIndex_title.setTag("rayIndex_title");
		rayIndex_txt.setTag("rayIndex_txt");
		spIndex_title.setTag("spIndex_title");
		spIndex_txt.setTag("spIndex_txt");
		clcIndex_title.setTag("clcIndex_title");
		clcIndex_txt.setTag("clcIndex_txt");
		// ����Ԥ���ӽ���
		TextView tdWeather = (TextView) viewPager
				.findViewById(R.id.detail_weather_text_forecast);
		TextView tdTgTem = (TextView) viewPager
				.findViewById(R.id.detail_weather_text_tem);
		TextView tdHum = (TextView) viewPager
				.findViewById(R.id.detail_weather_text_hum);
		TextView tdWind = (TextView) viewPager
				.findViewById(R.id.detail_weather_text_wind);
		tdWeather.setTag("tdWeather");
		tdTgTem.setTag("tdTgTem");
		tdHum.setTag("tdHum");
		tdWind.setTag("tdWind");
		// δ������Ԥ��

		// Ԥ���һ��
		TextView fir_week = (TextView) viewPager
				.findViewById(R.id.forecast_first_day_week);
		TextView fir_date = (TextView) viewPager
				.findViewById(R.id.forecast_first_day_date);
		TextView fir_weather = (TextView) viewPager
				.findViewById(R.id.forecast_first_day_weather);
		TextView fir_tem = (TextView) viewPager
				.findViewById(R.id.forecast_first_day_temperature);
		TextView fir_wind = (TextView) viewPager
				.findViewById(R.id.forecast_first_day_wind);
		TextView fir_pop = (TextView) viewPager
				.findViewById(R.id.forecast_first_day_humidity);
		TextView fir_vis = (TextView) viewPager
				.findViewById(R.id.forecast_first_day_vis);
		fir_week.setTag("fir_week");
		fir_weather.setTag("fir_weather");
		fir_date.setTag("fir_date");
		fir_tem.setTag("fir_tem");
		fir_wind.setTag("fir_wind");
		fir_pop.setTag("fir_pop");
		fir_vis.setTag("fir_vis");
		// Ԥ��ڶ���
		TextView sec_week = (TextView) viewPager
				.findViewById(R.id.forecast_second_day_week);
		TextView sec_date = (TextView) viewPager
				.findViewById(R.id.forecast_second_day_date);
		TextView sec_weather = (TextView) viewPager
				.findViewById(R.id.forecast_second_day_weather);
		TextView sec_tem = (TextView) viewPager
				.findViewById(R.id.forecast_second_day_temperature);
		TextView sec_wind = (TextView) viewPager
				.findViewById(R.id.forecast_second_day_wind);
		TextView sec_pop = (TextView) viewPager
				.findViewById(R.id.forecast_second_day_humidity);
		TextView sec_vis = (TextView) viewPager
				.findViewById(R.id.forecast_second_day_vis);
		sec_week.setTag("sec_week");
		sec_weather.setTag("sec_weather");
		sec_date.setTag("sec_date");
		sec_tem.setTag("sec_tem");
		sec_wind.setTag("sec_wind");
		sec_pop.setTag("sec_pop");
		sec_vis.setTag("sec_vis");
		// Ԥ�������
		TextView thir_week = (TextView) viewPager
				.findViewById(R.id.forecast_third_day_week);
		TextView thir_date = (TextView) viewPager
				.findViewById(R.id.forecast_third_day_date);
		TextView thir_weather = (TextView) viewPager
				.findViewById(R.id.forecast_third_day_weather);
		TextView thir_tem = (TextView) viewPager
				.findViewById(R.id.forecast_third_day_temperature);
		TextView thir_wind = (TextView) viewPager
				.findViewById(R.id.forecast_third_day_wind);
		TextView thir_pop = (TextView) viewPager
				.findViewById(R.id.forecast_third_day_humidity);
		TextView thir_vis = (TextView) viewPager
				.findViewById(R.id.forecast_third_day_vis);
		thir_week.setTag("thir_week");
		thir_weather.setTag("thir_weather");
		thir_date.setTag("thir_date");
		thir_tem.setTag("thir_tem");
		thir_wind.setTag("thir_wind");
		thir_pop.setTag("thir_pop");
		thir_vis.setTag("thir_vis");
		// ��ʼ��ͼƬ
		ImageView f1Icon = (ImageView) viewPager
				.findViewById(R.id.forecast_first_day_icon);
		f1Icon.setTag("f1Icon");
		ImageView f2Icon = (ImageView) viewPager
				.findViewById(R.id.forecast_second_day_icon);
		f2Icon.setTag("f2Icon");
		ImageView f3Icon = (ImageView) viewPager
				.findViewById(R.id.forecast_third_day_icon);
		f3Icon.setTag("f3Icon");
		ImageView now_detail_icon = (ImageView) viewPager
				.findViewById(R.id.detail_weather_icon);
		now_detail_icon.setTag("detail_weather_icon");
	}

	private void setTextValues(String parseCity) {
		String status = preferences.getString(parseCity + "_status", null);
		if (status == null) {
			Log.d(TAG, 3 + "������������");
			new UpdateUIAsyncTask().execute(currentCity);
		} else {
			TextView nowAirQuality = (TextView) viewPager
					.findViewWithTag("nowAirQuality");
			TextView nowAirPM = (TextView) viewPager
					.findViewWithTag("nowAirPM");
			TextView nowTemRange = (TextView) viewPager
					.findViewWithTag(" nowTemRange");
			TextView nowCond = (TextView) viewPager.findViewWithTag("nowCond");
			TextView nowTem = (TextView) viewPager.findViewWithTag("nowTem");

			nowAirQuality
					.setText("����������"
							+ preferences.getString(parseCity
									+ "_aqi_city_qlty", null));
			nowAirPM.setText("PM2.5: "
					+ preferences.getString(parseCity + "_aqi_city_pm25", null));

			nowTem.setText(preferences.getString(parseCity + "_now_tmp", null)
					+ "��");
			nowCond.setText(preferences.getString(parseCity + "_now_cond_txt",
					null));
			// Log.d(TAG,preferences.getString(parseCity+"_now_cond_txt",null));
			nowTemRange.setText(preferences.getString(parseCity
					+ "__forecast_zero_tmp_min", null)
					+ "��~"
					+ preferences.getString(parseCity
							+ "__forecast_zero_tmp_max", null) + "��");
			// suggestion����
			TextView clIndex_title = (TextView) viewPager
					.findViewWithTag("clIndex_title");
			TextView clIndex_txt = (TextView) viewPager
					.findViewWithTag("clIndex_txt");

			TextView clodIndex_title = (TextView) viewPager
					.findViewWithTag("clodIndex_title");
			TextView clodIndex_txt = (TextView) viewPager
					.findViewWithTag("clodIndex_txt");

			TextView rayIndex_txt = (TextView) viewPager
					.findViewWithTag("rayIndex_txt");
			TextView rayIndex_title = (TextView) viewPager
					.findViewWithTag("rayIndex_title");

			TextView spIndex_title = (TextView) viewPager
					.findViewWithTag("spIndex_title");
			TextView spIndex_txt = (TextView) viewPager
					.findViewWithTag("spIndex_txt");

			TextView clcIndex_title = (TextView) viewPager
					.findViewWithTag("clcIndex_title");
			TextView clcIndex_txt = (TextView) viewPager
					.findViewWithTag("clcIndex_txt");

			clIndex_title.setText(preferences.getString(parseCity
					+ "_suggestion_drsg_brf", null));
			clIndex_txt.setText(preferences.getString(parseCity
					+ "_suggestion_drsg_txt", null));
			clodIndex_title.setText(preferences.getString(parseCity
					+ "_suggestion_flu_brf", null));
			clodIndex_txt.setText(preferences.getString(parseCity
					+ "_suggestion_flu_txt", null));
			rayIndex_title.setText(preferences.getString(parseCity
					+ "_suggestion_uv_brf", null));
			rayIndex_txt.setText(preferences.getString(parseCity
					+ "_suggestion_uv_txt", null));
			spIndex_title.setText(preferences.getString(parseCity
					+ "_suggestion_sport_brf", null));
			spIndex_txt.setText(preferences.getString(parseCity
					+ "_suggestion_sport_txt", null));
			clcIndex_title.setText(preferences.getString(parseCity
					+ "_suggestion_cw_brf", null));
			clcIndex_txt.setText(preferences.getString(parseCity
					+ "_suggestion_cw_txt", null));
			// ����Ԥ��
			TextView tdWeather = (TextView) viewPager
					.findViewWithTag("tdWeather");
			TextView tdTgTem = (TextView) viewPager.findViewWithTag("tdTgTem");
			TextView tdHum = (TextView) viewPager.findViewWithTag("tdHum");
			TextView tdWind = (TextView) viewPager.findViewWithTag("tdWind");
			tdWeather.setText(preferences.getString(
					parseCity + "_now_cond_txt", null));
			tdTgTem.setText(preferences.getString(parseCity + "_now_fl", null)
					+ "��");
			tdHum.setText(preferences.getString(parseCity
					+ "__forecast_zero_hum", null)
					+ "%");
			tdWind.setText(preferences.getString(parseCity
					+ "__forecast_zero_wind_dir", null)
					+ "/"
					+ preferences.getString(parseCity
							+ "__forecast_zero_wind_sc", null));
			// Ԥ���һ��
			TextView fir_week = (TextView) viewPager
					.findViewWithTag("fir_week");
			TextView fir_date = (TextView) viewPager
					.findViewWithTag("fir_date");
			TextView fir_weather = (TextView) viewPager
					.findViewWithTag("fir_weather");
			TextView fir_tem = (TextView) viewPager.findViewWithTag("fir_tem");
			TextView fir_wind = (TextView) viewPager
					.findViewWithTag("fir_wind");
			TextView fir_pop = (TextView) viewPager.findViewWithTag("fir_pop");
			TextView fir_vis = (TextView) viewPager.findViewWithTag("fir_vis");
			fir_week.setText("����"
					+ weeks[(getWeek(System.currentTimeMillis()) + 1) % 7]);
			fir_weather.setText(preferences.getString(parseCity
					+ "__forecast_first_cond_txtd", null));
			fir_date.setText(getDate(preferences.getString(parseCity
					+ "__forecast_first_date", null)));
			fir_tem.setText(preferences.getString(parseCity
					+ "__forecast_first_tmp_min", null)
					+ "��~"
					+ preferences.getString(parseCity
							+ "__forecast_first_tmp_max", null) + "��");
			fir_wind.setText(preferences.getString(parseCity
					+ "__forecast_first_wind_sc", null)
					+ "��");
			fir_pop.setText(preferences.getString(parseCity
					+ "__forecast_first_pop", null)
					+ "%");
			fir_vis.setText(preferences.getString(parseCity
					+ "__forecast_first_vis", null)
					+ "km");
			// Ԥ��ڶ���
			TextView sec_week = (TextView) viewPager
					.findViewWithTag("sec_week");
			TextView sec_date = (TextView) viewPager
					.findViewWithTag("sec_date");
			TextView sec_weather = (TextView) viewPager
					.findViewWithTag("sec_weather");
			TextView sec_tem = (TextView) viewPager.findViewWithTag("sec_tem");
			TextView sec_wind = (TextView) viewPager
					.findViewWithTag("sec_wind");
			TextView sec_pop = (TextView) viewPager.findViewWithTag("sec_pop");
			TextView sec_vis = (TextView) viewPager.findViewWithTag("sec_vis");
			sec_week.setText("����"
					+ weeks[(getWeek(System.currentTimeMillis()) + 2) % 7]);
			sec_weather.setText(preferences.getString(parseCity
					+ "__forecast_second_cond_txtd", null));
			sec_date.setText(getDate(preferences.getString(parseCity
					+ "__forecast_second_date", null)));
			sec_tem.setText(preferences.getString(parseCity
					+ "__forecast_second_tmp_min", null)
					+ "��~"
					+ preferences.getString(parseCity
							+ "__forecast_second_tmp_max", null) + "��");
			sec_wind.setText(preferences.getString(parseCity
					+ "__forecast_second_wind_sc", null)
					+ "��");
			sec_pop.setText(preferences.getString(parseCity
					+ "__forecast_second_pop", null)
					+ "%");
			sec_vis.setText(preferences.getString(parseCity
					+ "__forecast_second_vis", null)
					+ "km");
			// Ԥ�������
			TextView thir_week = (TextView) viewPager
					.findViewWithTag("thir_week");
			TextView thir_date = (TextView) viewPager
					.findViewWithTag("thir_date");
			TextView thir_weather = (TextView) viewPager
					.findViewWithTag("thir_weather");
			TextView thir_tem = (TextView) viewPager
					.findViewWithTag("thir_tem");
			TextView thir_wind = (TextView) viewPager
					.findViewWithTag("thir_wind");
			TextView thir_pop = (TextView) viewPager
					.findViewWithTag("thir_pop");
			TextView thir_vis = (TextView) viewPager
					.findViewWithTag("thir_vis");
			thir_week.setText("����"
					+ weeks[(getWeek(System.currentTimeMillis()) + 3) % 7]);
			thir_weather.setText(preferences.getString(parseCity
					+ "__forecast_third_cond_txtd", null));
			thir_date.setText(getDate(preferences.getString(parseCity
					+ "__forecast_third_date", null)));
			thir_tem.setText(preferences.getString(parseCity
					+ "__forecast_third_tmp_min", null)
					+ "��~"
					+ preferences.getString(parseCity
							+ "__forecast_third_tmp_max", null) + "��");
			thir_wind.setText(preferences.getString(parseCity
					+ "__forecast_third_wind_sc", null)
					+ "��");
			thir_pop.setText(preferences.getString(parseCity
					+ "__forecast_third_pop", null)
					+ "%");
			thir_vis.setText(preferences.getString(parseCity
					+ "__forecast_third_vis", null)
					+ "km");

		}
		// ��ʼ��ͼƬ
		ImageView f1Icon = (ImageView) viewPager.findViewWithTag("f1Icon");
		//
		// f1Icon.setImageResource(R.drawable.fifteen);
		f1Icon.setImageResource(getDrawableID(preferences.getString(parseCity
				+ "__forecast_first_cond_txtd", null)));

		ImageView f2Icon = (ImageView) viewPager.findViewWithTag("f2Icon");
		f2Icon.setBackgroundResource(getDrawableID(preferences.getString(
				parseCity + "__forecast_second_cond_txtd", null)));
		ImageView f3Icon = (ImageView) viewPager.findViewWithTag("f3Icon");
		f3Icon.setBackgroundResource(getDrawableID(preferences.getString(
				parseCity + "__forecast_third_cond_txtd", null)));
		ImageView now_detail_icon = (ImageView) viewPager
				.findViewWithTag("detail_weather_icon");
		//����ʱ��
		String time=(String) preferences.getString(parseCity + "_basic_update_loc",null);
		if (time!=null) {
			
			publishTime.setText("������"+(time.subSequence(11, 16)));//����ʱ��
		}
		
		Log.d(TAG,preferences.getString(parseCity + "_basic_update_loc",null)+"����ʱ��");
		/*
		 * Log.d(TAG,now_detail_icon+"11111"); Log.d(TAG,"��������ֵ"+);
		 * Log.d(TAG,+"2222");
		 */
		now_detail_icon.setBackgroundResource(getBigDrawableId(preferences
				.getString(parseCity + "__forecast_zero_cond_txtd", null)));
		
		listener.notifyAdapter(parseCity);//����ҳ�����ݷ����仯ʱ֪ͨ���������ҲҪ�ı�
		if (preferences.getString("scCity",beforeCityList.get(0)).equals(parseCity)) {
			scListener.showCityChange(parseCity);//Ҳ֪ͨ��ʾ���иı�
			
		}
		
	}

	public int getWeek(long time) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(time));
		int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (week_index < 0) {
			week_index = 0;
		}

		return week_index;
	}

	private String getDate(String date) {
		return (String) date.replaceAll("-", ".").subSequence(5, 10);
	}

	public static int getDrawableID(String cond) {
		int drawalbe[] = { R.drawable.one, R.drawable.two, R.drawable.two,
				R.drawable.three, R.drawable.four, R.drawable.five,
				R.drawable.six, R.drawable.seven, R.drawable.eight,
				R.drawable.nine, R.drawable.ten, R.drawable.elevn,
				R.drawable.twelev, R.drawable.thirteen, R.drawable.fourteen,
				R.drawable.fourteen, R.drawable.fifteen, R.drawable.sixteen,
				R.drawable.seventeen, R.drawable.eighteen, R.drawable.nineteen,
				R.drawable.twenty, R.drawable.twentyone, R.drawable.twentytwo,
				R.drawable.twentythree, R.drawable.twentyfour,
				R.drawable.twentyfive, R.drawable.twentysix,
				R.drawable.twentyseven, R.drawable.twentyeight,
				R.drawable.twentynine, R.drawable.thirteen,
				R.drawable.thirdtyone, R.drawable.thirdtyfour };
		String[] weather = { "��", "����", "��", "����", "������", "��������б���", "���ѩ",
				"С��", "����", "����", "����", "����", "�ش���", "��ѩ", "Сѩ", "��ѩ", "��ѩ",
				"��ѩ", "��", "����", "ɳ����", "С������", "�е�����", "�󵽱���", "���굽����",
				"���굽�ش���", "С����ѩ", "�е���ѩ", "�е���ѩ", "����", "��ɳ", "ǿɳ����", "��" };
		for (int i = 0; i < weather.length; i++) {
			if (weather[i].equalsIgnoreCase(cond)) {
				return drawalbe[i];
			}
		}

		return 0;

	}

	public static int getBigDrawableId(String cond) {
		int drawalbe[] = { R.drawable.b_one, R.drawable.b_two,
				R.drawable.b_two, R.drawable.b_three, R.drawable.b_four,
				R.drawable.b_five, R.drawable.b_six, R.drawable.b_seven,
				R.drawable.b_eight, R.drawable.b_nine, R.drawable.b_ten,
				R.drawable.b_elevn, R.drawable.b_twele, R.drawable.b_thirteen,
				R.drawable.b_fourteen, R.drawable.b_fourteen,
				R.drawable.b_fifteen, R.drawable.b_sixteen,
				R.drawable.b_seventeen, R.drawable.b_eightteen,
				R.drawable.b_nineteen, R.drawable.b_twenty,
				R.drawable.b_twentyone, R.drawable.b_twentytwo,
				R.drawable.b_twentythree, R.drawable.b_twentyfour,
				R.drawable.b_twentyfive, R.drawable.b_twentysix,
				R.drawable.b_twentyseven, R.drawable.b_twentyeight,
				R.drawable.b_twentynine, R.drawable.b_thirteen,
				R.drawable.b_thirtyone, R.drawable.b_thirtytwo };
		String[] weather = { "��", "����", "��", "����", "������", "��������б���", "���ѩ",
				"С��", "����", "����", "����", "����", "�ش���", "��ѩ", "Сѩ", "��ѩ", "��ѩ",
				"��ѩ", "��", "����", "ɳ����", "С������", "�е�����", "�󵽱���", "���굽����",
				"���굽�ش���", "С����ѩ", "�е���ѩ", "�е���ѩ", "����", "��ɳ", "ǿɳ����", "��" };
		for (int i = 0; i < weather.length; i++) {
			if (weather[i].equalsIgnoreCase(cond)) {
				return drawalbe[i];
			}
		}

		return 0;

	}

}
