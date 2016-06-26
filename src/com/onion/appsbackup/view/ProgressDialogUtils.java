package com.onion.appsbackup.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onion.appsbackup.R;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
   自定义dialog
 * * * * * * * * * * * * * * * * * * * * * * *
 */
public class ProgressDialogUtils {
        private Activity ac;
        private Dialog dialog;
        private TextView tv;

        public ProgressDialogUtils(Activity activity) {
                ac = activity;
                LinearLayout parent = (LinearLayout) LayoutInflater.from(activity)
                        .inflate(R.layout.item_loading, null);
                tv = (TextView) parent.findViewById(R.id.wait_loading_text);
                dialog = new Dialog(ac, R.style.dialog);
                dialog.setContentView(parent);
                dialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        }

        public boolean isShow() {
                if (dialog != null && ac != null && !ac.isFinishing()) {
                        return dialog.isShowing();
                } else {
                        return false;
                }

        }

        public void show() {
                try {
                        if (dialog != null && ac != null && !ac.isFinishing()) {
                                dialog.show();
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public void show(final String text) {
                try {
                        if (dialog != null && ac != null && !ac.isFinishing()) {
                                ac.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                                tv.setText(text);
                                                dialog.show();
                                        }
                                });

                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public void hide() {
                try {
                        if (dialog != null && ac != null && !ac.isFinishing()) {
                                dialog.dismiss();
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }

        }

        public void setBackfinishActivity(final Activity ac, boolean b) {

                if (!b) {
                        dialog.setOnCancelListener(null);
                } else if (ac != null) {
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                        ac.finish();
                                }
                        });
                }


        }
}
