package com.bestv.ott.jingxuan.view;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelIndex extends FrameLayout {
	private static final String TAG = "ChannelIndex";
	private ImageView mChannelNum1 = null;
	private ImageView mChannelNum2 = null;
	private TextView mChannelDay = null;
	private TextView mChannelName = null;
	
	private String mRawDayStr;
	
	public ChannelIndex(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelIndex(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public ChannelIndex(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	private void initViews(Context context) {
		Log.d(TAG, "enter initViews");
		LayoutInflater.from(context).inflate(R.layout.jx_iotv_channel_index, this);
		mChannelNum1 = (ImageView) findViewById(R.id.channel_num_1);
		mChannelNum2 = (ImageView) findViewById(R.id.channel_num_2);
		mChannelDay = (TextView) findViewById(R.id.channel_day);
		mChannelName = (TextView) findViewById(R.id.channel_name);
	}
	
	/**
	 * 设置日期，今天、周一（忙死day）、周二、周三…
	 * @param day
	 */
	public void setChannelDay(String day){
		mRawDayStr = day;
		
		if (utils.isNull(day)){
			mChannelDay.setText("");
		}else{
			mChannelDay.setText(IOTV_Date.instance().converTimeStr(mRawDayStr));
		}
		
	}
	
	public String getChannelDay(){
		return mRawDayStr;
	}
	
	public void setChannelDayVisible(boolean visiblity){
		if(visiblity){
			mChannelDay.setVisibility(View.VISIBLE);
		}else{
			mChannelDay.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 设置频道号
	 * 
	 * @param channelNum
	 */
	public void setChannelNum(int channelNum) {
		if (channelNum < 0){
			mChannelNum1.setVisibility(View.INVISIBLE);
			mChannelNum2.setVisibility(View.INVISIBLE);
			return;
		}
		
		mChannelNum1.setVisibility(View.VISIBLE);
		mChannelNum2.setVisibility(View.VISIBLE);
		
		int num1 = channelNum / 10;
		int num2 = channelNum % 10;
		setNum(num1, mChannelNum1);
		setNum(num2, mChannelNum2);
	}
	
	public void setChannelName(String name){
		mChannelName.setText(name);
	}
	
	private void setNum(int num, ImageView imageView) {
		if (imageView != null) {
			int resId = R.drawable.jx_iotv_num_0_small;
			switch (num) {
			case 0:
				resId = R.drawable.jx_iotv_num_0_small;
				break;
			case 1:
				resId = R.drawable.jx_iotv_num_1_small;
				break;
			case 2:
				resId = R.drawable.jx_iotv_num_2_small;
				break;
			case 3:
				resId = R.drawable.jx_iotv_num_3_small;
				break;
			case 4:
				resId = R.drawable.jx_iotv_num_4_small;
				break;
			case 5:
				resId = R.drawable.jx_iotv_num_5_small;
				break;
			case 6:
				resId = R.drawable.jx_iotv_num_6_small;
				break;
			case 7:
				resId = R.drawable.jx_iotv_num_7_small;
				break;
			case 8:
				resId = R.drawable.jx_iotv_num_8_small;
				break;
			case 9:
				resId = R.drawable.jx_iotv_num_9_small;
				break;
			default:
				resId = R.drawable.jx_iotv_num_0_small;
				break;
			}
			imageView.setImageResource(resId);
		}
	}
}
