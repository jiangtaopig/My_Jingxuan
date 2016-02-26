package com.bestv.ott.jingxuan.view;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.ChannelIconMgr;
import com.bestv.ott.jingxuan.iotvlogservice.utils.FileUtils;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.view.focus.FocusAnimation;
import com.bestv.ott.jingxuan.view.focus.FocusInterface;
import com.bestv.ott.jingxuan.view.focus.SelectInterface;
import com.bestv.jingxuan.R;
import com.bestv.ott.framework.utils.utils;

public class PlayCompleteView extends FrameLayout implements IViewBase {
	private static final String TAG = "PlayCompleteView";
	private static final int MSG_RESET_FOCUS = 0x6642;
	private static final int MSG_REFOCUS_VIEW = 0x6643;
	/**
	 * 推荐最多数量
	 */
	private static final int RECOMMEND_MAX_NUM = 3;
	private IControllerBase mController = null;
	private ImageButton mBtnChannelList = null;
	private ImageView mImgPlayType = null;
	private ImageView mImgChannelListHit = null;
	private ChannelView mChannelLive = null;
	private GridView mGridRecommend = null;
	private ChannelTitle mChannelTitle = null;
	/**
	 * 选中焦点
	 */
	private FrameLayout mFocusLayout = null;
	/**
	 * 平移焦点
	 */
	private FocusAnimation mFocusAnim = null;
	private HorizontalScrollView mHorizScrollRecommend = null;
	private MyFocusInterface mFocusListener = null;
	private MySelectInterface mSelectInterface = null;
	private RecommendAdapter mRecommendAdapter = null;
	private Channel mCurChannel = null;
	private List<Channel> mRecommendChannels = null;
	private MyOnClickListener mOnClickListener = null;
	private MyOnTitleProgressFinishListener mOnFinishListener = null;
	private MyOnItemClickListener mOnItemClickListener = null;
	private MyGridOnFocusChangeListener mGridOnFocusChangeListener = null;
	private MyOnProgramProgressFinishListener mProgressFinishListener = null;
	private ChannelManager mChannelManager = null;
	private boolean mInitFinish = false;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_RESET_FOCUS:
				setViewsFocusable(true);
				mChannelLive.requestFocus();
				mFocusLayout.setVisibility(View.VISIBLE);
				mInitFinish = true;
				break;
			case MSG_REFOCUS_VIEW:
				if (msg.obj != null && msg.obj instanceof View) {
					View v = (View) msg.obj;
					mFocusAnim.drawFocus(v);
				}
				setViewsFocusable(true);
				mFocusLayout.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	};

	public PlayCompleteView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PlayCompleteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PlayCompleteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setDataController(IControllerBase c) {
		// TODO Auto-generated method stub
		mController = c;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		mInitFinish = false;
		this.setVisibility(View.VISIBLE);
		Log.d(TAG, "enter show");
		initData();
		initViews();
	}

	private void initData() {
		mChannelManager = ChannelManager.getInstance();
		mCurChannel = mController.getCurrentChannel();
		mRecommendChannels = mController.getPlayRecords();
		mRecommendAdapter = new RecommendAdapter(getContext(),
				mRecommendChannels, mController);
		
		if (mOnClickListener == null) {
			mOnClickListener = new MyOnClickListener();
			mOnFinishListener = new MyOnTitleProgressFinishListener();
			mOnItemClickListener = new MyOnItemClickListener();
			mFocusListener = new MyFocusInterface();
			mSelectInterface = new MySelectInterface();
			mGridOnFocusChangeListener = new MyGridOnFocusChangeListener();
		}
	}

	private void initViews() {
		mChannelTitle = (ChannelTitle) findViewById(R.id.channe_title);
		initTitle();
		mImgPlayType = (ImageView)findViewById(R.id.img_play_type);
		initPlayType();
		mBtnChannelList = (ImageButton) findViewById(R.id.btn_channel_list);
		mBtnChannelList.setOnClickListener(mOnClickListener);
		mChannelLive = (ChannelView) findViewById(R.id.channel_live);
		mChannelLive.setOnClickListener(mOnClickListener);
		bindLive();
		mImgChannelListHit = (ImageView) findViewById(R.id.img_channel_list_hit);
		if(OttContextMgr.getInstance().isIOTVRunning() && mImgChannelListHit != null){
			mImgChannelListHit.setBackgroundResource(R.drawable.jx_iotv_channel_list_hit_menu);
		}
		mHorizScrollRecommend = (HorizontalScrollView) findViewById(R.id.horiz_scroll_recommend);
		mGridRecommend = (GridView) findViewById(R.id.grid_recommend);
		mGridRecommend.setAdapter(mRecommendAdapter);
		mGridRecommend.setNumColumns(mRecommendChannels.size());
		int width = (int) (getResources().getDimension(
				R.dimen.jx_channel_view_width) + getResources().getDimension(
				R.dimen.jx_oper_item_spacing))
				* mRecommendChannels.size();
		int height = (int) (getResources()
				.getDimension(R.dimen.jx_channel_view_height));
		LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
				width, height);
		mFocusLayout = (FrameLayout) findViewById(R.id.frame_focus);
		mFocusAnim = new FocusAnimation(getContext(), mFocusLayout);

		mGridRecommend.setLayoutParams(layoutparams);
		mGridRecommend.setOnItemClickListener(mOnItemClickListener);
		mGridRecommend.setOnFocusChangeListener(mGridOnFocusChangeListener);
		
		mGridRecommend.setSelection(1);
		mFocusLayout.setVisibility(View.INVISIBLE);

		mFocusAnim.foucsChange(mChannelLive);
		mFocusAnim.foucsChange(mBtnChannelList);
		mFocusAnim.setFocusListener(mFocusListener);
		mFocusAnim.setOffset((int) getContext().getResources().getDimension(
				R.dimen.jx_tv_focus_offset));
		mFocusAnim.selectChange(mGridRecommend, mSelectInterface);

		mHandler.sendEmptyMessageDelayed(MSG_RESET_FOCUS, 500);
	}

	private void initTitle() {
		Log.d(TAG, "enter initTitle");
		if (mCurChannel == null) {
			Log.w(TAG, "mCurChannel is null");
			return;
		}
		mChannelTitle.setChannelNum(Integer.valueOf(mCurChannel.getChannelNo()));
		mChannelTitle.setChannelName(mCurChannel.getChannelName());
		mChannelTitle.setChannelIcon(ChannelIconMgr.getChannelIcon(mCurChannel.getChannelName(), mCurChannel.getIcon1()));
		mChannelTitle.setOnTitleProgressFinishListener(mOnFinishListener);
//		ChannelData curChannelData = ChannelManager.getInstance().getCurrentSchedule(mCurChannel);
		ChannelData curChannelData = ChannelManager.getInstance().getSchedule(mCurChannel, mController.getCurrentPlayingTime());
		if (curChannelData == null) {
			Log.w(TAG, "curChannelData is null");
			return;
		}
		String iconPath = curChannelData.getChannelIconPath();
		if (utils.isNotNull(iconPath) && FileUtils.fileExisted(iconPath)) {
			Bitmap icon = BitmapFactory.decodeFile(iconPath);
			if (icon != null) {
				mChannelTitle.setChannelIcon(icon);
			}
		}
		//long startTime = curChannelData.getStartTime();
		//long endTime = curChannelData.getEndTime();
		//long curTime = curChannelData.getCurTime();
		//mChannelTitle.setProgressData(startTime, endTime, curTime);
		//String startTimeStr = utils
		//		.safeString(curChannelData.getStartTimeStr());
		//String scheduleName = utils
		//		.safeString(curChannelData.getScheduleName());
		mChannelTitle.setCurProgram(this.getContext().getResources().getString(R.string.next_prog_str));

//		ChannelData nxtChannelData = ChannelManager.getInstance().getNextSchedule(mCurChannel);
		ChannelData nxtChannelData = ChannelManager.getInstance().getNextSchedule(mCurChannel, mController.getCurrentPlayingTime());
		if (nxtChannelData == null) {
			Log.w(TAG, "nxtChannelData is null");
			return;
		}
		String startTimeStr = nxtChannelData.getStartTimeStr();
		String scheduleName = nxtChannelData.getScheduleName();
		if (startTimeStr != null && scheduleName != null){
		    mChannelTitle.setNextProgram(startTimeStr + " " + scheduleName);
		}
		mChannelTitle.mNextProgram.setTextColor(Color.WHITE);
	}

	private void bindLive() {
		Log.d(TAG, "enter initLive");
		if (mChannelLive == null) {
			Log.d(TAG, "mChannelLive is null");
			return;
		}
		
		Date today = IOTV_Date.getDate();
		Channel live = ChannelManager.getChannelByCode(mController.getChannelList(IOTV_Date.dateFormat.format(today)), mCurChannel.getChannelCode());
		
		if (live == null){
			return;
		}
	
		ChannelData channelData = ChannelManager.getInstance().getCurrentSchedule(live);
		mChannelLive.setChannelIcon(ChannelIconMgr.getChannelIcon(channelData.getChannelName(), channelData.getChannelIconPath()));

		long curTime = channelData.getCurTime();
		long endTime = channelData.getEndTime();
		long startTime = channelData.getStartTime();
		String startTimeStr = channelData.getStartTimeStr();
		String endTimeStr = channelData.getEndTimeStr();
		mChannelLive.setProgramName(utils.safeString(channelData.getScheduleName()));
		mChannelLive.setTag(live);
		mChannelLive.setChannelName(live.getChannelName());
		mChannelLive.setOnProgramProgressFinishListener(mProgressFinishListener);
	}

	private void initPlayType(){
		if(mImgPlayType != null && mController != null){
			int type = mController.getCurrentIOTVType();
			switch (type) {
			case Constants.IOTV_PLAY_LIVE_SOURCE:
				mImgPlayType.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
				break;
			case Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE:
				mImgPlayType.setBackgroundResource(R.drawable.jx_iotv_lookback_prompt_img);
				break;
				
			default:
				break;
			}
		}
	}
	
	@Override
	public void hide() {
		// TODO Auto-generated method stub
		mInitFinish = false;
		if (!isshow()){
			return;
		}
		
		this.setVisibility(View.INVISIBLE);
		if (mRecommendAdapter != null) {
			mRecommendAdapter.releaseData();
		}
		if (mChannelTitle != null) {
			mChannelTitle.releaseData();
		}
	}
	
	@Override
	public void reset() {
		
	}

	@Override
	public boolean isshow() {
		// TODO Auto-generated method stub
		return getVisibility() == VISIBLE;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return this;
	}

	private class MyOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (!mInitFinish){
				return;
			}
			
			int id = v.getId();
			if (id == R.id.channel_live) {
				mController.iotvPlay(mCurChannel, null);
				hide();
			} else if (id == R.id.btn_channel_list) {
				mController.gotoChannelList();
				hide();
			} else {
			}
		}
	}

	private class MyOnTitleProgressFinishListener implements
			OnTitleProgressFinishListener {

		@Override
		public void onFinish(ChannelTitle channelTitle) {
			// TODO Auto-generated method stub
			initTitle();
		}
	}

	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if(parent.getAdapter() instanceof RecommendAdapter){
				RecommendAdapter adapter = (RecommendAdapter) parent.getAdapter();
				Channel channel = (Channel)adapter.getItem(position);
				if (channel == null || utils.isNull(channel.getChannelCode())){
					return;
				}
				
				mController.iotvPlay(channel, mChannelManager.getCurrentSchedule(channel).getSchedule());
				hide();
			}
		}

	}

	private class MyFocusInterface implements FocusInterface {

		@Override
		public void onFoucsEvent(View v, boolean hasFoucs) {
			// TODO Auto-generated method stub

		}

	}

	private class MyOnProgramProgressFinishListener implements
			OnProgramProgressFinishListener {

		@Override
		public void onFinish(ChannelView channelView) {
			// TODO Auto-generated method stub
			bindLive();
		}

	}

	// 列表项焦点改变监听器
	private class MyGridOnFocusChangeListener implements OnFocusChangeListener {
		private static final String TAG = "MyGridOnFocusChangeListener";

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			/*
			 * 这段代码使用反射的方式调用GridView的setSelectionInt()方法将Item的选中次数还原到原始状态
			 * 这样可以解决光标从GridView上移开后再移回相同item后onItemSelected()不调用的问题
			 */
			if (!hasFocus) {
				Log.d(TAG, "onFocusChange");
				resetGridViewSelection(v);
			}
		}
	}

	private class MySelectInterface implements SelectInterface {
		/**
		 * 是否需要等待滑动完成后再画
		 */
		private boolean needDrawFocus = false;

		@Override
		public void onSelectedEvent(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onSelectedEvent view:" + view + ", needDrawFocus : " + needDrawFocus);
			if (needDrawFocus) {
				if (position >= RECOMMEND_MAX_NUM - 1) {
					mHorizScrollRecommend
							.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
				} else if (position <= 0) {
					mHorizScrollRecommend
							.fullScroll(HorizontalScrollView.FOCUS_LEFT);
				}
				mFocusLayout.setVisibility(View.INVISIBLE);
				Message msg = mHandler.obtainMessage(MSG_REFOCUS_VIEW, view);
				mHandler.sendMessageDelayed(msg, 200);
				setViewsFocusable(false);
				needDrawFocus = false;
			}
			if (position == 1) {
				needDrawFocus = true;
			}
		}

		@Override
		public void onNothingSelectedEvent() {
			// TODO Auto-generated method stub
			needDrawFocus = false;
		}

	}

	private void setViewsFocusable(boolean focusable) {
		mBtnChannelList.setFocusable(focusable);
		mBtnChannelList.setFocusableInTouchMode(focusable);
		mChannelLive.setFocusable(focusable);
		mChannelLive.setFocusableInTouchMode(focusable);
	}

	private void resetGridViewSelection(View v) {
		Log.d(TAG, "enter resetGridViewSelection");
		try {
			@SuppressWarnings("unchecked")
			Class<GridView> c = (Class<GridView>) Class
					.forName("android.widget.GridView");
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

	/**
	 * 生成时间文字
	 * 
	 * @param strTime
	 * @return
	 */
	private String getTimeRange(String startTime, String endTime) {
		StringBuffer range = new StringBuffer(12);
		range.append(utils.safeString(startTime)).append("-")
				.append(utils.safeString(endTime));

		return range.toString();
	}

	@Override
	public void notifyChange(String day) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			return true;
		}
		return super.onKeyUp(keyCode, event);
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
