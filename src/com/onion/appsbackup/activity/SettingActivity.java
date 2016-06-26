package com.onion.appsbackup.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;

import com.onion.appsbackup.Constant;
import com.onion.appsbackup.R;
import com.onion.appsbackup.util.BackupActivityManager;
import com.onion.appsbackup.util.tencent.TencentUtil;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class SettingActivity extends Activity implements OnClickListener {

	private Button btn_exit;
	private CustomedActionBar ab_setting;

//	private LinearLayout ll_contact;
//	private LinearLayout ll_sms;
	private LinearLayout ll_share;
	private LinearLayout ll_nickname;
	private LinearLayout ll_gender;
	private LinearLayout ll_change_phone;
	private LinearLayout ll_set_bg;
	private LinearLayout ll_about;
	
	private static Tencent mTencent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		if (mTencent == null) {
	        mTencent = Tencent.createInstance(Constant.TENCENT_APP_ID, this);
	    }

		initComponent();

		ab_setting = (CustomedActionBar) findViewById(R.id.ab_setting);
		ab_setting.setOnLeftIconClickListener(new OnLeftIconClickListener() {

			@Override
			public void onLeftIconClick() {
				onBackPressed();
			}
		});
	}

	private void initComponent() {
		// TODO Auto-generated method stub
		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_exit.setOnClickListener(this);

		/*ll_contact = (LinearLayout) findViewById(R.id.ll_contact);
		ll_contact.setOnClickListener(this);

		ll_sms = (LinearLayout) findViewById(R.id.ll_sms);
		ll_sms.setOnClickListener(this);*/

		ll_share = (LinearLayout) findViewById(R.id.ll_share);
		ll_share.setOnClickListener(this);
		
		ll_nickname = (LinearLayout) findViewById(R.id.ll_nickname);
		ll_nickname.setOnClickListener(this);

		ll_gender = (LinearLayout) findViewById(R.id.ll_gender);
		ll_gender.setOnClickListener(this);

		ll_change_phone = (LinearLayout) findViewById(R.id.ll_change_phone);
		ll_change_phone.setOnClickListener(this);

		ll_set_bg = (LinearLayout) findViewById(R.id.ll_set_bg);
		ll_set_bg.setOnClickListener(this);

		ll_about = (LinearLayout) findViewById(R.id.ll_about);
		ll_about.setOnClickListener(this);

	}
	
	/*private boolean isIconReady() {
		File iconFile = new File(getFilesDir().getPath() + "/ic_launcher.png");
		return iconFile.exists();
	}
	
	private void copyIcon() {
		File iconFile = new File(getFilesDir().getPath() + "/ic_launcher.png");
        try {
            if (!iconFile.exists()) {
                InputStream is = getAssets().open("ic_launcher.png");
                FileOutputStream fos = new FileOutputStream(iconFile);
                byte[] buf = new byte[1024];
                int count = 0;
                while ((count = is.read(buf)) > 0) {
                    fos.write(buf, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}*/
	
	private void onClickShare() { 
	    final Bundle params = new Bundle();
	    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
	    params.putString(QQShare.SHARE_TO_QQ_TITLE, getString(R.string.label_share_titile));
	    params.putString(QQShare.SHARE_TO_QQ_SUMMARY, getString(R.string.label_share_desc));
	    /*if (!isIconReady()) {
	    	copyIcon();
	    }*/
	    
	    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, "http://bdbea3.duapp.com/pcs_download.php?id=950&link=%2Fapps%2Fhgf_blog%2Fic_launcher.png");
	    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "http://a.app.qq.com/o/simple.jsp?pkgname=com.itant.topspeedbackup");
	    params.putString(QQShare.SHARE_TO_QQ_APP_NAME, getString(R.string.app_name));
	    mTencent.shareToQQ(SettingActivity.this, params, new BaseUiListener());
	}
	
	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(Object response) {
            if (null == response) {
                TencentUtil.showResultDialog(SettingActivity.this, getString(R.string.msg_return_null), getString(R.string.msg_login_fail));
                return;
            }
            
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
            	TencentUtil.showResultDialog(SettingActivity.this, getString(R.string.msg_return_null), getString(R.string.msg_login_fail));
                return;
            }
            TencentUtil.showResultDialog(SettingActivity.this, response.toString(), getString(R.string.msg_login_suc));
			doComplete((JSONObject)response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			TencentUtil.toastMessage(SettingActivity.this, "onError: " + e.errorDetail);
			TencentUtil.dismissDialog();
		}

		@Override
		public void onCancel() {
			TencentUtil.toastMessage(SettingActivity.this, "onCancel: ");
			TencentUtil.dismissDialog();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_exit:
			exitDialog();
			break;
		/*case R.id.ll_contact:
			manageContactDialog();
			break;
		case R.id.ll_sms:
			manageSmsDialog();
			break;*/
		case R.id.ll_share:
			// 分享给QQ好友
			onClickShare();
			break;
		case R.id.ll_nickname:
			Toast.makeText(this, R.string.label_coming, Toast.LENGTH_SHORT).show();
			break;
		case R.id.ll_gender:
			Toast.makeText(this, R.string.label_coming, Toast.LENGTH_SHORT).show();
			break;
		case R.id.ll_change_phone:
			Toast.makeText(this, R.string.label_coming, Toast.LENGTH_SHORT).show();
			break;
		case R.id.ll_set_bg:
			Toast.makeText(this, R.string.label_coming, Toast.LENGTH_SHORT).show();
			break;
		case R.id.ll_about:
			startActivity(new Intent(this, AboutActivity.class));
			break;

		default:
			break;
		}
	}

	/*private void manageContactDialog() {
			final AlertDialog operateDialog = new AlertDialog.Builder(SettingActivity.this).create();
			operateDialog.show();
			operateDialog.setContentView(R.layout.dialog_operate_contacts);
			
			Button btn_rename = (Button) operateDialog.findViewById(R.id.btn_rename);
			btn_rename.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 点击重命名，弹出重命名对话框
					operateDialog.cancel();
				}
			});
			
			Button btn_delete = (Button) operateDialog.findViewById(R.id.btn_delete);
			btn_delete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 点击删除，弹出是否删除该报文的对话框
					operateDialog.cancel();
				}
			});
			
			Button btn_upload = (Button) operateDialog.findViewById(R.id.btn_upload);
			btn_upload.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					operateDialog.cancel();
				}
			});
	}
	
	private void manageSmsDialog() {
		final AlertDialog operateDialog = new AlertDialog.Builder(SettingActivity.this).create();
		operateDialog.show();
		operateDialog.setContentView(R.layout.dialog_operate_sms);
		
		Button btn_rename = (Button) operateDialog.findViewById(R.id.btn_rename);
		btn_rename.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 点击重命名，弹出重命名对话框
				operateDialog.cancel();
			}
		});
		
		Button btn_delete = (Button) operateDialog.findViewById(R.id.btn_delete);
		btn_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 点击删除，弹出是否删除该报文的对话框
				operateDialog.cancel();
			}
		});
		
		Button btn_upload = (Button) operateDialog.findViewById(R.id.btn_upload);
		btn_upload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				operateDialog.cancel();
			}
		});
	}*/
	
	/**
	 * 退出对话框
	 */
	private void exitDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this)
				.create();
		dialog.show();
		dialog.setContentView(R.layout.dialog_exit);

		TextView tv_dialog_title = (TextView) dialog
				.findViewById(R.id.tv_dialog_title);
		TextView tv_dialog_desc = (TextView) dialog
				.findViewById(R.id.tv_dialog_desc);
		tv_dialog_title.setText(R.string.label_exit_title);
		tv_dialog_desc.setText(R.string.label_exit_desc);
		TextView tv_confirm = (TextView) dialog.findViewById(R.id.tv_confirm);
		TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancel);

		tv_confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
				// 退出
				BmobUser.logOut(SettingActivity.this);
				Tencent mTencent = Tencent.createInstance(Constant.TENCENT_APP_ID,
						SettingActivity.this);
				mTencent.logout(SettingActivity.this);
				BackupActivityManager.getInstance().clearActivities();
				startActivity(new Intent(SettingActivity.this,
						LoginActivity.class));
				finish();
			}
		});

		tv_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
	}
}
