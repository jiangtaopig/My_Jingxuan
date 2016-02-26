package com.bestv.ott.jingxuan;

import java.io.File;
import java.security.acl.Owner;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.JXSubscription;
import com.bestv.ott.jingxuan.data.RecommendItem;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.iotvlogservice.logmanager.LogUploadReceiver;
import com.bestv.ott.jingxuan.livetv.IOTVBuilder;
import com.bestv.ott.jingxuan.livetv.IOTVDialog;
import com.bestv.ott.jingxuan.livetv.IOTVPlayRecords;
import com.bestv.ott.jingxuan.livetv.JingxuanCacheService;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.livetv.ShortcutKeyMgr;
import com.bestv.ott.jingxuan.livetv.controller.IOTVControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.model.IDataModel;
import com.bestv.ott.jingxuan.model.IStateChangeListener;
import com.bestv.ott.jingxuan.net.HttpUtils;
import com.bestv.ott.jingxuan.net.IOTVNetException;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.TimeUseDebugUtils;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.view.IViewBase;
import com.bestv.ott.jingxuan.view.IViewFactoryBase;
import com.bestv.ott.jingxuan.view.MediaControlView;
import com.bestv.ott.jingxuan.view.RestPromptView;
import com.bestv.ott.jingxuan.view.RestPromptView.ResetViewShowListener;
import com.bestv.ott.mediaplayer.BesTVPlayerStatus;
import com.bestv.ott.mediaplayer.LogReport;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer.VideoPlayLogItemInfo;
import com.bestv.ott.proxy.qos.QosLogManager;
import com.bestv.ott.proxy.qos.VideoPlayLog;

public class JXController extends IOTVControllerBase implements SurfaceHolder.Callback{
	private static final String TAG = "JXController";
	private Activity mOwner;
	private IDataModel mDataMgr;
	
	static final int IOTV_MAIN_VIEW = 9999;
	static final int BACK_VIEW = IViewFactoryBase.TYPE_BACK_VIEW;
	static final int PAUSE_VIEW = IViewFactoryBase.TYPE_PAUSE_VIEW;
	static final int PLAY_COMPLETE_VIEW = IViewFactoryBase.TYPE_PLAY_COMPLETE_VIEW;
	static final int LOADING_VIEW = IViewFactoryBase.TYPE_LOADING_VIEW;
	static final int CHANNEL_LIST_VIEW = IViewFactoryBase.TYPE_JINGXUAN_CHANNEL_LIST_VIEW;
	static final int MEDIACONTROL_VIEW = IViewFactoryBase.TYPE_MEDIACONTROL_VIEW;
	static final int JINGXUAN_FAKE_LOADING_VIEW = IViewFactoryBase.TYPE_JINGXUAN_FAKE_LOADING_VIEW;
	
	private static final int EXPIRE_HIDE_TIME = 10000;
	private static final int[] expireHideViews = {CHANNEL_LIST_VIEW};
	
	//the time to delay start play if surface view is not created yet.
	public static final int WAIT_SURFACE_INIT = 500;
	//the interval time to ignore buffering state after first playing state is invoked.
	public static final int INGORE_BUFFERING_TIME = 2000;
	//the delay time to determine user input channel finish
	public static final int CHANNEL_INPUT_DELAY = 2000;
	//the max length of channel number
	private static final int CHANNEL_NUMBER_MAX = 4;
	//the time to show channel number
	public static final int CHANNEL_INPUT_SHOW_TIME = 2000;
	//the interval time to retry play
	public static final int RETRY_PLAY_TIME = 10000;
	
	public static final int MSG_DATALOAD_FINISH = 1000;
	public static final int MSG_START_PLAYURL = 1001;
	public static final int MSG_CHANNEL_INPUT = 1002;
	public static final int MSG_CHANNEL_CLEAR = 1003;
	public static final int MSG_RETRY_PLAY_AFTER_ERROR = 1004;
	//auto hide specified view if no key events are generated in ten seconds time.
	public static final int MSG_AUTO_HIDEVIEW = 1005; 
	public static final int MSG_AUTO_HIDELISTVIEW = 1006; 

	public static final int MSG_REFRESH_DATA_FINISH = 1007;
	
	public static final int JX_MSG_STOP_LIVE = 2001; //精选直播节目endtime到了之后显示加载页面并暂停
	public static final int JX_MSG_START_LIVE = 2002; //精选直播节目endtime到了之后显示加载页面3秒之后继续直播
	public static final int JX_MSG_SHOW_EXIT_TOAST = 2003;
	public static final int JX_MSG_SHOW_NEXT_TOAST = 2004;  //当前节目播放完之前，显示“下一节目”信息
	public static final int JX_MSG_HIDE_NEXT_TOAST = 2005;  //隐藏已经显示的“下一节目”信息
	
	public static final int AIRPLAY_TOAST_DISPLAY_TIME = 3000;//exitToast显示的时长
	
	// zhangchao add for YDJD
	private static final String EPG_USER_GROUP = "epg.usergroup";
	private static final String EPG_USER_TOKEN = "epg.token";
		
	//default start type
	private int mStartType = START_BY_NORMAL;
	//default start schedule
	private Schedule mStartSchedule;
	//default start date
	private String mStartDate;
	//the start types of iotv activity by intent
	private static final int START_BY_NORMAL = 0;
	private static final int START_BY_CHANNELCODE = 1;
	private static final int START_BY_CHANNELSCHEDULE = 2;
	
	private int current_channel_index = 0; //current channel index in current channel list
	private Channel currentChannel;        //current play channel
	
	private Channel mPrevChannel;
	private int mPrevChannelIndex;
	private Schedule mPrevSchedule;
	private int mPrevIOTVType;
	private String mPrevPlayUrl;
	
	private ChannelDay currentChannels;    //current channel list and it's date.
	private String mCurrentPlayUrl;        //current play url
	
	private ViewGroup mainLayer; 
	private SurfaceHolder surfaceHolder;
	private SurfaceView surfaceView;
	private IOTVMediaPlayer mIOTVPlayer;
	private IOTVPlayRecords mPlayRecords; //use to save play records
	private Toast mSeekPromptToast;
	
	private VideoPlayLog mPlaylog;
	
	private boolean isSurfaceViewCreated = false;
	
	private HashMap<Integer, int[]> mCoexistViewsMap = new HashMap<Integer, int[]>();
	private IViewBase[] baseViews;
	private TextView mChannleNumView;
	//private RestPromptView mRestPromptView;
	
	//only valid in look back mode.
	private Schedule currentSchedule;
	private Schedule currentSubSchedule;
	private long mStartTime; //in second
	private long mEndTime; //in second
	//indicates current play type, live source or look back.
	private int mIOTV_Type = Constants.IOTV_PLAY_LIVE_SOURCE;

	//save system time when first playing is call-backed.
	private long first_playing_timestamp = 0;
	private boolean mForceRefresh = false;  //force refresh channel data
	private boolean mIsCompleteViewShowed = false;   //only show once in each play.
	private boolean mIsLoadingFinish = false;
	private int mCurrentNetworkType;
	private IOTVDialog mErrorDlg;
	private boolean mIsFirstRun;
	
	//indicates that IOTV is available or not
	private Boolean mIsIOTVAvailable;
	
	private ShortcutKeyMgr mShotcutKeyMgr;
	private Toast mExitToast; 
	private Toast nextScheduleToast;   //下一节目信息Toast
	private long exitTime = 0;
	
	private boolean isResumeExcuted = false;
	
	IOTVMediaPlayer.IOTVMediaPlayerEventL mMediaPlayerEventListener;
	//用于VideoLoadingView中显示下一条节目
	private Schedule mNextSchedule = null;
	class ChannelDay{
		String day;
		List<Channel> list;
	}
	
