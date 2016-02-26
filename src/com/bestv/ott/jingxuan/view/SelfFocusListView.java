package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.bestv.ott.jingxuan.view.focus.FocusAnimation;

public class SelfFocusListView extends ListView {
	private FocusAnimation mFocusAni;

	public SelfFocusListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SelfFocusListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SelfFocusListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub
		super.setSelection(position);
		Log.d("setSelection", "FocusAni != null:"+(mFocusAni != null)+" position >= 0:"+(position >= 0)+" getAdapter() != null:"+(getAdapter() != null));
		if (mFocusAni != null && position >= 0 && (getAdapter() != null)){
			mFocusAni.drawFocus((View)getAdapter().getView(position, null, null), this);
		}
	}
	
	public void setFocusAnimation(FocusAnimation fs){
		mFocusAni = fs;
	}
}
