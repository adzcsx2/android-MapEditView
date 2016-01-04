package com.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.intelrobotdemo.R;
import com.view.ColorPickView;
import com.view.ColorPickView.OnColorChangedListener;
import com.view.MapEditView;

public class MapEditActivity extends Activity implements OnClickListener {

	private static final String TAG = "MapEditActivity";
	View btn_move, btn_draw, btn_clear, btn_back, btn_finish;
	MapEditView mapEditView;
	int paint_color = 0;
	String map_path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapedit);
		ViewInit();
	}

	private void ViewInit() {
		mapEditView = (MapEditView) findViewById(R.id.mapEditView);
		Intent intent = getIntent();
		map_path = intent.getStringExtra("map");
		Log.e(TAG, map_path);
		Bitmap bitmap = BitmapFactory.decodeFile(map_path);
		mapEditView.setMap(bitmap);
		btn_move = findViewById(R.id.btn_move);
		btn_draw = findViewById(R.id.btn_draw);
		btn_clear = findViewById(R.id.btn_clear);
		btn_back = findViewById(R.id.btn_back);
		btn_finish = findViewById(R.id.btn_finish);
		btn_move.setOnClickListener(this);
		btn_clear.setOnClickListener(this);
		btn_back.setOnClickListener(this);
		btn_draw.setOnClickListener(this);
		btn_finish.setOnClickListener(this);
		findViewById(R.id.paint_line).setOnClickListener(this);
		findViewById(R.id.paint_circle).setOnClickListener(this);
		findViewById(R.id.paint_rect).setOnClickListener(this);
		findViewById(R.id.btn_colorpicker).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent data = null;
		switch (v.getId()) {
		case R.id.btn_move:
			mapEditView.setCanMove(true);
			break;
		case R.id.btn_draw:
			mapEditView.setCanMove(false);
			break;
		case R.id.btn_clear:
			mapEditView.clear();
			break;
		case R.id.btn_back:
			mapEditView.back();
			break;
		case R.id.btn_finish:
			// 复制一个文件，文件名为原文件名+a
			data = new Intent();
			if (mapEditView.getPathList().size() == 0) {
				// 没有改变原图
				data.putExtra("map", map_path);
			} else {
				String newPath = map_path.replace(".", "a.");
				File file = new File(newPath);
				data = new Intent();
				data.putExtra("map", newPath);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file);
					Bitmap bitmap = mapEditView.getMap();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
					MediaStore.Images.Media.insertImage(getContentResolver(), newPath, "title", "description");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			setResult(RESULT_OK, data);
			MapEditActivity.this.finish();
			break;
		case R.id.btn_colorpicker:
			final Dialog dialog = new Dialog(MapEditActivity.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			View view = LayoutInflater.from(MapEditActivity.this).inflate(
					R.layout.dialog_colorpicker, null);
			view.findViewById(R.id.btn_cancel).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
			view.findViewById(R.id.btn_commit).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if (paint_color != 0) {
								mapEditView.setPaint_color(paint_color);
							}
							dialog.dismiss();
						}
					});
			ColorPickView cp = (ColorPickView) view
					.findViewById(R.id.view_colorpicker);
			final View view_color = view.findViewById(R.id.view_color);
			view_color.setBackgroundColor(paint_color);
			cp.setOnColorChangedListener(new OnColorChangedListener() {

				@Override
				public void onColorChange(int color) {
					// TODO Auto-generated method stub
					paint_color = color;
					view_color.setBackgroundColor(paint_color);
				}
			});
			dialog.setContentView(view);
			dialog.show();
			break;
		case R.id.paint_line:
			mapEditView.setPaintMode(MapEditView.PAINT_LINE);
			break;
		case R.id.paint_circle:
			mapEditView.setPaintMode(MapEditView.PAINT_CIRCLE);
			break;
		case R.id.paint_rect:
			mapEditView.setPaintMode(MapEditView.PAINT_RECT);
			break;

		}
	}

}
