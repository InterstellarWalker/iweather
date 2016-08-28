package activity;

import java.util.List;

import service.AutoUpdateService;
import service.NoticationService;
import modle.IWeatherDB;
import com.example.iweather.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import fragment.MainFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Setting extends Activity {
	// ����չʾ���б仯
	public interface ShowCityListener {
		void showCityChange(String city);
	}

	private static final String TAG = "Setting";
	ListView lv_function;
	ListView lv_others;

	private List<String> cList;
	private IWeatherDB iWDB;
	private NotificationManager nm;
	private MyAdapter scAdapter;

	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;

	@ViewInject(R.id.city_show)
	RelativeLayout showCity;
	@ViewInject(R.id.schint)
	TextView scHint;;

	@ViewInject(R.id.re_weather)
	RelativeLayout reWeather;
	@ViewInject(R.id.rwhint)
	TextView rwHint;
	@ViewInject(R.id.CheckBox03)
	CheckBox cb3;
	@ViewInject(R.id.CheckBox02)
	CheckBox cb2;

	@ViewInject(R.id.CheckBox01)
	CheckBox cb1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_setting);
		// ��ʼ������
		getActionBar().setDisplayHomeAsUpEnabled(true);// ���÷��ص���
		getActionBar().setDisplayShowHomeEnabled(false);// ȡ��ͼ��

		// ��ʼ������
		ViewUtils.inject(this);// ע����ͼ
		iWDB = IWeatherDB.getInstance(this);//
		cList = iWDB.queryCityList();
		preferences = getSharedPreferences("iweather", MODE_PRIVATE);
		editor = preferences.edit();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		initData();

		// scListener=(ShowCityListener)getParent();

	}

	private void initData() {
		// TODO Auto-generated method stub
		Boolean notification = preferences.getBoolean("notification", false);
		Boolean alarm = preferences.getBoolean("topBar", false);
		Boolean splash = preferences.getBoolean("splash", false);

		String rwCity = preferences.getString("refreshTime", "�ֶ�ˢ��");
		String scCity = preferences.getString("scCity", "��ѡ��");
		Log.d(TAG, "splash" + splash);
		cb1.setChecked(notification);
		cb2.setChecked(alarm);
		cb3.setChecked(splash);
		scHint.setText(scCity);
		rwHint.setText(rwCity);
	}

	// ��������
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.city_show:

		default:
			break;
		}
		return true;
	}

	@OnClick({ R.id.CheckBox01, R.id.CheckBox02, R.id.CheckBox03, R.id.suggest,
			R.id.help })
	public void cbOnClick(View v) {
		switch (v.getId()) {
		case R.id.CheckBox01:
			if (cb1.isChecked()) {

				IWeatherDB iWDb = IWeatherDB.getInstance(MyApplication
						.getContext());
				List<String> cityList = iWDb.queryCityList();

				String currentCity = cityList.get(0);

				String currentWeather = preferences.getString(currentCity
						+ "_now_cond_txt", "null");
				String currentRange = preferences.getString(currentCity
						+ "__forecast_zero_tmp_min", "0")
						+ "~"
						+ preferences.getString(currentCity
								+ "__forecast_zero_tmp_max", "6");
				int currentIcon = MainFragment.getDrawableID(currentWeather);
				Intent in = new Intent(Setting.this, NoticationService.class);
				in.putExtra("currentCity", currentCity);
				in.putExtra("currentWeather", currentWeather);
				in.putExtra("currentRange", currentRange);
				in.putExtra("currentIcon", currentIcon);
				startService(in);
				/*
				 * Log.d(TAG,in.getStringExtra("currentCity"));
				 * Log.d(TAG,in.getStringExtra("currentWeather")+"11");
				 * Log.d(TAG,in.getStringExtra("currentRange"));
				 * Log.d(TAG,in.getIntExtra("currentIcon", 0)+"");
				 */
				editor.putBoolean("notification", true);
				editor.commit();
				Log.d(TAG, "ѡ��");
			} else {
				editor.putBoolean("notification", false);
				editor.commit();
				nm.cancelAll();
				Log.d(TAG, "δѡ��");
			}
			break;
		case R.id.CheckBox02:
			Intent intent = new Intent();

			if (cb2.isChecked()) {
				intent.putExtra("topBar", true);
				editor.putBoolean("topBar", true);
				editor.commit();
				// editor.putBoolean(key, value)
				cb2.setChecked(true);
				Log.d(TAG, "ѡ��");
			} else {
				intent.putExtra("topBar", false);
				editor.putBoolean("topBar", false);
				cb2.setChecked(false);
				editor.commit();
				Log.d(TAG, "δѡ��");
			}
			intent.setAction("action.isAlarm");
			sendBroadcast(intent);
			break;
		case R.id.CheckBox03:
			if (cb3.isChecked()) {

				editor.putBoolean("splash", true);
				editor.commit();
				// editor.putBoolean(key, value)
				cb3.setChecked(true);
				Log.d(TAG, "ѡ��");
			} else {
				editor.putBoolean("splash", false);
				cb3.setChecked(false);
				editor.commit();
				Log.d(TAG, "δѡ��");
			}
			break;

		}
	}

	@OnClick(R.id.city_show)
	public void cityShowonClick(View v) {
		// Log.d(TAG,"�����չʾ����");
		// ����showCity������
		scAdapter = new MyAdapter();
		showCityDialog("ѡ�����");
	}

	protected void showCityDialog(String title) {
		final AlertDialog.Builder builder = new Builder(Setting.this);
		// builder.setMessage(message);
		builder.setTitle(title);
		builder.setIcon(R.drawable.place);
		builder.setSingleChoiceItems(scAdapter, 0, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// Log.d(TAG,"����˵�ѡ��");
				// ���͸������й㲥
				Intent intent = new Intent();
				intent.setAction("action.refreshShowCity");
				String scCity = null;
				scCity = cList.get(which);
				intent.putExtra("showCity", scCity);
				scHint.setText(scCity);
				editor.putString("scCity", scCity);
				editor.commit();
				sendBroadcast(intent);
				Log.d(TAG, "scCity" + scCity);
				// scListener.showCityChange(cList.get(which));
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	// ��������
	@OnClick(R.id.re_weather)
	public void reWeatherClick(View v) {
		// Log.d(TAG,"�����չʾ����");
		reWeatherDialog("��������");
	}

	protected void reWeatherDialog(String title) {
		final AlertDialog.Builder builder = new Builder(Setting.this);
		// builder.setMessage(message);
		builder.setTitle(title);
		builder.setIcon(R.drawable.ic_action_cloud);

		builder.setSingleChoiceItems(new String[] { "�ֶ�����", "1Сʱ", "2Сʱ",
				"3Сʱ", "4Сʱ", "5Сʱ" }, 0, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// TODO Auto-generated method stub // Log.d(TAG,"����˵�ѡ��");
				// ���͸������й㲥
				switch (which) {
				case 0:
					editor.putInt("updateTime", 0);
					editor.commit();
					rwHint.setText("�ֶ�����");
					break;
				case 1:
					Intent one = new Intent(Setting.this,
							AutoUpdateService.class);
					one.putExtra("updateTime", 1);
					editor.putInt("updateTime", 1);
					editor.putString("refreshTime", "1Сʱ");
					editor.commit();
					rwHint.setText("1Сʱ");
					startService(one);
					break;
				case 2:
					Intent two = new Intent(Setting.this,
							AutoUpdateService.class);
					two.putExtra("updateTime", 2);
					editor.putInt("updateTime", 2);
					editor.putString("refreshTime", "2Сʱ");
					editor.commit();
					rwHint.setText("2Сʱ");
					startService(two);
					break;
				case 3:
					Intent three = new Intent(Setting.this,
							AutoUpdateService.class);
					three.putExtra("updateTime", 3);
					editor.putInt("updateTime", 3);
					editor.putString("refreshTime", "3Сʱ");
					editor.commit();
					rwHint.setText("3Сʱ");
					startService(three);
					break;
				case 4:
					Intent four = new Intent(Setting.this,
							AutoUpdateService.class);
					four.putExtra("updateTime", 4);
					editor.putInt("updateTime", 4);
					editor.putString("refreshTime", "4Сʱ");
					editor.commit();
					rwHint.setText("4Сʱ");
					startService(four);
					break;
				case 5:
					Intent five = new Intent(Setting.this,
							AutoUpdateService.class);
					five.putExtra("updateTime", 5);
					editor.putInt("updateTime", 5);
					editor.putString("refreshTime", "5Сʱ");
					editor.commit();
					rwHint.setText("5Сʱ");
					startService(five);
					break;

				default:
					break;
				}

				// Log.d(TAG,cList.get(which)+"");
				// scListener.showCityChange(cList.get(which));
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cList.size();
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
			View view = View.inflate(Setting.this, R.layout.dialog_showcity,
					null);
			ViewUtils.inject(this, parent);
			// Log.d(TAG,cList.size()+"");
			TextView sCity = (TextView) view.findViewById(R.id.showcity);

			sCity.setText(cList.get(position));

			return view;
		}
	}

}
