package com.onion.appsbackup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onion.appsbackup.R;
import com.onion.appsbackup.model.App;

import java.util.List;

public class AppAdapter extends BaseAdapter {
	
	private List<App> apps;
	private Context mContext; 
	private ViewHolder mHolder;
	
	public AppAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}
	
	public void setApps(List<App> apps) {
		this.apps = apps;
	}



	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (apps != null) {
			return apps.size();
		}
		return 0;
	}

	@Override
	public App getItem(int position) {
		// TODO Auto-generated method stub
		if (apps != null) {
			return apps.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		App app = apps.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_app_list, null);
			mHolder = new ViewHolder();
			mHolder.iv_app_icon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
			mHolder.tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
			mHolder.tv_app_package_name = (TextView) convertView.findViewById(R.id.tv_app_package_name);
			mHolder.iv_checked = (ImageView) convertView.findViewById(R.id.iv_checked);
			mHolder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.ll_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (onAppItemClickListener != null) {
					onAppItemClickListener.onAppItemClick(position);
				}
			}
		});

		if (app.getAppIcon() != null) {
			mHolder.iv_app_icon.setImageBitmap(app.getAppIcon());
		}

		mHolder.tv_app_name.setText(app.getAppName());
		mHolder.tv_app_package_name.setText(app.getAppPackageName());
		if (app.isChecked()) {
			//mHolder.ll_item.setBackgroundColor(mContext.getResources().getColor(R.color.cute_dream_green_light));
			mHolder.iv_checked.setVisibility(View.VISIBLE);
			mHolder.tv_app_name.setTextColor(mContext.getResources().getColor(R.color.black));
		} else {
			//mHolder.ll_item.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
			mHolder.iv_checked.setVisibility(View.GONE);
			mHolder.tv_app_name.setTextColor(mContext.getResources().getColor(R.color.grey_text));
		}
		return convertView;
	}
	
	private static class ViewHolder {
		LinearLayout ll_item;
		ImageView iv_app_icon;
		TextView tv_app_name;
		TextView tv_app_package_name;
		ImageView iv_checked;
	}

	public interface OnAppItemClickListener {
		public void onAppItemClick(int position);
	}
	
	private OnAppItemClickListener onAppItemClickListener;

	public void setOnAppItemClickListener(OnAppItemClickListener onAppItemClickListener) {
		this.onAppItemClickListener = onAppItemClickListener;
	}
}
