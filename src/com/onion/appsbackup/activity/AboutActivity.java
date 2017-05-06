package com.onion.appsbackup.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.onion.appsbackup.R;
import com.onion.appsbackup.util.AppTool;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.umeng.analytics.MobclickAgent;

public class AboutActivity extends Activity implements OnClickListener {

	private CustomedActionBar ab_about;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		ab_about = (CustomedActionBar) findViewById(R.id.ab_about);
		ab_about.setOnLeftIconClickListener(new OnLeftIconClickListener() {
			
			@Override
			public void onLeftIconClick() {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});

		TextView tv_name = (TextView) findViewById(R.id.tv_name);
		tv_name.setText(getString(R.string.label_version) + AppTool.getVersionName(this));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		default:
			break;
		}
	}
}
