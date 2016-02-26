package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bestv.jingxuan.R;

public class MediaSeekProgress extends ProgressBar {
	private Paint mPaint;
	private static final int TEXT_SIZE = 20;
    private int MAX_PROGRESS_HEIGHT;
	
	private Drawable mProgressDrawable;
	private Drawable mProgressEnableDrawable;
	private Drawable mBackgroundDrawable;
	
	private NinePatch mThumbDrawable;
	private int mThumbWidth;
	private int mThumbHeight;
	
	private Rect mCurrentBounds = null;
	private int mMaxEnablePos = 0;
	
	String mCurrentTime;
	
	public MediaSeekProgress(Context context) {
		super(context);
		initText();
	}

	public MediaSeekProgress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initText();
	}

	public MediaSeekProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		initText();
	}

	public synchronized void setProgress(int progress) {
		if (mMaxEnablePos > 0 && progress > mMaxEnablePos){
			return;
		}
		
		super.setProgress(progress);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		if (mCurrentBounds == null){
			mCurrentBounds = new Rect(this.getPaddingLeft(), this.getPaddingTop(), this.getWidth() - this.getPaddingRight(), MAX_PROGRESS_HEIGHT);
			mBackgroundDrawable.setBounds(mCurrentBounds);
		}
		
		mBackgroundDrawable.draw(canvas);
		//Log.d("harish", "rate_enable = " + mMaxEnablePos);
		//Log.d("harish", "curP = " + curpos + " max = " + getMax() + " width = " + mCurrentBounds.width() + " rate = "
		//		+ rate + "  result = " + (int) (mCurrentBounds.width() * rate));		
		//Log.d("harish", "left = " + r.left + " top = " + r.top + " width = " + r.width() + " height = " + r.height());
		if (mMaxEnablePos > 0) {
			float epos = mMaxEnablePos;
			
			float rate_enable = epos / getMax();
			//Log.d("harish", "rate_enable = " + rate_enable);
			Rect renable = new Rect(mCurrentBounds.left, mCurrentBounds.top, mCurrentBounds.left + (int) (mCurrentBounds.width() * rate_enable), MAX_PROGRESS_HEIGHT);
			mProgressEnableDrawable.setBounds(renable);
			mProgressEnableDrawable.draw(canvas);
		}
		
		float curpos = this.getProgress();

		float rate = curpos / this.getMax();	
		Rect r = new Rect(mCurrentBounds.left, mCurrentBounds.top, mCurrentBounds.left + (int) (mCurrentBounds.width() * rate), MAX_PROGRESS_HEIGHT);
		
		mProgressDrawable.setBounds(r);
		mProgressDrawable.draw(canvas);
		
		int left = mCurrentBounds.left + r.width() - mThumbWidth / 2 + 3;
		int top = mCurrentBounds.top - mThumbHeight / 2 + 5;
		int right = left + mThumbWidth;
		int bottom = top + mThumbHeight;
		
		RectF rThumb = new RectF(left, top, right, bottom);
		mThumbDrawable.draw(canvas, rThumb);
		
		float textwidth = mPaint.measureText(mCurrentTime);
		float text_x = rThumb.left + rThumb.width() / 2 - textwidth / 2;
		float text_y = rThumb.top;
		
		canvas.drawText(mCurrentTime, text_x, text_y, mPaint);
	}
	
	public void setContext(ViewGroup vg) {
	}

	private void initText() {
		MAX_PROGRESS_HEIGHT = getContext().getResources().getDimensionPixelSize(R.dimen.jx_progress_bar_height);
				
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setColor(Color.WHITE);
		this.mPaint.setTextSize(TEXT_SIZE);
		mCurrentTime = "00:00:00";
		
		Resources resource = getContext().getResources();
		mBackgroundDrawable = resource.getDrawable(R.drawable.jx_iotv_progressbarpanel);
		mProgressDrawable = resource.getDrawable(R.drawable.jx_iotv_progressbarcurrenttime);
		mProgressEnableDrawable = mProgressDrawable;
		
        Bitmap bmp_9path = BitmapFactory.decodeResource(getResources(), R.drawable.jx_iotv_process_new);
        mThumbWidth = bmp_9path.getWidth();
        mThumbHeight = bmp_9path.getHeight();
        mThumbDrawable = new NinePatch(bmp_9path, bmp_9path.getNinePatchChunk(), null);  
	}

	public void setText(int resID) {
		String str = getResources().getString(resID);
		mCurrentTime = str;
	}

	public void setText(String str) {
		mCurrentTime = str;
	}

	public String getText() {
		return mCurrentTime;
	}
	
	public void setMaxEnablePos(int pos){
		if (pos > getMax()){
			pos = getMax();
		}
		
		mMaxEnablePos = pos;
	}
}
