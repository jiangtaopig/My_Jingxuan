package com.bestv.ott.jingxuan.view;

import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.jingxuan.R;

import android.content.Context;
import android.view.LayoutInflater;

public class IOTVViewFactory implements IViewFactoryBase{
	//按“返回键”的退出界面
	public IViewBase createBackView() {
		// TODO Auto-generated method stub
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		BackView backView = (BackView) inflater.inflate(R.layout.jx_iotv_back_view, null);
		return backView;
	}

	public IViewBase createPauseView() {
		// TODO Auto-generated method stub
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		PauseView pauseView = (PauseView) inflater.inflate(R.layout.jx_iotv_pause_view, null);
		return pauseView;
	}

	public IViewBase createPlayCompleteView() {
		// TODO Auto-generated method stub
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		PlayCompleteView playCompleteView = (PlayCompleteView) inflater.inflate(R.layout.jx_iotv_play_complete_view, null);
		return playCompleteView;
	}

	public IViewBase createLoadingView() {
		// TODO Auto-generated method stub
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		VideoLoadingView loadingview = (VideoLoadingView) inflater.inflate(R.layout.jx_jingxuan_iotv_video_loading, null);
		return loadingview;
	}
	
	public IViewBase createJxFakeLoadingView() {
		// TODO Auto-generated method stub
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		FakeVideoLoadingView loadingview = (FakeVideoLoadingView) inflater.inflate(R.layout.jx_jingxuan_iotv_video_fake_loading, null);
		return loadingview;
	}

	public IViewBase createChannelListView() {
		// TODO Auto-generated method stub
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		ChannelListView listview = (ChannelListView) inflater.inflate(R.layout.jx_iotv_channel_list_view, null);
		return listview;
	}
	
	public IViewBase createJingxuanChannelListView() {
		// TODO Auto-generated method stub
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		JingxuanChannelListView listview = (JingxuanChannelListView) inflater.inflate(R.layout.jx_jingxuan_channel_list_view, null);
		return listview;
	}
	
	IViewBase createMediaControlView(){
		Context c = OttContextMgr.getInstance().getContext();
		
		LayoutInflater inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		MediaControlView controlview = (MediaControlView) inflater.inflate(R.layout.jx_iotv_media_control_view, null);
		return controlview;
	}

	@Override
	public IViewBase createView(int type) {
		// TODO Auto-generated method stub
		switch (type) {
		case TYPE_BACK_VIEW:
			return createBackView();
		case TYPE_LOADING_VIEW:
			return createLoadingView();
		case TYPE_CHANNEL_LIST_VIEW:
			return createChannelListView();
		case TYPE_PAUSE_VIEW:
			return createPauseView();
		case TYPE_PLAY_COMPLETE_VIEW:
			return createPlayCompleteView();
		case TYPE_MEDIACONTROL_VIEW:
			return createMediaControlView();
		case TYPE_JINGXUAN_CHANNEL_LIST_VIEW:
			return createJingxuanChannelListView();
		case TYPE_JINGXUAN_FAKE_LOADING_VIEW:
			return createJxFakeLoadingView();
		default:
			break;
		}
		
		return null;
	}
}
