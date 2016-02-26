package com.bestv.ott.jingxuan.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.jingxuan.R;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
 

/**
 * @author hu.fuyi
 * 
 * A compatible class which is used to fake a system time for synchronization IOTV channel schedules with server
 * if current system time is normal, use it directly, else fake a system time which take from server(by queryChannel) 
 * */
public class IOTV_Date {
	//cache the days before today.
	private static long CACHE_DAYS = 3;
	
	private static final int SOLID_DATE_FOR_JX = 1;
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	public static SimpleDateFormat dateFormat_full = new SimpleDateFormat("yyyyMMddHHmmss");
	
	//the value is used to check current system time is normal or not.
	private static final String BAD_DATE_VALUES = "20000101";
	
	//the milliseconds of one hour and day
	public static final long HOUR_TIME = 1000 * 60 * 60;
	public static final long DAY_TIME = HOUR_TIME * 24;
	
	private static IOTV_Date instance = null;
	private long faketime = -1;
	private long elapsedtime_start = -1;
	private static Context mContext;
	
	private static String mToday = "";
	private static Map<String, String> mDayNameMap = new HashMap<String, String>();
	
	public static Date getDate(){
		return IOTV_Date.instance().getDateToday();
	}
	
	public static String getDateFormatNormal(Date date){
		return dateFormat.format(date);
	}
	
	public static String getDateFormatFull(Date date){
		return dateFormat_full.format(date);
	}
	
	public static Date getDateParseFull(String time){
		Date date = getDate();
		try {
			date = dateFormat_full.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public static String StringToHourMinutes(String date){
		String str = "";
		try {
			Date time = IOTV_Date.dateFormat_full.parse(date);
			String hours = time.getHours() + "";
			String minutes = time.getMinutes() + "";
		    
			if (hours.length() == 1){
				hours = "0" + hours;
			}
			
			if (minutes.length() == 1){
				minutes = "0" + minutes;
			}
			
			str = hours + ":" + minutes;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return str;
	}
	
	private IOTV_Date(){
	}
	
	public void setContext(Context context){
		mContext = context;
	}
	
	/**
	 * check system date is normal or not.
	 * 
	 * true is normal, else system time is not.
	 * */
	public boolean checkDateState(){
		long currenttime = System.currentTimeMillis();
		
		Date d = null;
		
		try {
			d = dateFormat.parse(BAD_DATE_VALUES);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long bad_values = 0;// d.getTime();
		if(d != null){
			bad_values = d.getTime();
		}
		
		return currenttime > bad_values;
	}
	
	public static IOTV_Date instance(){
		if (instance == null){
			instance = new IOTV_Date();
		}
		
		return instance;
	}
	
	public Date getDateToday(){
		Date d = new Date();
		
		//if time is not normal, set fake time as current system time.
		if (!checkDateState()){
			d.setTime(faketime + (SystemClock.elapsedRealtime() - elapsedtime_start));
			Logger.d("IOTV_Date ", "time = "+(faketime + (SystemClock.elapsedRealtime() - elapsedtime_start)));
		}
		
		return d;
	}
	
	public Date getDateYesterday(){
		Date d = getDateToday();
		return new Date(d.getTime() - DAY_TIME);
	}
	
	public Date getDateYesterdayBefore(){
		Date d = getDateToday();
		return new Date(d.getTime() - DAY_TIME * 2);
	}
	
	public Date getDateAsHour(int hour){
		Date d = getDateToday();
		
		return new Date(d.getTime() + hour * HOUR_TIME);
	}
	
	public static Date getPrevDay(int prevs){
		Date d = IOTV_Date.getDate();
		return new Date(d.getTime() - DAY_TIME * prevs);
	}
	
	public boolean isDateFaked(){
		return faketime != -1;
	}
	
	public void setFakeTime(long time){
		faketime = time;
		elapsedtime_start = SystemClock.elapsedRealtime();
	}
	
	public List<String> getPrevDays(){
		long cacheDays = DataConfig.getDataConfigValueL(mContext, DataConfig.TVOD_DATE_COUNT);
		if (cacheDays == -1){
			cacheDays = CACHE_DAYS;
		}
		
		if (OttContextMgr.getInstance().isJXRunning()){
			//fix cache day as defaul for jingxuan
			cacheDays = SOLID_DATE_FOR_JX;
		}
		
		List<String> l = new ArrayList<String>();
		
		for (int i = 0; i < cacheDays; i++){
			String day = dateFormat.format(getPrevDay(i));
			l.add(day);
		}
		
		return l;
	}
	
	public boolean isValidPrevDay(String day){
		boolean valid = false;
		
		for (String s : getPrevDays()){
			if (s.equals(day)){
				valid = true;
				break;
			}
		}
		
		return valid;
	}
	
	public static String getCurrentTimeS(){
		return IOTV_Date.getDateFormatFull(IOTV_Date.getDate());
	}
	
	public static String converTimeStr(String time) {
		String today = IOTV_Date.dateFormat.format(IOTV_Date.getDate());
		
		if (!mToday.equals(today)){
			mDayNameMap.clear();
			mToday = today;
		}else{
			//try to get the name from cache
			String name = mDayNameMap.get(time);
			if (name != null){
				return name;
			}
		}
		
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
				// int curWeek = calendar.get(Calendar.DAY_OF_WEEK);

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

				// 上周一……上周日
				if (curWeekOfMounth > weekOfMounth || curMouth != mouth) {
					str = getString(R.string.tv_date_before) + str;
				}

			} while (false);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	public static String getString(int stringId) {
		String str = "";
		if (mContext != null) {
			str = mContext.getString(stringId);
		}
		return str;
	}
	
	public static String converyyyyMMddHHmmss2yyyyMMdd(String time){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
		String date = "";
		try {
			date = dateFormat2.format(dateFormat.parse(time));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
}
