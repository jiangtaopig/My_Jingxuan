package com.bestv.ott.jingxuan.view;

import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.bestv.ott.jingxuan.view.focus.FocusAnimation;
import com.bestv.ott.jingxuan.view.focus.FocusInterface;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer;
import com.bestv.jingxuan.R;
import com.bestv.ott.framework.utils.utils;

public class PauseView extends FrameLayout implements IViewBase {
	private static final String TAG = "PauseView";
	private static final int MSG_RESET_FOCUS = 0x6642;
	private IControllerBase mController = null;
	private IOTVMediaPlayer mMediaPlayer = null;
	private ImageButton mBtnChannelList = null;
	private ImageButton mBtnPause = null;
	private ImageView mImgPlayType = null;
	private ImageView mImgChannelListHit = null;
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
	private MyFocusInterface mFocusListener = null;
	private RecommendAdapter mRecommendAdapter = null;
	private Channel mCurChannel = null;
	private List<Channel> mRecommendChannels = null;
	private MyOnClickListener mOnClickListener = null;
	private MyOnTitleProgressFinishListener mOnFinishListener = null;
	private MyOnItemClickListener mOnItemClickListener = null;
	private MyGridOnFocusChangeListener mGridOnFocusChangeListener = null;
	private ChannelManager mChannelManager = null;
	private boolean mInitFinish = false;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_RESET_FOCUS:
				mFocusLayout.setVisibility(View.VISIBLE);
				mBtnPause.requestFocus();
				mInitFinish = true;
				break;

			default:
				break;
			}

		}

	};

	public PauseView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PauseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PauseView(Context context, AttributeSet attrs) {
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
		if(mMediaPlayer != null){
			mMediaPlayer.pause();
		}
		initViews();
	}

	@Override
	public void reset() {
		
	}
	
	private void initData() {
		mChannelManager = ChannelManager.getInstance();
		if (mController != null) {
			mCurChannel = mController.getCurrentChannel();
			mMediaPlayer = mController.getIOTVMediaPlayer();
			mRecommendChannels = mController.getPlayRecords();
			mRecommendAdapter = new RecommendAdapter(getContext(),
					mRecommendChannels, mController);
			mOnClickListener = new MyOnClickListener();
			mOnFinishListener = new MyOnTitleProgressFinishListener();
			mOnItemClickListener = new MyOnItemClickListener();
			mFocusListener = new MyFocusInterface();
			mGridOnFocusChangeListener = new MyGridOnFocusChangeListener();
		} else {
			Log.w(TAG, "mController is null");
		}
	}

	private void initViews() {
		mChannelTitle = (ChannelTitle) findViewById(R.id.channe_title);
		initTitle();
		mImgPlayType = (ImageView) findViewById(R.id.img_play_type);
		initPlayType();
		mImgChannelListHit = (ImageView) findViewById(R.id.img_channel_list_hit);
		if(OttContextMgr.getInstance().isIOTVRunning() && mImgChannelListHit != null){
			mImgChannelListHit.setBackgroundResource(R.drawable.jx_iotv_channel_list_hit_menu);
		}
		mBtnChannelList = (ImageButton) findViewById(R.id.btn_channel_list);
		mBtnChannelList.setOnClickListener(mOnClickListener);
		mBtnPause = (ImageButton) findViewById(R.id.btn_pause);
		mBtnPause.setOnClickListener(mOnClickListener);
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

		mFocusAnim.foucsChange(mBtnPause);
		mFocusAnim.foucsChange(mBtnChannelList);
		mFocusAnim.setFocusListener(mFocusListener);
		mFocusAnim.setOffset((int) getContext().getResources().getDimension(
				R.dimen.jx_tv_focus_offset));
		mFocusAnim.selectChange(mGridRecommend, null);
		
		mHandler.sendEmptyMessageDelayed(MSG_RESET_FOCUS, 800);
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
		long startTime = curChannelData.getStartTime();
		long endTime = curChannelData.getEndTime();
		long curTime = curChannelData.getCurTime();
		mChannelTitle.setProgressData(startTime, endTime, curTime);
		String startTimeStr = utils
				.safeString(curChannelData.getStartTimeStr());
		String scheduleName = utils
				.safeString(curChannelData.getScheduleName());
		mChannelTitle.setCurProgram(startTimeStr + " " + scheduleName);

//		ChannelData nxtChannelData = ChannelManager.getInstance().getNextSchedule(mCurChannel);
		ChannelData nxtChannelData = ChannelManager.getInstance().getNextSchedule(mCurChannel, mController.getCurrentPlayingTime());
		if (nxtChannelData == null) {
			Log.w(TAG, "nxtChannelData is null");
			return;
		}
		startTimeStr = nxtChannelData.getStartTimeStr();
		scheduleName = nxtChannelData.getScheduleName();
		if (startTimeStr != null && scheduleName != null){
		    mChannelTitle.setNextProgram(startTimeStr + " " + scheduleName);
		}
	}

	private void initPlayType() {
		if (mImgPlayType != null && mController != null) {
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
		this.setVisibility(View.INVISIBLE);
		if (mRecommendAdapter != null) {
			mRecommendAdapter.releaseData();
		}
		if (mChannelTitle != null) {
			mChannelTitle.releaseData();
		}
		if(mMediaPlayer != null){
			mMediaPlayer.unpause();
		}
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
			if (id == R.id.btn_pause) {
				hide();
			} else if (id == R.id.btn_channel_list) {
				mController.gotoChannelList();
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
