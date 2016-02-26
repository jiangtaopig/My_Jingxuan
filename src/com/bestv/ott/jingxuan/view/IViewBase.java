package com.bestv.ott.jingxuan.view;

import android.view.KeyEvent;
import android.view.View;

import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;

public interface IViewBase {
	void setDataController(IControllerBase c);
	View getView();
	/**
	 * 显示或者刷新view数据
	 * */
	void show();
	void hide();
	void reset();
	boolean isshow();
	
	void notifyChange(String day);
	
	boolean onKeyDown(int keyCode, KeyEvent event);
	boolean onKeyUp(int keyCode, KeyEvent event);
	
	void setCurProgrammeViaIndex(int index);
	void setCurSubProgrammeViaIndex(int index);
	void setPauseIconVisible(boolean visible);
	void setFirstSelectSubProgramPos(int position);
}
