package com.bestv.ott.jingxuan;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.proxy.qos.PageVisitedLog;
import com.bestv.ott.proxy.qos.QosBaseActivity;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ViewSwitcher.ViewFactory;

public class WelcomeActivity extends QosBaseActivity implements OnClickListener {
	private static final String TAG = "WelcomeActivity";
	private ImageSwitcher mImgSwitcher = null;
	private int[] mWelcomeImgIds = new int[] { R.drawable.jx_iotv_welcome_1,
			R.drawable.jx_iotv_welcome_2, R.drawable.jx_iotv_welcome_3 };
	private int mImgIndex = 0;
	private Button mBtnWelcome = null;
	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jx_iotv_welcome_activity);
		initData();
		initViews();
	}

	private void initData() {
		Logger.d(TAG, "enter initData");
		mContext = (Context) this;
	}

	private void initViews() {
		Logger.d(TAG, "enter initViews");
		mBtnWelcome = (Button) findViewById(R.id.iotv_btn_welcome);
		mBtnWelcome.setOnClickListener(this);
		mImgSwitcher = (ImageSwitcher) findViewById(R.id.iotv_switcher_welcome);
		mImgSwitcher.setFactory(new ViewFactory() {

			@Override
			public View makeView() {
				// TODO Auto-generated method stub
				ImageView imageView = new ImageView(mContext);
				imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
						ImageSwitcher.LayoutParams.MATCH_PARENT,
						ImageSwitcher.LayoutParams.MATCH_PARENT));
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
				return imageView;
			}
		});
		if (mWelcomeImgIds != null && mWelcomeImgIds.length > 0) {
			mImgSwitcher.setImageResource(mWelcomeImgIds[mImgIndex]);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (mImgIndex > 0 && mWelcomeImgIds.length > 0) {
				mImgIndex--;
				mImgSwitcher.setInAnimation(AnimationUtils.loadAnimation(
						getApplication(), android.R.anim.slide_in_left));
				mImgSwitcher.setOutAnimation(AnimationUtils.loadAnimation(
						getApplication(), android.R.anim.slide_out_right));
				mImgSwitcher.setImageResource(mWelcomeImgIds[mImgIndex]);
				setBtnText();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (mImgIndex < mWelcomeImgIds.length - 1) {
				mImgIndex++;
				mImgSwitcher.setInAnimation(AnimationUtils.loadAnimation(
						getApplication(), R.anim.jx_slide_in_right));
				mImgSwitcher.setOutAnimation(AnimationUtils.loadAnimation(
						getApplication(), R.anim.jx_slide_out_left));
				mImgSwitcher.setImageResource(mWelcomeImgIds[mImgIndex]);
				setBtnText();
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			leave();
			break;
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void setBtnText() {
		Logger.d(TAG, "enter setBtnsVisible");
		Logger.d(TAG, "mImgIndex: " + mImgIndex);
		if (mImgIndex == mWelcomeImgIds.length - 1) {
			mBtnWelcome.setText(R.string.jx_try_now);
			mBtnWelcome.setBackgroundResource(R.drawable.jx_iotv_dialog_selector);
		} else {
			mBtnWelcome.setText(R.string.jx_skip);
			mBtnWelcome.setBackgroundResource(R.drawable.jx_iotv_dialog_gery_selector);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.iotv_btn_welcome) {
			leave();
		} 
//		switch (v.getId()) {
//		case R.id.iotv_btn_welcome:
//			leave();
//			break;
//
//		default:
//			break;
//		}
	}

	private void leave() {
		try {
			int curVer = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionCode;
			Logger.d("current version = " + curVer + ", write version sharepreferences");
			DataConfig.setDataConfigValueI(mContext,
					DataConfig.VERSION_CODE_VALUE, curVer);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
	}
	
	@Override
	protected void setPageVisitedLogParam() {
		mPageVisitedLog = new PageVisitedLog();
		mPageVisitedLog.setPageName("JingxuanWelcomePage"); 
		mPageVisitedLog.setPageType(PageVisitedLog.PAGE_TYPE_OTHER);
		mPageVisitedLog.setContentType(PageVisitedLog.CONTENT_TYPE_JINGXUAN); 
	}
}
