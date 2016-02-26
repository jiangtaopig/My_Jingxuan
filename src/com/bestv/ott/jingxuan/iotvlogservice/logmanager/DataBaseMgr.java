package com.bestv.ott.jingxuan.iotvlogservice.logmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.bestv.ott.jingxuan.iotvlogservice.beans.LogUploadBean;
import com.bestv.ott.jingxuan.iotvlogservice.utils.CommonTool;
import com.bestv.ott.jingxuan.iotvlogservice.utils.utils;

public class DataBaseMgr {
    private static final String DATABASE_NAME = "IOTV_logupload.db";
    private static final int DATABASE_VERSION = 1;
    
    public static final String TABLE_NAME = "iotvunupload_logs";
    public static final String _ID = "_id";
    public static final String VERSION = "version";
    public static final String LOG_TYPE = "logtype";
    public static final String FIRM_VERSION = "firmversion";
    public static final String CURRENT_TIME = "currenttime";
    public static final String TVID = "tvid";
    public static final String USERID = "userid";
    public static final String LOG_PARAMS = "logparams";
    
	private static DataBaseMgr instance = null;

	private Context mContext;
	private DatabaseHelper mDatabase;
	
	public static class LogItem{
		public int id; 
		public int type;
		public String version;
		public String farmversion;
		public String time;
		public String tvID;
		public String userID;
		public String[] params;
	}
	
	public static DataBaseMgr instance(Context context){
		if (instance == null){
			instance = new DataBaseMgr(context);
		}
		
		return instance;
	}
	
	class DataInsertRunnable implements Runnable{
		String[] params;
		int type;
		
		public DataInsertRunnable(int t, String[] p) {
			// TODO Auto-generated constructor stub
			type = t;
			
			if (p != null){
				params = new String[p.length];
				
				for(int i = 0; i < p.length; i++){
					params[i] = p[i];
				}
			}
		}
		
		public void run() {
			// TODO Auto-generated method stub
			insert(null, type, null, null, null, null, params);
		}
	}
	
	private DataBaseMgr(Context context){
		mContext = context;
		mDatabase = new DatabaseHelper(mContext);
	}
	
	public Cursor query(String selection, String[] selectionArgs){
		Cursor c = mDatabase.getReadableDatabase().query(TABLE_NAME, 
				null, 
				selection, 
				selectionArgs,
				null, 
				null,
				"_id asc"
		);
		
		return c;
	}
	
	public long delete(int id){
		return mDatabase.getWritableDatabase().delete(TABLE_NAME, "_id = " + id, null);
	}
	
	private long insert(ContentValues cv){
		return mDatabase.getWritableDatabase().insert(TABLE_NAME, null, cv);
	}
	
	private long insert(String version, int type, String firmwareVersion, String currentTime, String tvID,
			String userID, String[] params){
		ContentValues cv = new ContentValues();
		cv.put(LOG_TYPE, type);
		cv.put(LOG_PARAMS, toParamsString(params));
		
		cv.put(VERSION, (version == null ? utils.LOG_UPLOAD_VERSION : version));
		cv.put(FIRM_VERSION,  LogUploadMgr.getFirmwareVersion());
		
		cv.put(CURRENT_TIME, (currentTime == null ? CommonTool.yearEndSecond() : currentTime));
		cv.put(TVID, "");
		cv.put(USERID, "");
		
		if (utils.checkDebug()){
			utils.saveReceviedData(toParamsString(version, type, firmwareVersion, currentTime, tvID, userID, params));
		}
		
		long i = insert(cv);
		
		Log.d("harish4", "inset type = " + type + " return = " + i);
		
		Log.d(LogUploadMgr.TAG, "current log item insert result = " + i);
		
		//if insert fail, try it again
		int try_count = 5;
		int count = 0;
		while (i == -1){
			if (count >= try_count){
				break;
			}
			
			if (utils.checkDebug()){
			    utils.saveInsertFailedData(toParamsString(version, type, firmwareVersion, currentTime, tvID, userID, params));
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			i = insert(cv);
			count++;
		}
	
		return i;
	}
	
	public void insert(int type, String[] params){	
		//make sure this invoke is not blocked, so lets work thread to insert log data into database
		DataInsertRunnable r = new DataInsertRunnable(type, params);
		WorkHandlerMgr.getInstance().getHandler().post(r);
	}
	
	public static String toParamsString(String[] params){
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				sb.append(LogUploadBean.ensure(params[i])).append(utils.LOG_SEPARATOR);
			}
		}
		return sb.toString();
	}
	
	public static String toParamsString(String version, int type, String firmwareVersion, String currentTime, String tvID,
			String userID, String[] params) {
		StringBuffer sb = new StringBuffer();
		sb.append(version).append(utils.LOG_SEPARATOR).append(type).append(utils.LOG_SEPARATOR)
				.append(LogUploadBean.ensure(firmwareVersion)).append(utils.LOG_SEPARATOR).append(LogUploadBean.ensure(userID));

		// 如果是开/关机日志类型，则保证有TVID
		if (type == LogUploadMgr.TYPE_POWER) {
			sb.append(utils.LOG_SEPARATOR).append(LogUploadBean.ensure(tvID));
		} else {
			if (tvID != null) {
				sb.append(utils.LOG_SEPARATOR).append(LogUploadBean.ensure(tvID));
			}
		}

		sb.append(utils.LOG_SEPARATOR).append(currentTime);

		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				sb.append(utils.LOG_SEPARATOR).append(LogUploadBean.ensure(params[i]));
			}
		}
		return sb.toString();
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
					+ _ID + " INTEGER PRIMARY KEY,"
					+ VERSION + " TEXT,"
					+ LOG_TYPE + " INTEGER,"
					+ FIRM_VERSION + " TEXT,"
					+ CURRENT_TIME + " TEXT,"
					+ TVID + " TEXT,"
					+ USERID + " TEXT,"
					+ LOG_PARAMS + " TEXT"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}
	}
}
