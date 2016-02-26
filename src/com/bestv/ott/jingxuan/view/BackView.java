package com.bestv.ott.jingxuan.view;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
import android.widget.TextView;

import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.RecommendItem;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.view.focus.FocusAnimation;
import com.bestv.ott.jingxuan.view.focus.FocusInterface;
import com.bestv.jingxuan.R;
import com.bestv.ott.framework.utils.utils;

public class BackView extends FrameLayout implements IViewBase {
	private static final String TAG = "BackView";
	private static final int MSG_RESET_FOCUS = 0x6642;
	private IControllerBase mController = null;
	private ImageButton mBtnBackHome = null;
	private GridView mGridRecommend = null;
	private TextView mTvType = null;
	private TextView mTvName = null;
	/**
	 * 选中焦点
	 */
	private FrameLayout mFocusLayout = null;
	/**
	 * 平移焦点
	 */
	private FocusAnimation mFocusAnim = null;
	private MyFocusInterface mFocusListener = null;
	private JxRecommendAdapter mRecommendAdapter = null;
	private Channel mCurChannel = null;
	private List<RecommendItem> mRecommendItems = null;
	private MyOnClickListener mOnClickListener = null;
	private MyOnItemClickListener mOnItemClickListener = null;
	private MyGridOnFocusChangeListener mGridOnFocusChangeListener = null;
	private ChannelManager mChannelManager = null;
	private boolean mInitFinish = false;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_RESET_FOCUS:
				mFocusLayout.setVisibility(View.VISIBLE);
				mGridRecommend.requestFocus();
				mBtnBackHome.requestFocus();
				//mBtnChannelList.requestFocus();
				mInitFinish = true;
				break;

