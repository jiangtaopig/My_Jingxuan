package com.bestv.ott.jingxuan.view.focus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bestv.jingxuan.R;

/**
 * 用于绘制Foucs效果
 * 
 * @author fan.jianfeng
 * 
 */
public class FocusAnimation {
	private static final String TAG = "FocusAnimation";
	private Context mContext;
	/**
	 * 用来画选中放大效果，最上层的FrameLayout
	 */
	private FrameLayout mFrameLayout = null;
	/**
	 * 设置有焦点功能，但没有呼吸框及移动动画
	 * 注意：设置了这个，使用时必须要实现FoucsInterface中FoucsEvent方法，因为你设置的VIEW获得焦点后，会调用这个方法
	 */
	private List<View> mNoAniViews = new ArrayList<View>();
	/**
	 * 放大的焦点图片
	 */
	private ImageView mScaleUpFoucs = null;
	/**
	 * 自定义Foucs图片的偏移量
	 */
	private int mFoucsOffset = 27;
	/**
	 * Foucs框透明度
	 */
	private int mFoucsAlpha = 255;
	/**
	 * 焦点框资源ID
	 */
	private int mFoucsResID = R.drawable.jx_iotv_tv_focus;
	private FocusInterface mFocusListener = null;
	private View mCurView;
	private Object mLockFocusView;

	public FocusAnimation(final Context context, final FrameLayout frameLayout) {
		this.mContext = context;
		mFrameLayout = frameLayout;
		mScaleUpFoucs = new ImageView(mContext);
		mScaleUpFoucs.setScaleType(ScaleType.FIT_XY);
	}

	// 设置焦点框偏移量
	public void setOffset(int offset) {
		mFoucsOffset = offset;
	}

	public void setFrameLayoutVisible() {
		mFrameLayout.setVisibility(View.VISIBLE);
	}

	public void setFrameLayoutInvisible() {
		mFrameLayout.setVisibility(View.INVISIBLE);
	}
	
	public void setLockFocusView(Object av){
		mLockFocusView = av;
	}

	/**
	 * 传入的ViewGroup下所有的View都会有Foucs效果
	 * 
	 * @param views
	 */
	public void setAllViewChangeFoucs(ViewGroup views) {
		for (int i = 0; i < views.getChildCount(); i++) {
			View v = views.getChildAt(i);
			foucsChange(v);
			if (v instanceof ViewGroup) {
				setAllViewChangeFoucs((ViewGroup) v);
			}
		}
	}

	public void setFocusListener(FocusInterface focusListener) {
		mFocusListener = focusListener;
	}

