package com.bestv.ott.jingxuan.util;

import android.util.Log;

/**
 * responsible for recording app log info
 * 
 * @author hu.fuyi
 * 
 */
public class Logger {

	private static String LOG_PREFIX = "IOTV";
	private static boolean isOpenLog = true;

	public static void initLog(boolean isOpen) {
		isOpenLog = isOpen;
	}

	public static void setLogPrefix(String prefix) {
		LOG_PREFIX += "_" + prefix;
	}

	/**
	 * info
	 * 
	 * @param tag
	 * @param message
	 */
	public static void i(String message) {
		if (isOpenLog && message != null) {
			Log.i(LOG_PREFIX, message);
		}
	}

	/**
	 * Debug
	 * 
	 * @param tag
	 * @param message
	 */
	public static void d(String message) {
		if (isOpenLog && message != null) {
			Log.d(LOG_PREFIX, message);
		}
	}

	public static void d(String Tag, String message) {
		if (isOpenLog && message != null) {
			Log.d(Tag, message);
		}
	}

	/**
	 * warn
	 * 
	 * @param tag
	 * @param message
	 */
	public static void w(String message) {
		if (isOpenLog && message != null) {
			Log.w(LOG_PREFIX, message);
		}
	}

	/**
	 * error
	 * 
	 * @param tag
	 * @param message
	 */
	public static void e(String message) {
		if (isOpenLog && message != null) {
			Log.e(LOG_PREFIX, message);
		}
	}

	/**
	 * error
	 * 
	 * @param tag
	 * @param message
	 */
	public static void e(Throwable e) {
		if (isOpenLog && e != null) {
			Log.e(LOG_PREFIX, e.getMessage(), e);
		}
	}

	/**
	 * error
	 * 
	 * @param tag
	 * @param message
	 */
	public static void e(String message, Throwable e) {
		if (isOpenLog && e != null) {
			Log.e(LOG_PREFIX, message, e);
		}
	}

	/**
	 * error
	 * 
	 * @param tag
	 * @param message
	 */
	public static void w(Throwable e) {
		if (isOpenLog && e != null) {
			Log.e(LOG_PREFIX, e.getMessage(), e);
		}
	}

}
