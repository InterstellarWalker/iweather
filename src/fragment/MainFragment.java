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
	private DrawerLayout drawerLayout;// 侧边栏对象
	protected static final int UPTEXT_DATA = 10;
	protected static final String TAG = "MainFragment";

	public IWeatherDB iWDb;// 数据库实例
	protected static final int REQUEST_CODE = 101;// 请求码
	public MyPagerAdapter vpAdapter;
	protected HttpUtil httpUtil;
	public ImageButton btSliding; // Log.d(TAG, "初始化页面ViewPager数据");

	public PullToRefreshScrollView scrollView;
	private Long lastLoadTime;
	private boolean isRefresh;// 判断从哪里获取数据
	private int mPointWidth;// 圆点之间的间距；
	private View viewBlackPoint;// 小黑点
	private LinearLayout lLayout;
	private List<String> beforeCityList;
	private View view;
	private String currentCity;// 当前位置的城市名称
	private CityChangedListener listener;// 城市改变监听
	private TextView publishTime;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private int flag = 0;

	// index 信息
	public String[] weeks = { "日", "一", "二", "三", "四", "五", "六" };

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
		// 从选择城市进入主页面
		beforeCityList = (List<String>) iWDb.queryCityList();// 保存已经选择的城市
		if (beforeCityList.size() == 0) {
			cityNumNull(0);
		} else {
			initView();
		}
		return view;
	}

	@SuppressWarnings("deprecation")
	private View initView() {// 初始化视图

		// TODO Auto-generated method stub
		view = View.inflate(mActivity, R.layout.main_fragment, null);
		publishTime=(TextView) view.findViewById(R.id.weather_publish_time);
		
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer);
		lLayout = (LinearLayout) view.findViewById(R.id.fg_linearLayout);
		viewBlackPoint = view.findViewById(R.id.the_black_point);
		viewPager.setOnPageChangeListener(new WeatherPagerChanged());
		// 初始化小圆点
		for (int i = 0; i < beforeCityList.size(); i++) {
			vpList.add(View.inflate(mActivity, R.layout.layout_weather_detail,
					null));// 装载视图数据
			addIndicatePoint(i);// 增加指示点
		}
		// 计算小圆点之间间隔
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
						if (num > 1) {// 必须有俩个圆点时白点才会有间距
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
			// Log.d(TAG,"页签数量"+vpList.size());

			return vpList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {

			container.removeView(vpList.get(position));

			Log.d(TAG, "移除" + position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {

			container.addView(vpList.get(position));// 将对应位置的view添加进viewpager中

			// 初始化标题
			cityTitleName = (TextView) container
					.findViewById(R.id.tx_main_title);
			// cityTitleName.setTag("title");
			Log.d(TAG,"scrollView呗初始化");
			scrollView = (PullToRefreshScrollView) view
					.findViewById(R.id.layout_weathwer_detail_scrollview);
			// 每次滑动时会调用notifyDataSetChanged,使cityTitleName.getText()为空所以每次都会滑动都会调用异步更新

			// 当标题被赋值文本以后每次滑动就不会调用异步更新了
			if (TextUtils.isEmpty(cityTitleName.getText())) {
				currentCity = beforeCityList.get(position);// 初始化对应的按钮,并保证当前城市为当前页面城市，否则会被viewpager预加载机制覆盖
				initScrollView(currentCity, scrollView);// 传入最新的scrollview对象
				initTextView(container);// 初始化文本控件
				cityTitleName.setText(currentCity);
				// 判断是否需要从网络获取数据，还是从sharepreferences中获取数据
				//publishTime.setText(preferences.getString(currentCity + "_basic_update_loc",null));//更新时间
				setTextValues(currentCity);
				if (isFromDB(currentCity)) {
					Log.d(TAG, 2 + "调用网络数据");
					new UpdateUIAsyncTask().execute(currentCity);
					// scrollView.onRefreshComplete();
					// 如果刷新间隔超过10分钟就重新否则从数据库中取出数据(无网络情况也应该从数据库中加载数据);
				} 
				
				Log.d(TAG, "当前城市1" + currentCity);
			}

			// 初始化按钮
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
						Toast.makeText(mActivity, "添加城市过多，请先删除城市后再添加（长摁侧边栏城市栏）", Toast.LENGTH_SHORT).show();
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
		} else if (resultCode == 110 && requestCode == REQUEST_CODE) {// 如果否，则跳到该city所在的页面
			// Log.d(TAG, "选择的城市" + saveCity);
			viewPager.setCurrentItem(beforeCityList.indexOf(data
					.getStringExtra("saveCity")));
		}
	}

	// 增加城市数量
	public void addCityNum() {
		int cityNum = beforeCityList.size();
		vpList.add(View
				.inflate(mActivity, R.layout.layout_weather_detail, null));// 加载新增加的城市
		addIndicatePoint(cityNum);// 加载指示点
		getPointDis(cityNum);// 计算此时小灰点的距离
		vpAdapter.notifyDataSetChanged();

		countPoitnWidth(0, cityNum);
		// Log.d(TAG, "响应选择城市后城市数量=" + vpList.size());

	}

	// 减少城市数量
	public void reduceCityNum(int position) {
		// beforeCityList =iWDb.queryCityList();//获取最新城市列表
		// 考虑使用set集合判断是否重复，list集合保存

		// viewPager.removeViewAt(0);
		iWDb.db.delete("CitySet", "_id=?", new String[] { position + "" });
		beforeCityList = iWDb.queryCityList();
		lLayout.removeViewAt(beforeCityList.size());

		// lLayout.removeAllViews();
		// reduceIndicatePoint(beforeCityList.size());
		viewPager.setAdapter(null);// viewPager没有减少页签的方法只能将viewPager全部销毁掉再重新设置adapter
		vpList.remove(position);
		viewPager.setAdapter(vpAdapter);
		vpAdapter.notifyDataSetChanged();
		if (beforeCityList.size() == 0) {
			startActivityForResult(new Intent(mActivity, ChooseCity.class),
					REQUEST_CODE);
		}

	}

	// 判断城市是否为空
	public View cityNumNull(int cityNum) {
		// 判断是否保存过城市
		startActivity(new Intent(getActivity(), ChooseCity.class));
		Log.d(TAG, "当前没有城市");
		startActivity(new Intent(mActivity, ChooseCity.class));
		mActivity.finish();
		return null;

	}

	// 增加指示点
	private void addIndicatePoint(int i) {
		View point = new View(mActivity);
		point.setBackgroundResource(R.drawable.the_gray_point);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				DensityUtils.dp2px(mActivity, 5), DensityUtils.dp2px(mActivity,
						5));
		if (i > 0) {
			params.leftMargin = DensityUtils.dp2px(mActivity, 8);// 设置每个小圆点之间的间距
		}
		// params.setLayoutDirection(layoutDirection);
		point.setLayoutParams(params);
		lLayout.addView(point);
	}

	

	// 计算黑点的移动距离
	private void countPoitnWidth(float positionOffset, int position) {
		int len = (int) (mPointWidth * positionOffset) + position * mPointWidth;
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewBlackPoint
				.getLayoutParams();
		params.leftMargin = len;
		viewBlackPoint.setLayoutParams(params);
	}

	class WeatherPagerChanged implements OnPageChangeListener {
		@Override
		// 监听滑动状态的变化
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			Log.d(TAG, state + "onPage");
		}

		@Override
		// 监听滑动参数
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// TODO Auto-generated method stub
			countPoitnWidth(positionOffset, position);
		}

		@Override
		// 监听哪一页被选中
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			// 第一时间被调用（在绘图之前）
			// Log.d(TAG, "选中的位置=" + position);

			if (position > 0) {
				vpAdapter.notifyDataSetChanged();
			}
			

		}
	}

	// 异步更新数据
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
			// params 是代表所传参数集合
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
			Log.d(TAG, "刷新完成后调用onRefreshComplete()");
			Boolean isHasData = preferences.getBoolean(
					parseCity + "_isHasData", false);

			if (isHasData) {
				vpAdapter.notifyDataSetChanged();
				scrollView.onRefreshComplete();
				setTextValues(parseCity);
				Toast.makeText(mActivity, "刷新成功", Toast.LENGTH_SHORT).show();

			} else {
				vpAdapter.notifyDataSetChanged();
				scrollView.onRefreshComplete();
				
				if (iWDb.queryCityNameInfo(parseCity)
						.get("city_code")==null) {
					Toast.makeText(mActivity, "正在下载该城市信息请稍后尝试", Toast.LENGTH_SHORT).show();
					
				}else{
				Toast.makeText(mActivity, "无网络", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	private void initScrollView(final String currentCity,
			PullToRefreshScrollView scrollView) {
		// TODO Auto-generated method stub
		
		ILoadingLayout startLabels = scrollView.getLoadingLayoutProxy();
		startLabels.setPullLabel("下拉刷新");
		startLabels.setRefreshingLabel("正在刷新");
		startLabels.setReleaseLabel("松开刷新");

		scrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				String label = DateUtils.formatDateTime(
						MyApplication.getContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				Log.d(TAG, "1111调用网络数据");

				new UpdateUIAsyncTask().execute(currentCity);
			}
		});
		// scrollView.onRefreshComplete();
	}

	private Boolean isFromDB(String parseCity) {
		// 判断应该从哪里开始更新
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

		// 注入now界面
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
		// now界面参数
		nowAirQuality.setTag("nowAirQuality");
		nowAirPM.setTag("nowAirPM");
		nowCond.setTag("nowCond");
		nowTemRange.setTag(" nowTemRange");
		nowTem.setTag("nowTem");
		// sugesstion界面
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
		// 今日预报接界面
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
		// 未来三天预测

		// 预测第一天
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
		// 预测第二天
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
		// 预测第三天
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
		// 初始化图片
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
			Log.d(TAG, 3 + "调用网络数据");
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
					.setText("空气质量："
							+ preferences.getString(parseCity
									+ "_aqi_city_qlty", null));
			nowAirPM.setText("PM2.5: "
					+ preferences.getString(parseCity + "_aqi_city_pm25", null));

			nowTem.setText(preferences.getString(parseCity + "_now_tmp", null)
					+ "°");
			nowCond.setText(preferences.getString(parseCity + "_now_cond_txt",
					null));
			// Log.d(TAG,preferences.getString(parseCity+"_now_cond_txt",null));
			nowTemRange.setText(preferences.getString(parseCity
					+ "__forecast_zero_tmp_min", null)
					+ "°~"
					+ preferences.getString(parseCity
							+ "__forecast_zero_tmp_max", null) + "°");
			// suggestion参数
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
			// 今日预报
			TextView tdWeather = (TextView) viewPager
					.findViewWithTag("tdWeather");
			TextView tdTgTem = (TextView) viewPager.findViewWithTag("tdTgTem");
			TextView tdHum = (TextView) viewPager.findViewWithTag("tdHum");
			TextView tdWind = (TextView) viewPager.findViewWithTag("tdWind");
			tdWeather.setText(preferences.getString(
					parseCity + "_now_cond_txt", null));
			tdTgTem.setText(preferences.getString(parseCity + "_now_fl", null)
					+ "°");
			tdHum.setText(preferences.getString(parseCity
					+ "__forecast_zero_hum", null)
					+ "%");
			tdWind.setText(preferences.getString(parseCity
					+ "__forecast_zero_wind_dir", null)
					+ "/"
					+ preferences.getString(parseCity
							+ "__forecast_zero_wind_sc", null));
			// 预测第一天
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
			fir_week.setText("星期"
					+ weeks[(getWeek(System.currentTimeMillis()) + 1) % 7]);
			fir_weather.setText(preferences.getString(parseCity
					+ "__forecast_first_cond_txtd", null));
			fir_date.setText(getDate(preferences.getString(parseCity
					+ "__forecast_first_date", null)));
			fir_tem.setText(preferences.getString(parseCity
					+ "__forecast_first_tmp_min", null)
					+ "°~"
					+ preferences.getString(parseCity
							+ "__forecast_first_tmp_max", null) + "°");
			fir_wind.setText(preferences.getString(parseCity
					+ "__forecast_first_wind_sc", null)
					+ "级");
			fir_pop.setText(preferences.getString(parseCity
					+ "__forecast_first_pop", null)
					+ "%");
			fir_vis.setText(preferences.getString(parseCity
					+ "__forecast_first_vis", null)
					+ "km");
			// 预测第二天
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
			sec_week.setText("星期"
					+ weeks[(getWeek(System.currentTimeMillis()) + 2) % 7]);
			sec_weather.setText(preferences.getString(parseCity
					+ "__forecast_second_cond_txtd", null));
			sec_date.setText(getDate(preferences.getString(parseCity
					+ "__forecast_second_date", null)));
			sec_tem.setText(preferences.getString(parseCity
					+ "__forecast_second_tmp_min", null)
					+ "°~"
					+ preferences.getString(parseCity
							+ "__forecast_second_tmp_max", null) + "°");
			sec_wind.setText(preferences.getString(parseCity
					+ "__forecast_second_wind_sc", null)
					+ "级");
			sec_pop.setText(preferences.getString(parseCity
					+ "__forecast_second_pop", null)
					+ "%");
			sec_vis.setText(preferences.getString(parseCity
					+ "__forecast_second_vis", null)
					+ "km");
			// 预测第三天
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
			thir_week.setText("星期"
					+ weeks[(getWeek(System.currentTimeMillis()) + 3) % 7]);
			thir_weather.setText(preferences.getString(parseCity
					+ "__forecast_third_cond_txtd", null));
			thir_date.setText(getDate(preferences.getString(parseCity
					+ "__forecast_third_date", null)));
			thir_tem.setText(preferences.getString(parseCity
					+ "__forecast_third_tmp_min", null)
					+ "°~"
					+ preferences.getString(parseCity
							+ "__forecast_third_tmp_max", null) + "°");
			thir_wind.setText(preferences.getString(parseCity
					+ "__forecast_third_wind_sc", null)
					+ "级");
			thir_pop.setText(preferences.getString(parseCity
					+ "__forecast_third_pop", null)
					+ "%");
			thir_vis.setText(preferences.getString(parseCity
					+ "__forecast_third_vis", null)
					+ "km");

		}
		// 初始化图片
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
		//更新时间
		String time=(String) preferences.getString(parseCity + "_basic_update_loc",null);
		if (time!=null) {
			
			publishTime.setText("更新于"+(time.subSequence(11, 16)));//更新时间
		}
		
		Log.d(TAG,preferences.getString(parseCity + "_basic_update_loc",null)+"更新时间");
		/*
		 * Log.d(TAG,now_detail_icon+"11111"); Log.d(TAG,"今日天气值"+);
		 * Log.d(TAG,+"2222");
		 */
		now_detail_icon.setBackgroundResource(getBigDrawableId(preferences
				.getString(parseCity + "__forecast_zero_cond_txtd", null)));
		
		listener.notifyAdapter(parseCity);//当主页面数据发生变化时通知侧边栏数据也要改变
		if (preferences.getString("scCity",beforeCityList.get(0)).equals(parseCity)) {
			scListener.showCityChange(parseCity);//也通知显示城市改变
			
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
		String[] weather = { "晴", "多云", "阴", "阵雨", "雷阵雨", "雷阵雨伴有冰雹", "雨夹雪",
				"小雨", "中雨", "大雨", "暴雨", "大暴雨", "特大暴雨", "阵雪", "小雪", "中雪", "大雪",
				"暴雪", "雾", "冻雨", "沙尘暴", "小到中雨", "中到大雨", "大到暴雨", "暴雨到大暴雨",
				"大暴雨到特大暴雨", "小到中雪", "中到大雪", "中到大雪", "浮尘", "扬沙", "强沙尘暴", "霾" };
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
		String[] weather = { "晴", "多云", "阴", "阵雨", "雷阵雨", "雷阵雨伴有冰雹", "雨夹雪",
				"小雨", "中雨", "大雨", "暴雨", "大暴雨", "特大暴雨", "阵雪", "小雪", "中雪", "大雪",
				"暴雪", "雾", "冻雨", "沙尘暴", "小到中雨", "中到大雨", "大到暴雨", "暴雨到大暴雨",
				"大暴雨到特大暴雨", "小到中雪", "中到大雪", "中到大雪", "浮尘", "扬沙", "强沙尘暴", "霾" };
		for (int i = 0; i < weather.length; i++) {
			if (weather[i].equalsIgnoreCase(cond)) {
				return drawalbe[i];
			}
		}

		return 0;

	}

}
