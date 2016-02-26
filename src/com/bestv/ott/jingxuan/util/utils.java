package com.bestv.ott.jingxuan.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.bestv.jingxuan.R;
import com.bestv.ott.framework.beans.JsonResult;
import com.bestv.ott.jingxuan.data.Category;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.RecommendItem;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.iotvlogservice.utils.HashUtil;
import com.bestv.ott.jingxuan.livetv.ShortcutKeyMgr;
import com.google.gson.Gson;

public class utils {
	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	public static SimpleDateFormat dateFormat2 = new SimpleDateFormat(
			"HH:mm:ss");

	public static SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH:mm");

	public static SimpleDateFormat dateFormat4 = new SimpleDateFormat(
			"yyyyMMdd");
	
	private static SimpleDateFormat logTime = new SimpleDateFormat(
			"HH:mm:ss SSS");
	private static SimpleDateFormat dateFormatToSs = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private static SimpleDateFormat dateFormatToDd = new SimpleDateFormat(
			"yyyyMMdd");
	
	static boolean mbLOGD = true;
	static boolean mbLOGI = true;
	static boolean mbLOGW = true;
	static boolean mbLOGE = true;
	static boolean mbPrintByteArray = true;
	
	static final String packageName = "com.bestv.ott.livetv";
	static final String action = "bestv.ott.action.uploadlogcat";
	
	public static Gson gson = new Gson();

