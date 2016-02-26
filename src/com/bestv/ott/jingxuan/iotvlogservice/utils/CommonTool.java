package com.bestv.ott.jingxuan.iotvlogservice.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;


@SuppressWarnings("rawtypes")
public class CommonTool {
	private static SimpleDateFormat logTime = new SimpleDateFormat(
			"HH:mm:ss SSS");
	private static SimpleDateFormat dateFormatToSs = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private static SimpleDateFormat dateFormatToDd = new SimpleDateFormat(
			"yyyyMMdd");
	//the max STB elapse time is five years
	public static final String MAX_STB_ELAPSETIME = 1000 * 60  * 60 * 24 * 365 * 5 + ""; 

	/**
	 * 判断是否为null或空
	 * 
	 * @param arg
	 *            待校验参数
	 * @return true为空，反之不为空
	 */
	public static boolean isNull(String arg) {
		boolean result = false;
		if (null == arg || "".equals(arg.trim()))
			result = true;
		return result;
	}

	/**
	 * 判断是否为null或空
	 * 
	 * @param arg
	 *            待校验参数
	 * @return true不为空，反之为空
	 */
	public static boolean isNotNull(String arg) {
		boolean result = true;
		if (null == arg || "".equals(arg.trim()))
			result = false;
		return result;
	}

	/**
	 * 判断是否为null或空
	 * 
	 * @param arg
	 *            待校验参数
	 * @return true不为空，反之为空
	 */
	public static boolean isNotNull(Map arg) {
		boolean result = true;
		if (null == arg || arg.size() == 0)
			result = false;
		return result;
	}

	/**
	 * 判断是否为null或空
	 * 
	 * @param arg
	 *            待校验参数
	 * @return true不为空，反之为空
	 */
	public static boolean isNotNull(List arg) {
		boolean result = true;
		if (null == arg || arg.size() == 0)
			result = false;
		return result;
	}

	/**
	 * 判断是否为null或空
	 * 
	 * @param arg
	 *            待校验参数
	 * @return true为空，反之不为空
	 */
	public static boolean isNull(List arg) {
		boolean result = false;
		if (null == arg || arg.size() == 0)
			result = true;
		return result;
	}

	/**
	 * 判断是否为null或空
	 * 
	 * @param arg
	 *            待校验参数
	 * @return true为空，反之不为空
	 */
	public static boolean isNull(Object[] arg) {
		boolean result = false;
		if (null == arg || arg.length == 0)
			result = true;
		return result;
	}

	/**
	 * 判断字符是否为null或空串
	 * 
	 * @param arg
	 *            待校验参数
	 * @return true为空，反之不为空
	 */
	public static boolean isNull(Map arg) {
		boolean result = false;
		if (null == arg || arg.size() == 0)
			result = true;
		return result;
	}

	/**
	 * 根据页数返回符合条件的集合
	 * 
	 * @param list
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public static List sliceList(List list, int pageIndex, int pageSize) {
		return sliceListEx(list, pageIndex, pageSize, pageSize);
	}
	
	/**
	 * 根据页数返回符合条件的集合
	 * 
	 * @param list
	 * @param pageIndex
	 * @param pageSize
	 * @param len
	 * @return
	 */
	public static List sliceListEx(List list, int pageIndex, int pageSize, int len) {
		if (isNull(list) || pageIndex == 0) {
			return new ArrayList();
		}

		int start = (pageIndex - 1) * pageSize;
		int end = start + len;
		int count = list.size();

		if (start >= count)
			return new ArrayList();

		if (end > list.size())
			end = list.size();

		return list.subList(start, end);
	}

	/**
	 * 获取当前系统时间 从小时精确到毫秒
	 * 
	 * @return
	 */
	public static String hourEndMillis() {
		return logTime.format(new Date());
	}

	/**
	 * 获取当前系统时间 从年精确到秒
	 * 
	 * @return
	 */
	public static String yearEndSecond() {
		return dateFormatToSs.format(new Date());
	}
	
	public static String yearEndSecond(Date d) {
		return dateFormatToSs.format(d);
	}

	/**
	 * 获取当前系统时间 从年精确到日
	 * 
	 * @return
	 */
	public static String yearEndDate() {
		return dateFormatToDd.format(new Date());
	}

	/**
	 * 和当前日期比较日期相差天数
	 * 
	 * @param date
	 * @return 相差天数
	 */
	public static int compareCurDate(String date) {
		int result = -1;
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			result = (int) (calendar.getTimeInMillis() - dateFormatToDd.parse(
					date).getTime())
					/ (24 * 60 * 60 * 1000);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取升级目录已占用空间
	 * 
	 * @param file
	 *            升级文件目录
	 * @return 已占用空间，单位：字节
	 */
	public static long getUpgradeUsedSpace(File file) {
		long size = 0;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File tmp : files) {
				size += getUpgradeUsedSpace(tmp);
			}
		} else if (file.isFile()) {
			size = file.length();
		}
		return size;
	}
	
	public static String safeString(String value){
		String ret=value;
		if (null == ret){
			ret = "";
		}
		return ret;
	}
	
	public static String byteArrayToHexString(byte source[], int offset, int size){
		String dest="", strTmp=null;
		int len=source.length;
		if ((null==source) || (len<=0)) return dest;		
		
		for (int tmp=offset; tmp<len && tmp<offset+size; tmp++){
			strTmp=String.format("%02x", source[tmp]);
			dest += strTmp;
		}

		return dest;
	}
	
	public static String getIPAddress(){
		String ip="";
		try {  
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			if (null != en){
				for (; en.hasMoreElements();) {  
				    NetworkInterface intf = en.nextElement();  
				    if ((null!=intf) && (intf.getName().equalsIgnoreCase("eth0"))){
					    Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
					    if (null != enumIpAddr){
						    for (; enumIpAddr.hasMoreElements();) {  
						        InetAddress inetAddress = enumIpAddr.nextElement();  
						        if (!inetAddress.isLoopbackAddress()) {  
						            ip = inetAddress.getHostAddress().toString();  
						            return ip;
						        }  
						    }  
					    }
				    }
				}  
			}
	    } catch (Throwable e) {  
	    	e.printStackTrace();
		}  
		return ip;
	}
}
