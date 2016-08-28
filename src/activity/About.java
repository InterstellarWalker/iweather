package activity;



import com.example.iweather.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;



public class About extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_about);
		getActionBar().setDisplayHomeAsUpEnabled(true);// 设置返回导航
		getActionBar().setDisplayShowHomeEnabled(false);// 取消图标
	}
	//导航监听
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.city_show:

			default:
				break;
			}
			return super.onOptionsItemSelected(item);
		}
}