	public static JsonResult checkJsonValid(String json) {
		JsonResult result = new JsonResult();

		try {
			JSONObject jsonObject = new JSONObject(json);
			result.setValid(true);
			JSONObject responseObj = jsonObject.getJSONObject("Response");
			JSONObject headerObj = responseObj.getJSONObject("Header");
			JSONObject bodyObj = responseObj.getJSONObject("Body");
			result.setCompleted(true);
			if (null != headerObj) {
				result.setRetCode(headerObj.getInt("RC"));
				result.setRetMsg(headerObj.getString("RM"));
				if (0 == result.getRetCode()) {
					result.setRetOK(true);
				}
				result.setObj(bodyObj);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result.setRetCode(-1);
			result.setRetMsg(e.getStackTrace().toString());
		}

		return result;
	}
	
	public static void send(Context context, int errorCode) {
		Intent it = new Intent(action);
		it.putExtra("pkgName", packageName);
		it.putExtra("errorCode", errorCode + "");
		context.sendBroadcast(it);
		Logger.d("send error log..." + errorCode);
	}

	public static void send(Context context, String errorCode) {
		if (context == null){
			return;
		}
		
		Intent it = new Intent(action);
		it.putExtra("pkgName", packageName);
		it.putExtra("errorCode", errorCode);
		context.sendBroadcast(it);
		Logger.d("send error log..." + errorCode);
	}

	public static void setPrintByteArrayFlag(boolean flag) {
		mbPrintByteArray = flag;
	}

	public static void intToByteArray(int source, byte dest[], int offset) {
		if (null == dest)
			return;

		if (dest.length < 4 + offset) {
			throw new IndexOutOfBoundsException();
		}

		dest[0 + offset] = (byte) (source >> 24 & 0xFF);
		dest[1 + offset] = (byte) (source >> 16 & 0xFF);
		dest[2 + offset] = (byte) (source >> 8 & 0xFF);
		dest[3 + offset] = (byte) (source & 0xFF);
	}

	public static int stringToByteArray(String source, byte dest[], int offset) {
		int len = source.length();
		if ((null == dest) || (len <= 0))
			return 0;
		if (dest.length < len + offset) {
			len = dest.length - offset;
		}

		byte strByte[] = source.getBytes();

		for (int index = 0; index < len; index++) {
			dest[index + offset] = strByte[index];
		}

		// source.getBytes(0, len-1, dest, offset);
		return len;
	}

	public static String ByteArrayToPrintString(byte source[], int offset,
			int size) {
		String dest = "", strTmp = null;
		if (!mbPrintByteArray)
			return dest;
		if ((null == source) || (source.length <= 0))
			return dest;

		for (int tmp = offset; tmp < source.length && tmp < offset + size; tmp++) {
			strTmp = String.format("%02x ", source[tmp]);
			dest += strTmp;
		}

		return dest;
	}

	public static int stringToInt(String src) {
		int ret = 0;
		try {
			ret = Integer.parseInt(src);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static String byteArrayToHexString(byte source[], int offset,
			int size) {
		String dest = "", strTmp = null;
		int len = source.length;
		if ((null == source) || (len <= 0))
			return dest;

		for (int tmp = offset; tmp < len && tmp < offset + size; tmp++) {
			strTmp = String.format("%02x", source[tmp]);
			dest += strTmp;
		}

		return dest;
	}

	public static boolean isNull(String arg) {
		boolean result = false;
		if (null == arg || "".equals(arg.trim()))
			result = true;
		return result;
	}

	public static boolean isNotNull(String arg) {
		boolean result = true;
		if (null == arg || "".equals(arg.trim()))
			result = false;
		return result;
	}

	public static String getInternalSDCardDir() {
		String ret = "/mnt/sdcard";
		File file = new File(ret);
		if (!(file.exists() && file.canWrite())) {
			ret = null;
		}
		return ret;
	}

	public static String getExternalSDCardDir() {
		String ret = "/mnt/sdcard/external_sdcard";
		File file = new File(ret);
		if (!(file.exists() && file.canWrite() && (doExec("df").indexOf(ret) >= 0))) {
			ret = null;
		}
		return ret;
	}

	public static String getUsbDir() {
		String ret = null;
		File file = new File("/mnt");
		if (file.isDirectory()) {
			String[] subfiles = file.list();
			for (int index = 0; index < subfiles.length; index++) {
				if (subfiles[index].toLowerCase().startsWith("sd")
						&& (0 != subfiles[index].compareToIgnoreCase("sdcard"))) {
					ret = subfiles[index];
					break;
				}
			}
		}
		if (null != ret) {
			ret = "/mnt/" + ret;
		}
		return ret;
	}

	public static String doExec(String cmd) {
		String s = "";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				s += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger.d("doExec '" + cmd + "' return " + s);
		return s;
	}

	public static String getMacAddress() {
		String mac = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			if (null != en) {
				for (; en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					if ((null != intf)
							&& (intf.getName().equalsIgnoreCase("eth0"))) {
						byte hardwareAddr[] = intf.getHardwareAddress();
						mac = byteArrayToHexString(hardwareAddr, 0,
								hardwareAddr.length);
						break;
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Logger.d("getMacAddress : " + mac);
		return mac;
	}

	public static String getIPAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			if (null != en) {
				for (; en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					if ((null != intf)
							&& (intf.getName().equalsIgnoreCase("eth0"))) {
						Enumeration<InetAddress> enumIpAddr = intf
								.getInetAddresses();
						if (null != enumIpAddr) {
							for (; enumIpAddr.hasMoreElements();) {
								InetAddress inetAddress = enumIpAddr
										.nextElement();
								if (!inetAddress.isLoopbackAddress()) {
									ip = inetAddress.getHostAddress()
											.toString();
									Logger.d("getIPAddress : " + ip);
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
		Logger.d("getIPAddress : " + ip);
		return ip;
	}

	public static String safeString(String value) {
		String ret = value;
		if (null == ret) {
			ret = "";
		}
		return ret;
	}

	public static int uriForward(Context context, String uri) {

		Logger.d("uri:" + uri);

		final int FORWARD_ERROR_CODE = -1;

		final int FORWARD_SUCCESS_CODE = 1;

		String actionName = null;
		String param = null;
		String activityId = null;

		if (null != uri && !"".equals(uri)) {

			String[] ss = uri.split(":");
			if (ss.length < 1) {
				Logger.w("the uri is null or empty");
				return FORWARD_ERROR_CODE;
			}

			if (ss.length > 2) {
				activityId = ss[2];
			}
			if (ss.length > 1) {
				param = ss[1];
			}
			if (ss.length > 0) {
				actionName = ss[0];
			}

			if (null != actionName && !"".equals(actionName)) {
				try {
					Logger.d("the uri detail is: actionName=" + actionName
							+ ",param=" + param + ",activityId=" + activityId);
					Intent intent = new Intent();
					intent.setAction(actionName);
					intent.putExtra("param", param);
					intent.putExtra("activityId", activityId);
					context.startActivity(intent);

					return FORWARD_SUCCESS_CODE;
				} catch (Exception e) {
					Logger.w("start activity fail , actionName is ="
							+ actionName);
					return FORWARD_ERROR_CODE;
				}
			} else {
				Logger.w("the uri actionName is null or empty");
				return FORWARD_ERROR_CODE;
			}
		} else {
			Logger.w("the uri is null or empty");
			return FORWARD_ERROR_CODE;
		}

	}
	
	public static String getUrl(String url) {
		if (url.contains("?")) {
			if (url.endsWith("&") == false)
				return url + "&";
			return url;
		}

		return url + "?";
	}

	public static int getVersionCode(Context context) {

		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static String getVersionName(Context context) {

		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static boolean isEmpty(String inputs) {
		if (inputs == null || inputs.equals(""))
			return true;

		return false;
	}

	public static String getAppInfo(Context context) {
		String format = context.getString(R.string.key_appinfo);
		return format(format, getVersionCode(context)
				+ "", getVersionName(context));
	}

	public static void getClassString(Object data, Class<?> c, StringBuilder sb) {

		Field[] fileds = c.getDeclaredFields();
		String value = "";
		for (Field f : fileds) {
			f.setAccessible(true);
			try {
				if (f.get(data) != null) {
					value = f.get(data).toString();
				}
				sb.append(f.getName() + ":" + value).append("|");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		if (c.getGenericSuperclass() != null
				&& c.getGenericSuperclass().toString().indexOf("Object") == -1) {
			Class superClass = c.getSuperclass();
			getClassString(data, superClass, sb);
		}
	}

	public static String getDeviceID(Context context) {
		try {
			String id = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);
			if (id == null || id.equals("")) {
				id = android.os.Build.SERIAL;
			}
			return id;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static boolean isSameDay(String day) {
		String d2 = dateFormat4.format(IOTV_Date.getDate());
		return day.equals(d2);
	}

	public static String getDay() {
		return dateFormat4.format(IOTV_Date.getDate());
	}

	public static String getCurrentTime() {
		return dateFormat.format(IOTV_Date.getDate());
	}

	public static long getCurrentSystemTimeMilli() {
		return IOTV_Date.getDate().getTime();
	}

	/** 用于判断当前播放状态 **/
	public static int getState(String StartTime, String EndTime) {
		int state = -1;
		try {
			Date starttime = dateFormat.parse(StartTime);
			Date endtime = dateFormat.parse(EndTime);
			Date nowtime = IOTV_Date.getDate();
			if (nowtime.before(starttime)) {
				state = 2;
			} else if (nowtime.after(endtime)) {
				state = 1;
			} else {
				state = 0;
			}
			// Logger.d(StartTime + ",  " + EndTime + ", "
			// + dateFormat.format(nowtime));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return state;
	}

	public static int getlocation(Channel c, List<Channel> lists) {
		for (int i = 0; i < lists.size(); i++) {
			if (c.getChannelName().equals(lists.get(i).getChannelName()))
				return i;
		}
		return -1;
	}

	/**
	 * 用于判断当前节目播放状态 0表示正在直播 1表示直播完成，可以回看 2表示还未直播
	 * **/
	public static int getState(Schedule s) {
		if (s == null)
			return 2;
		return getState(s.getStartTime(), s.getEndTime());
	}

	public static int getExpandState(Schedule s) {
		if (s == null)
			return 2;

		String endTime = Timeadd2(s.getEndTime(), 10000);
		return getState(s.getStartTime(), endTime);
	}

	/** 将yyyymmddHHMMss 转化为HH：MM：ss格式 */
	public static String getTime(String time) {
		String ttime = "";
		try {
			Date starttime = dateFormat.parse(time);
			ttime = dateFormat2.format(starttime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ttime;
	}
	
	/** 将yyyymmddHHMMss 转化为HH：MM：ss格式 */
	public static String getTimeL(long time) {
		String ttime = "";

		Date starttime = new Date(time);
		ttime = dateFormat2.format(starttime);
		return ttime;
	}

	/** 将yyyymmddHHMMss 转化为HH：MM格式 */
	public static String getTime2(String time) {
		String ttime = "";
		try {
			Date starttime = dateFormat.parse(time);
			ttime = dateFormat3.format(starttime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ttime;
	}

	/** 将yyyymmddHHMMss 转化为long */
	public static long Time2long(String time) {
		long l = 0;
		try {
			l = dateFormat.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return l;
	}

	/** 将HH:MM:ss 转化为long */
	public static long Time2long2(String time) {
		long l = 0;
		try {
			if (null != time && !time.equals(""))
				l = dateFormat2.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return l;
	}

	/** 将long 转化为HH:MM:ss格式 */
	public static String Long2String(long l) {
		String s = dateFormat2.format(new Date(l));
		return s;
	}

	/** 将long 转化为yyyymmddHHMMss格式 */
	public static String Long2String2(long l) {
		String s = dateFormat.format(new Date(l));
		return s;
	}

	/** HH:MM:ss时间自增加 */
	public static String Timeadd(String time, long addtime) {
		time = Long2String(Time2long(time) + addtime);
		return time;
	}

	public static String Timeadd2(String time, long addtime) {
		time = Long2String2(Time2long(time) + addtime);
		return time;
	}

	public static void Sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String format(String format, String... strings) {
		MessageFormat msg = new MessageFormat(format);
		return msg.format(strings);
	}
	
	//added by hufuyi 2014/3/14
	public static void setQosOpenState(Context context, boolean open){
		SharedPreferences sp = context.getSharedPreferences("qos_upload", Context.MODE_WORLD_WRITEABLE);
		
		Editor ed = sp.edit();
		ed.putBoolean("qos_open", open);
		ed.commit();
	}
	
	public static boolean getQosOpenState(Context context){
		SharedPreferences sp = context.getSharedPreferences("qos_upload", Context.MODE_WORLD_WRITEABLE);
		return sp.getBoolean("qos_open", true);
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
	public static List sliceListEx(List list, int pageIndex, int pageSize,
			int len) {
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
		return logTime.format(IOTV_Date.getDate());
	}

	/**
	 * 获取当前系统时间 从年精确到秒
	 * 
	 * @return
	 */
	public static String yearEndSecond() {
		return dateFormatToSs.format(IOTV_Date.getDate());
	}

	/**
	 * 获取当前系统时间 从年精确到日
	 * 
	 * @return
	 */
	public static String yearEndDate() {
		return dateFormatToDd.format(IOTV_Date.getDate());
	}

	/**
	 * 和当前日期比较日期相差天数
	 * 
	 * @param date
	 * @return 相差天数
	
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
	} */

	/** 对mac地址进行分割符替换并进行base64编码 */
	public static String getMac(String temp) {

		try {
			if (isNotNull(temp)) {
				temp = temp.replaceAll(":", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return HashUtil.getBase64(temp);
	}

	/*** 将时间以hh：mm：ss格式显示 */
	public static String inttoString(int time) {
		long hh = time / 3600;
		long mm = time % 3600 / 60;
		long ss = time % 60;
		String HH = "", MM = "", SS = "";
		if (hh < 10) {
			HH = "0" + hh;
		} else {
			HH = hh + "";
		}
		if (mm < 10) {
			MM = "0" + mm;
		} else {
			MM = mm + "";
		}
		if (ss < 10) {
			SS = "0" + ss;
		} else {
			SS = ss + "";
		}
		return HH + ":" + MM + ":" + SS;
	}
	/*** 将时间以hh：mm：ss格式显示 */
	public static String longtoString(long time) {
		long hh = time / 3600;
		long mm = time % 3600 / 60;
		long ss = time % 60;
		String HH = "", MM = "", SS = "";
		if (hh < 10) {
			HH = "0" + hh;
		} else {
			HH = hh + "";
		}
		if (mm < 10) {
			MM = "0" + mm;
		} else {
			MM = mm + "";
		}
		if (ss < 10) {
			SS = "0" + ss;
		} else {
			SS = ss + "";
		}
		return HH + ":" + MM + ":" + SS;
	}
	
	public static void unzip(InputStream zipFileName, String outputDirectory) {
		try {
			ZipInputStream in = new ZipInputStream(zipFileName);
			// 获取ZipInputStream中的ZipEntry条目，一个zip文件中可能包含多个ZipEntry，
			// 当getNextEntry方法的返回值为null，则代表ZipInputStream中没有下一个ZipEntry，
			// 输入流读取完成；
			ZipEntry entry = in.getNextEntry();
			while (entry != null) {
				// 创建以zip包文件名为目录名的根目录
				File file = new File(outputDirectory);
				file.mkdir();
				if (entry.isDirectory()) {
					String name = entry.getName();
					name = name.substring(0, name.length() - 1);

					file = new File(outputDirectory + File.separator + name);
					file.mkdir();

				} else {
					file = new File(outputDirectory + File.separator
							+ entry.getName());
					file.createNewFile();
					FileOutputStream out = new FileOutputStream(file);
					int b;
					while ((b = in.read()) != -1) {
						out.write(b);
					}
					out.close();
				}
				// 读取下一个ZipEntry
				entry = in.getNextEntry();
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
			Logger.d("IOTV2", "11ns,e.." + e.getMessage());
		}
	}
	
	public static Channel category2Channel(Category category){
		Channel channel = new Channel();
		channel.setIcon1(category.getIcon1());
		channel.setIcon2(category.getIcon2());
		channel.setBKImage1(category.getBgImage1());
		channel.setBKImage2(category.getBgImage2());
		channel.setChannelCode(category.getCode());
		channel.setChannelName(category.getName());
		return channel;
	}
	
	public static boolean isIOTVMenuKey(int keyCode){
		return (keyCode == ShortcutKeyMgr.MENU_KEY || keyCode == KeyEvent.KEYCODE_MENU);
	}
	
	public static boolean isIOTVChannelUPKey(int keyCode){
		return keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_UP;
	}
	
	public static boolean isIOTVChannelDownKey(int keyCode){
		return keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN;
	}
			
	public static boolean isIOTVPauseKey(int keyCode){
		return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
	}
	
	/**
     * 返回随机n个节目
     * @param list 备选号码
     * @param count 备选数量
     * @return
     */
	public static List<RecommendItem> getRandomRecommendList(Channel channel, List<Schedule> srclist,
			int count, int currentPlayPos) {
		List<Schedule> list = new ArrayList<Schedule>(srclist);
		List<RecommendItem> reList = new ArrayList<RecommendItem>();
		if (list != null && list.size() > 0) {
			Random random = new Random();
			// 先抽取，备选数量的个数
			if (list.size() >= count) {
				for (int i = 0; i < count; i++) {
					// 随机数的范围为0-list.size()-1;
					int target = random.nextInt(list.size());
					if (target != currentPlayPos) {
						reList.add(new RecommendItem(channel, list.get(target)));
					}
					list.remove(target);
				}
			} else {
				count = list.size();
				for (int i = 0; i < count; i++) {
					// 随机数的范围为0-list.size()-1;
					int target = random.nextInt(list.size());
					if (target != currentPlayPos) {
						reList.add(new RecommendItem(channel, list.get(target)));
					}
					list.remove(target);
				}
			}
		}
		return reList;
	}
	
	public static Channel getChannelByChannelCode(List<Channel> channelList, String channelCode){
		for(Channel channel:channelList){
			if(channel.getChannelCode().equalsIgnoreCase(channelCode)){
				return channel;
			}
		}
		return null;
		
	}
	/**
	 * App是否在运行 需要权限<uses-permission android:name="android.permission.GET_TASKS"
	 * />
	 * 
	 * @param activityName
	 * @param context
	 * @return
	 */
	public static boolean isActivityRunning(String activityName, Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = am.getRunningTasks(30);
		boolean isAppRunning = false;
		for (RunningTaskInfo info : runningTaskInfos) {
			if (info.topActivity.getClassName().equals(activityName)
					|| info.baseActivity.getClassName().equals(activityName)) {
				isAppRunning = true;
				break;
			}
		}
		return isAppRunning;
	}
	
	public static List<Schedule> filterPlayableSchedules(List<Schedule> srcSchedules){
		if(srcSchedules == null){
			return null;
		}
		
		List<Schedule> playableSchedules = new ArrayList<Schedule>(srcSchedules);
		List<Schedule> removeList = new ArrayList<Schedule>();
		for(Schedule sc : playableSchedules){
			if(!isPlayable(sc.getEndTime())){
				removeList.add(sc);
			}
		}
		playableSchedules.removeAll(removeList);
		return playableSchedules;
	}
	
	public static boolean markSubscribeStatus(List<Schedule> srcSchedules, String subscribeTime){
		Logger.d("utils", "enter markSubscribeStatus , subscribeTime = "+subscribeTime);
		if(srcSchedules == null){
			return false;
		}
		boolean ifUpdate = false;
		for (Schedule sc : srcSchedules) {
			if (isPlayable(sc.getEndTime()) && !sc.isHasWatched() && isUpdate(subscribeTime, sc.getEndTime())) {//如果是可播放的（即结束时间endTime已过）且没有观看过且结束时间晚于订阅时间
				ifUpdate = true;
				Log.d("lhh", "subscribed programs have update:" + sc.getName()
						+ ", endtime:" + sc.getEndTime() + "currenttime:"
						+ IOTV_Date.getCurrentTimeS());
				sc.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_UPDATE);
			}else{
				sc.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_NORMAL);
			}
		}
		Logger.d("utils", "ifUpdate = "+ifUpdate);
		return ifUpdate;
	}
	
	public static boolean isPlayable(String startTime) {
		boolean flag = false;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date date = dateFormat.parse(startTime);
			Date curDate = IOTV_Date.getDate();
			//String testTime = "20151028000000";
			//Date curDate =dateFormat.parse(testTime);
			//Logger.d("isPlayable", "date = "+date+" , curDate = "+curDate);
			flag = date.before(curDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	public static boolean isUpdate(String subscribeTime, String endTime) {
		if(subscribeTime == null || endTime == null){
			return false;
		}
		boolean flag = false;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date date = dateFormat.parse(endTime);
			Date subDate = dateFormat.parse(subscribeTime);
			flag = subDate.before(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	public static int getIndexOfTheFirstPartInTheFirstPlayableSchedule(List<Schedule> scheduleList){
		int index = 0;
		boolean hasPlayable = false;
		if(scheduleList == null || scheduleList.size() <= 1){
			return index;
		}

		for(index = 0; index < scheduleList.size(); index++){
			if(isPlayable(scheduleList.get(index).getEndTime())){
				hasPlayable = true;
				break;
			}
		}
		
		if(!hasPlayable)  //如果没有可播放的节目，则index返回为-1.
			return -1;
		
		String firstScheduleName = scheduleList.get(index).getName();
		Log.d("getIndexOfTheFirstPartInTheFirstSchedule", "first playable schedule index is : " + index + ", name is : " + firstScheduleName);
		if(firstScheduleName != null){
			for(int i = index + 1; i < scheduleList.size(); i++){
				if((i >= scheduleList.size()) || (!scheduleList.get(i).getName().equals(firstScheduleName))){
					break;
				}else{
					index = i;
				}
			}
		}
		Log.d("getIndexOfTheFirstPartInTheFirstSchedule", "first part of the first playable schedule index is : " + index + ", name is : " + scheduleList.get(index).getName());
		
		return index;
	}
	
	public static int getIndexOfTheFirstPlayableSchedule(List<Schedule> scheduleList){
		int index = 0;
		if(scheduleList == null || scheduleList.size() <= 1){
			return index;
		}

		for(index = 0; index < scheduleList.size(); index++){
			if(isPlayable(scheduleList.get(index).getEndTime())){
				break;
			}
		}
		return index;
	}
	
	public static String getEndTimeOfTheFirstPlayableSchedule(List<Schedule> scheduleList){
		String endtime = IOTV_Date.getCurrentTimeS();
		int index = 0;
		if(scheduleList == null || scheduleList.size() <= 1){
			return endtime;
		}

		for(index = 0; index < scheduleList.size(); index++){
			if(isPlayable(scheduleList.get(index).getEndTime())){
				endtime = scheduleList.get(index).getEndTime();
				break;
			}
		}
		return endtime;
	}
	
	public static ArrayList<Schedule> deepCloneScheduleList(List<Schedule> srcScheduleList){
		if(srcScheduleList == null){
			return null;
		}
		ArrayList<Schedule> newScheduleList = new ArrayList<Schedule>();
		for(Schedule sc : srcScheduleList){
			newScheduleList.add((Schedule)sc.clone());
		}
		return newScheduleList;
	}
	
	/**
	 * 显示普通Toast
	 * @param content
	 */
	public static void showToast(Context context,String content){
		Toast t = Toast.makeText(context,"", Toast.LENGTH_LONG);
		TextView tv = new TextView(context);
	    ColorDrawable cd = new ColorDrawable(Color.BLACK);
	    cd.setAlpha(175);
	    tv.setBackgroundDrawable(cd);
	    
		tv.setText(content);
		tv.setTextSize(22f);
		t.setView(tv);
		t.show();
	}
	
	public static List<Schedule> filterSchedulesWithPlayableSubScheduleList(List<Schedule> srcSchedules){
		if(srcSchedules == null){
			return null;
		}
		
		List<Schedule> playableSchedules = new ArrayList<Schedule>(srcSchedules);
		List<Schedule> removeList = new ArrayList<Schedule>();
		for(Schedule sc : playableSchedules){
			if(sc.getSubScheduleList() != null && sc.getSubScheduleList().size()>0){//有子集的节目
				if(!isPlayable(sc.getSubScheduleList().get(sc.getSubScheduleList().size() - 1).getEndTime())){//最后一条都没到时间，说明该节目没有可以播放的子集
					removeList.add(sc);
				}
			}else{//无子集的单条节目
				if(!isPlayable(sc.getEndTime())){//结束时间未到，说明该节目不能播放
					removeList.add(sc);
				}
			}
		}
		//Logger.d("filterSchedulesWithPlayableSubScheduleList", "removeList.size = "+removeList.size());
		playableSchedules.removeAll(removeList);
		return playableSchedules;
	}
	
	public static int findSchedule(List<Schedule> ScheduleList , Schedule schedule){
		int index = -1;
		int i = 0;
		for(Schedule sc : ScheduleList){
			if(sc.equals(schedule)){
				index = i;
			}
			i++;
		}
		Log.d("findSchedule", "index = " + index);
		return index;
	}
}
