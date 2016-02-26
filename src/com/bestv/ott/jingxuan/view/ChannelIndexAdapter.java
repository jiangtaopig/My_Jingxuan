package com.bestv.ott.jingxuan.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;

public class ChannelIndexAdapter extends BaseAdapter {
	private static final int MAX_PAGE_ITEMS = 8;
	
	private static final String TAG = "ChannelIndexAdapter";
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<ChannelIndex> mChannelIndexs = null;
	private IControllerBase mController = null;
	private String mDateStr = null;
	private int mPosition = 0;
	private int mListSize = MAX_PAGE_ITEMS;

	public ChannelIndexAdapter(Context context, IControllerBase controller, String date) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mChannelIndexs = new ArrayList<ChannelIndex>();
		mController = controller;

		initViews();
	}
	
	public void updateListContent(List<Channel> newlist){
		Logger.d("enter updateListContent");
		int indexsize = mChannelIndexs.size();
		int newlistsize = newlist.size();
		Logger.d(TAG, "updateListContent , indexsize = "+indexsize+" , newlistsize = "+newlistsize);
		mListSize = newlistsize;
		
		for (int i = 0; i < indexsize; i++){
	        ChannelIndex ci = mChannelIndexs.get(i);
			clearChannelIndex(ci);
			
			if (i < newlistsize){
				Channel channel = newlist.get(i);
				bindChannelData(ci, channel);
			}
		}
		
		if (newlistsize < MAX_PAGE_ITEMS){
			for(int i = newlistsize; i < MAX_PAGE_ITEMS; i++){
				mChannelIndexs.get(i).setVisibility(View.INVISIBLE);
			}
		}else{
			for(int i = 0; i < MAX_PAGE_ITEMS; i++){
				mChannelIndexs.get(i).setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListSize;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mChannelIndexs.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private void initViews() {
		Logger.d(TAG, "enter initViews");
		try {
			for (int i = 0; i < MAX_PAGE_ITEMS; i++) {
				ChannelIndex channelIndex = (ChannelIndex) mLayoutInflater
						.inflate(R.layout.jx_iotv_channel_list_index_item, null);
				//channelIndex = bindChannelData(channelIndex, channel);
				//Log.d(TAG, "Channel : " + channel.getChannelName());
				if (channelIndex != null) {
					Logger.d(TAG, "channelIndex != null");
					mChannelIndexs.add(channelIndex);
					bindChannelData(channelIndex, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null && mChannelIndexs != null) {
			convertView = mChannelIndexs.get(position);
		}
		return convertView;
	}

	private ChannelIndex bindChannelData(ChannelIndex channelIndex,Channel channel) {
		Logger.d(TAG, "bindChannelData . channel = "+channel);
		if (channel != null) {
			String channelName = channel.getChannelName();
			channelIndex.setChannelName(channelName);
			Log.d("harish_2015", "channel name = " + channelName + " day = " + channel.getDay());
			channelIndex.setChannelDay(channel.getDay());
			int channelNum = Integer.parseInt(channel.getChannelNo());
			channelIndex.setChannelNum(channelNum);
			channelIndex.setTag(channel);
		}
		channelIndex.setBackgroundResource(R.drawable.jx_iotv_tv_tran_black_bg);
		return channelIndex;
	}
	
	private void clearChannelIndex(ChannelIndex channelIndex){
		channelIndex.setChannelName("");
		channelIndex.setChannelDay("");
		channelIndex.setChannelNum(-1);
		channelIndex.setTag(null);
	}

	public void setSelectPosition(int position) {
		Log.d(TAG, "setSelectPosition : " + position);
		if (position >= 0 && mChannelIndexs != null
				&& position < mChannelIndexs.size()) {
			if (mPosition >= 0) {
				ChannelIndex channelIndex = mChannelIndexs.get(mPosition);
				channelIndex.setChannelDayVisible(false);
				channelIndex.setBackgroundResource(R.drawable.jx_iotv_tv_tran_black_bg);
			}
			mPosition = position;
			ChannelIndex channelIndex = mChannelIndexs.get(mPosition);
			channelIndex.setChannelDayVisible(true);
			channelIndex.setBackgroundResource(R.drawable.jx_iotv_item_focus_bg);
		}
	}	
}
