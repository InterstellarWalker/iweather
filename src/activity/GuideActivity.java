package activity;



import service.AutoUpdateService;
import service.DCServiceIntent;

import com.example.iweather.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

import android.widget.ImageView;

import android.widget.TextView;
public class GuideActivity extends Activity{
	private static final String TAG = " GuideActivity ";
	@ViewInject(R.id.sp_icon)
	ImageView icon;
	@ViewInject(R.id.sp_title)
	TextView title;
	@ViewInject(R.id.sp_txt)
	TextView txt;
	@ViewInject(R.id.layout_sp)
	ImageView layout_sp;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_guide);
		preferences = getSharedPreferences("iweather", MODE_PRIVATE);
		Boolean switchActivity=preferences.getBoolean("splash",false);
		int num=preferences.getInt("updateTime", 0);
		if(num!=0){
			Intent in=new Intent(GuideActivity.this,AutoUpdateService.class);
			in.putExtra("updateTime",num);
			//startService(in);
		}
		//Log.d(TAG,"swicthActivity"+switchActivity);
		//Log.d(TAG,"isDownCity"+preferences.getBoolean("isDownCity", true));
		if (isDownCity()&&switchActivity) {
			Log.d(TAG,"直接进入下个页面");
			startActivity(new Intent(GuideActivity.this,MainActivity.class));
			finish();//关闭闪屏页
			
		}else{
			Log.d(TAG,"开启动画后直接进入下个页面");
			ViewUtils.inject(this);
			startAnimation();//开启闪屏页
		}
	}
	
	private void startAnimation() {
		// TODO Auto-generated method stub
		// 动画集合
				//AnimationSet set = new AnimationSet(false);

				// 旋转动画
				RotateAnimation rotate = new RotateAnimation(0, 360,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
						0.5f);
				rotate.setDuration(2000);// 动画时间
				rotate.setFillAfter(true);// 保持动画状态

				// 缩放动画
				ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
						0.5f);
				scale.setDuration(2000);// 动画时间
				scale.setFillAfter(true);// 保持动画状态

				// 渐变动画
				AlphaAnimation alpha = new AlphaAnimation(0, 1);
				alpha.setDuration(2000);// 动画时间
				alpha.setFillAfter(true);// 保持动画状态
				
				rotate.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						startActivity(new Intent(GuideActivity.this,MainActivity.class));
						finish();
					}
				});
				icon.startAnimation(alpha);
				title.startAnimation(scale);
				txt.startAnimation(scale);
				layout_sp.startAnimation(rotate);
				
				
	}

	private Boolean isDownCity(){
		
		if (preferences.getBoolean("isDownCity", true)) {
			Intent in=new Intent(this,DCServiceIntent.class);
			Log.d(TAG,"是否下载城市"+preferences.getBoolean("isDownCity", true));
			Log.d(TAG,"开始下载城市11111");
			startService(in);
			return false;
		}
		
		return true;
	}
	
}
