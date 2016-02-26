package com.bestv.ott.jingxuan.view;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.util.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelTitle extends FrameLayout implements ProgramProgressListener{
	private static final String TAG = "ChannelTitle";
	private ProgramProgressView mChannelProgress = null;
	public TextView mCurProgram = null;
	public TextView mNextProgram = null;
	private TextView mCurChannelName = null;
	private ImageView mChannelIcon = null;
	private ImageView mChannelNum1 = null;
	private ImageView mChannelNum2 = null;
	private ImageView mProgressBGImageView = null;
	private OnTitleProgressFinishListener mFinishListener = null;

	public ChannelTitle(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelTitle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	private void initViews(Context context) {
		Log.d(TAG, "enter initViews");
		LayoutInflater.from(context).inflate(R.layout.jx_iotv_channel_title, this);
		mCurProgram = (TextView) findViewById(R.id.channel_cur_program);
		mNextProgram = (TextView) findViewById(R.id.channel_next_program);
		mCurChannelName = (TextView) findViewById(R.id.channel_cur_name);
		mChannelProgress = (ProgramProgressView) findViewById(R.id.channel_progress);
		mChannelProgress.setProgressListener(this);
		mChannelIcon = (ImageView) findViewById(R.id.channel_icon);
		mChannelNum1 = (ImageView) findViewById(R.id.channel_num_1);
		mChannelNum2 = (ImageView) findViewById(R.id.channel_num_2);
		mProgressBGImageView = (ImageView) findViewById(R.id.channel_title_progress_bg);
		mProgressBGImageView.setVisibility(View.INVISIBLE);
		Log.d(TAG, "mChannelIcon : " + mChannelIcon);
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
				+ mChannelProgress + ",startTime : " + startTime
				+ ", endTime : " + endTime + ", curTime : " + curTime);
		if (mChannelProgress != null) {
			mChannelProgress.setTimeRange(startTime, endTime);
			mChannelProgress.setCurTime(curTime);
			mChannelProgress.autoUpdate();
		}
	}
	
	public void setChannelName(String name){
		mCurChannelName.setText(name);
	}

	/**
	 * 不显示时释放资源
	 */
	public void releaseData() {
		if (mChannelProgress != null) {
			mChannelProgress.stopUpdate();
		}
	}

	/**
	 * 设置频道号
	 * 
	 * @param channelNum
	 */
	public void setChannelNum(int channelNum) {
		int num1 = channelNum / 10;
		int num2 = channelNum % 10;
		setNum(num1, mChannelNum1);
		setNum(num2, mChannelNum2);
	}

	public void setChannelIcon(int resID) {
		if (mChannelIcon != null) {
			mChannelIcon.setImageResource(resID);
		}
	}

	public void setChannelIcon(Drawable drawable) {
		if (mChannelIcon != null) {
			if (drawable != null){
				mChannelIcon.setVisibility(View.VISIBLE);
			    mChannelIcon.setImageDrawable(drawable);
			}else{
				mChannelIcon.setVisibility(View.GONE);
				mChannelIcon.setImageDrawable(drawable);
			}
		}
	}

	public void setChannelIcon(Bitmap bitmap) {
		if (mChannelIcon != null && bitmap != null) {
			mChannelIcon.setImageBitmap(bitmap);
		}
	}

	/**
	 * 设置当前节目时间&名字
	 * 
	 * @param curProgram
	 */
	public void setCurProgram(String curProgram) {
		if (mCurProgram != null) {
			if (utils.isNotNull(curProgram)){
			    mCurProgram.setText(utils.safeString(curProgram));
			    mProgressBGImageView.setVisibility(View.VISIBLE);
			}else{
				mCurProgram.setText("");
				mProgressBGImageView.setVisibility(View.INVISIBLE);
			}
		}
	}

	/**
	 * 设置下一个节目时间&名字
	 * 
	 * @param nextProgram
	 */
	public void setNextProgram(String nextProgram) {
		if (mNextProgram != null) {
			if (nextProgram != null && !nextProgram.startsWith("null")){
				Log.d("harish5", "nextProgram = " + nextProgram);
			    mNextProgram.setText(utils.safeString(nextProgram));
			}else{
				mNextProgram.setText("");
			}
		}
	}
	
	public void setOnTitleProgressFinishListener(OnTitleProgressFinishListener finishListener){
		mFinishListener = finishListener;
	}

	private void setNum(int num, ImageView imageView) {
		if (imageView != null) {
			int resId = R.drawable.jx_iotv_num_0;
			switch (num) {
			case 0:
				resId = R.drawable.jx_iotv_num_0;
				break;
			case 1:
				resId = R.drawable.jx_iotv_num_1;
				break;
			case 2:
				resId = R.drawable.jx_iotv_num_2;
				break;
			case 3:
				resId = R.drawable.jx_iotv_num_3;
				break;
			case 4:
				resId = R.drawable.jx_iotv_num_4;
				break;
			case 5:
				resId = R.drawable.jx_iotv_num_5;
				break;
			case 6:
				resId = R.drawable.jx_iotv_num_6;
				break;
			case 7:
				resId = R.drawable.jx_iotv_num_7;
				break;
			case 8:
				resId = R.drawable.jx_iotv_num_8;
				break;
			case 9:
				resId = R.drawable.jx_iotv_num_9;
				break;
			default:
				resId = R.drawable.jx_iotv_num_0;
				break;
			}
			imageView.setImageResource(resId);
		}
	}

	@Override
	public void onProgressFinish() {
		// TODO Auto-generated method stub
		if(mFinishListener != null){
			Log.d(TAG, "onProgressFinish");
			mFinishListener.onFinish(this);
		}
	}
}

/**
 * 节目进度条结束
 * @author fan.jianfeng
 *
 */
interface OnTitleProgressFinishListener {
	public void onFinish(ChannelTitle channelTitle);
}
