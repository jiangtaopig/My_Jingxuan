package com.bestv.ott.jingxuan.view;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.JXSubscription;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.TimeUseDebugUtils;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.view.focus.FocusAnimation;
import com.bestv.ott.jingxuan.view.focus.FocusInterface;
import com.bestv.ott.jingxuan.view.focus.SelectInterface;

public class JingxuanChannelListView extends RelativeLayout implements IViewBase {
	// private static final int MSG_UPDATE_CHANNEL_DATE = 100;
	private static final String TAG = "JingxuanChannelListView";
	private static final int UI_FOCUS_DELAY_TIME = 100;
	private static final int DATA_PROGRAM_LOAD_DELAY_TIME = 0;
	private static final int DATA_RECORD_LOAD_DELAY_TIME = 0;
	// private static final int DATA_RECORD_LOAD_DELAY_TIME = 500;
	private static final int MSG_FOCUS_INDEX_LIST = 0x3339;
	private static final int MSG_FOCUS_PROGRAM_LIST = 0x4000;
	private static final int MSG_FOCUS_SUB_PROGRAM_LIST = 0x4001;
	private static final int MSG_LOAD_PROGRAM = 0x4002;
	private static final int MSG_LOAD_RECORD = 0x4003;
	private static final int MSG_UPDATE_PLAYINFO = 0x4004;
	private static final int MSG_UPDATE_SCHEDULE = 0x4009;
	private static final int MSG_SET_SUB_PROGRAM_LIST_SELECTION = 0x400A;
	private static final int MSG_PRELOAD_LIVE_HOT_PROGRAMME = 0x400B;
	
	private static final int UPDATE_PLAYINFO_INTERVAL = 2000;
	
	private static final int SWITCHPAGE_KEYREPEAT_COUNT = 5;
	
	private static final int UPDATE_CHANNEL_PAROGRAM_SOON = 1;
	
	/**
	 * List一页内容数量
	 */
	private static final int MAX_LIST_ITEM_NUM = 8;
	/**
	 * 频道总页数,从0开始算，程序猿的计算方式
	 */
	private int mMaxIndexPage = 0;
	/**
	 * 频道当前页数
	 */
	private int mCurIndexPage = 0;
	/**
	 * 全部频道数
	 */
	private int mTotalChannel = 0;
	/**
	 * 节目总页数,从0开始算，程序猿的计算方式
	 */
	private int mMaxProgramPage = 0;
	/**
	 * 当前节目页数
	 */
	private int mCurProgramPage = 0;
	/**
	 * 当前频道全部节目数
	 */
	private int mTotalSchedule = 0;

	private IControllerBase mController;

	/**
	 * 当前频道索引
	 */
	private SelfFocusListView mCurIndexList = null;
	/**
	 * 当前节目
	 */
	private SelfFocusListView mCurProgramList = null;
	private ImageView mIndexArrowUp = null;
	private ImageView mIndexArrowDown = null;
	private ImageView mProgramArrowUp = null;
	private ImageView mProgramArrowDown = null;
	/**
	 * 为了界面翻页，焦点切换的效果以及世界和平而存在
	 */
	private ImageButton mBtnSafe = null;
	/**
	 * 日期布局
	 */
	private ViewGroup mSubProgramGroup = null;
	private ViewGroup mChannelProgramGroup = null;
	/**
	 * 日期界面频道名字
	 */
	private TextView mNoProgramHit = null;
	/**
	 * 日期列表
	 */
	private SelfFocusListView mSubProgramList = null;
	// private ImageView mImgHelp = null;
	// private Animation mAnimHelp = null;

	private JingxuanChannelIndexAdapter mChannelIndexAdapter;    //Channel list adapter
	private JingxuanChannelProgramAdapter mChannelProgramAdapter;//schedule list adapter
	private JingxuanSubProgramAdapter mSubProgramAdapter;		//sub programs list adapter
	private List<Channel> mCurChannels;     //所有的Programs
	private List<Schedule> mCurPrograms;    //当前显示页的Programs
	//private List<String> mDates = new ArrayList<String>();
	private List<Schedule> mSubPrograms = new ArrayList<Schedule>();//订阅的子剧集
	//当前子剧集
	private List<Schedule> mCurSubPrograms = new ArrayList<Schedule>();
	private int mTotalSubSchedule = 0;
	private int mMaxSubProgramPage = 0;
	private int mCurSubProgramPage = 0;
	private static final int MAX_SUB_LIST_ITEM_NUM = 7;
	//private HashMap<String, Integer> mDateFilterMap = new HashMap<String, Integer>();
	
	/**
	 * 选中焦点
	 */
	private FrameLayout mMainFocusLayout = null;
	
	/**
	 * 焦点所在控件
	 */
	public  static int mFocusedView=0;
	public final static int FOCUSED_CHANNEL=0;//频道列表获得焦点
	public final static int FOCUSED_PROGRAM=1;//节目列表获得焦点
	public final static int FOCUSED_SUB=2;//订阅列表获得焦点
	
	/**
	 * 平移焦点
	 */
	private FocusAnimation mMainFocusAnim = null;
	private MyIndexSelectInterface mIndexSelectListener = null;
	private MyProgramSelectInterface mProgramSelectListener = null;
	private MySubProgramSelectInterface mDateSelectListener = null;
	private List<Channel> mChannels = null;
	private List<Schedule> mPrograms = null;
	//private List<Schedule> mProgramsFilterByDate = new ArrayList<Schedule>();
	private ChannelManager mChannelManager = null;

	private TextView mPlayInfoView;
	
	//just use for lock focus animation to prevent any draw focus request.
	private Object mLockDrawFcosuObj = new Object();
	
	/**
	 * 已经加载完成的频道
	 */
	private Channel mSelectChannel = null;
	/**
	 * 当前播放的频道
	 */
	private Channel mCurPlayChannel = null;
	private Schedule mCurPlaySchedule = null;
	private int mSelectChannelPos = -1;  
	private int mSelectProgramPos = 0;
	

	private int mPlayProgramPos = 0;

	private int mPlayProgramPage = 0;
	
	private int mPlaySubProgramPos = 0;

	private int mPlaySubProgramPage = 0;
	//use for switch list page 
	private int mPrevSelectChannelPos = -1;
	private int mPrevSelectProgramPos = -1;
	/**
	 * yyyyMMdd
	 */
	private String mSelectDate = "";
	private int mSelectSubProgramPos = 0;
	/**
	 * add by zjt 15/11/5
	 * 第一次进入时根据能够播放的子节目来设置播放位置
	 */
	private int mFirstSelectSubProgramPos = 0;
	/**
	 * 第一次进入
	 */
	private boolean mDateRefresh = true;
	private boolean isFirstEnter = true;
	/**
	 * 无视所有频道焦点变化引起的数据更新
	 */
	private boolean mChannelHold = true;
	private boolean mIsDateLoaded = true;
	private boolean mIsFirst = true;
	
	/**
	 * 右下角暂停图标
	 */
	private ImageView mPauseIcon = null;
	
	/**
	 * 订阅按钮
	 */
	private ImageButton mSubscribeButton = null;
	/*add by xubin 2015.12.04  for Bug #11948 begin*/
	private boolean isPosChange = false;
	/*add by xubin 2015.12.04  for Bug #11948 end*/
			
	private boolean checkedSubscribeUpdate = false;//是否查看过“订阅”中的更新，如果焦点移至“订阅”的ProgramList，则置为true。并且在hide()时，将所有订阅中的status改为NORMAL
	private boolean haveUpdateSchedules = false;//是否有订阅内容更新
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_FOCUS_INDEX_LIST:
				int index = msg.arg1;
				mCurIndexList.requestFocus();
				mMainFocusAnim.setLockFocusView(mCurIndexList);
				if (index >= 0) {
					mCurIndexList.setSelection(index);
					mChannelIndexAdapter.setSelectPosition(index);
				} else {
					mCurIndexList.setSelection(mSelectChannelPos);
					mChannelIndexAdapter.setSelectPosition(mSelectChannelPos);
				}
				setSafeButton(false);
				setIsProgramListFocused(false);
				setIsSubProgramListFocused(false);
				setIsChannelListFocused(true);
				refreshIndexArrow();
				mChannelHold = false;
				
				if (msg.arg2 == UPDATE_CHANNEL_PAROGRAM_SOON){
					mCurProgramPage = 0;
					mSelectProgramPos = 0;
					
					JingxuanChannelIndex channelIndex =  (JingxuanChannelIndex) mChannelIndexAdapter.getItem(mSelectChannelPos);
					mSelectChannel = (Channel) channelIndex.getTag();
					
					mSelectChannel.setLookbackSchedule(null);
					channelIndex.setChannelDay(mSelectChannel.getDay());
					
					mHandler.sendEmptyMessageDelayed(MSG_LOAD_PROGRAM,
							DATA_PROGRAM_LOAD_DELAY_TIME);
				}
				break;
			case MSG_FOCUS_PROGRAM_LIST:
				int program = msg.arg1;
				Log.d(TAG, "msg : MSG_FOCUS_PROGRAM_LIST " + program);
				mCurProgramList.requestFocus();
				mMainFocusAnim.setLockFocusView(mCurProgramList);
				if (program != 0) {
					mCurProgramList.setSelection(program);
					if (mChannelProgramAdapter != null) {
					    mChannelProgramAdapter.setSelectPosition(program);
					}
				} else {
					mCurProgramList.setSelection(mSelectProgramPos);
					if (mChannelProgramAdapter != null) {
					    mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
					}
				}
				setSafeButton(false);
				setIsProgramListFocused(true);
				setIsSubProgramListFocused(false);
				setIsChannelListFocused(false);
				refreshProgramArrow();
//				if(mCurProgramList.isFocused() && !mSelectChannel.getChannelCode().equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)){
//					mSubProgramGroup.setVisibility(View.VISIBLE);
//				}else{
//					hideSubProgramGroup();
//				}
				break;
			case MSG_FOCUS_SUB_PROGRAM_LIST:
				int position = msg.arg1;
				Logger.d(TAG, " enter MSG_FOCUS_SUB_PROGRAM_LIST , position = "+position);
				
