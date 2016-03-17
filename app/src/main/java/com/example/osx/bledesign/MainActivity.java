package com.example.osx.bledesign;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    Timer timer;
    public static FragmentManager fragmentmanager;

    public static final int REQUEST_CODE = 30;
    Intent service;
    RefreshTask task = null;
    static int a = 1;
    static int value = 0;
    ArrayList<Integer> listofvalues = new ArrayList<>();
    static int sum = 0;
    static int b = 0;
    boolean flagg = false;
    private android.support.v7.app.ActionBarDrawerToggle drawerToggle;
    static boolean flag = false;
    public static RedBearService mBearService;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    static ArrayList<DeviceData> devices = new ArrayList<DeviceData>();
    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBearService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBearService = ((RedBearService.LocalBinder) service)
                    .getService();

            if (mBearService != null) {
                mBearService.setListener(mIScanDeviceListener);
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("inside in");
                            mBearService.startScanDevice();
                            Thread.sleep(900);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("inside out");
                        mBearService.stopScanDevice();
                        int size = BluetoothAdapter.getDefaultAdapter().getBondedDevices().size();
                        System.out.println("paide devices" + size);
                    }
                }, 900, 900);
            }
        }
    };

    IRedBearServiceEventListener mIScanDeviceListener = new IRedBearServiceEventListener() {
        @Override
        public void onDeviceFound(String deviceAddress, String name, int rssi, int bondState, byte[] scanRecord, ParcelUuid[] uuids) {
            DeviceData deviceData = new DeviceData();
            deviceData.address = deviceAddress;
            deviceData.bondState = bondState;
            deviceData.name = name;
            deviceData.uuids = uuids;
            deviceData.rssi=rssi;
            deviceData.scanReadData = scanRecord;
            deviceData.distance = "0";
            deviceData.buzzer_volume = 1;
            deviceData.tone_location = "default";
            deviceData.volume = 1;
            deviceData.image = Utils.getimage(MainActivity.this);
            BluetoothDevice device = Utils.getbluetoothdevice(deviceAddress);
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                Mydatabase mydatabasetabase = new Mydatabase(MainActivity.this);
                ArrayList<DeviceData> listt = mydatabasetabase.getDataFromDatabase();
                for (DeviceData data : listt) {
                    if (data.getAddress().equalsIgnoreCase(deviceAddress)) {
                        if (data.getName() != null) {
                            deviceData.name = data.getName();
                        } else {
                            deviceData.name = name;
                        }
                        deviceData.distance = data.getDistance();
                        deviceData.buzzer_volume = data.getBuzzer_volume();
                        deviceData.tone_location = data.getTone_location();
                        deviceData.volume = data.getVolume();
                        deviceData.image = data.getImage();
                    }
                }
                mydatabasetabase.close();
                if (devices.size() == 0) {
                    devices.add(deviceData);
                    EventBus.getDefault().post(devices);
                } else {
                    List<String> list = new ArrayList<>();
                    for (DeviceData dd : devices) {
                        list.add(dd.address);
                    }
                    if (list.contains(deviceAddress)) {
                        int index = list.indexOf(deviceAddress);
                        devices.set(index, deviceData);
                        Fragment f = fragmentmanager.findFragmentById(R.id.container);
                        if (f instanceof AddTracker || f instanceof MyDashboard) {
                            EventBus.getDefault().post(devices);
                        }
                    } else {
                        devices.add(deviceData);
                        EventBus.getDefault().post(devices);
                    }
                }

            }
        }

        @Override
        public void onDeviceRssiUpdate(String deviceAddress, int rssi, int state, int batterylevel) {
            System.out.println("rssi value is"+rssi);
            System.out.println("battery is"+batterylevel);
            value=value+1;
            if(value<=20){
                listofvalues.add(rssi);
                if(value==20){
                    Collections.sort(listofvalues);
                    for(int i=2;i<18;i++){
                        sum=sum+listofvalues.get(i);
                    }
                    value=0;
                }
            }
            List<String> list = new ArrayList<>();
            for (DeviceData dd : devices) {
                list.add(dd.address);
            }
            if (list.contains(deviceAddress)) {
                int index = list.indexOf(deviceAddress);
                if(listofvalues.size()==20){
                    listofvalues.clear();
                    System.out.println("sum" + sum);
                    devices.get(index).setRssi(sum / 16);
                    devices.get(index).setAveragerssi(sum/16);
                    if (batterylevel > 0) {
                        devices.get(index).setBattery(batterylevel);
                    }
                    sum=0;
                    flagg=true;
                }

            }
            Fragment frag = fragmentmanager.findFragmentById(R.id.container);
            if (frag instanceof Proximity) {
                EventBus.getDefault().post(devices);
            } else if (frag instanceof MyDashboard) {
                if(flagg==true){
                    EventBus.getDefault().post(devices);
                    flagg=false;
                }
            }
            for (DeviceData d : devices) {
                int statefetch = d.getBondState();
                if (statefetch == 2) {
                    String distance = d.getDistance();
                    int dis = 0;
                    if (distance != null) {
                        if (!distance.equalsIgnoreCase("0")) {
                            String dd = distance.substring(0, distance.length() - 2);
                            dis = Integer.parseInt(dd);
                        }
                    } else {
                        d.setDistance("0");
                        dis = Integer.parseInt(d.getDistance());
                    }
                    if (dis > 0) {
                        double val = Utils.calculateDistance(-59,sum/16);
                        BigDecimal decimal=new BigDecimal(val);
                        double dist=decimal.doubleValue();
                        int type = d.getVolume();
                        if (dist > dis) {
                            String location = d.getTone_location();
                            if (location != null) {
                                if (location.equalsIgnoreCase("default")) {
                                    MediaPlayer mediaPlayer = null;
                                    try {
                                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.win);
                                        mediaPlayer.start();
                                        Thread.sleep(2000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        mediaPlayer.stop();
                                    }
                                } else {
                                    Ringtone manager = null;
                                    try {
                                        manager = RingtoneManager.getRingtone(MainActivity.this, Uri.parse(location));
                                        manager.play();
                                        System.out.println("inside location manager" + location);
                                        Thread.sleep(2000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        manager.stop();
                                    }


                                }
                            }


                        }
                    }

                }
            }
        }

        @Override
        public void onDeviceConnectStateChange(String deviceAddress, int state) {
            System.out.println("disconnecting again connection change");
            List<String> list = new ArrayList<>();
            for (DeviceData dd : devices) {
                list.add(dd.address);
            }
            if (list.contains(deviceAddress)) {
                int index = list.indexOf(deviceAddress);
                devices.get(index).setAddress(deviceAddress);
                devices.get(index).setBondState(state);
            }
            Fragment f = fragmentmanager.findFragmentById(R.id.container);
            if (f instanceof Settings) {
                try {
                    Thread.sleep(5000);
                    if (RedBearService.mBluetoothGatt != null && state == 0) {
                        RedBearService.mBluetoothGatt.close();
                    }
                    EventBus.getDefault().post(state + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    EventBus.getDefault().post(devices);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onDeviceReadValue(double[] value) {
            System.out.println("inside device read");
            Intent i = new Intent(Proximity.myaction);
            i.putExtra("data", value);
            sendBroadcast(i);
        }

        @Override
        public void onDeviceCharacteristicFound() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fragmentmanager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        byte[] arrayOfBytee = new byte[2];
        arrayOfBytee[0] = (byte) 0x0100;
        System.out.println("array of byte" + arrayOfBytee[0]);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initDrawerLayout();
        FragmentTransactions(new MyDashboard(), "mydashboard");
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect ble devices.");
                builder.setPositiveButton("Ok", null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        final Menu m = menu;
        final MenuItem item = menu.findItem(R.id.menu_hotlist);
        item.getActionView().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentTransactions(new Notifications(), "notifications");
            }
        });
        return true;
    }

    private void initDrawerLayout() {
        drawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            if (fragmentmanager.getBackStackEntryCount() > 1) {
                fragmentmanager.popBackStack();
            } else {
                finish();
            }
        }
    }

    public void FragmentTransactions(Fragment fragment, String tag) {
        Fragment frag = fragmentmanager.findFragmentByTag(tag);
        if (frag == null) {
            FragmentTransaction fragmentTransaction = fragmentmanager
                    .beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commit();
        } else {
            fragmentmanager.popBackStack(tag, 0);
        }
        System.out.println("fragment count" + fragmentmanager.getBackStackEntryCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("dashboard on resume");
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (!ba.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CODE);
        } else {
            service = new Intent(
                    MainActivity.this, RedBearService.class);
            bindService(createExplicitFromImplicitIntent(MainActivity.this, service), conn, BIND_AUTO_CREATE);
            if (mBearService != null) {
                mBearService.setListener(mIScanDeviceListener);
            }
            if (task != null) {
                task.cancel(true);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else {
            service = new Intent(
                    MainActivity.this, RedBearService.class);
            bindService(createExplicitFromImplicitIntent(MainActivity.this, service), conn, BIND_AUTO_CREATE);
            if (mBearService != null) {
                mBearService.setListener(mIScanDeviceListener);
            }
            if (task != null) {
                task.cancel(true);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover ble devices when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    class RefreshTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mBearService != null) {
                mBearService.stopScanDevice();
                mBearService.startScanDevice();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                Thread.sleep(1000);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mBearService != null) {
                mBearService.stopScanDevice();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBearService != null) {
            if (conn != null) {
                unbindService(conn);
            }

        }
        if (timer != null) {
            timer.cancel();
        }

    }


}
