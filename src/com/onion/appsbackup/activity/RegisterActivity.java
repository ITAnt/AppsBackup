package com.onion.appsbackup.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;

import com.onion.appsbackup.R;
import com.onion.appsbackup.model.User;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.view.CustomedActionBar;
import com.onion.appsbackup.view.CustomedActionBar.OnLeftIconClickListener;
import com.onion.appsbackup.view.ProgressDialogUtils;
import com.umeng.analytics.MobclickAgent;

public class RegisterActivity extends Activity implements OnClickListener,
		TextWatcher {

	private CustomedActionBar ab_register;

	private EditText et_phone;
	private EditText et_name;
	private EditText et_password;

	private String phone;
	private String userName;
	private String password;

	private Button btn_register;

	private ProgressDialogUtils pDlgUtl;

	private boolean continueSend = true;
	private String chaptcha;
	private Button btn_get_chaptcha;
	private EditText et_chaptcha;
	private Handler mTimeHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int obj = (int) msg.obj;
			if (obj == 0) {
				btn_get_chaptcha.setText("获取验证码");
				btn_get_chaptcha.setEnabled(true);
			} else {
				btn_get_chaptcha.setText(obj + "S");
			}
			return true;
		}
	});

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
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		initComponent();
	}

	private void initComponent() {
		// TODO Auto-generated method stub
		ab_register = (CustomedActionBar) findViewById(R.id.ab_register);
		ab_register.setOnLeftIconClickListener(new OnLeftIconClickListener() {
			@Override
			public void onLeftIconClick() {
				onBackPressed();
			}
		});

		et_phone = (EditText) findViewById(R.id.et_phone);
		et_phone.addTextChangedListener(this);
		et_name = (EditText) findViewById(R.id.et_name);
		et_name.addTextChangedListener(this);
		et_password = (EditText) findViewById(R.id.et_password);
		et_password.addTextChangedListener(this);

		btn_register = (Button) findViewById(R.id.btn_register);
		btn_register.setOnClickListener(this);

		et_chaptcha = (EditText) findViewById(R.id.et_chaptcha);
		et_chaptcha.addTextChangedListener(this);
		btn_get_chaptcha = (Button) findViewById(R.id.btn_get_chaptcha);
		btn_get_chaptcha.setOnClickListener(this);
	}

	/**
	 * 显示加载中...
	 */
	private void showProgressDialog() {
		if (pDlgUtl == null) {
			pDlgUtl = new ProgressDialogUtils(RegisterActivity.this);
		}
		pDlgUtl.show();
	}

	private void cancelProgress() {
		if (pDlgUtl != null) {
			pDlgUtl.hide();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.btn_register:
			if (HttpTools.isNetworkConnected(this)) {
				showProgressDialog();
				/*User user = new User();
				user.setUsername(userName);
				user.setNickName(userName);
				user.setPassword(password);
				// user.setThePassword(password);
				user.setMobilePhoneNumber(phone);
				user.setPhoneNumber(((TelephonyManager) RegisterActivity.this
						.getSystemService(Context.TELEPHONY_SERVICE))
						.getLine1Number());
				user.signUp(this, new SaveListener() {

					@Override
					public void onSuccess() {
						// 注册成功
						cancelProgress();
						BackupActivityManager.getInstance().clearActivities();
						startActivity(new Intent(RegisterActivity.this,
								MainActivity.class));
						RegisterActivity.this.finish();
					}

					@Override
					public void onFailure(int arg0, String err) {
						// 注册失败
						cancelProgress();
						Toast.makeText(RegisterActivity.this,
								getString(R.string.msg_register_fail) + err,
								Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				Toast.makeText(RegisterActivity.this,
						R.string.msg_check_network, Toast.LENGTH_SHORT).show();
			}*/
				
				// 校验验证码
				BmobSMS.verifySmsCode(this, phone, chaptcha, new VerifySMSCodeListener() {

				    @Override
				    public void done(BmobException ex) {
				        // TODO Auto-generated method stub
				        if(ex==null){//短信验证码已验证成功
				            //Log.i("smile", "验证通过");
				        	User user = new User();
				        	user.setUsername(userName);
				        	user.setNickName(userName);
							user.setPassword(password);
							user.setMobilePhoneNumber(phone);
							user.setAge(0);
							user.setSex("男");
							user.setPhoneName(Build.MODEL);
							user.setAndroidRelease(Build.VERSION.RELEASE);
							user.setPhoneNumber(((TelephonyManager) RegisterActivity.this
									.getSystemService(Context.TELEPHONY_SERVICE))
									.getLine1Number());
							user.signUp(RegisterActivity.this, new SaveListener() {
								
								@Override
								public void onSuccess() {
									// TODO Auto-generated method stub
									if (pDlgUtl != null) {
										pDlgUtl.hide();
									}
									Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
									User user = BmobUser.getCurrentUser(RegisterActivity.this, User.class);
									if (user != null) {
										//==================//QuestionnaireActivityManager.getInstance().clearActivities();
										//================//startActivity(new Intent(RegisterActivity.this, StudentActivity.class));
										finish();
									} else {
										Toast.makeText(RegisterActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
										onBackPressed();
									}
								}
								
								@Override
								public void onFailure(int code, String exception) {
									// TODO Auto-generated method stub
									if (pDlgUtl != null) {
										pDlgUtl.hide();
									}
									String toast = "注册失败";
									
									switch (code) {
									case 209:
										toast = "手机号已被注册";
										break;
									case 202:
										toast = "用户名已存在";
										break;

									default:
										toast = "注册失败";
										break;
									} 
									Toast.makeText(RegisterActivity.this, toast, Toast.LENGTH_SHORT).show();
								}
							});
				        }else{
				            //Log.i("smile", "验证失败：code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
				        	if (pDlgUtl != null) {
								pDlgUtl.hide();
							}
				        	Toast.makeText(RegisterActivity.this, "手机验证失败", Toast.LENGTH_SHORT).show();
				        }
				    }
				});
			} else {
				Toast.makeText(RegisterActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
			}

			break;
			
		case R.id.btn_get_chaptcha:
			if (!TextUtils.isEmpty(phone)) {
				continueSend = true;
				btn_get_chaptcha.setEnabled(false);
				// 发送验证码
				BmobSMS.requestSMSCode(this, phone, "超级问卷短信模板", new RequestSMSCodeListener() {

					@Override
					public void done(Integer code, BmobException exception) {
						// TODO Auto-generated method stub
						if(exception == null){
				        	//验证码发送成功
				        	//用于查询本次短信发送详情
				            //Log.i("smile", "短信id："+smsId);
							Toast.makeText(RegisterActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
							new Thread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									for (int i = 60; i > -1; i--) {
										if (!continueSend) {
											return;
										}
										
										Message message = Message.obtain();
										message.obj = i;
										mTimeHandler.sendMessage(message);
										try {
											Thread.sleep(1000);
										} catch (Exception e) {
											// TODO: handle exception
											message.obj = 1;
											mTimeHandler.sendMessage(message);
										}
										
									}
								}
							}).start();
				        } else {
							// 发送验证码失败
				        	continueSend = false;
				        	btn_get_chaptcha.setText("获取验证码");
				        	btn_get_chaptcha.setEnabled(true);
				        	if (code == 301) {
				        		Toast.makeText(RegisterActivity.this, "手机格式不正确", Toast.LENGTH_SHORT).show();
				        	}
						}
					}
				});
			}
			
			break;

		default:
			break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		phone = et_phone.getText().toString();
		userName = et_name.getText().toString();
		password = et_password.getText().toString();
		chaptcha = et_chaptcha.getText().toString();

		boolean canRegister = !TextUtils.isEmpty(phone)
				&& (phone.length() == 11) && !TextUtils.isEmpty(userName)
				&& !TextUtils.isEmpty(password)
				&& !TextUtils.isEmpty(chaptcha);
		btn_register.setEnabled(canRegister);
	}
}
