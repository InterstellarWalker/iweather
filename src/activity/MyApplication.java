package activity;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
	public static Context context;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		context=getApplicationContext();
	}
	public static Context getContext(){
		return context;
	}
}
