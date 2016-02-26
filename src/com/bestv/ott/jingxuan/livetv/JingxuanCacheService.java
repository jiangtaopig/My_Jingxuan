package com.bestv.ott.jingxuan.livetv;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.bestv.ott.jingxuan.JX_Activity;
import com.bestv.ott.jingxuan.data.ChannelResult;
import com.bestv.ott.jingxuan.data.DataTransaction;
import com.bestv.ott.jingxuan.data.JxAllScheduleItem;
import com.bestv.ott.jingxuan.livetv.OttContextMgr.OttDataLoadFinishListener;
import com.bestv.ott.jingxuan.net.IOTVNetException;
import com.bestv.ott.jingxuan.net.ModuleServiceMgr;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;

public class JingxuanCacheService extends Service {
	private static final String TAG = "JingxuanCacheService";
	// handler message
	private static final int MSG_CHECK_OTT_FINISH = 100; // indices check and initialize ott environment finish
	private static final int MSG_START_INIT_JINGXUAN_ALL_SCHEDULES = 101; // init data by local cache
	private static final int MSG_START_INIT_JINGXUAN_ALL_CATEGERYS = 102; // init data by local cache
	private static final int MINUTE_MIN = 0; // update schedule
	private static final int MINUTE_MAX = 60; // update schedule
	private static final int ALARM_ID = 4839;
	private Context mContext = null;
	private Handler mHandler = null;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = getApplicationContext();
		Logger.d("Apk info: " + utils.getVersionCode(mContext) + ", "
				+ utils.getVersionName(mContext));

		DataConfig.initPackageName(mContext);
		DataConfig.resetDataConfig(mContext);
		IOTV_Date.instance().setContext(mContext.getApplicationContext());

		HandlerThread handlerThread = new HandlerThread(
				"jingxuan_cache_service_workthread", Process.THREAD_PRIORITY_BACKGROUND);
		handlerThread.start();
		mHandler = new CacheHandler(handlerThread.getLooper());
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Logger.d(TAG, "enter onDestroy");
		if (mHandler != null) {
			mHandler.getLooper().quit();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "JingxuanCacheService start");
		if (isJxActivityRunning()) {
		Logger.d(TAG, "JxActivity is running, needn't CacheService");
			return super.onStartCommand(intent, flags, startId);
		}
		OttContextMgr.getInstance().lockInstance(true);
		OttContextMgr.getInstance().setAppRunningType(
				OttContextMgr.APP_RUNNING_TYPE_JX);
		OttContextMgr.getInstance().setContext(mContext);
		OttContextMgr.getInstance().initContext(
				new OttDataLoadFinishListener() {

					@Override
					public void OttDatainitFinish() {
						// TODO Auto-generated method stub
						mHandler.sendEmptyMessage(MSG_CHECK_OTT_FINISH);
					}
				});
		return super.onStartCommand(intent, flags, startId);
	}

	class CacheHandler extends Handler {

		public CacheHandler(Looper looper) {
			// TODO Auto-generated constructor stub
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_CHECK_OTT_FINISH:
				Logger.d(TAG, "check jxCacheService ott finish -----------------------");
				initServiceUrl();
				//sendEmptyMessage(MSG_START_INIT_JINGXUAN_ALL_SCHEDULES);
				sendEmptyMessage(MSG_START_INIT_JINGXUAN_ALL_CATEGERYS);
				break;
			case MSG_START_INIT_JINGXUAN_ALL_CATEGERYS:
				Logger.d(TAG,"int jx_categorys --------------------");
				try {
					DataTransaction.getJXCategorys(mContext,true);
				} catch (IOTVNetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendEmptyMessage(MSG_START_INIT_JINGXUAN_ALL_SCHEDULES);
				break;
			case MSG_START_INIT_JINGXUAN_ALL_SCHEDULES:
				Logger.d(TAG, "init jx_all_schedules -----------------------");
				initJxAllSchedules();
				break;
			default:
				break;
			}
		}

	}

	public void initServiceUrl() {
		Logger.d("enter initServiceUrl");
		// init live source url by module service.
//		ModuleServiceMgr.getInstance(mContext).setUserProfile(
//				OttContextMgr.getInstance().getUserProfile());
//		ModuleServiceMgr.getInstance(mContext).generateServiceSync();

		DataConfig.initDataConfig(mContext);
	}

	private void initJxAllSchedules() {
		Logger.d("enter initJxAllSchedules");
		try {
			// check next day's file is exist or not
			// get next day
			Date day = IOTV_Date.instance().getDateToday();
			String today = IOTV_Date.getDateFormatNormal(day);
			Logger.d(TAG, "initJxAllSchedules , today = "+today);

			File file = new File(DataConfig.getFilePath(Constants.JX_ALL_SCHEDULE_FILE + today));
			if (!file.exists()) {
				Logger.d("jingxuan today = " + file.getName()
						+ " is not exist, let's download it now");
				DataTransaction.getAllJXCategorysAndSchedules(
						mContext, today);
			}else{
				DataTransaction.getJxDataFormJson(today);
			}
			
			
			
		} catch (Exception e) {
		}
		regidetNextStart();
	}

	private boolean isJxActivityRunning() {
		Logger.d(TAG, "ernter isJxActivityRunning");
		boolean flag = utils.isActivityRunning(JX_Activity.class.getName(),
				mContext);
		Logger.d(TAG, "isJxActivityRunning: " + flag);
		return flag;
	}

	private void regidetNextStart() {
		Logger.d(TAG, "enter regidetNextStart");
		Date curDate = IOTV_Date.instance().getDateToday();
		Logger.d(TAG, "curDate: " + IOTV_Date.getDateFormatFull(curDate));
		Date nextDate = IOTV_Date.instance().getDateToday();
		Calendar c = Calendar.getInstance();
		c.setTime(curDate);
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		if (curHour >= Constants.CHECK_JINGXUAN_START_TIME_HOUR) {
			nextDate = IOTV_Date.instance().getDateAsHour(24);
			c.setTime(nextDate);
		}
		Random r = new Random();
		int nextHour = Constants.CHECK_JINGXUAN_START_TIME_HOUR
				+ r.nextInt(Constants.CHECK_JINGXUAN_END_TIME_HOUR
						- Constants.CHECK_JINGXUAN_START_TIME_HOUR);
		int nextMin = MINUTE_MIN + r.nextInt(MINUTE_MAX - MINUTE_MIN);
		c.set(Calendar.HOUR_OF_DAY, nextHour);
		c.set(Calendar.MINUTE, nextMin);
		nextDate = c.getTime();
		Logger.d(TAG, "nextDate: " + IOTV_Date.getDateFormatFull(nextDate));
		long time = nextDate.getTime() - curDate.getTime();

		int alarmId = ALARM_ID;
		Logger.d(TAG, "alarmId: " + alarmId);
		Logger.d(TAG, "delay time: " + time);
		long alermTime = SystemClock.elapsedRealtime() + time;
		AlarmManager alarm = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext, JingxuanCacheService.class);
		// 取消以前的提醒，准备重设
		PendingIntent pIntent = PendingIntent.getService(mContext, alarmId,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarm.set(AlarmManager.ELAPSED_REALTIME, alermTime, pIntent);
		Logger.d(TAG, "alarm.set()");
	}
}
