package com.bestv.ott.jingxuan.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.IOTV_Date;

public class ChannelProgramAdapter extends BaseAdapter {
	private static final String TAG = "ChannelProgramAdapter";
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<Schedule> mScheduleList = null;
	private List<ChannelProgram> mChannelPrograms = null;
	private Channel mChannel = null;
	private Schedule mSchedule = null;
	private IControllerBase mController = null;
	private ChannelManager mChannelManager = null;
	private int mPosition = 0;
	private SimpleDateFormat mFullDateFormat = null;
	private SimpleDateFormat mMiniDateFormat = null;
	private boolean mIsFocus = false;
	private ChannelData mChannelData = null;

	public ChannelProgramAdapter(Context context, List<Schedule> schedules,
			Channel channel, IControllerBase controller) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mScheduleList = schedules;
		mChannelPrograms = new ArrayList<ChannelProgram>();
		mController = controller;
		mChannel = channel;
		mChannelManager = ChannelManager.getInstance();
		mFullDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		mMiniDateFormat = new SimpleDateFormat("HH:mm");
		mChannelData = mChannelManager.getCurrentSchedule(mChannel);
		mSchedule = mController.getPlayingSchedule();
		if (mController.getCurrentIOTVType() != Constants.IOTV_PLAY_LIVE_SOURCE
				&& mSchedule != null) {
			Log.d(TAG, "mController.getCurrentChannel().getChannelNo() : "
					+ mController.getCurrentChannel().getChannelNo());
			Log.d(TAG, "mChannel.getChannelNo() : " + mChannel.getChannelNo());

			if (!mController.getCurrentChannel().getChannelNo()
					.equalsIgnoreCase(mChannel.getChannelNo())) {
				mSchedule = null;
				Log.d(TAG, "Record is not in this channel");
			} else {
				Log.d(TAG, "Have a record playing in this channel");
			}
		} else {
			Log.d(TAG, "Now living");
		}
		initViews();
		Log.d(TAG, "channel : " + mChannel.getChannelName());
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mScheduleList == null ? 0 : mScheduleList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mScheduleList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private void initViews() {
		Log.d(TAG, "enter initViews");
		if (mScheduleList == null) {
			Log.w(TAG, "mChannelList is null");
			return;
		}
		try {
			refreshChannelData();

			for (Schedule schedule : mScheduleList) {
				ChannelProgram channelProgram = (ChannelProgram) mLayoutInflater
						.inflate(R.layout.jx_iotv_channel_list_program_item, null);
				channelProgram = bindProgramData(channelProgram, schedule,
						mChannelData);
				if (channelProgram != null) {
					mChannelPrograms.add(channelProgram);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateViews() {
		Log.d(TAG, "enter updateViews");
		if (mScheduleList == null) {
			Log.w(TAG, "mScheduleList is null");
			return;
		}
		try {
			refreshChannelData();
			int channelSize = mChannelPrograms.size();
			int scheduleSize = mScheduleList.size();
			Log.d("Karel", "channelSize : " + channelSize);
			Log.d("Karel", "scheduleSize : " + scheduleSize);
			if (channelSize < scheduleSize) {
				for (int i = 0; i < (scheduleSize - channelSize); i++) {
					ChannelProgram channelProgram = (ChannelProgram) mLayoutInflater
							.inflate(R.layout.jx_iotv_channel_list_program_item,
									null);
					mChannelPrograms.add(channelProgram);
				}
				Log.d("Karel", "1mChannelPrograms.size : " + mChannelPrograms.size());
			} else if (channelSize > scheduleSize) {
//				for (int i = 0; i < (channelSize - scheduleSize); i++) {
//					mChannelPrograms.remove(mChannelPrograms.size() - 1);
//				}
			}
			int position = 0;
			for (Schedule schedule : mScheduleList) {
				ChannelProgram channelProgram = mChannelPrograms.get(position);
				channelProgram = bindProgramData(channelProgram, schedule,
						mChannelData);
				Log.d("Karel",
						"getProgramName : "
								+ mChannelPrograms.get(position)
										.getProgramName());
				position++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// if (convertView == null && mChannelPrograms != null) {
		convertView = mChannelPrograms.get(position);
		return convertView;
	}

	private ChannelProgram bindProgramData(ChannelProgram channelProgram,
			Schedule schedule, ChannelData channelData) {
		channelProgram.setProgramName(schedule.getName());
		String timeStr = converTimeStr(schedule.getStartTime());
		long time = convertTime(schedule.getStartTime());
		if (time < channelData.getCurTime()) {
			channelProgram.setProgramStatus(ChannelProgram.STATUS_UNFOCUS);
		} else if (time > channelData.getCurTime()) {
			channelProgram.setProgramStatus(ChannelProgram.STATUS_FUTURE);
		}
		channelProgram.setProgramTime(timeStr);

		if (channelData.getStartTimeStr() != null
				&& channelData.getEndTimeStr() != null && channelData != null
				&& channelData.getStartTimeStr().equalsIgnoreCase(timeStr)) {
			boolean isToadyProgram = true;
			if (mController.getPlayingSchedule() != null) {
				isToadyProgram = mController.getPlayingSchedule()
						.getStartTime().startsWith(getToday());
			}
			boolean isShowProgress = !(mController.getCurrentChannel()
					.getChannelNo().equalsIgnoreCase(mChannel.getChannelNo())
					&& (mController.getCurrentIOTVType() == Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE) && isToadyProgram);
			if (isShowProgress) {
				channelProgram.setProgressData(channelData.getStartTime(),
						channelData.getEndTime(), channelData.getCurTime());
			}
			channelProgram.setProgramStatus(ChannelProgram.STATUS_LIVE);
		}
		if (mSchedule != null
				&& mSchedule.getStartTime().equalsIgnoreCase(
						schedule.getStartTime())
				&& mController.getCurrentIOTVType() == Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE) {
			long startTime = convertTime(schedule.getStartTime());
			long endTime = convertTime(schedule.getEndTime());
			long curTime = mController.getCurrentPlayingTime();
			channelProgram.setProgressData(startTime, endTime, curTime);
			channelProgram.setProgramStatus(ChannelProgram.STATUS_RECORD);
		}
		channelProgram.setBackgroundResource(R.drawable.jx_iotv_tv_tran_black_bg);
		return channelProgram;
	}

	public void setIsFocus(boolean isFocused) {
		if (mIsFocus != isFocused) {
			mIsFocus = isFocused;
			setSelectPosition(mPosition);
		}
	}

	public void setSelectPosition(int position) {
		if (position < 0){
			return;
		}
		
		refreshChannelData();
		if (position >= 0 && mChannelPrograms != null
				&& position < mChannelPrograms.size()) {
			if (mPosition >= 0 && mPosition < mChannelPrograms.size()) {
				ChannelProgram channelProgram = mChannelPrograms.get(mPosition);
				channelProgram
						.setBackgroundResource(R.drawable.jx_iotv_tv_tran_black_bg);
				channelProgram.setProgramStatus(getProgramStatus(mPosition,
						false, mChannelData));
			}
			if (mIsFocus) {
				mPosition = position;
				ChannelProgram channelProgram = mChannelPrograms.get(mPosition);
				if (channelProgram.getProgramStatus() != ChannelProgram.STATUS_FUTURE) {
					channelProgram
							.setBackgroundResource(R.drawable.jx_iotv_item_focus_bg);
				}
				channelProgram.setProgramStatus(getProgramStatus(mPosition,
						true, mChannelData));
			}
		} else {
			if (mPosition >= 0 && mPosition < mChannelPrograms.size()) {
				mPosition = position;
				ChannelProgram channelProgram = mChannelPrograms.get(mPosition);
				channelProgram
						.setBackgroundResource(R.drawable.jx_iotv_tv_tran_black_bg);
				channelProgram.setProgramStatus(getProgramStatus(mPosition,
						false, mChannelData));
			}
		}
	}

	private int getProgramStatus(int position, boolean selected,
			ChannelData channelData) {
		int status = ChannelProgram.STATUS_RECORD;
		if (mChannelPrograms != null && mScheduleList != null
				&& mScheduleList.size() > position) {
			Schedule schedule = mScheduleList.get(position);
			String timeStr = converTimeStr(schedule.getStartTime());
			long time = convertTime(schedule.getStartTime());
			if (time < channelData.getCurTime()) {
				if (selected) {
					status = ChannelProgram.STATUS_RECORD;
				} else {
					status = ChannelProgram.STATUS_UNFOCUS;
				}
			} else if (time > channelData.getCurTime()) {
				status = ChannelProgram.STATUS_FUTURE;
			}
			if (channelData.getStartTimeStr() != null
					&& channelData.getEndTimeStr() != null
					&& channelData != null
					&& channelData.getStartTimeStr().equalsIgnoreCase(timeStr)) {
				status = ChannelProgram.STATUS_LIVE;
			}
			if (mSchedule != null
					&& mSchedule.getStartTime().equalsIgnoreCase(
							schedule.getStartTime()) && mController.getCurrentIOTVType() == Constants.IOTV_PLAYTYPE_LOOKBACK_SOURCE) {
				status = ChannelProgram.STATUS_RECORD;
			}
		}
		return status;
	}

	/**
	 * yyyyMMddHHmmss转化为HH:mm
	 * 
	 * @param strTime
	 * @return
	 */
	private String converTimeStr(String time) {
		String str = "";
		try {
			Date date = mFullDateFormat.parse(time);
			str = mMiniDateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 转换日期到long
	 * 
	 * @param strTime
	 * @return
	 */
	private long convertTime(String strTime) {
		long time = 0L;
		try {
			Date date = mFullDateFormat.parse(strTime);
			time = date.getTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}

	private void refreshChannelData() {
		Date endDate = new Date(mChannelData.getEndTime());
		Date curDate = IOTV_Date.getDate();
		String endStr = IOTV_Date.getDateFormatNormal(endDate);
		String curStr = IOTV_Date.getDateFormatNormal(curDate);
		if (mChannelData != null && endDate.before(curDate)
				&& endStr.equals(curStr)) {
			Log.d("Karel", "refreshChannelData");
			mChannelData = mChannelManager.getCurrentSchedule(mChannel);
		}
	}

	public void refreshListData(List<Schedule> schedules, Channel channel) {
		Log.d("Karel", "enter refreshDate");
		mScheduleList = schedules;
		mChannel = channel;
		mChannelData = mChannelManager.getCurrentSchedule(mChannel);
		mSchedule = mController.getPlayingSchedule();
		if (mController.getCurrentIOTVType() != Constants.IOTV_PLAY_LIVE_SOURCE
				&& mSchedule != null) {
			Log.d(TAG, "mController.getCurrentChannel().getChannelNo() : "
					+ mController.getCurrentChannel().getChannelNo());
			Log.d(TAG, "mChannel.getChannelNo() : " + mChannel.getChannelNo());

			if (!mController.getCurrentChannel().getChannelNo()
					.equalsIgnoreCase(mChannel.getChannelNo())) {
				mSchedule = null;
				Log.d(TAG, "Record is not in this channel");
			} else {
				Log.d(TAG, "Have a record playing in this channel");
			}
		} else {
			Log.d(TAG, "Now living");
		}
		// if(mChannelPrograms != null && mChannelPrograms.size() ==
		// mScheduleList.size()){
		Log.d("Karel", "from list");
		updateViews();
		// notifyDataSetChanged();
		// }
		// else{
		// Log.d("Karel", "from xml");
		// mChannelPrograms = new ArrayList<ChannelProgram>();
		// initViews();
		// }

	}

	public void releaseData() {
		if (mChannelPrograms != null) {
			for (ChannelProgram channelProgram : mChannelPrograms) {
				channelProgram.releaseData();
			}
		}
	}

	private String getToday() {
		return IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
	}
}
