package com.bestv.ott.jingxuan.iotvlogservice.logmanager;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.bestv.ott.jingxuan.iotvlogservice.beans.LogUploadBean;
import com.bestv.ott.jingxuan.iotvlogservice.logmanager.DataBaseMgr.LogItem;
import com.bestv.ott.jingxuan.iotvlogservice.utils.utils;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.net.ModuleServiceMgr;

public class IOTV_LogUploadService extends Service {
	public static final String EXTRA_SYSLOG = "syslogupload";
	
	static final boolean DELETE_AFTER_POSTFAILED = true;
	
	static final int RETRY_INTERVAL = 5000;
	static final int CHECK_STATUS = 98;
	static final int UPLOAD_LOGS = 99;
	
	static final int UPLOAD_SYSLOG_FILES = 100;
	static final int UPLOAD_SYSLOG_INTERVAL = 1000 * 60 * 30; //interval upload time
	
	int MAX_ITEMS_BUFFERING = 20;
	int MAX_ITEMS_DOWNLOAD_DETAIL = 20;
	
	UploadHandler mHandler = null;
	
	LogUploadMgr mLoguploadMgr;
	
	class UploadHandler extends Handler{
		public UploadHandler(Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			if(msg.what == CHECK_STATUS){
				if (LogUploadMgr.isUserprofileEmpty()){
					if (mLoguploadMgr == null){
						mLoguploadMgr = LogUploadMgr.getInstance(IOTV_LogUploadService.this);
					}else{
						mLoguploadMgr.initUserprofile();
						
						//after check status finish
	                    //try to get log service from bestv module server
						if (!LogUploadMgr.isUserprofileEmpty()){
							ModuleServiceMgr.getInstance(getBaseContext()).generateServiceSync();
						}
					}
					
					Log.d(LogUploadMgr.TAG, "check status userprfile = " + LogUploadMgr.userprofile);
					sendEmptyMessageDelayed(CHECK_STATUS, RETRY_INTERVAL);
				}else{
					Log.d(LogUploadMgr.TAG, "check status success!!, userprfile = " + LogUploadMgr.userprofile);
					sendEmptyMessage(UPLOAD_LOGS);
				}
			}else if (msg.what == UPLOAD_LOGS){
				if (LogUploadMgr.isUserprofileEmpty()){
					sendEmptyMessageDelayed(CHECK_STATUS, RETRY_INTERVAL);
				}else{
					Log.d(LogUploadMgr.TAG, "check status success!!, start loading.");
					upload_logs();
				}
			}
		}
		
