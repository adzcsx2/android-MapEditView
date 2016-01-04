package com.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.util.SerializablePath;
import com.util.Utils;

public class MapEditView extends SurfaceView implements SurfaceHolder.Callback {

	public static final int PAINT_LINE = 0;
	public static final int PAINT_CIRCLE = 1;
	public static final int PAINT_RECT = 2;
	public static final int PAINT_TEXT = 3;

	private static final String TAG = "MapEditView";
	private static final float MIN_RATE = 0.5f, MAX_RATE = 100f;
	private int PAINTMODE = 0;
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
	private SerializablePath path = new SerializablePath();
	private int paint_color = 0;
	private ArrayList<SerializablePath> pathList = new ArrayList<SerializablePath>();
	private ArrayList<Integer> pathColorList = new ArrayList<Integer>();
	private Canvas canvas;

	public MapEditView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		holder = this.getHolder();
		holder.addCallback(this);
		p = new Paint();
		paint_color = Color.RED;
		p.setTextSize(10);
		p.setAntiAlias(true);
		p.setStyle(Style.STROKE);
	}

	public MapEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder = this.getHolder();
		p = new Paint();
		paint_color = Color.RED;
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
		paint_color = Color.RED;
		p.setTextSize(10);
		p.setAntiAlias(true);
		p.setStyle(Style.STROKE);
	}

	public ArrayList<SerializablePath> getPathList() {
		return pathList;
	}

	public ArrayList<Integer> getPathColorList() {
		return pathColorList;
	}

	public void setPaint_color(int paint_color) {
		this.paint_color = paint_color;
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

	public void setPaintMode(int mode) {
		this.PAINTMODE = mode;
	}

	public void clear() {
		path.reset();
		pathList.clear();
		pathColorList.clear();
		draw(canvas);
	}

	public void back() {
		if (pathList.size() != 0) {
			path.reset();
			pathList.remove(pathList.size() - 1);
			pathColorList.remove(pathColorList.size() - 1);
			draw(canvas);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
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
	private float init_x = 0, init_y = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/** 处理单点、多点触摸 **/
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 单点触摸
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "单点down");
			isMove = false;
			init_x = down_x = event.getX() / rate;
			init_y = down_y = event.getY() / rate;

			isPointer = false;
			first_time = System.currentTimeMillis();
			path = new SerializablePath();
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
				pathColorList.add(p.getColor());
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

					for (SerializablePath paths : pathList) {
						paths.offset(off_x, off_y);
					}

				} else {
					end_x = event.getX() / rate;
					end_y = event.getY() / rate;
					switch (PAINTMODE) {
					case PAINT_LINE:
						path.lineTo(end_x, end_y);
						break;
					case PAINT_CIRCLE:
						path.reset();
						path.addCircle(init_x, init_y, (int) Utils.getDistance(
								init_x, init_y, end_x, end_y),
								SerializablePath.Direction.CW);
						break;
					case PAINT_RECT:
						path.reset();
						path.addRect(init_x, init_y, end_x, end_y,
								SerializablePath.Direction.CW);
						break;
					case PAINT_TEXT:
						break;
					}
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

	public void draw(Canvas canvas) {
		synchronized (holder) {
			// 避免第一次绘制报hasLock的exception
			if (canvas == null) {
				canvas = holder.lockCanvas();
				this.canvas = canvas;
			} else {
				holder.lockCanvas();
			}
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
		myDraw(canvas, true);
		return bitmap;
	}

	private void myDraw(Canvas canvas, boolean isSave) {
		canvas.drawColor(Color.WHITE);
		// 将比例、位移初始化
		if (isSave) {
			rate = 1;
			for (SerializablePath paths : pathList) {
				paths.offset(-position_x, -position_y);
			}
			position_x = 0;
			position_y = 0;
		}

		if (pointer_centerX != 0 && pointer_centerY != 0) {
			canvas.scale(rate, rate, 0, 0);
		}
		canvas.drawBitmap(map, position_x, position_y, p);
		for (int i = 0; i < pathList.size(); i++) {
			p.setColor(pathColorList.get(i));
			canvas.drawPath(pathList.get(i), p);
		}
		p.setColor(paint_color);
		canvas.drawPath(path, p);
	}

}