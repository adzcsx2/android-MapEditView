package com.intelrobotdemo;

import java.io.ByteArrayOutputStream;

import com.view.RobotView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "MainActivity";
	private static final int RESULT_LOAD_IMAGE = 0;
	private static final int RESULT_MAP_EDIT = 1;
	private RobotView robotView;
	private boolean isRun = false;
	private View btn_choose, btn_edit, btn_clear, btn_rePlan, btn_stop,
			btn_start;

	
	public Thread t;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewInit();
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String request_get = HttpUtil.request_get(HttpUtil.getInitPos);
				Float[] position = HttpUtil.getPosition(request_get);
				Log.e(TAG, "x = " + position[0] + "  ,  y = " + position[1]);
			}
		}).start();

	}

	private void ViewInit() {
		robotView = (RobotView) findViewById(R.id.robotView);

		Bitmap map = BitmapFactory.decodeResource(getResources(),
				R.drawable.map).copy(Bitmap.Config.ARGB_8888, true);
		robotView.setMap(map);

		btn_choose = findViewById(R.id.btn_choose);
		btn_choose.setOnClickListener(this);
		btn_edit = findViewById(R.id.btn_edit);
		btn_edit.setOnClickListener(this);
		btn_clear = findViewById(R.id.btn_clear);
		btn_clear.setOnClickListener(this);
		btn_rePlan = findViewById(R.id.btn_rePlan);
		btn_rePlan.setOnClickListener(this);
		btn_stop = findViewById(R.id.btn_stop);
		btn_stop.setOnClickListener(this);
		btn_start = findViewById(R.id.btn_start);
		btn_start.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.btn_choose:
			intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, RESULT_LOAD_IMAGE);
			break;
		case R.id.btn_edit:
			intent = new Intent(MainActivity.this, MapEditActivity.class);
			Bitmap map = robotView.getMap();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			map.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] mapByte = baos.toByteArray();
			intent.putExtra("map", mapByte);
			startActivityForResult(intent, RESULT_MAP_EDIT);
			break;
		case R.id.btn_rePlan:
			robotView.clear_historyAim();
			break;
		case R.id.btn_clear:
			clear();
			break;
		case R.id.btn_stop:
			isRun = false;
			btn_start.setClickable(true);
			btn_rePlan.setClickable(true);
			btn_clear.setClickable(true);
			break;
		case R.id.btn_start:
			isRun = true;
			btn_start.setClickable(false);
			btn_rePlan.setClickable(false);
			btn_clear.setClickable(false);
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (isRun) {
						robotView.setStart_x(robotView.getStart_x() + 5);
						robotView.setStart_y(robotView.getStart_y() + 5);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();
			break;
		default:
			break;
		}
	}

	private void clear(){
		robotView.clear_historyAim();
		robotView.clear_historyRobot();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case R.id.action_others:
			Intent intent = new Intent(MainActivity.this,
					OthersInfoActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			// String picturePath contains the path of selected Image
			Bitmap map = BitmapFactory.decodeFile(picturePath);
			robotView.setMap(map);
			clear();
		} else if (requestCode == RESULT_MAP_EDIT && resultCode == RESULT_OK
				&& null != data) {
			byte[] mapByteArray = data.getByteArrayExtra("map");
			Bitmap bitmap = BitmapFactory.decodeByteArray(mapByteArray, 0,
					mapByteArray.length);
			robotView.setMap(bitmap);
		}
	}
	
}
