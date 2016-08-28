package activity;

import modle.City;

import com.example.iweather.R;
import fragment.LeftFragment;
import fragment.MainFragment;

import activity.Setting.ShowCityListener;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentTransaction;

import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.Window;


public class MainActivity extends FragmentActivity implements
	MainFragment.CityChangedListener,LeftFragment.ClickCityListener,ShowCityListener{
	private static final String TAG = "MainActivity";
	Intent chooseIntent;
	private String saveCity;
	ViewPager viewPager;
	private FragmentTransaction transaction;
	private LeftFragment lefFragment;
	public City city;
	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;
	MainFragment mainFragment;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_main);
		
		city = new City();
		initView();
	}

	/*
	 * private void initView() { // TODO Auto-generated method stub
	 * 
	 * }
	 */
	// 初始化侧边栏

	private void initView() {
		
		FragmentManager fm = getSupportFragmentManager();// 获取碎片管理者
		transaction = fm.beginTransaction();// 开启碎片事务
		lefFragment = new LeftFragment();
		//((View)lefFragment).setTag("leftFragment");
		transaction.replace(R.id.sl, lefFragment);// 将原始的侧边栏替换成自定义侧边栏
		 mainFragment = new MainFragment();
		//
		transaction.replace(R.id.ml, mainFragment);
		transaction.commit();// 提交事务

	}
	@Override
	public void notifyAdapter(String saveCity) {
		// TODO Auto-generated method stub
		Log.d(TAG, "2222saveCity" + saveCity);
		if (saveCity != null) {
			 lefFragment.addCityNum();
		}
	}
	 
	@Override
	public void returnCity(int position) {
		// TODO Auto-generated method stub
		mainFragment.viewPager.setCurrentItem(position);
	}

	@Override
	public void changeCity(int position) {
		// TODO Auto-generated method stub
		mainFragment.reduceCityNum(position);
		//mainFragment.removePage(position);
		Log.d(TAG,"changeCity");
		
	}

	@Override
	public void showCityChange(String city) {
		// TODO Auto-generated method stub
	//	new LeftFragment().updateTopBar();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		/*preferences = getSharedPreferences("iweather", MODE_PRIVATE);
		editor = preferences.edit();
		editor.putBoolean("notification",false);
		editor.commit()*/;
	}
}
