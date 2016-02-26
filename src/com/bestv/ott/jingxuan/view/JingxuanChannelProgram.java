package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestv.jingxuan.R;

public class JingxuanChannelProgram extends FrameLayout implements
		JingxuanProgramProgressListener {
	private static final String TAG = "JingxuanChannelProgram";
	public static final int STATUS_LIVE = 0x1000;
	public static final int STATUS_RECORD = 0x1001;
	public static final int STATUS_FUTURE = 0x1002;
	public static final int STATUS_UNFOCUS = 0x1003;
	private JingxuanProgramProgressView mProgramProgress = null;
	private ImageView mProgramChannelIcon = null;
	private ImageView mProgramSubscribeUpdate = null;
	private ImageView mProgramSubscribeInvalid = null;
	private TextView mProgramName = null;
	/*add by xubin 2015.11.24 begin*/
	private TextView mNextProgramName = null;
	/*add by xubin 2015.11.24 end*/
	private int mStatus = STATUS_UNFOCUS;
	private boolean mIsCurrentPlayProgram =false;
//	private OnProgramProgressFinishListener mFinishListener = null;

	public JingxuanChannelProgram(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public JingxuanChannelProgram(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public JingxuanChannelProgram(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	private void initViews(Context context) {
		Log.d(TAG, "enter initViews");
		LayoutInflater.from(context).inflate(R.layout.jx_jingxuan_iotv_channel_program, this);
		mProgramProgress = (JingxuanProgramProgressView) findViewById(R.id.progress_item_progress);
		mProgramChannelIcon = (ImageView) findViewById(R.id.program_channel_icon);
		mProgramName = (TextView) findViewById(R.id.program_name);
		/*add by xubin 2015.11.24 begin*/
		mNextProgramName = (TextView) findViewById(R.id.next_program_name);
		/*add by xubin 2015.11.24 end*/
		mProgramSubscribeUpdate  = (ImageView) findViewById(R.id.program_subscribe_update);
		mProgramSubscribeInvalid = (ImageView) findViewById(R.id.program_subscribe_invalid);
	}

	public void setProgramTime(String time) {
		//mProgramTime.setText(time);
	}

	public void setProgramName(String name) {
		mProgramName.setText(name);
	}
	
	/*add by xubin 2015.11.24 begin*/
	public void setNextProgramName(String name, boolean isFocus){
		if(isFocus){
			mNextProgramName.setVisibility(View.VISIBLE);
		}else{
			mNextProgramName.setVisibility(View.GONE);
		}
		mNextProgramName.setText(name);
	}
	public void setProgramNameEx(String name, boolean isFocus) {
		if(isFocus){
			mProgramName.setSingleLine(false);
		}else{
			mProgramName.setSingleLine(true);
		}
		mProgramName.setText(name);
	}
	/*add by xubin 2015.11.24 end*/

	public String getProgramName() {
		return mProgramName.getText().toString();
	}
	
	public void setDateArrowVisibility(int visibility){
		
	}
	
	public void setProgramStatus(int status) {
		mStatus = status;
	}
	
	public int getProgramStatus(){
		return mStatus;
	}

	/**
	 * 设置观看进度条数据
	 * 
	 * @param startTime
	 *            节目开始时间
	 * @param endTime
	 *            界面结束时间
	 * @param curTime
	 *            当前时间
	 */
	public void setProgressData(long startTime, long endTime, long curTime) {
		Log.d(TAG, "enter setProgressData, mChannelProgress : "
				+ mProgramProgress + ",startTime : " + startTime
				+ ", endTime : " + endTime + ", curTime : " + curTime);
		if (mProgramProgress != null) {
			mProgramProgress.setTimeRange(startTime, endTime);
			mProgramProgress.setCurTime(curTime);
			mProgramProgress.autoUpdate();
			mProgramProgress.setProgressListener(this);
		}
	}

	/**
	 * 不显示时释放资源
	 */
	public void releaseData() {
		if (mProgramProgress != null) {
			mProgramProgress.stopUpdate();
		}
	}

//	public void setOnProgramProgressFinishListener(
//			OnProgramProgressFinishListener finishListener) {
//		mFinishListener = finishListener;
//	}

	@Override
	public void onProgressFinish() {
		// TODO Auto-generated method stub
//		if (mFinishListener != null) {
//			Log.d(TAG, "onProgressFinish");
//			mFinishListener.onFinish(this);
//		}
	}

//	/**
//	 * 节目进度条结束
//	 * 
//	 * @author fan.jianfeng
//	 * 
//	 */
//	interface OnProgramProgressFinishListener {
//		public void onFinish(ChannelProgram channelProgram);
//	}
	
	public void setProgramChannelIcon(Drawable drawable) {
		mProgramChannelIcon.setVisibility(View.VISIBLE);
		if (drawable != null) {
			mProgramChannelIcon.setImageDrawable(drawable);
		}else{
			mProgramChannelIcon.setImageDrawable(null);
		}
	}
	
	public void setProgramDate(String date) {
		mProgramChannelIcon.setVisibility(View.GONE);
	}
	
	public void setProgramChannelIconGone() {
		mProgramChannelIcon.setVisibility(View.GONE);
	}
	
	public void setProgressViewVisible(boolean visible){
		if(!mIsCurrentPlayProgram){
			mProgramProgress.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
		}
	}
	
	public void setProgressViewVisible(boolean visible, boolean isCurrentPlayProgram){
		mIsCurrentPlayProgram = isCurrentPlayProgram;
		mProgramProgress.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
	}
	
	@Override
	public void setBackgroundResource(int resid){
		if(mStatus != ChannelProgram.STATUS_LIVE){
			super.setBackgroundResource(resid);
		}else{
			super.setBackgroundResource(android.R.color.transparent);
		}
	}
	
	public void setSubscribeUpdateVisible(boolean visible) {
		mProgramSubscribeUpdate.setVisibility(visible ? View.VISIBLE
				: View.GONE);
	}
	
	public void setSubscribeInvalidVisible(boolean visible) {
		mProgramSubscribeInvalid.setVisibility(visible ? View.VISIBLE
				: View.GONE);
	}
}
