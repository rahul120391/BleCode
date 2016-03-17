package com.example.osx.bledesign;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by osx on 23/12/15.
 */
public class MyListViewDevices extends Fragment {
    View view=null;

    @Bind(R.id.lv_devices)
    ListView lv_devices;
    @Bind(R.id.tv_alert)
    TextView tv_alert;

    @Bind(R.id.layout_center)
    LinearLayout layout_center;
    MyDevicesAdapter adapter;

    Timer mytimer;
    TimerTask timertask;
    static int a = 0;
    static boolean flag = false;
    List<DeviceData> data = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try{
            view=inflater.inflate(R.layout.fragment_mylistviewdevices,null);
            ButterKnife.bind(this,view);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if (mytimer != null) {
            mytimer.cancel();
        }
        if (timertask != null) {
            timertask.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        data.clear();
        data = fetchStoreDeviceList();
        List<String> addresses = new ArrayList<>();
        for (DeviceData d : data) {
            String address = d.getAddress();
            System.out.println("address stored"+address);
            addresses.add(address);
        }

        for(DeviceData dd:MainActivity.devices){
            if(addresses.contains(dd.getAddress())){
                int index=addresses.indexOf(dd.getAddress());
                System.out.println("inside devicess");
                data.get(index).setRssi(dd.getRssi());
            }

        }

        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : devices) {
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                if (!addresses.contains(device.getAddress())) {
                    System.out.println("not contains address");
                    System.out.println("address stored not contain"+device.getAddress());
                    DeviceData dataa = new DeviceData();
                    dataa.setBondState(Utils.CheckConnectionState(getActivity(), device.getAddress()));
                    dataa.setName(device.getName());
                    dataa.setImage(Utils.getimage(getActivity()));
                    dataa.setAddress(device.getAddress());
                    data.add(dataa);
                } else {
                    System.out.println("address stored contain"+device.getAddress());
                    int index = addresses.indexOf(device.getAddress());
                    data.get(index).setBondState(Utils.CheckConnectionState(getActivity(), device.getAddress()));
                }
            }
        }
        adapter=new MyDevicesAdapter(getActivity(),data);
        lv_devices.setAdapter(adapter);
        if(MainActivity.devices.size()>1){
            MainActivity.devices.clear();
        }
        System.out.println("data size is"+data.size());
        if(data.size()>0){
            adapter.NotifyData(data);
            layout_center.setVisibility(View.GONE);
            lv_devices.setVisibility(View.VISIBLE);
        }
        if (data.size() > 1) {
            for(DeviceData d:data){
                if(MainActivity.mBearService!=null){
                    System.out.println("disconnecting again");
                    MainActivity.mBearService.disconnectDevice(d.getAddress());
                }
            }

            adapter.NotifyData(data);
            layout_center.setVisibility(View.GONE);
            lv_devices.setVisibility(View.VISIBLE);
        } else if(data.size()==0){
            layout_center.setVisibility(View.VISIBLE);
            lv_devices.setVisibility(View.GONE);
        }
        

    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
    public ArrayList<DeviceData> fetchStoreDeviceList() {
        Mydatabase database = new Mydatabase(getActivity());
        ArrayList<DeviceData> list = database.getDataFromDatabase();
        System.out.println("database data size"+list.size());
        return list;
    }

    public void onEventMainThread(final List<DeviceData> list) {
        layout_center.setVisibility(View.GONE);
        System.out.println("list size" + list.size());
        System.out.println("data size is"+data.size());
        System.out.println("name is"+list.get(0).getName());
        if (data.size() == 0) {
            for (int i=0;i<list.size();i++){
                if(i%2==0){
                    list.get(i).setEven_odd(0);
                }
                else{
                    list.get(i).setEven_odd(1);
                }
                list.get(i).setImage(Utils.getimage(getActivity()));
                data.add(list.get(i));
            }
        }
        else{
            List<String> addresses = new ArrayList<>();
            for (DeviceData dataa : data) {
                addresses.add(dataa.getAddress());
            }

            System.out.println("list is coming" + list);
            for(int i=0;i<list.size();i++){
                if(addresses.contains(list.get(i).getAddress())){
                    int index = addresses.indexOf(list.get(i).getAddress());
                    if(data.get(index).name!=null){
                        list.get(i).setName(data.get(index).name);
                    }
                    list.get(i).setImage(data.get(index).getImage());
                    data.set(index, list.get(i));
                }
                else{
                    if(i%2==0){
                        list.get(i).setEven_odd(0);
                    }
                    else{
                        list.get(i).setEven_odd(1);
                    }
                    data.add(list.get(i));
                }
            }
        }
        if (data.size() == 0) {
            tv_alert.setVisibility(View.VISIBLE);
            lv_devices.setVisibility(View.GONE);
        }
        else if(data.size()==1){
            System.out.println("inside size 1");
            tv_alert.setVisibility(View.GONE);
            lv_devices.setVisibility(View.VISIBLE);
            if(list.get(0).getBondState()!=2){
                MainActivity.mBearService.connectDevice(list.get(0).getAddress(), true);
            }
            adapter.NotifyData(data);
        }
        else if (data.size() > 1) {
            tv_alert.setVisibility(View.GONE);
            lv_devices.setVisibility(View.VISIBLE);
            try {

                if (mytimer != null) {
                    mytimer.cancel();
                }
                if (timertask != null) {
                    timertask.cancel();
                }

                timertask = new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("inside timer");
                        if (flag == false) {
                            if(list.size()>0){
                                MainActivity.mBearService.connectDevice(list.get(a).getAddress(), true);
                                flag = true;
                            }

                        } else if (flag == true) {
                            if(list.size()>0){
                                    MainActivity.mBearService.disconnectDevice(list.get(a).getAddress());
                                flag = false;
                                if (a == (list.size() - 1)) {
                                    a = 0;
                                } else {
                                    a = a + 1;
                                }
                            }

                        }

                    }
                };
                mytimer = new Timer();
                mytimer.schedule(timertask, 500, 500);
                adapter.NotifyData(data);

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
}
