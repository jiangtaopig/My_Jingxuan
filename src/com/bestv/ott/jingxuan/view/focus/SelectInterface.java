package com.bestv.ott.jingxuan.view.focus;

import android.view.View;
import android.widget.AdapterView;

public interface SelectInterface {
	public void onSelectedEvent(AdapterView<?> parent, View view, int position,
			long id);
	public void onNothingSelectedEvent();
}
