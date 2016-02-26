package com.bestv.ott.jingxuan.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.view.focus.FocusAnimation;
import com.bestv.ott.jingxuan.view.focus.SelectInterface;

public class ChannelListView extends RelativeLayout implements IViewBase {
	// private static final int MSG_UPDATE_CHANNEL_DATE = 100;
	private static final String TAG = "ChannelListView";
	private static final int UI_FOCUS_DELAY_TIME = 100;
	private static final int DATA_PROGRAM_LOAD_DELAY_TIME = 0;
	private static final int DATA_RECORD_LOAD_DELAY_TIME = 0;
	// private static final int DATA_RECORD_LOAD_DELAY_TIME = 500;
	private static final int MSG_FOCUS_INDEX_LIST = 0x3339;
	private static final int MSG_FOCUS_PROGRAM_LIST = 0x4000;
	private static final int MSG_FOCUS_DATE_LIST = 0x4001;
	private static final int MSG_LOAD_PROGRAM = 0x4002;
	private static final int MSG_LOAD_RECORD = 0x4003;
	private static final int MSG_UPDATE_PLAYINFO = 0x4004;
	private static final int MSG_GOTO_CHANNEL_NEXT = 0x4005;
	private static final int MSG_GOTO_CHANNEL_PREV = 0x4006;
	private static final int MSG_GOTO_CHA_PARAM_NEXT = 0x4007;
	private static final int MSG_GOTO_CHA_PARAM_PREV = 0x4008;
	
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
	 * 显示频道or回看
	 */
	private ImageView mChannelType = null;
	/**
	 * 为了界面翻页，焦点切换的效果以及世界和平而存在
	 */
	private ImageButton mBtnSafe = null;
	/**
	 * 日期布局
	 */
	private ViewGroup mChannelDateGroup = null;
	private ViewGroup mChannelProgramGroup = null;
	/**
	 * 日期界面频道名字
	 */
	// private TextView mChannelName = null;
	private TextView mChannelDate = null;
	private TextView mNoProgramHit = null;
	/**
	 * 日期列表
	 */
	private SelfFocusListView mDateList = null;
	// private ImageView mImgHelp = null;
	// private Animation mAnimHelp = null;

	private ChannelIndexAdapter mChannelIndexAdapter;
	private ChannelProgramAdapter mChannelProgramAdapter;
	private ChannelDateAdapter mChannelDateAdapter;
	private List<Channel> mCurChannels;
	private List<Schedule> mCurPrograms;
	private List<String> mDates = null;
	/**
	 * 选中焦点
	 */
	private FrameLayout mMainFocusLayout = null;
	/**
	 * 平移焦点
	 */
	private FocusAnimation mMainFocusAnim = null;
	private MyIndexSelectInterface mIndexSelectListener = null;
	private MyProgramSelectInterface mProgramSelectListener = null;
	private MyDateSelectInterface mDateSelectListener = null;
	private List<Channel> mChannels = null;
	private List<Schedule> mPrograms = null;
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
	
	//use for switch list page 
	private int mPrevSelectChannelPos = -1;
	private int mPrevSelectProgramPos = -1;
	/**
	 * yyyyMMdd
	 */
	private String mSelectDate = "";
	private int mSelectDatePos = 0;
	/**
	 * 第一次进入
	 */
	private boolean mDateRefresh = true;
	/**
	 * 无视所有频道焦点变化引起的数据更新
	 */
	private boolean mChannelHold = true;
	private boolean mIsDateLoaded = true;
	private boolean mIsFirst = true;

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
				refreshIndexArrow();
				mChannelHold = false;
				
