package com.bestv.ott.jingxuan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.bestv.jingxuan.R;
import com.bestv.ott.framework.proxy.authen.AuthParam;
import com.bestv.ott.framework.proxy.authen.AuthResult;
import com.bestv.ott.framework.utils.JsonUtils;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.ChannelIconMgr;
import com.bestv.ott.jingxuan.data.ChannelResult;
import com.bestv.ott.jingxuan.data.DataTransaction;
import com.bestv.ott.jingxuan.data.JXSubscription;
import com.bestv.ott.jingxuan.data.JxAllScheduleItem;
import com.bestv.ott.jingxuan.data.JxAllScheduleKey;
import com.bestv.ott.jingxuan.data.JxAllScheduleResult;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.livetv.OttContextMgr.OttDataLoadFinishListener;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.model.IDataModel;
import com.bestv.ott.jingxuan.model.IStateChangeListener;
import com.bestv.ott.jingxuan.net.IOTVNetException;
import com.bestv.ott.jingxuan.net.ModuleServiceMgr;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.TimeUseDebugUtils;
import com.bestv.ott.jingxuan.util.utils;


public class JXDataModel implements IDataModel {
	private String TAG = "JXDataModel";
	private HandlerThread workThread;
	private UpdateHandler mHandler;

	private Handler mMainThreadHandler;

	private Context mContext;
	private IStateChangeListener mStateListener;

	private boolean mForceRefresh = true;
	//indicates at current time, work thread is mixing data or not.
	private boolean mMixingData = false;

	private static final int SCHEDULE_FORCE_UPDATE = 1;
	private static final int SCHEDULE_OPTION_UPDATE = 0;

	// handler message
	private static final int MSG_CHECK_OTT_FINISH = 100; // indices check and
															// initialize ott
															// environment
															// finish
	private static final int MSG_START_INIT_LOCAL_IOTV_DATA = 101; // init data by
																// local cache
	private static final int MSG_START_LOAD_NET_IOTV_DATA = 102; // sync data with
	
															// server
	private static final int MSG_START_MONITOR = 103; // monitor message
	private static final int MSG_UPDATE_SCHEDULE = 104; // update schedule
	private static final int MSG_GET_JX_CATEGORY = 105;
	private static final int MSG_MIXING_DATA = 106; //mixing the data of iotv and jingxuan
	private static final int MSG_START_LOAD_NET_JX_DATA = 107;
	private static final int MSG_START_INIT_LOCAL_JX_DATA = 108;
	// cache the data which was mixed by the date of iotv and jingxuan
	private Map<String, List<Channel>> mCacheMixedMap = new HashMap<String, List<Channel>>();
	// cache iotv data
	private Map<String, List<Channel>> mCacheIotvMap = new HashMap<String, List<Channel>>();
	// the default available channel data that will be used if the specify day's
	// channel data is not found.
	// e.g. the application just right runs covering two days, controller wants
	// to request next day's channel data but they are not
	// ready now, the default cached data will be returned(the previous day's
	// data in normal)
	
	List<Channel> mIOTVChannelCache = null;
	List<Channel> mDefaultChannelCache;

	List<Long> mIotvSchedulesEndTimeList;

	//use to save jingxuan category data
	private ChannelResult mJxCategoryResult;
	//indicates that IOTV is available or not
	/**
	 * 表示是否有“最新”---也就是直播
	 */
	private boolean mIsIOTVAvailable;
	
	private ChannelIconMgr mChannelIconMgr;

	private Map<String, List<Schedule>> mScheduleMap;
	//private Map<String, String> mChannelPlayUrlMap = new HashMap<String, String>();
	private static final long JX_EXPIRE_TIME = 60 * 1000 * 30;
	private static final long IOTV_SCHEDULE_EXPIRE_TIME = 30 * 1000; // 30
																		// seconds
	private boolean isRefreshingData = false;
	private boolean isParsingJxSchedules = true;
	public JXDataModel() {
		mForceRefresh = true;
		mChannelIconMgr = new ChannelIconMgr();
		mIotvSchedulesEndTimeList = new ArrayList<Long>();
		mScheduleMap = new HashMap<String, List<Schedule>>();

		mMainThreadHandler = new Handler();
	}

	@Override
	public void start(boolean forceRefresh) {
		// TODO Auto-generated method stub
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter start");
		if (workThread == null || !workThread.isAlive()) {
			workThread = new HandlerThread("data_model_workthread");
			// workThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
			workThread.start();

			mHandler = new UpdateHandler(workThread.getLooper());
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				}
			});

			// start the mechanism of clearing cache data
			//new ScheduleExpireRunnable().run();
		}

		mForceRefresh = forceRefresh;

		// if system time is not normal, force refresh
		if (!IOTV_Date.instance().checkDateState()) {
			mForceRefresh = true;
		}

		Logger.d("start loading msg = " + "force refresh = " + mForceRefresh);
		
		OttContextMgr.getInstance().initContext(new OttDataLoadFinishListener() {
					public void OttDatainitFinish() {
						// TODO Auto-generated method stub
						Logger.d("force Refresh = " + mForceRefresh);
						TimeUseDebugUtils.timeUsageDebug(TAG+",enter OttDatainitFinish");
						mHandler.sendEmptyMessage(MSG_CHECK_OTT_FINISH);
					}
				});
	}
	
	private synchronized boolean isMixing(){
		return mMixingData;
	}
	
	private synchronized void setMixing(boolean mix){
		mMixingData = mix;
	}

	@Override
	public List<Channel> getChannelsSync(String day, boolean forcerefresh) {
		// TODO Auto-generated method stub
		//Logger.d("enter getChannelsSync " );
		TimeUseDebugUtils.timeUsageDebug(TAG+"enter getChannelsSync day = "+day);
		if (day == null) {
			// get today
			day = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
		}
		/*modify by xubin 2015.11.10 begin*/
		if (!mIsIOTVAvailable && mJxCategoryResult != null){
				return mJxCategoryResult.getItems();
		}
		/*modify by xubin 2015.11.10 end*/
		List<Channel> tmp = getMixedCacheFormMap(day);
		if (tmp != null) {
			Logger.d("tmp != null");
			return tmp;
		}
		
		//if this day's IOTV data was already download but not mixing
		//let's mixing them now
		tmp = getIotvCacheFormMap(day);
		if (tmp != null){
			if (!isMixing()){
				setMixing(true);
				
				Message msg = mHandler.obtainMessage(MSG_MIXING_DATA);
				msg.obj = day;
				msg.sendToTarget();
			}
		}

		if(mIOTVChannelCache != null) {
			Logger.d("mIOTVChannelCache != null");
			return mIOTVChannelCache;
		}
		// try to get cached channel data
		//List<Channel> cache_list = getCacheData(Constants.CHANNEL_FILE); //change by zjt 11/4
		List<Channel> cache_list = DataTransaction.getChannelData();
		
		//Logger.d("cache_list.size() =  "+cache_list.size());
		if (cache_list != null && cache_list.size() > 0) {
			mIOTVChannelCache = cache_list;
			return cache_list;
		}

		return null;
	}