		void upload_logs(){
			Cursor cursor = null;
			
			try{
				//get log without buffering and download log from database
				//then send these logs by http get
				ArrayList<LogItem> items = new ArrayList<LogItem>();
				boolean isFailed = false;
				String selection = "logtype!=? and logtype!=?";
				cursor = DataBaseMgr.instance(IOTV_LogUploadService.this).query(selection, 
						             new String[]{LogUploadMgr.TYPE_BUFFERING + "", LogUploadMgr.TYPE_DOWNLOAD_DETAIL + ""});
				
				if (cursor != null && cursor.getCount() > 0){
					Log.d(LogUploadMgr.TAG, "cursor 1 = " + cursor.getCount());
					while(cursor.moveToNext()){
						LogItem temp = new LogItem();
						temp.id = cursor.getInt(cursor.getColumnIndex(DataBaseMgr._ID));
						temp.type = cursor.getInt(cursor.getColumnIndex(DataBaseMgr.LOG_TYPE));
						temp.version = cursor.getString(cursor.getColumnIndex(DataBaseMgr.VERSION));
						temp.farmversion = cursor.getString(cursor.getColumnIndex(DataBaseMgr.FIRM_VERSION));
						temp.time = cursor.getString(cursor.getColumnIndex(DataBaseMgr.CURRENT_TIME));
						temp.tvID = LogUploadMgr.getTVID();
						temp.userID = LogUploadMgr.userprofile.getUserID();
						temp.params = cursor.getString(cursor.getColumnIndex(DataBaseMgr.LOG_PARAMS)).split("\\" + utils.LOG_SEPARATOR);
						
						items.add(temp);
						String p = "";
						for (String s : temp.params){
							p += "+" + s;
						}
						Log.d(LogUploadMgr.TAG, "find cursor id = " + temp.id + " params = " + p);
					}
					
					cursor.close();
					
					for(LogItem item : items){
						boolean succ = LogUploadMgr.getInstance(IOTV_LogUploadService.this).upload(item);
						
						if (succ){
							//send success, delete this item in database
							Log.d(LogUploadMgr.TAG, "upload success, delete current item in database which id = " + item.id);
							DataBaseMgr.instance(IOTV_LogUploadService.this).delete(item.id);
						}else{
							//send failed, try later
							isFailed = true;
							break;
						}
					}
				}
				
				//then send buffering log by http post if need.
				items.clear();
				selection = "logtype=?";
				cursor = DataBaseMgr.instance(IOTV_LogUploadService.this).query(selection, 
						             new String[]{LogUploadMgr.TYPE_BUFFERING + ""});
				Log.d(LogUploadMgr.TAG, "cursor 2 = " + cursor.getCount());
				if (cursor != null){
					//send logs if they are greater than max values
					if (cursor.getCount() >= MAX_ITEMS_BUFFERING){
						Log.d(LogUploadMgr.TAG, "cursor 2 = " + cursor.getCount());
						StringBuilder strBuilder = new StringBuilder();
						
						while(cursor.moveToNext()){
							LogItem temp = new LogItem();
							temp.id = cursor.getInt(cursor.getColumnIndex(DataBaseMgr._ID));
							temp.type = cursor.getInt(cursor.getColumnIndex(DataBaseMgr.LOG_TYPE));
							temp.version = cursor.getString(cursor.getColumnIndex(DataBaseMgr.VERSION));
							temp.farmversion = cursor.getString(cursor.getColumnIndex(DataBaseMgr.FIRM_VERSION));
							temp.time = cursor.getString(cursor.getColumnIndex(DataBaseMgr.CURRENT_TIME));
							temp.tvID = LogUploadMgr.getTVID();
							temp.userID = LogUploadMgr.userprofile.getUserID();
							temp.params = cursor.getString(cursor.getColumnIndex(DataBaseMgr.LOG_PARAMS)).split("\\" + utils.LOG_SEPARATOR);
							
							items.add(temp);
							
							LogUploadBean b = LogUploadMgr.convertToBean(temp);
							
							if (b != null){
								strBuilder.append(b.toParamString());
								strBuilder.append(utils.LOG_GROUP_SEPARATOR);
							}
						}
						
						boolean succ = LogUploadMgr.getInstance(IOTV_LogUploadService.this).uploadPOST(LogUploadMgr.TYPE_BUFFERING, strBuilder.toString());
						if (succ || DELETE_AFTER_POSTFAILED){
							//send success, delete this item in database
							Log.d(LogUploadMgr.TAG, "delete type buffering data!!!!");
							for (LogItem i : items){
								DataBaseMgr.instance(IOTV_LogUploadService.this).delete(i.id);
							}
						}else{
							//send failed, try later
							isFailed = true;
						}
					}
					
					cursor.close();
				}
				
				//then send buffering log by http post if need.
				items.clear();
				selection = "logtype=?";
				cursor = DataBaseMgr.instance(IOTV_LogUploadService.this).query(selection, 
						             new String[]{LogUploadMgr.TYPE_DOWNLOAD_DETAIL + ""});
				Log.d(LogUploadMgr.TAG, "cursor 3 = " + cursor.getCount());
				if (cursor != null){
					//send logs if they are greater than max values
					if (cursor.getCount() >= MAX_ITEMS_DOWNLOAD_DETAIL){
						StringBuilder strBuilder = new StringBuilder();
						
						while(cursor.moveToNext()){
							LogItem temp = new LogItem();
							temp.id = cursor.getInt(cursor.getColumnIndex(DataBaseMgr._ID));
							temp.type = cursor.getInt(cursor.getColumnIndex(DataBaseMgr.LOG_TYPE));
							temp.version = cursor.getString(cursor.getColumnIndex(DataBaseMgr.VERSION));
							temp.farmversion = cursor.getString(cursor.getColumnIndex(DataBaseMgr.FIRM_VERSION));
							temp.time = cursor.getString(cursor.getColumnIndex(DataBaseMgr.CURRENT_TIME));
							temp.tvID = LogUploadMgr.getTVID();
							temp.userID = LogUploadMgr.userprofile.getUserID();
							temp.params = cursor.getString(cursor.getColumnIndex(DataBaseMgr.LOG_PARAMS)).split("\\" + utils.LOG_SEPARATOR);
							
							items.add(temp);
							
							LogUploadBean b = LogUploadMgr.convertToBean(temp);
							
							if (b != null){
								strBuilder.append(b.toParamString());
								strBuilder.append(utils.LOG_GROUP_SEPARATOR);
							}
						}
						
						boolean succ = LogUploadMgr.getInstance(IOTV_LogUploadService.this).uploadPOST(LogUploadMgr.TYPE_DOWNLOAD_DETAIL, strBuilder.toString());
						if (succ || DELETE_AFTER_POSTFAILED){
							//send success, delete this item in database
							Log.d(LogUploadMgr.TAG, "delete type download detail data!!!!");
							for (LogItem i : items){
								DataBaseMgr.instance(IOTV_LogUploadService.this).delete(i.id);
							}
						}else{
							//send failed, try later
							isFailed = true;
						}
					}
					
					cursor.close();
				}
				
				if (isFailed){
					if (mHandler != null){
						Log.d(LogUploadMgr.TAG, "upload failed, try do it later.");
						//mHandler.sendEmptyMessageDelayed(CHECK_STATUS, RETRY_INTERVAL);
					}
				}
			}finally{
				if (cursor != null && !cursor.isClosed()){
					cursor.close();
				}
			}
		}
	}
		
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		String temp = OttContextMgr.getInstance().getConfig(OttContextMgr.BESTV_CONFIG_FILE, OttContextMgr.LOG_UPLOAD_MAX_BUFFERINGS);
		if (!temp.equals("")){
			MAX_ITEMS_BUFFERING = Integer.parseInt(temp);
		}
		
