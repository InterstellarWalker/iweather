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
			Log.d(TAG,"ֱ�ӽ����¸�ҳ��");
			startActivity(new Intent(GuideActivity.this,MainActivity.class));
			finish();//�ر�����ҳ
			
		}else{
			Log.d(TAG,"����������ֱ�ӽ����¸�ҳ��");
			ViewUtils.inject(this);
			startAnimation();//��������ҳ
		}
	}
	
	private void startAnimation() {
		// TODO Auto-generated method stub
		// ��������
				//AnimationSet set = new AnimationSet(false);

				// ��ת����
				RotateAnimation rotate = new RotateAnimation(0, 360,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
						0.5f);
				rotate.setDuration(2000);// ����ʱ��
				rotate.setFillAfter(true);// ���ֶ���״̬

				// ���Ŷ���
				ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
						0.5f);
				scale.setDuration(2000);// ����ʱ��
				scale.setFillAfter(true);// ���ֶ���״̬

				// ���䶯��
				AlphaAnimation alpha = new AlphaAnimation(0, 1);
				alpha.setDuration(2000);// ����ʱ��
				alpha.setFillAfter(true);// ���ֶ���״̬
				
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
			Log.d(TAG,"�Ƿ����س���"+preferences.getBoolean("isDownCity", true));
			Log.d(TAG,"��ʼ���س���11111");
			startService(in);
			return false;
		}
		
		return true;
	}
	
}