//
//	private List<Channel> getCacheData(String day) {
//		Logger.d(TAG,"start getCacheData, day :" + day+ " id = "+Thread.currentThread().getId());
//		String path = DataConfig.getFilePath(day);
//
//		List<Channel> list = null;
//
//		try {
//			File f = new File(path);
//			if (f.exists()) {
//				FileInputStream input = new FileInputStream(f);
//
//				byte data[] = new byte[input.available()];
//
//				input.read(data);
//				input.close();
//
//				String jsondata = new String(data);
//				ChannelResult result = utils.gson.fromJson(jsondata,
//						ChannelResult.class);
//				if(result != null && result.getItems().size() > 0){
//					list = result.getItems();
//					Logger.d(TAG, "list size = "+list.size());
//				}
//				
//				String setDay = day;
//
//				if (setDay.equals(Constants.CHANNEL_FILE)) {
//					setDay = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
//				}
//
//				for (Channel channel : list) {
//					channel.setDay(setDay);
//				}
//
//				if (Constants.TEST_LIVE_SHIFT) {
//					// list.get(0).setLivePlayUrl(Constants.TEST_LIVE_SRC);
//				}
//			}
//		} catch (Exception e) {
//			list = null;
//		}
//		//Logger.d("end getCacheData , list.size = "+list.size());
//		Logger.d("end getCacheData");
//		return list;
//	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		if (mHandler != null) {
			try {
				// enqueue quit message in looper
				mHandler.getLooper().quit();
				workThread = null;
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

			if (quit) {
				return;
			}

			switch (msg.what) {
			case MSG_CHECK_OTT_FINISH: {
				Logger.d("check ott finish -----------------------");
				TimeUseDebugUtils.timeUsageDebug(TAG+",enter MSG_CHECK_OTT_FINISH");
				// init live source url by module service.
//				ModuleServiceMgr.getInstance(mContext).setUserProfile(
//						OttContextMgr.getInstance().getUserProfile());
//				ModuleServiceMgr.getInstance(mContext).generateServiceSync();

				DataConfig.initDataConfig(mContext);

				// init cache data
				if(mCacheIotvMap != null){
					mCacheIotvMap.clear();
					mCacheMixedMap.clear();
				}
				initChannelData();
				//init JX category data first
				if(initJxCategoryData()){
					Logger.d("jingxuan category initializes success, mIsIOTVAvailable = " + mIsIOTVAvailable);
					if (mIsIOTVAvailable){
						//有“最新”分类
						// start init local data
						sendEmptyMessage(MSG_START_INIT_LOCAL_IOTV_DATA);
					}else{
						//无“最新”分类
						DataConfig.setDataConfigValueL(mContext, DataConfig.LIVE_PLAY_ENDACT, DataConfig.PLAY_AS_JX);
						sendEmptyMessage(MSG_START_INIT_LOCAL_JX_DATA);
//						//pre-load the schedule of first category to make sure application can play video soon. 
//						String categoryCode = mJxCategoryResult.getItems().get(0).getChannelCode();
//						QueryScheduleRunnable wt = new QueryScheduleRunnable(categoryCode, null);
//						wt.run();
//						
//						mJxCategoryResult.getItems().get(0).setSchedules(mScheduleMap.get(categoryCode));
//						notifyLoadFinish();
					}
				}
			}
				break;
			case MSG_START_LOAD_NET_IOTV_DATA: {
				Logger.d(TAG, "MSG_START_LOAD_NET_IOTV_DATA");
				boolean needNotify = false;
				ChannelResult cr = null;

				try {
					cr = DataTransaction.getNewChannels(mContext, 1, null);

					Logger.d("iotv channel list before--- = " + cr.getItems().size());

					if (!isEmptyChannelResult(cr)) {
						// filter lianlian kan channel out and clone iotv
						// channel
						List<Channel> iotvchannellist = new ArrayList<Channel>();
						for (Channel c : cr.getItems()) {
							if (c.getType() == Constants.CHANNEL_IOTV) {
								iotvchannellist.add(c);
								Logger.d("channalID = " + c.getChannelId()+", channelCode = "+c.getChannelCode());
							}
						}

						cr.setItems(iotvchannellist);
						Logger.d("iotv channel list = "+ iotvchannellist.size());
						DataTransaction.saveChannelData(Constants.CHANNEL_FILE,cr);

						if (!isEmptyChannelResult(cr)) {
							mIOTVChannelCache = iotvchannellist;
							notifyLoadFinish();
							sendEmptyMessage(MSG_START_INIT_LOCAL_JX_DATA);
						}
					}
				} catch (IOTVNetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					Logger.d("IOTV1", "IOTVNetexception errorcode = " + e.getCompleteErrorCode());
					
					if (mStateListener != null && isEmptyChannelResult(cr)){
						int type = IOTVNetException.TYPE_INIT_CHANNELLIST;
						mStateListener.reportError(type, IOTVNetException.builder(mContext, type, R.string.jx_channellist_failed));
					}
					return;
				}

				// force update all schedule
				List<String> dayList = IOTV_Date.instance().getPrevDays();
				if(dayList != null){
					for (String day : dayList) {
						Logger.d(TAG, " , day = "+day);
						Message m = mHandler.obtainMessage(MSG_UPDATE_SCHEDULE);
						m.arg1 = SCHEDULE_FORCE_UPDATE;
						m.obj = day;
						m.sendToTarget();
					}
				}

				if (needNotify) {
					// notifyLoadFinish();
				}
				clearCache();
			}
				break;
			case MSG_START_INIT_LOCAL_IOTV_DATA: {
				Logger.d(TAG, "MSG_START_INIT_LOCAL_IOTV_DATA");
				// channel load finish and then load schedules
				
//				mIOTVChannelCache = null;
//				List<Channel> cache_channel_list = getCacheData(Constants.CHANNEL_FILE);
//				if (cache_channel_list != null && cache_channel_list.size() > 0) {
//					mIOTVChannelCache = cache_channel_list;
//					notifyLoadFinish();
//				}
				Logger.d(TAG, "mIOTVChannelCache = "+mIOTVChannelCache);
				if (mIOTVChannelCache != null && mIOTVChannelCache.size() > 0) {
						notifyLoadFinish();
				}
				
				
				Date d = IOTV_Date.instance().getDateToday();
				String day = IOTV_Date.getDateFormatNormal(d);

				// remove memory cache
				removeIotvCacheInMap(day);

				TimeUseDebugUtils.timeUsageDebug(TAG+" ,try to get Data from Cache");
				//Logger.d(TAG,"try to get Data from Cache");
				//List<Channel> cache_list = getCacheData(day); //change by zjt 11/4
				List<Channel> cache_list = DataTransaction.getChannelDateData();
				
				setIotvCacheInMap(day, cache_list);
				mDefaultChannelCache = cache_list;

				if (cache_list != null && cache_list.size() > 0) {
					Logger.d("IOTV4", "init local data success! day:" + day
							+ ", cache_list size: " + cache_list.size());
					List<Schedule> s = cache_list.get(0).getSchedules();
					
					if (s != null && s.size() > 0) {
						Logger.d(TAG, "Schedule size = "+s.size());
						initJxChannelData(day, true);
						if (mStateListener != null) {
							mStateListener.iotvDataLoadFinish(Boolean.valueOf(mIsIOTVAvailable));
						}
						mHandler.sendEmptyMessage(MSG_START_INIT_LOCAL_JX_DATA);
						return;
					}
				}
				Logger.d("IOTV4", "init local data fail! day:" + day
						+ ", start load netdata.");
				setIotvCacheInMap(day, null);
				mDefaultChannelCache = null;

				sendEmptyMessage(MSG_START_LOAD_NET_IOTV_DATA);


			}
				break;
			case MSG_START_MONITOR:
				startMonitor();
				break;
			case MSG_UPDATE_SCHEDULE: {
				TimeUseDebugUtils.timeUsageDebug(TAG+" , MSG_UPDATE_SCHEDULE");
				String day = (String) msg.obj;
				boolean isForce = msg.arg1 == SCHEDULE_FORCE_UPDATE;

				Logger.d("IOTV4", "update schedule day = " + day
						+ " isforce = " + isForce);

				File f = new File(DataConfig.getFilePath(day));

				if (f.exists() && !isForce) {
					if (mStateListener != null) {
						mStateListener.iotvDataLoadFinish(Boolean.valueOf(mIsIOTVAvailable));
					}
					return;
				}

				List<String> tmpL = new ArrayList<String>();
				tmpL.add(day);

				//List<Channel> cache_list = getCacheData(Constants.CHANNEL_FILE);
				List<Channel> cache_list = DataTransaction.getChannelData(); //change by zjt
				mIOTVChannelCache = cache_list;

				ChannelResult rawcr = new ChannelResult();
				rawcr.setItems(cache_list);
				if(cache_list != null){
					Logger.d("request schedules, channel count = "
						+ cache_list.size());
				}

				DataTransaction.requestSchedules(rawcr, tmpL);//获取当前直播的节目单
				if (!isEmptyChannelResult(rawcr)){
					Channel c = rawcr.getItems().get(0);
					
					if (c != null && c.getSchedules() != null && c.getSchedules().size() > 0){
						// remove memory cache
						removeIotvCacheInMap(day);

						//cache_list = getCacheData(day); //change by zjt 11/4
						cache_list = DataTransaction.getChannelDateData();
						setIotvCacheInMap(day, cache_list);
						mDefaultChannelCache = cache_list;

						// set current time as the update time of main data.
						updateMainDataUpdateTime();
						initJxChannelData(day, true);
						if (mStateListener != null) {
							mStateListener.iotvDataLoadFinish(Boolean.valueOf(mIsIOTVAvailable));
						}
					}else{
						//failed to get the data of schedules
						int type = IOTVNetException.TYPE_INIT_SCHEDULEDATA;
						if(mStateListener != null){
							mStateListener.reportError(type, IOTVNetException.builder(mContext, type, R.string.jx_schedule_failed));
						}
						return;
					}
				}
			}
				break;
				
			case MSG_GET_JX_CATEGORY:
			{
				QueryScheduleRunnable work = (QueryScheduleRunnable) msg.obj;
				if(work != null){
					work.run();
				}
				break;
			}
			
			case MSG_MIXING_DATA:
			{
				Logger.d("MSG_MIXING_DATA");
				String day = (String) msg.obj;
				initJxChannelData(day, false);
				setMixing(false);
				break;
			}
			
			case MSG_START_LOAD_NET_JX_DATA:
			{
				Logger.d(TAG, "MSG_START_LOAD_NET_JX_DATA");
				initAllJxSchedule();
				if(!mIsIOTVAvailable){
					notifyLoadFinish();
				}
				clearCache();
				mHandler.sendEmptyMessage(MSG_START_MONITOR);
				break;
			}
			case MSG_START_INIT_LOCAL_JX_DATA:{
				Logger.d(TAG, "MSG_START_INIT_LOCAL_JX_DATA");
				//long dataParseStart = System.currentTimeMillis();
				TimeUseDebugUtils.timeUsageDebug(TAG+",enter  MSG_START_INIT_LOCAL_JX_DATA");
				isParsingJxSchedules = true;
				Date d = IOTV_Date.instance().getDateToday();
				String day = IOTV_Date.getDateFormatNormal(d);

				// remove memory cache
				
				//List<JxAllScheduleItem> cache_list = getJxCacheData(day); //change by zjt 11/4
				List<JxAllScheduleItem> cache_list = DataTransaction.getAllJXSchedules();
						
				TimeUseDebugUtils.timeUsageDebug(TAG+",start  local parse");
				if (cache_list != null && cache_list.size() > 0) {
					Logger.d("zjt", "cache_list size = "+cache_list.size());
					for(JxAllScheduleItem item : cache_list){
						List<JxAllScheduleKey> keys = item.getKeys();
						List<Schedule> scheduleList = new ArrayList<Schedule>();
						if(keys != null && keys.size() > 0){
							Logger.d("zjt", "keys size = "+keys.size());
							int scheduleindex = 0;
							for(JxAllScheduleKey key : keys){
								if(key.getSchedules() == null || key.getSchedules().size() == 0){
									continue;
								}
								
//								String channelCode = key.getSchedules().get(0).getChannelCode();
//								//Logger.d(TAG,"channelCode = "+channelCode);
//								
//								Channel ch = getChannelByChannelCode(channelCode);
//								String liveUrl = null;//ch.getLivePlayUrl();
//								String lookBackUrl = null;//ch.getLookbackUrl();
//								if(ch != null){
//									liveUrl = ch.getLivePlayUrl();
//									lookBackUrl = ch.getLookbackUrl();
//								}
//								//Logger.d(TAG, "liveUrl = "+liveUrl);
//								//Logger.d(TAG, "ScheduleLookbackUrl = "+lookBackUrl);
//								if(lookBackUrl != null && lookBackUrl.endsWith("&")){
//									lookBackUrl = lookBackUrl.substring(0, lookBackUrl.length()-1);
//								}
								//Logger.d(TAG, "ScheduleLookbackUrl = "+lookBackUrl);
								
								Schedule sc = new Schedule();
								if(key.getKeyname() != null && !key.getKeyname().equals("")){//有剧集名字，说明是剧集
									sc.setName(key.getKeyname());
									sc.setChannelId(new String (key.getSchedules().get(0).getChannelCode()));
									sc.setChannelCode(item.getCategorycode());
									sc.setStartTime(IOTV_Date.getCurrentTimeS());
									sc.setEndTime("20550101000000");
									int subscheduleindex = 0;
									for(Schedule subSc : key.getSchedules()){
										subSc.setScheduleIndex(subscheduleindex);
										subSc.setChannelId(new String (subSc.getChannelCode()));
										subSc.setCompleteName(key.getKeyname() + subSc.getName() + subSc.getNamePostfix());
										
										// add by zjt 2015/11/09 从直播中获取直播url和回看url
//										if(TextUtils.isEmpty(subSc.getLiveUrl())){
//											subSc.setLiveUrl(liveUrl);
//										}
//										if(TextUtils.isEmpty(subSc.getScheduleLookbackUrl())){
//											Logger.d(TAG, "..set url...");
//											subSc.setScheduleLookbackUrl(lookBackUrl);
//										}
										
										subscheduleindex++;
									}
									sc.setSubScheduleList(key.getSchedules());
									sc.setScheduleIndex(scheduleindex);
									scheduleindex++;
									scheduleList.add(sc);
								}else{//无剧集名字，说明是单条节目
									if(key.getSchedules() != null && key.getSchedules().size() > 0){
										for(Schedule sc_single: key.getSchedules()){
											sc_single.setChannelId(new String(sc_single.getChannelCode()));
											sc_single.setChannelCode(item.getCategorycode());
											sc_single.setScheduleIndex(scheduleindex);
											// add by zjt 2015/11/09 从直播中获取直播url和回看url
//											if(TextUtils.isEmpty(sc_single.getLiveUrl())){
//												sc_single.setLiveUrl(liveUrl);
//											}
//											if(TextUtils.isEmpty(sc_single.getScheduleLookbackUrl())){
//												sc_single.setScheduleLookbackUrl(lookBackUrl);
//											}
											
											scheduleindex++;
											scheduleList.add(sc_single);
										}
									}
								}
							}
						}
						if(mScheduleMap != null && item != null){
							mScheduleMap.put(item.getCategorycode(), scheduleList);
						}
						
						if(mJxCategoryResult != null){// add by zjt 11/11
							for(Channel channel : mJxCategoryResult.getItems()){
								if(channel.getChannelCode().equals(item.getCategorycode())){
									channel.setSchedules(scheduleList);
									break;
								}
							}
						}
						
					}
					Iterator i = mScheduleMap.entrySet().iterator();
					while(i.hasNext()){
					    Object o = i.next();
					    String key = o.toString();
					   //这样就可以遍历该HashMap的key值了。
					    //Log.d("MSG_START_INIT_LOCAL_JX_DATA", "mScheduleMap key: " + key);
					}
					isParsingJxSchedules = false;
					TimeUseDebugUtils.timeUsageDebug(TAG+",exit  MSG_START_INIT_LOCAL_JX_DATA");
					if(!mIsIOTVAvailable){
						notifyLoadFinish();
					}
					mHandler.sendEmptyMessage(MSG_START_MONITOR);
					return;
				}

				isParsingJxSchedules = false;
				sendEmptyMessage(MSG_START_LOAD_NET_JX_DATA);
				break;
			}
			}
		}
	}
	
	
	/**
	 * @param channelcode
	 * @return
	 */
	private Channel getChannelByChannelCode(String channelcode){
		Logger.d(TAG, "enter getChannelByChannelCode ");
		List<Channel> channel_list = DataTransaction.getChannelData();
		
		if(channel_list == null || channel_list.size() <= 0){
			DataTransaction.getCacheData(Constants.CHANNEL_FILE);
			channel_list = DataTransaction.getChannelData();
		}
		
		if(channel_list != null && channel_list.size() >0 && !TextUtils.isEmpty(channelcode)){
			for(Channel c : channel_list){
				if(c.getChannelCode().equalsIgnoreCase(channelcode)){
					return c;
				}
			}
		}
		return null;
		
	}
	
	
	
	private void initChannelData(){
		Logger.d("enter initChannelData");
		mIOTVChannelCache = null;
		//List<Channel> cache_channel_list = getCacheData(Constants.CHANNEL_FILE); change by zjt 2015/11/4 
		List<Channel> cache_channel_list = DataTransaction.getChannelData();
		
		if (cache_channel_list != null && cache_channel_list.size() > 0) {
			Logger.d(TAG, "cache_channel_list size = "+cache_channel_list.size());
			mIOTVChannelCache = cache_channel_list;
//			ChannelPlayUrlUtils.clearChannelPlayUrl();
//			for(Channel item:mIOTVChannelCache){
//				ChannelPlayUrlUtils.setChannelPlayUrl(item.getChannelCode(), item.getLookbackUrl());
//			}
		}		
	}
	private boolean initJxCategoryData(){
		mIsIOTVAvailable = false;
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter initJxCategoryData");
		try {
			//mJxCategoryResult = DataTransaction.getJXCategorys(mContext,true);//change by zjt 11/4
			mJxCategoryResult = DataTransaction.getJXCategorys();
			if(isEmptyChannelResult(mJxCategoryResult)){
				Logger.d(TAG, "local mJxCategoryResult is null , download from net");
				mJxCategoryResult = DataTransaction.getJXCategorys(mContext,true);
			}
		} catch (IOTVNetException e) {
			// TODO Auto-generated catch block
			int type = IOTVNetException.TYPE_INIT_JXCATEGORY;
			mStateListener.reportError(type, IOTVNetException.builder(
					mContext, type, R.string.jx_category_failed));
		}
		
		boolean result = !isEmptyChannelResult(mJxCategoryResult);
		
		if (result){
			int index = 0;
			for (Channel c : mJxCategoryResult.getItems()){
				if (c.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
					mIsIOTVAvailable = true;
				}
				
				c.setType(Constants.IOTV_PLAY_JINGXUAN);
				c.setChannelNo((index + 1) + "");
				index++;
			}
		}
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit initJxCategoryData");
		return result;
	}

	private void initJxChannelData(String day, boolean notifyFinish) {
		//Logger.d("enter initJxChannelData");
		TimeUseDebugUtils.timeUsageDebug(TAG+" , enter initJxChannelData");
		if (OttContextMgr.getInstance().isJXRunning()) {
			if (!isEmptyChannelResult(mJxCategoryResult)) {
				composeJXChannelData(day, mJxCategoryResult, notifyFinish);
			} else {
				// failed to get the category info of JX
				int type = IOTVNetException.TYPE_INIT_JXCATEGORY;
				mStateListener.reportError(type, IOTVNetException.builder(
						mContext, type, R.string.jx_category_failed));
				return;
			}
		}
	}

	private void updateMainDataUpdateTime() {
		long current_time = IOTV_Date.getDate().getTime();
		DataConfig.setDataConfigValueL(mContext,
				DataConfig.JX_UPDATE_DATA_TIME, current_time);
	}

	private boolean isMainDataExpire() {
		long current_time = IOTV_Date.getDate().getTime();
		long update_time = DataConfig.getDataConfigValueL(mContext,
				DataConfig.JX_UPDATE_DATA_TIME);
		// if update time plus expire time is larger than current time, means
		// data is not expired.
		if (((update_time + DataConfig.JX_MAX_EXPIRE_TIME_MAINDATA) > current_time)
				&& IOTV_Date.instance().checkDateState()) {
			Logger.d("IOTV4", "jing xuan channel data doesn't expire, so it needn't refresh");
			return false;
		} else {
			// channel load finish and then load schedules
			return true;
		}
	}

	private void composeJXChannelData(String day, ChannelResult cr, boolean notifyFinish) {
		//Logger.d("enter composeJXChannelData");
		TimeUseDebugUtils.timeUsageDebug(TAG+", enter composeJXChannelData , day = "+day);
		List<Schedule> iotvSchedules = getIOTVSchedulesByCurrentTime(day);
		if(iotvSchedules != null){
			Logger.d(TAG, "iotvSchedules.size = "+iotvSchedules.size());
		}
		// and then, set the find out data for the first channel of JX
		List<Channel> jxChannelList = cr.getItems();
		if (jxChannelList != null && jxChannelList.size() > 0) {
			for (int index = 0; index < jxChannelList.size(); index++) {
				Channel c = jxChannelList.get(index);
				if (c.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)) {
					c.setSchedules(iotvSchedules);
					c.setChannelNo((index + 1) + "");
					c.setType(Constants.IOTV_PLAY_LIVE_SOURCE);
					mScheduleMap.put(Constants.JX_LATEST_CHANNEL_CODE,iotvSchedules);
					resetIOTVSchedulesInExpireTime();
				} else {
					c.setType(Constants.IOTV_PLAY_JINGXUAN);
					c.setChannelNo((index + 1) + "");
				}
			}
		}

		// then update cache map of today
		mCacheMixedMap.put(day, jxChannelList);

		if (notifyFinish){
		    notifyLoadFinish();
		}
	}

	/**
	 * 通过二分查找法找出当前各个卫视正在直播的节目
	 * @param day
	 * @return
	 */
	private List<Schedule> getIOTVSchedulesByCurrentTime(String day) {
		TimeUseDebugUtils.timeUsageDebug(TAG+" , enter getIOTVSchedulesByCurrentTime , day = "+day);
		Logger.d(TAG, "thread_name = "+Thread.currentThread().getName());
		// searching out current playing schedule in each channel by current time
		long curTime = IOTV_Date.getDate().getTime();

		// Date d = IOTV_Date.instance().getDateToday();
		// String today = IOTV_Date.getDateFormatNormal(d);

		List<Channel> dayChannel = mCacheIotvMap.get(day);
		if (dayChannel == null) {
			dayChannel = mDefaultChannelCache;
		}

		if (dayChannel == null) {
			return null;
		}

		List<Schedule> scheduleList = new ArrayList<Schedule>();
		mIotvSchedulesEndTimeList.clear();

		if (dayChannel != null && dayChannel.size() > 0) {
			int i = 0;
			for (Channel cl : dayChannel) {
				//TimeUseDebugUtils.timeUsageDebug("enter for , size = "+ dayChannel.size());
				Schedule sl = ChannelManager.getInstance()
						.binarySearchSchedule(cl.getSchedules(), curTime);
				
				if(sl == null){//如果该频道没有获取到节目单或其他原因导致sl为空，则仍旧添加到节目列表中
					sl = new Schedule();
					sl.setStartTime(IOTV_Date.getCurrentTimeS());//starttime设为当前值，防止其他地方产生空指针异常
					sl.setEndTime("20550101000000");//endtime设一个大值，防止影响缓存
					sl.setName(cl.getChannelName());
				}
				
				sl.setChannelIcon(cl.getIcon1());
				sl.setLiveUrl(cl.getLivePlayUrl());
				sl.setScheduleLookbackUrl(cl.getLookbackUrl());

				sl.setChannelCode(Constants.JX_LATEST_CHANNEL_CODE);
				sl.setChannelId(cl.getChannelId());

				sl.setScheduleIndex(i);

				//Log.d("harish1", "schedule url = " + sl.getLiveUrl()); //close by zjt 2015/12/17
				scheduleList.add(sl);

				mIotvSchedulesEndTimeList.add(IOTV_Date.getDateParseFull(
						sl.getEndTime()).getTime());

				i++;
				//TimeUseDebugUtils.timeUsageDebug("end for");
			}
		}

		TimeUseDebugUtils.timeUsageDebug(TAG+" , end getIOTVSchedulesByCurrentTime");
		return scheduleList;
	}

	@Override
	public void setContext(Context context) {
		// TODO Auto-generated method stub
		mContext = context;
	}

	private void playAuthen(AuthParam param, int timeout) {
		Logger.d("enter play(" + param + ", " + timeout + ")");

		AuthResult result = OttContextMgr.getInstance().playAuthen(param,
				timeout);

		if (mStateListener != null) {
			Object data = null;

			if (result == null || utils.isNull(result.getPlayURL())) {
				IOTVNetException e = IOTVNetException.builder(mContext,
						IOTVNetException.TYPE_PLAY_AUTHEN,
						R.string.play_authen_failed);
				e.setErrorCode("05");
				data = e;
			} else {
				data = result.getPlayURL();
			}

			mStateListener.playAuthenFinish(data);
		}
	}

	@Override
	public void getCDNToken() {
		// TODO Auto-generated method stub
		long current_time = IOTV_Date.getDate().getTime();
		long update_time = DataConfig.getDataConfigValueL(mContext,
				DataConfig.UPDATE_CDN_TOKEN_TIME);
		// if update time plus expire time is larger than current time, means
		// data is not expired.
		if ((update_time + DataConfig.MAX_EXPIRE_TIME_CDNTOKEN) > current_time) {
			String cdntoken = DataConfig.getDataConfigValueS(mContext,
					DataConfig.CDN_TOKEN_VALUE);
			if (!cdntoken.equals("")) {
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
				// update update time and values in preference
				DataConfig.setDataConfigValueL(mContext,
						DataConfig.UPDATE_CDN_TOKEN_TIME, IOTV_Date.getDate()
								.getTime());
				DataConfig.setDataConfigValueS(mContext,
						DataConfig.CDN_TOKEN_VALUE, cdntoken);
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

		Logger.d("monitor cache file list success! currentHour = "+ currentHour);

		if (currentHour >= Constants.CHECK_START_TIME_HOUR
				&& currentHour <= Constants.CHECK_END_TIME_HOUR) {
			try {
				// check next day's file is exist or not
				// get next day
				Logger.d(TAG +" , get zhibo schedule");
				Date nextDate = IOTV_Date.instance().getDateAsHour(24);
				String nextDay = IOTV_Date.getDateFormatNormal(nextDate);

				File file = new File(DataConfig.getFilePath(nextDay));
				if (!file.exists()) {
					Logger.d("next day = " + file.getName()
							+ " is not exist, let's download it now");
					List<String> list = new ArrayList<String>();
					list.add(nextDay);

					ChannelResult cr = new ChannelResult();
					cr.setItems(mDefaultChannelCache);
					DataTransaction.requestSchedules(cr, list);

					Logger.d(TAG, "....getCacheData..");
					
					//List<Channel> cache_list = getCacheData(nextDay); //change by zjt 11/4
					List<Channel> cache_list = DataTransaction.getChannelDateData();
					setIotvCacheInMap(nextDay, cache_list);
					clearCache();
				}
			} catch (Exception e) {
			}
		}
		
		if (currentHour >= Constants.CHECK_JINGXUAN_START_TIME_HOUR
				&& currentHour <= Constants.CHECK_JINGXUAN_END_TIME_HOUR) {
			try {
				// check next day's file is exist or not
				// get next day
				Date day = IOTV_Date.instance().getDateToday();
				String today = IOTV_Date.getDateFormatNormal(day);

				File file = new File(DataConfig.getFilePath(Constants.JX_ALL_SCHEDULE_FILE + today));
				if (!file.exists()) {
					Logger.d("jingxuan today = " + file.getName()
							+ " is not exist, let's download it now");
					QueryAllJxScheduleRunnable wt = new QueryAllJxScheduleRunnable(null, today);
					wt.run();
					clearCache();
				}
			} catch (Exception e) {
			}
		}

		Random r = new Random();
		int v = r.nextInt(Constants.MONITOR_INTERVAL_MAX - Constants.MONITOR_INTERVAL_MIN);

		v = Constants.MONITOR_INTERVAL_MIN + v;
		// monitor in a random time
		mHandler.sendEmptyMessageDelayed(MSG_START_MONITOR, v);
		
		Logger.d("JXModel::startMonitor will be invoked in " + v);
	}

	/**
	 * we must make sure the cache file is normally generated.
	 * */
	private List<String> checkSuccess() {
		List<String> l = IOTV_Date.instance().getPrevDays();
		List<String> n = new ArrayList<String>();

		for (String s : l) {
			File f = new File(DataConfig.getFilePath(s));

			if (!f.exists()) {
				n.add(s);
			}
		}

		return n;
	}

	private void notifyLoadFinish() {
		List<String> iconList = new ArrayList<String>();
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter  notifyLoadFinish, mDefaultChannelCache = "+mDefaultChannelCache);
		if (mDefaultChannelCache != null) {
			for (Channel c : mDefaultChannelCache) {
				iconList.add(c.getIcon1());
			}

			mChannelIconMgr.start(iconList);
		}
		TimeUseDebugUtils.timeUsageDebug(TAG+",call  loadFinish");
		mStateListener.loadFinish(Boolean.valueOf(mIsIOTVAvailable));
		Logger.d(TAG,"end notifyLoadFinish");
	}

	private void clearCache() {
		try {
			Logger.d("start clear cache");
			List<String> ignoreFileList = new ArrayList<String>();
			ignoreFileList.addAll(IOTV_Date.instance().getPrevDays());
			Date date = IOTV_Date.instance().getDateToday();
			String today = IOTV_Date.getDateFormatNormal(date);
			Date nextDate = IOTV_Date.instance().getDateAsHour(24);
			String nextDay = IOTV_Date.getDateFormatNormal(nextDate);

			ignoreFileList.add(nextDay);
			ignoreFileList.add(Constants.CHANNEL_FILE);
			ignoreFileList.add(Constants.JX_ALL_SCHEDULE_FILE + today);
			ignoreFileList.add(Constants.JX_ALL_SCHEDULE_FILE + nextDay);
			ignoreFileList.add(Constants.JX_SUBSCRIBE_FILE);

			File f = new File(DataConfig.getFileDir());
			String[] filelist = f.list();

			List<String> deleteList = new ArrayList<String>();

			for (String s : filelist) {
				boolean isfind = false;

				for (String tmp : ignoreFileList) {
					if (s.equals(tmp + ".json")) {
						isfind = true;
						break;
					}
				}

				if (!isfind) {
					deleteList.add(s);
				}
			}

			for (String tmp : deleteList) {
				Logger.d("delete file " + tmp);
				f = new File(DataConfig.getFileDir() + File.separator + tmp);
				f.delete();
			}
		} catch (Exception e) {
		}
	}

	boolean isEmptyChannelResult(ChannelResult cr) {
		boolean b = cr != null && cr.getItems() != null
				&& cr.getItems().size() > 0;
				Logger.d(TAG, "isEmptyChannelResult , b = "+b);
		return !b;
	}

	private synchronized List<Channel> getIotvCacheFormMap(String key) {
		if (mCacheIotvMap.containsKey(key)) {
			return mCacheIotvMap.get(key);
		}

		return null;
	}

	private synchronized List<Channel> getMixedCacheFormMap(String key) {
		if (mCacheMixedMap.containsKey(key)) {
			return mCacheMixedMap.get(key);
		}

		return null;
	}

	private synchronized void removeIotvCacheInMap(String key) {
		mCacheIotvMap.remove(key);
	}

	private synchronized void setIotvCacheInMap(String key, List<Channel> value) {
		mCacheIotvMap.put(key, value);
	}

	private synchronized boolean hasIotvCacheInMap(String key) {
		return mCacheIotvMap.containsKey(key);
	}

	private void resetIOTVSchedulesInExpireTime() {
		mMainThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mScheduleMap.put(Constants.JX_LATEST_CHANNEL_CODE, null);
			}
		}, IOTV_SCHEDULE_EXPIRE_TIME);
	}

	private boolean checkIotvSchedulesIsExpired() {
		/*add by xubin 2015.11.10 begin*/
		if(mIotvSchedulesEndTimeList == null || null == IOTV_Date.getDate()){
			return false;
		}
		/*add by xubin 2015.11.10 end*/
		long curTime = IOTV_Date.getDate().getTime();
		
//		for(Long l : mIotvSchedulesEndTimeList){
//			if (curTime >= l){
//				return true;
//			}
//		}
		//change for-each to this add by zjt 2016/1/14 for ConcurrentModificationException异常
		for (int i =  0; i < mIotvSchedulesEndTimeList.size(); i++) {
			if (curTime >= mIotvSchedulesEndTimeList.get(i)){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public List<Schedule> getScheduleByCategory(String category, Message msg) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "enter getScheduleByCategory , category = "+category);
		if (category.equals(Constants.JX_LATEST_CHANNEL_CODE)) {
			Date d = IOTV_Date.instance().getDateToday();
			String today = IOTV_Date.getDateFormatNormal(d);

			List<Schedule> iotvSchedule = mScheduleMap.get(Constants.JX_LATEST_CHANNEL_CODE);
			boolean checkexpired = checkIotvSchedulesIsExpired();
			
			if (iotvSchedule == null || checkexpired) {
				Logger.d("iotv schedule data is expired, must be generated again.");
				iotvSchedule = getIOTVSchedulesByCurrentTime(today);
				mScheduleMap.put(Constants.JX_LATEST_CHANNEL_CODE, iotvSchedule);
				//resetIOTVSchedulesInExpireTime();
			}

			return iotvSchedule;
		}
		
		List<Schedule> temp = mScheduleMap.get(category);
		if (temp != null) {
			Logger.d(TAG, "getScheduleByCategory temp.size = "+temp.size());
			List<Schedule> scheduleList = utils.filterSchedulesWithPlayableSubScheduleList(temp);
			int i = 0;
			for(Schedule schedule:scheduleList){
				schedule.setScheduleIndex(i);
				i++;
			}
			return scheduleList;
		}
		return null;
	}

	class QueryScheduleRunnable implements Runnable {
		public String category;
		public Message msg;

		public QueryScheduleRunnable(String cat, Message m) {
			category = cat;
			msg = m;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<Schedule> scheduleList = DataTransaction.getJXSchedules(
						mContext, category);
				if (scheduleList != null && scheduleList.size() > 0) {
					Logger.d("zjt ", "mScheduleMap.put");
					mScheduleMap.put(category, scheduleList);
					
					if (msg != null){
					    msg.obj = scheduleList;
					    msg.sendToTarget();
					}
				}
			} catch (IOTVNetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Schedule getNextScheduleOfChannel(int ChannelIndex){
		Logger.d(TAG, " , enter getNextScheduleOfChannel , ChannelIndex = "+ChannelIndex);
		/*modify by xubin 2015.11.10 begin*/
		Schedule schedule = null;
		Date d = IOTV_Date.instance().getDateToday();
		String today = IOTV_Date.getDateFormatNormal(d);
		List<Channel> dayChannel = null;//mCacheIotvMap.get(today);
		if(mCacheIotvMap != null){
			dayChannel = mCacheIotvMap.get(today);
		}
		if (dayChannel == null || dayChannel.size() <= ChannelIndex) {
			return null;
		}
		Channel channel = dayChannel.get(ChannelIndex);
		long curTime = IOTV_Date.getDate().getTime();
		Schedule sl = null;//ChannelManager.getInstance().binarySearchSchedule(channel.getSchedules(), curTime);
		if(channel != null){
			Logger.d(TAG, "dayChannel .size = "+dayChannel.size()+" , channel name = "+channel.getChannelName());
			sl = ChannelManager.getInstance().binarySearchSchedule(channel.getSchedules(), curTime);
		}
		if (sl != null && channel != null) {
			int i = channel.getSchedules().indexOf(sl);
			Logger.d(TAG, "sl name = "+sl.getName()+" , i = "+i);
			if(i >= 0 && i < channel.getSchedules().size() - 1){
				schedule = channel.getSchedules().get(i + 1);
			}else if(i == channel.getSchedules().size() - 1){//如果当前播放的为今天最后一条节目，那么从明天的节目单中取下一条
				Log.d("lhh", "getNextScheduleOfChannel get from schedules of tomorrow");
				Date nextDate = IOTV_Date.instance().getDateAsHour(24);
				String nextDay = IOTV_Date.getDateFormatNormal(nextDate);
				List<Channel> nextdayChannel = null;//mCacheIotvMap.get(nextDay);
				if(mCacheIotvMap != null){
					nextdayChannel = mCacheIotvMap.get(nextDay);
				}
				if (nextdayChannel == null || nextdayChannel.size() <= ChannelIndex) {
					return null;
				}
				Channel channel_tomorrow = nextdayChannel.get(ChannelIndex);
				if(channel_tomorrow != null && channel_tomorrow.getSchedules() != null 
						&& channel_tomorrow.getSchedules().size() > 0){
					schedule = channel_tomorrow.getSchedules().get(0);
				}
				Log.d("lhh", "getNextScheduleOfChannel schedule:" + schedule.getName());
			}
		}
		/*modify by xubin 2015.11.10 end*/
		return schedule;
	}
	
	/**
	 * 加载所有分类（除最新）的节目单，供订阅功能使用
	 */
	public void initAllJxSchedule(){
		QueryAllJxScheduleRunnable wt = new QueryAllJxScheduleRunnable(null);
		wt.run();
	}
	
	class QueryAllJxScheduleRunnable implements Runnable {
		public Message msg;
		public String mDay;
		public QueryAllJxScheduleRunnable(Message m) {
			msg = m;
		}
		public QueryAllJxScheduleRunnable(Message m, String day) {
			msg = m;
			mDay = new String(day);
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				isParsingJxSchedules = true;
				List<JxAllScheduleItem> categoryList = DataTransaction.getAllJXCategorysAndSchedules(
						mContext, mDay);
				if (categoryList != null && categoryList.size() > 0) {
					for(JxAllScheduleItem item : categoryList){
						
						List<JxAllScheduleKey> keys = item.getKeys();
						List<Schedule> scheduleList = new ArrayList<Schedule>();
						if(keys != null && keys.size() > 0){
							int scheduleindex = 0;
							for(JxAllScheduleKey key : keys){
								if(key.getSchedules() == null || key.getSchedules().size() == 0){
									continue;
								}
								
//								String channelCode = key.getSchedules().get(0).getChannelCode();
//								Logger.d(TAG,"channelCode = "+channelCode);
//								
//								Channel ch = getChannelByChannelCode(channelCode);
//								String liveUrl = null;//ch.getLivePlayUrl();
//								String lookBackUrl = null;//ch.getLookbackUrl();
//								if(ch != null){
//									liveUrl = ch.getLivePlayUrl();
//									lookBackUrl = ch.getLookbackUrl();
//								}
//								//Logger.d(TAG, "liveUrl = "+liveUrl);
//								//Logger.d(TAG, "ScheduleLookbackUrl = "+lookBackUrl);
//								if(lookBackUrl != null && lookBackUrl.endsWith("&")){
//									lookBackUrl = lookBackUrl.substring(0, lookBackUrl.length()-1);
//								}
//								Logger.d(TAG, "ScheduleLookbackUrl = "+lookBackUrl);
								
								Schedule sc = new Schedule();
								if(key.getKeyname() != null && !key.getKeyname().equals("")){//有剧集名字，说明是剧集
									sc.setName(key.getKeyname());
									sc.setChannelCode(item.getCategorycode());
									sc.setStartTime(IOTV_Date.getCurrentTimeS());
									sc.setEndTime("20550101000000");
									int subscheduleindex = 0;
									for(Schedule subSc : key.getSchedules()){
										subSc.setScheduleIndex(subscheduleindex);
										subSc.setCompleteName(key.getKeyname() + subSc.getName() + subSc.getNamePostfix());
										//add by zjt 2015/11/09 从直播列表中获取直播url和回看url
//										if(TextUtils.isEmpty(subSc.getLiveUrl())){
//											subSc.setLiveUrl(liveUrl);
//										}
//										if(TextUtils.isEmpty(subSc.getScheduleLookbackUrl())){
//											subSc.setScheduleLookbackUrl(lookBackUrl);
//										}
										subscheduleindex++;
									}
									sc.setSubScheduleList(key.getSchedules());
									sc.setScheduleIndex(scheduleindex);
									scheduleindex++;
									scheduleList.add(sc);
								}else{//无剧集名字，说明是单条节目
									if(key.getSchedules() != null && key.getSchedules().size() > 0){
										for(Schedule sc_single: key.getSchedules()){
											sc_single.setChannelCode(item.getCategorycode());
											sc_single.setScheduleIndex(scheduleindex);
											//add by zjt 2015/11/09 从直播列表中获取直播url和回看url
//											if(TextUtils.isEmpty(sc_single.getLiveUrl())){
//												sc_single.setLiveUrl(liveUrl);
//											}
//											if(TextUtils.isEmpty(sc_single.getScheduleLookbackUrl())){
//												sc_single.setScheduleLookbackUrl(lookBackUrl);
//											}
											scheduleindex++;
											scheduleList.add(sc_single);
										}
									}
								}
							}
						}
						if(mScheduleMap != null && item != null){
							mScheduleMap.put(item.getCategorycode(), scheduleList);
						}
						/*modify by xubin 2015.11.10 begin*/
						if(mJxCategoryResult != null && item != null){
							for(Channel channel : mJxCategoryResult.getItems()){
								if(channel.getChannelCode().equals(item.getCategorycode())){
									channel.setSchedules(scheduleList);
									break;
								}
							}
						}
						/*modify by xubin 2015.11.10 end*/
					}
					
				}
				isParsingJxSchedules = false;
			} catch (IOTVNetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	@Override
//	public List<Schedule> getLatestSubscribeSchedules(
//			List<Schedule> subscribeSchedules) {
//		List<Schedule> latestSubscribeSchedules = new ArrayList<Schedule>();
//		if(subscribeSchedules == null){
//			return null;
//		}
//		
//		if(mScheduleMap.size() == 0 || isParsingJxSchedules){//如果mScheduleMap还未准备就绪，则直接返回原订阅内容
//			return subscribeSchedules;
//		}
//		
//		for(Schedule subscribeSchedule : subscribeSchedules){
//			//找对应频道中的所有节目
//			List<Schedule> scList = mScheduleMap.get(subscribeSchedule.getOriginalChannelCode());//找到mScheduleMap中对应ChannelCode的ScheduleList
//			
//			if(scList != null){
//				boolean exist = false;//现有节目单中是否还存在该条节目
//				for(Schedule cacheSchedule : scList){
//					if(cacheSchedule.getName().equals(subscribeSchedule.getName())){//根据name，找到订阅的schedule
//						
//						Schedule newSubscribeSchedule = (Schedule)cacheSchedule.clone();
//						newSubscribeSchedule.setChannelCode(subscribeSchedule.getChannelCode());
//						newSubscribeSchedule.setOriginalChannelCode(subscribeSchedule.getOriginalChannelCode());
//						newSubscribeSchedule.setSubscribeTime(subscribeSchedule.getSubscribeTime());
//						
//						//节目的最后访问时间
//						newSubscribeSchedule.setLastVisitedTime(subscribeSchedule.getLastVisitedTime());
//						//子剧集最后访问时间
//						for(Schedule newSub:newSubscribeSchedule.getSubScheduleList()){
//							for(Schedule sub:subscribeSchedule.getSubScheduleList()){
//								if(newSub.getName().equals(sub.getName())&&newSub.getNamePostfix().equals(sub.getNamePostfix())){
//									newSub.setLastVisitedTime(sub.getLastVisitedTime());
//									break;
//								}
//							}
//						}
//						
//						
//						if(newSubscribeSchedule.getSubScheduleList() != null){//将还未到时间的subSchedule过滤掉，并且将更新的subSchedule的subscribe状态设为UPDATE
//							//List<Schedule> subList = new ArrayList<Schedule>(newSubscribeSchedule.getSubScheduleList());
////							if(utils.filterPlayableSchedulesAndMarkUpdate(newSubscribeSchedule.getSubScheduleList(), newSubscribeSchedule.getSubscribeTime())){
////								newSubscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_UPDATE);
////							}
//							
//							//add shensong
//							List<Schedule> playableSchedules=utils.filterPlayableSchedules(newSubscribeSchedule.getSubScheduleList());
//							
//							if(null!=playableSchedules&&playableSchedules.size()>0){
//								String subMaxTime=playableSchedules.get(0).getStartTime();//所有子节目的最晚开始时间
//								
//								//保存上次保留的子剧集的播放记录
//								for(Schedule sch:playableSchedules){
//									//单个剧集的更新状态
//									if(sch.getLastVisitedTime().equals("")){//没有访问过，为空
//										sch.setLastVisitedTime(sch.getStartTime());
//									}
//									if(sch.getLastVisitedTime().compareTo(sch.getStartTime())>0){//如果访问时间晚于播放时间的话，将其设置已观看
//										sch.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_NORMAL);
//									}else{//设置为更新状态
//										sch.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_UPDATE);
//									}
//									if(subMaxTime.compareTo(sch.getStartTime())<0) subMaxTime=sch.getStartTime();
//								}
//								
//								//整个节目的更新状态
//								if(newSubscribeSchedule.getLastVisitedTime().equals("")){
//									newSubscribeSchedule.setLastVisitedTime(subMaxTime);
//								}
//								if(newSubscribeSchedule.getLastVisitedTime().compareTo(subMaxTime)>0){
//									newSubscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_NORMAL);
//								}else{
//									newSubscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_UPDATE);
//								}
//								newSubscribeSchedule.setSubScheduleList(playableSchedules);
//							}
//						}
//						
//						latestSubscribeSchedules.add(newSubscribeSchedule);
//						exist = true;
//						break;
//					}
//				}
//				if(!exist){
//					subscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_INVALID);
//					latestSubscribeSchedules.add(subscribeSchedule);
//				}
//			}else{//如果没有对应的ChannelCode，说明该分类已下线，订阅状态设为失效
//				subscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_INVALID);
//				latestSubscribeSchedules.add(subscribeSchedule);
//			}
//		}
//		return latestSubscribeSchedules;
//	}
	
	
	//更新订阅节目单的状态
	//1.获得订阅的节目中的所有子节目（不再将未到时间的节目过滤掉）
	//2.如果子节目单中存在更新前的子节目，将更新前子节目的状态复制过来
	public List<Schedule> getLatestSubscribeSchedules(List<Schedule> subscribeSchedules){
		
		List<Schedule> latestSubscribeSchedules = new ArrayList<Schedule>();
		
		if(subscribeSchedules == null){
			return null;
		}
		
		if(mScheduleMap.size() == 0 || isParsingJxSchedules){//如果mScheduleMap还未准备就绪，则直接返回原订阅内容
			return subscribeSchedules;
		}
		
		for(Schedule subscribeSchedule : subscribeSchedules){
			//找对应频道中的所有节目
			List<Schedule> scList = getScheduleByCategory(subscribeSchedule.getOriginalChannelCode(), null);
			if(scList != null){
				boolean exist = false;//现有节目单中是否还存在该条节目
				for(Schedule cacheSchedule : scList){
					Logger.d(TAG, "getLatestSubscribeSchedules , cacheSchedule.name = "+cacheSchedule.getName()+", size = "+scList.size());
					if(cacheSchedule.getName().equals(subscribeSchedule.getName())){//根据name，找到订阅的schedule
						
						Schedule newSubscribeSchedule = (Schedule)cacheSchedule.clone();
						newSubscribeSchedule.setChannelCode(subscribeSchedule.getChannelCode());
						newSubscribeSchedule.setOriginalChannelCode(subscribeSchedule.getOriginalChannelCode());
						newSubscribeSchedule.setSubscribeTime(subscribeSchedule.getSubscribeTime());
						newSubscribeSchedule.setScheduleIndex(subscribeSchedule.getScheduleIndex());
						//节目的最后访问时间
						newSubscribeSchedule.setLastVisitedTime(subscribeSchedule.getLastVisitedTime());
						//设置子剧集
						//List<Schedule> playableSchedules=utils.filterPlayableSchedules(newSubscribeSchedule.getSubScheduleList());
						newSubscribeSchedule.setSubScheduleList(combineSubSchedules(subscribeSchedule.getSubScheduleList(), newSubscribeSchedule.getSubScheduleList()));
						
						if (newSubscribeSchedule.getSubScheduleList() != null) {// 将更新的subSchedule的subscribe状态设为UPDATE
							String firstPlayableScheduleEndTime = utils
									.getEndTimeOfTheFirstPlayableSchedule(newSubscribeSchedule
											.getSubScheduleList());// 子集中最新一条可播节目的结束时间
							Logger.d(TAG, "t1 = "+firstPlayableScheduleEndTime+" , t2 = "+subscribeSchedule.getLastVisitedTime());
							if (subscribeSchedule.getLastVisitedTime()
									.compareTo(firstPlayableScheduleEndTime) < 0 // 在上次焦点经过该节目后，又有更新的子集
									&& utils.markSubscribeStatus(
											newSubscribeSchedule.getSubScheduleList(),
											newSubscribeSchedule.getSubscribeTime())) {
								newSubscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_UPDATE);
							} else {
								newSubscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_NORMAL);
							}
						}
						latestSubscribeSchedules.add(newSubscribeSchedule);
						exist = true;
						break;
					}
				}
				if(!exist){
					Log.d("getLatestSubscribeSchedules", subscribeSchedule.getOriginalChannelCode() + " in mScheduleMap does not have : " + subscribeSchedule.getName());
					subscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_INVALID);
					subscribeSchedule.setSubScheduleList(null);
					latestSubscribeSchedules.add(subscribeSchedule);
				}
			}else{//如果没有对应的ChannelCode，说明该分类已下线，订阅状态设为失效
				Log.d("getLatestSubscribeSchedules", "mScheduleMap does not have key: " + subscribeSchedule.getOriginalChannelCode());
				
				Iterator i = mScheduleMap.entrySet().iterator();
				while(i.hasNext()){
				    Object o = i.next();
				    String key = o.toString();
				   //这样就可以遍历该HashMap的key值了。
				   // Log.d("getLatestSubscribeSchedules", "mScheduleMap key: " + key);
				}
				subscribeSchedule.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_INVALID);
				subscribeSchedule.setSubScheduleList(null);
				latestSubscribeSchedules.add(subscribeSchedule);
			}
		}
		return latestSubscribeSchedules;
	}
	
	/**
	 * 将内存中的子节目单更新的部分拼接至订阅节目的子节目单,并去除失效的部分
	 * @param subscribeSubScheduleList 订阅节目的子节目单
	 * @param newSubScheduleList 新的子节目单
	 * @return
	 */
	private List<Schedule> combineSubSchedules(List<Schedule> subscribeSubScheduleList,
			List<Schedule> newSubScheduleList) {
		Logger.d(TAG, "enter combineSubSchedules");
		if(newSubScheduleList == null){
			return null;
		}else if(subscribeSubScheduleList == null || subscribeSubScheduleList.size() == 0){
			return newSubScheduleList;
		}else{
			
			Logger.d(TAG, "subscribeSubScheduleList size = "+subscribeSubScheduleList.size()
					+" , newSubScheduleList.size = "+newSubScheduleList.size());
			
			List<Schedule> subList_pre = new ArrayList<Schedule>();
			List<Schedule> subList_post = new ArrayList<Schedule>();
			int index;
			boolean exist = false;
			Schedule firstScheduleOfSubscribe = subscribeSubScheduleList.get(0);
			Logger.d(TAG, "name1 = "+firstScheduleOfSubscribe.getName()+" , NamePostfix1 = "+firstScheduleOfSubscribe.getNamePostfix());
			for(index = 0; index < newSubScheduleList.size();index++){
				Logger.d(TAG, "name2 = "+newSubScheduleList.get(index).getName()+", NamePostfix2 = "+ newSubScheduleList.get(index).getNamePostfix());
				if(newSubScheduleList.get(index).getName().equals(firstScheduleOfSubscribe.getName())
						&& newSubScheduleList.get(index).getNamePostfix().equals(firstScheduleOfSubscribe.getNamePostfix())){
					exist = true;
					break;
				}
			}
			
			int index2;
			boolean exist2 = false;
			Schedule lastScheduleOfNew = newSubScheduleList.get(newSubScheduleList.size() - 1);
			Logger.d(TAG, "name3 = "+lastScheduleOfNew.getName()+" , NamePostfix3 = "+lastScheduleOfNew.getNamePostfix());
			for(index2 = subscribeSubScheduleList.size() - 1; index2 >= 0;index2--){
				Logger.d(TAG, "name4 = "+subscribeSubScheduleList.get(index2).getName()+" , NamePostfix4 = "+subscribeSubScheduleList.get(index2).getNamePostfix());
				if(subscribeSubScheduleList.get(index2).getName().equals(lastScheduleOfNew.getName())
						&& subscribeSubScheduleList.get(index2).getNamePostfix().equals(lastScheduleOfNew.getNamePostfix())){
					exist2 = true;
					break;
				}
			}
			Logger.d(TAG, "exist = "+exist+", exist2 = "+exist2+" ,index = "+index+" , index2 = "+index2);
			if(exist){
				subList_pre = new ArrayList<Schedule>(newSubScheduleList.subList(0, index));
			}
			if(exist2){
				subList_post = new ArrayList<Schedule>(subscribeSubScheduleList.subList(0, index2 + 1));
			}
			if(subList_pre.size() > 0){
				subList_pre.addAll(subList_post);
				int i = 0;
				for(Schedule ch:subList_pre){
					ch.setScheduleIndex(i);
					i++;
				}
			}else{
				subList_pre.addAll(subList_post);
			}
			return subList_pre;
		}
	}
	
	private final String openLocalFile(String filePath){
		File dir = new File(filePath);
		if (dir.exists()) {
			InputStreamReader reader = null;
			StringBuffer buffer = new StringBuffer();
			try {
				FileInputStream fis = new FileInputStream(dir);
				reader = new InputStreamReader(fis, "utf-8");
				char [] c = new char[1024];
				int length;
				while((length = reader.read(c)) != -1){
					buffer.append(c, 0, length);
				}
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
				//LogUtils.debug(e);
			} finally{
				try {
					reader.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return buffer.toString();
		} else {
			//LogUtils.debug("this file does not exist !!!");
			return null;
		}
	}
	
	
	private List<JxAllScheduleItem> getJxCacheData(String day) {
		Logger.d("start getJxCacheData, day :" + day);
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter  getJxCacheData");
		String path = DataConfig.getFilePath(Constants.JX_ALL_SCHEDULE_FILE + day);

		List<JxAllScheduleItem> listCategory = null;

		try {
//			File f = new File(path);
//			if (f.exists()) {
//				FileInputStream input = new FileInputStream(f);
//
//				byte data[] = new byte[input.available()];
//
//				input.read(data);
//				input.close();
//
//				String jsondata = new String(data);
//				TimeUseDebugUtils.timeUsageDebug(TAG+",call  gson.fromJson");
//				JxAllScheduleResult catItem = JsonUtils.ObjFromJson(jsondata, JxAllScheduleResult.class);
//				TimeUseDebugUtils.timeUsageDebug(TAG+",after  gson.fromJson");
//				
//				listCategory = catItem.getItems();
//			}
			
			String jsondata = openLocalFile(path);
			TimeUseDebugUtils.timeUsageDebug(TAG+",call  gson.fromJson");
			JxAllScheduleResult catItem = JsonUtils.ObjFromJson(jsondata, JxAllScheduleResult.class);
			TimeUseDebugUtils.timeUsageDebug(TAG+",after  gson.fromJson");
			
			listCategory = catItem.getItems();
		} catch (Exception e) {
			listCategory = null;
		}
		Logger.d("end getJxCacheData");
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit  getJxCacheData");
		
		return listCategory;
	}

	@Override
	public void refreshData(String day, Message msg) {
		// TODO Auto-generated method stub
		Logger.d("enter refreshData, " + day);
		if(isRefreshingData){
			return;
		}
		isRefreshingData = true;
		File file = new File(DataConfig.getFilePath(day));
		if (!file.exists()) {
			Logger.d("day = " + file.getName()
					+ " is not exist, let's download it now");
			List<String> list = new ArrayList<String>();
			list.add(day);

			ChannelResult cr = new ChannelResult();
			cr.setItems(mDefaultChannelCache);
			DataTransaction.requestSchedules(cr, list);

			Logger.d(TAG,"///////////////");
			//List<Channel> cache_list = getCacheData(day); //cahge by zjt 11/4
			List<Channel> cache_list = DataTransaction.getChannelDateData();
			setIotvCacheInMap(day, cache_list);
		}
		
		File file_jx = new File(DataConfig.getFilePath(Constants.JX_ALL_SCHEDULE_FILE + day));
		if (!file_jx.exists()) {
			Logger.d("jingxuan today = " + file.getName()
					+ " is not exist, let's download it now");
			QueryAllJxScheduleRunnable wt = new QueryAllJxScheduleRunnable(null, day);
			wt.run();
		}
		initJxChannelData(day, false);
		if(msg != null){
			msg.sendToTarget();
		}
		isRefreshingData = false;
	}

	@Override
	public List<Channel> getIotvChannelListByDay(String today) {
		return getIotvCacheFormMap(today);
	}
}
