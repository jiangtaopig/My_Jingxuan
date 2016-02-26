package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.util.utils;

public class ChannelView extends FrameLayout implements ProgramProgressListener {
	private static final String TAG = "ChannelView";
	private ImageView mChannelIcon = null;
	private TextView mChannelName = null;
	private TextView mProgramName = null;
	private OnProgramProgressFinishListener mFinishListener = null;
	
	private ViewGroup mNoDataPrompt;
	private ViewGroup mProgramGroup;
	public ChannelView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	private void initViews(Context context) {
		Log.d(TAG, "enter initViews");
		LayoutInflater.from(context).inflate(R.layout.jx_iotv_channel_view, this);
		mChannelIcon = (ImageView) findViewById(R.id.channel_icon);
		mChannelName = (TextView) findViewById(R.id.recommand_channel_item_type);
		mProgramName = (TextView) findViewById(R.id.recommand_channel_item_name);
		mNoDataPrompt = (ViewGroup) findViewById(R.id.channel_view_nodata);
		mProgramGroup = (ViewGroup) findViewById(R.id.channel_view_pram);
		Log.d(TAG, "mChannelIcon : " + mChannelIcon);
		setBackgroundResource(R.drawable.jx_iotv_channel_view_selector);
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
	 * 设置节目名称
	 * 
	 * @param name
	 */
	public void setProgramName(String name) {
		mProgramName.setText(name);
	}

	public void setOnProgramProgressFinishListener(OnProgramProgressFinishListener finishListener){
		mFinishListener = finishListener;
	}
	
	@Override
	public void onProgressFinish() {
		// TODO Auto-generated method stub
		if(mFinishListener != null){
			Log.d(TAG, "onProgressFinish");
			mFinishListener.onFinish(this);
		}
	}
	
	public void setChannelName(String name){
		mChannelName.setText(name);
		
		if (utils.isNotNull(name)){
			mNoDataPrompt.setVisibility(INVISIBLE);
			mChannelName.setVisibility(VISIBLE);
			mProgramGroup.setVisibility(VISIBLE);
		}
	}
}

/**
 * 节目进度条结束
 * @author fan.jianfeng
 *
 */
interface OnProgramProgressFinishListener {
	public void onFinish(ChannelView channelView);
}
