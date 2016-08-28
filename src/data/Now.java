package data;

import java.io.Serializable;

import android.util.Log;

public class Now implements Serializable{
	
	
	public String temperature;
	public String temperature_time;
	public int pm25;
	public String quality;
	public int maxTem;
	public int minTem;
	
	public String aqi;
	public Object aqiDetail;
	public String sd;
	public String weather;
	public String weather_code;
	public String weather_pic;
	public String wind_direction;
	public String wind_power;
	public String area;
	private String city;
	private String time;
	public String  getQuality(){
		return quality;
		
	}
	public void setQuality(String quality){
		this.quality=quality;
	}
	public int getPM25(){
		return pm25;
	}
	public void setPM25(int pm25){
		 this.pm25=pm25;
	}
	public int getMaxTem(){
		return maxTem;
	}
	public void setMaxTem(int maxTem){
		this.maxTem=maxTem;
	}
	public int getMinTem(){
		return minTem;
	}
	public void setMinTem(int minTem){
		this.minTem=minTem;
	}
	public String getTemperature() {

		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String geTtemperature_time() {
		return temperature_time;
	}

	public void setTemperature_time(String temperature_time) {
		this.temperature_time = temperature_time;
	}

	/*
	 * public String toString(){ //Log.d("哈哈", "解析NOW对象后的数据"+temperature);
	 * return "解析NOW对象后的数据"+temperature; }
	 */
	public String getAqi() {
		return aqi;
	}

	public void setAqi(String aqi) {
		this.aqi = aqi;

	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;

	}

	public String getWeather_pic() {
		return weather_pic;
	}

	public void setWeather_pic(String weather_pic) {
		this.weather_pic = weather_pic;

	}

	public String getWind_direction() {
		return wind_direction;
	}

	public void setWind_direction(String wind_direction) {
		this.wind_direction = wind_direction;

	}

	public String getWind_power() {
		return wind_power;
	}

	public void setWind_power(String wind_power) {
		this.wind_power = wind_power;

	}
	public String getArea() {
		return area;
	}
	
	public void setArea(String area) {
		this.area = area;
		
	}
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
		
	}
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
		
	}

}
