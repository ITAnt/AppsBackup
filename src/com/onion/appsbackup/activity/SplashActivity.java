package com.onion.appsbackup.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

import com.onion.appsbackup.R;
import com.onion.appsbackup.model.User;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		// 初始化友盟
        MobclickAgent.updateOnlineConfig(this);
		// 初始化 Bmob SDK
        Bmob.initialize(this, "eede8a6319740556e6a80c7cd7985753");
        
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				User user = BmobUser.getCurrentUser(SplashActivity.this, User.class);
				if (user != null) {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
				} else {
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
				}
				
				finish();
			}
		}, 2500);
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
}
