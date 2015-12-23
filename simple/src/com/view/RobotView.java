package com.view;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.intelrobotdemo.HttpUtil;
import com.intelrobotdemo.R;
import com.intelrobotdemo.R.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RobotView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "MapView";
	private static final float MIN_RATE = 0.5f, MAX_RATE = 5f;
	private UiThread ui_thread;
	private SurfaceHolder holder;
	private float end_x = 0, end_y = 0, start_x, start_y;
	private LinkedList<Float[]> history_robot = new LinkedList<Float[]>();
	private LinkedList<Float[]> history_aim = new LinkedList<Float[]>();
	private ExecutorService singleThread = Executors.newSingleThreadExecutor();
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

	public RobotView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		holder = this.getHolder();
		holder.addCallback(this);
		ui_thread = new UiThread(holder);
		singleThread.execute(getInitPosRunnable);
	}

	public RobotView(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder = this.getHolder();
		holder.addCallback(this);
		singleThread.execute(getInitPosRunnable);
		ui_thread = new UiThread(holder);
	}

	public RobotView(Context context) {
		super(context);
		ui_thread = new UiThread(holder);
		holder = this.getHolder();
		holder.addCallback(this);
		singleThread.execute(getInitPosRunnable);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		ui_thread = new UiThread(holder);
		ui_thread.isRun = true;
		ui_thread.start();
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

	public Bitmap getMap() {
		return this.map == null ? BitmapFactory.decodeResource(getResources(),
				R.drawable.map) : map;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "stop");
		surfaceStop();
	}

	public void surfaceStop() {
		// TODO Auto-generated method stub
		ui_thread.isRun = false;
		ui_thread.interrupt();
	}

	private long down_time;
	private long first_time, last_time;
	// 两点触控的中间点
	private float pointer_centerX = 0, pointer_centerY = 0;
	// 计算每次move时的偏移
	private float down_x = 0, down_y = 0;
	// 计算Move时距按下初始点的距离(用来判断是否是move)
	private float init_x = 0, init_y = 0;

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
			init_x = event.getX() / rate;
			init_y = event.getY() / rate;
			isPointer = false;
			first_time = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_UP:
			last_time = System.currentTimeMillis();
			down_time = last_time - first_time;
			if (!isPointer) {
				if (down_time >= 200) {
					if (!isMove) {
						// 长按
						start_x = event.getX() / rate - position_x;
						start_y = event.getY() / rate - position_y;
						singleThread.execute(setInitPosRunnable);
					}
				} else {
					// 短按
					end_x = event.getX() / rate - position_x;
					end_y = event.getY() / rate - position_y;
					singleThread.execute(setEndPosRunnable);
				}
			}
			break;
		// 多点触摸
		case MotionEvent.ACTION_POINTER_DOWN:
			isPointer = true;
			Log.i(TAG, "多点down");
			break;
		case MotionEvent.ACTION_MOVE:
			// 缩放
			if (isPointer) {
				Log.i(TAG, "多点move");
				try {
					float x1 = event.getX(1);
					float x0 = event.getX(0);
					float y1 = event.getY(1);
					float y0 = event.getY(0);
					if (isScaleFirst) {
						// 得到第一次触屏时线段的长度
						oldLineDistance = (float) Math.sqrt(Math
								.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
						isScaleFirst = false;
					} else {
						// 得到触屏时线段的长度
						float newLineDistance = (float) Math.sqrt(Math.pow(x1
								- x0, 2)
								+ Math.pow(y1 - y0, 2));
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
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// 位移
			else {
				Log.i(TAG, "单点move");
				// 每次move时的位移
				off_x = event.getX() / rate - down_x;
				off_y = event.getY() / rate - down_y;
				// move时距初始点的位移
				float init_off_x = event.getX() / rate - init_x;
				float init_off_y = event.getY() / rate - init_y;
				if (Math.sqrt(init_off_x * init_off_x + init_off_y * init_off_y) > 5) {
					isMove = true;
				} else {
					isMove = false;
				}

				position_x += off_x;
				position_y += off_y;
				down_x = event.getX() / rate;
				down_y = event.getY() / rate;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Log.i(TAG, "多点up");
			oldRate = rate;
			isScaleFirst = true;
			break;
		}
		return true;
	}

	Runnable setEndPosRunnable = new Runnable() {

		@Override
		public void run() {
			String result = HttpUtil.request_get(HttpUtil.setEndPos
					+ HttpUtil.X(end_x) + HttpUtil.Y(end_y));
			Log.i(TAG, result);
		}
	};

	Runnable setInitPosRunnable = new Runnable() {

		@Override
		public void run() {
			String result = HttpUtil.request_get(HttpUtil.setInitPos
					+ HttpUtil.X(start_x) + HttpUtil.Y(start_y));
			Log.i(TAG, result);
		}
	};
	Runnable getInitPosRunnable = new Runnable() {

		@Override
		public void run() {
			String result = HttpUtil.request_get(HttpUtil.getInitPos);
			Float[] position = HttpUtil.getPosition(result);
			start_x = position[0];
			start_y = position[1];
		}
	};

	public void clear_historyRobot() {
		history_robot.clear();
	}

	public void clear_historyAim() {
		history_aim.clear();
		end_x = 0;
		end_y = 0;

	}

	private class UiThread extends Thread {

		private static final int width = 30;

		public boolean isRun = false;
		private SurfaceHolder holder;

		public UiThread(SurfaceHolder holder) {
			super();
			this.holder = holder;
		}

		@Override
		public void run() {
			while (isRun) {
				Canvas canvas = null;
				try {
					synchronized (holder) {
						canvas = holder.lockCanvas();// 锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
						// 在surfaceDestroy的时候会报空指针
						if (canvas == null) {
							continue;
						}
						canvas.drawColor(Color.WHITE);// 设置画布背景颜色
						Paint p = new Paint();
						if (pointer_centerX != 0 && pointer_centerY != 0) {
							canvas.scale(rate, rate, 0, 0);
						}
						// 绘制位图icon
						canvas.drawBitmap(map, position_x, position_y, p);
						// 目标点
						if (end_x != 0 && end_y != 0) {
							history_aim.add(new Float[] { end_x, end_y });
							p.setColor(Color.RED);
							for (Float[] floats : history_aim) {
								RectF rect = new RectF(floats[0] + width / 2
										+ position_x, floats[1] + width / 2
										+ position_y, floats[0] - width / 2
										+ position_x, floats[1] - width / 2
										+ position_y);
								canvas.drawRect(rect, p);
							}
						}
						// 机器人的位置点
						history_robot.add(new Float[] { start_x, start_y });
						p.setColor(Color.GREEN);
						for (Float[] floats : history_robot) {
							canvas.drawCircle(floats[0] + position_x, floats[1]
									+ position_y, 10, p);
						}
						p.setColor(Color.BLUE);
						canvas.drawCircle(start_x + position_x, start_y
								+ position_y, 10, p);
						// 目前位置坐标点
						p.setColor(Color.BLACK);
						p.setTextSize(17);
						p.setTypeface(Typeface.DEFAULT_BOLD);
						p.setAntiAlias(true);
						p.setDither(true);
						canvas.drawText(start_x + "," + start_y, start_x - 80
								+ position_x, start_y + 30 + position_y, p);
						Thread.sleep(100);// 睡眠时间为100毫秒
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);// 结束锁定画图，并提交改变。
					}
				}
			}
		}
	}
}
