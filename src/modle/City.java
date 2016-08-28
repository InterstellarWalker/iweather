package modle;



public class City {

	protected static final String TAG = null;
	private String cityName;
	private String cityCode;
	private String CityQuName;
	private String cityPyName;
	private int cityNumber;
	private String checkedName;

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCityPyName() {
		return cityPyName;
	}

	public void setCityPyName(String cityPyName) {
		this.cityPyName = cityPyName;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCityQuName() {
		return CityQuName;
	}

	public void setCityQuName(String CityQuName) {
		this.CityQuName = CityQuName;
	}
	public int getCityNumber() {
		return cityNumber;
	}
	
	public void setCityNumber(int cityNumber) {
		this.cityNumber = cityNumber;
	}
	public String getCheckedCity() {
		return checkedName;
	}
	
	public void setCheckedCity(String checkedName) {
		this.checkedName = checkedName;
	}
	

}