package com.onion.appsbackup.activity;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.onion.appsbackup.R;
import com.onion.appsbackup.adapter.AppAdapter;
import com.onion.appsbackup.adapter.AppAdapter.OnAppItemClickListener;
import com.onion.appsbackup.model.App;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.onion.appsbackup.view.CustomedActionBar.OnRightTextClickListener;
import com.onion.appsbackup.view.ProgressDialogUtils;
import com.umeng.analytics.MobclickAgent;

public class SendActivity extends Activity implements OnClickListener {

	private List<App> appList;
	private ListView lv_app;
	private AppAdapter mAppAdapter;
	private CustomedActionBar ab_backup;
	private boolean isChecked;
	private ProgressDialogUtils pDlgUtl;
	private Button btn_backup_checked;
	
	private WifiManager mWifiManager;
	private static final int CLIENT_DISCONNECTED = 0;
	private static final int CLIENT_CONNECTED = 1;
	private static final int HOT_SPOT_READY = 2;
	private static final int SENDING_APK = 3;
	private static final int SENDING_FINISHED = 4;
	
	private ServerSocket mServerSocket;
	private Socket mClient;
	private boolean mIsServiceDestoryed = false;
	/**往客户端发送文件的入口**/
	private DataOutputStream mOutputStream;
	private Handler mServerHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case CLIENT_DISCONNECTED:
				btn_backup_checked.setEnabled(false);
				break;
			case CLIENT_CONNECTED:
				// 客户端连上了(监听断开)
				btn_backup_checked.setEnabled(true);
				break;
			case HOT_SPOT_READY:
				// 热点创建成功，开启socket服务器端，监听客户端连接(监听断开)
				new ServerThread().start();
				break;

			case SENDING_APK:
				// 开始发送应用
				showProgressDialog();
				break;
			case SENDING_FINISHED:
				// 发送应用完成
				cancelProgress();
				break;
			default:
				break;
			}
			return true;
		}
	});
	
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
		
		btn_backup_checked = (Button) findViewById(R.id.btn_backup_checked);
		btn_backup_checked.setOnClickListener(this);
		btn_backup_checked.setEnabled(false);
		
		mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		new GetApksTask().execute();
		// 创建热点
		String hotSpotName = "backup" + System.currentTimeMillis();
		createAP(hotSpotName, "away6899");
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
			// 发送所选应用
			if (appList != null && appList.size() > 0) {
				mServerHandler.sendEmptyMessage(SENDING_APK);
				int checkNum = 0;
				for (App app : appList) {
					if (app.isChecked()) {
						final File file = new File(app.getPath());
						try {
							// 把文件名告诉客户端
							mOutputStream.writeUTF(app.getAppName() + ".apk");  
							mOutputStream.flush();
							
							// 把文件长度诉客户端，否则不知道一个文件结尾在哪
							mOutputStream.writeLong(file.length());  
							mOutputStream.flush();
							
							DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
							byte buf[] = new byte[1024];
					        int len = -1;
				        
				            while ((len = inputStream.read(buf)) != -1) {
				            	mOutputStream.write(buf, 0, len);
				            }
				            mOutputStream.flush();
				            inputStream.close();
				            // 一个文件结束了
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						checkNum++;
					}
				}
				mServerHandler.sendEmptyMessage(SENDING_FINISHED);
				Toast.makeText(SendActivity.this, R.string.toast_transfer_complete, Toast.LENGTH_SHORT).show();
				if (checkNum == 0) {
					Toast.makeText(SendActivity.this, R.string.toast_please_select, Toast.LENGTH_SHORT).show();
				} else {
					for (App app : appList) {
						app.setChecked(false);
					}
					mAppAdapter.notifyDataSetChanged();
				}
			} else {
				Toast.makeText(SendActivity.this, R.string.toast_no_apps, Toast.LENGTH_SHORT).show();
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
			pDlgUtl = new ProgressDialogUtils(SendActivity.this);
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
		setResult(666);
		super.onBackPressed();
		cancelProgress();
		
	}
	
	/**
	 * 获取已安装的应用的安装包
	 * @author Jason
	 *
	 */
	private class GetApksTask extends AsyncTask<Void, Void, List<App>> {

		@Override
		protected List<App> doInBackground(Void... params) {
			List<App> apps = new ArrayList<App>();
			
			PackageManager pm = getPackageManager();
			List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(PackageManager.GET_META_DATA);
			for (ApplicationInfo applicationInfo : applicationInfos) {
				
				if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
					// 非系统应用
					App app = new App();
	        	    // 图标  
	        	    app.setAppIcon(((BitmapDrawable) applicationInfo.loadIcon(pm)).getBitmap());
	                // 应用程序名称  
	        	    app.setAppName(applicationInfo.loadLabel(pm).toString());
	                // 应用程序包名  
	        	    app.setAppPackageName(applicationInfo.packageName);
	        	    // 应用路径
	        	    app.setPath(applicationInfo.publicSourceDir);
	                apps.add(app); 
				}
			}
			return apps;
		}
		
		@Override
		protected void onPostExecute(List<App> apps) {
			super.onPostExecute(apps);
			
			appList.addAll(apps);
			mAppAdapter.notifyDataSetChanged();
			cancelProgress();
		}
	}
	
	/**
     * 创建热点
     * @param ssid 热点名称
     * @param password 密码
     */
    private void createAP(String ssid, String password){
		if (mWifiManager.isWifiEnabled()) {
			// 如果开启了WIFI，则将它关闭
			mWifiManager.setWifiEnabled(false);
		} 
		
		
		try{
			WifiConfiguration wifiConfiguration = new WifiConfiguration();
			//热点名称
			wifiConfiguration.SSID = ssid;
			//热点密码
			wifiConfiguration.preSharedKey = password;
			wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

			// 利用反射获取开启热点的方法创建热点
			HttpTools.setWifiApEnabled(mWifiManager, wifiConfiguration, true);
			
			final Method isHotSpotEnable = mWifiManager.getClass().getMethod("isWifiApEnabled");
			
			// 每隔1.5秒查看热点是否创建成功
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (true) {
						try {
							boolean hotSpotEnable = (boolean) isHotSpotEnable.invoke(mWifiManager);
							if (hotSpotEnable) {
								mServerHandler.sendEmptyMessage(HOT_SPOT_READY);
								break;
							}
							Thread.sleep(1500);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}
			}).start();
		}catch(Exception e){
			e.printStackTrace();
		}
    }
    
    /**
     * Socket服务器端
     * @author 詹子聪
     *
     */
    private class ServerThread extends Thread {
    	
		@Override
    	public void run() {
            try {
                mServerSocket = new ServerSocket(8688);
            } catch (IOException e) {
                System.err.println("establish tcp server failed, port:8688");
                e.printStackTrace();
                return;
            }

            while (!mIsServiceDestoryed) {
                try {
                    // 一个客户端连接上了
                    mClient = mServerSocket.accept();
                    mOutputStream = new DataOutputStream(mClient.getOutputStream());
                    mServerHandler.sendEmptyMessage(CLIENT_CONNECTED);
                } catch (IOException e) {
                    e.printStackTrace();
                    // 如果客户端断开连接，这里会不会执行？？？？？？？？？？？？？？？？？？？？应该不会
                    mServerHandler.sendEmptyMessage(CLIENT_DISCONNECTED);
                }
            }
    	}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mIsServiceDestoryed = false;
    	
    	// 关闭流
    	if (mServerSocket != null) {
    		try {
    			if (mOutputStream != null) {
    				mOutputStream.close();
    			}
    			if (mClient != null) {
    				mClient.close();
    			}
    			mServerSocket.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
}