	IStateChangeListener mModelStateChange = new IStateChangeListener() {
		@Override
		public void sheduleListUpdate(final String day, List<Channel> clist) {
		}
			
		@Override
		public void channelListUpdate(List<Channel> clist) {
			// TODO Auto-generated method stub
		}

		@Override
		public void loadFinish(Object extraData) {
			// TODO Auto-generated method stub
			TimeUseDebugUtils.timeUsageDebug(TAG+",loadFinish , mStartType = "+mStartType);
			
			if (mStartType == START_BY_NORMAL || mStartType == START_BY_CHANNELCODE){
				Message msg = mHandler.obtainMessage(MSG_DATALOAD_FINISH);
				msg.arg1 = mStartType;
				msg.obj = extraData;
				msg.sendToTarget();
			}
			
			if (mStartType == START_BY_CHANNELSCHEDULE){
				List<Channel> list = mDataMgr.getChannelsSync(mStartDate, false);
				
				if (list != null && list.size() > 0 && list.get(0).getSchedules() != null){
					Message msg = mHandler.obtainMessage(MSG_DATALOAD_FINISH);
					msg.arg1 = mStartType;
					msg.obj = extraData;
					msg.sendToTarget();
					
					//reset start type to normal
					mStartType = START_BY_NORMAL;
				}
			}
			
		    mCurrentNetworkType = HttpUtils.checkNetworkStatus(mOwner);
		}

		@Override
		public void reportError(int type, IOTVNetException exception) {
			// TODO Auto-generated method stub
			showErrorPrompt(exception);
		}

		@Override
		public void playAuthenFinish(Object data) {
			// TODO Auto-generated method stub
			//donePlayAuthenFinish(data);
			Message msg = mHandler.obtainMessage(MSG_START_PLAYURL);
			msg.obj = data;
			msg.sendToTarget();
		}

		@Override
		public void iotvDataLoadFinish(Object extraData) {

//			if(checkSuccess1()){
			if (extraData != null && extraData instanceof Boolean){
				mIsIOTVAvailable = (Boolean)extraData;
			}else{
				mIsIOTVAvailable = true;
			}
			String day = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
			TimeUseDebugUtils.timeUsageDebug("iotvDataLoadFinish"+" , mIsIOTVAvailable : "+mIsIOTVAvailable+ " day = "+day);
			//Log.d("iotvDataLoadFinish", "mIsIOTVAvailable : " + mIsIOTVAvailable);
			
			currentChannels = new ChannelDay();
			currentChannels.day = day;
			currentChannels.list = mDataMgr.getChannelsSync(currentChannels.day, false);
			current_channel_index = 0;
			
			TimeUseDebugUtils.timeUsageDebug("iotvDataLoadFinish ,.......");
			/*modify by xubin 2015.11.10 begin*/
			
			if(currentChannels.list != null){
				Log.d("iotvDataLoadFinish", "currentChannels.list : " + currentChannels.list);
				if(mIsIOTVAvailable){//如果有“最新”分类
					int i = 0;
					for(Channel c : currentChannels.list){//找到“最新”所在的位置
						if(c.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
							current_channel_index = i;
							break;
						}
						i++;
					}
					currentChannel = currentChannels.list.get(current_channel_index);	
				}else{//如果没有“最新”分类
					currentChannel = currentChannels.list.get(1);  //由于index = 0 为“订阅”,如果没有“最新”，播index = 1。
				}
			}
			/*modify by xubin 2015.11.10 end*/
			showView(CHANNEL_LIST_VIEW, true);
			if (mHandler.hasMessages(MSG_AUTO_HIDEVIEW)) {
				mHandler.removeMessages(MSG_AUTO_HIDEVIEW);
			}

			mHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDEVIEW,
					EXPIRE_HIDE_TIME);
//			}else{
//				showSeekPromptToast(R.string.loadingSchedule_str);
//			}
			TimeUseDebugUtils.timeUsageDebug("end iotvDataLoadFinish ..."+current_channel_index);
		}
	};
	
	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mOwner.registerReceiver(mBroadcastReceiver, filter);
	}
	
	private void unregisterBraodcast(){
		mOwner.unregisterReceiver(mBroadcastReceiver);
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d("IOTV_Activity onReceive action = " + intent.getAction());

			if (intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (mIsFirstRun){
					mIsFirstRun = false;
					return;
				}
				
				int networkstatus = HttpUtils.checkNetworkStatus(context);

				if (networkstatus != -1) {
					if (mErrorDlg != null){
						mErrorDlg.dismiss();
						mErrorDlg = null;
						mDataMgr.start(mForceRefresh);
					}
				} else {
					if (mErrorDlg != null && mErrorDlg.isShowing()){
						return;
					}
					
					if (mCurrentNetworkType == ConnectivityManager.TYPE_ETHERNET){
					    IOTVNetException e = IOTVNetException.builder(context, IOTVNetException.TYPE_NETWORK_UNAVAILABLE, R.string.network_error_by_ethernet);
					    showErrorPrompt(e);
					}else{
					    IOTVNetException e = IOTVNetException.builder(context, IOTVNetException.TYPE_NETWORK_UNAVAILABLE, R.string.network_error);
					    showErrorPrompt(e);
					}
				}
			}else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				Logger.d("iotv was received A screen off boradcast");
				mOwner.finish();
			}
		}
	};
	
	private void showErrorPrompt(IOTVNetException exception) {
		if (exception.getType() == IOTVNetException.TYPE_PLAY_AUTHEN){
			//showSeekPromptToast(exception.getMessage() + exception.getCompleteErrorCode());
			return;
		}
		
		IOTVDialog.Builder builder = new IOTVDialog.Builder(mOwner);
		builder.setTitle(exception.getMessage());
		
		String completeCode = exception.getCompleteErrorCode();
		Logger.d("IOTV9", "compltecode = " + completeCode);
		if (completeCode.length() > 0){
			completeCode = mOwner.getResources().getString(R.string.error_Code) + ": " + completeCode; 
		}
		builder.setMessage(completeCode);
		builder.setNegativeButton(R.string.app_exit,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mErrorDlg.dismiss();
						exitApp();
					}
				});
		
		mErrorDlg = builder.createMessage();
		mErrorDlg.setCancelable(false);
		//change by zjt 2015/11/12 判断activity是否存在，不存在就不弹框
		if(!mOwner.isFinishing()){
			mErrorDlg.show();
		}
		
	}
	
    Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case MSG_DATALOAD_FINISH:
			{
				Logger.d("channel data load finish!!");
				//showView(CHANNEL_LIST_VIEW, true);
				//mHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDELISTVIEW, 5000);

				if (!isSurfaceViewCreated){
					sendEmptyMessageDelayed(MSG_DATALOAD_FINISH, WAIT_SURFACE_INIT);
					return;
				}
				

				mForceRefresh = false;
				mIsLoadingFinish = true;
				
				if (msg.obj != null && msg.obj instanceof Boolean){
					mIsIOTVAvailable = (Boolean)msg.obj;
				}else{
					mIsIOTVAvailable = true;
				}
				String day = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
				if (msg.arg1 == START_BY_CHANNELSCHEDULE){
					day = mStartDate;
				}
				//initialize the channel today
				currentChannels = new ChannelDay();
				currentChannels.day = day;
				currentChannels.list = mDataMgr.getChannelsSync(currentChannels.day, false);
//				if(currentChannels.list != null){
//					Logger.d("currentChannels.list size = " + currentChannels.list.size());
//				}
				current_channel_index = 0;
				if(currentChannels.list != null){
					Logger.d("currentChannels.list size = " + currentChannels.list.size());
					if(mIsIOTVAvailable){//如果有“最新”分类
						int i = 0;
						for(Channel c : currentChannels.list){//找到“最新”所在的位置
							if(c.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
								current_channel_index = i;
								break;
							}
							i++;
						}
						Logger.d(TAG + " , current_channel_index = "+current_channel_index);
						currentChannel = currentChannels.list.get(current_channel_index);	
					}else{//如果没有“最新”分类
	//					currentChannel = currentChannels.list.get(0);
						currentChannel = currentChannels.list.get(1);  //由于index = 0 为“订阅”,如果没有“最新”，播index = 1。
					}
				}
				if (currentChannel != null && currentChannel.getSchedules() != null
						&& currentChannel.getSchedules().size() > 0) {
					int size = currentChannel.getSchedules().size();
					Logger.d("Schedules size = " + size);
					int defaultScheduleIndex = (int) DataConfig.getDataConfigValueL(mOwner, DataConfig.DEFAULT_SCHEDULE_INDEX_JX);
					Logger.d("get DEFAULT_SCHEDULE_INDEX_JX = " + defaultScheduleIndex);
					
					Schedule canpalySchedule = null;
					int index = 0;
					if (!mIsIOTVAvailable || defaultScheduleIndex < 0 || defaultScheduleIndex >= size){
						if(!mIsIOTVAvailable){//没有最新
							//找出Schedules中能够播放的
							for(int i = 0; i < size; i++){
								Logger.d(TAG, "i = "+ i);
								index = i;
								Schedule schedule = currentChannel.getSchedules().get(i);
								if (schedule != null && schedule.getSubScheduleList()!=null){//该节目有子节目
									List<Schedule>scheduleList = schedule.getSubScheduleList();
									for(int j = 0; j < schedule.getSubScheduleList().size(); j++){
										if(utils.isPlayable(scheduleList.get(j).getEndTime())){
											canpalySchedule = schedule;
											i = size;
											break;
										}
									}
								}
							}
//							if(baseViews[CHANNEL_LIST_VIEW] != null){
//								baseViews[CHANNEL_LIST_VIEW].setFirstSelectSubProgramPos(0);
//							}
							iotvPlay(currentChannel, canpalySchedule);
						}else{
							iotvPlay(currentChannel, currentChannel.getSchedules().get(0));
						}
						
//						if(baseViews[CHANNEL_LIST_VIEW] != null){
//							baseViews[CHANNEL_LIST_VIEW].setFirstSelectSubProgramPos(index);
//						}
//						
//						iotvPlay(currentChannel, canpalySchedule);
						//iotvPlay(currentChannel, currentChannel.getSchedules().get(0));
					}else{
						iotvPlay(currentChannel, currentChannel.getSchedules().get(defaultScheduleIndex));
					}
				} else {
					Logger.d("currentChannel has no schedule, so play channel directly.");
					int defaultScheduleIndex = (int) DataConfig.getDataConfigValueL(mOwner, DataConfig.DEFAULT_SCHEDULE_INDEX_JX);
					if(currentChannels.list != null){
						if (!mIsIOTVAvailable || defaultScheduleIndex < 0 
								|| defaultScheduleIndex >= currentChannels.list.size()){
							defaultScheduleIndex = 0;
						}
					}
					if(currentChannels.list != null && currentChannels.list.size() > defaultScheduleIndex){
						currentChannel = currentChannels.list.get(defaultScheduleIndex);
						if(baseViews[CHANNEL_LIST_VIEW] != null){
							baseViews[CHANNEL_LIST_VIEW].setCurProgrammeViaIndex(defaultScheduleIndex);
						}
					}
					iotvPlay(currentChannel, null);
				}
				
				if(!mIsIOTVAvailable){
					showView(CHANNEL_LIST_VIEW, true);
					if (mHandler.hasMessages(MSG_AUTO_HIDEVIEW)) {
						mHandler.removeMessages(MSG_AUTO_HIDEVIEW);
					}

					mHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDEVIEW,
							EXPIRE_HIDE_TIME);
				}
			}
			    break;
			case MSG_START_PLAYURL:
			{
				donePlayAuthenFinish(msg.obj);
			}
				break;
			case MSG_CHANNEL_CLEAR:
				showChannelText("");
				break;
			case MSG_RETRY_PLAY_AFTER_ERROR:
			{
				if (currentChannel != null && currentChannel.getType() == Constants.CHANNEL_LIANLIANKAN){
					startPlay();
				}else{
				    iotvPlay(currentChannel, currentSchedule);
				}
			    break;
			}
			case MSG_AUTO_HIDELISTVIEW:
			case MSG_AUTO_HIDEVIEW:
			{
				if(mIOTVPlayer!=null && mIOTVPlayer.isPlaying() && expireHideViews != null){
					for (int view : expireHideViews){
						if (baseViews[view].isshow()){
							baseViews[view].hide();
						}
					}
				}
			    break;
			}
			case JX_MSG_STOP_LIVE:
				if(msg.arg1 == DataConfig.PLAY_AS_IOTV_INTERRUPTED){
					if(mIOTVPlayer!=null){
						mIOTVPlayer.pause();
					}
					updateIotvSchedules();
					currentSchedule = updateIotvScheduleIfNeed(currentSchedule);
					showView(JINGXUAN_FAKE_LOADING_VIEW, true);
					mHandler.removeMessages(JX_MSG_START_LIVE);
					mHandler.sendEmptyMessageDelayed(JX_MSG_START_LIVE, 3000);
				}else{
					/*add by xubin 2015.11.04 begin*/
					int index = 0;//currentSchedule.getScheduleIndex();
					if(currentSchedule != null){
						index = currentSchedule.getScheduleIndex();
					}
					List<Schedule> ls = null;//currentChannel.getSchedules();
					if(currentChannel != null){
						ls = currentChannel.getSchedules();
					}
					Logger.d(TAG, "JX_MSG_STOP_LIVE ls is:" + ls);
					
					int size = 0;//ls.size();
					if(ls !=null){
						size = ls.size();
					}
					index++;
					
					if (index >= size){
						index = 0;
					}
					
					Schedule nextplaySchedule = null;// = ls.get(index);
					if(ls != null){
						nextplaySchedule = ls.get(index);
					}
					iotvPlay(currentChannel, nextplaySchedule);
					/*add by xubin 2015.11.04 end*/
				}
				break;
			case JX_MSG_START_LIVE:
				hideView(JINGXUAN_FAKE_LOADING_VIEW);
				hideNextScheduleToast();//隐藏“下一节目信息”Toast显示。
				if(mIOTVPlayer!=null){
					mIOTVPlayer.unpause();
				}
				startScheduleCountdown(currentSchedule.getEndTime(),
						DataConfig.PLAY_AS_IOTV_INTERRUPTED);
				break;
			case JX_MSG_SHOW_EXIT_TOAST:
				cancelToast();  
                break;
			case JX_MSG_SHOW_NEXT_TOAST:  //显示下一节目信息
				showNextScheduleToast(mNextSchedule);
				break;
			case JX_MSG_HIDE_NEXT_TOAST:
				hideNextScheduleToast();
				break;
			case MSG_REFRESH_DATA_FINISH:
				String today = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
				currentChannels = new ChannelDay();
				currentChannels.day = today;
				currentChannels.list = mDataMgr.getChannelsSync(currentChannels.day, false);
				break;
			default:			
				super.handleMessage(msg);
				break;
			}
		}
    };
    
    private void donePlayAuthenFinish(Object data){
		if (data instanceof IOTVNetException){
			showErrorPrompt((IOTVNetException) data);
			rollbackChannelState();
			return;
		}
		
		if (currentChannel.getType() == Constants.CHANNEL_LIANLIANKAN){
			if (data == null){
				return;
			}
			
			mCurrentPlayUrl = (String) data;
			//Logger.d("play url = " + mCurrentPlayUrl);
			startPlay();
			return;
		}
		String cdntoken = (String) data;
		
		Logger.d("play cdn token = " + cdntoken);
		startPlay(cdntoken);
    }
    
	public void onCreate(Bundle savedInstanceState) {
		//controllerStartTime = System.currentTimeMillis();
		TimeUseDebugUtils.setStartPoint();
		TimeUseDebugUtils.timeUsageDebug(TAG+",onCreate");
		stopCacheService();
		initAppRunEnv(mOwner);
		initViews();
		Logger.d("oncreate = " + mOwner.getIntent().getStringExtra("channel"));
		if(needShowWelcome()){
			Logger.d("need show weclome activity");
			Intent intent = new Intent(mOwner, WelcomeActivity.class);
			mOwner.startActivity(intent);
		}
	}
	
	private void stopCacheService(){
		Logger.d("enter stopCacheService");
		Intent stopServiceIntnet = new Intent(mOwner, JingxuanCacheService.class);
		mOwner.stopService(stopServiceIntnet);
	}
	
	private boolean needShowWelcome(){
		boolean flag = false;
		int oldVer = DataConfig.getDataConfigValueI(mOwner, DataConfig.VERSION_CODE_VALUE);
		Logger.d("version in sharepreferences = " + oldVer);
		PackageManager pm = mOwner.getPackageManager();
		try {
			String curVerName = pm.getPackageInfo(mOwner.getPackageName(), 0).versionName;
			int curVer = pm.getPackageInfo(mOwner.getPackageName(), 0).versionCode;
			Logger.d("current versionCode= " + curVer + "current versionName= " + curVerName);
			if((curVerName.contains("Jingxuan") || curVerName.contains("jingxuan")) && oldVer != curVer){
				flag = true;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	private void initViews(){
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter initViews");
		surfaceView = (SurfaceView) mainLayer.findViewById(R.id.mediaview);

		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
		
		IViewFactoryBase ivf = IOTVBuilder.createViews();
		baseViews = new IViewBase[IViewFactoryBase.VIEW_COUNT];
		
		initItemView(ivf, LOADING_VIEW);
		initItemView(ivf, BACK_VIEW);
		initItemView(ivf, CHANNEL_LIST_VIEW);
		initItemView(ivf, MEDIACONTROL_VIEW);
		initItemView(ivf, JINGXUAN_FAKE_LOADING_VIEW);
		initCoExistViews();
		
		showView(LOADING_VIEW, true);
		
		ViewGroup vp = (ViewGroup) mOwner.getLayoutInflater().inflate(R.layout.jx_iotv_channel_number, null);
		mainLayer.addView(vp);
		
		mChannleNumView = (TextView) vp.findViewById(R.id.channel_num_main);
		mChannleNumView.setText("");
		
//		mRestPromptView = (RestPromptView) mOwner.getLayoutInflater().inflate(R.layout.jx_iotv_restpromptview, null);
//		mainLayer.addView(mRestPromptView);
//		mRestPromptView.setVisibility(View.INVISIBLE);
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit initViews");
	}
	
	private void initCoExistViews(){
		mCoexistViewsMap.clear();
		int[] existviews = new int[3];
		existviews[0] = BACK_VIEW;
		existviews[1] = MEDIACONTROL_VIEW;
		existviews[2] = CHANNEL_LIST_VIEW;
		mCoexistViewsMap.put(LOADING_VIEW, existviews); //set loading view's exist views
		
		existviews = new int[1];
		existviews[0] = LOADING_VIEW;
		mCoexistViewsMap.put(CHANNEL_LIST_VIEW, existviews); //set loading view's exist views
		
		existviews = new int[1];
		existviews[0] = LOADING_VIEW;
		mCoexistViewsMap.put(BACK_VIEW, existviews); //set loading view's exist views
		
		existviews = new int[1];
		existviews[0] = LOADING_VIEW;
		mCoexistViewsMap.put(MEDIACONTROL_VIEW, existviews); //set loading view's exist views
	}
	
	private void initItemView(IViewFactoryBase factorybase, int type){
		IViewBase baseview = factorybase.createView(type);
		if (baseview == null){
			return;
		}
		baseview.setDataController(this);
		mainLayer.addView(baseview.getView());
		baseview.hide();
		
		baseViews[type] = baseview;
	}
	
	private void showView(final int type, final boolean show){
//		if (mRestPromptView != null && mRestPromptView.isShown()){
//			return;
//		}
		
		final IViewBase itemview = baseViews[type];
		
		if (itemview != null){
			if (itemview.isshow() != show){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (show){
							for (int i = 0; i < IViewFactoryBase.VIEW_COUNT; i++){
								//hide no coexist view
								if (!isCoExistView(type, i)){
									hideView(i);
								}
							}
							Logger.d("show type = " + type);
							itemview.show();
						}else{
							itemview.hide();
						}
						mainLayer.requestLayout();
					}
				});
			}
		}
	}
	
	private void resetView(int type) {
		final IViewBase itemview = baseViews[type];
		if (itemview != null){
			itemview.reset();
		}
	}
	
	private void hideView(int type){
		if (mHandler.hasMessages(MSG_AUTO_HIDELISTVIEW)){
			mHandler.removeMessages(MSG_AUTO_HIDELISTVIEW);
		}
		
		IViewBase itemview = baseViews[type];
		
		if (itemview != null && itemview.isshow()){
			itemview.hide();
		}
	}
	
	private boolean isCoExistView(int base, int target){
		if (mCoexistViewsMap.containsKey(base)){
			int[] views = mCoexistViewsMap.get(base);
			
			for (int i : views){
				if (i == target){
					return true;
				}
			}
		}
	
		return false;
	}
	
	private int getShowViewType(){
		int type = IOTV_MAIN_VIEW;
		
		for (int i = 0; i < IViewFactoryBase.VIEW_COUNT; i++){
			IViewBase itemview = baseViews[i];
			
			if (itemview == null){
				continue;
			}
			
			if (itemview.isshow()){
				type = i;
				break;
			}
		}
		
		return type;
	}
	
	private boolean isViewShow(int type){
		IViewBase itemview = baseViews[type];
		
		if (itemview != null){
			return itemview.isshow();
		}
		
		return false;
	}
	
	public void initAppRunEnv(Context context){
		Logger.d("Apk info: " + utils.getVersionCode(context.getApplicationContext())
				+ ", " + utils.getVersionName(context.getApplicationContext()));

		DataConfig.initPackageName(context);
		DataConfig.resetDataConfig(context);
		IOTV_Date.instance().setContext(context.getApplicationContext());
		
		ErrorInfoMgr.getInstance().setContext(mOwner.getApplicationContext());
		ErrorInfoMgr.getInstance().setController(this);
		
		mIsFirstRun = true;
		isSurfaceViewCreated = false;
		mForceRefresh = true;
		mIOTV_Type = Constants.IOTV_PLAY_LIVE_SOURCE;
		
		registerBroadcast();

		OttContextMgr.getInstance().setContext(context.getApplicationContext());
		
		mDataMgr = IOTVBuilder.createDataModel(IOTVBuilder.BUILD_TYPE_JX);
		mDataMgr.setContext(context.getApplicationContext());
		mDataMgr.addStateChangeListener(mModelStateChange);
		
		mShotcutKeyMgr = new ShortcutKeyMgr();
		mShotcutKeyMgr.init(context.getApplicationContext(), this);
	}


	@Override
	public Channel getCurrentChannel() {
		// TODO Auto-generated method stub
		return currentChannel;
	}

	private void iotvPlay(Channel channel) {
		// TODO Auto-generated method stub
		iotvPlay(channel, null);
	}
	
	private String operAuth(String playUrl) {
//		return OttContextMgr.getInstance().operAuth(playUrl, currentChannel.getChannelCode());
		/*modify by xubin 2015.11.10 begin*/
		if(currentChannel == null){
			return null;
		}
		if(currentChannel.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
			if(currentSchedule != null){
				return OttContextMgr.getInstance().operAuth(playUrl, currentSchedule.getChannelId());
			}else{
				return null;
			}
		} else if(currentChannel.getChannelId() != null){
			return OttContextMgr.getInstance().operAuth(playUrl, currentChannel.getChannelId());
		} else {
			return OttContextMgr.getInstance().operAuth(playUrl, currentChannel.getChannelCode());
		}
		/*modify by xubin 2015.11.10 end*/
	}
	
	private void startPlay(final String cdntoken){
		Logger.d("start play = " + mCurrentPlayUrl);
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter startPlay");
		if (utils.isNull(mCurrentPlayUrl)){
			return;
		}
		mCurrentPlayUrl = mCurrentPlayUrl.trim();
		
		Uri.Builder builder = Uri.parse(mCurrentPlayUrl).buildUpon();
		builder.appendQueryParameter("STBID", OttContextMgr.getInstance().getSTBID());
		
		//append query parameters for look back
		if (mIOTV_Type == Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE ||
				mIOTV_Type == Constants.IOTV_PLAY_JINGXUAN){
			//builder.appendQueryParameter("code", currentChannel.getChannelId());
			builder.appendQueryParameter("source", "BesTV");
			builder.appendQueryParameter("client", 1 + "");
			builder.appendQueryParameter("resolution", 1 + "");
			/*modify by xubin 2015.11.17 begin*/
			String startTime = null;
			String endTime = null;
			if(currentSchedule != null){
				startTime = currentSchedule.getStartTime();
				endTime = currentSchedule.getEndTime();
			}
			mStartTime = utils.Time2long(currentSubSchedule == null ? startTime:currentSubSchedule.getStartTime()) / 1000;
			mEndTime = utils.Time2long(currentSubSchedule == null ? endTime:currentSubSchedule.getEndTime()) / 1000;
			String endT = String.valueOf(mEndTime);
			if(endT.startsWith("2055")){
				showToast(endT);
				return ;
			}
			/*modify by xubin 2015.11.17 end*/
			builder.appendQueryParameter("starttime", mStartTime + "");
			builder.appendQueryParameter("endtime", mEndTime + "");
		}
		
		final String playurl = builder.build().toString();
		Log.d(TAG,"mIOTVPlayer = " + mIOTVPlayer );
		Log.d(TAG,"playurl = " + playurl );
		//add by zjt 2016/01/21 for 空指针
		if(mIOTVPlayer != null){
            Log.d(TAG,"mIOTVPlayer.getUrl = " + mIOTVPlayer.getUrl());
		}
		
		Log.d(TAG,"mIOTV_Type = " + mIOTV_Type);
		if(!(mIOTVPlayer == null) && null != mIOTVPlayer.getUrl() &&(-1 != mIOTVPlayer.getUrl().indexOf(playurl)) && 
				mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE && mIOTVPlayer.isPlaying()) {
			Log.d(TAG,"The same URL ,return ");
			return;
		}
		Log.d(TAG,"The same URL ,after ");
		
		mIsCompleteViewShowed = false;
		
		showView(LOADING_VIEW, true);
		resetPlayingTimeStamp();
		
		
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				
				try{
					if (mIOTVPlayer == null){
						return;
					}
					
					mIOTVPlayer.stop();
//					mIOTVPlayer.close();

					if (cdntoken != null && !cdntoken.equals("")) {
						Map<String, String> headers = new HashMap<String, String>();
						headers.put("CDNToken", cdntoken);
						Logger.d("set CDNTOken header = " + cdntoken);
						mIOTVPlayer.setHeaders(headers);
					}
					
					String authUrl = deprecateUrl(operAuth(playurl));
					
					/**add by zjt 2016/01/11*/
					 
					if(utils.isNull(authUrl)){
						return ;
					}
					/**add end by zjt 2016/01/11*/
					VideoPlayLogItemInfo iteminfo = new VideoPlayLogItemInfo();
					
					if(currentSchedule != null && currentChannel != null){
						iteminfo.itemCode = currentSchedule.getCode();
						iteminfo.videoClipCode = currentSchedule.getClipCode();
						iteminfo.startDuration = currentSchedule.getStartTime();
						iteminfo.channelCode = currentChannel.getChannelCode();
						iteminfo.playType = QosLogManager.TYPE_JINGXUAN;
						iteminfo.channelName = currentChannel.getChannelName();
						iteminfo.playUrl = authUrl;
					}else{
						Log.d(TAG,"currentSchedule or currentChannel is null ");
					}
					mIOTVPlayer.setPlayUrl(authUrl, mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE);
					mIOTVPlayer.play(iteminfo);
					
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}).start();
		
		//save current channel index
		/*modify by xubin for NullPointerException 2015.11.26*/
		if(currentSchedule != null && currentChannel != null && currentChannel.getChannelCode() != null 
				&& currentChannel.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
			Logger.d("set DEFAULT_SCHEDULE_INDEX_JX = " + currentSchedule.getScheduleIndex());
			DataConfig.setDataConfigValueL(mOwner, DataConfig.DEFAULT_SCHEDULE_INDEX_JX, currentSchedule.getScheduleIndex());
		}	
		if(baseViews[CHANNEL_LIST_VIEW] != null && currentSchedule != null){
			baseViews[CHANNEL_LIST_VIEW].setCurProgrammeViaIndex(currentSchedule.getScheduleIndex());
		}
		//refresh data if possible.
		baseViews[LOADING_VIEW].show();
	}
	
	private void startPlay(){
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter startPlay");
		mIsCompleteViewShowed = false;
		showView(LOADING_VIEW, true);
		resetPlayingTimeStamp();
		if(utils.isNotNull(mCurrentPlayUrl))
			mCurrentPlayUrl = mCurrentPlayUrl.trim();
		
		Uri.Builder builder = Uri.parse(mCurrentPlayUrl).buildUpon();
		builder.appendQueryParameter("STBID", OttContextMgr.getInstance().getSTBID());
	
		final String playurl = builder.build().toString();
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				if (mIOTVPlayer == null){
					return;
				}
				
				mIOTVPlayer.stop();
//				mIOTVPlayer.close();
				
				String authUrl = deprecateUrl(operAuth(playurl));
				
				VideoPlayLogItemInfo iteminfo = new VideoPlayLogItemInfo();
				if(currentChannel != null){
					iteminfo.channelCode = currentChannel.getChannelCode();
					iteminfo.playType = QosLogManager.TYPE_JINGXUAN;
					iteminfo.channelName = currentChannel.getChannelName();
					iteminfo.playUrl = authUrl;
				}
				
				if (currentSchedule != null){
					iteminfo.itemCode = currentSchedule.getCode();
					iteminfo.videoClipCode = currentSchedule.getClipCode();
				}
				
				String taskID = Uri.parse(authUrl).getQueryParameter("taskID");
				if (taskID != null){
					iteminfo.taskID = taskID;
				}
				mIOTVPlayer.setPlayUrl(authUrl, mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE);
//				mIOTVPlayer.setPlayUrl(authUrl);
				mIOTVPlayer.play(iteminfo);
			}
		}).start();
		
		//save current channel index
		if(currentChannel != null && currentChannel.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
			Logger.d("set DEFAULT_SCHEDULE_INDEX_JX = " + currentSchedule.getScheduleIndex());
			DataConfig.setDataConfigValueL(mOwner, DataConfig.DEFAULT_SCHEDULE_INDEX_JX, currentSchedule.getScheduleIndex());
		}
		if(baseViews[CHANNEL_LIST_VIEW] != null && currentSchedule != null){
			baseViews[CHANNEL_LIST_VIEW].setCurProgrammeViaIndex(currentSchedule.getScheduleIndex());
		}
		//refresh data if possible.
		baseViews[LOADING_VIEW].show();
	}
	
	void resetPlayingTimeStamp(){
		first_playing_timestamp = 0;
	}
	
	/*
	 *  zhangchao add for YDJD
	 *  根据YDJD要求，在播放的url中添加stdId/userToken/usergroup 字段
	 *  不从移动基地接口拿信息，直接从系统属性拿信息,参考Authen对移动基地的处理
	 */	
	String deprecateUrl(String url) {
		if (url != null && OttContextMgr.getInstance().getTargetOEM().equalsIgnoreCase("YDJD")){
			url += "&stbId=" + OttContextMgr.getInstance().getSTBID();
			url += "&userToken=" + utils.safeString(SystemProperties.get(EPG_USER_TOKEN));
			url += "&usergroup=" + utils.safeString(SystemProperties.get(EPG_USER_GROUP));
		}
		Logger.d("deprecateUrl = " + url);
		return url;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		Logger.d("surface view created");
		initBesTVPlayer();
		if (mIOTVPlayer != null){
		    mIOTVPlayer.setSurfaceView(surfaceView);
		}
		Logger.d("after initBesTVPlayer = " + mIOTVPlayer);
		isSurfaceViewCreated = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		isSurfaceViewCreated = false;
		
		if (mIOTVPlayer != null){
			mIOTVPlayer.stop();
			//mIOTVPlayer.close();
		}
	}
	
	private void initBesTVPlayer() {
		Logger.d("initBesTVPlayer = " + mIOTVPlayer);
		if (mIOTVPlayer != null) {
			Logger.d("mBesTVPlayer has created.");
			return;
		}

		mIOTVPlayer = IOTVMediaPlayer.getInstance();
		mIOTVPlayer.setContext(mOwner.getApplicationContext());
		
		mMediaPlayerEventListener = new IOTVMediaPlayer.IOTVMediaPlayerEventL() {
			public void onBesTVMediaPlayerEvent(BesTVMediaPlayerEvent event) {
				switch (event.getParam3()) {
				case BesTVPlayerStatus.BESTV_PLAYER_PLAYING:
					//Logger.d("IOTV_P", "----------playing------------ current time = " + mIOTVPlayer.getCurrentTime() + " currentSchedule = " + currentSchedule);
					//stamp time for the first playing call back
					if (first_playing_timestamp == 0){
						first_playing_timestamp = SystemClock.elapsedRealtime();
						if(currentChannel != null){
							Logger.d("---------iotv type = " + mIOTV_Type + " channel type = " + currentChannel.getType());
						}
						/*if (mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE && currentChannel.getType() == Constants.CHANNEL_LIANLIANKAN){
							if (currentSchedule != null){
							    long seekto = getCurrentPlayingTime() - utils.Time2long(currentSchedule.getStartTime());
							    Logger.d("IOTV_P", "iotv play seek to = " + seekto / 1000);
							    
							    long distance = Math.abs(mIOTVPlayer.getCurrentTime() - seekto / 1000);
							    if (distance > 10){
							        mIOTVPlayer.setSeekTimeEx((int) (seekto / 1000));
							    }
							}
						}*/
					}
				
					if (mIOTVPlayer.isFirstPlaying() && mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE){
						if (currentChannel != null && currentChannel.getType() == Constants.CHANNEL_LIANLIANKAN){
							if (currentSchedule != null){
							    long seekto = getCurrentPlayingTime() - utils.Time2long(currentSchedule.getStartTime());
							    Logger.d("IOTV_P", "iotv play seek to = " + seekto / 1000);
							    
							    long distance = Math.abs(mIOTVPlayer.getCurrentTime() - seekto / 1000);
							    if (distance > 10){
							        mIOTVPlayer.setSeekTimeEx((int) (seekto / 1000));
							    }
							}
						}
						
//						if (DataConfig.supportLiveTimeShift(mOwner)){
//						    Integer hideTime = new Integer(MediaControlView.HIDE_TIME_FIRST);
//						    baseViews[MEDIACONTROL_VIEW].getView().setTag(hideTime);
//						    showView(MEDIACONTROL_VIEW, true);
//						}
					}
					
					showView(LOADING_VIEW, false);
					checkAndShowPlayCompleteView();
					break;
				case BesTVPlayerStatus.BESTV_PLAYER_SEEKING:
					showView(LOADING_VIEW, true);
					break;
				case BesTVPlayerStatus.BESTV_PLAYER_BUFFERING:
					Logger.d("IOTV_P", "----------buffering------------");
					if (first_playing_timestamp > 0){
						long current_time = SystemClock.elapsedRealtime();
						
						//if less than interval time, ignore this call-back.
						if (current_time - first_playing_timestamp <= INGORE_BUFFERING_TIME){
							Logger.d("less than interval time, ignore this call-back.");
							return;
						}else{
							Logger.d("normal buffering data, rest playing time stamp");
							resetPlayingTimeStamp();
						}
					}
					showView(LOADING_VIEW, true);
					break;
				case BesTVPlayerStatus.BESTV_PLAYER_END:
					Logger.d( "----------play over------------");
					showView(LOADING_VIEW, false);
					showView(PLAY_COMPLETE_VIEW, false);
					showView(MEDIACONTROL_VIEW, false);
					
					playNextSchedule();
					break;
				case BesTVPlayerStatus.BESTV_PLAYER_ERROR:
					Logger.d("----------play error------------");
					showView(LOADING_VIEW, true);
					
					if (!mHandler.hasMessages(MSG_RETRY_PLAY_AFTER_ERROR)){
						//play error, try to play again
						mHandler.sendEmptyMessageDelayed(MSG_RETRY_PLAY_AFTER_ERROR, RETRY_PLAY_TIME);
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
//								LogReport lr = mIOTVPlayer.getLogReport();
//								if (lr != null){
//									//050000
//									//showSeekPromptToast(ErrorInfoMgr.getInstance().getPlayErrorPromptMsg(lr.errorCode));	
//								}
							}
						});
					}
					break;
				}
			}
		};
		
		mMediaPlayerEventListener.setEventName("MainPlayer");
		mIOTVPlayer.setEventListener(mMediaPlayerEventListener);
		
		//mIOTVPlayer.setMediaPlayerWin(0, 0, 1280, 720);
		
