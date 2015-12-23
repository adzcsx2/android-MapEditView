package com.view;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.intelrobotdemo.R;

public class MapEditView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "MapEditView";
	private static final float MIN_RATE = 0.5f, MAX_RATE = 100f;
	private SurfaceHolder holder;
	private float end_x = 0, end_y = 0, start_x, start_y;
	private boolean isPointer = false;
	// 放大比例
	private float rate = 1, oldRate = 1;
	// 偏移距离
	private float off_x = 0, off_y = 0, position_x = 0, position_y = 0;
	// 记录第一次触屏时线段的距离
	private float oldLineDistance;
	private boolean isScaleFirst = true;
	private boolean isMove = false;

	private Bitmap map = null;

	private boolean canMove = false;

	private Paint p;
	private Path path = new Path();
	private LinkedList<Path> pathList = new LinkedList<Path>();
	private Canvas canvas;

	public MapEditView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		holder = this.getHolder();
		holder.addCallback(this);
		p = new Paint();
		p.setColor(Color.RED);
		p.setTextSize(10);
		p.setAntiAlias(true);
		p.setStyle(Style.STROKE);
	}

	public MapEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder = this.getHolder();
		p = new Paint();
		p.setColor(Color.RED);
		p.setTextSize(10);
		p.setAntiAlias(true);
		p.setStyle(Style.STROKE);
		holder.addCallback(this);
	}

	public MapEditView(Context context) {
		super(context);
		holder = this.getHolder();
		holder.addCallback(this);
		p = new Paint();
		p.setColor(Color.RED);
		p.setTextSize(10);
		p.setAntiAlias(true);
		p.setStyle(Style.STROKE);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		canvas = getHolder().lockCanvas();
		draw(canvas);
	}

	public float getStart_x() {
		return start_x;
	}

	public void setStart_x(float start_x) {
		this.start_x = start_x;
	}

	public float getStart_y() {
		return start_y;
	}

	public void setStart_y(float start_y) {
		this.start_y = start_y;
	}

	public void setMap(Bitmap map) {
		this.map = map;
	}

	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}

	public Bitmap getMap() {
		return saveDraw();
	}

	public void clear() {
		path.reset();
		pathList.clear();
		draw(canvas);
	}

	public void back() {
		path.reset();
		pathList.removeLast();
		draw(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private long down_time;
	private long first_time, last_time;
	// 两点触控的中间点
	private float pointer_centerX = 0, pointer_centerY = 0;
	private float down_x = 0, down_y = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/** 处理单点、多点触摸 **/
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 单点触摸
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "单点down");
			isMove = false;
			down_x = event.getX() / rate;
			down_y = event.getY() / rate;
			isPointer = false;
			first_time = System.currentTimeMillis();
			path = new Path();
			path.moveTo((event.getX()) / rate, (event.getY()) / rate);
			draw(canvas);

			break;
		case MotionEvent.ACTION_UP:
			last_time = System.currentTimeMillis();
			down_time = last_time - first_time;
			if (!isPointer) {
				if (down_time >= 200) {
					if (!isMove) {
						// 长按
						Log.i(TAG, "长按");
						start_x = event.getX() / rate - position_x;
						start_y = event.getY() / rate - position_y;
					}
				} else {
					// 短按
					Log.i(TAG, "短按");
					end_x = event.getX() / rate - position_x;
					end_y = event.getY() / rate - position_y;
				}
				pathList.add(path);
			}
			break;
		// 多点触摸
		case MotionEvent.ACTION_POINTER_DOWN:
			isPointer = true;
			Log.i(TAG, "多点down");
			break;
		case MotionEvent.ACTION_MOVE:
			// 缩放
			draw(canvas);
			if (isPointer) {
				Log.i(TAG, "多点move");
				if (isScaleFirst) {
					// 得到第一次触屏时线段的长度
					oldLineDistance = (float) Math.sqrt(Math.pow(event.getX(1)
							- event.getX(0), 2)
							+ Math.pow(event.getY(1) - event.getY(0), 2));
					isScaleFirst = false;
				} else {
					// 得到触屏时线段的长度
					float newLineDistance = (float) Math.sqrt(Math.pow(
							event.getX(1) - event.getX(0), 2)
							+ Math.pow(event.getY(1) - event.getY(0), 2));
					// 得到触屏时中间点，用来确定缩放中心
					pointer_centerX = (event.getX(1) + event.getX(0)) / 2;
					pointer_centerY = (event.getY(1) + event.getY(0)) / 2;

					// 获取本次的缩放比例
					rate = oldRate * newLineDistance / oldLineDistance;
					if (rate < MIN_RATE)
						rate = MIN_RATE;
					if (rate > MAX_RATE)
						rate = MAX_RATE;
					Log.i(TAG, "比例:" + rate);
				}
			}
			// 位移
			else {
				Log.i(TAG, "单点move");
				if (canMove) {
					isMove = true;
					off_x = event.getX() / rate - down_x;
					off_y = event.getY() / rate - down_y;
					position_x += off_x;
					position_y += off_y;
					down_x = event.getX() / rate;
					down_y = event.getY() / rate;

					for (Path paths : pathList) {
						paths.offset(off_x, off_y);
					}

				} else {
					end_x = event.getX() / rate - position_x;
					end_y = event.getY() / rate - position_y;
					path.lineTo((event.getX()) / rate, (event.getY()) / rate);
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Log.i(TAG, "多点up");
			oldRate = rate;
			break;
		}
		return true;
	}

	@Override
	public void draw(Canvas canvas) {
		synchronized (holder) {
//			holder.lockCanvas();
			// //////////////////////////////////////////////
			myDraw(canvas, false);
			// ///////////////////////////////////////////////
			holder.unlockCanvasAndPost(canvas);
		}
	}

	public Bitmap saveDraw() {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		// 把屏幕的内容画在新画布上
		myDraw(canvas,true);
		return bitmap;
	}

	private void myDraw(Canvas canvas, boolean isSave) {
		canvas.drawColor(Color.WHITE);
		//将比例、位移初始化
		if (isSave) {
			rate = 1;
			for (Path paths : pathList) {
				paths.offset(-position_x, -position_y);
			}
			position_x = 0;
			position_y = 0;
		}

		if (pointer_centerX != 0 && pointer_centerY != 0) {
			canvas.scale(rate, rate, 0, 0);
		}
		canvas.drawBitmap(map, position_x, position_y, p);
		for (Path paths : pathList) {
			canvas.drawPath(paths, p);
		}
		canvas.drawPath(path, p);
	}

}