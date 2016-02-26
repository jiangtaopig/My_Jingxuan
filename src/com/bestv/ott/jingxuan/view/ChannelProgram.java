package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestv.jingxuan.R;

public class ChannelProgram extends FrameLayout implements
		ProgramProgressListener {
	private static final String TAG = "ChannelProgram";
	public static final int STATUS_LIVE = 0x1000;
	public static final int STATUS_RECORD = 0x1001;
	public static final int STATUS_FUTURE = 0x1002;
	public static final int STATUS_UNFOCUS = 0x1003;
	private ProgramProgressView mProgramProgress = null;
	private ImageView mProgramStatus = null;
	private ImageView mProgramDate = null;
	private TextView mProgramTime = null;
	private TextView mProgramName = null;
	private int mStatus = STATUS_UNFOCUS;
//	private OnProgramProgressFinishListener mFinishListener = null;

	public ChannelProgram(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelProgram(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelProgram(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	private void initViews(Context context) {
		Log.d(TAG, "enter initViews");
		LayoutInflater.from(context).inflate(R.layout.jx_iotv_channel_program, this);
		mProgramProgress = (ProgramProgressView) findViewById(R.id.progress_item_progress);
		mProgramStatus = (ImageView) findViewById(R.id.program_status);
		mProgramTime = (TextView) findViewById(R.id.program_time);
		mProgramName = (TextView) findViewById(R.id.program_name);
		mProgramDate = (ImageView) findViewById(R.id.program_date);
		mProgramDate.setVisibility(View.GONE);
	}

	public void setProgramTime(String time) {
		mProgramTime.setText(time);
	}

	public void setProgramName(String name) {
		mProgramName.setText(name);
	}

	public String getProgramName() {
		return mProgramName.getText().toString();
	}
	
	public void setDateArrowVisibility(int visibility){
		mProgramDate.setVisibility(visibility);
	}
	
	public void setProgramStatus(int status) {
		mStatus = status;
		switch (status) {
		case STATUS_LIVE:
			mProgramStatus.setBackgroundResource(R.drawable.jx_iotv_status_live);
			mProgramName.setTextColor(getContext().getResources().getColor(android.R.color.white));
			mProgramTime.setTextColor(getContext().getResources().getColor(android.R.color.white));
			break;
		case STATUS_RECORD:
			mProgramStatus.setBackgroundResource(R.drawable.jx_iotv_status_record);
			mProgramName.setTextColor(getContext().getResources().getColor(android.R.color.white));
			mProgramTime.setTextColor(getContext().getResources().getColor(android.R.color.white));
			break;
		case STATUS_FUTURE:
			mProgramStatus.setBackgroundColor(getContext().getResources()
					.getColor(android.R.color.transparent));
			mProgramName.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
			mProgramTime.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
			break;
		case STATUS_UNFOCUS:
			mProgramStatus.setBackgroundColor(getContext().getResources()
					.getColor(android.R.color.transparent));
			mProgramName.setTextColor(getContext().getResources().getColor(android.R.color.white));
			mProgramTime.setTextColor(getContext().getResources().getColor(android.R.color.white));
			break;
		default:
//			mProgramStatus.setBackgroundResource(R.drawable.status_record);
			mProgramStatus.setBackgroundColor(getContext().getResources()
					.getColor(android.R.color.transparent));
			mProgramName.setTextColor(getContext().getResources().getColor(android.R.color.white));
			mProgramTime.setTextColor(getContext().getResources().getColor(android.R.color.white));
			break;
		}
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
}
