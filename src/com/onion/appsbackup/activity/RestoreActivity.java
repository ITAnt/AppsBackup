package com.onion.appsbackup.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.GetListener;

import com.onion.appsbackup.R;
import com.onion.appsbackup.adapter.RestoreAppAdapter;
import com.onion.appsbackup.adapter.RestoreAppAdapter.OnAppItemClickListener;
import com.onion.appsbackup.model.RestoreApp;
import com.onion.appsbackup.model.User;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.util.JsonHelper;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.onion.appsbackup.view.CustomedActionBar.OnRightIconClickListener;
import com.onion.appsbackup.view.ProgressDialogUtils;
import com.umeng.analytics.MobclickAgent;

public class RestoreActivity extends Activity {

	private ListView lv_old_apps;
	private RestoreAppAdapter mAdapter;
	private List<RestoreApp> appList;
	
	private CustomedActionBar ab_restore;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore);
		
		ab_restore = (CustomedActionBar) findViewById(R.id.ab_restore);
		ab_restore.setOnLeftIconClickListener(new OnLeftIconClickListener() {
			
			@Override
			public void onLeftIconClick() {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		
		ab_restore.setOnRightIconClickListener(new OnRightIconClickListener() {
			
			@Override
			public void onRightIconClick() {
				if (HttpTools.isNetworkConnected(RestoreActivity.this)) {
					// 恢复应用
					restoreApps();
				} else {
					Toast.makeText(RestoreActivity.this, R.string.msg_check_network, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		appList = new ArrayList<RestoreApp>();
		mAdapter = new RestoreAppAdapter(this);
		mAdapter.setApps(appList);
		lv_old_apps = (ListView) findViewById(R.id.lv_old_apps);
		lv_old_apps.setAdapter(mAdapter);
		mAdapter.setOnAppItemClickListener(new OnAppItemClickListener() {
			
			@Override
			public void onAppItemClick(int position) {
				appList.get(position).setChecked(false);
				mAdapter.notifyDataSetChanged();
				// TODO Auto-generated method stub
				ClipboardManager cManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				cManager.setText(appList.get(position).getAppName());
				Toast.makeText(RestoreActivity.this, String.format(getString(R.string.msg_name_to_clipboard), cManager.getText()), Toast.LENGTH_SHORT).show();
			}
		});

		if (HttpTools.isNetworkConnected(this)) {
			// 恢复应用
			restoreApps();
		} else {
			Toast.makeText(this, R.string.msg_check_network, Toast.LENGTH_SHORT).show();
		}
	}
	
	private ProgressDialogUtils pDlgUtl;
	/**
	 * 显示加载中...
	 */
	private void showProgressDialog() {
		if (pDlgUtl == null) {
			pDlgUtl = new ProgressDialogUtils(RestoreActivity.this);
		}
		pDlgUtl.show();
	}
	private void cancelProgress() {
		if (pDlgUtl != null) {
			pDlgUtl.hide();
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		cancelProgress();
	}

	public void restoreApps() {
		// 获取已经备份好的
		showProgressDialog();
		BmobUser bmobUser = BmobUser.getCurrentUser(this);
		if (bmobUser != null) {
			BmobQuery<User> query = new BmobQuery<>();
			query.getObject(RestoreActivity.this, bmobUser.getObjectId(), new GetListener<User>() {
				
				@Override
				public void onFailure(int arg0, String arg1) {
					// TODO Auto-generated method stub
					cancelProgress();
					Toast.makeText(RestoreActivity.this, R.string.msg_get_data_fail, Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onSuccess(User user) {
					// TODO Auto-generated method stub
					cancelProgress();
					String apps = user.getApps();
					if (!TextUtils.isEmpty(apps)) {
						parseApps(apps);
					} else {
						Toast.makeText(RestoreActivity.this, R.string.msg_no_backup, Toast.LENGTH_SHORT).show();
					}
				}
			});
		} else {
			Toast.makeText(RestoreActivity.this, R.string.msg_please_relogin, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	/**
	 * 解析备份数据
	 * @param content
	 */
	private void parseApps(String content) {
		if (content != null) {
			List<RestoreApp> apps = JsonHelper.convertList(content, RestoreApp.class);
			if (apps != null && apps.size() > 0) {
				if (appList != null && appList.size() > 0) {
					appList.clear();
				}
				appList.addAll(apps);
				mAdapter.notifyDataSetChanged();
				MobclickAgent.onEvent(RestoreActivity.this,"restore");
			}
		}
	}
}
