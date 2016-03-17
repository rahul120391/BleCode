package com.example.osx.bledesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by osx on 29/12/15.
 */
public class NotificationsAdapter extends BaseAdapter {

    LayoutInflater inflater;
    public NotificationsAdapter(Context context){
        inflater=LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view=inflater.inflate(R.layout.notifications_row_item,null);
        }
        return view;
    }
}
