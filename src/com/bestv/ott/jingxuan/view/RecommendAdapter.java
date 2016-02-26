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
import com.bestv.ott.jingxuan.iotvlogservice.utils.FileUtils;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;

public class RecommendAdapter extends BaseAdapter implements
		OnProgramProgressFinishListener {
	private static final String TAG = "RecommendAdapter";
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<Channel> mRecommendChannels = null;
	private List<ChannelView> mChannelViews = null;
	private IControllerBase mController = null;

	public RecommendAdapter(Context context, List<Channel> channels,
			IControllerBase controller) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mRecommendChannels = channels;
		mChannelViews = new ArrayList<ChannelView>();
		mController = controller;
		Log.d(TAG, "mRecommendChannels : " + mRecommendChannels);
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
		Channel channel = null;
		if (mRecommendChannels != null) {
			channel = mRecommendChannels.get(position);
		}
		return channel;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private void initViews() {
		Log.d(TAG, "enter initViews");
		if (mRecommendChannels == null) {
			Log.w(TAG, "mRecommendChannels is null");
			return;
		}

		try {
			for (Channel channel : mRecommendChannels) {
				ChannelView channelView = (ChannelView) mLayoutInflater
						.inflate(R.layout.jx_iotv_recommend_item, null);
				channelView = bindChannelData(channelView, channel);
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
		mRecommendChannels = null;
	}

	@Override
	public void onFinish(ChannelView channelView) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onFinish");
		if (channelView != null && channelView.getTag() != null
				&& channelView.getTag() instanceof Channel) {
			Channel channel = (Channel) channelView.getTag();
			bindChannelData(channelView, channel);
		}
	}

	private ChannelView bindChannelData(ChannelView channelView, Channel channel) {
		if (mController == null) {
			Log.w(TAG, "mController is null");
			return null;
		}
		ChannelData channelData = ChannelManager.getInstance()
				.getCurrentSchedule(channel);
		String iconPath = channelData.getChannelIconPath();
		if (utils.isNotNull(iconPath) && FileUtils.fileExisted(iconPath)) {
			Bitmap icon = BitmapFactory.decodeFile(iconPath);
			if (icon != null) {
				channelView.setChannelIcon(icon);
			}
		}

		long curTime = channelData.getCurTime();
		long endTime = channelData.getEndTime();
		long startTime = channelData.getStartTime();
		String startTimeStr = channelData.getStartTimeStr();
		String endTimeStr = channelData.getEndTimeStr();
		channelView.setProgramName(utils.safeString(channelData
				.getScheduleName()));
		channelView.setChannelIcon(ChannelIconMgr.getChannelIcon(channelData.getChannelName(), channelData.getChannelIconPath()));
		channelView.setTag(channel);
		channelView.setOnProgramProgressFinishListener(this);
		String channelName = channel.getChannelName();
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
