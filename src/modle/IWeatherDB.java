package modle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.AqiDetail;
import data.CityInfo;
import data.ForecastFirstDay;
import data.Now;

import DB.MyDatabaseHelper;
import activity.MyApplication;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;
import android.widget.Toast;

public class IWeatherDB {
	private static final int VERSION = 1;
	private static final String DB_NAME = "i_weather";
	private static final String TAG = "IWeather";
	private static IWeatherDB iWeatherDB;
	public SQLiteDatabase db;

	private IWeatherDB(Context context) {
		// TODO Auto-generated constructor stub
		MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context,
				DB_NAME, null, VERSION);
		db = myDatabaseHelper.getWritableDatabase();
	}

	public synchronized static IWeatherDB getInstance(Context context) {
		if (iWeatherDB == null) {
			iWeatherDB = new IWeatherDB(context);
		}
		return iWeatherDB;
	}

	public void saveCityList(String saveCity) {
		ContentValues values = new ContentValues();
		values.put("city_saveName", saveCity);
		Log.d(TAG, "已保存城市数据:" + saveCity);
		db.insert("CitySet", null, values);

	};

	public List<String> queryCityList() {
		Cursor cursor = db.query("CitySet", null, null, null, null, null, null);
		List<String> saveCityList = new ArrayList<String>();
		while (cursor.moveToNext()) {
			saveCityList.add(cursor.getString(cursor
					.getColumnIndex("city_saveName")));
			/*
			 * Log.d(TAG, "可查询城市数据" + cursor.getString(cursor
			 * .getColumnIndex("city_saveName")));
			 */
		}
		return saveCityList;
	};

	// 保存全国所有城市
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_pyName", city.getCityPyName());
			values.put("city_code", city.getCityCode());
			values.put("city_quName", city.getCityQuName());
			/*
			 * if (db.query("City", new String []{"city_name"}, "city_name=?",
			 * new String []{city.getCityName()}, null, null, null)==null) {
			 * Log.d(TAG, "保存城市成功"); }
			 */
			db.insert("City", null, values);
		}
	}

	// 查询全国所有城市
	public Map<String, String> queryCityNameInfo(String selections) {
		try {
			Map<String, String> cityInfoMap = new HashMap<String, String>();
			String code = null;
			String name = null;
			String quName = null;
			String pyName = null;
			// Log.d(TAG, "city="+selections);
			Cursor cursor = db
					.rawQuery(
							"select * from City where city_name like? or city_pyName like?",
							new String[] { selections });

			// Log.d(TAG, "cityInfo22222" + cursor);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					name = cursor.getString(cursor.getColumnIndex("city_name"));
					code = cursor.getString(cursor.getColumnIndex("city_code"));
					quName = cursor.getString(cursor
							.getColumnIndex("city_quName"));
					pyName = cursor.getString(cursor
							.getColumnIndex("city_pyName"));
					cityInfoMap.put("city_name", name);
					cityInfoMap.put("city_code", code);
					cityInfoMap.put("city_quName", quName);
					cityInfoMap.put("city_pyName", pyName);

				}
				// Log.d(TAG, "cityInfo" + code);
				cursor.close();
				return cityInfoMap;
			} else {
				// Toast.makeText(context,"网络访问失败" , duration)；
				return null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Toast.makeText(MyApplication.getContext(), "对不起没有您要找的城市",
					Toast.LENGTH_SHORT).show();
		}
		return null;

	}

	// 保存天气-城市具体信息
	public void saveCityInfo(CityInfo cityInfo) {
		if (cityInfo != null) {
			ContentValues values = new ContentValues();

			values.put("c1", cityInfo.getC1());
			values.put("c2", cityInfo.getC2());
			values.put("c3", cityInfo.getC4());
			values.put("c4", cityInfo.getC4());
			values.put("c5", cityInfo.getC5());
			values.put("c6", cityInfo.getC6());
			values.put("c7", cityInfo.getC7());
			values.put("c8", cityInfo.getC8());
			values.put("c9", cityInfo.getC9());
			values.put("c10", cityInfo.getC10());
			values.put("c11", cityInfo.getC11());
			values.put("c12", cityInfo.getC12());
			values.put("c15", cityInfo.getC15());
			values.put("c16", cityInfo.getC16());
			values.put("c17", cityInfo.getC17());
			values.put("latitude", cityInfo.getLatitude());
			values.put("longitude", cityInfo.getLongitude());

			// Log.d(TAG, "c9 :" + cityInfo.getC9());
			// Log.d(TAG, "temperature_time" + now.geTtemperature_time());
			// Log.d(TAG, "area" + now.getArea());
			db.insert("CityInfo", null, values);
		}
	}

}
