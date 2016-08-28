package activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.spec.OAEPParameterSpec;

import modle.IWeatherDB;
import service.AutoUpdateService;
import service.HandleDataIntentService;
import view.CustomSearchView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.iweather.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ChooseCity extends Activity {

	private ListView cityLv;
	private static final String TAG = "ChooseCity";
	protected static final String CHOOSED_CITY = "choose_city";

	public Cursor cursor;
	LocateBroadcastReceiver lBroadcast;// �㲥������

	private IWeatherDB iWdb;// ���ݿ�
	private CustomSearchView searchView;// ������
	boolean isDownCity;// ��¼�Ƿ����ع�����Ŀ¼
	private AlertDialog.Builder builder;// �Ի���
	Dialog dialog;
	private RelativeLayout relayout;
	// ���ؿؼ�
	@ViewInject(R.id.beijin)
	Button beijin;
	@ViewInject(R.id.shanghai)
	Button shanghai;
	@ViewInject(R.id.guangzhou)
	Button guangzhou;
	@ViewInject(R.id.shenzhen)
	Button shenzhen;
	@ViewInject(R.id.tianjin)
	Button tianjin;
	@ViewInject(R.id.xingyang)
	Button xinyang;
	@ViewInject(R.id.hangzhou)
	Button hangzhou;
	@ViewInject(R.id.ningbo)
	Button ningbo;
	@ViewInject(R.id.xian)
	Button xian;
	@ViewInject(R.id.chengdu)
	Button chengdu;
	@ViewInject(R.id.chongqin)
	Button chongqin;
	@ViewInject(R.id.nanjin)
	Button nanjin;
	@ViewInject(R.id.suzhou)
	Button suzhou;
	@ViewInject(R.id.wuhan)
	Button wuhan;
	@ViewInject(R.id.dongguan)
	Button dongguan;
	@ViewInject(R.id.xiamen)
	Button xiamen;
	@ViewInject(R.id.auto_position)
	TextView autoPosition;

	// ��ʼ����������
	// private
	/*
	 * private ServiceConnection connection = new ServiceConnection() {
	 * 
	 * @Override public void onServiceDisconnected(ComponentName name) { // TODO
	 * Auto-generated method stub }
	 * 
	 * @Override public void onServiceConnected(ComponentName name, IBinder
	 * service) { // TODO Auto-generated method stub mBinder = (Download)
	 * service; mBinder.saveAllCity(); Log.d(TAG, "���ط�����"); } };
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_choose_city);
		iWdb = IWeatherDB.getInstance(this);// ��ʼ�����ݿ�
		// isNeedDownCity();
		// ע��㲥
		IntentFilter inFilter = new IntentFilter();
		inFilter.addAction("locateSuccessfull");
		lBroadcast = new LocateBroadcastReceiver();
		registerReceiver(lBroadcast, inFilter);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		ViewUtils.inject(this);

		searchView = (CustomSearchView) findViewById(R.id.searchView);
		cityLv = (ListView) findViewById(R.id.search_lv);

		relayout = (RelativeLayout) findViewById(R.id.cc_relativeL);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			// ��ѯ�ı��ύʱִ�еķ���
			@Override
			public boolean onQueryTextSubmit(final String query) {
				// TODO Auto-generated method stub
				// Log.d(TAG, "onQueryTextSubmit����ִ��"+query);
				// ����ַ�����0���߿��򷵻���
				if (TextUtils.isEmpty(query)) {
					Toast.makeText(ChooseCity.this, "����������ַ�",
							Toast.LENGTH_SHORT).show();
				} else {

				}
				return true;

			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				// Log.d(TAG, "onQueryTextChange����ִ��"+newText);
				if (TextUtils.isEmpty(newText)) {
					relayout.setVisibility(View.VISIBLE);
					cursor.close();
				} else {
					relayout.setVisibility(View.INVISIBLE);
					cursor = iWdb.db.rawQuery(
							"select * from City where city_name like?",
							new String[] { "%" + newText + "%" });
					@SuppressWarnings("deprecation")
					SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(
							ChooseCity.this,
							android.R.layout.simple_list_item_1, cursor,
							new String[] { "city_name" },
							new int[] { android.R.id.text1 });
					cityLv.setAdapter(mAdapter);
				}
				cityLv.setOnItemClickListener(new MyItemClickListener());
				return true;
			}
		});
	}

	// ���ص���¼��Ľ�����¸�Activity
	private void returnClickResult(String saveCity) {
		Log.d(TAG, saveCity);
		// List<String> beforeAddList//=new ArrayList<String>();
		List<String> beforeAddList = iWdb.queryCityList();// ���ǰ�����б�
		int cityNum = 0;
		cityNum = beforeAddList.size();
		if (cityNum == 0) {
			iWdb.saveCityList(saveCity);
			Intent in = new Intent(ChooseCity.this, MainActivity.class);
			startActivity(in);
			finish();
		} else {
			Set<String> set = new HashSet<String>();
			for (int i = 0; i < cityNum; i++) {
				set.add(beforeAddList.get(i));
			}
			if (set.add(saveCity)) {
				iWdb.saveCityList(saveCity);
				// List<String> afterAddList=iWdb.queryCityList();//��Ӻ�����б�
				Intent in = new Intent(ChooseCity.this, MainActivity.class);
				in.putExtra("saveCity", saveCity);
				setResult(Activity.RESULT_OK, in);
			} else {
				Intent in = new Intent(ChooseCity.this, MainActivity.class);
				in.putExtra("saveCity", saveCity);
				setResult(110, in);
			}
		}

		finish();
	}

	class MyItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			String saveCity = ((TextView) view).getText().toString();
			returnClickResult(saveCity);

		}
	}

	@OnClick({ R.id.beijin, R.id.shanghai, R.id.guangzhou, R.id.shenzhen,
			R.id.tianjin, R.id.xingyang, R.id.hangzhou, R.id.ningbo, R.id.xian,
			R.id.chengdu, R.id.chongqin, R.id.nanjin, R.id.suzhou, R.id.wuhan,
			R.id.dongguan, R.id.xiamen, R.id.auto_position })
	public void onClickMethod(View v) {
		switch (v.getId()) {
		case R.id.beijin:
			// String saveCity=
			returnClickResult(beijin.getText().toString());
			break;
		case R.id.shanghai:

			returnClickResult(shanghai.getText().toString());
			break;
		case R.id.guangzhou:

			returnClickResult(guangzhou.getText().toString());
			break;
		case R.id.shenzhen:

			returnClickResult(shenzhen.getText().toString());
			break;
		case R.id.tianjin:

			returnClickResult(tianjin.getText().toString());
			break;
		case R.id.xingyang:

			returnClickResult(xinyang.getText().toString());
			break;

		case R.id.hangzhou:

			returnClickResult(hangzhou.getText().toString());
			break;

		case R.id.ningbo:

			returnClickResult(ningbo.getText().toString());
			break;

		case R.id.xian:

			returnClickResult(xian.getText().toString());
			break;

		case R.id.chengdu:

			returnClickResult(chengdu.getText().toString());
			break;

		case R.id.chongqin:

			returnClickResult(chongqin.getText().toString());
			break;
		case R.id.nanjin:
			returnClickResult(nanjin.getText().toString());
			break;
		case R.id.suzhou:
			returnClickResult(suzhou.getText().toString());
			break;

		case R.id.wuhan:
			returnClickResult(wuhan.getText().toString());
			break;

		case R.id.dongguan:
			returnClickResult(dongguan.getText().toString());
			break;

		case R.id.xiamen:
			returnClickResult(xiamen.getText().toString());
			break;
		case R.id.auto_position:
			// getWebSite();
			// returnClickResult(autoPosition.getText().toString());
			// ������λ����
			Intent in = new Intent(this, HandleDataIntentService.class);
			// in.putExtra("config", GolbalConstant.LOCATION);
			startService(in);
			// �����Ի���
			builder = new Builder(ChooseCity.this);
			dialog = builder.create();
			builder.setMessage("���ڶ�λ���Ե�").setTitle("��λ��");
			dialog.show();

			// dialog.dismiss();
			break;

		default:
			break;
		}
	}

	class LocateBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String lcCity = intent.getStringExtra("locateCity");

			if (lcCity != null && !lcCity.equals("null")) {
				Toast.makeText(getApplicationContext(), "��λ�ɹ�",
						Toast.LENGTH_SHORT).show();
				returnClickResult(lcCity);
			} else {
				Toast.makeText(getApplicationContext(), "��λʧ��",
						Toast.LENGTH_SHORT).show();
			}
			dialog.dismiss();
			// new UpdateUIAsyncTask().execute(a,b);
			Log.d(TAG, "���յ��ĳ���" + intent.getStringExtra("locateCity"));

		}

	}

/*	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (lBroadcast != null) {
			unregisterReceiver(lBroadcast);
			Log.d(TAG, "ȡ��ע��");
		}
		if (dialog != null) {
			dialog.dismiss();
		}
	}*/
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (lBroadcast != null) {
			unregisterReceiver(lBroadcast);
			Log.d(TAG, "ȡ��ע��");
		}
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	
	

}