//		mRestPromptView.init(mIOTVPlayer, this);
//		mRestPromptView.setRestShowListener(new ResetViewShowListener() {
//			public void onShow() {
//				// TODO Auto-generated method stub
//				if (baseViews[LOADING_VIEW] != null) {
//					baseViews[LOADING_VIEW].hide();
//				}
//				if (baseViews[BACK_VIEW] != null) {
//					baseViews[BACK_VIEW].hide();
//				}
//				if (baseViews[PAUSE_VIEW] != null) {
//					baseViews[PAUSE_VIEW].hide();
//				}
//			}
//		});

		QosLogManager.Builder builder = new QosLogManager.Builder(mOwner);
		long download_sampling_time = 0;
		String temp = OttContextMgr.getInstance().getConfig(OttContextMgr.BESTV_CONFIG_FILE, OttContextMgr.LOG_DOWNLOAD_SAMPLING_TIME);
		if (!temp.equals("")){
			download_sampling_time = Long.parseLong(temp);
		}
		
		int max_download_details = 0;
		temp = OttContextMgr.getInstance().getConfig(OttContextMgr.BESTV_CONFIG_FILE, OttContextMgr.LOG_DOWNLOAD_MAX_ITEMS);
		if (!temp.equals("")){
			max_download_details = Integer.parseInt(temp);
		}
		
		//mPlaylog.logEnable(VideoPlayLog.LOG_TYPE_APPSTATE | VideoPlayLog.LOG_TYPE_PLAYSTART);
		builder.setVideoPlayLogCallbacks(new VideoPlayLog.VideoPlayLogCallbacks() {
			@Override
			public void playError(String errorCode) {
				// TODO Auto-generated method stub
				utils.send(mOwner, errorCode);
			}
			
			@Override
			public boolean getLogOpenState(Context context) {
				// TODO Auto-generated method stub
				Logger.d("IOTV_QOS", "get log open state");
				return utils.getQosOpenState(context);
			}
			
			@Override
			public Date getCurrentSystemTime() {
				// TODO Auto-generated method stub
				Logger.d("IOTV_QOS", "getCurrentSystemTime----");
				return IOTV_Date.getDate();
			}

			@Override
			public void onIntentReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				LogUploadReceiver.onReceive(context, intent);
			}
			
		}).setDownloadDetailLogConfig(download_sampling_time, max_download_details);
		builder.setOwnerMediaPlayer(mIOTVPlayer);

		// 设置要开启的日志类型，目前总共有四种，多个或运算后传入
		// 起播日志 LOG_TYPE_PLAYSTART = 0x0002;
		// 播放日志 LOG_TYPE_PLAY = 0x0004;
		// 下载详情 LOG_TYPE_DOWNLOAD_DETAIL = 0x0008;
		// 缓存详情 LOG_TYPE_BUFFERING_DETAIL = 0x0010;
		builder.enableLog(QosLogManager.LOG_TYPE_ALL);

		mPlaylog = builder.create();
		mPlayRecords = new IOTVPlayRecords(mOwner.getApplicationContext(), mIOTVPlayer);
	}

	public void onStop() {
		// TODO Auto-generated method stub
		Logger.d("=====onStop===========>DestroyBesTVPlayer");
		
		isResumeExcuted = false;//add by zjt 2016/2/1
		//add by zjt 2016/01/19  退出精选还会弹出提示“下一节目”的信息
		mHandler.removeMessages(JX_MSG_SHOW_NEXT_TOAST);
		destroyPlayer();
		QosLogManager.sendAppEndLog(mOwner, mOwner.getClass().getName());
	}

	public void onDestroy() {
		// TODO Auto-generated method stub
		mPlaylog.notifyAppDestroy();
//		mRestPromptView.quit();
		unregisterBraodcast();
		mDataMgr.stop();
		OttContextMgr.getInstance().reset();
		JXSubscription.getInstance().save();
	}
	
	public void onPause() {
		// TODO Auto-generated method stub
		Logger.d(TAG, "...onPause...  ");
		isResumeExcuted = false;//add by zjt 2016/2/1 保证只加载一次 onResume
		
		if (surfaceView != null){
			surfaceView.setVisibility(View.INVISIBLE);
		}
		
		//hide all visible views before pause activity
		for (IViewBase v : baseViews){
			if (v != null){
				v.hide();
			}
		}
		
		destroyPlayer();
	}
	
	public void onResume() {
		// TODO Auto-generated method stub
		TimeUseDebugUtils.timeUsageDebug(TAG+",onResume , isResumeExcuted = "+isResumeExcuted);
		if(!isResumeExcuted){
			Log.d(TAG, "resume excuting ...");
			if (surfaceView != null){
				surfaceView.setVisibility(View.VISIBLE);
			}
			//initBesTVPlayer();
			showView(LOADING_VIEW, true);
			hideView(BACK_VIEW);
			hideView(CHANNEL_LIST_VIEW);
			
			initChannelIndex();
			
			mIsLoadingFinish = false;
			//start load channel data
			TimeUseDebugUtils.timeUsageDebug(TAG+",mDataMgr.start ");
			mDataMgr.start(true);
			initBesTVPlayer();
			
			isResumeExcuted = true;
			
			Logger.d("on resume....");
		}
		
	}
	
	public void onStart() {
		// TODO Auto-generated method stub
		QosLogManager.setAppStartTime();
	}

	private void initChannelIndex(){
		mStartType = START_BY_NORMAL;
		mStartSchedule = new Schedule();
		
		Intent intent = mOwner.getIntent();
		String index = intent.getStringExtra("channel");
		
		Logger.d("initChannelIndex = " + index);
		if (index == null || index.equals("")){
			String channelCode = intent.getStringExtra("ChannelCode");
			//channelCode = "Umai:CHAN/1376@BESTV.SMG.SMG";
			//start by uri
			String param = intent.getStringExtra("param");
			Log.d("harish", "param = " + param);
			if (param != null){
				channelCode = param.replace(IOTVPlayRecords.PLAYRECORD_REPLACE_STR, ":");
			}
			
			if (channelCode != null){
				mStartType = START_BY_CHANNELCODE;
				mStartSchedule.setChannelCode(channelCode);
				
				String date = intent.getStringExtra("Date");
				String startTime = intent.getStringExtra("StartTime");
				String endTime = intent.getStringExtra("EndTime");
				
				//date = "20140901";
				//startTime = "20140901184500";
				//endTime = "20140901192800";
				
				if (date != null && startTime != null && endTime != null){
					mStartType = START_BY_CHANNELSCHEDULE;
					
					mStartDate = date;
					mStartSchedule.setStartTime(startTime);
					mStartSchedule.setEndTime(endTime);
				}
			}
		}
		
		if (index == null){
			index = "0";
		}
		
		current_channel_index = Integer.parseInt(index) - 1;
		
		//reset intent
		Logger.d("current channel = " + current_channel_index);
	}

	private void destroyPlayer() {
		if (null != mIOTVPlayer) {
			if(null != mMediaPlayerEventListener){
				mIOTVPlayer.removeEventListener(mMediaPlayerEventListener);
			}
			mIOTVPlayer.stop();
			mIOTVPlayer.close();
			mIOTVPlayer.release();
//			mIOTVPlayer.destroy();
//			mIOTVPlayer.reset();
			mIOTVPlayer = null;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//Channel c = new Channel();
		//c.setChannelCode("live_sports");
		//getJXSchedule(c, mHandler.obtainMessage(9999));
		
		//find visible view
		boolean isfind = false;
		for (int type = 0; type < baseViews.length; type++){
			IViewBase itemview = baseViews[type];
			
			if (itemview == null){
				continue;
			}
			
			if (itemview.isshow()){
				if (type != LOADING_VIEW){
					itemview.hide();
					isfind = true;
				}
			}
		}
		
		if (!isfind){
//		    //no more sub view is showing, so exit application
//			exitApp();
			showView(BACK_VIEW, !isViewShow(BACK_VIEW));
		}
	}

	@Override
	public List<Channel> getPlayRecords() {
		// TODO Auto-generated method stub
		List<String> codelist = mPlayRecords.getPlayRecords();
		List<Channel> channellist = new ArrayList<Channel>();
		
		for (String s : codelist){
			Date today = IOTV_Date.getDate();
			
			Channel c = ChannelManager.getChannelByCode(mDataMgr.getChannelsSync(IOTV_Date.dateFormat.format(today), false), s);
			
			if (utils.isNotNull(c.getChannelCode())){
				channellist.add(c);
			}
		}
		
		for (int index = channellist.size(); index < IOTVPlayRecords.MAX_RETURN_RECORDS_SIZE; index++){
			channellist.add(new Channel());
		}
		
		return channellist;
	}

	@Override
	public void gotoChannelList() {
		// TODO Auto-generated method stub
		showView(CHANNEL_LIST_VIEW, true);
	}

	@Override
	public void exitApp() {
		// TODO Auto-generated method stub
		//add by zjt 2016/01/19 退出精选还会弹出提示“下一节目”的信息
		mHandler.removeMessages(JX_MSG_SHOW_NEXT_TOAST);
		resetView(MEDIACONTROL_VIEW);
		mOwner.finish();
	}
	/*add by xubin 2016.01.19 begin*/
	public void removeShowNextMsg(){
		Log.d(TAG, "removeShowNextMsg:" + mHandler.hasMessages(JX_MSG_SHOW_NEXT_TOAST));
		if(mHandler != null && mHandler.hasMessages(JX_MSG_SHOW_NEXT_TOAST)){
			mHandler.removeMessages(JX_MSG_SHOW_NEXT_TOAST);
		}
	}
	/*add by xubin 2016.01.19 end*/
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//send message to hide specified view if necessary.
		hideNextScheduleToast(); //按任意键时，隐藏该Toast
		
		if (keyCode == ShortcutKeyMgr.MENU_KEY || keyCode == KeyEvent.KEYCODE_MENU){
			return true;
		}
		if (mShotcutKeyMgr.isShortcutKeys(keyCode)){
			return false;
		}
		
		if (!mIsLoadingFinish){
			Log.d("onKeyDown", "mIsLoadingFinish = false, so return false.");
			return false;
		}
		
//		if (mRestPromptView != null){
//			mRestPromptView.resetDuration();
//		}
//		
//		if (mRestPromptView.getVisibility() == View.VISIBLE){
//			return false;
//		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
			//show pause view just on main view.
			if (getShowViewType() == IOTV_MAIN_VIEW || getShowViewType() == LOADING_VIEW){
				
				if (!checkSuccess1()){
					showSeekPromptToast(R.string.loadingSchedule_str);
				}else{
					if (mIOTV_Type == Constants.IOTV_PLAY_JINGXUAN) {
						if (mIOTVPlayer != null) {
							mIOTVPlayer.pause();
							if (baseViews[CHANNEL_LIST_VIEW] != null) {
								baseViews[CHANNEL_LIST_VIEW]
										.setPauseIconVisible(true);
							}
						}
					} else {
						if (baseViews[CHANNEL_LIST_VIEW] != null) {
							baseViews[CHANNEL_LIST_VIEW]
									.setPauseIconVisible(false);
						}
					}
					showView(CHANNEL_LIST_VIEW, true);
				}
			}
			
//			if (getShowViewType() == MEDIACONTROL_VIEW){
//				if (mIOTV_Type == Constants.IOTV_PLAY_JINGXUAN){
//					showView(MEDIACONTROL_VIEW, false);
//				}
//			}
			
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (getShowViewType() == IOTV_MAIN_VIEW) {
				if (mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE /*&& !isCurrentIotvChannelSupportTS()*/) {//delete by xubin 2015.11.23
					showSeekPromptToast();
					return true;
				} else {
					showView(MEDIACONTROL_VIEW, true);
				}
			}
		}
		
		if (dispatch2ItemView(event)){
			return true;
		}
		
