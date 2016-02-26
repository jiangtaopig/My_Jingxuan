package com.bestv.ott.jingxuan.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.Logger;
import com.shcmcc.tools.GetSysInfo;

public class JingxuanChannelIndexAdapter extends BaseAdapter {
	private static final int MAX_PAGE_ITEMS = 8;
	
	private static final String TAG = "JingxuanChannelIndexAdapter";
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<JingxuanChannelIndex> mChannelIndexs = null;
	private IControllerBase mController = null;
	private String mDateStr = null;
	private int mPosition = 0;
	private int mListSize = MAX_PAGE_ITEMS;

	private boolean mHaveUpdateSchedules;

	private boolean mIsFocus;
	
	private ArrayList<String> mHightChannelArr;

	public JingxuanChannelIndexAdapter(Context context, IControllerBase controller, String date) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mChannelIndexs = new ArrayList<JingxuanChannelIndex>();
		mController = controller;

		initViews();
		initHightChannels();
	}
	
	private void initHightChannels(){
		String[] highLightContents=mContext.getResources().getStringArray(R.array.channelindex_hight);
		mHightChannelArr=new ArrayList<String>();
		Collections.addAll(mHightChannelArr, highLightContents);
	}
	
	public void updateListContent(List<Channel> newlist, boolean haveUpdateSchedules){
		int indexsize = mChannelIndexs.size();
		int newlistsize = newlist.size();
		Logger.d(TAG, "enter updateListContent , indexsize = "+indexsize+" , newlistsize = "+newlistsize+" ,mPosition = "+mPosition);
		mListSize = newlistsize;
		mHaveUpdateSchedules = haveUpdateSchedules;
		for (int i = 0; i < indexsize; i++){
			JingxuanChannelIndex ci = mChannelIndexs.get(i);
			clearChannelIndex(ci);
			
			if (i < newlistsize){
				Channel channel = newlist.get(i);
				if(channel!=null&&channel.getChannelName()!=null&&mHightChannelArr.contains(channel.getChannelName()) && i != mPosition){//add shensong
					ci.getChannnelNameTView().setTextColor(mContext.getResources().getColor(R.color.channel_index));
				}else{
					ci.getChannnelNameTView().setTextColor(Color.WHITE);
				}
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
		Log.d(TAG, "enter initViews");
		try {
			for (int i = 0; i < MAX_PAGE_ITEMS; i++) {
				JingxuanChannelIndex channelIndex = (JingxuanChannelIndex) mLayoutInflater
						.inflate(R.layout.jx_jingxuan_iotv_channel_list_index_item, null);
				//channelIndex = bindChannelData(channelIndex, channel);
				//Log.d(TAG, "Channel : " + channel.getChannelName());
				if (channelIndex != null) {
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

	private JingxuanChannelIndex bindChannelData(JingxuanChannelIndex channelIndex,
			Channel channel) {
		if (channel != null) {
			String channelName = channel.getChannelName();
			channelIndex.setChannelName(channelName);
			Log.d("harish", "channel name = " + channelName + " day = " + channel.getDay());
			channelIndex.setChannelDay(channel.getDay());
//			int channelNum = Integer.parseInt(channel.getChannelNo());
//			channelIndex.setChannelNum(channelNum);
			channelIndex.setTag(channel);
			if(channel.getChannelCode().equalsIgnoreCase(Constants.JX_SUBSCRP_CHANNEL_CODE) && mHaveUpdateSchedules){
				channelIndex.setSubscribeUpdateVisible(true);
			}else{
				channelIndex.setSubscribeUpdateVisible(false);
			}
		}
		//channelIndex.setBackgroundResource(R.drawable.jx_iotv_tv_tran_black_darker_bg);
		return channelIndex;
	}
	
	private void clearChannelIndex(JingxuanChannelIndex channelIndex){
		channelIndex.setChannelName("");
		channelIndex.setChannelDay("");
		channelIndex.setChannelNum(-1);
		channelIndex.setTag(null);
	}

	public void setSelectPosition(int position) {
		Log.d(TAG, "setSelectPosition : " + position+", mPosition = "+mPosition+" ,mIsFocus = "+mIsFocus);
		if (position >= 0 && mChannelIndexs != null
				&& position < mChannelIndexs.size()) {
			if (mPosition >= 0) {
				JingxuanChannelIndex channelIndex = mChannelIndexs.get(mPosition);
				channelIndex.setBackgroundResource(android.R.color.transparent);
				String channelName=channelIndex.getChannnelNameTView().getText().toString();
				if(mHightChannelArr.contains(channelName)){
					channelIndex.getChannnelNameTView().setTextColor(mContext.getResources().getColor(R.color.channel_index));
				}else{
					channelIndex.getChannnelNameTView().setTextColor(Color.WHITE);
				}
			}
			if (mIsFocus) {
				mPosition = position;
				JingxuanChannelIndex channelIndex = mChannelIndexs.get(mPosition);
				channelIndex.setBackgroundResource(R.drawable.jx_focus_bg);
				channelIndex.getChannnelNameTView().setTextColor(Color.WHITE);
				resetChanelsColor(position);
			}else{
				mPosition = position; 
				JingxuanChannelIndex channelIndex = mChannelIndexs.get(mPosition);
				channelIndex.setBackgroundResource(R.drawable.jx_channel_selected_bg);
				String channelName=channelIndex.getChannnelNameTView().getText().toString();
				if(mHightChannelArr.contains(channelName)){
					channelIndex.getChannnelNameTView().setTextColor(mContext.getResources().getColor(R.color.channel_index));
				}else{
					channelIndex.getChannnelNameTView().setTextColor(Color.WHITE);
				}
			}
//			mPosition = position;
//			JingxuanChannelIndex channelIndex = mChannelIndexs.get(mPosition);
//			Channel channel = (Channel)mChannelIndexs.get(position).getTag();
//			if(channel != null){
//				if(channel.getChannelCode().equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)){
//					channelIndex.setBackgroundResource(R.drawable.jx_iotv_item_focus_bg_green);
//				}else{
//					channelIndex.setBackgroundResource(R.drawable.jx_iotv_item_focus_bg);
//				}
//			}
		}
	}	
	
	/**
	 * 
	 * 重新设置频道列表的字体颜色
	 * @param curPosition
	 */
	private void resetChanelsColor(int curPosition){
		for(int i=0;i<mChannelIndexs.size();i++){
			if(i==curPosition) continue;
			JingxuanChannelIndex channelIndex=mChannelIndexs.get(i);
			String channelName=channelIndex.getChannnelNameTView().getText().toString();
			if(mHightChannelArr.contains(channelName)){
				channelIndex.getChannnelNameTView().setTextColor(mContext.getResources().getColor(R.color.channel_index));
			}else{
				channelIndex.getChannnelNameTView().setTextColor(Color.WHITE);
			}
		}
	}
	
	public void setSubscribeUpdateVisible(int position, boolean visible){
		if(position < mChannelIndexs.size()){
			JingxuanChannelIndex channelIndex = mChannelIndexs.get(position);
			channelIndex.setSubscribeUpdateVisible(visible);
		}
	}

	public void setIsFocus(boolean isFocused) {
		Logger.d(TAG, "enter setIsFocus , isFocused = "+isFocused);
		if (mIsFocus != isFocused) {
			mIsFocus = isFocused;
			setSelectPosition(mPosition);
		}
	}
}
