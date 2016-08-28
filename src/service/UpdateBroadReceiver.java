package service;

import activity.MyApplication;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateBroadReceiver extends BroadcastReceiver{
	int i=0;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("UpdateBroadReceiver", "我被执行了");
		MyApplication.getContext().startService(new Intent(MyApplication.getContext(),AutoUpdateService.class));
	}

}
