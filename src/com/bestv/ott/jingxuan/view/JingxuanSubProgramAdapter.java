package com.bestv.ott.jingxuan.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.Logger;

public class JingxuanSubProgramAdapter extends BaseAdapter{
	private static final String TAG = "JingxuanSubProgramAdapter";
	private List<Schedule> mSubSchedules = null;
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<JingxuanSubProgram> mSubRrograms = null;
	private boolean mIsFocus = false;
	private int mPosition = 0;

	public JingxuanSubProgramAdapter(Context context, List<Schedule> subSchedules) {
		mContext = context;
		mSubSchedules = subSchedules;
		mLayoutInflater = LayoutInflater.from(mContext);
		mSubRrograms = new ArrayList<JingxuanSubProgram>();
		initViews();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mSubSchedules == null ? 0 : mSubSchedules.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mSubSchedules != null) {
			return mSubSchedules.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	//add shensong
	public View getView(int position){
		return mSubRrograms.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null && mSubRrograms.size() > position) {
			convertView = mSubRrograms.get(position);
		}
		return convertView;
	}

	private void initViews() {
		Log.d(TAG, "enter initViews");
		if (mSubSchedules == null) {
			Log.w(TAG, "mChannelList is null");
			return;
		}
		try {
			for (Schedule subSchedule : mSubSchedules) {
				JingxuanSubProgram view = (JingxuanSubProgram) mLayoutInflater.inflate(
						R.layout.jx_jingxuan_iotv_channel_list_sub_program_item, null);
				view.setProgramName(subSchedule.getName() + subSchedule.getNamePostfix());
				TextView subProgram = (TextView) view
						.findViewById(R.id.subProgram);
				if(subSchedule.getSubscribeStatus() == Schedule.SUBSCRIBE_STATUS_UPDATE){
					subProgram.setTextColor(Color.parseColor("#FF256C1A"));
				}else{
					subProgram.setTextColor(mContext.getResources().getColor(android.R.color.white));
				}
				mSubRrograms.add(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resetBackground() {
		if (mSubRrograms != null) {
			for (int i = 0; i < mSubRrograms.size(); i++) {
				View view = mSubRrograms.get(i);
				TextView subProgram = (TextView) view
						.findViewById(R.id.subProgram);
				Schedule subSchedule = mSubSchedules.get(i);
				if(subSchedule.getSubscribeStatus() == Schedule.SUBSCRIBE_STATUS_UPDATE){
//					subProgram.setTextColor(Color.parseColor("#FF256C1A"));
					subProgram.setTextColor(mContext.getResources().getColor(R.color.channel_index));
				}else{
					subProgram.setTextColor(mContext.getResources().getColor(android.R.color.white));
				}
				view.setBackgroundColor(mContext.getResources()
						.getColor(android.R.color.transparent));
			}
		}
	}

	public void setSelectPosition(int position) {
		Logger.d(TAG, "enter subProgram setSelectPosition , position = "+position+" , mIsFocus = "+mIsFocus);
		if (position < 0){
			return;
		}
		mPosition = position;
		resetBackground();
		if (mPosition >= 0 && mSubRrograms != null
				&& mPosition < mSubRrograms.size()) {
			View view = mSubRrograms.get(mPosition);
			TextView subProgram = (TextView) view
					.findViewById(R.id.subProgram);
			view.setBackgroundResource(R.drawable.jx_datefocus_bg);
			subProgram.setTextColor(Color.WHITE);
			if(mIsFocus){//没有在选中状态
				view.getBackground().setAlpha(255);
			}else{
				view.getBackground().setAlpha(122);
			}
		}
	}
	public void setIsFocus(boolean isFocused) {
		Logger.d(TAG, "enter subProgram setIsFocus , isFocused = "+isFocused+" , mIsFocus = "+mIsFocus);
		if (mIsFocus != isFocused) {
			mIsFocus = isFocused;
			setSelectPosition(mPosition);
		}
	}
}
