package com.bestv.ott.jingxuan.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.util.IOTV_Date;

public class MetroScheduleListAdapter extends BaseAdapter{
    private List<Schedule> itemlist;
    private Context mContext;
    
    LayoutInflater inflater;
    
    View[] mViewGroup;
    
    public MetroScheduleListAdapter(Context c){
    	mContext = c;
    	
    	inflater = (LayoutInflater)c.getSystemService
    		      (Context.LAYOUT_INFLATER_SERVICE);
    	
    	itemlist = new ArrayList<Schedule>();
    }
    
    public void setItemList(List<Schedule> list){
    	itemlist = list;
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return itemlist.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return itemlist.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View viewitem;
		if (convertView == null) {
			viewitem = inflater.inflate(R.layout.jx_iotv_schedule_list_item, null);
		} else {
			viewitem = convertView;
		}
		
		Schedule s = itemlist.get(position);
		TextView v = (TextView) viewitem.findViewById(R.id.schedule_name);
		v.setText(s.getName());
		
		v = (TextView) viewitem.findViewById(R.id.schedule_start_time);
		v.setText(IOTV_Date.StringToHourMinutes(s.getStartTime()));

		return viewitem;
	}
}
