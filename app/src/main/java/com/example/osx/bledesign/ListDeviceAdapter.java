package com.example.osx.bledesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by osx on 24/12/15.
 */
public class ListDeviceAdapter extends BaseAdapter{

    LayoutInflater inflater;
    List<DeviceData> data;
    TextView tv_trackername,tv_macaddress;
    public ListDeviceAdapter(Context context,List<DeviceData> data){
        inflater=LayoutInflater.from(context);
        this.data=data;
    }
    public void NotifyData(List<DeviceData> data){
        this.data=data;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view=inflater.inflate(R.layout.layout_listdevices_row_item,null);
        }
        tv_trackername=(TextView)view.findViewById(R.id.tv_trackername);
        tv_macaddress=(TextView)view.findViewById(R.id.tv_macaddress);
        tv_trackername.setText("Tracker Name:"+data.get(i).getName());
        tv_macaddress.setText("Mac Address:"+data.get(i).getAddress());
        return view;
    }
}
