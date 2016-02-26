package com.bestv.ott.jingxuan.livetv;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.bestv.ott.jingxuan.JX_Activity;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.ChannelResult;
import com.bestv.ott.jingxuan.data.DataTransaction;
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

public class IotvCacheService extends Service {
	private static final String TAG = "IotvCacheService";
	// handler message
	private static final int MSG_CHECK_OTT_FINISH = 100; // indices check and
															// initialize ott
															// environment
															// finish
	private static final int MSG_START_INIT_LOCALDATA = 101; // init data by
																// local cache
	private static final int MSG_START_LOAD_NETDATA = 102; // sync data with
	// server
	private static final int MSG_START_MONITOR = 103; // monitor message
	private static final int MSG_UPDATE_SCHEDULE = 104; // update schedule
	private static final int MINUTE_MIN = 0; // update schedule
	private static final int MINUTE_MAX = 60; // update schedule
	private static final int ALARM_ID = 3839;
	private Context mContext = null;
	private Handler mHandler = null;
	private ChannelResult mCacheChannel = null;

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
				"iotv_cache_service_workthread", Process.THREAD_PRIORITY_BACKGROUND);
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
		Logger.d(TAG, "IotvCacheService start");
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
				Logger.d(TAG, "check iotvCacheService ott finish -----------------------");
				initServiceUrl();
				sendEmptyMessage(MSG_START_INIT_LOCALDATA);
				break;
			case MSG_START_INIT_LOCALDATA:
				initLoaclData();
				break;
			case MSG_START_LOAD_NETDATA:
				Logger.d(TAG, "start load data from server-------------- ");
				loadNetData();
				break;
			case MSG_UPDATE_SCHEDULE:
				Logger.d(TAG, "start update schedule-------------- ");
				String day = (String) msg.obj;
				updateSchedule(day);
				break;
			case MSG_START_MONITOR:
				Logger.d(TAG, "start monitor-------------- ");
				startMonitor();
				regidetNextStart();
				break;
			default:
				break;
			}
		}

	}

	public void initServiceUrl() {
		// init live source url by module service.
		ModuleServiceMgr.getInstance(mContext).setUserProfile(
				OttContextMgr.getInstance().getUserProfile());
		ModuleServiceMgr.getInstance(mContext).generateServiceSync();

		DataConfig.initDataConfig(mContext);
	}

	private void initLoaclData() {
		Logger.d(TAG, "enter initLoaclData");
		List<Channel> cache_list = getCacheData(Constants.CHANNEL_FILE);

		Date day = IOTV_Date.instance().getDateToday();
		String today = IOTV_Date.getDateFormatNormal(day);
		Logger.d(TAG, "initLoaclData , today = "+today);
		
		DataTransaction.getCacheData(today);   
		
		if (cache_list != null && cache_list.size() >= 0) {
			Logger.d(TAG, "size = "+cache_list.size());
			
			DataTransaction.getCacheData(Constants.CHANNEL_FILE);
			
			mCacheChannel = new ChannelResult();
			mCacheChannel.setItems(cache_list);

			long current_time = IOTV_Date.getDate().getTime();
			long update_time = DataConfig.getDataConfigValueL(mContext,
					DataConfig.UPDATE_DATA_TIME);
			// if update time plus expire time is larger than current
			// time, means data is not expired.
			if (((update_time + DataConfig.MAX_EXPIRE_TIME_MAINDATA) > current_time)
					&& IOTV_Date.instance().checkDateState()) {
				Logger.d(TAG,
						"channel data doesn't expire, so it needn't refresh");
				mHandler.sendEmptyMessage(MSG_START_MONITOR);
				return;
			} else {
				// channel load finish and then load schedules
				mHandler.sendEmptyMessage(MSG_START_LOAD_NETDATA);
			}
		} else {
			// channel load finish and then load schedules
			Logger.d(TAG, "cache_list is null || cache_list.size() <0");
			mHandler.sendEmptyMessage(MSG_START_LOAD_NETDATA);
		}
	}

	private void loadNetData() {
		Logger.d(TAG, "enter loadNetData");
		ChannelResult cr = null;
		try {
			cr = DataTransaction.getNewChannels(mContext, 1, null);

			Logger.d(TAG, "cr = "+cr+" , mCacheChannel = "+mCacheChannel);
			if (!isEmptyChannelResult(cr)) {
				
				if (!isEmptyChannelResult(mCacheChannel)) {
					// cache channel size is not same as the new channel
					// size
					// need refresh all
					int cache_size = mCacheChannel.getItems().size();
					int sync_size = cr.getItems().size();

					if (cache_size != sync_size) {
						Logger.d(TAG,
								"channel is not the same, force clear all cache file cache size = "
										+ cache_size + " sync size = "
										+ sync_size);
						DataConfig.clearAllCacheFile();
					}
				}

				DataTransaction.saveChannelData(Constants.CHANNEL_FILE, cr);
			}
		} catch (IOTVNetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Logger.d(TAG,
					"IOTVNetexception errorcode = " + e.getCompleteErrorCode());
			return;
		}

		if (!isEmptyChannelResult(cr)) {
			if (mCacheChannel == null) {
				mCacheChannel = cr;
			} else {
				mCacheChannel = cr;
			}

			// set every channel date as today
			String setDay = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
			for (Channel channel : cr.getItems()) {
				channel.setDay(setDay);
			}

			// update channel update time
			DataConfig.setDataConfigValueL(mContext,
					DataConfig.UPDATE_DATA_TIME, IOTV_Date.getDate().getTime());
		}

		// force update all schedule
		// List<String> dayList = IOTV_Date.instance().getPrevDays();
		// for (String day : dayList) {
		// Message m = mHandler.obtainMessage(MSG_UPDATE_SCHEDULE);
		// m.obj = day;
		// m.sendToTarget();
		// }
		// force update today schedule
		if (!isTodayScheduleExists()) {
			Logger.d(TAG, "download today's schedules");
			String day = IOTV_Date.getDateFormatNormal(IOTV_Date.instance()
					.getDateToday());
			Message m = mHandler.obtainMessage(MSG_UPDATE_SCHEDULE);
			m.obj = day;
			m.sendToTarget();
		} else {
			Logger.d(TAG, "today's schedules is existed");
		}

		mHandler.sendEmptyMessage(MSG_START_MONITOR);
	}

	private void updateSchedule(String day) {
		Logger.d(TAG, "update schedule day = " + day);
		List<String> tmpL = new ArrayList<String>();
		tmpL.add(day);
		DataTransaction.requestSchedules(mCacheChannel, tmpL);
	}

	private List<Channel> getCacheData(String day) {
		Logger.d(TAG, "enter getCacheData...day = "+day);
		String path = DataConfig.getFilePath(day);

		List<Channel> list = null;

		try {
			File f = new File(path);
			if (f.exists()) {
				FileInputStream input = new FileInputStream(f);

				byte data[] = new byte[input.available()];

				input.read(data);
				input.close();

				String jsondata = new String(data);
				ChannelResult result = utils.gson.fromJson(jsondata,
						ChannelResult.class);
				list = result.getItems();

				String setDay = day;

				if (setDay.equals(Constants.CHANNEL_FILE)) {
					setDay = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
					Logger.d(TAG, "setDay = "+setDay);
				}

				for (Channel channel : list) {
					channel.setDay(setDay);
				}

			}
		} catch (Exception e) {
			list = null;
			Logger.d(TAG, "file "+day+".json is not exits");
		}

		return list;
	}

	/**
	 * MUST run in work thread.
	 * */
	private void startMonitor() {
		Logger.d(TAG, "enter startMonitor");
		Date d = IOTV_Date.instance().getDateToday();
		int currentHour = d.getHours();

		Logger.d(TAG, "monitor cache file list success! currentHour = "
				+ currentHour);

		if (currentHour >= Constants.CHECK_START_TIME_HOUR
				&& currentHour <= Constants.CHECK_END_TIME_HOUR) {
			try {
				// check next day's file is exist or not
				// get next day
				Date nextDate = IOTV_Date.instance().getDateAsHour(24);
				String nextDay = IOTV_Date.getDateFormatNormal(nextDate);

				File file = new File(DataConfig.getFilePath(nextDay));
				if (!file.exists()) {
					Logger.d("next day = " + file.getName()
							+ " is not exist, let's download it now");
					List<String> list = new ArrayList<String>();
					list.add(nextDay);
					DataTransaction.requestSchedules(mCacheChannel, list);
				}
			} catch (Exception e) {
			}
		}
		Logger.d(TAG,"end startMonitor");
	}

	private boolean isJxActivityRunning() {
		Logger.d(TAG, "ernter isJxActivityRunning");
		boolean flag = utils.isActivityRunning(JX_Activity.class.getName(),
				mContext);
		Logger.d(TAG, "isJxActivityRunning: " + flag);
		return flag;
	}

	private boolean isTodayScheduleExists() {
		Logger.d(TAG, "ernter isTodayScheduleExists");
		String day = IOTV_Date.getDateFormatNormal(IOTV_Date.instance()
				.getDateToday());
		String path = DataConfig.getFilePath(day);
		boolean flag = new File(path).exists();
		Logger.d(TAG, "day: " + day + ", isTodayScheduleExists: " + flag);
		return flag;
	}

	private boolean isEmptyChannelResult(ChannelResult cr) {
		boolean b = cr != null && cr.getItems() != null
				&& cr.getItems().size() > 0;
		return !b;
	}

	private void regidetNextStart() {
		Logger.d(TAG, "enter regidetNextStart");
		Date curDate = IOTV_Date.instance().getDateToday();
		Logger.d(TAG, "curDate: " + IOTV_Date.getDateFormatFull(curDate));
		Date nextDate = IOTV_Date.instance().getDateToday();
		Calendar c = Calendar.getInstance();
		c.setTime(curDate);
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		if (curHour >= Constants.CHECK_START_TIME_HOUR) {
			nextDate = IOTV_Date.instance().getDateAsHour(24);
			c.setTime(nextDate);
		}
		Random r = new Random();
		int nextHour = Constants.CHECK_START_TIME_HOUR
				+ r.nextInt(Constants.CHECK_END_TIME_HOUR
						- Constants.CHECK_START_TIME_HOUR);
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
		Intent intent = new Intent(mContext, IotvCacheService.class);
		// 取消以前的提醒，准备重设
		PendingIntent pIntent = PendingIntent.getService(mContext, alarmId,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarm.set(AlarmManager.ELAPSED_REALTIME, alermTime, pIntent);
		Logger.d(TAG, "alarm.set()");
	}
}
