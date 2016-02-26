package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestv.jingxuan.R;

public class JingxuanSubProgram extends FrameLayout {
	private static final String TAG = "JingxuanSubProgram";
	
	private TextView subProgram = null;
	
	private boolean mIsCurrentPlayProgram =false;
//	private OnProgramProgressFinishListener mFinishListener = null;

	public JingxuanSubProgram(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public JingxuanSubProgram(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	public JingxuanSubProgram(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}

	private void initViews(Context context) {
		Log.d(TAG, "enter initViews");
		LayoutInflater.from(context).inflate(R.layout.jx_jingxuan_iotv_sub_program, this);
		subProgram = (TextView) findViewById(R.id.subProgram);
	}

	public void setProgramTime(String time) {
		//mProgramTime.setText(time);
	}

	public void setProgramName(String name) {
		subProgram.setText(name);
	}

	public String getProgramName() {
		return subProgram.getText().toString();
	}
}
