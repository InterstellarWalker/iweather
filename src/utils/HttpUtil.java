package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import modle.AlarmList;
import modle.AqiCity;
import modle.Basic;
import modle.City;

import modle.IWeatherDB;

import modle.UpDate;
import modle.Wind;
import modle.Now;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.gson.Gson;

import data.AqiDetail;
import data.CityInfo;
import data.ForecastFirstDay;
import activity.MyApplication;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.AvoidXfermode.Mode;
import android.util.Log;
import android.widget.Toast;

public class HttpUtil {
	public IWeatherDB db;

	private static final String TAG = "HttpUtil";
	ExecutorService cachedThreadPool = Executors.newCachedThreadPool();// 创建缓存线程池

	public void sendRequestHttp(final String address,
			final HttpCallBackListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpURLConnection connection = null;
				try {
					Log.d(TAG, "开始解析" + System.currentTimeMillis());
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					// Log.d(TAG, "从网络下载数据正在进行1");
					connection.setRequestMethod("GET");
					// Log.d(TAG, "从网络下载数据正在进行2");
					InputStream in = connection.getInputStream();
					// Log.d(TAG, "从网络下载数据正在进行3");

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					// Log.d(TAG, "从网络下载数据正在进行4");
					StringBuilder responseData = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						responseData.append(line);
						// Log.d(TAG, "从网络下载数据正在进行555");
					}
					// Log.d(TAG, "responseData: " + responseData);
					if (responseData != null) {
						listener.onFinish(responseData.toString());
						// Log.d(TAG, "从网络下载数据正在进行");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// Toast.makeText(MyApplication.context,"请输入正确的城市名",Toast.LENGTH_SHORT).show();
					listener.onError(e);
				} finally {
					connection.disconnect();
				}
			}
		}).start();
	}

	public static void getWeatherResponse(String parseCity) {
		HttpURLConnection connection = null;
		try {
			IWeatherDB iWDb = IWeatherDB
					.getInstance(MyApplication.getContext());
			Log.d(TAG,"parcity"+parseCity);
			String chooseCityID = iWDb.queryCityNameInfo(parseCity).get(
					"city_code");
			String adWeather = Url.getHeFengUrl(chooseCityID);// 获取天气URL
			// Log.d(TAG, "开始解析" + params[0]);
			URL url = new URL(adWeather);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(8000);
			connection.setRequestMethod("GET");
			InputStream in = connection.getInputStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder responseData = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				responseData.append(line);
				Log.d(TAG, responseData.toString());
			}
			if (responseData.toString() != null) {
				parseHeWeather(responseData.toString(), parseCity);
			}

		} catch (Exception e) {
			e.printStackTrace();

			Log.d(TAG, "网络故障");
		} finally {
			connection.disconnect();
		}
	}

	public static void getAqiResponse(String parseCity) {
		HttpURLConnection connection = null;
		try {
			IWeatherDB iWDb = IWeatherDB
					.getInstance(MyApplication.getContext());
			Log.d(TAG,"parcity"+parseCity);
			String chooseCityID = iWDb.queryCityNameInfo(parseCity).get(
					"city_code");
			String adAqi = Url.getWeatherFromLNUrl(chooseCityID);// 获取空气质量URL
			// Log.d(TAG, "开始解析" + params[0]);
			URL url = new URL(adAqi);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(8000);
			connection.setRequestMethod("GET");
			InputStream in = connection.getInputStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder responseData = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				responseData.append(line);
				Log.d(TAG, responseData.toString());
			}
			if (responseData.toString() != null) {
				// Log.d(TAG,"res"+responseData);
				// Log.d(TAG,"parseCity"+responseData);
				parseAQI(responseData.toString(), parseCity);
			}

		} catch (Exception e) {
			e.printStackTrace();

			Log.d(TAG, "网络故障");
		} finally {
			connection.disconnect();
		}
	}

	public static void parseHeWeather(String response, String parseCity) {
		try {

			// 初始化数据
			SharedPreferences preferences = MyApplication.getContext()
					.getSharedPreferences("iweather", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			Gson gson = new Gson();

			// 解析数据
			JSONObject jsonObject = new JSONObject(response);
			JSONArray HeWeather = jsonObject
					.getJSONArray("HeWeather data service 3.0");
			// 第一层解析
			JSONObject Zero = HeWeather.getJSONObject(0);
			// 第二层解析
			/*
			 * JSONObject aqiData=null; aqiData= Zero .getJSONObject("aqi");
			 */
			JSONObject basicData = Zero.getJSONObject("basic");
			JSONObject nowData = Zero.getJSONObject("now");
			JSONObject suggestionData = Zero.getJSONObject("suggestion");
			JSONArray daily_forecastData = Zero.getJSONArray("daily_forecast");
			String status = Zero.getString("status");

			// 第三层解析
			// aqi_city
			/*
			 * JSONObject aqiCityData= aqiData .getJSONObject("city"); AqiCity
			 * aqiCity=gson.fromJson(aqiCityData.toString(),AqiCity.class);
			 */
			// basic_update
			Basic basic = gson.fromJson(basicData.toString(), Basic.class);
			JSONObject upDateData = basicData.getJSONObject("update");
			UpDate upDate = gson.fromJson(upDateData.toString(), UpDate.class);

			// 获取now消息
			Now now = gson.fromJson(nowData.toString(), Now.class);
			JSONObject windData = nowData.getJSONObject("wind");
			// 获取now_cond_txt数据
			JSONObject cond = nowData.getJSONObject("cond");
			String txt = cond.getString("txt");
			// 获取now_wind
			Wind wind = gson.fromJson(windData.toString(), Wind.class);
			// 获取前四天的对象
			// 第一天
			JSONObject for_zero = daily_forecastData.getJSONObject(0);
			String zero_date = for_zero.getString("date");
			String zero_vis = for_zero.getString("vis");
			String zero_hum = for_zero.getString("hum");
			String zero_pcpn = for_zero.getString("pcpn");
			String zero_pop = for_zero.getString("pop");
			String zero_pres = for_zero.getString("pres");
			JSONObject zero_astro = for_zero.getJSONObject("astro");
			String zero_astro_sr = zero_astro.getString("sr");
			String zero_astro_ss = zero_astro.getString("ss");

			JSONObject zero_cond = for_zero.getJSONObject("cond");
			String zero_cond_txtd = zero_cond.getString("txt_d");
			String zero_cond_txtn = zero_cond.getString("txt_n");

			JSONObject zero_tmp = for_zero.getJSONObject("tmp");
			String zero_tmp_max = zero_tmp.getString("max");
			String zero_tmp_min = zero_tmp.getString("min");

			JSONObject zero_wind = for_zero.getJSONObject("wind");
			String zero_wind_deg = zero_wind.getString("deg");
			String zero_wind_dir = zero_wind.getString("dir");
			String zero_wind_sc = zero_wind.getString("sc");
			String zero_wind_spd = zero_wind.getString("spd");

			JSONObject for_first = daily_forecastData.getJSONObject(1);
			String first_vis = for_first.getString("vis");
			String first_date = for_first.getString("date");
			String first_hum = for_first.getString("hum");
			String first_pcpn = for_first.getString("pcpn");
			String first_pop = for_first.getString("pop");
			String first_pres = for_first.getString("pres");
			JSONObject first_astro = for_first.getJSONObject("astro");
			String first_astro_sr = first_astro.getString("sr");
			String first_astro_ss = first_astro.getString("ss");

			JSONObject first_cond = for_first.getJSONObject("cond");
			String first_cond_txtd = first_cond.getString("txt_d");
			String first_cond_txtn = first_cond.getString("txt_n");

			JSONObject first_tmp = for_first.getJSONObject("tmp");
			String first_tmp_max = first_tmp.getString("max");
			String first_tmp_min = first_tmp.getString("min");

			JSONObject first_wind = for_first.getJSONObject("wind");
			String first_wind_deg = first_wind.getString("deg");
			String first_wind_dir = first_wind.getString("dir");
			String first_wind_sc = first_wind.getString("sc");
			String first_wind_spd = first_wind.getString("spd");

			JSONObject for_second = daily_forecastData.getJSONObject(2);
			String second_vis = for_second.getString("vis");
			String second_date = for_second.getString("date");
			String second_hum = for_second.getString("hum");
			String second_pcpn = for_second.getString("pcpn");
			String second_pop = for_second.getString("pop");
			String second_pres = for_second.getString("pres");

			JSONObject second_astro = for_second.getJSONObject("astro");
			String second_astro_sr = second_astro.getString("sr");
			String second_astro_ss = second_astro.getString("ss");

			JSONObject second_cond = for_second.getJSONObject("cond");
			String second_cond_txtd = second_cond.getString("txt_d");
			String second_cond_txtn = second_cond.getString("txt_n");

			JSONObject second_tmp = for_second.getJSONObject("tmp");
			String second_tmp_max = second_tmp.getString("max");
			String second_tmp_min = second_tmp.getString("min");

			JSONObject second_wind = for_second.getJSONObject("wind");
			String second_wind_deg = second_wind.getString("deg");
			String second_wind_dir = second_wind.getString("dir");
			String second_wind_sc = second_wind.getString("sc");
			String second_wind_spd = second_wind.getString("spd");

			JSONObject for_third = daily_forecastData.getJSONObject(3);
			String third_vis = for_third.getString("vis");
			String third_date = for_third.getString("date");
			String third_hum = for_third.getString("hum");
			String third_pcpn = for_third.getString("pcpn");
			String third_pop = for_third.getString("pop");
			String third_pres = for_third.getString("pres");
			JSONObject third_astro = for_third.getJSONObject("astro");
			String third_astro_sr = third_astro.getString("sr");
			String third_astro_ss = third_astro.getString("ss");

			JSONObject third_cond = for_third.getJSONObject("cond");
			String third_cond_txtd = third_cond.getString("txt_d");
			String third_cond_txtn = third_cond.getString("txt_n");

			JSONObject third_tmp = for_third.getJSONObject("tmp");
			String third_tmp_max = third_tmp.getString("max");
			String third_tmp_min = third_tmp.getString("min");

			JSONObject third_wind = for_third.getJSONObject("wind");
			String third_wind_deg = third_wind.getString("deg");
			String third_wind_dir = third_wind.getString("dir");
			String third_wind_sc = third_wind.getString("sc");
			String third_wind_spd = third_wind.getString("spd");

			// 解析suggestion
			JSONObject comf = suggestionData.getJSONObject("comf");
			String comf_brf = comf.getString("brf");
			String comf_txt = comf.getString("txt");

			JSONObject cw = suggestionData.getJSONObject("cw");
			String cw_brf = cw.getString("brf");
			String cw_txt = cw.getString("txt");

			JSONObject drsg = suggestionData.getJSONObject("drsg");
			String drsg_brf = drsg.getString("brf");
			String drsg_txt = drsg.getString("txt");

			JSONObject flu = suggestionData.getJSONObject("flu");
			String flu_brf = flu.getString("brf");
			String flu_txt = comf.getString("txt");

			JSONObject sport = suggestionData.getJSONObject("sport");
			String sport_brf = sport.getString("brf");
			String sport_txt = sport.getString("txt");

			JSONObject trav = suggestionData.getJSONObject("trav");
			String trav_brf = trav.getString("brf");
			String trav_txt = trav.getString("txt");

			JSONObject uv = suggestionData.getJSONObject("uv");
			String uv_brf = uv.getString("brf");
			String uv_txt = uv.getString("txt");

			// 保存aqi_city数据
			/*
			 * editor.putString(parseCity+"_aqi_city_aqi", aqiCity.aqi);
			 * editor.putString(parseCity+"_aqi_city_co", aqiCity.co);
			 * editor.putString(parseCity+"_aqi_city_no2",aqiCity.no2 );
			 * editor.putString(parseCity+"_aqi_city_o3", aqiCity.o3);
			 * editor.putString(parseCity+"_aqi_city_pm10", aqiCity.pm10);
			 * editor.putString(parseCity+"_aqi_city_pm25", aqiCity.pm25);
			 * editor.putString(parseCity+"_aqi_city_qlty",aqiCity.qlty );
			 * editor.putString(parseCity+"_aqi_city_so2",aqiCity.so2 );
			 */

			// 保存basic数据
			editor.putString(parseCity + "_basic_city", basic.city);
			editor.putString(parseCity + "_basic_cnty", basic.cnty);
			editor.putString(parseCity + "_basic_id", basic.id);
			editor.putString(parseCity + "_basic_lat", basic.lat);
			editor.putString(parseCity + "_basic_lon", basic.lon);
			// 保存basic_update
			editor.putString(parseCity + "_basic_update_loc", upDate.loc);
			editor.putString(parseCity + "_basic_update_utc", upDate.utc);

			// 保存now数据
			editor.putString(parseCity + "_now_fl", now.fl);
			editor.putString(parseCity + "_now_hum", now.hum);
			editor.putString(parseCity + "_now_pcpn", now.pcpn);
			editor.putString(parseCity + "_now_tmp", now.tmp);
			editor.putString(parseCity + "_now_vis", now.vis);
			editor.putString(parseCity + "_now_pres", now.pres);
			editor.putString(parseCity + "_now_cond_txt", txt);
			// 保存now_wind数据
			editor.putString(parseCity + "_now_wind_deg", wind.deg);
			editor.putString(parseCity + "_now_wind_dir", wind.dir);
			editor.putString(parseCity + "_now_wind_sc", wind.sc);
			editor.putString(parseCity + "_now_wind_spd", wind.spd);
			// 保存未来三天预测信息
			// 第zero天
			editor.putString(parseCity + "__forecast_zero_astro_sr",
					zero_astro_sr);
			editor.putString(parseCity + "__forecast_zero_astro_ss",
					zero_astro_ss);
			editor.putString(parseCity + "__forecast_zero_cond_txtd",
					zero_cond_txtd);
			editor.putString(parseCity + "__forecast_zero_cond_txtn",
					zero_cond_txtn);
			editor.putString(parseCity + "__forecast_zero_tmp_max",
					zero_tmp_max);
			editor.putString(parseCity + "__forecast_zero_tmp_min",
					zero_tmp_min);
			editor.putString(parseCity + "__forecast_zero_wind_deg",
					zero_wind_deg);
			editor.putString(parseCity + "__forecast_zero_wind_dir",
					zero_wind_dir);
			editor.putString(parseCity + "__forecast_zero_wind_sc",
					zero_wind_sc);
			editor.putString(parseCity + "__forecast_zero_wind_spd",
					zero_wind_spd);
			editor.putString(parseCity + "__forecast_zero_date", zero_date);
			editor.putString(parseCity + "__forecast_zero_hum", zero_hum);
			editor.putString(parseCity + "__forecast_zero_pcpn", zero_pcpn);
			editor.putString(parseCity + "__forecast_zero_pop", zero_pop);
			editor.putString(parseCity + "__forecast_zero_pres", zero_pres);
			editor.putString(parseCity + "__forecast_zero_vis", zero_vis);
			// 第first天
			editor.putString(parseCity + "__forecast_first_astro_sr",
					first_astro_sr);
			editor.putString(parseCity + "__forecast_first_astro_ss",
					first_astro_ss);
			editor.putString(parseCity + "__forecast_first_cond_txtd",
					first_cond_txtd);
			editor.putString(parseCity + "__forecast_first_cond_txtn",
					first_cond_txtn);
			editor.putString(parseCity + "__forecast_first_tmp_max",
					first_tmp_max);
			editor.putString(parseCity + "__forecast_first_tmp_min",
					first_tmp_min);
			editor.putString(parseCity + "__forecast_first_wind_deg",
					first_wind_deg);
			editor.putString(parseCity + "__forecast_first_wind_dir",
					first_wind_dir);
			editor.putString(parseCity + "__forecast_first_wind_sc",
					first_wind_sc);
			editor.putString(parseCity + "__forecast_first_wind_spd",
					first_wind_spd);
			editor.putString(parseCity + "__forecast_first_date", first_date);
			editor.putString(parseCity + "__forecast_first_hum", first_hum);
			editor.putString(parseCity + "__forecast_first_pcpn", first_pcpn);
			editor.putString(parseCity + "__forecast_first_pop", first_pop);
			editor.putString(parseCity + "__forecast_first_pres", first_pres);
			editor.putString(parseCity + "__forecast_first_vis", first_vis);
			// 第second天
			editor.putString(parseCity + "__forecast_second_astro_sr",
					second_astro_sr);
			editor.putString(parseCity + "__forecast_second_astro_ss",
					second_astro_ss);
			editor.putString(parseCity + "__forecast_second_cond_txtd",
					second_cond_txtd);
			editor.putString(parseCity + "__forecast_second_cond_txtn",
					second_cond_txtn);
			editor.putString(parseCity + "__forecast_second_tmp_max",
					second_tmp_max);
			editor.putString(parseCity + "__forecast_second_tmp_min",
					second_tmp_min);
			editor.putString(parseCity + "__forecast_second_wind_deg",
					second_wind_deg);
			editor.putString(parseCity + "__forecast_second_wind_dir",
					second_wind_dir);
			editor.putString(parseCity + "__forecast_second_wind_sc",
					second_wind_sc);
			editor.putString(parseCity + "__forecast_second_wind_spd",
					second_wind_spd);
			editor.putString(parseCity + "__forecast_second_date", second_date);
			editor.putString(parseCity + "__forecast_second_hum", second_hum);
			editor.putString(parseCity + "__forecast_second_pcpn", second_pcpn);
			editor.putString(parseCity + "__forecast_second_pop", second_pop);
			editor.putString(parseCity + "__forecast_second_pres", second_pres);
			editor.putString(parseCity + "__forecast_second_vis", second_vis);
			// 第third天
			editor.putString(parseCity + "__forecast_third_astro_sr",
					third_astro_sr);
			editor.putString(parseCity + "__forecast_third_astro_ss",
					third_astro_ss);
			editor.putString(parseCity + "__forecast_third_cond_txtd",
					third_cond_txtd);
			editor.putString(parseCity + "__forecast_third_cond_txtn",
					third_cond_txtn);
			editor.putString(parseCity + "__forecast_third_tmp_max",
					third_tmp_max);
			editor.putString(parseCity + "__forecast_third_tmp_min",
					third_tmp_min);
			editor.putString(parseCity + "__forecast_third_wind_deg",
					third_wind_deg);
			editor.putString(parseCity + "__forecast_third_wind_dir",
					third_wind_dir);
			editor.putString(parseCity + "__forecast_third_wind_sc",
					third_wind_sc);
			editor.putString(parseCity + "__forecast_third_wind_spd",
					third_wind_spd);
			editor.putString(parseCity + "__forecast_third_date", third_date);
			editor.putString(parseCity + "__forecast_third_hum", third_hum);
			editor.putString(parseCity + "__forecast_third_pcpn", third_pcpn);
			editor.putString(parseCity + "__forecast_third_pop", third_pop);
			editor.putString(parseCity + "__forecast_third_pres", third_pres);
			editor.putString(parseCity + "__forecast_third_vis", third_vis);

			// 保存sugesstion数据
			editor.putString(parseCity + "_suggestion_comf_brf", comf_brf);
			editor.putString(parseCity + "_suggestion_comf_txt", comf_txt);
			editor.putString(parseCity + "_suggestion_cw_brf", cw_brf);
			editor.putString(parseCity + "_suggestion_cw_txt", cw_txt);
			editor.putString(parseCity + "_suggestion_drsg_brf", drsg_brf);
			editor.putString(parseCity + "_suggestion_drsg_txt", drsg_txt);
			editor.putString(parseCity + "_suggestion_flu_brf", flu_brf);
			editor.putString(parseCity + "_suggestion_flu_txt", flu_txt);
			editor.putString(parseCity + "_suggestion_sport_brf", sport_brf);
			editor.putString(parseCity + "_suggestion_sport_txt", sport_txt);
			editor.putString(parseCity + "_suggestion_trav_brf", trav_brf);
			editor.putString(parseCity + "_suggestion_trav_txt", trav_txt);
			editor.putString(parseCity + "_suggestion_uv_brf", uv_brf);
			editor.putString(parseCity + "_suggestion_uv_txt", uv_txt);

			// 存入刷新时间
			editor.putLong(parseCity + "_refresh_time",
					System.currentTimeMillis());
			editor.putBoolean(parseCity + "_isHasData", true);
			editor.putString(parseCity + "_status", status);
			//
			editor.commit();
			// 检测信息是否保存
			/*
			 * Log.d(TAG,"status"+status);
			 * Log.d(TAG,"aqi"+preferences.getString(parseCity+"_aqi_city_aqi",
			 * null));
			 * Log.d(TAG,"basic_city"+preferences.getString(parseCity+"_basic_city"
			 * , null));
			 * Log.d(TAG,"utc"+preferences.getString(parseCity+"_basic_update_utc"
			 * , null));
			 * Log.d(TAG,"now.fl"+preferences.getString(parseCity+"_now_fl",
			 * null));
			 * Log.d(TAG,"wind.dir"+preferences.getString(parseCity+"_now_wind_dir"
			 * , null));
			 * Log.d(TAG,"suggestion.uv_text"+preferences.getString(parseCity
			 * +"_suggestion_uv_txt", null));
			 * Log.d(TAG,"zero.t"+preferences.getString
			 * (parseCity+"__forecast_zero_pres", null));
			 * Log.d(TAG,"first.t"+preferences
			 * .getString(parseCity+"__forecast_first_pres", null));
			 * Log.d(TAG,"first.t"
			 * +preferences.getString(parseCity+"__forecast_first_wind_sc"
			 * ,null));
			 * Log.d(TAG,"third.t"+"/n"+first_tmp_min+"/"+first_wind_spd
			 * +"/"+first_wind_dir);
			 */
			Log.d(TAG, preferences.getString(parseCity + "_now_cond_txt", null));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void parseAQI(String response, String parseCity) {
		AlarmList alarmList =null;
		try {

			/*
			 * Gson gson = new Gson(); // 解析数据 JSONObject jsonObject = new
			 * JSONObject(response); JSONArray HeWeather = jsonObject
			 * .getJSONArray("HeWeather data service 3.0"); // 第一层解析 JSONObject
			 * Zero = HeWeather.getJSONObject(0); JSONObject aqiData =
			 * Zero.getJSONObject("aqi"); JSONObject aqiCityData =
			 * aqiData.getJSONObject("city"); AqiCity aqiCity =
			 * gson.fromJson(aqiCityData.toString(), AqiCity.class);
			 */
			SharedPreferences preferences = MyApplication.getContext()
					.getSharedPreferences("iweather", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			// 解析数据
			JSONObject jsonObject = new JSONObject(response);
			JSONObject showapi_res_body = jsonObject
					.getJSONObject("showapi_res_body");

			// JSONObject now = showapi_res_body.getJSONObject("now");
			// 第一层解析
			JSONArray alarmListInfo = showapi_res_body
					.getJSONArray("alarmList");
			JSONObject nowInfo = showapi_res_body.getJSONObject("now");
			JSONObject aqiDetailInfo = nowInfo.getJSONObject("aqiDetail");
			// JSONArray alarmListInfo =
			// showapi_res_body.getJSONArray("alarmList");
			Gson gson = new Gson();
			// 获取空气质量和预警对象
			AqiDetail aqiDetail = gson.fromJson(aqiDetailInfo.toString(),
					AqiDetail.class);

			// 保存aqi_city数据
			editor.putString(parseCity + "_aqi_city_aqi", aqiDetail.aqi);
			editor.putString(parseCity + "_aqi_city_co", aqiDetail.co);
			editor.putString(parseCity + "_aqi_city_no2", aqiDetail.no2);
			editor.putString(parseCity + "_aqi_city_o3", aqiDetail.o3);
			editor.putString(parseCity + "_aqi_city_pm10", aqiDetail.pm10);
			editor.putString(parseCity + "_aqi_city_pm25", aqiDetail.pm2_5);
			editor.putString(parseCity + "_aqi_city_qlty", aqiDetail.quality);
			editor.putString(parseCity + "_aqi_city_so2", aqiDetail.so2);
			// 保存alarm_list数据
			// 第二层解析
			
			if (alarmListInfo.length()>=1) {
				//判断是否有预警如果有预警则获取数据，如果没有就不获取数据，否则会报空指针
				JSONObject zero = alarmListInfo.getJSONObject(alarmListInfo.length()-1);
				 alarmList = gson.fromJson(zero.toString(),
						AlarmList.class);
				editor.putString(parseCity + "_aqi_alarm_city", alarmList.city);
				editor.putString(parseCity + "_aqi_alarm_issueContent",
						alarmList.issueContent);
				editor.putString(parseCity + "_aqi_alarm_issueTime",
						alarmList.issueTime);
				editor.putString(parseCity + "_aqi_alarm_province",
						alarmList.province);
				editor.putString(parseCity + "_aqi_alarm_signalLevel",
						alarmList.signalLevel);
				editor.putString(parseCity + "_aqi_alarm_signalType",
						alarmList.signalType);
			}
			//Log.d(TAG,"有预警"+alarmList.city+"\n"+alarmList.issueContent);

			// 保存aqi_city数据
			/*
			 * editor.putString(parseCity + "_aqi_city_aqi", aqiCity.aqi);
			 * editor.putString(parseCity + "_aqi_city_co", aqiCity.co);
			 * editor.putString(parseCity + "_aqi_city_no2", aqiCity.no2);
			 * editor.putString(parseCity + "_aqi_city_o3", aqiCity.o3);
			 * editor.putString(parseCity + "_aqi_city_pm10", aqiCity.pm10);
			 * editor.putString(parseCity + "_aqi_city_pm25", aqiCity.pm25);
			 * editor.putString(parseCity + "_aqi_city_qlty", aqiCity.qlty);
			 * editor.putString(parseCity + "_aqi_city_so2", aqiCity.so2);
			 */
			Log.d(TAG,
					"aqi"
							+ preferences.getString(
									parseCity + "_aqi_city_aqi", null));
			//Log.d(TAG,a"/n"+aqiDetail.quality);
			editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parseXMLData(String response) {
		try {
			db = IWeatherDB.getInstance(MyApplication.getContext());
			City city = new City();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			xmlPullParser.setInput(new StringReader(response));
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName = xmlPullParser.getName();
				String pyName = "";
				String url = "";
				String cityName = "";
				String quName = "";
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if ("d".equalsIgnoreCase(nodeName)) {
						url = xmlPullParser.getAttributeValue(null, "d1");// 记录城市的区域代码
						cityName = xmlPullParser.getAttributeValue(null, "d2");// 记录城市的名称
						pyName = xmlPullParser.getAttributeValue(null, "d3");// 记住城市的拼音
						quName = xmlPullParser.getAttributeValue(null, "d4");// 记住城市的省份

						city.setCityCode(url);
						city.setCityName(cityName);
						city.setCityPyName(pyName);
						city.setCityQuName(quName);
						db.saveCity(city);

					}
					break;
				case XmlPullParser.END_TAG:

					break;
				}
				if (cityName.equalsIgnoreCase("首尔")) {
					Log.d(TAG, "已加载到最后一个城市：" + pyName);
					Log.d(TAG, "解析结束" + System.currentTimeMillis());
					Log.d(TAG, "数据更新完成");
					break;
				} else {
					eventType = xmlPullParser.next();
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getTime() {
		DateFormat sl = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = sl.format(new Date());
		return time;
	};
}
