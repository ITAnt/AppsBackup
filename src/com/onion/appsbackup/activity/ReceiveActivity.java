package com.onion.appsbackup.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.Toast;

import com.onion.appsbackup.R;
import com.onion.appsbackup.adapter.AppAdapter;
import com.onion.appsbackup.adapter.AppAdapter.OnAppItemClickListener;
import com.onion.appsbackup.model.App;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.util.SystemTools;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.onion.appsbackup.view.ProgressDialogUtils;
import com.umeng.analytics.MobclickAgent;

public class ReceiveActivity extends Activity {

	// 10秒扫描一次
	private static final int WAIT_FOR_SCAN_RESULT = 5 * 1000;
	// 20秒之内没有得到扫描结果就不要再扫描了
	private static final int WIFI_SCAN_TIMEOUT = 200 * 1000;
	
	private static final int CONNECT_SERVER_SUC = 1;
	private Socket mClientSocket;
	
	private WifiManager mWifiManager;
	private Handler mConnectHotSpotHandler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case CONNECT_SERVER_SUC:
				Toast.makeText(ReceiveActivity.this, R.string.toast_hotspot_connected, Toast.LENGTH_SHORT).show();
				// WiFi连接成功，开socket客户端去连接服务器以接收文件
				final String serverIP = HttpTools.getSenderIP(mWifiManager);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						connectTCPServer(serverIP);
					}
				}).start();
				break;

			default:
				Toast.makeText(ReceiveActivity.this, R.string.toast_hotspot_failed, Toast.LENGTH_SHORT).show();
				break;
			}
			return true;
		}
	});
	
	private List<App> appList;
	private ListView lv_app;
	private AppAdapter mAppAdapter;
	private CustomedActionBar ab_backup;
	private ProgressDialogUtils pDlgUtl;
	private PackageManager mPackageManager;
	// 这个路径保存从服务器接收到的应用
	private String mDestDir;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive);
		
		mDestDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "apks" + File.separator;
		mPackageManager = getPackageManager();
		
		ab_backup = (CustomedActionBar) findViewById(R.id.ab_backup);
		ab_backup.setOnLeftIconClickListener(new OnLeftIconClickListener() {
			
			@Override
			public void onLeftIconClick() {
				onBackPressed();
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
				// 安装应用
				
				Intent installIntent = new Intent();
				installIntent.setAction(Intent.ACTION_INSTALL_PACKAGE);
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				installIntent.setData(Uri.fromFile(new File(mDestDir + appList.get(position).getAppName() + ".apk")));
				installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
				startActivity(installIntent);
			}
		});
		lv_app.setAdapter(mAppAdapter);
		
		
		mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		// 搜索并连接我们APP创建的热点
		new ConnectTask().execute();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		new GetAppsTask().execute();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	/**
	 * 显示加载中...
	 */
	private void showProgressDialog() {
		if (pDlgUtl == null) {
			pDlgUtl = new ProgressDialogUtils(ReceiveActivity.this);
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
	 * 获取我们的文件夹里已经接收的应用
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
			List<App> apps = new ArrayList<App>();
			
			File destFileDir = new File(mDestDir);
			if (!destFileDir.exists()) {
				destFileDir.mkdirs();
			}

			File[] appFiles = destFileDir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					return filename.endsWith(".apk");
				}
			});
			
			if (appFiles != null && appFiles.length > 0) {
				for (File file : appFiles) {
					// 从apk文件获取图标等信息
					String apkPath = file.getAbsolutePath();
					PackageInfo packageInfo = mPackageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
					if (packageInfo != null) {
						ApplicationInfo applicationInfo = packageInfo.applicationInfo;
						// 重要！！！！！把APK的路径告诉系统，这样才能解析出安装包信息
						applicationInfo.sourceDir = apkPath;
						applicationInfo.publicSourceDir = apkPath;
						
						App app = new App();
		        	    // 图标  
		        	    app.setAppIcon(((BitmapDrawable) applicationInfo.loadIcon(mPackageManager)).getBitmap());
		                // 应用程序名称  
		        	    app.setAppName(applicationInfo.loadLabel(mPackageManager).toString());
		                // 应用程序包名  
		        	    app.setAppPackageName(applicationInfo.packageName);
		        	    
		        	    if (SystemTools.isApkInstalled(mPackageManager, applicationInfo.packageName)) {
		        	    	// 已安装就打上勾
		        	    	app.setChecked(true);
		        	    }
		        	    
		        	    // 文件的路径
		        	    app.setPath(applicationInfo.publicSourceDir);
		                apps.add(app);
					}
				}
			}
			return apps;
		}
		
		@Override
		protected void onPostExecute(List<App> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if (appList != null) {
				appList.clear();
			}
			
			if (result != null) {
				appList.addAll(result);
				mAppAdapter.notifyDataSetChanged();
				cancelProgress();
			}
		}
	}
	
	/**
	 * 后台搜索并连接热点
	 * @author 詹子聪
	 */
	private class ConnectTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			connectToHotSpot("away6899", HttpTools.TYPE_WPA);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
	}

	/**
	 * 连接特定热点
	 * @param ssid 热点名称
	 * @param passwd 密码
	 */
	private void connectToHotSpot(String passwd, int type) {
		// 关闭热点
		HttpTools.setWifiApEnabled(mWifiManager, null, false);
		
		// 打开WiFi
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}

		List<ScanResult> scanResults = mWifiManager.getScanResults();
		if (scanResults == null) {
			// 如果没有可用的扫描结果，激活扫描
			mWifiManager.startScan();
			boolean isScanResultIsAvailable = false;
			long startTime = System.currentTimeMillis();
			while (!isScanResultIsAvailable) {
				if ((System.currentTimeMillis() - startTime) > WIFI_SCAN_TIMEOUT) {
					// 20秒之内还没搜到可用热点就不要再搜了
					return;
				}

				// 等待扫描结果，等5秒之后查看结果
				synchronized (this) {
					try {
						this.wait(WAIT_FOR_SCAN_RESULT);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if ((mWifiManager.getScanResults() == null) || (mWifiManager.getScanResults().size() <= 0)) {
						continue;
					}
					isScanResultIsAvailable = true;
				}
			}
		}

		// 获取扫描结果(代码能走到这里一定有结果了)
		scanResults = mWifiManager.getScanResults();
		for (ScanResult scanResult : scanResults) {
			// 如果当前这个热点是我们想要连接的热点，那么就连接它
			if (scanResult.SSID.startsWith("backup")) {
				final String ssid = "\"" + scanResult.SSID + "\"";
				WifiConfiguration wifiConfiguration = HttpTools.createWifiInfo(ssid, passwd, type);
				int networkId = mWifiManager.addNetwork(wifiConfiguration);
				// 连接特定热点，并断开其他连接
				mWifiManager.enableNetwork(networkId, true);
				mWifiManager.saveConfiguration();
				mWifiManager.reconnect();
				// 开一个线程去轮询连接情况
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						while (true) {
							// 割两秒去查一下是否已经连上了WiFi
							try {
								Thread.sleep(2000);
							} catch (Exception e) {
								// TODO: handle exception
							}
							
							if (HttpTools.isWiFiConnected(ReceiveActivity.this)) {
								// 已经连接上的热点真的是我们想要连接的热点
								if (TextUtils.equals(ssid, HttpTools.getConnectedWifiSsid(mWifiManager))) {
									mConnectHotSpotHandler.sendEmptyMessage(CONNECT_SERVER_SUC);
									break;
								}
							} else {
								// 当前连接的WiFi不是我们想要的，重新连接
								connectToHotSpot("away6899", HttpTools.TYPE_WPA);
							}
						}
					}
				}).start();
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mClientSocket != null) {
			try {
				mClientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 连接服务器，从服务器接收应用
	 * @param serverIP
	 */
	private void connectTCPServer(String serverIP) {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket(serverIP, 8688);
                mClientSocket = socket;
            } catch (IOException e) {
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed, retry...");
            }
        }

        try {
            // 接收服务器端的消息和文件
        	String destPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "apks" + File.separator;
			File destFileDir = new File(destPath);
			if (!destFileDir.exists()) {
				destFileDir.mkdirs();
			}
			
			try {
				
				DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));  
				// 不断地监听，看服务器是否有东西过来
				while (!ReceiveActivity.this.isFinishing()) {
		            String fileName = inputStream.readUTF();
		            if (!TextUtils.isEmpty(fileName)) {
		            	long fileSize = inputStream.readLong();
			            File destFile = new File(destPath + fileName);
			            if (destFile.exists()) {
			            	destFile.delete();
			            }
			            
			            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)));
			            
			            byte[] buffer = new byte[(int)fileSize]; 
		                int nIdx = 0; 
		                int nTotalLen = buffer.length; 
		                int nReadLen = 0; 
		                
		                while (nIdx < nTotalLen) { 
		                	// 从nIdx开始，想读nTotalLen - nIdx那么多，实际上这次读了nReadLen
		                    nReadLen = inputStream.read(buffer, nIdx, nTotalLen - nIdx); 
		                    
		                    if (nReadLen > 0) { 
		                    	outputStream.write(buffer, nIdx, nReadLen);
		                        nIdx = nIdx + nReadLen; 
		                    } else { 
		                        break; 
		                    } 
		                }
				        outputStream.close();
				        
				        // 接收完了一个应用，刷新列表
				        new GetAppsTask().execute();
		            }
				}
			} catch (Exception e) {  
	            e.printStackTrace();  
	        }
        	
            System.out.println("quit...");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