		temp = OttContextMgr.getInstance().getConfig(OttContextMgr.BESTV_CONFIG_FILE, OttContextMgr.LOG_UPLOAD_MAX_DOWNLOADS);
		if (!temp.equals("")){
			MAX_ITEMS_DOWNLOAD_DETAIL = Integer.parseInt(temp);
		}
		
		Log.d(LogUploadMgr.TAG, "MAX_ITEMS_BUFFERING time = " + MAX_ITEMS_BUFFERING + " MAX_ITEMS_DOWNLOAD_DETAIL = " + MAX_ITEMS_DOWNLOAD_DETAIL);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(LogUploadMgr.TAG, "Logupload service started");
		if (mHandler == null){
			mHandler = new UploadHandler(WorkHandlerMgr.getInstance().getMainLooper());
		}
		
		if (mHandler != null){
			Log.d(LogUploadMgr.TAG, "Logupload service start check status");
			mHandler.sendEmptyMessage(CHECK_STATUS);
			
			boolean t = intent.getBooleanExtra(EXTRA_SYSLOG, false);
			//if upload now
			if (t){
				//if message has sent, remove it
				if (mHandler.hasMessages(UPLOAD_SYSLOG_FILES)){
					mHandler.removeMessages(UPLOAD_SYSLOG_FILES);
				}
				
				//then, send no delay message now
				Log.d(LogUploadMgr.TAG, "send upload syslog message now");
				mHandler.sendEmptyMessage(UPLOAD_SYSLOG_FILES);
			}else{
				//send delay, so we just check message is exist or not
				//if not, send delay message now
				if (!mHandler.hasMessages(UPLOAD_SYSLOG_FILES)){
					Log.d(LogUploadMgr.TAG, "send delay upload syslog msg, delay time = " + UPLOAD_SYSLOG_INTERVAL);
					mHandler.sendEmptyMessageDelayed(UPLOAD_SYSLOG_FILES, UPLOAD_SYSLOG_INTERVAL);
				}
			}
		}
		
		return START_NOT_STICKY;
	}
}
