package com.example.osx.bledesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by osx on 04/01/16.
 */
public class RingtonesAdapter extends BaseAdapter {

    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    LayoutInflater inflater;
    TextView tv_title;

    public RingtonesAdapter(ArrayList<HashMap<String, String>> list, Context context) {
        this.list = list;
        inflater = LayoutInflater.from(context);
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
        if (view == null) {
            view = inflater.inflate(R.layout.list_ringtones_row_item, null);
        }
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        HashMap<String, String> map = list.get(i);
        tv_title.setText(list.get(i).get("title"));
        return view;
    }
}
