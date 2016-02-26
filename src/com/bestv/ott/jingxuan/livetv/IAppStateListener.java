package com.bestv.ott.jingxuan.livetv;

import android.os.Bundle;
import android.view.KeyEvent;

public interface IAppStateListener {
	void onBackPressed();
	void onCreate(Bundle savedInstanceState);
	void onDestroy();
	boolean onKeyDown(int keyCode, KeyEvent event);
	boolean onKeyUp(int keyCode, KeyEvent event);
	void onPause();
	void onResume();
	void onStart();
	void onStop();
	boolean dispatchKeyEvent(KeyEvent event);
}
