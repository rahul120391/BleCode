package com.example.osx.bledesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by osx on 05/01/16.
 */
public class SelectionAdapter extends BaseAdapter {

    List<String> list;
    LayoutInflater inf;
    TextView tv_title;
    public SelectionAdapter(List<String> list,Context context){
        inf=LayoutInflater.from(context);
        this.list=list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view=inf.inflate(R.layout.list_ringtones_row_item,null);
        }
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(list.get(i));
        return view;
    }
}
