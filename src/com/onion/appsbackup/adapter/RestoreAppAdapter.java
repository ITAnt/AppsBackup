package com.onion.appsbackup.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onion.appsbackup.R;
import com.onion.appsbackup.model.RestoreApp;

public class RestoreAppAdapter extends BaseAdapter {
	
	private List<RestoreApp> apps;
	private Context mContext; 
	private ViewHolder mHolder;
	
	public RestoreAppAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}
	
	public void setApps(List<RestoreApp> apps) {
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
	public RestoreApp getItem(int position) {
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
		RestoreApp app = apps.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_restore_app_list, null);
			mHolder = new ViewHolder();
			mHolder.ll_restore_app_item = (LinearLayout) convertView.findViewById(R.id.ll_restore_app_item);
			mHolder.tv_restore_app_item = (TextView) convertView.findViewById(R.id.tv_restore_app_item);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		if (app.isChecked()) {
			mHolder.tv_restore_app_item.setTextColor(mContext.getResources().getColor(R.color.black));
		} else {
			mHolder.tv_restore_app_item.setTextColor(mContext.getResources().getColor(R.color.grey_text));
			
		}
		
		mHolder.ll_restore_app_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (onAppItemClickListener != null) {
					onAppItemClickListener.onAppItemClick(position);
				}
			}
		});
		mHolder.tv_restore_app_item.setText(app.getAppName());
		return convertView;
	}
	
	private static class ViewHolder {
		
		LinearLayout ll_restore_app_item;
		TextView tv_restore_app_item;
	}

	public interface OnAppItemClickListener {
		public void onAppItemClick(int position);
	}
	
	private OnAppItemClickListener onAppItemClickListener;

	public void setOnAppItemClickListener(OnAppItemClickListener onAppItemClickListener) {
		this.onAppItemClickListener = onAppItemClickListener;
	}
}
