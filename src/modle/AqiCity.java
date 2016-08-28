package modle;

import android.util.Log;

public class AqiCity {
	public String pm25;
	public String pm10;
	public String so2;
	public String no2;
	public String o3;
	public String aqi;
	public String co;

	public String qlty;

	public String getPM25() {
		return pm25;
	}

	public String getPM10() {
		return pm10;
	}

	public String getSO2() {
		return so2;
	}

	public String getO3() {
		return o3;
	}

	public String getNO2() {
		return no2;
	}

	public String getCO() {
		return co;
	}

	public String getQuality() {
		return qlty;
	}

	public String getAqi() {
		return aqi;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		Log.d("¹þ¹þ",""+aqi);
		return super.toString();
	}
}
