package com.bestv.ott.jingxuan.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.ChannelIconMgr;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.model.IDataModel;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
/**
 * 第二列的adapter
 */
public class JingxuanChannelProgramAdapter extends BaseAdapter {
	private static final String TAG = "JingxuanChannelProgramAdapter";
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<Schedule> mScheduleList = null;
	private List<JingxuanChannelProgram> mChannelPrograms = null;
	private Channel mChannel = null;
	private Schedule mSchedule = null;
	private IControllerBase mController = null;
	private ChannelManager mChannelManager = null;
	private int mPosition = 0;
	private SimpleDateFormat mFullDateFormat = null;
	private SimpleDateFormat mMiniDateFormat = null;
	private boolean mIsFocus = false;
	private ChannelData mChannelData = null;
	/*add by xubin 2015.11.24 begin*/
	private IDataModel mDataMgr = null;
	/*add by xubin 2015.11.24 end*/
	
	public JingxuanChannelProgramAdapter(Context context, List<Schedule> schedules,
			Channel channel, IControllerBase controller) {
		Logger.d(TAG, "enter JingxuanChannelProgramAdapter");
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mScheduleList = schedules;
		mChannelPrograms = new ArrayList<JingxuanChannelProgram>();
		mController = controller;
		mChannel = channel;
		mChannelManager = ChannelManager.getInstance();
		mFullDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		mMiniDateFormat = new SimpleDateFormat("HH:mm");
		mChannelData = mChannelManager.getCurrentSchedule(mChannel);
		mSchedule = mController.getPlayingSchedule();
		/*add by xubin 2015.11.24 begin*/
		mDataMgr = mController.getIDataModel();
		/*add by xubin 2015.11.24 end*/
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
		if (mScheduleList == null) {
			Log.w(TAG, "mChannelList is null");
			return;
		}
		Log.d(TAG, "enter initViews , mScheduleList size = "+mScheduleList.size());
		try {
			refreshChannelData();

			for (Schedule schedule : mScheduleList) {
				Logger.d("zjt", "schedule name = "+schedule.getName());
				JingxuanChannelProgram channelProgram = (JingxuanChannelProgram) mLayoutInflater
						.inflate(R.layout.jx_jingxuan_iotv_channel_list_program_item, null);
				channelProgram = bindProgramData(channelProgram, schedule,mChannelData);
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
					JingxuanChannelProgram channelProgram = (JingxuanChannelProgram) mLayoutInflater
							.inflate(R.layout.jx_jingxuan_iotv_channel_list_program_item,null);
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
				JingxuanChannelProgram channelProgram = mChannelPrograms.get(position);
				channelProgram = bindProgramData(channelProgram, schedule,mChannelData);
				Log.d("Karel","getProgramName : "+ mChannelPrograms.get(position).getProgramName());
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
		if(mChannelPrograms != null && mChannelPrograms.size() > position){
			convertView = mChannelPrograms.get(position);
		}
		return convertView;
	}

	private JingxuanChannelProgram bindProgramData(JingxuanChannelProgram channelProgram,
			Schedule schedule, ChannelData channelData) {
		channelProgram.setProgramName(schedule.getName());
		channelProgram.setSubscribeUpdateVisible(false);
		channelProgram.setSubscribeInvalidVisible(false);
		channelProgram.setProgramChannelIconGone();
		String timeStr = converTimeStr(schedule.getStartTime());
		if(schedule.getChannelCode().equals(Constants.JX_LATEST_CHANNEL_CODE)){
			/*add by xubin 2015.11.24 begin*/
			channelProgram.setNextProgramName(null, false);
			/*add by xubin 2015.11.24 end*/
			channelProgram.setProgramTime(timeStr);
			channelProgram.setProgramChannelIcon(ChannelIconMgr.getChannelIcon("", schedule.getChannelIcon()));
			channelProgram.setProgramStatus(ChannelProgram.STATUS_LIVE);
		}else if(schedule.getChannelCode().equals(Constants.JX_SUBSCRP_CHANNEL_CODE)){
			if(schedule.getSubscribeStatus() == Schedule.SUBSCRIBE_STATUS_UPDATE){
				channelProgram.setSubscribeUpdateVisible(true);
			}else{
				channelProgram.setSubscribeUpdateVisible(false);
			}
			
			if(schedule.getSubscribeStatus() == Schedule.SUBSCRIBE_STATUS_INVALID){
				channelProgram.setSubscribeInvalidVisible(true);
			}else{
				channelProgram.setSubscribeInvalidVisible(false);
			}
			

			channelProgram.setProgramStatus(ChannelProgram.STATUS_RECORD);
		}else{
			channelProgram.setProgramDate( IOTV_Date
					.converyyyyMMddHHmmss2yyyyMMdd(schedule.getStartTime()));
			channelProgram.setProgramStatus(ChannelProgram.STATUS_RECORD);
		}
		if (mSchedule != null
				&& mChannel.getType() == Constants.IOTV_PLAY_LIVE_SOURCE) {

			long startTime = convertTime(schedule.getStartTime());
			long endTime = convertTime(schedule.getEndTime());
			long curTime = mController.getCurrentPlayingTime();
			channelProgram.setProgressData(startTime, endTime, curTime);
			if (mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE) {
				if (mSchedule.getScheduleIndex() == schedule.getScheduleIndex()
						&& mSchedule.getChannelCode().equalsIgnoreCase(
								schedule.getChannelCode())) {
					channelProgram.setProgressViewVisible(true, true);
				}else{
					channelProgram.setProgressViewVisible(false, false);
				}
			}
		}
		//channelProgram.setBackgroundResource(R.drawable.jx_iotv_tv_tran_black_bg);
		return channelProgram;
	}

	public void setIsFocus(boolean isFocused) {
		Logger.d(TAG, "enter setIsFocus ,  isFocused = "+isFocused+" , mIsFocus = "+mIsFocus);
		if (mIsFocus != isFocused) {
			mIsFocus = isFocused;
			setSelectPosition(mPosition);
		}
	}

	public void setSelectPosition(int position) {
		Logger.d(TAG, "enter channelProgram setSelectPosition , position = "+position+", mIsFocus = " 
	             + mIsFocus+", mPosition = "+mPosition);
		if (position < 0){
			return;
		}
		refreshChannelData();
		
		if (position >= 0 && mChannelPrograms != null
				&& position < mChannelPrograms.size()) {
			if (mPosition >= 0 && mPosition < mChannelPrograms.size()) {
				JingxuanChannelProgram channelProgram = mChannelPrograms.get(mPosition);
				channelProgram.setBackgroundResource(android.R.color.transparent);
				channelProgram.setProgressViewVisible(false);
			}
			/*add by xubin 2015.11.24 begin*/
			updateViews();
			/*add by xubin 2015.11.24 end*/
			if (mIsFocus) {
				mPosition = position;
				JingxuanChannelProgram channelProgram = mChannelPrograms.get(mPosition);
				if (channelProgram.getProgramStatus() != ChannelProgram.STATUS_FUTURE) {
					channelProgram.setBackgroundResource(R.drawable.jx_focus_bg);
				}
				channelProgram.setProgressViewVisible(true);
				
				/*add by xubin 2015.11.24 begin*/
				String channelCode = null;
				if(mScheduleList != null && mPosition < mScheduleList.size()){
					channelCode = mScheduleList.get(mPosition).getChannelCode();
				}
				Logger.d(TAG, "***channel code is:" + channelCode);
				if(channelCode != null && channelCode.equalsIgnoreCase(Constants.JX_LATEST_CHANNEL_CODE)){
					Schedule currentSchedule = null;
					if(mScheduleList != null && mPosition < mScheduleList.size()){
						currentSchedule = mScheduleList.get(mPosition);
					}
					String scheduleName = null;
					if(currentSchedule != null){
						scheduleName = currentSchedule.getCompleteName() == null ? 
								currentSchedule.getName():currentSchedule.getCompleteName();
					}
					if(scheduleName == null){
						scheduleName = "";
					}
					
					Schedule nextSchedule = null;
					int scheduleIndex = 0;
					if(currentSchedule != null){
						scheduleIndex = currentSchedule.getScheduleIndex();
					}
					if(mDataMgr != null){
						nextSchedule = mDataMgr.getNextScheduleOfChannel(scheduleIndex);
					}
					String nextScheduleName = null;
					if(nextSchedule != null){
						//nextScheduleName = nextSchedule.getName();
						nextScheduleName = nextSchedule.getCompleteName() == null ? 
								nextSchedule.getName() : nextSchedule.getCompleteName();
					}
					if(nextScheduleName == null){
						nextScheduleName = "";
					}
					channelProgram.setProgramName(mContext.getString(R.string.current_schedule) + scheduleName);
					channelProgram.setNextProgramName(mContext.getString(R.string.sub_schedule) 
							+ nextScheduleName, mIsFocus);
					/*add by xubin 2015.11.24 end*/
				}
			}else{
				Logger.d(TAG, "...zjt...test...");
				if(JingxuanChannelListView.mFocusedView!=JingxuanChannelListView.FOCUSED_CHANNEL){
					mPosition = position;
					Logger.d(TAG, "...zjt...mPosition = "+mPosition);
					JingxuanChannelProgram channelProgram = mChannelPrograms.get(mPosition);
					channelProgram.setBackgroundResource(R.drawable.jx_program_selected_bg);
				}
			}
		} else {
			if (mPosition >= 0 && mPosition < mChannelPrograms.size()) {
				mPosition = position;
				JingxuanChannelProgram channelProgram = mChannelPrograms.get(mPosition);
				channelProgram.setBackgroundResource(android.R.color.transparent);
				channelProgram.setProgressViewVisible(false);
			}
		}
	}

	private int getProgramStatus(int position, boolean selected,
			ChannelData channelData) {
		int status = JingxuanChannelProgram.STATUS_RECORD;
		if (mChannelPrograms != null && mScheduleList != null
				&& mScheduleList.size() > position) {
			Schedule schedule = mScheduleList.get(position);
			String timeStr = converTimeStr(schedule.getStartTime());
			long time = convertTime(schedule.getStartTime());
			if (time < channelData.getCurTime()) {
				if (selected) {
					status = JingxuanChannelProgram.STATUS_RECORD;
				} else {
					status = JingxuanChannelProgram.STATUS_UNFOCUS;
				}
			} else if (time > channelData.getCurTime()) {
				status = JingxuanChannelProgram.STATUS_FUTURE;
			}
			if (channelData.getStartTimeStr() != null
					&& channelData.getEndTimeStr() != null
					&& channelData != null
					&& channelData.getStartTimeStr().equalsIgnoreCase(timeStr)) {
				status = JingxuanChannelProgram.STATUS_LIVE;
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
				//mSchedule = null;
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
			for (JingxuanChannelProgram channelProgram : mChannelPrograms) {
				channelProgram.releaseData();
			}
		}
	}

	private String getToday() {
		return IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
	}
	
	public List<JingxuanChannelProgram> getChannelPrograms(){
		return mChannelPrograms;
	}
	
	public void clearAllSubscribeUpdateMark(){
		for(JingxuanChannelProgram c : mChannelPrograms){
			c.setSubscribeInvalidVisible(false);
			c.setSubscribeUpdateVisible(false);
		}
		notifyDataSetChanged();
	}
}
