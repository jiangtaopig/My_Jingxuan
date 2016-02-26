package com.bestv.ott.jingxuan.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bestv.jingxuan.R;
import com.bestv.ott.framework.utils.utils;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.ChannelIconMgr;
import com.bestv.ott.jingxuan.data.RecommendItem;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.iotvlogservice.utils.FileUtils;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;

public class JxRecommendAdapter extends BaseAdapter implements
		OnProgramProgressFinishListener {
	private static final String TAG = "RecommendAdapter";
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<RecommendItem> mRecommendItems = null;
	private List<ChannelView> mChannelViews = null;
	private IControllerBase mController = null;

	public JxRecommendAdapter(Context context, List<RecommendItem> recommendItem,
			IControllerBase controller) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mRecommendItems = recommendItem;
		mChannelViews = new ArrayList<ChannelView>();
		mController = controller;
		Log.d(TAG, "mRecommendItems : " + mRecommendItems);
		initViews();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mChannelViews == null ? 0 : mChannelViews.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		RecommendItem recommendItem = null;
		if (mRecommendItems != null) {
			recommendItem = mRecommendItems.get(position);
		}
		return recommendItem;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private void initViews() {
		Log.d(TAG, "enter initViews");
		if (mRecommendItems == null) {
			Log.w(TAG, "mRecommendChannels is null");
			return;
		}

		try {
			for (RecommendItem recommendItem : mRecommendItems) {
				ChannelView channelView = (ChannelView) mLayoutInflater
						.inflate(R.layout.jx_iotv_recommend_item, null);
				channelView = bindChannelData(channelView, recommendItem);
				Log.d(TAG, "channelView : " + channelView);
				if (channelView != null) {
					mChannelViews.add(channelView);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null && mChannelViews != null) {
			convertView = mChannelViews.get(position);
		}
		return convertView;
	}

	/**
	 * 生成时间文字
	 * 
	 * @param strTime
	 * @return
	 */
	private String getTimeRange(String startTime, String endTime) {
		StringBuffer range = new StringBuffer(12);
		if (utils.isNull(startTime) || utils.isNull(endTime)) {
			range.append("");
		} else {
			range.append(utils.safeString(startTime)).append("-")
					.append(utils.safeString(endTime));
		}

		return range.toString();
	}

	public void releaseData() {
		Log.d(TAG, "enter releaseData");
		if (mChannelViews != null) {
			mChannelViews = null;
		}
		mRecommendItems = null;
	}

	@Override
	public void onFinish(ChannelView channelView) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onFinish");
		if (channelView != null && channelView.getTag() != null
				&& channelView.getTag() instanceof Channel) {
			RecommendItem recommendItem = (RecommendItem) channelView.getTag();
			bindChannelData(channelView, recommendItem);
		}
	}

	private ChannelView bindChannelData(ChannelView channelView, RecommendItem recommendItem) {
		if (mController == null) {
			Log.w(TAG, "mController is null");
			return null;
		}
		if(recommendItem.getSchedule().getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
			channelView.setChannelIcon(ChannelIconMgr.getChannelIcon("", recommendItem.getSchedule().getChannelIcon()));			
		}
		String channelName = recommendItem.getChannel().getChannelName();
		channelView.setProgramName(recommendItem.getSchedule().getName());
		channelView.setTag(recommendItem);
		channelView.setOnProgramProgressFinishListener(this);
		
		channelView.setChannelName(channelName);
		if (utils.isNull(channelName)) {
			channelView.setBackgroundColor(mContext.getResources().getColor(
					android.R.color.transparent));
		} else {
			channelView
					.setBackgroundResource(R.drawable.jx_iotv_grid_channel_view_selector);
		}

		return channelView;
	}
}