//		if (keyCode == KeyEvent.KEYCODE_BACK
//				&& (getShowViewType() == IOTV_MAIN_VIEW || isViewShow(LOADING_VIEW))) {
//			if ((System.currentTimeMillis() - exitTime) > 3000) {
//				showExitToast();
//				exitTime = System.currentTimeMillis();
//				return true;
//			}
//		}
		
		return false;
	}

	private boolean isCurrentIotvChannelSupportTS() {
		boolean supportTS = false;
		String today = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
		/*modify by xubin 2015.11.10 begin*/
		List<Channel> channelList = null;//mDataMgr.getIotvChannelListByDay(today);
		if(mDataMgr !=null){
			channelList = mDataMgr.getIotvChannelListByDay(today);
		}
		/*modify by xubin 2015.11.10 end*/
		if(channelList != null && currentSchedule != null && channelList.size() > currentSchedule.getScheduleIndex()){
			supportTS = channelList.get(currentSchedule.getScheduleIndex()).getTSSupport() == Constants.IS_LIVE_SHIFT_SUPPORT;
		}
		return supportTS;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		try{
			if (keyCode == ShortcutKeyMgr.MENU_KEY || keyCode == KeyEvent.KEYCODE_MENU){
				return true;
	//			if (!checkSuccess1()){
	//				showSeekPromptToast(R.string.loadingSchedule_str);
	//			}else{
	//				showView(CHANNEL_LIST_VIEW, !isViewShow(CHANNEL_LIST_VIEW));
	//				getIOTVMediaPlayer().unpause();
	//				baseViews[CHANNEL_LIST_VIEW].setPauseIconVisible(false);
	//				return true;
	//			}
			}
			
			if (!mIsLoadingFinish){
				Log.d("onKeyUp", "mIsLoadingFinish = false, so return false.");
				return false;
			}
			
//			if (mRestPromptView.getVisibility() == View.VISIBLE){
//				return false;
//			}
			
			if (dispatch2ItemView(event)){
				return true;
			}
			
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
				Logger.d(TAG, "KEYCODE_DPAD_DOWN");
				if (!checkSuccess1()){
					showSeekPromptToast(R.string.loadingSchedule_str);
				}else{
					resetView(MEDIACONTROL_VIEW);
					playNextSchedule();
					return true;
				}
			}
			
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
				if (!checkSuccess1()){
					showSeekPromptToast(R.string.loadingSchedule_str);
				}else{
					resetView(MEDIACONTROL_VIEW);
					playPrevSchedule();
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	boolean dispatch2ItemView(KeyEvent event){
		for (int type = baseViews.length - 1; type >= 0 ; type--){
			IViewBase itemview = baseViews[type];
			
			if (itemview == null){
				continue;
			}
			
			if (itemview.isshow()){
				if (event.getAction() == KeyEvent.ACTION_DOWN){
					return itemview.onKeyDown(event.getKeyCode(), event);
				}else{
					return itemview.onKeyUp(event.getKeyCode(), event);
				}
			}
		}
		
		return false;
	}
	
	
	private void showChannelText(String text){
		mChannleNumView.setText(text);
	}
	
	@Override
	public List<Channel> getChannelList(String day) {
		// TODO Auto-generated method stub
		return mDataMgr.getChannelsSync(day, true);
	}

	@Override
	public void setCurrentChannel(String day, List<Channel> list) {
		if(currentChannels == null){
			currentChannels = new ChannelDay();
		}
		if (currentChannels.day != null && currentChannels.day.equals(day)){
			return;
		}
		
		if (list == null){
			List<Channel> chlist = mDataMgr.getChannelsSync(day, false);
			if (chlist != null && chlist.size() > 0 && isChannelListReady(chlist)){
				currentChannels.day = day;
				currentChannels.list = chlist;
			}
		}
	}

	@Override
	public int getCurrentIOTVType() {
		// TODO Auto-generated method stub
		return mIOTV_Type;
	}
	/*add by xubin 2015.11.11 begin*/
	private void playNextScheduleEx() {
		Log.d("playNextScheduleEx", "currentSubSchedule:" + currentSubSchedule + "--currentChannel:" + currentChannel);
		if (currentSubSchedule == null) {// 正在播放单条节目，非剧集
			List<Schedule> lists = null;
			if(currentChannel != null){
				if(currentChannel.getChannelCode().equals(JXSubscription.CHANNECODE)){
					lists = utils.filterSchedulesWithPlayableSubScheduleList(currentChannel.getSchedules());
				}else{
					lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null);
				}
			}
			int index = 0;//currentSchedule.getScheduleIndex();
			Log.d("playNextScheduleEx", "currentSchedule:" + currentSchedule + "--lists:" + lists);
			if(currentSchedule != null){
				index = currentSchedule.getScheduleIndex();
			}
			int size = 0;//lists.size();
			if(lists != null){
				size = lists.size();
				if(index < lists.size() - 1 && !lists.get(index).equals(currentSchedule)){
					index = utils.findSchedule(lists, currentSchedule);
				}
			}
			index++;
			Log.d("playNextScheduleEx", "index:" + index + "--size:" + size);
			if (index >= size) {
				index = 0;
			}
			Schedule tmp = null;//lists.get(index);
			if(lists != null){
				tmp = lists.get(index);
			}
			Log.d("playNextScheduleEx", "tmp:" + tmp);
			if(tmp == null){
				showSeekPromptToast(mOwner.getString(R.string.channel_not_begin));
			}else{
				iotvPlay(currentChannel, tmp);
			}
		} else {// 正在播放剧集
			//获得当前节目单需要的信息和焦点位置
			List<Schedule> lists = null;
			if(currentChannel != null){
				if(currentChannel.getChannelCode().equals(JXSubscription.CHANNECODE)){
					updateScheduleList(currentChannel);
					lists = utils.filterSchedulesWithPlayableSubScheduleList(currentChannel.getSchedules());
					if(!isSubscribedProgram(currentSchedule)){//如果该节目已取消订阅					
						for (Channel channel : getJXCategory()) { //currentChannel置为原分类
							if (channel.getChannelCode().equalsIgnoreCase(currentSchedule.getOriginalChannelCode())) {
								currentChannel = channel;
								break;
							}
						}
						lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null); //lists置为原分类的节目单
						for(Schedule sc: lists){//currentSchedule置为原分类中的节目
							if(sc.equalsForSubscribe(currentSchedule)){
								currentSchedule = sc;
								break;
							}
						}
					}
				}else{
					lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null);
				}
			}
			int scheduleIndex = 0;//currentSchedule.getScheduleIndex();
			if(currentSchedule != null){
				scheduleIndex = currentSchedule.getScheduleIndex();
			}
			int size = 0;//lists.size();
			if(lists != null){
				size = lists.size();
			}
			List<Schedule> subList = null;//currentSchedule.getSubScheduleList();
			if(currentSchedule != null){
				subList = currentSchedule.getSubScheduleList();
			}
			int subIndex = 0;//currentSubSchedule.getScheduleIndex();
			if(currentSubSchedule != null){
				subIndex = currentSubSchedule.getScheduleIndex();
			}

			if ((subIndex == 0) 														// 如果正在播放子集的第一集
					|| (subList != null && !utils.isPlayable(subList.get(subIndex - 1).getEndTime()))) { 	// 或者下一集子集不能播
				if(lists != null && scheduleIndex < lists.size() - 1 
						&& !lists.get(scheduleIndex).equals(currentSchedule)){
					scheduleIndex = utils.findSchedule(lists, currentSchedule);
				}
				scheduleIndex++; 														// 直接切换到下一个节目
				subIndex = 0;															// 的第一个子集
				if (scheduleIndex >= size) {
					scheduleIndex = 0;
				}
				if(lists != null){
					iotvPlay(currentChannel, lists.get(scheduleIndex));
				}else{
					iotvPlay(currentChannel, null);
				}
			} else {																	//此时正在特定节目的分类子集中
				subIndex--;																//则在子集中切换下一子集
				iotvPlay(currentChannel, currentSchedule, subIndex);
			}
		}
	}
	/*add by xubin 2015.11.11 end*/

	@Override
	public void iotvPlay(Channel channel, Schedule schedule) {
		Logger.d(TAG,"iotvPlay(Channel , Schedule )");
		currentSubSchedule = null;
		// TODO Auto-generated method stub
		if (channel == null){
			return;
		}
		
		setNextScheduleToast(schedule); //设置“下一节目信息”Toast显示

		if (schedule != null && schedule.getSubScheduleList()!=null) {//该节目有子集
			int firstPlayIndex = utils.getIndexOfTheFirstPartInTheFirstPlayableSchedule(schedule.getSubScheduleList());
			Logger.d(TAG,"iotvPlay , firstPlayIndex = "+firstPlayIndex
					+" , SubScheduleList size = "+schedule.getSubScheduleList().size());
			if(firstPlayIndex >= 0){
				iotvPlay(channel, schedule, firstPlayIndex);
			}else{
				/*modify by xubin 2015.11.09 begin*/
				//showSeekPromptToast(mOwner.getString(R.string.channel_not_begin));
				playNextScheduleEx();
				/*modify by xubin 2015.11.09 end*/
				Log.d(TAG,"no Playable schedule");
			}
			return;
		}
		Logger.d("iotvplay " + channel.getChannelName() + " number = " + channel.getChannelNo() + " channel type = " + channel.getType());
		//Logger.d("IOTVD",Log.getStackTraceString(new Throwable()));  
		if (mHandler.hasMessages(MSG_RETRY_PLAY_AFTER_ERROR)){
			mHandler.removeMessages(MSG_RETRY_PLAY_AFTER_ERROR);
		}
		
		mIOTV_Type = channel.getType();
		
		mHandler.removeMessages(JX_MSG_STOP_LIVE);
		mHandler.removeMessages(JX_MSG_START_LIVE);
		long type = DataConfig.getDataConfigValueL(mOwner, DataConfig.LIVE_PLAY_ENDACT);
		Logger.d("LivePlayEndAct is : " + type);
		
		if (schedule != null) {
			try {
				if (channel.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)) {
					schedule = updateIotvScheduleIfNeed(schedule);
				}
			} catch (Exception e) {

			}
			
			if (type == DataConfig.PLAY_AS_IOTV) {
				if (mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE) {
					mCurrentPlayUrl = schedule.getLiveUrl();
				} else {
					mCurrentPlayUrl = schedule.getScheduleLookbackUrl();
				}
			} else if (type == DataConfig.PLAY_AS_IOTV_INTERRUPTED) {
				if (mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE) {
					mCurrentPlayUrl = schedule.getLiveUrl();

					startScheduleCountdown(schedule.getEndTime(),
							DataConfig.PLAY_AS_IOTV_INTERRUPTED);
				} else {
					mCurrentPlayUrl = schedule.getScheduleLookbackUrl();
				}
			} else if (type == DataConfig.PLAY_AS_JX) {
				if (mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE) {
					mCurrentPlayUrl = schedule.getLiveUrl();

					startScheduleCountdown(schedule.getEndTime(),
							DataConfig.PLAY_AS_JX);
				} else {
					mCurrentPlayUrl = schedule.getScheduleLookbackUrl();
				}
			} else {
				if (mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE) {
					mCurrentPlayUrl = schedule.getLiveUrl();
				} else {
					mCurrentPlayUrl = schedule.getScheduleLookbackUrl();
				}
			}
		}else{
			mCurrentPlayUrl = channel.getLivePlayUrl();
		}
		
		//Log.d("harish1", "live url = " + schedule.getLiveUrl() + " lookback url = " + schedule.getScheduleLookbackUrl());
		
		//update channel info
		Integer id = new Integer(channel.getChannelNo());
		current_channel_index = id - 1;
		currentChannel = channel;
		currentSchedule = schedule;
		

		if(baseViews[CHANNEL_LIST_VIEW] != null){
			if(schedule != null){
				baseViews[CHANNEL_LIST_VIEW].setCurProgrammeViaIndex(schedule.getScheduleIndex());
			}
			baseViews[CHANNEL_LIST_VIEW].setCurSubProgrammeViaIndex(0);
		}
		
		//then , try to update the schedules of this channel if needed.
		//if (currentChannel.getSchedules() == null || currentChannel.getSchedules().size() == 0){
			currentChannel.setSchedules(mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null));
		//}
		//设置下一条节目，用于VideoLoadingView显示
		mNextSchedule = null;
		if(currentChannel != null && currentChannel.getChannelCode().equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)){
			if(currentSchedule != null){
				mNextSchedule = mDataMgr.getNextScheduleOfChannel(currentSchedule.getScheduleIndex());
			}
		}else if(currentSchedule != null && currentChannel != null && currentChannel.getSchedules() != null && currentChannel.getSchedules().size() > 0){
			int index = currentSchedule.getScheduleIndex();
			index ++;
			if (index >= currentChannel.getSchedules().size()){
				index = 0;
			}
			mNextSchedule = currentChannel.getSchedules().get(index);
		}
		Logger.d("iotv play current channel index = " + current_channel_index);

		
		mDataMgr.getCDNToken();
		mIsCompleteViewShowed = false;
	}
	
	@Override
	public void iotvPlay(Channel channel, Schedule schedule, int subScheduleIndex) {
		Logger.d(TAG,"iotvPlay(Channel , Schedule ,int ) , subScheduleIndex = "+subScheduleIndex);
		if (schedule == null || schedule.getSubScheduleList() == null || subScheduleIndex >= schedule.getSubScheduleList().size()){
			return;
		}
		currentSubSchedule = schedule.getSubScheduleList().get(subScheduleIndex);
		if(currentSubSchedule == null){
			return;
		}
		Logger.d(TAG, "currentSubSchedule name = "+currentSubSchedule.getName()+" , schedule.name = "+schedule.getName());
		if (channel == null){
			return;
		}
		if(isSubscribedProgram(schedule)){
			Schedule subscribeSchedule = null;
			for(Schedule sch:JXSubscription.getInstance().getSubscriptionSchs()){
				if(sch.getName().equals(schedule.getName())){
					subscribeSchedule = sch;
				}
			}
			if(null != subscribeSchedule && null != subscribeSchedule.getSubScheduleList() && subscribeSchedule.getSubScheduleList().size()>0){
				//子剧集设置为已观看
				for(Schedule sch:subscribeSchedule.getSubScheduleList()){
					if(sch.getName().equals(currentSubSchedule.getName())&&sch.getNamePostfix().equals(currentSubSchedule.getNamePostfix())){
						sch.setHasWatched(true);
						sch.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_NORMAL);
						break;
					}
				}
			}
		}
		
		setNextScheduleToast(currentSubSchedule);// 设置下一条节目信息Toast显示
		
		Logger.d("iotvplay " + channel.getChannelName() + " number = " + channel.getChannelNo() 
				+ " channel type = " + channel.getType());
		//Logger.d("IOTVD",Log.getStackTraceString(new Throwable()));  
		if (mHandler.hasMessages(MSG_RETRY_PLAY_AFTER_ERROR)){
			mHandler.removeMessages(MSG_RETRY_PLAY_AFTER_ERROR);
		}
		
		mIOTV_Type = channel.getType();
		
		mHandler.removeMessages(JX_MSG_STOP_LIVE);
		mHandler.removeMessages(JX_MSG_START_LIVE);
		
		mCurrentPlayUrl = currentSubSchedule.getScheduleLookbackUrl();
		
		//Log.d("harish1", "live url = " + schedule.getLiveUrl() + " lookback url = " + schedule.getScheduleLookbackUrl());
		
		//update channel info，根据当前播放节目的channel信息，确定current_channel_index.
		Integer id = new Integer(channel.getChannelNo());
		current_channel_index = id - 1;
		currentChannel = channel;
		currentSchedule = schedule;
		
		if(baseViews[CHANNEL_LIST_VIEW] != null){
			baseViews[CHANNEL_LIST_VIEW].setCurProgrammeViaIndex(schedule.getScheduleIndex());
			baseViews[CHANNEL_LIST_VIEW].setCurSubProgrammeViaIndex(subScheduleIndex);
		}
		//then , try to update the schedules of this channel if needed.
		//if (currentChannel.getSchedules() == null || currentChannel.getSchedules().size() == 0){
			if(currentChannel.getChannelCode() == Constants.JX_SUBSCRP_CHANNEL_CODE)  //如果是订阅频道，则从订阅频道中拿Schedules列表信息,added by ding.jian ,2015/6/10
			{
				JXSubscription jxSubscription = JXSubscription.getInstance();
				currentChannel.setSchedules(jxSubscription.getSubscriptionSchs());
			}else
				currentChannel.setSchedules(mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null));
		//}
		
		//设置下一条节目，用于VideoLoadingView显示
		mNextSchedule = null;
		if(currentChannel.getChannelCode().equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)){
			mNextSchedule = mDataMgr.getNextScheduleOfChannel(currentSchedule.getScheduleIndex());
		}else if(currentSchedule != null && currentChannel != null 
				&& currentChannel.getSchedules() != null && currentChannel.getSchedules().size() > 0){	
			Logger.d(TAG, "zjt . currentSubSchedule ScheduleIndex = "+currentSubSchedule.getScheduleIndex());
			if(currentSubSchedule.getScheduleIndex() > 0){
				mNextSchedule = currentSchedule.getSubScheduleList().get(currentSubSchedule.getScheduleIndex() - 1);
				
				Logger.d(TAG, "mNextSchedule.name = "+mNextSchedule.getName());
				
				if(!utils.isPlayable(mNextSchedule.getEndTime())){//如果结束时间还未到
					int index = currentSchedule.getScheduleIndex();
					Logger.d(TAG, "zjt . index = "+index+" , currentChannel.getSchedules().size = "+ currentChannel.getSchedules().size());
					index ++;
					if (index >= currentChannel.getSchedules().size()){
						index = 0;
					}
					mNextSchedule = currentChannel.getSchedules().get(index);
					Logger.d(TAG, "....mNextSchedule.name = "+mNextSchedule.getName());
				}else{
//					showSeekPromptToast("该节目还未开始");
					Log.d(TAG,"no playable schedule");
				}
			}else{
				int index = currentSchedule.getScheduleIndex();
				index ++;
				if (index >= currentChannel.getSchedules().size()){
					index = 0;
				}
				mNextSchedule = currentChannel.getSchedules().get(index);
			}
		}
		Logger.d("iotv play current channel index = " + current_channel_index);

		
		mDataMgr.getCDNToken();
		mIsCompleteViewShowed = false;
	}
	
	private void rollbackChannelState(){
		if (mPrevChannel != null) {
			current_channel_index = mPrevChannelIndex;
			currentChannel = mPrevChannel;
			currentSchedule = mPrevSchedule;
			mCurrentPlayUrl = mPrevPlayUrl;
			mIOTV_Type = mPrevIOTVType;
		}
	}

	@Override
	public IOTVMediaPlayer getIOTVMediaPlayer() {
		// TODO Auto-generated method stub
		return mIOTVPlayer;
	}
	
	private void showSeekPromptToast(){
		if (mSeekPromptToast == null){
			mSeekPromptToast = Toast.makeText(mOwner.getApplicationContext(),
			     "", Toast.LENGTH_LONG);
			
		    ImageView img = new ImageView(mOwner);
		    
		    ColorDrawable cd = new ColorDrawable(Color.BLACK);
		    cd.setAlpha(200);
		    img.setBackgroundDrawable(cd);
		    img.setImageResource(R.drawable.jx_jingxuan_iotv_prevent_seek);
		    mSeekPromptToast.setView(img);
		}
		
		mSeekPromptToast.show();
	}
	
	private void showSeekPromptToast(String str){
		Toast t = Toast.makeText(mOwner.getApplicationContext(),
			     "", Toast.LENGTH_LONG);
		TextView tv = new TextView(mOwner);
	    ColorDrawable cd = new ColorDrawable(Color.BLACK);
	    cd.setAlpha(175);
	    tv.setBackgroundDrawable(cd);
	    
		tv.setText(str);
		tv.setTextSize(22f);
		t.setView(tv);
		t.show();
	}
	
	private void showSeekPromptToast(int strid){
		showSeekPromptToast(mOwner.getResources().getString(strid));
	}

	@Override
	public Schedule getPlayingSchedule() {
		// TODO Auto-generated method stub
		return currentSchedule;
	}
	
	@Override
	public Schedule getPlayingSubSchedule() {
		// TODO Auto-generated method stub
		return currentSubSchedule;
	}

	@Override
	public long getCurrentPlayingTime() {
		// TODO Auto-generated method stub
		long time = IOTV_Date.getDate().getTime();
		
		if (mIOTV_Type == Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE && currentSchedule != null && mIOTVPlayer != null){
			try {
				long start_time = IOTV_Date.dateFormat_full.parse(currentSchedule.getStartTime()).getTime();
				time = start_time + mIOTVPlayer.getCurrentTime() * 1000;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return time;
	}
	
	private void checkAndShowPlayCompleteView(){
		if (mIOTV_Type != Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE || baseViews[PLAY_COMPLETE_VIEW].isshow()){
			return;
		}
		
		long currenttime = mIOTVPlayer.getIOTVCurTime();
		long duration = mIOTVPlayer.getIOTVTotalTime();
		
		long distance = duration - currenttime;
		
		if (distance <= Constants.DISTANCE2SHOW_COMPLETEVIEW){
			if (mIsCompleteViewShowed || duration == 0 || distance == 0){
				return;
			}
			
			showView(PLAY_COMPLETE_VIEW, true);
			mIsCompleteViewShowed = true;
		}else{
			showView(PLAY_COMPLETE_VIEW, false);
		}
	}
	
	public void playNextSchedule() {
		Logger.d(TAG, "enter playNextSchedule");
		// ChannelData data = ChannelManager.getInstance().getNextSchedule(currentChannel, currentSchedule.getStartTime());
		/*modify by xubin 2015.11.11 begin*/
		if (currentSchedule == null) {
			Logger.d(TAG, "currentSchedule == null");
			iotvPlay(currentChannel, null);
			return;
		}
		if (currentSubSchedule == null) {// 正在播放单条节目，非剧集
			Logger.d(TAG, "currentSubSchedule == null");
			List<Schedule> lists = null;
			if(currentChannel != null){
				if(currentChannel.getChannelCode().equals(JXSubscription.CHANNECODE)){
					updateScheduleList(currentChannel);
					lists = utils.filterSchedulesWithPlayableSubScheduleList(currentChannel.getSchedules());
				}else{
					lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null);
				}
			}
			int index = currentSchedule.getScheduleIndex();
			int size = 0;//lists.size();
			if(lists != null){
				size = lists.size();
			}
			if(lists != null && index < lists.size() - 1 && !lists.get(index).equals(currentSchedule)){
				index = utils.findSchedule(lists, currentSchedule);
			}
			index++;
			if (index >= size) {
				index = 0;
			}
			Schedule tmp = null;//lists.get(index);
			if(lists != null){
				tmp = lists.get(index);
			}
			
			iotvPlay(currentChannel, tmp);
		} else {// 正在播放剧集
			//获得当前节目单需要的信息和焦点位置
			List<Schedule> lists = null;
			if(currentChannel != null && currentChannel.getChannelCode().equals(JXSubscription.CHANNECODE)){
				Logger.d(TAG, "playNextSchedule , currentChannel channelCode = "+currentChannel.getChannelCode());
				updateScheduleList(currentChannel);
				lists = utils.filterSchedulesWithPlayableSubScheduleList(currentChannel.getSchedules());
				if(!isSubscribedProgram(currentSchedule)){//如果该节目已取消订阅
					if(getJXCategory() != null){
						for (Channel channel : getJXCategory()) { //currentChannel置为原分类
							if (channel.getChannelCode().equalsIgnoreCase(currentSchedule.getOriginalChannelCode())) {
								currentChannel = channel;
								break;
							}
						}
					}
					lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null); //lists置为原分类的节目单
					if(lists != null){
						for(Schedule sc: lists){//currentSchedule置为原分类中的节目
							if(sc.equalsForSubscribe(currentSchedule)){
								currentSchedule = sc;
								break;
							}
						}
					}
				}
			}else{
				if(currentChannel != null){
					lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null);
				}
			}
			int scheduleIndex = currentSchedule.getScheduleIndex();
			int size = 0;//lists.size();
			if(lists != null){
				size= lists.size();
				//************************//
				for(int i = 0;i<lists.size();i++){
					Logger.d(TAG, "........nname = "+lists.get(i).getName());
				}
			}
			List<Schedule> subList = currentSchedule.getSubScheduleList();
			int subIndex = currentSubSchedule.getScheduleIndex();

			Logger.d(TAG, " , size = "+size+" , subIndex = "+subIndex+" , name = "+subList.get(0).getName()
					+", scheduleIndex = "+scheduleIndex+" , currentSchedule name = "+currentSchedule.getName());
			if ((subIndex == 0) 														// 如果正在播放子集的第一集
					|| (subList != null && !utils.isPlayable(subList.get(subIndex - 1).getEndTime()))) { 	// 或者下一集子集不能播
				if(lists != null && scheduleIndex < lists.size() - 1 && !lists.get(scheduleIndex).getName().equals(currentSchedule.getName())){
					scheduleIndex = utils.findSchedule(lists, currentSchedule);
				}
				scheduleIndex++; 														// 直接切换到下一个节目
				subIndex = 0;															// 的第一个子集
				if (scheduleIndex >= size) {
					scheduleIndex = 0;
				}
				if(lists != null){
					iotvPlay(currentChannel, lists.get(scheduleIndex));
				}else{
					iotvPlay(currentChannel, null);
				}
			} else {																	//此时正在特定节目的分类子集中
				subIndex--;																//则在子集中切换下一子集
				iotvPlay(currentChannel, currentSchedule, subIndex);
			}
		}
		/*modify by xubin 2015.11.11 end*/
	}
	
	public void playPrevSchedule() {
		if (currentSchedule == null) {
			iotvPlay(currentChannel, null);
			return;
		}
		if (currentSubSchedule == null) {// 正在播放单条节目，非剧集
			List<Schedule> lists;
			if(currentChannel.getChannelCode().equals(JXSubscription.CHANNECODE)){
				lists = utils.filterSchedulesWithPlayableSubScheduleList(currentChannel.getSchedules());
			}else{
				lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null);
			}
			int index = currentSchedule.getScheduleIndex();
			//change by zjt 11/12 
			Schedule temp = null;
			if(lists != null){
				int size = lists.size();
				if(index < lists.size() - 1 && !lists.get(index).equals(currentSchedule)){
					index = utils.findSchedule(lists, currentSchedule);
				}
				index--;
				if (index < 0) {
					index = size - 1;
				}
				temp = lists.get(index);
			}
			iotvPlay(currentChannel, temp);
			//iotvPlay(currentChannel, lists.get(index));
		} else {// 正在播放剧集
			// 获得当前节目单需要的信息和焦点位置
			List<Schedule> lists;
			if(currentChannel.getChannelCode().equals(JXSubscription.CHANNECODE)){
				updateScheduleList(currentChannel);
				lists = utils.filterSchedulesWithPlayableSubScheduleList(currentChannel.getSchedules());
				if(!isSubscribedProgram(currentSchedule)){//如果该节目已取消订阅					
					for (Channel channel : getJXCategory()) { //currentChannel置为原分类
						if (channel.getChannelCode().equalsIgnoreCase(currentSchedule.getOriginalChannelCode())) {
							currentChannel = channel;
							break;
						}
					}
					lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null); //lists置为原分类的节目单
					for(Schedule sc: lists){//currentSchedule置为原分类中的节目
						if(sc.equalsForSubscribe(currentSchedule)){
							currentSchedule = sc;
							break;
						}
					}
				}
			}else{
				lists = mDataMgr.getScheduleByCategory(currentChannel.getChannelCode(), null);
			}
			int scheduleIndex = currentSchedule.getScheduleIndex();
			int size = 0;
			if(lists != null){
				size = lists.size();
			}
			
			List<Schedule> subList = currentSchedule.getSubScheduleList();
			int subIndex = currentSubSchedule.getScheduleIndex();

			if (subIndex < subList.size() - 1) {// 如果正在播放子集，且不是最后一集
				subIndex++;
				iotvPlay(currentChannel, currentSchedule, subIndex);
			} else {// 如果正在播放子集的最后一集
				//change by zjt 2015/12/13 , lists.get(scheduleIndex)后面加了.equals
				if(lists != null && scheduleIndex < lists.size() - 1 && !lists.get(scheduleIndex).getName().equals(currentSchedule.getName())){
					scheduleIndex = utils.findSchedule(lists, currentSchedule);
				}
				//change by zjt 2015/11/12
				Schedule tmp = null;
				if(lists != null){
					scheduleIndex--;
					if (scheduleIndex < 0) {
						scheduleIndex = size - 1;
					}

					subIndex = 0;
					tmp = lists.get(scheduleIndex);
				}
				iotvPlay(currentChannel, tmp);
				
				//iotvPlay(currentChannel, lists.get(scheduleIndex));
			}
		}
	}
	
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
	
	private boolean checkSuccess1(){
		if (mIsLoadingFinish){
			String today = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
			if (mIsIOTVAvailable){
				if (!today.equals(currentChannels.day)){
					Message msg = mHandler.obtainMessage(MSG_REFRESH_DATA_FINISH);
					mDataMgr.refreshData(today, msg);
				}
				
				boolean listready = isChannelListReady(currentChannels.list);
				Log.d(TAG, "listready = " + listready);
				
				File f = new File(DataConfig.getFilePath(Constants.CHANNEL_FILE));
				//File f2 = new File(DataConfig.getFilePath(Constants.JX_ALL_SCHEDULE_FILE + today));
				return f.exists() && currentSchedule != null && listready;
			}else{
				File f2 = new File(DataConfig.getFilePath(Constants.JX_ALL_SCHEDULE_FILE + today));
				return f2.exists();
			}
		}
		
		return false;
	}
	
	private boolean isChannelListReady(List<Channel> list){
		if (list != null && list.size() > 0){
			Channel firstChannel = null;
			if(mIsIOTVAvailable){
				for(Channel c:list){
					if(c.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
						firstChannel = c;
						break;
					}
				}
			}
			if(firstChannel == null){
				firstChannel = list.get(1);
			}
			if (firstChannel.getSchedules() != null && firstChannel.getSchedules().size() > 0){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void setOwner(Activity owner) {
		// TODO Auto-generated method stub
		mOwner = owner;
	}
	
	@Override
	public Activity getOwner() {
		// TODO Auto-generated method stub
		return mOwner;
	}

	@Override
	public View getRootView() {
		// TODO Auto-generated method stub
		if (mainLayer == null){
			mainLayer = (ViewGroup) mOwner.getLayoutInflater().inflate(R.layout.jx_iotv_main, null);
		}
		return mainLayer;
	}

	@Override
	public List<Channel> getCurrentChannelList() {
		// TODO Auto-generated method stub
		String today = IOTV_Date.dateFormat.format(IOTV_Date.getDate());

		List<Channel> listtoday = mDataMgr.getChannelsSync(today, true);
		List<Channel> list = ChannelManager.getInstance().cloneChannelList(listtoday);
		
		if (mIOTV_Type == Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE && !today.equals(currentChannels.day)){
			Channel channel_today = list.get(current_channel_index);
			channel_today.setSchedules(currentChannel.getSchedules());
			channel_today.setDay(currentChannels.day);
		}
		
		return list;
	}
	@Override
	public int getCurrentChannelIndex(){
		return current_channel_index; 
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		//if (baseViews[CHANNEL_LIST_VIEW].isshow()) {
			if (mHandler.hasMessages(MSG_AUTO_HIDEVIEW)) {
				mHandler.removeMessages(MSG_AUTO_HIDEVIEW);
			}

			mHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDEVIEW,
					EXPIRE_HIDE_TIME);
		//}
		return false;
	}

	@Override
	public List<Channel> getJXCategory() {
		// TODO Auto-generated method stub
		Log.d(TAG, "getJX Category");
		updateIotvSchedules();
		return currentChannels.list;
	}
	
	private void updateIotvSchedules(){
		if (currentChannels.list != null){
			if (currentChannels.list.size() > 0){
				Logger.d(TAG, "currentChannels.list.size = "+currentChannels.list.size());
				Channel iotv = currentChannels.list.get(getChanelIndexByCode(currentChannels.list, Constants.JX_LATEST_CHANNEL_CODE));

				List<Schedule> iotvSchedule = getJXSchedule(iotv, null);
				if (iotvSchedule != null && iotvSchedule.size() > 0){
					Logger.d(TAG, "iotvSchedule.size = "+iotvSchedule.size());
					iotv.setSchedules(iotvSchedule);
				}
			}
		}
		Logger.d(TAG, "end updateIotvSchedules");
	}
	
	private Schedule updateIotvScheduleIfNeed(Schedule sd){
		Logger.d(TAG, "enter updateIotvScheduleIfNeed");
		long dateNow = IOTV_Date.getDate().getTime();
		long endTime = IOTV_Date.getDateParseFull(sd.getEndTime()).getTime();
		
		Schedule temp = sd;
		
		Channel iotv1 = currentChannels.list.get(getChanelIndexByCode(currentChannels.list, Constants.JX_LATEST_CHANNEL_CODE));
		List<Schedule> tmpschedule = iotv1.getSchedules();
		
		Logger.d("updateIotvScheduleIfNeed dateNow = " + dateNow + " endTime = " + endTime);
		//if this schedule has passed, means iotv schedules were expired,
		//they must be updated immediately.
		if (dateNow >= endTime || !(tmpschedule != null && tmpschedule.size() > 0)){
			updateIotvSchedules();
			
			int index = sd.getScheduleIndex();
			
			Channel iotv = currentChannels.list.get(getChanelIndexByCode(currentChannels.list, Constants.JX_LATEST_CHANNEL_CODE));
			List<Schedule> scheduls = iotv.getSchedules();
			
			if (scheduls != null && scheduls.size() > index){
				temp = scheduls.get(index);
			}
		}
		
		return temp;
	}
	
	@Override
	public List<Schedule> getJXSchedule(Channel c, Message response) {
		// TODO Auto-generated method stub	
		Logger.d("getJX Schedule of channel = " + c.getChannelCode());
		return mDataMgr.getScheduleByCategory(c.getChannelCode(), response);
	}
	
	public void startScheduleCountdown(String endtime, long type){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		long dateNow = IOTV_Date.getDate().getTime();
		long dateEndtime = dateNow;
		try {
			dateEndtime = sdf.parse(endtime)
					.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long delaytime = dateEndtime - dateNow;
		Logger.d("startScheduleCountdown(), nowTime is:" + dateNow
				+ ",Endtime is:" + dateEndtime + ",delaytime is:"
				+ delaytime + ",type is:" + type + ",mIOTV_Type:" + mIOTV_Type);
		mHandler.removeMessages(JX_MSG_STOP_LIVE);
		if (delaytime > 0) {
			Message msg = mHandler.obtainMessage(JX_MSG_STOP_LIVE);
			msg.arg1 = (int) type;
			mHandler.sendMessageDelayed(msg, delaytime);
		}else{
			mHandler.sendEmptyMessage(JX_MSG_STOP_LIVE);
		}
	}
	

	private void showExitToast() {
		showToast("");
		Message delayMsg = mHandler.obtainMessage(JX_MSG_SHOW_EXIT_TOAST);
		mHandler.sendMessageDelayed(delayMsg, AIRPLAY_TOAST_DISPLAY_TIME);
	}
	public void showToast(String msg) {  
		if (mExitToast == null) {
			mExitToast = new Toast(mOwner);
		}
		View toastRoot = mOwner.getLayoutInflater().inflate(
				R.layout.jx_jingxuan_toast, null);
		mExitToast.setView(toastRoot);
		TextView tv = (TextView) toastRoot.findViewById(R.id.TextViewInfo);
		//add by zjt 2015/11/26 for 显示无法播放
		if(msg.startsWith("2055")){
			tv.setText(R.string.err_2055);
		}else{
			tv.setText(R.string.jx_exit_toast_text);
		}
		
		mExitToast.setDuration(Toast.LENGTH_SHORT);
        mExitToast.show();  
    }  
 
    public void cancelToast() {  
            if (mExitToast != null) {  
                mExitToast.cancel();  
            }  
        }

	@Override
	public List<RecommendItem> getRandomSchedules(Channel channel, int size,
			Message response) {
		if(currentSchedule == null){
			return null;
		}
		if(channel.getChannelCode().equals(getCurrentChannel().getChannelCode())){
			return utils.getRandomRecommendList(channel, mDataMgr.getScheduleByCategory(channel.getChannelCode(), null), size, currentSchedule.getScheduleIndex());
		}else{
			return utils.getRandomRecommendList(channel, mDataMgr.getScheduleByCategory(channel.getChannelCode(), null), size, -1);
		}
	}

	@Override
	public Schedule getNextSchedule() {
		// TODO Auto-generated method stub
		return mNextSchedule;
	}

	@Override
	public List<Schedule> getLatestSubscribeSchedules(
			List<Schedule> subscribeSchedules) {
		// TODO Auto-generated method stub
		return mDataMgr.getLatestSubscribeSchedules(subscribeSchedules);
	} 
	


	
	/**
	 * 按照频道code找到频道index
	 * @param ch  当前所有频道List
	 * @param channelCode 要查找的频道Code
	 * @return    要查找的频道编号
	 */
	public int getChanelIndexByCode(List<Channel> ch,String channelCode){
		int i = 0;
		if(currentChannels.list != null && currentChannels.list.size() > 0){
			for(Channel c : currentChannels.list){
				if(c.getChannelCode() != null && c.getChannelCode().equals(channelCode)){
					return i;
				}
				i++;
			}
		}
		return 0;
	}
	
	
	/**
	 * 设置“下一节目信息”的显示的Toast
	 * @param time 需要延时的时间
	 */
	public void setNextScheduleToastByDelayTime(long delayTime){
		int timeOffset = 2*60*1000; //2 Min

		mHandler.removeMessages(JX_MSG_SHOW_NEXT_TOAST);
		Message msg = mHandler.obtainMessage(JX_MSG_SHOW_NEXT_TOAST);
		
		if(delayTime > timeOffset){
			mHandler.sendMessageDelayed(msg, delayTime - timeOffset);
		}else if(delayTime > 2){
			mHandler.sendMessageDelayed(msg, 0);
		}
	}
	
	/**
	 * 设置“下一节目信息”的Toast显示
	 * @param sch
	 */
	public void setNextScheduleToast(Schedule sch){
		if(sch == null){
			Log.e(TAG,"setNextScheduleToast Schedule is null,return");
			return;
		}
		int timeOffset = 2*60*1000; //2 Min
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		long dateNow = IOTV_Date.getDate().getTime();
		long startTime = 0,endTime = 0;
		try {
			startTime = sdf.parse(sch.getStartTime()).getTime();
			endTime = sdf.parse(sch.getEndTime()).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		if(sch.getStartTime().equals("")||sch.getStartTime().length() == 0)
		//直播节目，取当前时间为start time
		if(sch.getChannelCode() == Constants.JX_LATEST_CHANNEL_CODE)
			startTime = dateNow;
		
		//剩余节目时长	
		long scheduleLength = endTime - startTime;	
		
		mHandler.removeMessages(JX_MSG_SHOW_NEXT_TOAST);
		Message msg = mHandler.obtainMessage(JX_MSG_SHOW_NEXT_TOAST);
		
		if(scheduleLength > timeOffset)
			mHandler.sendMessageDelayed(msg, scheduleLength - timeOffset);
		else
			mHandler.sendMessageDelayed(msg, 0);

		}
	
	/**
	 * 显示下一节目信息
	 */
	public void showNextScheduleToast(Schedule sch){
		Logger.d(TAG, "enter showNextScheduleToast");
		if(!isAppOnForeground() || sch == null){
			mHandler.removeMessages(JX_MSG_SHOW_NEXT_TOAST);
			hideNextScheduleToast();
			Log.d(TAG,"showNextScheduleToast return");
			return;
		}
		int displayTime = 10*1000; // 10s 
		String lineOne = mOwner.getResources().getString(R.string.jx_future_schedule);
		String lineTwo = sch.getCompleteName() == null ? sch.getName():sch.getCompleteName();

		//设置Toast
		View toastBackgroud = mOwner.getLayoutInflater().inflate(R.layout.jx_next_schedule_toast, null);
		if(nextScheduleToast == null)
			nextScheduleToast=new Toast(mOwner.getApplicationContext());
		nextScheduleToast.setView(toastBackgroud);
		/*add by xubin begin 2015.11.23 begin*/
		if(is1080PMetrics()){
			nextScheduleToast.setGravity(Gravity.LEFT|Gravity.TOP, 1357, 882);
		}else{
			nextScheduleToast.setGravity(Gravity.LEFT|Gravity.TOP, 857, 585);
		}
		/*add by xubin begin 2015.11.23 end*/
		nextScheduleToast.setDuration(displayTime);
		
		TextView tv1=(TextView)toastBackgroud.findViewById(R.id.LineOne);
		tv1.setText(lineOne);
		
		TextView tv2=(TextView)toastBackgroud.findViewById(R.id.LineTwo);
		tv2.setText(lineTwo);

		nextScheduleToast.show();		
	}
	/*add by xubin 2015.11.23 begin*/
	/**
	 * 是否是1080P分辨率
	 */
	private boolean is1080PMetrics(){
		WindowManager windowManager = mOwner.getWindowManager();    
        Display display = windowManager.getDefaultDisplay();    
        int screenWidth = display.getWidth();    
        int screenHeight = display.getHeight();
        
        return screenHeight == 1080 && screenWidth == 1920;
	}
	/*add by xubin 2015.11.23 end*/
	

	/**
	 * 得到节目列表
	 * 如果是订阅分类，由于节目列表会变动，需要实时更新。
	 * @param channel
	 * @return
	 */
	
	public void updateScheduleList(Channel channel){
		List<Schedule> list = null;
		if(channel.getChannelCode() == Constants.JX_SUBSCRP_CHANNEL_CODE){  //如果是订阅频道，需要实时更新schedule list.
			JXSubscription jxSubscription = JXSubscription.getInstance();
			list = jxSubscription.getSubscriptionSchs();
			channel.setSchedules(list);
		}
		
	}
	
	/**
	 * 隐藏“下一条节目”的Toast显示
	 */
	public void hideNextScheduleToast(){
		if(nextScheduleToast != null)
			nextScheduleToast.cancel();
	}

	@Override
	public void resetStopLiveDelayTime(long time) {
		// TODO Auto-generated method stub
		long type = DataConfig.getDataConfigValueL(mOwner, DataConfig.LIVE_PLAY_ENDACT);
		Logger.d("resetStopLiveDelayTime(), delay time is:" + time + "LivePlayEndAct is:" + type);
		mHandler.removeMessages(JX_MSG_STOP_LIVE);
		if(mIOTV_Type == Constants.IOTV_PLAY_LIVE_SOURCE){
			if (time > 0) {
				Message msg = mHandler.obtainMessage(JX_MSG_STOP_LIVE);
				msg.arg1 = (int) type;
				mHandler.sendMessageDelayed(msg, time);
			}else{
				mHandler.sendEmptyMessage(JX_MSG_STOP_LIVE);
			}
		}
		
		setNextScheduleToastByDelayTime(time);
	}
	
	// 在进程中去寻找当前APP的信息，判断是否在前台运行
	private boolean isAppOnForeground() {
		ActivityManager activityManager = (ActivityManager) mOwner.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = mOwner.getApplicationContext().getPackageName();
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 检查是否是已订阅节目
	 * @param schedule
	 * @return
	 */
	public boolean isSubscribedProgram(Schedule schedule) {
		if(JXSubscription.getInstance().getSubscriptionSchs()==null){//将订阅的节目单拉入内存中
			JXSubscription.getInstance().initSubPrograms();
		}
		if(JXSubscription.getInstance().getSubscriptionSchs()==null){
			return false;
		}
		for(Schedule sch:JXSubscription.getInstance().getSubscriptionSchs()){
			if(sch.getName().equals(schedule.getName())){
				return true;
			}
		}
		return false;
	}

	@Override
	public void resetMediaControlView() {
		resetView(MEDIACONTROL_VIEW);
	}

	/*add by xubin 2015.11.24 begin*/
	@Override
	public IDataModel getIDataModel() {
		return mDataMgr;
	}
	/*add by xubin 2015.11.24 end*/
	@Override
	public void removeShowNextTostMsg() {
		// TODO Auto-generated method stub
		removeShowNextMsg();
	}
	
}
