package view;




import com.example.iweather.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;

public class CustomSearchView extends SearchView {
	public CustomSearchView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView();
	}

	public CustomSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		//hint����
		this.setQueryHint("�������������(����)");
		//Ĭ��չ��
		this.setIconifiedByDefault(false);
		//��ȡ������Id
		int searchPlateId=this.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		EditText searchPlate=(EditText) this.findViewById(searchPlateId);
		searchPlate.setBackground(null);
		//searchPlate.setLines(0);
		searchPlate.setBackgroundResource(android.R.color.darker_gray);
		//searchPlate.setBackground(R.drawable.)
		//����������ɫ
		//��ȡ����ͼ�겢���Լ���ͼ�����
		int search_mag_icon_id=this.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
		ImageView search_icon=(ImageView) this.findViewById(search_mag_icon_id);
		
		search_icon.setImageResource(R.drawable.ic_action_search);
		
	}

}
