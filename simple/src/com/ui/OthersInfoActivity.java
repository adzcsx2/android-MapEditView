package com.ui;

import com.intelrobotdemo.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class OthersInfoActivity extends Activity{

	private TextView tv_search,tv_tem,tv_light;
	private boolean isRun = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_others);
		
		tv_search = (TextView) findViewById(R.id.tv_search);
		tv_tem = (TextView) findViewById(R.id.tv_tem);
		tv_light = (TextView) findViewById(R.id.tv_light);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isRun = true;
		
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isRun = false;
	}
}
