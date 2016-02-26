package com.bestv.ott.jingxuan.model;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.bestv.ott.framework.proxy.authen.AuthParam;
import com.bestv.ott.framework.proxy.authen.AuthResult;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.ChannelIconMgr;
import com.bestv.ott.jingxuan.data.ChannelResult;
import com.bestv.ott.jingxuan.data.DataTransaction;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.livetv.OttContextMgr.OttDataLoadFinishListener;
import com.bestv.ott.jingxuan.net.IOTVNetException;
import com.bestv.ott.jingxuan.net.ModuleServiceMgr;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.jingxuan.R;

public class IOTVDataModel implements IDataModel{
	private HandlerThread workThread;
	private UpdateHandler mHandler;
	
	private Context mContext;
	private IStateChangeListener mStateListener;
	
	//the time distance to check and start download tommorrow's data
	private static final int CHECK_START_TIME_HOUR = 20;  //18:00
	private static final int CHECK_END_TIME_HOUR = 23; //23:00
	
	private boolean mForceRefresh = true;
	
	//the monitor interval time is random between below two values
	private static final int MONITOR_INTERVAL_MIN = 5 * 1000 * 60; //4 minutes
	private static final int MONITOR_INTERVAL_MAX = 60 * 1000 * 60; //8 minutes
	
	private static final int SCHEDULE_FORCE_UPDATE = 1;
	private static final int SCHEDULE_OPTION_UPDATE = 0;
	
	//handler message
	private static final int MSG_CHECK_OTT_FINISH = 100; //indices check and initialize ott environment finish
	private static final int MSG_START_INIT_LOCALDATA = 101;   //init data by local cache
	private static final int MSG_START_LOAD_NETDATA = 102;    //sync data with server
	private static final int MSG_START_MONITOR = 103;        //monitor message
	private static final int MSG_UPDATE_SCHEDULE = 104;    //update schedule
	
	private Map<String, List<Channel>> mCacheMap = new HashMap<String, List<Channel>>();
	private ChannelResult mCacheChannel = null;
	private ChannelResult mRawCacheChannel = null;
	private ChannelIconMgr mChannelIconMgr;
	
	public IOTVDataModel(){
		mForceRefresh = true;
		mChannelIconMgr = new ChannelIconMgr();
	}
	