			default:
				break;
			}
		}
	};

	public BackView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public BackView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public BackView(Context context, AttributeSet attrs) {
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
		try{
			mOnClickListener = new MyOnClickListener();
			mChannelManager = ChannelManager.getInstance();
			mCurChannel = mController.getCurrentChannel();
			if(mCurChannel == null){
				mInitFinish = true;
				return;
			}
			if(mCurChannel.getChannelCode().equalsIgnoreCase(
					Constants.JX_LATEST_CHANNEL_CODE)){
				mRecommendItems = new ArrayList<RecommendItem>(mController.getRandomSchedules(mCurChannel, 3, null));
			}else if(mCurChannel.getChannelCode().equalsIgnoreCase(
					Constants.JX_HOT_CHANNEL_CODE)){
				if(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE) != null){
					//如果有最新分类，则一最新二最热
					mRecommendItems = new ArrayList<RecommendItem>();
					mRecommendItems.add(mController.getRandomSchedules(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE), 1, null).get(0));
					List<RecommendItem> temp = mController.getRandomSchedules(mCurChannel, 2, null);
					mRecommendItems.add(temp.get(0));
					mRecommendItems.add(temp.get(1));
				}else{
					//如果没有最新分类，则三最热
					mRecommendItems = new ArrayList<RecommendItem>(mController.getRandomSchedules(mCurChannel, 3, null));
				}
			}else{
				if(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE) != null
						&& com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_HOT_CHANNEL_CODE) != null){
					//如果有最新和最热，则一最新一最热一当前
					mRecommendItems = new ArrayList<RecommendItem>();
					List temp1 = mController.getRandomSchedules(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE), 1, null);
					if(temp1 != null && temp1.size() > 0){
						mRecommendItems.add((RecommendItem) temp1.get( 0));
					}
					List temp2 = mController.getRandomSchedules(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_HOT_CHANNEL_CODE), 1, null);
					if(temp2 != null && temp2.size() > 0){
						mRecommendItems.add((RecommendItem) temp2.get(0));
					}
					mRecommendItems.add(mController.getRandomSchedules(mCurChannel, 1, null).get(0));
				}else if(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE) == null
						&& com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_HOT_CHANNEL_CODE) != null){
					//如果无最新有最热，则两最热一当前
					mRecommendItems = new ArrayList<RecommendItem>();
					List temp2 = mController.getRandomSchedules(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_HOT_CHANNEL_CODE), 2, null);
					if(temp2 != null && temp2.size() > 0){
						mRecommendItems.add((RecommendItem) temp2.get(0));
						mRecommendItems.add((RecommendItem) temp2.get(1));
					}
					mRecommendItems.add(mController.getRandomSchedules(mCurChannel, 1, null).get(0));
				}else if(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE) != null
						&& com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_HOT_CHANNEL_CODE) == null){
					//如果有最新无最热，则两最新一当前
					mRecommendItems = new ArrayList<RecommendItem>();
					List temp1 = mController.getRandomSchedules(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE), 2, null);
					if(temp1 != null && temp1.size() > 0){
						mRecommendItems.add((RecommendItem) temp1.get(0));
						mRecommendItems.add((RecommendItem) temp1.get(1));
					}
					mRecommendItems.add(mController.getRandomSchedules(mCurChannel, 1, null).get(0));
				}else if(com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_LATEST_CHANNEL_CODE) == null
						&& com.bestv.ott.jingxuan.util.utils.getChannelByChannelCode(mController.getCurrentChannelList(), Constants.JX_HOT_CHANNEL_CODE) == null){
					//如果无最新无最热，则一当前两其他
					mRecommendItems = new ArrayList<RecommendItem>(mController.getRandomSchedules(mCurChannel, 3, null));
				}
			}
			
			mRecommendAdapter = new JxRecommendAdapter(getContext(),
					mRecommendItems, mController);
			
			mOnItemClickListener = new MyOnItemClickListener();
			mFocusListener = new MyFocusInterface();
			mGridOnFocusChangeListener = new MyGridOnFocusChangeListener();	
		}catch(Exception e){
			mInitFinish = true;
			e.printStackTrace();
		}
	}

	private void initViews() {
		mTvType = (TextView)findViewById(R.id.jx_cur_title);
		mTvName = (TextView)findViewById(R.id.jx_cur_name);
		initTitle();
		mBtnBackHome = (ImageButton) findViewById(R.id.btn_back_home);
		mBtnBackHome.setOnClickListener(mOnClickListener);
		mGridRecommend = (GridView) findViewById(R.id.grid_recommend);
		
		if(mRecommendItems == null || mCurChannel == null){
			mInitFinish = true;
			mFocusLayout = (FrameLayout) findViewById(R.id.frame_focus);
			mFocusAnim = new FocusAnimation(getContext(), mFocusLayout);
			mFocusLayout.setVisibility(View.INVISIBLE);
			
			mFocusAnim.foucsChange(mBtnBackHome);
			mFocusAnim.setFocusListener(mFocusListener);
			mFocusAnim.setOffset((int) getContext().getResources().getDimension(
					R.dimen.jx_tv_focus_offset));
			mHandler.sendEmptyMessageDelayed(MSG_RESET_FOCUS, 100);
			return;
		}
		mGridRecommend.setAdapter(mRecommendAdapter);
		mGridRecommend.setNumColumns(mRecommendItems.size());
		int width = (int) (getResources().getDimension(
				R.dimen.jx_backview_channel_view_width) + getResources().getDimension(
				R.dimen.jx_oper_item_spacing))
				* mRecommendItems.size();
		int height = (int) (getResources()
				.getDimension(R.dimen.jx_channel_view_height));
		LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
				width, height);
		mFocusLayout = (FrameLayout) findViewById(R.id.frame_focus);
		mFocusAnim = new FocusAnimation(getContext(), mFocusLayout);
		
		mGridRecommend.setLayoutParams(layoutparams);
		mGridRecommend.setOnItemClickListener(mOnItemClickListener);
		mGridRecommend.setOnFocusChangeListener(mGridOnFocusChangeListener);
		mGridRecommend.setOnItemClickListener(mOnItemClickListener);
		
		mGridRecommend.setSelection(1);
		mFocusLayout.setVisibility(View.INVISIBLE);
		
		mFocusAnim.foucsChange(mBtnBackHome);
		mFocusAnim.setFocusListener(mFocusListener);
		mFocusAnim.setOffset((int) getContext().getResources().getDimension(
				R.dimen.jx_tv_focus_offset));
		mFocusAnim.selectChange(mGridRecommend, null);
		
		mHandler.sendEmptyMessageDelayed(MSG_RESET_FOCUS, 100);
	}
	
	private void initTitle() {
	//change by zjt 2016/1/17 for nullpointer
		try{
			if(mCurChannel != null){
				Log.d(TAG, "ChannelName = "+mCurChannel.getChannelName());
				mTvType.setText(mCurChannel.getChannelName());
			}
			
			if(mController != null && mController.getPlayingSchedule() != null){
				Log.d(TAG, "PlayingSchedule().getName = "+mController.getPlayingSchedule().getName());
				mTvName.setText(mController.getPlayingSchedule().getName());
			}
			
		}catch(Exception e){
			e.printStackTrace();
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
			if (id == R.id.btn_back_home) {
				mController.exitApp();
			} else if (id == R.id.btn_channel_list) {
				mController.gotoChannelList();
			} else {
			}
		}

	}

	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if(parent.getAdapter() instanceof JxRecommendAdapter){
				JxRecommendAdapter adapter = (JxRecommendAdapter) parent.getAdapter();
				RecommendItem recommendItem = (RecommendItem)adapter.getItem(position);
				Channel channel = recommendItem.getChannel();
				Schedule schedule = recommendItem.getSchedule();
				if (channel == null || utils.isNull(channel.getChannelCode())){
					return;
				}
				
				mController.iotvPlay(channel, schedule);
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
		
		private void resetGridViewSelection(View v){
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
