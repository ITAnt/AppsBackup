package com.onion.appsbackup.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;

import com.onion.appsbackup.Constant;

public class FileUtils {
	
	/**
	 * 将要备份的应用信息存放在文件里
	 * @param info 要备份的应用信息
	 */
	public static void saveAllInfos(Activity mActivity, String info) {
		if (info != null) {
			try {
				FileOutputStream outStream = mActivity.openFileOutput(Constant.NAME_BACKED_APPS, Context.MODE_PRIVATE);
				outStream.write(info.getBytes());
				outStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取要备份的应用信息
	 * @param mActivity
	 * @return 要备份的应用信息
	 */
	public static String getAllInfos(Activity mActivity) {
		String allInfos = null;
		try {
			FileInputStream inputStream = mActivity.openFileInput(Constant.NAME_BACKED_APPS);
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);
			allInfos = new String(b);
		} catch (Exception e) {
			
		}
		return allInfos;
	}
}
