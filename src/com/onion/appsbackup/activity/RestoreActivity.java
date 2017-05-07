package com.onion.appsbackup.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.onion.appsbackup.R;
import com.onion.appsbackup.adapter.RestoreAppAdapter;
import com.onion.appsbackup.adapter.RestoreAppAdapter.OnAppItemClickListener;
import com.onion.appsbackup.model.App;
import com.onion.appsbackup.model.RestoreApp;
import com.onion.appsbackup.model.User;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.util.JsonHelper;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.onion.appsbackup.view.CustomedActionBar.OnRightIconClickListener;
import com.onion.appsbackup.view.ProgressDialogUtils;
import com.onion.appsbackup.view.swipemenu.SwipeMenu;
import com.onion.appsbackup.view.swipemenu.SwipeMenuCreator;
import com.onion.appsbackup.view.swipemenu.SwipeMenuItem;
import com.onion.appsbackup.view.swipemenu.SwipeMenuListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;

public class RestoreActivity extends Activity {

	private SwipeMenuListView lv_old_apps;
	private RestoreAppAdapter mAdapter;
	private List<RestoreApp> appList;
	
	private CustomedActionBar ab_restore;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore);

		findViewById(R.id.btn_delete_all).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog dialog = new AlertDialog.Builder(RestoreActivity.this)
						.setMessage(R.string.lablel_delete_all)
						.setNegativeButton(getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

							}
						})
						.setPositiveButton(getResources().getString(R.string.label_confirm), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								deleteAllApps();
							}
						}).create();
				dialog.show();
			}
		});

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
		lv_old_apps = (SwipeMenuListView) findViewById(R.id.lv_old_apps);
		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(dp2px(90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		// set creator
		lv_old_apps.setMenuCreator(creator);

		// step 2. listener item click event
		lv_old_apps.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
				RestoreApp item = appList.get(position);
				switch (index) {
					case 0:
						// delete
						AlertDialog dialog = new AlertDialog.Builder(RestoreActivity.this)
								.setMessage(String.format(getString(R.string.label_delete_message), item.getAppName()))
								.setNegativeButton(getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {

									}
								})
								.setPositiveButton(getResources().getString(R.string.label_confirm), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										List<RestoreApp> backupApps = new ArrayList<RestoreApp>();
										backupApps.addAll(appList);
										backupApps.remove(position);
										if (backupApps != null && backupApps.size() > 0) {
											showProgressDialog();
											String appInfos = com.alibaba.fastjson.JSON.toJSONString(backupApps);
											saveApps(appInfos, position);
										} else {
											// 请选择至少一个应用
											Toast.makeText(RestoreActivity.this, R.string.msg_please_choose_at_least_one_app, Toast.LENGTH_SHORT).show();
										}
									}
								}).create();
						dialog.show();
						break;
				}
				return false;
			}
		});

		lv_old_apps.setAdapter(mAdapter);
		mAdapter.setOnAppItemClickListener(new OnAppItemClickListener() {
			
			@Override
			public void onAppItemClick(int position) {
				appList.get(position).setChecked(true);
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
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
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
				for (RestoreApp restoreApp : appList) {
					restoreApp.setChecked(false);
				}
				mAdapter.notifyDataSetChanged();

				new GetAppsTask().execute();


				MobclickAgent.onEvent(RestoreActivity.this, "restore");
			}
		}
	}

	/**
	 * 备份应用
	 */
	public void saveApps(String json, final int position) {
		BmobUser bmobUser = BmobUser.getCurrentUser(RestoreActivity.this);
		if (bmobUser != null) {
			User user = new User();
			user.setApps(json);
			user.update(RestoreActivity.this, bmobUser.getObjectId(), new UpdateListener() {

				@Override
				public void onSuccess() {
					// 提交成功
					cancelProgress();
					Toast.makeText(RestoreActivity.this, R.string.msg_back_suc, Toast.LENGTH_LONG).show();

					appList.remove(position);
					mAdapter.notifyDataSetChanged();
				}

				@Override
				public void onFailure(int arg0, String arg1) {
					// 提交失败
					cancelProgress();
					Toast.makeText(RestoreActivity.this, R.string.msg_backup_fail, Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			Toast.makeText(RestoreActivity.this, R.string.msg_please_relogin, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 删除备份
	 */
	public void deleteAllApps() {
		BmobUser bmobUser = BmobUser.getCurrentUser(RestoreActivity.this);
		if (bmobUser != null) {
			User user = new User();
			user.setApps("");
			user.update(RestoreActivity.this, bmobUser.getObjectId(), new UpdateListener() {

				@Override
				public void onSuccess() {
					// 提交成功
					cancelProgress();
					Toast.makeText(RestoreActivity.this, R.string.msg_back_suc, Toast.LENGTH_LONG).show();

					appList.clear();
					mAdapter.notifyDataSetChanged();
				}

				@Override
				public void onFailure(int arg0, String arg1) {
					// 提交失败
					cancelProgress();
					Toast.makeText(RestoreActivity.this, R.string.msg_backup_fail, Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			Toast.makeText(RestoreActivity.this, R.string.msg_please_relogin, Toast.LENGTH_SHORT).show();
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
			for (App app : result) {
				for (RestoreApp restoreApp : appList) {
					int index = restoreApp.getAppPackageName().indexOf("    版本：");
					if (index == -1) {
						// 没版本号
						if (TextUtils.equals(app.getAppPackageName(), restoreApp.getAppPackageName())) {
							restoreApp.setChecked(true);
						}
					} else {
						// 有版本号
						String serverPckName = restoreApp.getAppPackageName().substring(0, index);
						if (TextUtils.equals(app.getAppPackageName(), serverPckName)) {
							restoreApp.setChecked(true);
						}
					}
				}
			}
			mAdapter.notifyDataSetChanged();
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
			/*if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {

			}*/
			App app = new App();
			// 图标
			try {
				app.setAppIcon(((BitmapDrawable) pi.applicationInfo.loadIcon(pm)).getBitmap());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 应用程序名称
			app.setAppName(pi.applicationInfo.loadLabel(pm).toString());
			// 应用程序包名
			app.setAppPackageName(pi.applicationInfo.packageName);
			apps.add(app);
		}
		return apps;
	}
}
