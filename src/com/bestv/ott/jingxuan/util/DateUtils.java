package com.bestv.ott.jingxuan.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.bestv.jingxuan.R;


public class DateUtils {

	private static final String TAG = "DateUtils";
	public static final int SECOND = 1000; 
	public static final int MINUTE = 60 * SECOND;
	public static final int HOUR = 60 * MINUTE;
	public static final int DAY = 24;
	
	/**
	 * 计算目标时间与当前时间相差天数。下于0表示已经过去了多少天。大于0表示还剩余多少天。
	 * @param date	目标时间yyyy-MM-dd
	 * @return 天数，小于0表示已经过去了多少天
	 * @throws ParseException
	 */
	public static int getInterval(String date, String pattern) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date tarDate = simpleDateFormat.parse(date);
		return getInterval(tarDate);
	}
	/**
	 * 计算目标时间与当前时间相差天数。下于0表示已经过去了多少天。大于0表示还剩余多少天。
	 * @param tarDate	目标时间
	 * @return	天数，小于0表示已经过去了多少天
	 * @throws ParseException
	 */
	public static int getInterval(Date tarDate) throws ParseException {
		Date curDate = IOTV_Date.getDate();
		long time = tarDate.getTime() - curDate.getTime();
		if(time <= 0){
			return (int) (time/(1000 * 60 * 60 * 24));
		}else{
			return (int) (time/(1000 * 60 * 60 * 24) + 1);
		}
	}
	
	/**
	 * 计算目标时间与当前时间相差天数。下于0表示已经过去了多少天。大于0表示还剩余多少天。
	 * @param srcDate	当前时间
	 * @param tarDate	目标时间
	 * @return
	 * @throws ParseException
	 */
	public static int getInterval(Date srcDate, Date tarDate) throws ParseException {
		long time = tarDate.getTime() - srcDate.getTime();
		if(time <= 0){
			return (int) (time/(1000 * 60 * 60 * 24));
		}else{
			return (int) (time/(1000 * 60 * 60 * 24) + 1);
		}
	}
	
	/**
	 * 计算目标时间与当前时间相差天数。下于0表示已经过去了多少天。大于0表示还剩余多少天。
	 * @param srcDate	当前时间yyyy-MM-dd
	 * @param tarDate	目标时间yyyy-MM-dd
	 * @return
	 * @throws ParseException
	 */
	public static int getInterval(String strSrcDate, String strTarDate, String pattern) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date tarDate = simpleDateFormat.parse(strTarDate);
		Date srcDate = simpleDateFormat.parse(strSrcDate);
		return getInterval(srcDate, tarDate);
	}
	
	/**
	 * 计算距离某天指定天数后的日期。天数大于0表示之后多少天。
	 * @param strSrcDate	基于这个日期计算时间 yyyy-MM-dd
	 * @param days	指定天数，大于0表示在某日之后多少天
	 * @return	计算后的日期 yyyy-MM-dd
	 * @throws ParseException
	 */
	public static String getSpecifiedDate(String strSrcDate, int days, String pattern) throws ParseException{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date srcDate = simpleDateFormat.parse(strSrcDate);
		return simpleDateFormat.format(getSpecifiedDate(srcDate, days));
	}
	
	/**
	 * 计算距离某天指定天数后的日期。天数大于0表示之后多少天。
	 * @param curDate	基于这个日期计算时间
	 * @param days	指定天数，大于0表示在某日之后多少天
	 * @return	计算后的日期
	 */
	public static Date getSpecifiedDate(Date srcDate, int days){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(srcDate);
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + days);
		Date tarDate = calendar.getTime();
		return tarDate;
	}
	
	public static String formatElapsedTime(String time){
//		long distance = end - start;
//		
//		if (distance <= 0){
//			return "";
//		}
//		
////		if (distance <= MINUTE){
////			return "刚刚";
////		}
////		
////		if (distance <= HOUR){
////			return distance / MINUTE + "分钟前";
////		}
//	
//		long hours = distance / HOUR;
//		
//		Date d = new Date(end);
//		long curHours = d.getHours();
//		
//		if (curHours >= hours){
////			return hours + "小时前";
//			return "今日";
//		}
//		
//		long preDatHours = hours - curHours;
//		
//		if (preDatHours <= DAY){
//			return "昨日";
//		}
//		
////		if (preDatHours > DAY && preDatHours <= 2 * DAY){
////			return "前天";
////		}
		String date = "";
		if (IOTV_Date.getDateFormatNormal(IOTV_Date.getDate()).equals(time)) {
			date = "今日";
		} else if (IOTV_Date.getDateFormatNormal(
				IOTV_Date.instance().getDateYesterday()).equals(time)) {
			date = "昨日";
		} else {
			SimpleDateFormat dateFormatS = new SimpleDateFormat("MM/dd");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			try {
				Date temp = dateFormat.parse(time);
				date = dateFormatS.format(temp);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return date;
		//return "3天前";
	}
	
}