				mSubProgramList.requestFocus();
				mMainFocusAnim.setLockFocusView(mSubProgramList);
				mSubProgramList.setSelection(position);
				if (mSubProgramAdapter != null) {
					mSubProgramAdapter.setSelectPosition(position);
				}
				setIsSubProgramListFocused(true);
				setIsProgramListFocused(false);
				setIsChannelListFocused(false);
				break;
			case MSG_LOAD_PROGRAM:
				Log.d(TAG, "MSG_LOAD_PROGRAM");
				mChannelProgramGroup.setVisibility(View.INVISIBLE);
				if(mSelectChannel.getChannelCode().equalsIgnoreCase(Constants.JX_SUBSCRP_CHANNEL_CODE)){
					mNoProgramHit.setText(R.string.channel_list_no_subscribe_program);
				}else{
					mNoProgramHit.setText(R.string.channel_list_no_program);
				}
				mNoProgramHit.setVisibility(View.VISIBLE);
				initProgramData();   //初始化当前分类节目信息
				Logger.d(TAG, "code1 = "+mSelectChannel.getChannelCode()+", code2 = "+mController.getCurrentChannel().getChannelCode());
				if(mSelectChannel.getChannelCode().equalsIgnoreCase(mController.getCurrentChannel().getChannelCode()) //如果是播放节目的ChannelCode一致。
						||(subscriptionCanceled())){ 																  //或者当前节目是订阅了而又取消的节目
					setCurProgramPos();
					bindChannelsProgram(true);
					
					
					if (mChannelProgramAdapter != null) {
						mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
					}
					
			
					if(msg.arg1 == 1){
						if((mCurPrograms != null )&& (mCurPrograms.size() > 0)
								&&(mController.getPlayingSubSchedule() != null)){
							
								/*add by xubin 201.12.09 for Bug #12810 begin*/
//								if(mCurIndexList != null){
//									mCurIndexList.requestFocus();
//								}
//								if(mMainFocusAnim != null){
//									mMainFocusAnim.setLockFocusView(mCurIndexList);
//								}
//								if(mCurProgramList != null){
//									mCurProgramList.setSelection(mSelectProgramPos);
//								}
//								if (mChannelProgramAdapter != null) {
//									mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
//								}
			                    /*add by xubin 201.12.09 for Bug #12810 end*/
								
								initSubProgramData();			
								if((mSelectProgramPos == mPlayProgramPos && mCurProgramPage == mPlayProgramPage)){    // 
									setCurSubProgramPos();
								}else{
									mSelectSubProgramPos = 0;
									mCurSubProgramPage = 0;
								}
								/*modify by xubin 2015.12.14 for Bug #11622 begin*/
								/*if(mController != null && mController.getIOTVMediaPlayer().isPlaying()){
									bindSubProgram();
								}else{
									setCurSubProgramPos();
									mSelectSubProgramPos = mSelectSubProgramPos < 0 ? 0 : mSelectSubProgramPos;
									if(mSelectSubProgramPos == 0){
										bindSubProgram();
									}
								}*/
								bindSubProgram();
								/*modify by xubin 2015.12.14 for Bug #11622 end*/
								//显示节目订阅状态
								if(isSubscribedProgram(mCurPrograms.get(mSelectProgramPos))){
									mSubscribeButton.setBackgroundResource(R.drawable.jx_cancel_subscribe);
								}else{
									mSubscribeButton.setBackgroundResource(R.drawable.jx_subscribe);
								}
								
								showDate();
						}else{
							hideSubProgramGroup();

							mCurProgramList.requestFocus();
							/*modify by xubin for bug 11631 2015.11.26 begin*/
							if(mPrevSelectProgramPos != 0){
								mMainFocusAnim.setLockFocusView(mCurProgramList);
							}
							/*modify by xubin for bug 11631 2015.11.26 end*/
							setIsProgramListFocused(true);
							setIsSubProgramListFocused(false);
							setIsChannelListFocused(false);
							mCurProgramList.setSelection(mSelectProgramPos);
							if (mChannelProgramAdapter != null) {
								mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
							}
						}
					}else{
						mCurProgramList.setSelection(mSelectProgramPos);
						if (mChannelProgramAdapter != null) {
							mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
						}
					}
					
				}else{  //不是和当前播放节目分类相同，焦点在分类列表上
					hideSubProgramGroup();
					bindChannelsProgram(true);
					setIsProgramListFocused(false);
					setIsSubProgramListFocused(false);
					setIsChannelListFocused(true);
					if(msg.arg1 == 1){
						mCurProgramList.requestFocus();
						mMainFocusAnim.setLockFocusView(mCurProgramList);
						setIsProgramListFocused(true);
						setIsSubProgramListFocused(false);
						setIsChannelListFocused(false);
					}
					mCurProgramList.setSelection(0);
					if (mChannelProgramAdapter != null) {
						mChannelProgramAdapter.setSelectPosition(0);
					}
				}
				
				refreshProgramArrow();
				TimeUseDebugUtils.timeUsageDebug(TAG+" ,  end MSG_LOAD_PROGRAM");
				
//				if(msg.arg2 == 1){
//					mHandler.sendEmptyMessageDelayed(MSG_SET_SUB_PROGRAM_LIST_SELECTION, 20);
//				}
				break;
			case MSG_LOAD_RECORD:
				//initChannelsData(mSelectDate);
				//bindChannelsIndex(false);
				Logger.d(TAG, "MSG_LOAD_RECORD");
				mSelectChannel = mCurChannels.get(mSelectChannelPos);
				initProgramData();
				setCurProgramPos();
				bindChannelsProgram(true);
				refreshProgramArrow();
				new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						try {
							sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mIsDateLoaded = true;
					}

				}.start();
				break;
			case MSG_UPDATE_PLAYINFO:
				if (isshow() && mPlayInfoView.isShown()) {
					int playRate = mController.getIOTVMediaPlayer().getPlayRate();
					int downRate = mController.getIOTVMediaPlayer().getDownRate();

					if (downRate > 0) {
						mPlayInfoView.setText(String.format(getResources()
								.getString(R.string.play_detail_info),
								playRate, downRate));
					}

					sendEmptyMessageDelayed(MSG_UPDATE_PLAYINFO,
							UPDATE_PLAYINFO_INTERVAL);
				}
				break;
			case MSG_UPDATE_SCHEDULE:
				List<Schedule> scheduleList = (List<Schedule>) msg.obj;
				if(!scheduleList.get(0).getChannelCode().equals(mSelectChannel.getChannelCode())){
					break;
				}
				if(scheduleList != null){
					mTotalSchedule = scheduleList.size();
					// 0开始计数
					mMaxProgramPage = mTotalSchedule / MAX_LIST_ITEM_NUM - 1;
					mMaxProgramPage = mMaxProgramPage < 0 ? 0 : mMaxProgramPage;
					if (mTotalSchedule > MAX_LIST_ITEM_NUM
							&& mTotalSchedule % MAX_LIST_ITEM_NUM > 0) {
						mMaxProgramPage++;
					}
					Log.d(TAG, "mMaxProgramPage : " + mMaxProgramPage
							+ " , mCurProgramPage :" + mCurProgramPage);
					mPrograms = scheduleList;
				}
				
				bindChannelsProgram(true);
				refreshProgramArrow();
				mSelectProgramPos = 0;
				if (mChannelProgramAdapter != null) {
					mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
				}
				break;
			case MSG_SET_SUB_PROGRAM_LIST_SELECTION:
				mSubProgramList.setSelection(msg.arg1);
				break;
			case MSG_PRELOAD_LIVE_HOT_PROGRAMME:
				if(mChannels != null && mChannels.size() > 0){
					for(Channel c : mChannels){
						if(c.getChannelCode().equals(Constants.JX_HOT_CHANNEL_CODE)){
							mController.getJXSchedule(c, mHandler.obtainMessage(MSG_UPDATE_SCHEDULE));
						}
					}
				}
				break;
			default:
				break;
			}
		}

	};
	
	public JingxuanChannelListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public JingxuanChannelListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public JingxuanChannelListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setDataController(IControllerBase c) {
		// TODO Auto-generated method stub
		mController = c;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub		
		Logger.d(TAG,"enter show");
		initViews();
		
		boolean flag = initData();
		if (!flag) {
			Log.d(TAG, "initData failed");
			hide();
			return;
		}
		
		initState();
		
		this.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void reset() {
		
	}
	
	private void initState(){
		Logger.d(TAG, "enter initState");
		if(JXSubscription.getInstance().getSubscriptionSchs()==null){//将订阅的节目单拉入内存中
			JXSubscription.getInstance().initSubPrograms();
		}
		
		if (DataConfig.getDataConfigValueL(getContext(), DataConfig.DEFAULT_SHOW_DOWNDETAIL) > 0) {
			mPlayInfoView.setVisibility(View.VISIBLE);
		} else {
			mPlayInfoView.setVisibility(View.GONE);
		}
		
		if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
			hideSubProgramGroup();
		} else {
			mDateRefresh = false;
			//mChannelDateAdapter.setSelection(mSelectDatePos);
			//showDate();
//			mChannelDateGroup.setVisibility(View.INVISIBLE);
		}
		
//		bindChannelDates();
		bindChannelsIndex(true);
		Message msg = mHandler.obtainMessage(MSG_LOAD_PROGRAM);
		Logger.d(TAG, "initState , mIsFirst = "+mIsFirst+" ,mSelectChannelPos = "+mSelectChannelPos);
		if (mIsFirst){//change by zjt 2015/11/18 13:55 第一次进入时焦点在相应的位置上
			//mMainFocusAnim.drawFocus((View)mChannelIndexAdapter.getItem(mSelectChannelPos));
//			mCurIndexList.requestFocus();
			//mIsFirst = false; //add by zjt 2015/11/19 9:35
			mCurIndexList.setSelection(mSelectChannelPos);
			mChannelIndexAdapter.setSelectPosition(mSelectChannelPos);
//			mMainFocusAnim.setLockFocusView(mCurIndexList);
			//setCurProgramPos();
			msg.arg2=1;
//			bindChannelsProgram(true);
//			mCurProgramList.requestFocus();
//			mCurProgramList.setSelection(mSelectProgramPos);
//			mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
//			mMainFocusAnim.setLockFocusView(mCurProgramList);
//			mHandler.sendEmptyMessageDelayed(MSG_SET_DATE, 20);
		}else{
			mSelectChannelPos = 1;//WARNNING!!强制播放第1个channel（从0开始计算），这样处理并不好。modified by ding.jian，2015/6/9
			mChannelIndexAdapter.setSelectPosition(mSelectChannelPos);
			//requestIndexItemFocus(mSelectChannelPos, UI_FOCUS_DELAY_TIME);
			mIsFirst = false;
		}
		
		msg.arg1=1;
		// 加载节目数据
		mHandler.sendMessage(msg);
		// start update player info
		mHandler.sendEmptyMessage(MSG_UPDATE_PLAYINFO);
	}

	private boolean initData() {
		Log.d(TAG, "enter initData");
		boolean flag = true;
		haveUpdateSchedules = false;
		mMaxIndexPage = 0;
		mCurIndexPage = 0;
		mTotalChannel = 0;
		//mMaxProgramPage = 0;
		//mCurProgramPage = 0;
		mTotalSchedule = 0;
		mSelectChannelPos = 0;
		// mSelectDate = "";
		//mSelectDatePos = 0;
		//mDates = IOTV_Date.instance().getPrevDays();

		mChannelManager = ChannelManager.getInstance();

		mCurPlaySchedule = mController.getPlayingSchedule();
		mSelectDate = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
		//try to update channel list if system date changed.
		mController.setCurrentChannel(mSelectDate, null);
//			Date date = IOTV_Date.getDateParseFull(mCurPlaySchedule
//					.getStartTime());
//			mSelectDate = IOTV_Date.getDateFormatNormal(date);
		Log.d("Karel", "mSelectDate : " + mSelectDate);
		initChannelsData(mSelectDate);
		
		//mHandler.sendEmptyMessage(MSG_PRELOAD_LIVE_HOT_PROGRAMME);
		/*modify by xubin 2015.11.09 begin*/
		if(mController != null && mChannels != null 
				&& mController.getCurrentChannelIndex() < mChannels.size()
				&&mController.getCurrentChannelIndex() >=0 ){//add by zjt 2016/1/27 for 数组越界
			mCurPlayChannel = mChannels.get(mController.getCurrentChannelIndex());
		}
		/*modify by xubin 2015.11.09 end*/
		if (mCurPlayChannel == null) {
			Log.d(TAG, "mCurPlayChannel is null");
			flag = false;
			return flag;
		}
		Logger.d(TAG, "mCurPlayChannel.name = "+mCurPlayChannel.getChannelName());
//		else if(mCurPlayChannel.getChannelCode() == Constants.JX_SUBSCRP_CHANNEL_CODE   //如果当前播放节目在订阅分类
//				&&(!isSubscribedProgram(mCurPlaySchedule)								 //但已经被取消订阅
//				&&(mCurPlaySchedule.getOriginalChannelCode() != null))){				 //且有被设置过原始ChannelCode的，则焦点回到原始channelCode中
//			int realChannelIndex,realScheduleIndex;
//			if(mCurPlaySchedule.getOriginalChannelCode() != null){
//				mCurPlayChannel.setChannelCode(mCurPlaySchedule.getOriginalChannelCode());
//			
//			//找到实际Channel
//			realChannelIndex = findChannelIndexByCode(mCurPlayChannel,mChannels);
//			if(realChannelIndex > 0)
//				mCurPlayChannel = mChannels.get(realChannelIndex);
//			
//			//找到实际Schedule，更新index字段
//			realScheduleIndex = findSchduleIndexByName(mCurPlaySchedule,mCurPlayChannel);
//			if(realScheduleIndex > 0)
//				mCurPlaySchedule.setScheduleIndex(realScheduleIndex);
//			}
//		}		
		setCurChannelPos();		
		Log.d(TAG, "leave initData");
		return flag;
	}

	
