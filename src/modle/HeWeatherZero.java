package modle;

import java.util.ArrayList;

public class HeWeatherZero {
	public Object aqi;
	public Object basic;
	public Object now;
	public Object suggestion;
	public ArrayList<Object> daily_forecast;
	public ArrayList<Object> hourly_forecast;
	public String status;
	
	public String getStatus(){
		return status;
	}
}
