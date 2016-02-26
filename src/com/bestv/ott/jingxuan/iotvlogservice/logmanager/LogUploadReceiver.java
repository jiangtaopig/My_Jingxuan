package com.bestv.ott.jingxuan.iotvlogservice.logmanager;

import com.bestv.ott.jingxuan.util.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 接收日志上报相关广播
 */
public class LogUploadReceiver {
	private static final String TAG = "IOTV_QOS";
	
	private static final int INSTALL_FAILED_MODULE_VALUE = 31;
	private static final int MAX_ERRORCODE_LENGTH = 4;
	
	public static final int DEVICE_SUSPEND = 3;
	public static final int DEVICE_WAKEUP = 2;
	public static final int DEVICE_SHUTDOWN = 1;
	public static final int DEVICE_POWERON = 0;
	
	/**
	 * 应用操作广播
	 */
	public static final String USE_APP_OPEN = "com.bestv.msg.appopen";
	public static final String USE_APP_CLOSE = "com.bestv.msg.appexist";
	/**
	 * 视频播放广播
	 */
	public static final String VIDEO_PLAY = "com.bestv.msg.video.play";
	
	public static final String VIDEO_START_PLAY = "com.bestv.msg.video.play.start";

	/**
	 * 休眠广播
	 */
	public static final String SUSPEND = "com.bestv.msg.suspend";
	/**
	 * 唤醒广播
	 */
	public static final String WAKEUP = "com.bestv.msg.wakeup";
	/**
	 * 关机广播
	 */
	public static final String SHUTDOWN = "com.bestv.msg.shutdown";
	public static final String BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
	public static final String ERROR_INFO = "com.bestv.msg.errorinfo";
	public static final String UPGRADE = "com.bestv.msg.logupload.upgrade";
	
	public static final String UPLOAD_SYSTEM_LOG = "bestv.ott.action.uploadlogcat";
	public static final String VIDEO_PLAYER_ERROR = "com.bestv.ott.video.playerror";

	/**
	 * 颗粒化升级结果日志
	 */
	private static final String ACTION_REPORT_BEANS_UPGRADE_LOG = "com.bestv.msg.logup.beansupgrade";
	/**
	 * 应用安装失败广播
	 * */
	private static final String ACTION_APPINSTALL_FAILED = "bestv.ott.action.appinstallfailed";
	/**
	 * 该广播包含
	 * 视频播放过程中，指定时间间隔内的瞬时下载速度列表
	 * */
	private static final String ACTION_VIDEO_PLAYER_DOWNLOADSLICE_DETAIL = "com.bestv.msg.video.play.downloaddetail";
	/**
	 * 该广播包含
	 * 视频播放过程中，视频缓冲详情
	 * */
	private static final String ACTION_VIDEO_PLAYER_LOADING_DETAIL = "com.bestv.msg.video.play.loadingdetail";

	public static void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (!utils.getQosOpenState(context)){
			Log.d(TAG, "iotv qos upload feature is not open!");
			//iotv qos upload feature is not open, return directly.
			return;
		}

		Log.d(TAG, "iotvReceive() action:" + action + " context = " + context + " appcontext = " + context.getApplicationContext());
		boolean sysloguploadnow = false;
		if (action.equals(ACTION_VIDEO_PLAYER_LOADING_DETAIL)) {
			LogUploadMgr.bufferingLogUpload(intent, context);
		} else if (action.equals(ACTION_VIDEO_PLAYER_DOWNLOADSLICE_DETAIL)) {
			LogUploadMgr.downloadLogUpload(intent, context);
		} else if (action.equals(USE_APP_OPEN)) {
			LogUploadMgr.appLogUpload(intent, context);
		} else if (action.equals(VIDEO_PLAY)) {
			LogUploadMgr.playLogUpload(intent, context);
		} else if(action.equals(VIDEO_START_PLAY)){
			LogUploadMgr.playStartLogUpload(intent, context);
		}else{
			Log.d(TAG, "onReceive() action:" + action + " is not care");
		}
		
		Intent i = new Intent(context, IOTV_LogUploadService.class);
		i.putExtra(IOTV_LogUploadService.EXTRA_SYSLOG, sysloguploadnow);
		context.startService(i);
	}
}
