package com.bestv.ott.jingxuan.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.bestv.jingxuan.R;

public class ProgramProgressView extends FrameLayout {
	private static final String TAG = "ProgramProgressView";
	private static final int MSG_UPDATE_PROGRESS = 0x3339;
	private ProgressBar mProgramProgress = null;
	private ProgramProgressListener mProgramProgressListener = null;
	private long mStartTime = 0L;
	private long mEndTime = 0L;
	private long mCurTime = 0L;
	private Timer mTimer = null;
	private TimerTask mTimeTask = null;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_UPDATE_PROGRESS:
				calculateProgress();
				mCurTime += 1000;
				break;

			default:
				break;
			}
		}

	};

	public ProgramProgressView(Context context) {
		super(context);
		init(context);
	}

	public ProgramProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public ProgramProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		initViews(context);
	}

	private void initViews(Context context) {
		LayoutInflater.from(context).inflate(R.layout.jx_iotv_program_progress_view,
				this);
		mProgramProgress = (ProgressBar) findViewById(R.id.program_progress);
	}

	/**
	 * 设置进度条显示的节目时间范围
	 * 
	 * @param startTime
	 * @param endTime
	 */
	public void setTimeRange(long startTime, long endTime) {
		mProgramProgress.setVisibility(View.VISIBLE);
		mStartTime = startTime;
		mEndTime = endTime;
	}

	/**
	 * 设置当前时间
	 * 
	 * @param curTime
	 */
	public void setCurTime(long curTime) {
		mCurTime = curTime;
	}

	/**
	 * 针对当前节目开始自动更新进度
	 */
	public void autoUpdate() {
		initTimer();
		mTimer.schedule(mTimeTask, 0, 1000);
	}
	
	/**
	 * 停止跟新进度，释放资源
	 */
	public void stopUpdate() {
		mProgramProgress.setVisibility(View.INVISIBLE);
		releaseTimer();
	}
	
	public void setProgressListener(ProgramProgressListener programProgressListener){
		mProgramProgressListener = programProgressListener;
	}
	
	/**
	 * 计算并显示进度条
	 */
	private void calculateProgress() {
		int totalRange = (int) (mEndTime - mStartTime);
		int curRange = (int) (mCurTime - mStartTime);
		if (totalRange > 0 && curRange > 0) {
			int progress = curRange * 100 / totalRange;
			if(progress % 20 == 0){
				Log.d(TAG, "Progress : " + progress);
			}
			if(progress >= 100){
				if(mProgramProgressListener != null){
					releaseTimer();
					mProgramProgressListener.onProgressFinish();
				}else{
					Log.w(TAG, "mProgramProgressListener is null");
				}
			}else{
				if (mProgramProgress != null) {
					mProgramProgress.setProgress(progress);
				} else {
					Log.d(TAG, "mProgramProgress is null");
				}
			}
		}
	}
	
	private void initTimer() {
		releaseTimer();
		Log.d(TAG, "enter initTimer");
		mTimer = new Timer();
		mTimeTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
//				Log.d(TAG, "AAAAA");
				mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
			}
		};
	}
	
	private void releaseTimer(){
		Log.d(TAG, "enter releaseTimer");
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mTimeTask != null) {
			mTimeTask.cancel();
		}
	}
}

interface ProgramProgressListener {
	/**
	 * 进度条结束
	 */
	public void onProgressFinish();
	
}
