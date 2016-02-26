package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bestv.jingxuan.R;
import com.bestv.ott.framework.utils.utils;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.ChannelIconMgr;
import com.bestv.ott.jingxuan.iotvlogservice.utils.FileUtils;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.Logger;

public class FakeVideoLoadingView extends RelativeLayout implements IViewBase{
    private IControllerBase controller;
    
    private ImageView mTypeImage;
    private ChannelTitle mChannelTitle;
    private ImageView mListHintImage;
    
    private static final int MSG_AUTO_HIDE = 100;
    private static final int AUTO_HIDE_TIMEOUT = 2000;
    
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			if (msg.what == MSG_AUTO_HIDE){
				if (mChannelTitle != null){
					mChannelTitle.releaseData();
				}
				
				FakeVideoLoadingView.this.setVisibility(View.INVISIBLE);
			}
		}
    };
    
	public FakeVideoLoadingView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FakeVideoLoadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public FakeVideoLoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setDataController(IControllerBase c) {
		// TODO Auto-generated method stub
		controller = c;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
		mTypeImage = (ImageView) findViewById(R.id.loading_play_type);
		mChannelTitle = (ChannelTitle) findViewById(R.id.loading_channe_title);
		mListHintImage = (ImageView) findViewById(R.id.iotv_list_hint);
		
		
		if (mHandler.hasMessages(MSG_AUTO_HIDE)){
			mHandler.removeMessages(MSG_AUTO_HIDE);
		}
		
		showAllElements();
		
		if (OttContextMgr.getInstance().isJXRunning()) {
			mChannelTitle.setVisibility(View.GONE);
			mTypeImage.setVisibility(View.GONE);
			
			this.setBackgroundColor(Color.BLACK);
			showJxTitle();
		} else {
			if (controller.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
				mTypeImage.setBackgroundColor(getContext().getResources()
						.getColor(android.R.color.transparent));
			} else {
				mTypeImage.setBackgroundResource(R.drawable.jx_iotv_lookback_prompt_img);
			}

			initTitle();
		}

		this.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void reset() {
		
	}
	
	private void showJxTitle(){
		if (controller.getCurrentChannel() == null || controller.getPlayingSchedule() == null){
			return;
		}
		
		ViewGroup vg = (ViewGroup) this.findViewById(R.id.jx_loadingtitle);
		vg.setVisibility(View.VISIBLE);
		
		TextView title = (TextView) vg.findViewById(R.id.jx_cur_title);
		TextView name = (TextView) vg.findViewById(R.id.jx_cur_name);
		
		title.setText(R.string.jx_next_program);
		name.setText(controller.getPlayingSchedule().getName());
	}
	
	private void initTitle() {		
		Channel channel = controller.getCurrentChannel();
		if (channel == null || controller.getIOTVMediaPlayer() == null){
			return;
		}
		
		mChannelTitle.setChannelNum(Integer.valueOf(channel.getChannelNo()));
		mChannelTitle.setChannelName(channel.getChannelName());
		mChannelTitle.setChannelIcon(ChannelIconMgr.getChannelIcon(channel.getChannelName(), channel.getIcon1()));
		
		ChannelData curChannelData = ChannelManager.getInstance().getSchedule(channel, controller.getCurrentPlayingTime());
		
		if (curChannelData == null) {
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

		ChannelData nxtChannelData = ChannelManager.getInstance().getNextSchedule(channel, controller.getCurrentPlayingTime());
		if (nxtChannelData == null) {
			return;
		}
		startTimeStr = nxtChannelData.getStartTimeStr();
		scheduleName = nxtChannelData.getScheduleName();
		mChannelTitle.setNextProgram(startTimeStr + " " + scheduleName);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		Logger.d("hide fake loading view");
		setVisibility(INVISIBLE);

	}
	
	private void hideWithOutChannelTitle(){
		if (mListHintImage == null){
			return;
		}
		
		mListHintImage.setVisibility(INVISIBLE);
		mTypeImage.setVisibility(INVISIBLE);
	}
	
	private void showAllElements(){
		mListHintImage.setVisibility(VISIBLE);
		mTypeImage.setVisibility(VISIBLE);
	}

	@Override
	public boolean isshow() {
		// TODO Auto-generated method stub
		return this.getVisibility() == VISIBLE;
	}

	@Override
	public void notifyChange(String day) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            return false;
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
