package com.onion.appsbackup.util;

import android.content.pm.PackageManager;

public class SystemTools {

	/**
	 * 判断APK是否已经安装
	 * @param localPackageManager
	 * @param packagename
	 * @return
	 */
	public static boolean isApkInstalled(PackageManager localPackageManager, String packagename) {
		try {
			localPackageManager.getPackageInfo(packagename, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
			return false;
		}
	}
}
