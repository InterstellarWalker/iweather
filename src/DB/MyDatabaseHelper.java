package DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

	
	public static final String CREATE_CITY = "create table City("
			+ "_id integer primary key autoincrement," + "city_name text,"
			+ "city_pyName text," + "city_quName text," + "city_code text)";
	public static final String CREATE_CITYSET = "create table CitySet("
			+ "_id integer primary key autoincrement," + "city_saveName text)";
	public static final String CREATE_CITYINFO = "create table CityInfo("
			+ "_id integer primary key autoincrement," + "c1 text,"
			+ "c2 text," + "c3 text," + "c4 text," + "c5 text," + "c6 text,"
			+ "c7 text," + "c8 text," + "c9 text," + "c10 text," + "c11 text,"
			+ "c12 text," + "c15 text," + "c16 text," + "c17 text,"
			+ "latitude text," + "longitude text)";
	

	public MyDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		db.execSQL(CREATE_CITY);
		
		db.execSQL(CREATE_CITYSET);
		db.execSQL(CREATE_CITYINFO);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
