package com.example.osx.bledesign;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by osx on 23/12/15.
 */
public class MyDevicesAdapter extends BaseAdapter {
    LayoutInflater inflater;
    Context context;
    List<DeviceData> values;
    List<Integer> list;
    DonutProgress donut_progress;
    ImageView iv_settings, iv_trackerimage, iv_last_known;
    TextView tv_name, tv_distance, tv_proximity, tv_battery,tv_averagesum,tv_rssi;

    public MyDevicesAdapter(Context context, List<DeviceData> values) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
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
    public int getItemViewType(int position) {
        return values.get(position).getEven_odd();
    }

    public void NotifyData(List<DeviceData> data) {
        values = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        View v = convertView;
        int type = getItemViewType(position);
        if (v == null) {
            if (type == 0) {
                v = inflater.inflate(R.layout.listview_even_layout, viewGroup, false);
            } else {
                v = inflater.inflate(R.layout.listview_odd_layout, viewGroup, false);
            }
        }
        iv_trackerimage = (ImageView) v.findViewById(R.id.iv_trackerimage);
        tv_proximity = (TextView) v.findViewById(R.id.tv_proximity);
        tv_proximity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("proximity clicked");
                String address = values.get(position).getAddress();
                Bundle bundle = new Bundle();
                bundle.putString("address", address);
                Proximity prox = new Proximity();
                prox.setArguments(bundle);
                ((MainActivity) context).FragmentTransactions(prox, "proxy");
            }
        });
        iv_settings = (ImageView) v.findViewById(R.id.iv_settings);
        iv_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = values.get(position).getName();
                System.out.println("name is" + name);
                String address = values.get(position).getAddress();
                byte[] image = values.get(position).getImage();
                Settings settings = new Settings();
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("address", address);
                bundle.putByteArray("image", image);
                settings.setArguments(bundle);
                ((MainActivity) context).FragmentTransactions(settings, "settings");
            }
        });
        tv_name = (TextView) v.findViewById(R.id.tv_name);
        if (values.get(position).name != null) {
            tv_name.setText(values.get(position).name);
        } else {
            tv_name.setText("No name");
        }
        if (values.get(position).getImage() != null) {
            Drawable d = new BitmapDrawable(context.getResources(), Utils.getbitmap(values.get(position).getImage()));
            iv_trackerimage.setBackground(d);
        } else {
            iv_trackerimage.setBackgroundResource(R.drawable.header);
        }
        donut_progress = (DonutProgress) v.findViewById(R.id.donut_progress);
        donut_progress.setFinishedStrokeWidth(4);
        donut_progress.setUnfinishedStrokeWidth(4);
        donut_progress.setFinishedStrokeColor(ContextCompat.getColor(context, R.color.green));
        donut_progress.setUnfinishedStrokeColor(ContextCompat.getColor(context, R.color.grey));
        donut_progress.setTextColor(ContextCompat.getColor(context, R.color.green));
        donut_progress.setMax(100);
        donut_progress.setTextSize(0);
        tv_averagesum=(TextView)v.findViewById(R.id.tv_averagesum);
        tv_battery = (TextView) v.findViewById(R.id.tv_battery);
        if (values.get(position).getBattery() > 0) {
            tv_battery.setText(values.get(position).getBattery() + "%");
        }
        if (values.get(position).rssi == 10) {
            tv_proximity.setVisibility(View.GONE);
        } else {
            tv_proximity.setVisibility(View.VISIBLE);
            double val = Utils.calculateDistance(-59, values.get(position).rssi);
            BigDecimal decimal=new BigDecimal(val);
            double dis=decimal.doubleValue();
            String accuracy=getDistance(dis);
            tv_averagesum.setText(accuracy);
            System.out.println("accuracy is"+accuracy);
            System.out.println("rssi value coming" + values.get(position).rssi);
            System.out.println("value of distance is" + dis);
           // tv_proximity.setText(dis+"");
            if(dis<1){
                tv_proximity.setText("<"+1);
            }
            else if(dis>1 && dis<3){
                tv_proximity.setText("<"+3);
            }
            else if(dis>3 && dis<6){
                tv_proximity.setText("<"+6);
            }
            else if(dis>6 && dis<10){
                tv_proximity.setText("<"+10);
            }
            else{
                tv_proximity.setText(">"+10);
            }

        }
        return v;
    }

    private String getDistance(double accuracy) {
        if (accuracy == -1.0) {
            return "Unknown";
        } else if (accuracy < 1) {
            return "Less than 1 meter";
        } else if (accuracy < 3) {
            return "Less than 3 meter";
        }
        else if(accuracy<6){
            return "Less than 6 meter";
        }
        else if(accuracy<10){
            return "Less than 10 meter";
        }
        else {
            return "Far";
        }
    }
}
