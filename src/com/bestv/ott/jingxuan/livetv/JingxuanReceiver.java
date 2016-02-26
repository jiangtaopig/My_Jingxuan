package com.bestv.ott.jingxuan.livetv;

import com.bestv.ott.jingxuan.util.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class JingxuanReceiver extends BroadcastReceiver {
	private static final String TAG = "JingxuanReceiver";
	public static final String OTT_ACTION_LOGINED = "bestv.ott.action.logined";
	public static final String TEST_ACTION = "karelgt.action.test";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "enter onReceive");
		OttContextMgr.getInstance().setContext(context.getApplicationContext());

		if (OTT_ACTION_LOGINED.equalsIgnoreCase(intent.getAction())
				|| TEST_ACTION.equalsIgnoreCase(intent.getAction())) {
			Logger.d(TAG, "action logined");
			context.startService(new Intent(context, JingxuanCacheService.class));
			context.startService(new Intent(context, IotvCacheService.class));
		}

	}

}
