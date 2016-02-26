package com.bestv.ott.jingxuan.view;

public interface IViewFactoryBase {
	static final int VIEW_COUNT = 8;
	
	static final int TYPE_BACK_VIEW = 0;
	static final int TYPE_PAUSE_VIEW = 1;
	static final int TYPE_PLAY_COMPLETE_VIEW = 2;
	static final int TYPE_LOADING_VIEW = 3;
	static final int TYPE_CHANNEL_LIST_VIEW = 4;
	static final int TYPE_MEDIACONTROL_VIEW = 5;
	static final int TYPE_JINGXUAN_CHANNEL_LIST_VIEW = 6;
	static final int TYPE_JINGXUAN_FAKE_LOADING_VIEW = 7;
	
	IViewBase createView(int type);
}
