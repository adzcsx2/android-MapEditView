package com.intelrobotdemo;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.view.MapEditView;

public class MapEditActivity extends Activity implements OnClickListener {

	View btn_move, btn_draw, btn_clear, btn_back,btn_finish;
	MapEditView mapEditView;

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
		byte[] mapByteArray = intent.getByteArrayExtra("map");
		Bitmap bitmap=BitmapFactory.decodeByteArray(mapByteArray, 0, mapByteArray.length); 
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
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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
			Intent data = new Intent();
			Bitmap map = mapEditView.getMap();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			map.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] mapByte = baos.toByteArray();
			data.putExtra("map", mapByte);
			setResult(RESULT_OK,data);
			MapEditActivity.this.finish();
			break;

		}
	}

}