	/**
	 * 使该View会有Foucs效果
	 * 
	 * @param v
	 */
	public void foucsChange(View v) {
		if (v.isFocusable()) {
			v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					Log.d(TAG, "enter foucsChange");
					if (mNoAniViews != null && mNoAniViews.contains(v)) {
						Log.d(TAG, "mNoAniView");
					} else {
						if (hasFocus) {
							drawBound(v);
						} else {
						}
					}
					
					if (mFocusListener != null) {
						mFocusListener.onFoucsEvent(v, hasFocus);
					} else {
						Log.w(TAG, "mFocusListener is null");
					}
					
				}
			});
		}
	}

	/**
	 * 对于GridView和ListView的内容设置Focus效果
	 * 
	 * @param v
	 */
	public void selectChange(AbsListView v, final SelectInterface sl) {
		Log.d(TAG, "enter selectChange");
		if (v.isFocusable()) {
			Log.d(TAG, "setOnItemSelectedListener");
			v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				SelectInterface selectListener = sl;
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					drawBound(view, parent);
					
					if(selectListener != null){
						selectListener.onSelectedEvent(parent, view, position, id);
					}else{
						Log.w(TAG, "mSelectListener is null");
					}
					Log.d(TAG, "onItemSelected view :" + view + ",position : "+ position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					if(selectListener != null){
						selectListener.onNothingSelectedEvent();
					}
				}
			});
		}

	}

	/**
	 * 添加没有Foucs效果的控件
	 * 
	 * @param v
	 */
	public void addNoAnimationView(View v) {
		mNoAniViews.add(v);
	}

	/**
	 * 设置Foucs框图片，必须为9.png类型
	 * 
	 * @param resource
	 */
	public void setFoucsImage(int resource) {
		mFoucsResID = resource;
	}

	// 通过资源ID取9图
	// private NinePatch getNinePatch(int resource) {
	// Bitmap bitmap = BitmapFactory.decodeResource(
	// this.mContext.getResources(), resource);
	// return (bitmap != null) ? new NinePatch(bitmap,
	// bitmap.getNinePatchChunk(), null) : null;
	// }
	
	public void drawFocus(View v){
		drawBound(v);
	}
	
	public void drawFocus(View v, AdapterView<?> av){
		drawBound(v, av);
	}
	
	/**
	 * 放大特效
	 * 
	 * @param v
	 */
	private boolean drawBound(View v) {
		boolean flag = false;
		//Log.d("harish", "enter scaleUp v" + v + " mFrameLayout = " + mFrameLayout);
		if (mFrameLayout == null || v == null || mCurView == v) {
			Log.e(TAG, "zoom framelayout or view is null");
			return flag;
		}
		
		mCurView = v;
		mFrameLayout.removeAllViews();
		// 取画图位置
		Rect rect = new Rect();
		v.getGlobalVisibleRect(rect);

		int top = rect.top;
		int left = rect.left;
		// int right = rect.right;
		// int bottom = rect.bottom;
		// int cx = left + Math.max((right - left), 0) / 2;
		// int cy = top + Math.max((bottom - top), 0) / 2;
		int width = rect.width();
		int height = rect.height();

		mScaleUpFoucs.setImageResource(mFoucsResID);
		FrameLayout.LayoutParams frameLP = (FrameLayout.LayoutParams) mScaleUpFoucs
				.getLayoutParams();
		if (frameLP == null) {
			frameLP = new FrameLayout.LayoutParams(width, height);
		}
		frameLP.width = width + mFoucsOffset * 2;
		frameLP.height = height + mFoucsOffset * 2;
		frameLP.leftMargin = Math.max(left - mFoucsOffset, 0);
		frameLP.topMargin = Math.max(top - mFoucsOffset, 0);

		mFrameLayout.addView(mScaleUpFoucs, frameLP);

		return flag;
	}
	
	/**
	 * 放大特效
	 * 
	 * @param v
	 */
	private boolean drawBound(View v, AdapterView<?> parentView) {
		boolean flag = false;
		//Log.d("harish", "enter scaleUp v" + v + " mFrameLayout = " + mFrameLayout);
		if (mFrameLayout == null || v == null || mCurView == v) {
			Log.e(TAG, "zoom framelayout or view is null");
			return flag;
		}
		
		if (parentView != null && mLockFocusView != null){
			if (mLockFocusView != parentView){
				return flag;
			}
		}
		
		mCurView = v;
		mFrameLayout.removeAllViews();
		// 取画图位置
		Rect rect = new Rect();
		v.getGlobalVisibleRect(rect);

		int top = rect.top;
		int left = rect.left;
		// int right = rect.right;
		// int bottom = rect.bottom;
		// int cx = left + Math.max((right - left), 0) / 2;
		// int cy = top + Math.max((bottom - top), 0) / 2;
		int width = rect.width();
		int height = rect.height();

		mScaleUpFoucs.setImageResource(mFoucsResID);
		FrameLayout.LayoutParams frameLP = (FrameLayout.LayoutParams) mScaleUpFoucs
				.getLayoutParams();
		if (frameLP == null) {
			frameLP = new FrameLayout.LayoutParams(width, height);
		}
		frameLP.width = width + mFoucsOffset * 2;
		frameLP.height = height + mFoucsOffset * 2;
		frameLP.leftMargin = Math.max(left - mFoucsOffset, 0);
		frameLP.topMargin = Math.max(top - mFoucsOffset, 0);

		mFrameLayout.addView(mScaleUpFoucs, frameLP);

		return flag;
	}


	/**
	 * 清除屏幕上的缩放效果
	 */
	public void clearScale() {
		mFrameLayout.removeAllViews();
		mLockFocusView = null;
		mCurView = null;
	}
	
	public View getCurDrawView(){
		return mCurView;
	}
}