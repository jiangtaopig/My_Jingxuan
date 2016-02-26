package com.bestv.ott.jingxuan.livetv;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer.VideoPlayLogItemInfo;

public class IOTVPlayRecords extends IOTVMediaPlayer.IOTVMediaPlayerEventL{
	public static final String PLAYRECORD_REPLACE_STR = "####";
	
	static final String PREF_NAME = "iotv_play_records";
	static final String KEY_NAME = "play_records";
	static final String KEY_NAME_SIZE = "play_records_size";
	
	public static final int MAX_RETURN_RECORDS_SIZE = 3;
	
	static final long RECORD_MIN_TIME = 1000 * 60  * 5; //the minimize play time of each record which can be recorded.
	
	private IOTVMediaPlayer mMediaPlayer;
	private String mPrevChannelCode;
	private long mPrevTime;
	private Context mContext;
	
	static final int TV_PLAYRECODR_TYPE = 3;
	
	public IOTVPlayRecords(Context context, IOTVMediaPlayer player) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mMediaPlayer = player;
		eventName = "IOTVPlayRecords";
		mMediaPlayer.setEventListener(this);
	}
	
	public List<String> getPlayRecords(){
		SharedPreferences sp = mContext.getSharedPreferences(PREF_NAME, Context.MODE_WORLD_READABLE);
		List<String> datalist = new ArrayList<String>();
		
		int size = sp.getInt(KEY_NAME_SIZE, 0);
		
		if (size > 0){
			for (int index = size - 1; index >= 0; index--){
				String code = sp.getString(KEY_NAME + index, "");
				
				if (utils.isNotNull(code)){
					datalist.add(code);
				}
			}
		}
		
		datalist.add("");
		datalist.add("");
		datalist.add("");
		
		if (datalist.size() > MAX_RETURN_RECORDS_SIZE){
			return datalist.subList(0, MAX_RETURN_RECORDS_SIZE);
		}
		
		return datalist;
	}
	
    void insertPlayRecord(String channelCode, String channelName, long start_time, long end_time){
    	Logger.d("insert Play record = " + (end_time - start_time) + " channel Code = " + channelCode);
    	if (utils.isNull(channelCode)){
    		return;
    	}
    	
		if (end_time - start_time >= RECORD_MIN_TIME){
			SharedPreferences sp = mContext.getSharedPreferences(PREF_NAME, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);

			int size = sp.getInt(KEY_NAME_SIZE, 0);
			
			if (size > 0){
				ArrayList<String> datalist = new ArrayList<String>();
				
				for (int index = 0; index < size; index++){
					String code = sp.getString(KEY_NAME + index, "");
					
					if (utils.isNotNull(code) && !code.equals(channelCode)){
						datalist.add(code);
					}
				}
				
				//add new channel code at the end
				datalist.add(channelCode);
				
				//then refresh data
				Editor ed = sp.edit();
				int index = 0;
				for (String code : datalist){
					ed.putString(KEY_NAME + index, code);
					index++;
				}
				
				ed.putInt(KEY_NAME_SIZE, datalist.size());
				
			    ed.commit();
			}else{
				Editor ed = sp.edit();
				ed.putString(KEY_NAME + size, channelCode);
				ed.putInt(KEY_NAME_SIZE, size + 1);
				ed.commit();
			}
		}
	}

	@Override
	public void onBesTVMediaPlayerEvent(BesTVMediaPlayerEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void notifyStartPlay(VideoPlayLogItemInfo iteminfo, String playUrl,
			long elapsedtime) {
		// TODO Auto-generated method stub
		//it is switching play source in same channel
		if (mPrevChannelCode != null && mPrevChannelCode.equals(iteminfo.channelCode)){
			return;
		}
		
		Logger.d("notifyStartPlay channel code = " + iteminfo.channelCode + " elapsedtime = " + elapsedtime);
		
		if (mPrevChannelCode == null){
			mPrevChannelCode = iteminfo.channelCode;
			mPrevTime = elapsedtime;
		}else{
			insertPlayRecord(mPrevChannelCode, iteminfo.channelName, mPrevTime, elapsedtime);
			
			mPrevChannelCode = iteminfo.channelCode;
			mPrevTime = elapsedtime;
		}
	}

	@Override
	public void notifyStopPlay(VideoPlayLogItemInfo iteminfo, long elapsedtime) {
		// TODO Auto-generated method stub
		super.notifyStopPlay(iteminfo, elapsedtime);
		Logger.d("notify stop play, elapsedtime = " + elapsedtime);
		String name = "";
		if (iteminfo != null){
			name = iteminfo.channelName;
		}
		
		insertPlayRecord(mPrevChannelCode, name, mPrevTime, elapsedtime);
	}
}