	@Override
	public void start(boolean forceRefresh) {
		// TODO Auto-generated method stub
		if (workThread == null || !workThread.isAlive()){
			workThread = new HandlerThread("data_model_workthread");
			//workThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
			workThread.start();
			
			mHandler = new UpdateHandler(workThread.getLooper());
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				}
			});
		}
		
		//check network state
		/*if (HttpUtils.checkNetworkStatus(mContext) == -1){
			if (mStateListener != null){
				IOTVNetException e = IOTVNetException.builder(mContext, IOTVNetException.TYPE_NETWORK_UNAVAILABLE, R.string.network_error);
				mStateListener.reportError(IOTVNetException.TYPE_NETWORK_UNAVAILABLE, e);
				return;
			}
		}*/
		
		mForceRefresh = forceRefresh;
		
		//if system time is not normal, force refresh
		if (!IOTV_Date.instance().checkDateState()){
			mForceRefresh = true;
		}
		
		Logger.d("start loading msg = " + "force refresh = " + mForceRefresh);
		
		OttContextMgr.getInstance().initContext(new OttDataLoadFinishListener(){
			public void OttDatainitFinish() {
				// TODO Auto-generated method stub
				Logger.d("force Refresh = " + mForceRefresh);
				if (!mForceRefresh){
		              List<Channel> cache_list = getCacheData(Constants.CHANNEL_FILE);
						
						if (cache_list != null && cache_list.size() >= 0){
							mCacheChannel = new ChannelResult();
							mCacheChannel.setItems(cache_list);
							
						    notifyLoadFinish();
							
							if (mHandler.hasMessages(MSG_START_MONITOR)){
								//start monitor soon
								mHandler.sendEmptyMessage(MSG_START_MONITOR);
							}
							
							return;
						}
				}
				
				mHandler.sendEmptyMessage(MSG_CHECK_OTT_FINISH);
			}
		});
	}

	@Override
	public List<Channel> getChannelsSync(String day, boolean forcerefresh) {
		// TODO Auto-generated method stub
		if (day == null){
			//get today
			day = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
		}

		List<Channel> tmp = getCacheFormMap(day);
		if (tmp != null){
			return tmp;
		}

        List<Channel> cache_list = getCacheData(day);
        
		Date date = IOTV_Date.instance().getDate();
		String today = IOTV_Date.getDateFormatNormal(date);
        
		if (today.equals(day)){
			startCacheThread();
        }
        
        if (cache_list == null || cache_list.size() == 0){
        	Message msg = mHandler.obtainMessage(MSG_UPDATE_SCHEDULE);
        	
        	File file = new File(DataConfig.getFilePath(day));
            //if file is exist but channel list is empty, means file data format is abnormal,
        	//MUST force update it.
        	if (file.exists()){
        		msg.arg1 = SCHEDULE_FORCE_UPDATE;
        	}else{
        		//file is not exist, update it optionally
        	    msg.arg1 = SCHEDULE_OPTION_UPDATE;
        	}
        	
        	msg.obj = day;
        	msg.sendToTarget();
        	
        	cache_list = mRawCacheChannel.getItems();
        	return cache_list;
        }
        
        setCacheInMap(day, cache_list);
        return cache_list;
	}
	
	private void startCacheThread(){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				List<String> days = IOTV_Date.instance().getPrevDays();
				
				for (String day : days){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (hasCacheInMap(day)){
						continue;
					}
					
			        List<Channel> cache_list = getCacheData(day);
			        if (cache_list != null && cache_list.size() > 0){
			        	if (!hasCacheInMap(day)){
			        	    setCacheInMap(day, cache_list);
			        	}
			        }
				}
			}
		});
		
		t.start();
	}
	
	private List<Channel> getCacheData(String day){
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
				ChannelResult result = utils.gson.fromJson(jsondata, ChannelResult.class);
				list = result.getItems();
				
				String setDay = day;
				
				if (setDay.equals(Constants.CHANNEL_FILE)){
					setDay = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
				}
				
				for (Channel channel : list){
					channel.setDay(setDay);
				}
				
				if (Constants.TEST_LIVE_SHIFT){
					//list.get(0).setLivePlayUrl(Constants.TEST_LIVE_SRC);
				}
			}
		} catch (Exception e) {
			list = null;
		}
		
		return list;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		if (mHandler != null) {
			try {
				//enqueue quit message in looper
				mHandler.getLooper().quit();
			} catch (Exception e) {
			}
		}
		
		mChannelIconMgr.quit();
	}
	
	class UpdateHandler extends Handler {
		boolean quit = false;
		public UpdateHandler(Looper p) {
			// TODO Auto-generated constructor stub
			super(p);
			quit = false;
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			 
			if (quit){
				return;
			}
			
			switch (msg.what) {
			case MSG_CHECK_OTT_FINISH: {
				Logger.d("check ott finish -----------------------");
				//init live source url by module service.
				ModuleServiceMgr.getInstance(mContext).setUserProfile(OttContextMgr.getInstance().getUserProfile());
				ModuleServiceMgr.getInstance(mContext).generateServiceSync();
				
				DataConfig.initDataConfig(mContext);
					
				//init cache data
				mCacheMap.clear();	
				mCacheChannel = null;
				
				//start init local data
				sendEmptyMessage(MSG_START_INIT_LOCALDATA);
			}
				break;
			case MSG_START_LOAD_NETDATA: {
				Logger.d("start load data from server-------------- ");
				boolean needNotify = false;
				ChannelResult cr = null;
				
				try {
					cr = DataTransaction.getNewChannels(mContext, 1, null);
					
					if (!isEmptyChannelResult(cr)){
						if (!isEmptyChannelResult(mCacheChannel)){
							//cache channel size is not same as the new channel size
							//need refresh all 
							int cache_size = mCacheChannel.getItems().size();
							int sync_size = cr.getItems().size();
							
							if (cache_size != sync_size){
								Logger.d("IOTV1", "channel is not the same, force clear all cache file cache size = " + cache_size + " sync size = " + sync_size);
								DataConfig.clearAllCacheFile();
								mCacheMap.clear();
							}
						}
						
						DataTransaction.saveChannelData(Constants.CHANNEL_FILE, cr);
					}
				} catch (IOTVNetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					Logger.d("IOTV1", "IOTVNetexception errorcode = " + e.getCompleteErrorCode());
					
					if (mStateListener != null && isEmptyChannelResult(mCacheChannel)){
						mStateListener.reportError(IOTVNetException.TYPE_INIT_CHANNELLIST, e);
					}
					
					return;
				}
				
				if (!isEmptyChannelResult(cr)){
					if (mCacheChannel == null){
						mCacheChannel = cr;
						needNotify = true;
					}else{
						mCacheChannel = cr;
					}
					
					//set every channel date as today
					String setDay = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
				    for (Channel channel : cr.getItems()){
					    channel.setDay(setDay);
				    }
					
					mRawCacheChannel = new ChannelResult();
					mRawCacheChannel.setItems(ChannelManager.getInstance().cloneChannelList(cr.getItems()));
					
					//update channel update time
					DataConfig.setDataConfigValueL(mContext, DataConfig.UPDATE_DATA_TIME, IOTV_Date.getDate().getTime());
				}
				
				//force update all schedule
				List<String> dayList = IOTV_Date.instance().getPrevDays();	
				for (String day : dayList){
					Message m = mHandler.obtainMessage(MSG_UPDATE_SCHEDULE);
					m.arg1 = SCHEDULE_FORCE_UPDATE;
					m.obj = day;
					m.sendToTarget();
				}
				
				if (needNotify){
					notifyLoadFinish();
				}
				
				mHandler.sendEmptyMessage(MSG_START_MONITOR);
				clearCache();
			}
			break;
			case MSG_START_INIT_LOCALDATA:{
				Logger.d("IOTV4", "init local data");
                List<Channel> cache_list = getCacheData(Constants.CHANNEL_FILE);
				
				if (cache_list != null && cache_list.size() >= 0){
					mCacheChannel = new ChannelResult();
					mCacheChannel.setItems(cache_list);
					
					mRawCacheChannel = new ChannelResult();
					mRawCacheChannel.setItems(ChannelManager.getInstance().cloneChannelList(cache_list));
					
					notifyLoadFinish();
					
					long current_time = IOTV_Date.getDate().getTime();
					long update_time = DataConfig.getDataConfigValueL(mContext, DataConfig.UPDATE_DATA_TIME);
					//if update time plus expire time is larger than current time, means data is not expired.
					if (((update_time + DataConfig.MAX_EXPIRE_TIME_MAINDATA ) > current_time) && IOTV_Date.instance().checkDateState()){
						Logger.d("IOTV4", "channel data doesn't expire, so it needn't refresh");
						mHandler.sendEmptyMessage(MSG_START_MONITOR);
						return;
					}else{
						//channel load finish and then load schedules
						sendEmptyMessage(MSG_START_LOAD_NETDATA);
					}
				}else{
					//channel load finish and then load schedules
					sendEmptyMessage(MSG_START_LOAD_NETDATA);
				}
			}
			break;
			case MSG_START_MONITOR:
				startMonitor();
				break;
			case MSG_UPDATE_SCHEDULE:
			{
				String day = (String) msg.obj;
				boolean isForce = msg.arg1 == SCHEDULE_FORCE_UPDATE;
				
				Logger.d("IOTV4", "update schedule day = " + day + " isforce = " + isForce);
				
				File f = new File(DataConfig.getFilePath(day));
				
				if (f.exists() && !isForce){
					return;
				}
				
				List<String> tmpL = new ArrayList<String>();
				tmpL.add(day);
				
				DataTransaction.requestSchedules(mCacheChannel, tmpL);
				//remove memory cache
				removeCacheInMap(day);
				
		        List<Channel> cache_list = getCacheData(day);
		        if (cache_list != null && cache_list.size() > 0){
		        	setCacheInMap(day, cache_list);
		        }
				
				if (mStateListener != null){
					mStateListener.sheduleListUpdate(day, null);
				}
			}
				break;
			}	
		}
	}

	@Override
	public void setContext(Context context) {
		// TODO Auto-generated method stub
		mContext = context;
	}
	
	private void playAuthen(AuthParam param, int timeout) {
		Logger.d("enter play(" + param + ", " +  timeout + ")");

		AuthResult result = OttContextMgr.getInstance().playAuthen(param, timeout);
		
		if (mStateListener != null){
			Object data = null;
			
			if (result == null || utils.isNull(result.getPlayURL())){
				IOTVNetException e = IOTVNetException.builder(mContext, IOTVNetException.TYPE_PLAY_AUTHEN, R.string.play_authen_failed);
				e.setErrorCode("05");
				data = e;
			}else{
				data = result.getPlayURL();
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mStateListener.playAuthenFinish(data);
		}
	}

	@Override
	public void getCDNToken() {
		// TODO Auto-generated method stub
		long current_time = IOTV_Date.getDate().getTime();
		long update_time = DataConfig.getDataConfigValueL(mContext, DataConfig.UPDATE_CDN_TOKEN_TIME);
		//if update time plus expire time is larger than current time, means data is not expired.
		if ((update_time + DataConfig.MAX_EXPIRE_TIME_CDNTOKEN ) > current_time){
			String cdntoken = DataConfig.getDataConfigValueS(mContext, DataConfig.CDN_TOKEN_VALUE);
			if (!cdntoken.equals("")){
				Logger.d("CDNTOKEN doesn't expire, so it needn't refresh");
				mStateListener.playAuthenFinish(cdntoken);
				return;
			}
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String cdntoken = DataTransaction.getCDNToken();
				mStateListener.playAuthenFinish(cdntoken);
				//update update time and values in preference
				DataConfig.setDataConfigValueL(mContext, DataConfig.UPDATE_CDN_TOKEN_TIME, IOTV_Date.getDate().getTime());
				DataConfig.setDataConfigValueS(mContext, DataConfig.CDN_TOKEN_VALUE, cdntoken);
			}
		}).start();
	}

	@Override
	public void getPlayUrl(final AuthParam param) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				playAuthen(param, Constants.timeOut);
			}
		}).start();
	}

	public void addStateChangeListener(IStateChangeListener listener) {
		// TODO Auto-generated method stub
		mStateListener = listener;
	}
	
	/**
	 * MUST run in work thread.
	 * */
	private void startMonitor() {
		Date d = IOTV_Date.instance().getDateToday();
		int currentHour = d.getHours();

		Logger.d("monitor cache file list success! currentHour = "
				+ currentHour);

		if (currentHour >= CHECK_START_TIME_HOUR
				&& currentHour <= CHECK_END_TIME_HOUR) {
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

		Random r = new Random();
		int v = r.nextInt(MONITOR_INTERVAL_MAX - MONITOR_INTERVAL_MIN);

		// monitor in a random time
		mHandler.sendEmptyMessageDelayed(MSG_START_MONITOR,
				MONITOR_INTERVAL_MIN + v);
	}
	
	/**
	 * we must make sure the cache file is normally generated.
	 * */
	private List<String> checkSuccess(){
		List<String> l = IOTV_Date.instance().getPrevDays();
		List<String> n = new ArrayList<String>();
		
		for (String s : l){
			File f = new File(DataConfig.getFilePath(s));
			
			if (!f.exists()){
				n.add(s);
			}
		}
		
		return n;
	}
	
	private void notifyLoadFinish(){
		List<String> iconList = new ArrayList<String>();
		for(Channel c : mCacheChannel.getItems()){
			iconList.add(c.getIcon1());
		}
		
		mChannelIconMgr.start(iconList);
		mStateListener.loadFinish(null);
	}
	
	private void clearCache() {
		try {
			Logger.d("start clear cache");
			List<String> ignoreFileList = new ArrayList<String>();
			ignoreFileList.addAll(IOTV_Date.instance().getPrevDays());

			Date nextDate = IOTV_Date.instance().getDateAsHour(24);
			String nextDay = IOTV_Date.getDateFormatNormal(nextDate);

			ignoreFileList.add(nextDay);
			ignoreFileList.add(Constants.CHANNEL_FILE);

			File f = new File(DataConfig.getFileDir());
			String[] filelist = f.list();
        
			List<String> deleteList = new ArrayList<String>();
			
			for (String s : filelist) {
				boolean isfind = false;
				
				for(String tmp : ignoreFileList){
					if (s.equals(tmp + ".json")){
						isfind = true;
						break;
					}
				}
				
				if (!isfind){
					deleteList.add(s);
				}
			}
			
			for(String tmp : deleteList){
				Logger.d("delete file " + tmp);
				f = new File(DataConfig.getFileDir() + File.separator + tmp);
				f.delete();
			}
		} catch (Exception e) {
		}
	}
	
	boolean isEmptyChannelResult(ChannelResult cr){
		boolean b = cr != null && cr.getItems() != null && cr.getItems().size() > 0;
		return !b;
	}
	
	private synchronized List<Channel> getCacheFormMap(String key){
		if (mCacheMap.containsKey(key)){
			return mCacheMap.get(key);
		}
		
		return null;
	}
	
	private synchronized void removeCacheInMap(String key){
		mCacheMap.remove(key);
	}
	
	private synchronized void setCacheInMap(String key, List<Channel> value){
		mCacheMap.put(key, value);
	}
	
	private synchronized boolean hasCacheInMap(String key){
		return mCacheMap.containsKey(key);
	}

	@Override
	public List<Schedule> getScheduleByCategory(String category, Message msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Schedule getNextScheduleOfChannel(int ChannelIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Schedule> getLatestSubscribeSchedules(
			List<Schedule> subscribeSchedules) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshData(String today, Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Channel> getIotvChannelListByDay(String today) {
		// TODO Auto-generated method stub
		return null;
	}
}
