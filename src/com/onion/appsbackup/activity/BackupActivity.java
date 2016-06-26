package com.onion.appsbackup.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.UpdateListener;

import com.onion.appsbackup.R;
import com.onion.appsbackup.adapter.AppAdapter;
import com.onion.appsbackup.adapter.AppAdapter.OnAppItemClickListener;
import com.onion.appsbackup.model.App;
import com.onion.appsbackup.model.User;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.onion.appsbackup.view.CustomedActionBar.OnRightTextClickListener;
import com.onion.appsbackup.view.ProgressDialogUtils;
import com.umeng.analytics.MobclickAgent;

public class BackupActivity extends Activity implements OnClickListener {

	private List<App> appList;
	private ListView lv_app;
	private AppAdapter mAppAdapter;
	private CustomedActionBar ab_backup;
	private boolean isChecked;
	private ProgressDialogUtils pDlgUtl;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		
		ab_backup = (CustomedActionBar) findViewById(R.id.ab_backup);
		ab_backup.setOnLeftIconClickListener(new OnLeftIconClickListener() {
			
			@Override
			public void onLeftIconClick() {
				onBackPressed();
			}
		});
		ab_backup.setOnRightTextClickListener(new OnRightTextClickListener() {
			
			@Override
			public void onRightTextClick() {
				isChecked = !isChecked;
				if (appList != null && appList.size() > 0) {
					for (App app : appList) {
						app.setChecked(isChecked);
					}
					mAppAdapter.notifyDataSetChanged();
				}
			}
		});
		
		lv_app = (ListView) findViewById(R.id.lv_app);
		lv_app.setDividerHeight(0);
		
		appList = new ArrayList<App>();
		mAppAdapter = new AppAdapter(this);
		mAppAdapter.setApps(appList);
		mAppAdapter.setOnAppItemClickListener(new OnAppItemClickListener() {
			
			@Override
			public void onAppItemClick(int position) {
				// TODO Auto-generated method stub
				appList.get(position).setChecked(!appList.get(position).isChecked());
				mAppAdapter.notifyDataSetChanged();
			}
		});
		lv_app.setAdapter(mAppAdapter);
		
		findViewById(R.id.btn_backup_checked).setOnClickListener(this);
		
		new GetAppsTask().execute();
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
		case R.id.btn_backup_checked:
			if (HttpTools.isNetworkConnected(this)) {
				// 备份所选应用
				if (appList != null && appList.size() > 0) {
					List<App> checkedApps = new ArrayList<App>();
					for (App app : appList) {
						if (app.isChecked()) {
							App backupApp = new App();
							backupApp.setAppName(app.getAppName());
							backupApp.setAppPackageName(app.getAppPackageName());
							backupApp.setChecked(true);
							checkedApps.add(backupApp);
						}
					}
					
					if (checkedApps != null && checkedApps.size() > 0) {
						showProgressDialog();
						String appInfos = com.alibaba.fastjson.JSON.toJSONString(checkedApps);
						saveApps(appInfos);
					} else {
						// 没有网络
						Toast.makeText(this, R.string.msg_please_choose_at_least_one_app, Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				Toast.makeText(this, R.string.msg_check_network, Toast.LENGTH_SHORT).show();
			}
			
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * 显示加载中...
	 */
	private void showProgressDialog() {
		if (pDlgUtl == null) {
			pDlgUtl = new ProgressDialogUtils(BackupActivity.this);
		}
		pDlgUtl.show();
	}
	private void cancelProgress() {
		if (pDlgUtl != null) {
			pDlgUtl.hide();
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		cancelProgress();
	}
	
	/**
	 * 备份应用
	 * @param view
	 */
	public void saveApps(String json) {
		BmobUser bmobUser = BmobUser.getCurrentUser(BackupActivity.this);
		if (bmobUser != null) {
			User user = new User();
			user.setApps(json);
			user.update(BackupActivity.this, bmobUser.getObjectId(), new UpdateListener() {
				
				@Override
				public void onSuccess() {
					// 提交成功
					if (appList != null && appList.size() > 0) {
						for (App app : appList) {
							app.setChecked(false);
						}
						mAppAdapter.notifyDataSetChanged();
					}
					cancelProgress();
					Toast.makeText(BackupActivity.this, R.string.msg_back_suc, Toast.LENGTH_LONG).show();
				}
				
				@Override
				public void onFailure(int arg0, String arg1) {
					// 提交失败
					cancelProgress();
					Toast.makeText(BackupActivity.this, R.string.msg_backup_fail, Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			Toast.makeText(BackupActivity.this, R.string.msg_please_relogin, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/**
	 * 获取已安装应用的异步Task
	 * @author Jason
	 *
	 */
	private class GetAppsTask extends AsyncTask<Void, Void, List<App>> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgressDialog();
		}
		
		@Override
		protected List<App> doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return getApps();
		}
		
		@Override
		protected void onPostExecute(List<App> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			appList.addAll(result);
			mAppAdapter.notifyDataSetChanged();
			cancelProgress();
		}
		
	}
	
	/**
	 * @return 用户安装的应用
	 */
	private List<App> getApps() {
		List<App> apps = new ArrayList<App>();
		PackageManager pm = getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0);  

		for (PackageInfo pi : packs) {  
           //显示用户安装的应用程序，而不显示系统程序  
           if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {  
        	   App app = new App();
        	   // 图标  
        	   app.setAppIcon(((BitmapDrawable) pi.applicationInfo.loadIcon(pm)).getBitmap());
               // 应用程序名称  
        	   app.setAppName(pi.applicationInfo.loadLabel(pm).toString());
               // 应用程序包名  
        	   app.setAppPackageName(pi.applicationInfo.packageName);
               apps.add(app); 
           }
        }
		return apps;
	}
}