//	public void setFirstSelectSubProgramPos(int pos){
//		mFirstSelectSubProgramPos = pos;
//	}
	
	
	
	/**
	 * 加载指定日期的所有平道信息
	 * 
	 * @param date
	 */
	private void initChannelsData(String day) {
		Log.d(TAG, "enter initChannelsData day : " + day);

		mChannels = mController.getJXCategory();
		//Subscription channel handling .add by ding.jian .2015/6/8
		JXSubscription jxSubscription = JXSubscription.getInstance();
		haveUpdateSchedules = haveUpdateSchedules(jxSubscription.getSubscriptionSchs());
		if (mChannels != null && mChannels.size() > 0) {
			Log.d(TAG, "mChannels.size() : " + mChannels.size());
			mTotalChannel = mChannels.size();
			// 0开始计数
			mMaxIndexPage = mTotalChannel / MAX_LIST_ITEM_NUM - 1;
			mMaxIndexPage = mMaxIndexPage < 0 ? 0 : mMaxIndexPage;
			if (mTotalChannel > MAX_LIST_ITEM_NUM
					&& mTotalChannel % MAX_LIST_ITEM_NUM > 0) {
				mMaxIndexPage++;
			}

		} else {
			Log.d(TAG, "mChannels is null");
		}
		Log.d(TAG, "leave initChannelsData ,haveUpdateSchedules = "+haveUpdateSchedules);
	}

	@Override
	public void setFirstSelectSubProgramPos(int position) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "enter setFirstSelectSubProgramPos position = "+position);
		mFirstSelectSubProgramPos = position;
	}

	private boolean haveUpdateSchedules(List<Schedule> subscriptionSchs) {
		// TODO Auto-generated method stub
		if(subscriptionSchs == null){
			return false;
		}
		List<Schedule> l = mController.getLatestSubscribeSchedules(subscriptionSchs);
		if(l != null){
			for(Schedule sc : l){
				if(sc.getSubscribeStatus() == Schedule.SUBSCRIBE_STATUS_UPDATE){
					return true;
				}
			}
			return false;
		}else{
			return false;
		}
		
	}

	private void initViews() {
		Log.d(TAG, "enter initViews jxchannelListview");
		if (mPlayInfoView != null){
			return;
		}
		mSubscribeButton = (ImageButton) findViewById(R.id.ib_subscribe);
		mPauseIcon = (ImageView) findViewById(R.id.iv_pause);
		mIndexSelectListener = new MyIndexSelectInterface();
		mProgramSelectListener = new MyProgramSelectInterface();
		mDateSelectListener = new MySubProgramSelectInterface();
		mPlayInfoView = (TextView) findViewById(R.id.playinfo);
		mSubProgramGroup = (ViewGroup) findViewById(R.id.linear_channel_date);

		mChannelProgramGroup = (ViewGroup) findViewById(R.id.linear_program);
		mNoProgramHit = (TextView) findViewById(R.id.txt_no_program);

		mSubProgramList = (SelfFocusListView) findViewById(R.id.channel_date);
		// mIndexFlipper.setInAnimation(getContext(), android.R.anim.fade_in);
		// mIndexFlipper.setOutAnimation(getContext(), android.R.anim.fade_out);
		mCurIndexList = (SelfFocusListView) findViewById(R.id.channel_index_1);
		mCurIndexList.setFocusable(true);
		//mCurIndexList.setFocusableInTouchMode(true);
		
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mCurIndexList
				.getLayoutParams();
		layoutParams.height = (int) (MAX_LIST_ITEM_NUM * (getResources()
				.getDimension(R.dimen.jx_channel_index_height) + getResources()
				.getDimension(R.dimen.jx_oper_item_spacing)));
		mCurIndexList.setLayoutParams(layoutParams);
		// mProgramFlipper.setInAnimation(getContext(), android.R.anim.fade_in);
		// mProgramFlipper.setOutAnimation(getContext(),
		// android.R.anim.fade_out);
		mCurProgramList = (SelfFocusListView) findViewById(R.id.channel_program_1);
		layoutParams = (FrameLayout.LayoutParams) mCurProgramList
				.getLayoutParams();
		layoutParams.height = (int) (MAX_LIST_ITEM_NUM * (getResources()
				.getDimension(R.dimen.jx_channel_program_height) + getResources()
				.getDimension(R.dimen.jx_oper_item_spacing)));
		mCurProgramList.setLayoutParams(layoutParams);
		// mChannelProgram2 = (ListView) findViewById(R.id.channel_program_2);
		mIndexArrowUp = (ImageView) findViewById(R.id.channel_index_up_arrow);
		mIndexArrowDown = (ImageView) findViewById(R.id.channel_index_down_arrow);
		mProgramArrowUp = (ImageView) findViewById(R.id.channel_program_up_arrow);
		mProgramArrowUp.setVisibility(View.INVISIBLE);
		mProgramArrowDown = (ImageView) findViewById(R.id.channel_program_down_arrow);
		mProgramArrowDown.setVisibility(View.INVISIBLE);
		mMainFocusLayout = (FrameLayout) findViewById(R.id.frame_main_focus);
		mMainFocusAnim = new FocusAnimation(getContext(), mMainFocusLayout);
		
		//lock focus animation until channel list view initialize finish
		mMainFocusAnim.setLockFocusView(mLockDrawFcosuObj);

		mBtnSafe = (ImageButton) findViewById(R.id.btn_safe);
		// refreshIndexArrow();

		//mMainFocusAnim.setFocusListener(mFocusInterface);
		mMainFocusAnim.setOffset((int) getContext().getResources()
				.getDimension(R.dimen.jx_tv_focus_offset));
		// mImgHelp = (ImageView) findViewById(R.id.img_help);
		// mAnimHelp = AnimationUtils.loadAnimation(getContext(),
		// R.anim.tv_help);
		// mImgHelp.startAnimation(mAnimHelp);
		
		mMainFocusAnim.foucsChange(mSubscribeButton);
		SubscribeButtonFocusInterface mFocusListener = new SubscribeButtonFocusInterface();
		mMainFocusAnim.setFocusListener(mFocusListener);
		mSubscribeButton.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_LEFT:
						if (mCurProgramList != null && mCurPrograms != null
								&& mCurPrograms.size() > 0) {
							if (mIsDateLoaded) {
								if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
									//mChannelDateGroup
									//		.setVisibility(View.INVISIBLE);
								}
								mMainFocusAnim.setLockFocusView(mCurProgramList);
								requestProgramItemFocus(mSelectProgramPos,0);
								setIsProgramListFocused(true);
								mFocusedView=FOCUSED_PROGRAM;
							}
						} else {
							if (mIsDateLoaded) {
								if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
									//mChannelDateGroup
									//		.setVisibility(View.INVISIBLE);
								}
								mMainFocusAnim.setLockFocusView(mCurIndexList);
								requestIndexItemFocus(mSelectChannelPos, 0);
								setIsChannelListFocused(true);
								mFocusedView=FOCUSED_CHANNEL;
							}
						}
						return true;
					case KeyEvent.KEYCODE_DPAD_RIGHT:

						return true;
					case KeyEvent.KEYCODE_DPAD_UP:
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
						Logger.d(TAG, " , mSubscribeButton KEYCODE_DPAD_DOWN");
						mSubProgramList.requestFocus();
						mMainFocusAnim.setLockFocusView(mSubProgramList);
						mSelectSubProgramPos = mSelectSubProgramPos < 0 ? 0 : mSelectSubProgramPos;
						requestSubProgramItemFocus(mSelectSubProgramPos);
						setIsSubProgramListFocused(true);
						return true;
					default:
						break;
					}
				}
				return false;
			}
		});
		mSubscribeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 订阅
				JXSubscription jxSubscription = JXSubscription.getInstance();
				/*modify by xubin 2015.11.10 begin*/
				if(mCurPrograms != null && jxSubscription != null){
					if (isSubscribedProgram(mCurPrograms.get(mSelectProgramPos))) {// 如果是订阅过的节目，则执行取消订阅
						mSubscribeButton.setBackgroundResource(R.drawable.jx_subscribe2);
						jxSubscription.cancelSubscribeSchBySchedule(mCurPrograms.get(mSelectProgramPos));
						jxSubscription.save();
	//					initProgramData();
					}else{//如果不是已订阅的节目，则执行订阅
						Schedule sch = (Schedule)mCurPrograms.get(mSelectProgramPos).clone();
						if(sch != null){
							if(sch.getChannelCode().equals(Constants.JX_SUBSCRP_CHANNEL_CODE)){
								sch.setOriginalChannelCode(sch.getOriginalChannelCode());//store the original channel Code 
							}else{
								sch.setOriginalChannelCode(sch.getChannelCode()); //store the original channel Code
							}
							sch.subProgramInit();//订阅初始化
						}
						
						if(jxSubscription.setSubscribeSchBySchedule(sch)){  //订阅成功，显示成功Toast
	//						Log.d("lhh", sch.getName() + " subscribe time is : "+subscribeTime);
							jxSubscription.save();
							mSubscribeButton.setBackgroundResource(R.drawable.jx_cancel_subscribe2);
							showSubscribedToast(mSelectChannel,mCurPrograms.get(mSelectProgramPos));
						}else{    		//订阅失败，显示失败Toast
							utils.showToast(mController.getOwner().getApplicationContext()
									,getResources().getString(R.string.prompt_subscribe_too_much));
						}
					}
				}
				/*modify by xubin 2015.11.10 end*/
			}
		});
		
		mChannelIndexAdapter = new JingxuanChannelIndexAdapter(getContext(), mController, mSelectDate);
		mCurIndexList.setAdapter(mChannelIndexAdapter);
		mMainFocusAnim.selectChange(mCurIndexList, mIndexSelectListener);
		mCurIndexList.setFocusAnimation(mMainFocusAnim);

		mCurIndexList.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				mChannelHold = false;

				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_LEFT:

						return true;
					case KeyEvent.KEYCODE_DPAD_RIGHT:
						/*add by xubin 2015.12.04  for Bug #11948 begin*/						
						isPosChange = mPrevSelectProgramPos == mSelectProgramPos;
						if(!selectChannelSchedulePlayingSchedulesSame(mSelectProgramPos) && isPosChange){
							mSelectProgramPos = 0;
						}
						/*add by xubin 2015.12.04 for Bug #11948 end*/
						if (mCurProgramList != null && !isViewVisible(mNoProgramHit)) {
							//mChannelDateGroup.setVisibility(View.VISIBLE);
							
							mMainFocusAnim.setLockFocusView(mCurProgramList);
							
//							mSelectDate = mSelectChannel.getDay();
//							mSelectDatePos = mDates.indexOf(mSelectDate);
//							Log.d("Karel", "mSelectDatePos : " + mSelectDatePos);
//							mSelectDatePos = mSelectDatePos < 0 ? 0 : mSelectDatePos;
//							mDateList.setSelection(mSelectDatePos);
							
							requestProgramItemFocus(mSelectProgramPos, 0);
							setIsProgramListFocused(true);
							mFocusedView=FOCUSED_PROGRAM;
							setIsChannelListFocused(false);
						}
						if (mCurProgramList == null || mCurPrograms == null
								|| mCurPrograms.size() <= 0) {
							//showDate();
						}
						return true;
					case KeyEvent.KEYCODE_DPAD_UP:
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
						break;
					default:
						break;
					}
				}
				return false;
			}
		});

		mCurIndexList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
