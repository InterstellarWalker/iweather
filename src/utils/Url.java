package utils;


public class Url {
	public static String locationNameURL;
	public static String time;
	public static String city;
	public static String cityURL = "http://mobile.weather.com.cn/js/citylist.xml";
	public static String locationURL;
	public static String hefengURL;
	public static String aqiURL;

	public static String getWeatherFromLNUrl(String chooseCityID) {

		time = HttpUtil.getTime();
		locationNameURL = "https://route.showapi.com/9-2?areaid="
				+ chooseCityID
				+ "&needAlarm=1&needIndex=1&showapi_appid=16767&showapi_timestamp="
				+ time + "&showapi_sign=7c0323a05d634d11a20645cb22a69589";

		return locationNameURL;
	}

	public static String getHeFengUrl(String chooseCityID) {

		hefengURL = "https://api.heweather.com/x3/weather?cityid=CN"
				+ chooseCityID + "&key=afd4890c6e064f91ae5154c18a690b62";

		return hefengURL;
	}

	public static String getLocationURL(Double longitude, Double latitude) {
		time = HttpUtil.getTime();
		locationURL = "https://route.showapi.com/9-5?from=5&lat="
				+ latitude
				+ "&lng="
				+ longitude
				+ "&need3HourForcast=0&needAlarm=0&needHourData=0&needIndex=0&needMoreDay=0&showapi_appid=16767&showapi_timestamp="
				+ time + "&showapi_sign=7c0323a05d634d11a20645cb22a69589";
		return locationURL;

	}

	public static String getAQIURL(String chooseCityID) {
		time = HttpUtil.getTime();
		//http://www.pm25.in/api/querys/pm10.json?city=%E5%8C%97%E4%BA%AC&token=5j1znBVAsnSf5xQyNQyq&avg
		aqiURL = "https://api.heweather.com/x3/weather?cityid=CN"
				+ chooseCityID + "&key=afd4890c6e064f91ae5154c18a690b62";
		return aqiURL;
	}
}
