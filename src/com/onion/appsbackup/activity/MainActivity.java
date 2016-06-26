package com.onion.appsbackup.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.GetListener;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.onion.appsbackup.R;
import com.onion.appsbackup.model.User;
import com.onion.appsbackup.util.BackupActivityManager;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.util.UIUtils;
import com.onion.appsbackup.view.CircleImage;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends Activity implements OnClickListener {

	private WifiManager mWifiManager;
	private RelativeLayout rl_head;
	private CircleImage ci_icon;
	private TextView tv_nickname;
	
	private LinearLayout ll_transfer;
	private LinearLayout ll_backup;
	private LinearLayout ll_restore;
	private LinearLayout ll_setting;
	
	private InfoReceiver mReceiver;
	private boolean hasLoadInfo;
	private ImageView iv_gender;
	
	private class InfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (!hasLoadInfo) {
				if (HttpTools.isNetworkConnected(getApplicationContext())) {
					loadUserInfo();
				}
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		UmengUpdateAgent.update(this);
		
		BackupActivityManager.getInstance().addActivity(this);
		loadUserInfo();
		
		rl_head = (RelativeLayout) findViewById(R.id.rl_head);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		LayoutParams params = new LayoutParams(metrics.widthPixels, metrics.widthPixels * 2 / 3);
		params.bottomMargin = UIUtils.dip2px(this, 40);
		rl_head.setLayoutParams(params);
		
		ci_icon = (CircleImage) findViewById(R.id.ci_icon);
		tv_nickname = (TextView) findViewById(R.id.tv_nickname);
		iv_gender = (ImageView) findViewById(R.id.iv_gender);
		
		
		ll_transfer = (LinearLayout) findViewById(R.id.ll_transfer);
		ll_transfer.setOnClickListener(this);
		ll_backup = (LinearLayout) findViewById(R.id.ll_backup);
		ll_backup.setOnClickListener(this);
		ll_restore = (LinearLayout) findViewById(R.id.ll_restore);
		ll_restore.setOnClickListener(this);
		ll_setting = (LinearLayout) findViewById(R.id.ll_setting);
		ll_setting.setOnClickListener(this);
		
		mReceiver = new InfoReceiver();
		IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(mReceiver, filter);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}

	private void loadUserInfo() {
		// TODO Auto-generated method stub
		BmobUser bmobUser = BmobUser.getCurrentUser(this);
		if (bmobUser != null) {
			hasLoadInfo = true;
			BmobQuery<User> user = new BmobQuery<User>();
			user.getObject(this, bmobUser.getObjectId(), new GetListener<User>() {
				
				@Override
				public void onFailure(int arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onSuccess(User user) {
					tv_nickname.setText(user.getNickName());
					iv_gender.setVisibility(View.VISIBLE);
					
					BitmapUtils bitmapUtils = new BitmapUtils(MainActivity.this);
					bitmapUtils.display(ci_icon, user.getImageUrl(), new BitmapLoadCallBack<View>() {

						@Override
						public void onLoadCompleted(View view, String arg1, Bitmap bitmap, BitmapDisplayConfig arg3, BitmapLoadFrom arg4) {
							ci_icon.setImageBitmap(bitmap);
						}

						@Override
						public void onLoadFailed(View arg0, String arg1, Drawable arg2) {
							ci_icon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cat));
						}
					});
					user.getImageUrl();
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ll_transfer:
			// 点击面对面快传
			final AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.show();
			dialog.setContentView(R.layout.dialog_choose_character);
			Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
			btn_send.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 发送
					dialog.cancel();
					startActivityForResult(new Intent(MainActivity.this, SendActivity.class), 666);
				}
			});
			
			Button btn_receive = (Button) dialog.findViewById(R.id.btn_receive);
			btn_receive.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 接收
					startActivity(new Intent(MainActivity.this, ReceiveActivity.class));
					dialog.cancel();
				}
			});
			
			break;
		case R.id.ll_backup:
			// 点击备份
			startActivity(new Intent(MainActivity.this, BackupActivity.class));
			break;
			
		case R.id.ll_restore:
			// 点击恢复
			startActivity(new Intent(MainActivity.this, RestoreActivity.class));
			break;
		case R.id.ll_setting:
			// 点击个人设置
			startActivity(new Intent(MainActivity.this, SettingActivity.class));
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case 666:
			// 关闭热点，恢复网络
			HttpTools.setWifiApEnabled(mWifiManager, null, false);
			mWifiManager.setWifiEnabled(true);
			break;

		default:
			break;
		}
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	
	long exitTime;
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if((System.currentTimeMillis() - exitTime) > 2000){  
		    Toast.makeText(this, R.string.msg_press_again_exit, Toast.LENGTH_SHORT).show();                                
		    exitTime = System.currentTimeMillis();  
		    return;
		} else {
			super.onBackPressed();
		}
	}
}
