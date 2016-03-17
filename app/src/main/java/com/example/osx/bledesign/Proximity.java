package com.example.osx.bledesign;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by osx on 24/12/15.
 */
public class Proximity extends Fragment implements SensorEventListener {
    View view = null;
    private float currentDegree = 0f;

    public static final String myaction = "com.recieve";
    // device sensor manager
    @Bind(R.id.iv_sensorimage)
    ImageView iv_sensorimage;

    @Bind(R.id.btn_press)
    Button btn_press;


    @Bind(R.id.tv_x)
    TextView tv_x;

    @Bind(R.id.tv_y)
    TextView tv_y;

    @Bind(R.id.tv_z)
    TextView tv_z;

    Bundle bundle;
    String macaddress;
    double dis;
    ArrayList<DeviceData> data;
    static boolean flag1 = false;
    int state;

    float[] inR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];

    double azimuth = 0;
    double pitch = 0;
    double roll = 0;
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer;
    private final float NOISE = (float) 2.0;
    float x, y, z;
    double x1, y1, z1;
    boolean retvalue;
    static boolean myflag = false;
    ArrayList<Double> xvalues = new ArrayList<>();
    ArrayList<Double> yvalues = new ArrayList<>();
    ArrayList<Double> zvalues = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_proximity, null);
        getActivity().setTitle(getString(R.string.proxy));
        ButterKnife.bind(this, view);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        bundle = getArguments();
        if (bundle != null) {
            macaddress = bundle.getString("address");
            Mydatabase database = new Mydatabase(getActivity());
            data = database.getrecord(macaddress);
            database.close();
        }
        /*
        if(Utils.CheckConnectionState(getActivity(), macaddress) == 2) {
            try{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean retvalue=MainActivity.mBearService.EnableAccel(1);
                        if(retvalue){
                            System.out.println("getting xyz");
                            MainActivity.mBearService.getXYZ();
                        }
                    }
                });

            }
            catch (Exception ee){
                ee.printStackTrace();
            }

        }
        else{
            Utils.ShowSnackBar(getActivity(), "Connect device first to get the proximity");
        }
        */


        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        System.out.println("inside on prepare optionsmenu addtracker");
        menu.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            double x2 = x1 - x;
            double y2 = y1 - y;
            double z2 = z - z1;
          /*  System.out.println("x2 value is="+x2);
            System.out.println("y2 value is="+y2);
            System.out.println("z2 value is=" + z2);
            float degree=Math.round(x2);
            RotateAnimation ra=new RotateAnimation(currentDegree,degree,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            ra.setDuration(120);
            ra.setFillAfter(true);
            iv_sensorimage.startAnimation(ra);
            currentDegree=-degree;
            */
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(myreciever, new IntentFilter(myaction));
        EventBus.getDefault().register(this);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(myreciever);
        EventBus.getDefault().unregister(this);
        mSensorManager.unregisterListener(this);
    }

    public void onEventMainThread(final List<DeviceData> list) {
        System.out.println("inside proximity");
        List<String> data = new ArrayList<>();
        for (DeviceData dd : list) {
            data.add(dd.getAddress());
        }

        if (data.contains(macaddress)) {
            int index = data.indexOf(macaddress);
            int rssi = list.get(index).getRssi();
            dis = Utils.calculateDistance(-6, rssi);
            state = list.get(index).getBondState();
        } else {
            System.out.println("not containing mac address");
        }
    }

    @OnClick(R.id.btn_press)
    public void press() {
        System.out.println("data size is" + data.size());
        int type = 0;
        if (data.size() > 0) {
            String distance = data.get(0).getDistance();
            type = data.get(0).getBuzzer_volume();
            System.out.println("type is" + type);
        }
        if(retvalue==false){
            retvalue = MainActivity.mBearService.EnableAccel(1);
            System.out.println("enable accel" + retvalue);
            if (retvalue) {
                Utils.ShowSnackBar(getActivity(),"accelerometer enabled");
                MainActivity.mBearService.getXYZ();
            }
        }
        if (state == 2) {
            switch (type) {
                case 0:
                    MainActivity.mBearService.BeepBuzzer(1);
                    break;
                case 1:
                    MainActivity.mBearService.BeepBuzzer(1);
                    break;
                case 2:
                    MainActivity.mBearService.BeepBuzzer(2);
                    break;
            }

        }


    }

    BroadcastReceiver myreciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                System.out.println("inside my reciever");
                double[] data = intent.getDoubleArrayExtra("data");
                x1 = data[0];
                y1 = data[1];
                z1 = data[2];
                if(xvalues.size()<100){
                    xvalues.add(x1);
                }
                if (yvalues.size()<100){
                    yvalues.add(y1);
                }
                if(zvalues.size()<100){
                    zvalues.add(z1);
                }
                System.out.println("x values" + xvalues);
                System.out.println("y values" + yvalues);
                System.out.println("z values" + zvalues);
                tv_x.setVisibility(View.VISIBLE);
                tv_y.setVisibility(View.VISIBLE);
                tv_z.setVisibility(View.VISIBLE);
                tv_x.setText("X :" + x1);
                tv_y.setText("Y :" + y1);
                tv_z.setText("Z :" + z1);
                float degree = Math.round(x1);
                RotateAnimation ra = new RotateAnimation(
                        currentDegree, -degree,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);
                ra.setDuration(100);
                ra.setFillAfter(true);
                iv_sensorimage.startAnimation(ra);
                currentDegree = -degree;
            }
        }
    };
}