				if (msg.arg2 == UPDATE_CHANNEL_PAROGRAM_SOON){
					mCurProgramPage = 0;
					mSelectProgramPos = 0;
					
					ChannelIndex channelIndex =  (ChannelIndex) mChannelIndexAdapter.getItem(mSelectChannelPos);
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
				refreshProgramArrow();
				break;
			case MSG_FOCUS_DATE_LIST:
				int date = msg.arg1;
				mDateList.requestFocus();
				mMainFocusAnim.setLockFocusView(mDateList);
				mDateList.setSelection(date);
				setIsProgramListFocused(false);
				break;
			case MSG_LOAD_PROGRAM:
				Log.d(TAG, "MSG_LOAD_PROGRAM");
				initProgramData();

				setCurProgramPos();

				bindChannelsProgram();
				refreshProgramArrow();
				mSelectProgramPos = 0;
				if (mChannelProgramAdapter != null) {
					mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
				}
				break;
			case MSG_LOAD_RECORD:
				//initChannelsData(mSelectDate);
				//bindChannelsIndex(false);
				mSelectChannel = mCurChannels.get(mSelectChannelPos);
				initProgramData();
				setCurProgramPos();
				bindChannelsProgram();
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
			case MSG_GOTO_CHANNEL_NEXT:
				gotoChannelListNext(false);
				break;
			case MSG_GOTO_CHANNEL_PREV:
				gotoChannelListPrev(false);
				break;
			case MSG_GOTO_CHA_PARAM_NEXT:
				gotoChannelParamListNext(false);
				break;
			case MSG_GOTO_CHA_PARAM_PREV:
				gotoChannelParamListPrev(false);
				break;
			default:
				break;
			}
		}

	};

	public ChannelListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ChannelListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ChannelListView(Context context) {
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
		Logger.d(TAG,"enter initState");
		if (DataConfig.getDataConfigValueL(getContext(), DataConfig.DEFAULT_SHOW_DOWNDETAIL) > 0) {
			mPlayInfoView.setVisibility(View.VISIBLE);
		} else {
			mPlayInfoView.setVisibility(View.GONE);
		}
		
		if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
			mChannelDateGroup.setVisibility(View.INVISIBLE);
		} else {
			mDateRefresh = false;
			//mChannelDateAdapter.setSelection(mSelectDatePos);
			//showDate();
			mChannelDateGroup.setVisibility(View.INVISIBLE);
		}
		
		setChannelType();
		
		bindChannelDates();
		bindChannelsIndex(true);
		
		if (!mIsFirst){
			//mMainFocusAnim.drawFocus((View)mChannelIndexAdapter.getItem(mSelectChannelPos));
			mCurIndexList.requestFocus();
			mCurIndexList.setSelection(mSelectChannelPos);
			mChannelIndexAdapter.setSelectPosition(mSelectChannelPos);
			mMainFocusAnim.setLockFocusView(mCurIndexList);
		}else{
			mChannelIndexAdapter.setSelectPosition(mSelectChannelPos);
			requestIndexItemFocus(mSelectChannelPos, UI_FOCUS_DELAY_TIME);
		}
		
		// 加载节目数据
		mHandler.sendEmptyMessage(MSG_LOAD_PROGRAM);
		// start update player info
		mHandler.sendEmptyMessage(MSG_UPDATE_PLAYINFO);
	}

	private boolean initData() {
		Log.d(TAG, "enter initData");
		boolean flag = true;

		mMaxIndexPage = 0;
		mCurIndexPage = 0;
		mTotalChannel = 0;
		mMaxProgramPage = 0;
		mCurProgramPage = 0;
		mTotalSchedule = 0;
		mSelectChannelPos = 0;
		// mSelectDate = "";
		mSelectDatePos = 0;
		mDates = IOTV_Date.instance().getPrevDays();

		mChannelManager = ChannelManager.getInstance();

		mCurPlaySchedule = mController.getPlayingSchedule();
		if (utils.isNull(mSelectDate) || mCurPlaySchedule == null) {
			mSelectDate = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
		} else {
			Date date = IOTV_Date.getDateParseFull(mCurPlaySchedule
					.getStartTime());
			mSelectDate = IOTV_Date.getDateFormatNormal(date);
		}
		Log.d("Karel", "mSelectDate : " + mSelectDate);
		initChannelsData(mSelectDate);
		
		mCurPlayChannel = mChannels.get(mController.getCurrentChannelIndex());
		if (mCurPlayChannel == null) {
			Log.d(TAG, "mCurPlayChannel is null");
			flag = false;
			return flag;
		}
		
		setCurChannelPos();
		
		Log.d(TAG, "leave initData");
		return flag;
	}

	/**
	 * 加载指定日期的所有平道信息
	 * 
	 * @param date
	 */
	private void initChannelsData(String day) {
		Log.d(TAG, "enter initChannelsData day : " + day);

		mChannels = mController.getCurrentChannelList();
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
		Log.d(TAG, "leave initChannelsData");
	}

	private void initViews() {
		Log.d(TAG, "enter initViews");
		if (mPlayInfoView != null){
			return;
		}
		
		mIndexSelectListener = new MyIndexSelectInterface();
		mProgramSelectListener = new MyProgramSelectInterface();
		mDateSelectListener = new MyDateSelectInterface();
		mPlayInfoView = (TextView) findViewById(R.id.playinfo);
		mChannelDateGroup = (ViewGroup) findViewById(R.id.linear_channel_date);

		mChannelProgramGroup = (ViewGroup) findViewById(R.id.linear_program);
		mNoProgramHit = (TextView) findViewById(R.id.txt_no_program);

		// mChannelName = (TextView) findViewById(R.id.txt_channel_name);
		mChannelDate = (TextView) findViewById(R.id.txt_channel_date);

		mChannelType = (ImageView) findViewById(R.id.img_channel_type);

		mDateList = (SelfFocusListView) findViewById(R.id.channel_date);
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

		mChannelIndexAdapter = new ChannelIndexAdapter(getContext(), mController, mSelectDate);
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
						if (mCurProgramList != null) {
							mChannelDateGroup.setVisibility(View.VISIBLE);
							
							mMainFocusAnim.setLockFocusView(mCurProgramList);
							
							mSelectDate = mSelectChannel.getDay();
							mSelectDatePos = mDates.indexOf(mSelectDate);
							Log.d("Karel", "mSelectDatePos : " + mSelectDatePos);
							mSelectDatePos = mSelectDatePos < 0 ? 0 : mSelectDatePos;
							mDateList.setSelection(mSelectDatePos);
							
							requestProgramItemFocus(mSelectProgramPos, 0);
							setIsProgramListFocused(true);
						}
						if (mCurProgramList == null || mCurPrograms == null
								|| mCurPrograms.size() <= 0) {
							showDate();
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
				
			}
		});

		Log.d(TAG, "leave initViews");
	}

	/**
	 * 绑定当前频道信息
	 */
	private void bindChannelsIndex(boolean needBind) {
	//	Log.d(TAG, "enter bindChannelsIndex , mChannels = "+mChannels);
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
			Log.w(TAG, "start :" + start + ", end :" + end);
			if (end < start) {
				Log.w(TAG, " ..start :" + start + ", end :" + end);
			} else {
				mCurChannels = mChannels.subList(start, end);
				//for (Channel channel : mCurChannels) {
				//	Log.d(TAG, "channelName : " + channel.getChannelName());
				//}
			}
			
			mChannelIndexAdapter.updateListContent(mCurChannels);
		}else{
			Logger.d("mChannels is null");
		}
	}

	private void bindChannelDates() {
		Log.d(TAG, "enter bindChannelDates");
		if (mDates != null) {
			mChannelDateAdapter = new ChannelDateAdapter(getContext(), mDates);
			mDateList.setAdapter(mChannelDateAdapter);
			mDateList.setOnKeyListener(new OnKeyListener() {

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
									
									requestProgramItemFocus(mSelectProgramPos,
											0);
									setIsProgramListFocused(true);
								}
							} else {
								if (mIsDateLoaded) {
									if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
										//mChannelDateGroup
										//		.setVisibility(View.INVISIBLE);
									}

									requestIndexItemFocus(mSelectChannelPos, 0);
								}
							}
							return true;
						case KeyEvent.KEYCODE_DPAD_RIGHT:

							return true;
						case KeyEvent.KEYCODE_DPAD_UP:
							if (mSelectDatePos <= 0) {
								return true;
							}
							mHandler.removeMessages(MSG_LOAD_RECORD);
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (mSelectDatePos >= mDates.size() - 1) {
								return true;
							}
							mHandler.removeMessages(MSG_LOAD_RECORD);
							break;
						default:
							break;
						}
					}
					return false;
				}
			});
			mDateList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					/*if (getToday().equals(mSelectDate)) {
						mChannelDateGroup
								.setVisibility(View.INVISIBLE);
					}
					mDateFocusLayout
							.setVisibility(View.INVISIBLE);

					setChannelType();

					setIndexBtnsFocusable(false);
					setProgramBtnsFocusable(false);
					requestIndexItemFocus(mSelectChannelPos, 0);*/
				}

			});
			
			mDateList.setFocusable(true);
			mMainFocusAnim.selectChange(mDateList, mDateSelectListener);
			mDateList.setFocusAnimation(mMainFocusAnim);
			Log.d(TAG, "leave bindChannelDates");
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
		if (mSelectChannel != null && mSelectChannel.getSchedules() != null
				&& mSelectChannel.getSchedules().size() > 0) {

			mTotalSchedule = mSelectChannel.getSchedules().size();
			// 0开始计数
			mMaxProgramPage = mTotalSchedule / MAX_LIST_ITEM_NUM - 1;
			mMaxProgramPage = mMaxProgramPage < 0 ? 0 : mMaxProgramPage;
			if (mTotalSchedule > MAX_LIST_ITEM_NUM
					&& mTotalSchedule % MAX_LIST_ITEM_NUM > 0) {
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
	}

	private void bindChannelsProgram() {
		Log.d(TAG,
				"enter bindChannelsProgram : "
						+ mSelectChannel.getChannelName());
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
				mCurPrograms = mPrograms.subList(start, end);
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
				mChannelProgramAdapter = new ChannelProgramAdapter(
						getContext(), mCurPrograms, mSelectChannel, mController);
				mCurProgramList.setAdapter(mChannelProgramAdapter);
				mCurProgramList.setFocusable(true);
				mMainFocusAnim.selectChange(mCurProgramList, mProgramSelectListener);
				mCurProgramList.setFocusAnimation(mMainFocusAnim);
				
				mCurProgramList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						Schedule schedule = (Schedule) mChannelProgramAdapter
								.getItem(position);
						if (isPlayable(schedule.getStartTime())) {
							ChannelProgram program = (ChannelProgram) view;
							Log.d(TAG,
									"mSelectChannel : "
											+ mSelectChannel.getChannelName());
							Log.d(TAG, "schedule : " + schedule.getName());
							if (program.getProgramStatus() == ChannelProgram.STATUS_LIVE) {
								Log.d(TAG, "Goto Live");
								mController.iotvPlay(mSelectChannel, null);
							} else {
								mController.iotvPlay(mSelectChannel, schedule);
							}

							hide();
						}
					}
				});
				
				mCurProgramList.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_LEFT:
								mChannelDateGroup.setVisibility(View.INVISIBLE);
								setIsProgramListFocused(false);
								requestIndexItemFocus(mSelectChannelPos, 0);
								return true;
							case KeyEvent.KEYCODE_DPAD_RIGHT:
								showDate();
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
			}
		} else {
			mChannelProgramGroup.setVisibility(View.INVISIBLE);
			mNoProgramHit.setVisibility(View.VISIBLE);
		}
	}

	private void refreshProgramArrow() {
		mProgramArrowUp.setVisibility(View.VISIBLE);
		mProgramArrowDown.setVisibility(View.VISIBLE);
		if (mCurProgramPage <= 0) {
			mProgramArrowUp.setVisibility(View.INVISIBLE);
		}
		if (mCurProgramPage >= mMaxProgramPage) {
			mProgramArrowDown.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		if (mMainFocusAnim != null){
		    mMainFocusAnim.setLockFocusView(mLockDrawFcosuObj);
		}
		
		this.setVisibility(View.INVISIBLE);
		mChannelHold = true;
		if (mMainFocusAnim != null) {
			mMainFocusAnim.clearScale();
		}
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
			if (curDrawView instanceof ChannelIndex){
				gotoChannelListPrev(false);
			}else if (curDrawView instanceof ChannelProgram){
				//focus on channel program list
				gotoChannelParamListPrev(false);
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			if (curDrawView instanceof ChannelIndex){
				gotoChannelListNext(false);
			}else if (curDrawView instanceof ChannelProgram){
				//focus on channel program list
				gotoChannelParamListNext(false);
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			return false;
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
				if (curDrawView instanceof ChannelIndex){
					gotoChannelListNext(true);
				}else if (curDrawView instanceof ChannelProgram){
					//focus on channel program list
					gotoChannelParamListNext(true);
				}
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_PAGE_DOWN){
			if (curDrawView != null){
				//focus on channel list
				if (curDrawView instanceof ChannelIndex){
					gotoChannelListPrev(true);
				}else if (curDrawView instanceof ChannelProgram){
					//focus on channel program list
					gotoChannelParamListPrev(true);
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
				if (curDrawView instanceof ChannelIndex){
					//gotoChannelListNext(false);
				}else if (curDrawView instanceof ChannelProgram){
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
					+ ", mSelectChannelPos :" + mSelectChannelPos);

			// if (view != null) {
			// view.setBackgroundResource(R.drawable.iotv_item_focus_bg);
			// ChannelIndex channelIndex = (ChannelIndex) view;
			// channelIndex.setChannelDayVisible(true);
			// }
			if (!mChannelHold) {
				mPrevSelectChannelPos = mSelectChannelPos;
				mChannelIndexAdapter.setSelectPosition(position);
				setIsProgramListFocused(false);
				
				ChannelIndex channelIndex =  (ChannelIndex) mChannelIndexAdapter.getItem(position);
				if (channelIndex.getTag() == null){
					return;
				}
				
				//reset previous channel item as default
				ChannelIndex prevChannelIndex =  (ChannelIndex) mChannelIndexAdapter.getItem(mSelectChannelPos);
				mSelectChannel.setLookbackSchedule(null);
				prevChannelIndex.setChannelDay(mSelectChannel.getDay());
				
				mSelectChannel = (Channel) channelIndex.getTag();
				mSelectChannelPos = position;
				mCurProgramPage = 0;
				mSelectProgramPos = 0;
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
			// TODO Auto-generated method stub
			if (mCurPrograms == null) {
				Log.w(TAG, "mCurPrograms is null");
				return;
			}
			
			mPrevSelectProgramPos = mSelectProgramPos;
			mSelectProgramPos = position;
			// setIsProgramListFocused(true);
			if (mSelectProgramPos > mCurPrograms.size()) {
				mSelectProgramPos = 0;
			}
			mChannelProgramAdapter.setSelectPosition(mSelectProgramPos);
		}

		@Override
		public void onNothingSelectedEvent() {
			// TODO Auto-generated method stub
		}
	}

	private class MyDateSelectInterface implements SelectInterface {
		@Override
		public void onSelectedEvent(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			Log.d(TAG, "mDateRefresh : " + mDateRefresh);
			mChannelDateAdapter.resetDateType();
			if (view != null) {
				TextView txtDateStr = (TextView) view
						.findViewById(R.id.channel_date_str);
				TextView txtDateNum = (TextView) view
						.findViewById(R.id.channel_date_num);
				txtDateStr.setTextColor(getContext().getResources().getColor(
						android.R.color.black));
				txtDateNum.setTextColor(getContext().getResources().getColor(
						android.R.color.black));
				view.setBackgroundColor(getContext().getResources().getColor(
						android.R.color.white));
			}
			if (mChannelDateGroup != null
					&& mChannelDateGroup.getVisibility() == View.VISIBLE && mDateRefresh && mSelectDatePos != position) {
				mSelectDatePos = position;
				mSelectDate = mChannelDateAdapter.getItem(position).toString();

				List<Channel> channellist = mController.getChannelList(mSelectDate);
				if (channellist != null){
					Channel channel = channellist.get(mCurIndexPage * MAX_LIST_ITEM_NUM + mSelectChannelPos);
					List<Schedule> sl = channel.getSchedules();
					
					if (sl != null && sl.size() > 0){
						//Log.d("harish", "set look back schedule = " + mSelectDate + " size = " + sl.size());
						mSelectChannel.setLookbackSchedule(sl);
					}else{
						//Log.d("harish", "set empty look back schedule = " + mSelectDate);
						mSelectChannel.setLookbackSchedule(new ArrayList<Schedule>());
					}
				}
				
				ChannelIndex curObj = (ChannelIndex) mChannelIndexAdapter.getItem(mSelectChannelPos);
				curObj.setChannelDay(mSelectDate);
				
				//setChannelType();
				
				mIsDateLoaded = false;
				mHandler.sendEmptyMessageDelayed(MSG_LOAD_RECORD,
						DATA_RECORD_LOAD_DELAY_TIME);
			}
			mDateRefresh = true;
		}

		@Override
		public void onNothingSelectedEvent() {
			// TODO Auto-generated method stub
		}
	}

	/*private class MyFocusInterface implements FocusInterface {
		@Override
		public void onFoucsEvent(View v, boolean hasFoucs) {
			// TODO Auto-generated method stub
			if (hasFoucs) {
				int id = v.getId();
				if (id == R.id.next_channel_indexs) {
					// if (mCurIndexPage < mMaxIndexPage) {

				} else if (id == R.id.prev_channel_indexs) {
					// if (mCurIndexPage > 0) {

				} else if (id == R.id.next_channel_programs) {
					if (mCurProgramPage < mMaxProgramPage) {						
						mCurProgramPage++;
						Log.d(TAG, "down mCurProgramPage : " + mCurProgramPage);
						setSafeButton(true);
						bindChannelsProgram();
						mProgramFlipper.showNext();
						requestProgramItemFocus(0, UI_FOCUS_DELAY_TIME);
					}
				} else if (id == R.id.prev_channel_programs) {
					if (mCurProgramPage > 0) {						
						mCurProgramPage--;
						Log.d(TAG, "up mCurProgramPage : " + mCurProgramPage);
						setSafeButton(true);
						bindChannelsProgram();
						mProgramFlipper.showPrevious();
						int program = mCurPrograms.size() - 1;
						program = program < 0 ? 0 : program;
						requestProgramItemFocus(program, UI_FOCUS_DELAY_TIME);
					}
				} else {
				}
			}
		}
	}*/

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

	private void requestDateItemFocus(int position) {
		Message msg = mHandler.obtainMessage(MSG_FOCUS_DATE_LIST, position, 0);
		mHandler.sendMessageDelayed(msg, UI_FOCUS_DELAY_TIME);
	}

	private void setIsProgramListFocused(boolean isFocused) {
		if (mChannelProgramAdapter != null) {
			mChannelProgramAdapter.setIsFocus(isFocused);
		}
	}

	private boolean isPlayable(String startTime) {
		boolean flag = false;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date date = dateFormat.parse(startTime);
			Date curDate = IOTV_Date.getDate();
			flag = date.before(curDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		}
		return flag;
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
	 * 设置当前频道位置
	 */
	private void setCurChannelPos() {
		// 定位当前播放平道
		int index = 1;
		boolean hasChannel = false;
		for (Channel channel : mChannels) {
			if (channel.getChannelNo().equalsIgnoreCase(
					mCurPlayChannel.getChannelNo())) {
				mSelectChannel = mCurPlayChannel;
				hasChannel = true;
				break;
			}
			index++;
		}
		if (!hasChannel) {
			index = 0;
		}
		Log.d(TAG, "index : " + index);
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
	 * 设置当前频道位置
	 */
	private void setCurProgramPos() {
		Log.d(TAG, "enter setCurProgramPos");

		boolean isToadyProgram = true;
		if (mController.getPlayingSchedule() != null) {
			Log.d("Karel", "AAAAAA : "
					+ mController.getPlayingSchedule().getStartTime());
			Log.d("Karel", "BBBBBB : " + getToday());
			isToadyProgram = mController.getPlayingSchedule().getStartTime()
					.startsWith(getToday());
		}
		// 是否定位到当前直播节目
		boolean isShowLive = true;
		
		if (mCurPlayChannel.getChannelName().equals(mSelectChannel.getChannelName())){
			isShowLive = (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE)
			|| (getToday().equalsIgnoreCase(mSelectDate) && (!mSelectChannel
					.getChannelNo().equals(
							mController.getCurrentChannel().getChannelNo()) || !isToadyProgram));
		}
		
		if (!isShowLive) {
			Log.d(TAG, "Record");
			// 如果非直播
			Channel playChannel = mController.getCurrentChannel();
			if (playChannel.getChannelNo()
					.equals(mSelectChannel.getChannelNo())) {

				List<Schedule> schedules = mSelectChannel.getSchedules();
				int index = 1;
				boolean hasSchedule = false;
				for (Schedule schedule : schedules) {
					if (mCurPlaySchedule.getStartTime().equalsIgnoreCase(
							schedule.getStartTime())) {
						hasSchedule = true;
						break;
					}
					index++;
				}
				if (!hasSchedule) {
					index = 0;
				}
				Log.d(TAG, "index : " + index);
				if (index >= 0) {
					mCurProgramPage = index / MAX_LIST_ITEM_NUM - 1;
					mCurProgramPage = mCurProgramPage < 0 ? 0 : mCurProgramPage;
					if (index > MAX_LIST_ITEM_NUM
							&& index % MAX_LIST_ITEM_NUM > 0) {
						mCurProgramPage++;
					}
					if (mCurProgramPage > mMaxProgramPage) {
						mCurProgramPage = 0;
					} else {
						mSelectProgramPos = (index - 1) % MAX_LIST_ITEM_NUM;
					}
				}

			} else {
				mCurProgramPage = 0;
			}
		} else {
			// 如果是直播
			Log.d(TAG, "Live");
			Schedule liveSchedule = mChannelManager.getCurrentSchedule(
					mSelectChannel).getSchedule();
			Log.d("Karel", "liveSchedule : " + liveSchedule);
			if (liveSchedule == null) {
				mCurProgramPage = 0;
				return;
			}
			Log.d("Karel", "liveSchedule : " + liveSchedule.getName());
			int index = 1;
			List<Schedule> schedules = mSelectChannel.getSchedules();
			//Log.d(TAG, "schedules : " + schedules);
			boolean hasSchedule = false;
			for (Schedule schedule : schedules) {
				if (liveSchedule.getStartTime().equalsIgnoreCase(
						schedule.getStartTime())) {
					hasSchedule = true;
					break;
				}
				index++;
			}
			if (!hasSchedule) {
				index = 0;
			}
			Log.d(TAG, "index : " + index);
			if (index >= 0) {
				mCurProgramPage = index / MAX_LIST_ITEM_NUM - 1;
				mCurProgramPage = mCurProgramPage < 0 ? 0 : mCurProgramPage;
				if (index > MAX_LIST_ITEM_NUM && index % MAX_LIST_ITEM_NUM > 0) {
					mCurProgramPage++;
				}
				if (mCurProgramPage > mMaxProgramPage) {
					mCurProgramPage = 0;
				} else {
					mSelectProgramPos = (index - 1) % MAX_LIST_ITEM_NUM;
				}
			}
		}

		Log.d(TAG, "mMaxProgramPage : " + mMaxProgramPage
				+ " , mCurProgramPage : " + mCurIndexPage
				+ ", mSelectProgramPos : " + mSelectProgramPos);
	}

	private void setChannelType() {
		if (!getToday().equalsIgnoreCase(mSelectDate)
				|| mController.getCurrentIOTVType() == Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE) {
			mChannelType.setBackgroundResource(R.drawable.jx_iotv_lookback);
			mChannelDate.setText(converTimeNum(mSelectDate));
		} else {
			mChannelType.setBackgroundResource(R.drawable.jx_iotv_channel);
			mChannelDate.setText("");
		}
	}

	private void showDate() {
		String channelName = mSelectChannel.getChannelName();
		Log.d(TAG, "channelName : " + channelName);
		// mChannelName.setText(channelName);
		mChannelDateGroup.setVisibility(View.VISIBLE);
		mDateList.requestFocus();
		mSelectDatePos = mDates.indexOf(mSelectDate);
		Log.d("Karel", "mSelectDatePos : " + mSelectDatePos);
		mSelectDatePos = mSelectDatePos < 0 ? 0 : mSelectDatePos;
		requestDateItemFocus(mSelectDatePos);
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
		Logger.d(TAG, "enter gotoChannelListNext");
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
				
				if (mSelectChannelPos > maxpos){
					mSelectChannelPos = maxpos;
				}
			}
		}else if (mCurIndexPage == mMaxIndexPage){
			int maxpos = mTotalChannel % MAX_LIST_ITEM_NUM - 1;
			if (mSelectChannelPos == maxpos || mSelectChannelPos == (MAX_LIST_ITEM_NUM - 1)){
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
				requestIndexItemFocus(0, UI_FOCUS_DELAY_TIME);
			}
		}

		mPrevSelectChannelPos = mSelectChannelPos;
	}
	
	private void gotoChannelListPrev(boolean switchpage){
		Logger.d(TAG, "enter gotoChannelListPrev boolean");
		if (mSelectChannelPos == 0 || switchpage){
			mCurIndexPage--;
			if (mCurIndexPage < 0) {
				mCurIndexPage = mMaxIndexPage;
				
				if (switchpage){
					int maxpos = mTotalChannel % MAX_LIST_ITEM_NUM - 1;
					
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
				requestIndexItemFocus(index, UI_FOCUS_DELAY_TIME);
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
				bindChannelsProgram();

				if (switchpage){
					requestProgramItemFocus(mSelectProgramPos, UI_FOCUS_DELAY_TIME);
				}else{
					requestProgramItemFocus(0, UI_FOCUS_DELAY_TIME);
				}
			}
		}
		
		mPrevSelectProgramPos = mSelectProgramPos;
	}
	
	private void gotoChannelParamListPrev(boolean switchpage){
		if (mSelectProgramPos == 0 || switchpage){
			if (mCurProgramPage > 0) {						
				mCurProgramPage--;
				Log.d(TAG, "up mCurProgramPage : " + mCurProgramPage);
				setSafeButton(true);
				bindChannelsProgram();

				int program = mCurPrograms.size() - 1;
				program = program < 0 ? 0 : program;
				
				if (switchpage){
					program = mSelectProgramPos;
				}
				
				requestProgramItemFocus(program, UI_FOCUS_DELAY_TIME);
			}
		}
		
		mPrevSelectProgramPos = mSelectProgramPos;
	}

	@Override
	public void setCurProgrammeViaIndex(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPauseIconVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurSubProgrammeViaIndex(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFirstSelectSubProgramPos(int position) {
		// TODO Auto-generated method stub
		
	}
}
