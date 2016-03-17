package com.example.osx.bledesign;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by osx on 24/12/15.
 */
public class AvailableDevices extends Fragment{
    View view=null;
    PopupWindow popupWindow;
    View dialoglayout;
    @Bind(R.id.lv_listdevices)
    ListView lv_liListView;

    @Bind(R.id.tv_alert)
    TextView tv_alert;

    @Bind(R.id.layout_center)
    LinearLayout layout_center;
    ListDeviceAdapter adapter;
    List<DeviceData> data= new ArrayList<>();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_availabledevices,null);
        getActivity().setTitle(getString(R.string.available_devices));
        ButterKnife.bind(this, view);
        lv_liListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /*
                DeviceData data= (DeviceData) adapterView.getItemAtPosition(i);
                AddTracker tracker=new AddTracker();
                Bundle bundle=new Bundle();
                String name=data.getName();
                String address=data.getAddress();
                System.out.println("name"+name);
                bundle.putString("name",name);
                bundle.putString("address",address);
                tracker.setArguments(bundle);
                */
                ((MainActivity) getActivity()).FragmentTransactions(new AddTracker(), "addtracker");
            }
        });
        data.clear();
        /*
        if(data.size()>0){
            layout_center.setVisibility(View.GONE);
            lv_liListView.setVisibility(View.VISIBLE);
        }
        else{
            layout_center.setVisibility(View.VISIBLE);
            lv_liListView.setVisibility(View.GONE);
        }
        */
       // adapter = new ListDeviceAdapter(getActivity());
        lv_liListView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        System.out.println("inside on prepare optionsmenu addtracker");
        menu.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        /*
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        List<String> addresses = new ArrayList<>();
        for (DeviceData d : data) {
            String address = d.getAddress();
            addresses.add(address);
        }
        for (BluetoothDevice device : devices) {
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                if(addresses.contains(device.getAddress())){
                    data.remove(device.getAddress());
                }
            }
        }
        adapter.NotifyData(data);
        */

    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
    public void onEventMainThread(List<DeviceData> list) {
        System.out.println("inside event main thread");
        System.out.println("inside availble devices");
        layout_center.setVisibility(View.GONE);
        data=new ArrayList<>(list);
        if (data.size() == 0) {
            tv_alert.setVisibility(View.VISIBLE);
            lv_liListView.setVisibility(View.GONE);
        } else if (data.size() > 0) {
            System.out.println("list size" + list.size());
            tv_alert.setVisibility(View.GONE);
            lv_liListView.setVisibility(View.VISIBLE);
            adapter.NotifyData(data);
        }
    }


}