//				if (!mCurPlayChannel.getChannelName().equals(mSelectChannel.getChannelName())) {
//					mController.setCurrentChannel(mSelectDate, null);
//					mController.iotvPlay(mSelectChannel, null);
//					hide();
//				}
			}
		});

		Log.d(TAG, "leave initViews");
	}

	/**
	 * 绑定当前频道信息
	 */
	private void bindChannelsIndex(boolean needBind) {
		Log.d(TAG, "enter bindChannelsIndex");
		if (mChannels != null) {
			int start = 0;
			int end = 0;
			
			if (mCurIndexPage <= mMaxIndexPage) {
				start = mCurIndexPage * MAX_LIST_ITEM_NUM;
			}
			
			if (mCurIndexPage < mMaxIndexPage) {
				end = (mCurIndexPage + 1) * MAX_LIST_ITEM_NUM;
			} else {
				end = mTotalChannel;
			}
			
			if (end < start) {
				Log.w(TAG, "start :" + start + ", end :" + end);
			} else {
				mCurChannels = mChannels.subList(start, end);
				//for (Channel channel : mCurChannels) {
				//	Log.d(TAG, "channelName : " + channel.getChannelName());
				//}
			}
			
			mChannelIndexAdapter.updateListContent(mCurChannels, haveUpdateSchedules);
		}
	}

	private void refreshIndexArrow() {
		Log.d(TAG, "mMaxIndexPage : " + mMaxIndexPage + " , mCurIndexPage :"
				+ mCurIndexPage);
		mIndexArrowUp.setVisibility(View.VISIBLE);
		mIndexArrowDown.setVisibility(View.VISIBLE);
		// if (mCurIndexPage <= 0) {
		// mBtnPrevIndex.setFocusable(false);
		// mIndexArrowUp.setVisibility(View.INVISIBLE);
		// }
		// if (mCurIndexPage >= mMaxIndexPage) {
		// mBtnNextIndex.setFocusable(false);
		// mIndexArrowDown.setVisibility(View.INVISIBLE);
		// }
		// if (mCurIndexPage <= 0) {
		// mBtnPrevIndex.setFocusable(false);
		// mIndexArrowUp.setVisibility(View.INVISIBLE);
		// }
		// if (mCurIndexPage >= mMaxIndexPage) {
		// mBtnNextIndex.setFocusable(false);
		// mIndexArrowDown.setVisibility(View.INVISIBLE);
		// }
	}

	private void initProgramData() {
		Logger.d("enter initProgramData");
		if (mSelectChannel != null
				&& mSelectChannel.getChannelCode().equalsIgnoreCase(Constants.JX_SUBSCRP_CHANNEL_CODE)) {  //若是订阅分类，需要特殊处理,add by ding.jian
			Log.d(TAG, "init Subscription Programs(schedules)");
			JXSubscription jxSubscription = JXSubscription.getInstance();
			
			jxSubscription.subscriptionSchs=mController.getLatestSubscribeSchedules(jxSubscription.subscriptionSchs);
			mPrograms = JXSubscription.getInstance().getSubscriptionSchs();
			if ((mPrograms != null)&&(mPrograms.size() > 0)) {
				jxSubscription.setChannel(JXSubscription.CHANNELNAME,JXSubscription.CHANNECODE);
				mTotalSchedule = mPrograms.size();
			} else {
				mTotalSchedule = 0;
				mCurPrograms = null;
			}
			// // 0开始计数
			mMaxProgramPage = mTotalSchedule / MAX_LIST_ITEM_NUM - 1;
			mMaxProgramPage = mMaxProgramPage < 0 ? 0 : mMaxProgramPage;
			if (mTotalSchedule > MAX_LIST_ITEM_NUM
					&& mTotalSchedule % MAX_LIST_ITEM_NUM > 0) {
				mMaxProgramPage++;
			}
			Log.d(TAG, " In subcsription mMaxProgramPage : " + mMaxProgramPage
					+ " , mCurProgramPage :" + mCurProgramPage);
		}else if(mSelectChannel != null && mSelectChannel.getChannelCode().equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)){
			if (mSelectChannel.getSchedules() != null
					&& mSelectChannel.getSchedules().size() > 0) {
	
				Logger.d(TAG, "mSelectChannel size = "+mSelectChannel.getSchedules().size());
				mTotalSchedule = mSelectChannel.getSchedules().size();
				// 0开始计数
				mMaxProgramPage = mTotalSchedule / MAX_LIST_ITEM_NUM - 1;
				mMaxProgramPage = mMaxProgramPage < 0 ? 0 : mMaxProgramPage;
				if (mTotalSchedule > MAX_LIST_ITEM_NUM && mTotalSchedule % MAX_LIST_ITEM_NUM > 0) {
					mMaxProgramPage++;
				}
				Log.d(TAG, "mMaxProgramPage : " + mMaxProgramPage
						+ " , mCurProgramPage :" + mCurProgramPage);
				mPrograms = mSelectChannel.getSchedules();
			} else {
				Log.w(TAG, "this channel has no data");
				mPrograms = null;
				mCurPrograms = null;
			}
		}else if(mSelectChannel != null){
			Message msg = mHandler.obtainMessage(MSG_UPDATE_SCHEDULE);
			List<Schedule> scheduleList = mController.getJXSchedule(mSelectChannel, msg);    
			if(scheduleList != null){
				mTotalSchedule = scheduleList.size();
				Logger.d(TAG, "mTotalSchedule size = "+scheduleList.size());
				// 0开始计数
				mMaxProgramPage = mTotalSchedule / MAX_LIST_ITEM_NUM - 1;
				mMaxProgramPage = mMaxProgramPage < 0 ? 0 : mMaxProgramPage;
				if (mTotalSchedule > MAX_LIST_ITEM_NUM
						&& mTotalSchedule % MAX_LIST_ITEM_NUM > 0) {
					mMaxProgramPage++;
				}
				Log.d(TAG, "mMaxProgramPage : " + mMaxProgramPage
						+ " , mCurProgramPage :" + mCurProgramPage);
				mPrograms = scheduleList;
			}else{
				mPrograms = null;
				mCurPrograms = null;
			}
		}else {
			Log.w(TAG, "this channel has no data");
			mPrograms = null;
			mCurPrograms = null;
		}
	}

	/***
	 * 绑定频道数据，即第二列的listview
	 * @param refreshDateList
	 */
	private void bindChannelsProgram(boolean refreshDateList) {
		Log.d(TAG,"enter bindChannelsProgram : "+ mSelectChannel.getChannelName());
		if (mPrograms != null && mPrograms.size() > 0) {
			int start = 0;
			int end = 0;
			if (mCurProgramPage <= mMaxProgramPage) {
				start = mCurProgramPage * MAX_LIST_ITEM_NUM;
			}
			if (mCurProgramPage < mMaxProgramPage) {
				end = (mCurProgramPage + 1) * MAX_LIST_ITEM_NUM;
			} else {
				end = mTotalSchedule;
			}
			Log.d(TAG, "start :" + start + ", end :" + end);
			if (end < start) {
				start = end;
				mCurPrograms = null;
			} else {
				mCurPrograms = new ArrayList<Schedule>(mPrograms.subList(start, end));
			}
		}
		
		if (mCurPrograms != null && mCurPrograms.size() > 0) {
			Log.d(TAG, "mCurPrograms.size : " + mCurPrograms.size());
			mChannelProgramGroup.setVisibility(View.VISIBLE);
			mNoProgramHit.setVisibility(View.INVISIBLE);

			if (mChannelProgramAdapter != null) {
				mChannelProgramAdapter.releaseData();
				mChannelProgramAdapter.refreshListData(mCurPrograms,
						mSelectChannel);
			} else {
				Logger.d("mChannelProgramAdapter is null");
				mChannelProgramAdapter = new JingxuanChannelProgramAdapter(
						getContext(), mCurPrograms, mSelectChannel, mController);
				mCurProgramList.setAdapter(mChannelProgramAdapter);
				
				mCurProgramList.setFocusAnimation(mMainFocusAnim);
				mCurProgramList.setSelection(mFirstSelectSubProgramPos);//add by zjt 2015/11/5 for choose focus
				
				mCurProgramList.setFocusable(true);
				mMainFocusAnim.selectChange(mCurProgramList, mProgramSelectListener);
				mCurProgramList.setFocusAnimation(mMainFocusAnim);
				
				mCurProgramList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						/*modify by xubin 2015.11.10 begin*/
						Schedule schedule = null;//(Schedule) mChannelProgramAdapter.getItem(position);
						Logger.d(TAG, "mCurProgramList onItemClick position = "+position);
						if(mChannelProgramAdapter != null 
								&& mChannelProgramAdapter.getCount() > 0){//add by zjt 2016/1/27 for Invalid index 0, size is 0
							//Logger.d(TAG, "mCurProgramList onItemClick mChannelProgramAdapter size  = "+mChannelProgramAdapter.getCount());
							schedule = (Schedule) mChannelProgramAdapter.getItem(position);
						}
						if(schedule != null && schedule.getSubScheduleList() != null){//剧集，从最新一条的第一部分开始播放
							Logger.d(TAG, "currentClick schedule.name = "+schedule.getCompleteName()
									+" , subSchedule size = "+schedule.getSubScheduleList().size());
							int firstPlayIndex = utils.getIndexOfTheFirstPartInTheFirstPlayableSchedule(schedule.getSubScheduleList());
							if(firstPlayIndex < 0){  //对没有可播放的分集，进行包含
								utils.showToast(mController.getOwner().getApplicationContext(),"该频道还未开始");
								Log.d(TAG,"no playable schdule at first part");
								return;
							}
							Logger.d(TAG, "firstPlayIndex = "+firstPlayIndex);
							Schedule scheduletemp = schedule.getSubScheduleList().get(firstPlayIndex);
							if (utils.isPlayable(scheduletemp.getStartTime())) {
								JingxuanChannelProgram program = (JingxuanChannelProgram) view;
								Log.d(TAG,"mSelectChannel : "+ mSelectChannel.getChannelName());
								Log.d(TAG, "schedule : " + scheduletemp.getName());
								if (mController.getPlayingSubSchedule() != null && mController.getPlayingSubSchedule()
										.equals(scheduletemp)) {// 如果点击的是当前播放的节目，则不重新加载节目播放
									mController.getIOTVMediaPlayer().unpause();
									setPauseIconVisible(false);
								} else {
									mController.iotvPlay(mSelectChannel,schedule, firstPlayIndex);
									Log.d(TAG, "mCurProgramList onItemClick resetMediaControlView");
									mController.resetMediaControlView();
								}
//								if(mChannelProgramAdapter.getChannelPrograms()!=null){
//									mChannelProgramAdapter.getChannelPrograms().get(mPlayProgramPos).setProgressViewVisible(false, false);
//								}
								mPlayProgramPos = position;
								mPlayProgramPage = mCurProgramPage;
//								setCurSubProgrammeViaIndex(firstPlayIndex);
								hide();
							}else{
								utils.showToast(mController.getOwner().getApplicationContext(),"该频道还未开始");
								Log.d(TAG,"no playable Schedule");
								return;
							}
						}else{
							if (schedule != null && utils.isPlayable(schedule.getStartTime()) && schedule.getSubscribeStatus()!= Schedule.SUBSCRIBE_STATUS_INVALID) {
								JingxuanChannelProgram program = (JingxuanChannelProgram) view;
								Log.d(TAG,"mSelectChannel : "+ mSelectChannel.getChannelName());
								Log.d(TAG, "schedule : " + schedule.getName());
								if (mController.getPlayingSchedule()
										.equals(schedule)) {// 如果点击的是当前播放的节目，则不重新加载节目播放
									mController.getIOTVMediaPlayer().unpause();
									setPauseIconVisible(false);
								} else {
									mController.iotvPlay(mSelectChannel,schedule);
									Log.d(TAG, "mCurProgramList onItemClick resetMediaControlView");
									mController.resetMediaControlView();
								}
//								if(mChannelProgramAdapter.getChannelPrograms()!=null){
//									mChannelProgramAdapter.getChannelPrograms().get(mPlayProgramPos).setProgressViewVisible(false, false);
//								}
								mPlayProgramPos = position;
								mPlayProgramPage = mCurProgramPage;
//								setCurSubProgrammeViaIndex(0);
								hide();
							}
						}
						/*modify by xubin 2015.11.10 end*/
					}
				});
				
				mCurProgramList.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_LEFT:
								setIsProgramListFocused(false);
								mFocusedView=FOCUSED_CHANNEL;
								mMainFocusAnim.setLockFocusView(mCurIndexList);
								mChannelProgramAdapter.setSelectPosition(0);//仅仅是为了消除第二列的暗的背景
								requestIndexItemFocus(mSelectChannelPos, 0);
								setIsChannelListFocused(true);
								hideSubProgramGroup();
								return true;
							case KeyEvent.KEYCODE_DPAD_RIGHT:
								if (!mSelectChannel.getChannelCode().equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)){
									if((mSubPrograms != null)&&(mSubPrograms.size() > 0)) {
										showDate();
										//调一次 改变背景透明度
									}else{     //如果没有子集数据，清空adapter，焦点到“订阅”
										setIsProgramListFocused(false);
										mSubProgramList.setAdapter(null);
										mSubProgramGroup.setVisibility(View.VISIBLE);
										mSubscribeButton.requestFocus();
										mFocusedView=FOCUSED_SUB;
										mMainFocusAnim.setLockFocusView(mSubscribeButton);
									}
								}
								return true;
							case KeyEvent.KEYCODE_DPAD_UP:
								break;
							case KeyEvent.KEYCODE_DPAD_DOWN:
								break;
							default:
								break;
							}
						}
						return false;
					}
				});
				
				mCurProgramList.setOnFocusChangeListener(new MyListviewOnFocusChangeListener());
			}
		} else {
			mChannelProgramGroup.setVisibility(View.INVISIBLE);
			mNoProgramHit.setVisibility(View.VISIBLE);
		}
	}
	
	private void initSubProgramData() {
		Log.d(TAG, "enter initSubProgramData , mSelectProgramPos = "+mSelectProgramPos);
		/*modify by xubin 2015.11.10 begin*/
		List<Schedule> subScheduleList  = null;//utils.filterPlayableSchedules(mCurPrograms.get(mSelectProgramPos).getSubScheduleList());
		if(mCurPrograms != null && mCurPrograms.size() > mSelectProgramPos){
			subScheduleList = utils.filterPlayableSchedules(mCurPrograms.get(mSelectProgramPos).getSubScheduleList());
		}
		/*modify by xubin 2015.11.10 end*/
		if(subScheduleList != null){
			mTotalSubSchedule = subScheduleList.size();
			// 0开始计数
			mMaxSubProgramPage = mTotalSubSchedule / MAX_SUB_LIST_ITEM_NUM - 1;
			mMaxSubProgramPage = mMaxSubProgramPage < 0 ? 0 : mMaxSubProgramPage;
			if (mTotalSubSchedule > MAX_SUB_LIST_ITEM_NUM
					&& mTotalSubSchedule % MAX_SUB_LIST_ITEM_NUM > 0) {
				mMaxSubProgramPage++;
			}
			Log.d(TAG, "mMaxSubProgramPage : " + mMaxSubProgramPage
					+ " , mCurSubProgramPage :" + mCurSubProgramPage);
		}
		mSubPrograms = subScheduleList;
		mCurSubPrograms = null;
	}
	
	/**
	 * 绑定第3列的数据
	 */
	private void bindSubProgram() {
		Log.d(TAG,
				"enter bindSubProgram : "
						+ mSelectChannel.getChannelName());
		if (mSubPrograms != null && mSubPrograms.size() > 0) {
			int start = 0;
			int end = 0;
			if (mCurSubProgramPage <= mMaxSubProgramPage) {
				start = mCurSubProgramPage * MAX_SUB_LIST_ITEM_NUM;
			}
			if (mCurSubProgramPage < mMaxSubProgramPage) {
				end = (mCurSubProgramPage + 1) * MAX_SUB_LIST_ITEM_NUM;
			} else {
				end = mTotalSubSchedule;
			}
			Log.d(TAG, "start :" + start + ", end :" + end);
			if (end < start) {
				start = end;
				mCurSubPrograms = null;
			} else {
				mCurSubPrograms = new ArrayList<Schedule>(mSubPrograms.subList(start, end));
			}
		}
		mSubProgramGroup.setVisibility(View.VISIBLE);
		if (mCurSubPrograms != null && mCurSubPrograms.size() > 0) {
			Log.d(TAG, "mCurSubPrograms.size : " + mCurSubPrograms.size());
			
				mSubProgramAdapter = new JingxuanSubProgramAdapter(getContext(), mCurSubPrograms);
				
				mSubProgramList.setAdapter(mSubProgramAdapter);
				mSubProgramList.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_LEFT:
								if (mCurProgramList != null && mCurPrograms != null
										&& mCurPrograms.size() > 0) {
									if (mIsDateLoaded) {
										if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
											//mChannelDateGroup
											//		.setVisibility(View.INVISIBLE);
										}
										setIsSubProgramListFocused(false);
										mMainFocusAnim.setLockFocusView(mCurProgramList);
										requestProgramItemFocus(mSelectProgramPos,
												0);
										setIsProgramListFocused(true);
										mFocusedView=FOCUSED_PROGRAM; 
									}
								} else {
									if (mIsDateLoaded) {
										if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
											//mChannelDateGroup
											//		.setVisibility(View.INVISIBLE);
										}
										setIsSubProgramListFocused(false);
										mMainFocusAnim.setLockFocusView(mCurIndexList);
										requestIndexItemFocus(mSelectChannelPos, 0);
										setIsChannelListFocused(true);
										mFocusedView=FOCUSED_CHANNEL;
									}
								}
								return true;
							case KeyEvent.KEYCODE_DPAD_RIGHT:

								return true;
							case KeyEvent.KEYCODE_DPAD_UP:
								if(mCurSubProgramPage == 0 && mSelectSubProgramPos == 0){
									if (mSubProgramAdapter != null) {
										mSubProgramAdapter.resetBackground();
									}
									mSubscribeButton.requestFocus();
									mMainFocusAnim.setLockFocusView(mSubscribeButton);
									return true;
								}
								break;
							case KeyEvent.KEYCODE_DPAD_DOWN:
								break;
							default:
								break;
							}
						}
						return false;
					}
				});
				mSubProgramList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						// TODO Auto-generated method stub
						/*modify by xubin 2015.11.10 begin*/
						Schedule schedule = null;//mCurPrograms.get(mSelectProgramPos);
						if(mCurPrograms != null){
							schedule = mCurPrograms.get(mSelectProgramPos);
						}
						Schedule scheduletemp = null;//(Schedule) mSubProgramAdapter.getItem(position);
						if(mSubProgramAdapter != null){
							scheduletemp = (Schedule) mSubProgramAdapter.getItem(position);
						}
						if (scheduletemp != null && utils.isPlayable(scheduletemp.getStartTime())) {
							JingxuanSubProgram program = (JingxuanSubProgram) view;
							Log.d(TAG,"mSelectChannel : "+ mSelectChannel.getChannelName());
							Log.d(TAG, "schedule : " + scheduletemp.getName());
							if (mController.getPlayingSubSchedule() != null && mController.getPlayingSubSchedule()
									.equals(scheduletemp)) {// 如果点击的是当前播放的节目，则不重新加载节目播放
								mController.getIOTVMediaPlayer()
										.unpause();
								setPauseIconVisible(false);
							} else {
								mController.iotvPlay(mSelectChannel,schedule, scheduletemp.getScheduleIndex());
								Log.d(TAG, "mSubProgramList onItemClick resetMediaControlView");
								mController.resetMediaControlView();
								
//								if(isSubscribedProgram(schedule)){
//									Schedule subscribeSchedule = null;
//									if(schedule.getChannelCode().equals(JXSubscription.CHANNECODE)){
//										subscribeSchedule = schedule;
//									}else{
//										for(Schedule sch:JXSubscription.getInstance().getSubscriptionSchs()){
//											if(sch.getName().equals(schedule.getName())){
//												subscribeSchedule = sch;
//											}
//										}
//									}
//									if(null != subscribeSchedule && null != subscribeSchedule.getSubScheduleList() && subscribeSchedule.getSubScheduleList().size()>0){
//										//子剧集设置为已观看
//										for(Schedule sch:subscribeSchedule.getSubScheduleList()){
//											if(sch.getName().equals(scheduletemp.getName())&&sch.getNamePostfix().equals(scheduletemp.getNamePostfix())){
//												sch.setHasWatched(true);
//												sch.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_NORMAL);
//												break;
//											}
//										}
//									}
//								}
							}
							if(mChannelProgramAdapter.getChannelPrograms()!=null){
								mChannelProgramAdapter.getChannelPrograms().get(mPlayProgramPos).setProgressViewVisible(false, false);
							}
							mPlayProgramPos = mSelectProgramPos;
							mPlayProgramPage = mCurProgramPage;
//							setCurSubProgrammeViaIndex(scheduletemp.getScheduleIndex());
							hide();
						}else{
							utils.showToast(mController.getOwner().getApplicationContext(),"该频道还未开始");
							Log.d(TAG,"no playable Schedule");
						}
						/*modify by xubin 2015.11.10 end*/
					}

				});
				//mSubProgramList.setOnFocusChangeListener(new MyListviewOnFocusChangeListener());
				mSubProgramList.setFocusable(true);
				mMainFocusAnim.selectChange(mSubProgramList, mDateSelectListener);
				mSubProgramList.setFocusAnimation(mMainFocusAnim);
			
		} else {
//			mSubProgramGroup.setVisibility(View.INVISIBLE);
			mSubProgramList.setAdapter(null);
		}
	}
	
	private void refreshProgramArrow() {
		mProgramArrowUp.setVisibility(View.VISIBLE);
		mProgramArrowDown.setVisibility(View.VISIBLE);
//		if(mProgramsFilterByDate.size() > 0){
//			if (mCurProgramPageFilterByDate <= 0) {
//				mProgramArrowUp.setVisibility(View.INVISIBLE);
//			}
//			if (mCurProgramPageFilterByDate >= mMaxProgramPageFilterByDate) {
//				mProgramArrowDown.setVisibility(View.INVISIBLE);
//			}
//		}else{
			if (mCurProgramPage <= 0) {
				mProgramArrowUp.setVisibility(View.INVISIBLE);
			}
			if (mCurProgramPage >= mMaxProgramPage) {
				mProgramArrowDown.setVisibility(View.INVISIBLE);
			}
//		}
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub		
		cancelInvalidSubscription();
		
		if (mMainFocusAnim != null){
		    mMainFocusAnim.setLockFocusView(mLockDrawFcosuObj);
		}
//		mProgramsFilterByDate.clear();
		this.setVisibility(View.INVISIBLE);
		mChannelHold = true;
		if (mMainFocusAnim != null) {
			mMainFocusAnim.clearScale();
		}
	}

	private void cancelInvalidSubscription() {
		Logger.d(TAG, "enter cancelInvalidSubscription");
		List<Schedule> schs;
		JXSubscription jxSubscription = JXSubscription.getInstance();
		if(jxSubscription == null || jxSubscription.getSubscriptionSchs() == null){
			return;
		}
		schs = new ArrayList<Schedule>(jxSubscription.getSubscriptionSchs());
		if(schs != null && schs.size() > 0){
			for(Schedule sh : schs){
				if(sh.getSubscribeStatus() == Schedule.SUBSCRIBE_STATUS_INVALID){
					jxSubscription.cancelSubscribeSchBySchedule(sh);
				}
			}
		}
		jxSubscription.save();
	}

	@Override
	public boolean isshow() {
		// TODO Auto-generated method stub
		return this.getVisibility() == View.VISIBLE;
	}
	
	private View getCurrentFocusDrawView(){
		if (mMainFocusAnim == null){
			return null;
		}
		
		return mMainFocusAnim.getCurDrawView();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d("harish", "onkeyDown = " + keyCode);
		
		View curDrawView = getCurrentFocusDrawView();
	
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			if (curDrawView instanceof JingxuanChannelIndex){
				gotoChannelListPrev(false);
			}else if (curDrawView instanceof JingxuanChannelProgram){
				//focus on channel program list
				gotoChannelParamListPrev(false);
			}else if(curDrawView instanceof JingxuanSubProgram){
				gotoSubProgramListPrev(false);
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			if (curDrawView instanceof JingxuanChannelIndex){
				gotoChannelListNext(false);
			}else if (curDrawView instanceof JingxuanChannelProgram){
				//focus on channel program list
				gotoChannelParamListNext(false);
			}else if(curDrawView instanceof JingxuanSubProgram){
				gotoSubProgramListNext(false);
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			return false;
		}
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mController.getIOTVMediaPlayer().unpause();
			setPauseIconVisible(false);
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//Log.d("harish", "onKeyUp = " + keyCode);
		View curDrawView = getCurrentFocusDrawView();
		
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_PAGE_UP){
			if (curDrawView != null){
				//focus on channel list
				if (curDrawView instanceof JingxuanChannelIndex){
					gotoChannelListNext(true);
				}else if (curDrawView instanceof JingxuanChannelProgram){
					//focus on channel program list
					gotoChannelParamListNext(true);
				}else if(curDrawView instanceof JingxuanSubProgram){
					gotoSubProgramListNext(true);
				}
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_PAGE_DOWN){
			if (curDrawView != null){
				//focus on channel list
				if (curDrawView instanceof JingxuanChannelIndex){
					gotoChannelListPrev(true);
				}else if (curDrawView instanceof JingxuanChannelProgram){
					//focus on channel program list
					gotoChannelParamListPrev(true);
				}else if(curDrawView instanceof JingxuanSubProgram){
					gotoSubProgramListPrev(true);
				}
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			if (curDrawView != null){
				//focus on channel list
				if (curDrawView instanceof JingxuanChannelIndex){
					//gotoChannelListNext(false);
				}else if (curDrawView instanceof JingxuanChannelProgram){
					//focus on channel program list
					//gotoChannelParamListNext(false);
				}
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			return false;
		}

		return super.onKeyUp(keyCode, event);
	}

	private class MyIndexSelectInterface implements SelectInterface {

		@Override
		public void onSelectedEvent(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			Log.d("harish", "MyIndexSelectInterface onSelectedEvent :" + ", position :" + position
					+ ", mSelectChannelPos :" + mSelectChannelPos+" , mChannelHold = "+mChannelHold);

			// if (view != null) {
			// view.setBackgroundResource(R.drawable.iotv_item_focus_bg);
			// ChannelIndex channelIndex = (ChannelIndex) view;
			// channelIndex.setChannelDayVisible(true);
			// }
			if (!mChannelHold) {
				
				mPrevSelectChannelPos = mSelectChannelPos;
				mChannelIndexAdapter.setSelectPosition(position);
				setIsProgramListFocused(false);
				setIsSubProgramListFocused(false);
				
				JingxuanChannelIndex channelIndex =  (JingxuanChannelIndex) mChannelIndexAdapter.getItem(position);
				if (channelIndex.getTag() == null){
					return;
				}
				
				//reset previous channel item as default
				JingxuanChannelIndex prevChannelIndex =  (JingxuanChannelIndex) mChannelIndexAdapter.getItem(mSelectChannelPos);
				mSelectChannel.setLookbackSchedule(null);
				prevChannelIndex.setChannelDay(mSelectChannel.getDay());
				if(((Channel)prevChannelIndex.getTag()).getChannelCode().equals(JXSubscription.CHANNECODE)){
					cancelInvalidSubscription();
				}
				mSelectChannel = (Channel) channelIndex.getTag();
				if(mSelectChannel != null){
					Logger.d(TAG, "...mSelectChannel name = "+mSelectChannel.getChannelName());
				}
				mSelectChannelPos = position;
				mCurProgramPage = 0;
				mSelectProgramPos = 0;
				
				haveUpdateSchedules = haveUpdateSchedules(JXSubscription.getInstance().getSubscriptionSchs());
				mChannelIndexAdapter.updateListContent(mCurChannels, haveUpdateSchedules);
				mHandler.sendEmptyMessageDelayed(MSG_LOAD_PROGRAM,
						DATA_PROGRAM_LOAD_DELAY_TIME);
			}
		}

		@Override
		public void onNothingSelectedEvent() {
			// TODO Auto-generated method stub
		}
	}

	private class MyProgramSelectInterface implements SelectInterface {

		@Override
		public void onSelectedEvent(AdapterView<?> parent, View view,
				int position, long id) {
			Logger.d(TAG, "in MyProgramSelectInterface ,position = "+position);
			String func = "MyProgramSelectInterface.onSelectedEvent.";
			// TODO Auto-generated method stub
			if (mCurPrograms == null) {
				Log.w(TAG, "mCurPrograms is null");
				return;
			}
			mPrevSelectProgramPos = mSelectProgramPos;
			mSelectProgramPos = position;
			if (mSelectProgramPos > mCurPrograms.size()) {
				mSelectProgramPos = 0;
			}
			
			mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
			
			if (mCurProgramList.isFocused()
					&& !mSelectChannel.getChannelCode().equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)) {
				initSubProgramData();
				if(mSelectChannel.getChannelCode().equalsIgnoreCase(mController.getCurrentChannel().getChannelCode())
						&& mSelectProgramPos == mPlayProgramPos && mCurProgramPage == mPlayProgramPage){    // e...... - -||
					setCurSubProgramPos();
					bindSubProgram();
					Message msg = mHandler.obtainMessage(MSG_SET_SUB_PROGRAM_LIST_SELECTION, mSelectSubProgramPos, 0);
					mHandler.sendMessageDelayed(msg, 0);
				}else{
					/*modify by xubin 2015.12.02 for Bug #11947 begin*/	
					if(selectChannelSchedulePlayingSchedulesSame(mSelectProgramPos)
							&& !isSubscribedProgram(mCurPrograms.get(mSelectProgramPos))){
						
						setCurSubProgramPos();
						int selectSubProgramPos = mSelectSubProgramPos;
					    mSelectSubProgramPos = 0;
					    mCurSubProgramPage = 0;
					    bindSubProgram();
						Message msg = mHandler.obtainMessage(MSG_SET_SUB_PROGRAM_LIST_SELECTION, selectSubProgramPos, 0);
						mHandler.sendMessageDelayed(msg, 0);
					}else{
						
						//mMainFocusAnim.setLockFocusView(mCurProgramList);
						mSelectSubProgramPos = 0;
						mCurSubProgramPage = 0;
						bindSubProgram();
					    Message msg = mHandler.obtainMessage(MSG_SET_SUB_PROGRAM_LIST_SELECTION, 0, 0);
					    mHandler.sendMessageDelayed(msg, 0);
					}
					/*modify by xubin 2015.12.02 for Bug #11947 end*/
				}
				
				if(isSubscribedProgram(mCurPrograms.get(mSelectProgramPos))){//如果是已订阅的节目，显示“取消订阅”
					mSubscribeButton.setBackgroundResource(R.drawable.jx_cancel_subscribe);
					
					if(mSelectChannel.getChannelCode().equalsIgnoreCase(Constants.JX_SUBSCRP_CHANNEL_CODE)){
						//当前节目
						Schedule selectedSch=mCurPrograms.get(mSelectProgramPos);
						
						for(Schedule sch:JXSubscription.getInstance().getSubscriptionSchs()){
							if(sch.getName().equals(selectedSch.getName())){
								sch.update();
								break;
							}
						}
					}
				}else{//反之，显示“订阅该节目”
					mSubscribeButton.setBackgroundResource(R.drawable.jx_subscribe);
				}
			} else if(mCurProgramList.isFocused()) {
				hideSubProgramGroup();
			}
		}

		@Override
		public void onNothingSelectedEvent() {
			// TODO Auto-generated method stub
		}
	}

	private class MySubProgramSelectInterface implements SelectInterface {
		@Override
		public void onSelectedEvent(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			String func = "MySubProgramSelectInterface.onSelectedEvent().";
			Log.d(TAG, func + "position:" + position);
			
			mSelectSubProgramPos = position;
			mSubProgramAdapter.setSelectPosition(mSelectSubProgramPos);
		}

		@Override
		public void onNothingSelectedEvent() {
			// TODO Auto-generated method stub
		}
	}

	private void requestIndexItemFocus(int position, int delayTime) {
		requestIndexItemFocus(position, delayTime, false);
	}
	
	private void requestIndexItemFocus(int position, int delayTime, boolean updateProgram) {
		mChannelHold = true;
		mSelectChannelPos = position;
		Log.d("harish", "requestIndexItemFocus select pos = " + position);
		Message msg = mHandler.obtainMessage(MSG_FOCUS_INDEX_LIST, position, 0);
		
		msg.arg2 = 0;
		
		if (updateProgram){
			msg.arg2 = UPDATE_CHANNEL_PAROGRAM_SOON;
		}
		
		mHandler.sendMessageDelayed(msg, delayTime);
	}

	private void requestProgramItemFocus(int position, int delayTime) {
		mSelectProgramPos = position;
		Message msg = mHandler.obtainMessage(MSG_FOCUS_PROGRAM_LIST, position,
				0);
		mHandler.sendMessageDelayed(msg, delayTime);
	}

	private void requestSubProgramItemFocus(int position) {
		Message msg = mHandler.obtainMessage(MSG_FOCUS_SUB_PROGRAM_LIST, position, 0);
		mHandler.sendMessageDelayed(msg, UI_FOCUS_DELAY_TIME);
	}

	private void setIsProgramListFocused(boolean isFocused) {
		if (mChannelProgramAdapter != null) {
			mChannelProgramAdapter.setIsFocus(isFocused);
		}
	}
	
	private void setIsChannelListFocused(boolean isFocused) {
		if (mChannelIndexAdapter != null) {
			mChannelIndexAdapter.setIsFocus(isFocused);
		}
	}

	private void setIsSubProgramListFocused(boolean isFocused) {
		if (mSubProgramAdapter != null) {
			mSubProgramAdapter.setIsFocus(isFocused);
		}
	}

	/**
	 * 设置安全按钮可用否
	 * 
	 * @param focus
	 */
	private void setSafeButton(boolean focus) {
		if (focus) {
			mBtnSafe.setFocusable(true);
			mBtnSafe.requestFocus();
		} else {
			mBtnSafe.setFocusable(false);
		}
	}

	private String getToday() {
		return IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
	}

	/**
	 * yyyyMMdd转化为MM/dd
	 * 
	 * @param strTime
	 * @return
	 */
	private String converTimeNum(String time) {
		String str = "";
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat strFormat = new SimpleDateFormat("MM/dd");
			Date date = dateFormat.parse(time);
			str = strFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 设置当前频道分类位置
	 * 注意：当正在播放的已订阅节目取消订阅后，再次呼出菜单，焦点需要停留在“订阅前”的分类中。
	 */
	private void setCurChannelPos() {
		Logger.d(TAG, "enter setCurChannelPos");
		// 定位当前播放频道
		int index = 1;
		boolean hasChannel = false;
		String chCode = null;
		if(subscriptionCanceled()) {
			chCode = mCurPlaySchedule.getOriginalChannelCode();					   //取原始ChannelCode
//			mCurPlayChannel.setChannelCode(chCode);                                //并设置当前播放Channel为原始Channel
		}
		else
			chCode = mCurPlayChannel.getChannelCode();
		Logger.d(TAG, " , chCode = "+chCode);
		
		for (Channel channel : mChannels) {
			Logger.d(TAG, " , code = "+channel.getChannelCode());
			if (channel.getChannelCode().equalsIgnoreCase(chCode)) {
				hasChannel = true;
				mSelectChannel =  mChannels.get(index - 1);
				break;
			}
			index++;
		}
		if (!hasChannel) {
			index = 0;
		}
		Log.d(TAG, "index : " + index+" , mSelectChannel.name = "+mSelectChannel.getChannelName());
		if (index >= 0) {
			mCurIndexPage = index / MAX_LIST_ITEM_NUM - 1;
			mCurIndexPage = mCurIndexPage < 0 ? 0 : mCurIndexPage;
			if (index > MAX_LIST_ITEM_NUM && index % MAX_LIST_ITEM_NUM > 0) {
				mCurIndexPage++;
			}
			if (mCurIndexPage > mMaxIndexPage) {
				mCurIndexPage = 0;
			} else {
				mSelectChannelPos = (index - 1) % MAX_LIST_ITEM_NUM;
			}
		}
		Log.d(TAG, "mMaxIndexPage : " + mMaxIndexPage + " , mCurIndexPage : "
				+ mCurIndexPage + ", mSelectChannelPos : " + mSelectChannelPos);
	}

	/**
	 * 设置当前频道Schedule位置
	 */
	private void setCurProgramPos() {

		Log.d(TAG, "enter setCurProgramPos");
		/*modify by xubin 2015.11.05 begin*/
		if (mCurPlayChannel.getChannelCode() == Constants.JX_SUBSCRP_CHANNEL_CODE) {//如果当前播放节目在订阅分类
			if ((!isSubscribedProgram(mCurPlaySchedule)								//但已经被取消订阅
					&& (mCurPlaySchedule != null && mCurPlaySchedule.getOriginalChannelCode() != null))) { 		//并且OriginalChannelCode不为空，add
				int index = 1, schIndex = 0;
				Channel ch;
				if (mChannels == null)
					return;

				// 重新定位该频道在原始分类中的序列号
				for (Channel channel : mChannels) {
					if (mCurPlaySchedule != null && channel.getChannelCode().equalsIgnoreCase(mCurPlaySchedule.getOriginalChannelCode())) {
						ch = mChannels.get(index - 1);
						schIndex = findSchduleIndexByName(mCurPlaySchedule, ch);
//						schIndex += 1; // 序列号从1开始计数 ,- -||
						setCurProgrammeViaIndex(schIndex);
						break;
					}
					index++;
				}
			}else{ //如果没有被取消订阅，按照节目名称刷新节目位置
				int indexOfList = 0;
				JXSubscription jxSubscription = JXSubscription.getInstance();
				indexOfList = jxSubscription.getListIndex(jxSubscription.getSubscriptionSchs(), mCurPlaySchedule);
				if (indexOfList < 0){
					mSelectProgramPos = 0;
					mCurProgramPage = 0;
				}else{
					setCurProgrammeViaIndex(indexOfList);
				}
			}
		}
		/*modify by xubin 2015.11.05 end*/
		int currentIndex = mPlayProgramPage * MAX_LIST_ITEM_NUM + mPlayProgramPos;
		if(mPrograms != null && currentIndex < mPrograms.size() - 1 && mCurPlaySchedule != null && !mPrograms.get(currentIndex).equals(mCurPlaySchedule)){
			currentIndex = utils.findSchedule(mPrograms, mCurPlaySchedule);
			setCurProgrammeViaIndex(currentIndex);
		}
		mSelectProgramPos = mPlayProgramPos;
		mCurProgramPage = mPlayProgramPage;
		
		Log.d(TAG, "mMaxProgramPage : " + mMaxProgramPage
				+ " , mCurProgramPage : " + mCurProgramPage
				+ ", mSelectProgramPos : " + mSelectProgramPos);
	}
	
	/**
	 * 设置当前subprogram位置
	 */
	private void setCurSubProgramPos() {
		Log.d(TAG, "enter setCurSubProgramPos");
		if(mSubPrograms != null && mSubPrograms.size() > 0 && mSubPrograms.get(0).getScheduleIndex() != 0 && mController.getPlayingSubSchedule() != null){//第一条的ScheduleIndex不为0，即节目单中有未到时间的节目
			int originalIndex = mController.getPlayingSubSchedule().getScheduleIndex();
			if(originalIndex > 0){
				int nowIndex = originalIndex - mSubPrograms.get(0).getScheduleIndex();
				if(nowIndex >= 0){
					setCurSubProgrammeViaIndex(nowIndex);
				}
			}
		}
		mSelectSubProgramPos = mPlaySubProgramPos;
		mCurSubProgramPage = mPlaySubProgramPage;

		Log.d(TAG, "mMaxSubProgramPage : " + mMaxSubProgramPage
				+ " , mCurSubProgramPage : " + mCurSubProgramPage
				+ ", mSelectSubProgramPos : " + mSelectSubProgramPos);
	}

	private void showDate() {
		String channelName = mSelectChannel.getChannelName();
		Log.d(TAG, "enter showDate , channelName : " + channelName);
		// mChannelName.setText(channelName);
		setIsProgramListFocused(false);
		setIsChannelListFocused(false);
		mSubProgramGroup.setVisibility(View.VISIBLE);
		mFocusedView=FOCUSED_SUB;
		mSubProgramList.requestFocus();
		mMainFocusAnim.setLockFocusView(mSubProgramList);
//		mSelectDatePos = mDates.indexOf(mSelectDate);
		Log.d("Karel", "mSelectDatePos : " + mSelectSubProgramPos);
		mSelectSubProgramPos = mSelectSubProgramPos < 0 ? 0 : mSelectSubProgramPos;
		requestSubProgramItemFocus(mSelectSubProgramPos);
		setIsSubProgramListFocused(true);
	}

	@Override
	public void notifyChange(String day) {
		// TODO Auto-generated method stub
		Log.d(TAG, "enter notifyChange : " + day);
		Log.d(TAG, "mSelectDate : " + mSelectDate);
		if (day != null && day.equalsIgnoreCase(mSelectDate)
				&& mNoProgramHit.getVisibility() == View.VISIBLE) {
			mHandler.sendEmptyMessageDelayed(MSG_LOAD_RECORD,
					DATA_RECORD_LOAD_DELAY_TIME);
		}
	}
	
	private void gotoChannelListNext(boolean switchpage){
		Logger.d(TAG, "enter gotoChannelListNext , switchpage = "+switchpage);
		boolean doneSwitch = false;
		
		if (mCurProgramList != null) {
			mHandler.removeMessages(MSG_LOAD_PROGRAM);
		}
		
		Log.d("harish", "mSelectChannelPos = " + mSelectChannelPos + " mCurIndexPage = " + mCurIndexPage + " mMaxIndexPage = " + mMaxIndexPage);
		
		if (mCurIndexPage < mMaxIndexPage){
			if (mSelectChannelPos == MAX_LIST_ITEM_NUM - 1){
			    doneSwitch = true;
			}
			
			if (switchpage && mCurIndexPage == mMaxIndexPage - 1){
				int maxpos = mTotalChannel % MAX_LIST_ITEM_NUM - 1;
				if(maxpos == -1){
					maxpos = MAX_LIST_ITEM_NUM - 1;
				}
				
				if (mSelectChannelPos > maxpos){
					mSelectChannelPos = maxpos;
				}
			}
		}else if (mCurIndexPage == mMaxIndexPage){
			int maxpos = mTotalChannel % MAX_LIST_ITEM_NUM - 1;
			if(maxpos == -1){
				maxpos = MAX_LIST_ITEM_NUM - 1;
			}
			
			if (mSelectChannelPos == maxpos){
				doneSwitch = true;
			}
		}
		
		if (doneSwitch || switchpage) {
			mCurIndexPage++;
			if (mCurIndexPage > mMaxIndexPage) {
				mCurIndexPage = 0;
			}
			
			Log.d(TAG, "down mCurIndexPage : " + mCurIndexPage + "");
			setSafeButton(true);

			mHandler.removeMessages(MSG_LOAD_PROGRAM);
			bindChannelsIndex(true);
			
			if (switchpage){
				requestIndexItemFocus(mSelectChannelPos, UI_FOCUS_DELAY_TIME, true);
			}else{
				requestIndexItemFocus(0, UI_FOCUS_DELAY_TIME, true);
			}
		}

		mPrevSelectChannelPos = mSelectChannelPos;
	}
	
	private void gotoChannelListPrev(boolean switchpage){
		Logger.d(TAG, "enter gotoChannelListPrev , switchpage = "+switchpage);
		if (mSelectChannelPos == 0 || switchpage){
			mCurIndexPage--;
			if (mCurIndexPage < 0) {
				mCurIndexPage = mMaxIndexPage;
				
				if (switchpage){
					int maxpos = mTotalChannel % MAX_LIST_ITEM_NUM - 1;
					if(maxpos == -1){
						maxpos = MAX_LIST_ITEM_NUM - 1;
					}
					
					if (mSelectChannelPos > maxpos){
						mSelectChannelPos = maxpos;
					}
				}
			}
			Log.d(TAG, "up mCurIndexPage : " + mCurIndexPage);
			setSafeButton(true);

			mHandler.removeMessages(MSG_LOAD_PROGRAM);
			bindChannelsIndex(true);

			int index = mCurChannels.size() - 1;
			index = index < 0 ? 0 : index;
			
			if (switchpage){
				index = mSelectChannelPos;
				requestIndexItemFocus(index, UI_FOCUS_DELAY_TIME, true);
			}else{
				requestIndexItemFocus(index, UI_FOCUS_DELAY_TIME, true);
			}
		}
		
		mPrevSelectChannelPos = mSelectChannelPos;
	}
	
	private void gotoChannelParamListNext(boolean switchpage){
		boolean doneSwitch = false;
			if (mCurProgramPage < mMaxProgramPage){
				if (mSelectProgramPos == MAX_LIST_ITEM_NUM - 1){
	                doneSwitch = true;		
				}
				
				if (switchpage && mCurProgramPage == mMaxProgramPage - 1){
					int maxpos = mTotalSchedule % MAX_LIST_ITEM_NUM - 1;
					
					if (mSelectProgramPos > maxpos){
						mSelectProgramPos = maxpos;
					}
				}
			}else if (mCurProgramPage == mMaxProgramPage){
				int maxpos = mTotalSchedule % MAX_LIST_ITEM_NUM - 1;
				
				if (mSelectProgramPos == maxpos){
			        doneSwitch = true;
				}
			}
			
			if (doneSwitch || switchpage) {
				if (mCurProgramPage < mMaxProgramPage) {
					mCurProgramPage++;
					Log.d(TAG, "down mCurProgramPage : " + mCurProgramPage);
					setSafeButton(true);
					bindChannelsProgram(false);
	
					if (switchpage){
						requestProgramItemFocus(mSelectProgramPos, UI_FOCUS_DELAY_TIME);
					}else{
						requestProgramItemFocus(0, UI_FOCUS_DELAY_TIME);
					}
				}
			}
//		}
		
		mPrevSelectProgramPos = mSelectProgramPos;
	}
	
	private void gotoChannelParamListPrev(boolean switchpage){
			if (mSelectProgramPos == 0 || switchpage){
				if (mCurProgramPage > 0) {						
					mCurProgramPage--;
					Log.d(TAG, "up mCurProgramPage : " + mCurProgramPage);
					setSafeButton(true);
					bindChannelsProgram(false);
					/*modify by xubin 2015.11.10.begin*/
					int program = 0;//mCurPrograms.size() - 1;
					if(mCurPrograms != null){
						program = mCurPrograms.size() - 1;
					}
					/*modify by xubin 2015.11.10.end*/
					program = program < 0 ? 0 : program;
					
					if (switchpage){
						program = mSelectProgramPos;
					}
					
					requestProgramItemFocus(program, UI_FOCUS_DELAY_TIME);
				}
			}
		mPrevSelectProgramPos = mSelectProgramPos;
	}
	private void gotoSubProgramListNext(boolean switchpage){
		boolean doneSwitch = false;
		
		Log.d("harish", "mSelectSubProgramPos = " + mSelectSubProgramPos + " mCurSubProgramPage = " + mCurSubProgramPage + " mMaxSubProgramPage = " + mMaxSubProgramPage);
		
		if (mCurSubProgramPage < mMaxSubProgramPage){
			if (mSelectSubProgramPos == MAX_SUB_LIST_ITEM_NUM - 1){
			    doneSwitch = true;
			}
			
			if (switchpage && mCurSubProgramPage == mMaxSubProgramPage - 1){
				int maxpos = mTotalSubSchedule % MAX_SUB_LIST_ITEM_NUM - 1;
				
				if (mSelectSubProgramPos > maxpos){
					mSelectSubProgramPos = maxpos;
				}
			}
		}else if (mCurSubProgramPage == mMaxSubProgramPage){
			int maxpos = mTotalSubSchedule % MAX_SUB_LIST_ITEM_NUM - 1;
			
			if (mSelectSubProgramPos == maxpos){
				doneSwitch = true;
			}
		}
		
		if (doneSwitch || switchpage) {
			mCurSubProgramPage++;
			if (mCurSubProgramPage > mMaxSubProgramPage) {
				mCurSubProgramPage = 0;
			}
			
			Log.d(TAG, "down mCurSubProgramPage : " + mCurSubProgramPage + "");
			setSafeButton(true);
			bindSubProgram();
			
			if (switchpage){
				requestSubProgramItemFocus(mSelectSubProgramPos);
			}else{
				requestSubProgramItemFocus(0);
			}
		}

		mPrevSelectChannelPos = mSelectChannelPos;
	}
	
	private void gotoSubProgramListPrev(boolean switchpage){
		if (mSelectSubProgramPos == 0 || switchpage){
			mCurSubProgramPage--;
			if (mCurSubProgramPage < 0) {
				mCurSubProgramPage = mMaxSubProgramPage;
				
				if (switchpage){
					int maxpos = mTotalSubSchedule % MAX_SUB_LIST_ITEM_NUM - 1;
					
					if (mSelectSubProgramPos > maxpos){
						mSelectSubProgramPos = maxpos;
					}
				}
			}
			Log.d(TAG, "up mCurSubProgramPage : " + mCurSubProgramPage);
			setSafeButton(true);
			bindSubProgram();
			
			requestSubProgramItemFocus(mSelectSubProgramPos);
		}
		
		mPrevSelectChannelPos = mSelectChannelPos;
	}
	public void setCurProgrammeViaIndex(int index){
		Logger.d(TAG, "enter setCurProgrammeViaIndex , index = "+index);
		if (index >= 0) {
			mPlayProgramPage = (index + 1) / MAX_LIST_ITEM_NUM - 1;
			mPlayProgramPage = mPlayProgramPage < 0 ? 0 : mPlayProgramPage;
			if ((index + 1) > MAX_LIST_ITEM_NUM
					&& (index + 1) % MAX_LIST_ITEM_NUM > 0) {
				mPlayProgramPage++;
			}
			if (mMaxProgramPage > 0 && mPlayProgramPage > mMaxProgramPage) {
				mPlayProgramPage = 0;
				mPlayProgramPos = 0;
			} else {
				mPlayProgramPos = index % MAX_LIST_ITEM_NUM;
			}
		}
	}

	@Override
	public void setPauseIconVisible(boolean visible) {
		// TODO Auto-generated method stub
		if(mPauseIcon != null){
			mPauseIcon.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
		}
	}
	
	public boolean isViewVisible(View v){
		if(v.getVisibility() == View.VISIBLE){
			return true;
		}
		return false;
	}
	
	public void hideSubProgramGroup(){
		mSubProgramGroup.setVisibility(View.INVISIBLE);
	}
	
	

	@Override
	public void setCurSubProgrammeViaIndex(int index) {
		Logger.d(TAG, "enter setCurSubProgrammeViaIndex , index = "+index+" , mMaxSubProgramPage = "+mMaxSubProgramPage);
		if (index >= 0) {
			mPlaySubProgramPage = (index + 1) / MAX_SUB_LIST_ITEM_NUM - 1;
			mPlaySubProgramPage = mPlaySubProgramPage < 0 ? 0 : mPlaySubProgramPage;
			if ((index + 1) > MAX_SUB_LIST_ITEM_NUM
					&& (index + 1) % MAX_SUB_LIST_ITEM_NUM > 0) {
				mPlaySubProgramPage++;
			}
			if (mMaxSubProgramPage > 0 && mPlaySubProgramPage > mMaxSubProgramPage) {
				mPlaySubProgramPage = 0;
				mPlaySubProgramPos = 0;
			} else {
				mPlaySubProgramPos = index % MAX_SUB_LIST_ITEM_NUM;
			}
		}
	}
	
	private class SubscribeButtonFocusInterface implements FocusInterface {

		@Override
		public void onFoucsEvent(View v, boolean hasFoucs) {
			if(mCurPrograms == null || mCurPrograms.size() <= mSelectProgramPos){
				return;
			}
			if(hasFoucs){
				if(isSubscribedProgram(mCurPrograms.get(mSelectProgramPos))){
					v.setBackgroundResource(R.drawable.jx_cancel_subscribe2);
				}else{
					v.setBackgroundResource(R.drawable.jx_subscribe2);
				}
			}else{
				if(isSubscribedProgram(mCurPrograms.get(mSelectProgramPos))){
					v.setBackgroundResource(R.drawable.jx_cancel_subscribe);
				}else{
					v.setBackgroundResource(R.drawable.jx_subscribe);
				}
			}
		}

	}
	
	// 列表项焦点改变监听器
		private class MyListviewOnFocusChangeListener implements OnFocusChangeListener {
			private static final String TAG = "MyGridOnFocusChangeListener";

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				/*
				 * 这段代码使用反射的方式调用GridView的setSelectionInt()方法将Item的选中次数还原到原始状态
				 * 这样可以解决光标从GridView上移开后再移回相同item后onItemSelected()不调用的问题
				 */
				if (hasFocus) {
					Log.d(TAG, "onFocusChange");
					resetListViewSelection(v);
				}
			}
			
			private void resetListViewSelection(View v){
				Log.d(TAG, "enter resetGridViewSelection");
				try {
					@SuppressWarnings("unchecked")
					Class<ListView> c = (Class<ListView>) Class
							.forName("android.widget.ListView");
					Method[] flds = c.getDeclaredMethods();
					for (Method f : flds) {
						if ("setSelectionInt".equals(f.getName())) {
							f.setAccessible(true);
							f.invoke(v, new Object[] { Integer.valueOf(-1) });
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * 检查是否是已订阅节目
		 * @param schedule
		 * @return
		 */
		public boolean isSubscribedProgram(Schedule schedule) {
//			JXSubscription js = JXSubscription.getInstance();
//			List<Schedule> l = js.getSubscriptionSchs();
//			if(schedule == null || l == null){
//				return false;
//			}
//			for(Schedule sc : l){
//				if(sc.equalsForSubscribe(schedule)){
//					return true;
//				}
//			}
			
			JXSubscription js = JXSubscription.getInstance();
			/*add by xubin 2015.11.05 begin*/
			List<Schedule> l = null;//js.getSubscriptionSchs();
			if(js != null){
				l = js.getSubscriptionSchs();
			}
			/*add by xubin 2015.11.05 end*/
			if(schedule == null || l == null){
				return false;
			}
			for(Schedule sch : l){
				if(sch.getName().equals(schedule.getName())){
					return true;
				}
			}
			return false;
		}
		
		
		
		public void showSubscribedToast(Channel channel,Schedule schedule){			
			String channelCode = channel.getChannelName();
	        String scheduleName = schedule.getCompleteName() == null ? schedule.getName():schedule.getCompleteName();

			String str1 = getResources().getString(R.string.jx_subcribed_schedule1);
			String str2 = channelCode + "-" + scheduleName;
			String str3 = getResources().getString(R.string.jx_subcribed_schedule2);


			//设置Toast
			LayoutInflater inflater = LayoutInflater.from(this.getContext());  
			View toastView =  inflater.inflate(R.layout.jx_subcription_toast, null);
			Toast toast =new Toast(this.getContext());
			toast.setView(toastView);
			toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, 0);
			toast.setDuration(Toast.LENGTH_SHORT);
			
			TextView tv1=(TextView)toastView.findViewById(R.id.str1);
			tv1.setText(str1);
			
			TextView tv2=(TextView)toastView.findViewById(R.id.str2);
			tv2.setText(str2);
			
			TextView tv3=(TextView)toastView.findViewById(R.id.str3);
			tv3.setText(str3);

			toast.show();		
		
		}
		

		
		public int findChannelIndexByCode(Channel ch,List<Channel> Chs){
			int index = 0;
			boolean has = false;
			for(Channel c : Chs){
				index ++;
				if(c.getChannelCode().equals(ch.getChannelCode())){
					has = true;
					break;
				}
			}
			
			if(has)
				return index;
			else
				return -1;
		}
		
		
		/**
		 * 通过分类信息和频道信息，获得当前分类中的频道序列号
		 * @param sch 频道信息
		 * @param ch  分类信息
		 * @return    获得当前分类中的频道序列号
		 */
		public int findSchduleIndexByName(Schedule sch,Channel ch){
			int index = 0;
			boolean has = false;
			for(Schedule s : ch.getSchedules()){
				if(sch.getName().equals(s.getName())){
					has = true;
					break;
				}
				index ++;
			}
			
			if(has)
				return index;
			else
				return -1;
		}
		
		/**
		 * 判断订阅是否被取消
		 * 1,如果当前播放节目在订阅分类
		 * 2,但已经被取消订阅
		 * 3,并且OriginalChannelCode不为空，则焦点回到原分类列表中
		 */
		public boolean subscriptionCanceled(){
			/*modify by xubin 2015.11.5 begin*/
			Log.d(TAG, "mCurPlaySchedule:" + mCurPlaySchedule);
			if((mCurPlayChannel != null && mCurPlayChannel.getChannelCode() == Constants.JX_SUBSCRP_CHANNEL_CODE   
					&&(!isSubscribedProgram(mCurPlaySchedule)						       
					&&(mCurPlaySchedule != null) &&(mCurPlaySchedule.getOriginalChannelCode() != null)))			       
				){
				return true;
			}
			/*modify by xubin 2015.11.5 end*/
			return false;
			
		}
		/*add by xubin 2015.12.02 begin*/
		/*判断当前播放的节目名字跟选择频道的节目名字是否一样
		 * */
		private boolean selectChannelSchedulePlayingSchedulesSame(int position){
			
			boolean result = false;
			
			if(mController == null || mCurPrograms == null){
				return result;
			}
			
			if(mController.getPlayingSchedule() == null || position >= mCurPrograms.size()){
				return result;
			}
			
			if(mController.getPlayingSchedule().getName() == null || mCurPrograms.get(position) == null){
				return result;
			}
			
			Logger.d(TAG, "position:" + position + "--Playing name:" + mController.getPlayingSchedule().getName() + "--Select name:" + mCurPrograms.get(position).getName());
									
			if(mController.getPlayingSchedule().getName().equals(mCurPrograms.get(position).getName())){
				
				result = true;
			}
			
			return result;
			
		}				
		/*add by xubin 2015.12.02 end*/
}
