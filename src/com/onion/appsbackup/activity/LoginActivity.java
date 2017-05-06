package com.onion.appsbackup.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onion.appsbackup.Constant;
import com.onion.appsbackup.R;
import com.onion.appsbackup.model.User;
import com.onion.appsbackup.util.BackupActivityManager;
import com.onion.appsbackup.util.HttpTools;
import com.onion.appsbackup.view.CircleImage;
import com.onion.appsbackup.view.ProgressDialogUtils;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends Activity implements OnClickListener, TextWatcher {
	private Button btn_login;
	
	private EditText et_name;
	private String mUserName;
	private EditText et_password;
	private String mPassword;
	private CircleImage ci_qq;
	//private CircleImage ci_weibo;
	
	private TextView tv_register;
	private ProgressDialogUtils pDlgUtl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
        BackupActivityManager.getInstance().addActivity(this);
		if (mTencent == null) {
	        mTencent = Tencent.createInstance(Constant.TENCENT_APP_ID, this);
	    }
		initComponent();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	/**
	 * 初始化控件
	 */
	private void initComponent() {
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(this);
		
		et_name = (EditText) findViewById(R.id.et_phone);
		et_name.addTextChangedListener(this);
		et_password = (EditText) findViewById(R.id.et_password);
		et_password.addTextChangedListener(this);
		
		tv_register = (TextView) findViewById(R.id.tv_register);
		tv_register.setOnClickListener(this);
		
		ci_qq = (CircleImage) findViewById(R.id.ci_qq);
		ci_qq.setOnClickListener(this);
//		ci_weibo = (CircleImage) findViewById(R.id.ci_weibo);
//		ci_weibo.setOnClickListener(this);
	}
	
    /**************************************QQ开始*************************************/
	public static Tencent mTencent;
	private String openId;
	private UserInfo mInfo;
	private boolean isServerSideLogin = false;
	private void onClickLogin() {
		if (!mTencent.isSessionValid()) {
			mTencent.login(this, "all", loginListener);
            isServerSideLogin = false;
			Log.d("SDKQQAgentPref", "FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
		} else {
            if (isServerSideLogin) { 
            	// Server-Side 模式的登陆, 先退出，再进行SSO登陆
                mTencent.logout(this);
                mTencent.login(this, "all", loginListener);
                isServerSideLogin = false;
                Log.d("SDKQQAgentPref", "FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
                return;
            }
		}
	}
	IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
        	showProgressDialog();
        	Log.d("SDKQQAgentPref", "AuthorSwitch_SDK:" + SystemClock.elapsedRealtime());
            initOpenidAndToken(values);
            updateUserInfo();
        }
    };
    private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(Object response) {
            if (null == response) {
                // 返回为空，登录失败
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                // 返回为空，登录失败
                return;
            }
			//登录成功
			doComplete((JSONObject)response);
		}

		protected void doComplete(JSONObject values) {
			
		}

		@Override
		public void onError(UiError e) {
			Log.e("qq", e.errorDetail);
		}

		@Override
		public void onCancel() {
            if (isServerSideLogin) {
                isServerSideLogin = false;
            }
		}
	}
    
    private void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }
    
    private void updateUserInfo() {
		if (mTencent != null && mTencent.isSessionValid()) {
			IUiListener listener = new IUiListener() {

				@Override
				public void onError(UiError e) {
					cancelProgress();
				}

				@Override
				public void onComplete(final Object response) {
					Message msg = new Message();
					msg.obj = response;
					msg.what = 0;
					mHandler.sendMessage(msg);
					/*new Thread(){

						@Override
						public void run() {
							JSONObject json = (JSONObject)response;
							if(json.has("figureurl")){
								Bitmap bitmap = null;
								try {
									bitmap = TencentUtil.getbitmap(json.getString("figureurl_qq_2"));
								} catch (JSONException e) {

								}
								Message msg = new Message();
								msg.obj = bitmap;
								msg.what = 1;
								mHandler.sendMessage(msg);
							}
						}

					}.start();*/
				}

				@Override
				public void onCancel() {

				}
			};
			mInfo = new UserInfo(this, mTencent.getQQToken());
			mInfo.getUserInfo(listener);
		} else {
			
		}
	}
	
    Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				JSONObject response = (JSONObject) msg.obj;
				loginOrRegister(response);
			}else if(msg.what == 1){
				//Bitmap bitmap = (Bitmap)msg.obj;
				//mUserLogo.setImageBitmap(bitmap);=====================//头像
			}
		}
	};
	/************************************************QQ结束************************************************/
	
	/**
	 * 到数据库去查，如果没有就注册，有则登录
	 * @param response
	 */
	private void loginOrRegister(final JSONObject response) {
		BmobQuery<User> query = new BmobQuery<>();
		query.addWhereEqualTo("username", openId);
		query.findObjects(this, new FindListener<User>() {

			@Override
			public void onError(int arg0, String arg1) {
				//查找错误
				cancelProgress();
				mTencent.logout(LoginActivity.this);
				isServerSideLogin = false;
				Toast.makeText(LoginActivity.this, R.string.msg_update_fail, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSuccess(List<User> users) {
				// TODO Auto-generated method stub
				if (users != null && users.size() > 0) {
					// 已经存在该用户了
					BmobUser user = new BmobUser();
					user.setUsername(users.get(0).getUsername());
					user.setPassword(users.get(0).getThePassword());
					user.login(LoginActivity.this, new SaveListener() {
						
						@Override
						public void onSuccess() {
							// 登录成功
							cancelProgress();
							startActivity(new Intent(LoginActivity.this, MainActivity.class));
							finish();
						}
						
						@Override
						public void onFailure(int arg0, String arg1) {
							// 登录失败
							cancelProgress();
							mTencent.logout(LoginActivity.this);
							Toast.makeText(LoginActivity.this, R.string.msg_login_fail, Toast.LENGTH_SHORT).show();
						}
					});
					
				} else {
					User user = new User();
					user.setUsername(openId);
					String password = String.valueOf(System.currentTimeMillis());
					user.setPassword(password);
					user.setThePassword(password);
					if (response.has("nickname")) {
						try {
							user.setNickName(response.getString("nickname"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if (response.has("figureurl_qq_2")) {
						try {
							user.setImageUrl(response.getString("figureurl_qq_2"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if (response.has("gender")) {
						try {
							user.setSex(response.getString("gender"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					String address = "";
					if (response.has("province")) {
						try {
							address += response.getString("province");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (response.has("city")) {
						try {
							address += response.getString("city");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					user.setLocation(address);
					user.setAndroidRelease(Build.VERSION.RELEASE);
					user.setPhoneName(Build.MODEL);
					user.setAge(0);
					user.setPhoneNumber(((TelephonyManager) LoginActivity.this.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number());
					user.signUp(LoginActivity.this, new SaveListener() {
						
						@Override
						public void onSuccess() {
							// 成功
							cancelProgress();
							startActivity(new Intent(LoginActivity.this, MainActivity.class));
							finish();
						}
						
						@Override
						public void onFailure(int arg0, String arg1) {
							// 注册失败
							cancelProgress();
							mTencent.logout(LoginActivity.this);
							isServerSideLogin = false;
							Toast.makeText(LoginActivity.this, R.string.msg_update_fail, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    mTencent.onActivityResultData(requestCode, resultCode, data, loginListener);
	    super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 显示加载中...
	 */
	private void showProgressDialog() {
		if (pDlgUtl == null) {
			pDlgUtl = new ProgressDialogUtils(LoginActivity.this);
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
		if (HttpTools.isNetworkConnected(this)) {
			switch (v.getId()) {
			case R.id.ci_qq:
				// 用QQ登录
				onClickLogin();
				break;
				
			/*case R.id.ci_weibo:
				// 用微博登录
				Toast.makeText(this, "将在下一版本支持新浪微博登录", Toast.LENGTH_SHORT).show();
				break;*/
			
			case R.id.btn_login:
				// 点击登录
				showProgressDialog();
				BmobUser user = new BmobUser();
				user.setPassword(mPassword);
				user.setUsername(mUserName);
				user.login(this, new SaveListener() {
					
					@Override
					public void onSuccess() {
						if (pDlgUtl != null) {
							pDlgUtl.hide();
						}
						Toast.makeText(LoginActivity.this, R.string.msg_login_suc, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
						finish();
					}
					
					@Override
					public void onFailure(int arg0, String arg1) {
						// 用手机号登录
						BmobQuery<User> query = new BmobQuery<User>();
						query.addWhereEqualTo("mobilePhoneNumber", mUserName);
						
						query.findObjects(LoginActivity.this, new FindListener<User>() {

							@Override
							public void onError(int arg0, String arg1) {
								// TODO Auto-generated method stub
								cancelProgress();
								Toast.makeText(LoginActivity.this, R.string.msg_name_or_psw_error, Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onSuccess(List<User> users) {
								// TODO Auto-generated method stub
								if (users != null && users.size() == 1) {
									BmobUser retryUser = new BmobUser();
									retryUser.setPassword(mPassword);
									retryUser.setUsername(users.get(0).getUsername());
									retryUser.login(LoginActivity.this, new SaveListener() {
										
										@Override
										public void onSuccess() {
											cancelProgress();
											// 用手机号登录成功
											startActivity(new Intent(LoginActivity.this, MainActivity.class));
											finish();
										}
										
										@Override
										public void onFailure(int arg0, String arg1) {
											// 用手机号登录失败
											cancelProgress();
											Toast.makeText(LoginActivity.this, R.string.msg_name_or_psw_error, Toast.LENGTH_SHORT).show();
										}
									});
								} else {
									cancelProgress();
									Toast.makeText(LoginActivity.this, R.string.msg_name_or_psw_error, Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				});
				break;
				
			case R.id.tv_register:
				Toast.makeText(this, "注册暂时不开放了，请使用QQ登录", Toast.LENGTH_SHORT).show();
				//startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), 0);
				break;

			default:
				break;
			}
		} else {
			Toast.makeText(LoginActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
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
		mUserName = et_name.getText().toString();
		mPassword = et_password.getText().toString();
		if (!TextUtils.isEmpty(mUserName) && !TextUtils.isEmpty(mPassword)) {
			btn_login.setEnabled(true);
		} else {
			btn_login.setEnabled(false);
		}
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}
}
