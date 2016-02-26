package com.bestv.ott.jingxuan.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.util.DateUtils;
import com.bestv.ott.jingxuan.util.IOTV_Date;

public class ChannelDateAdapter extends BaseAdapter{
	private static final String TAG = "ChannelDateAdapter";
	private List<String> mStrDates = null;
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<ViewGroup> mChannelDates = null;

	public ChannelDateAdapter(Context context, List<String> dates) {
		mContext = context;
		mStrDates = dates;
		mLayoutInflater = LayoutInflater.from(mContext);
		mChannelDates = new ArrayList<ViewGroup>();
		initViews();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mStrDates == null ? 0 : mStrDates.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mStrDates != null) {
			return mStrDates.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null && mChannelDates.size() > position) {
			convertView = mChannelDates.get(position);
		}
		return convertView;
	}

	private void initViews() {
		Log.d(TAG, "enter initViews");
		if (mStrDates == null) {
			Log.w(TAG, "mChannelList is null");
			return;
		}
		try {
			for (String strDate : mStrDates) {
				ViewGroup view = (ViewGroup) mLayoutInflater.inflate(
						R.layout.jx_iotv_channel_list_date_item, null);
				TextView txtDateStr = (TextView) view
						.findViewById(R.id.channel_date_str);
				TextView txtDateNum = (TextView) view
						.findViewById(R.id.channel_date_num);
				txtDateStr.setText(converTimeStr(strDate));
				txtDateNum.setText(converTimeNum(strDate));
				mChannelDates.add(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resetDateType() {
		if (mChannelDates != null) {
			for (int i = 0; i < mChannelDates.size(); i++) {
				View view = mChannelDates.get(i);
				TextView txtDateStr = (TextView) view
						.findViewById(R.id.channel_date_str);
				TextView txtDateNum = (TextView) view
						.findViewById(R.id.channel_date_num);
				txtDateStr.setTextColor(mContext.getResources().getColor(android.R.color.white));
				txtDateNum.setTextColor(mContext.getResources().getColor(android.R.color.white));
				view.setBackgroundColor(mContext.getResources()
						.getColor(android.R.color.transparent));
			}
		}
	}
	
	/**
	 * yyyyMMdd转化为MM/dd
	 * 
	 * @param strTime
	 * @return
	 */
	private String converTimeNum(String time) {
		String str = "";
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			if(IOTV_Date.getDateFormatNormal(IOTV_Date.getDate()).equals(time)){
				str = getString(R.string.tv_today_short);
			}else if(IOTV_Date.getDateFormatNormal(IOTV_Date.instance().getDateYesterday()).equals(time)){
				str = getString(R.string.tv_yeaterday_short);
			}else{
				SimpleDateFormat strFormat = new SimpleDateFormat("dd");
				Date date = dateFormat.parse(time);
				str = strFormat.format(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
	
	/**
	 * yyyyMMdd转化为MM/dd
	 * 
	 * @param strTime
	 * @return
	 */
	private String converTimeStr(String time) {
		String str = "";
		try {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date date = dateFormat.parse(time);
			
			do {
				int interval = DateUtils.getInterval(date);
				// 今天、昨天
				if (interval == 0) {
					str = getString(R.string.tv_date_today);
					break;
				} else if (interval == -1) {
					str = getString(R.string.tv_date_yeaterday);
					break;
				}
				
				// 周一……周日
				calendar.setTime(IOTV_Date.getDate());
				int curWeekOfMounth = calendar.get(Calendar.WEEK_OF_MONTH);
				int curMouth = calendar.get(Calendar.MONTH);
//				int curWeek = calendar.get(Calendar.DAY_OF_WEEK);
				
				calendar.setTime(date);
				int weekOfMounth = calendar.get(Calendar.WEEK_OF_MONTH);
				int mouth = calendar.get(Calendar.MONTH);
				int week = calendar.get(Calendar.DAY_OF_WEEK);
				
				switch (week) {
				// SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY,
				// FRIDAY, and SATURDAY.
				case Calendar.SUNDAY:
					str = getString(R.string.tv_date_sunday);
					break;
				case Calendar.MONDAY:
					str = getString(R.string.tv_date_monday);
					break;
				case Calendar.TUESDAY:
					str = getString(R.string.tv_date_tuesday);
					break;
				case Calendar.WEDNESDAY:
					str = getString(R.string.tv_date_wednesday);
					break;
				case Calendar.THURSDAY:
					str = getString(R.string.tv_date_thursday);
					break;
				case Calendar.FRIDAY:
					str = getString(R.string.tv_date_friday);
					break;
				case Calendar.SATURDAY:
					str = getString(R.string.tv_date_saturday);
					break;
				default:
					str = getString(R.string.tv_date_monday);
					break;
				}
				
				//上周一……上周日
				if(curWeekOfMounth > weekOfMounth || curMouth != mouth){
					str = getString(R.string.tv_date_before) + str;
				}

			} while (false);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	private String getString(int stringId) {
		String str = "";
		if (mContext != null) {
			str = mContext.getString(stringId);
		}
		return str;
	}
}
