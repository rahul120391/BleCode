package com.example.osx.bledesign;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class RedBearService extends Service {

    static final String TAG = RedBearService.class.getName();

    public static final UUID RBL_SERVICE = UUID
            .fromString("713D0000-503E-4C75-BA94-3148F18D941E");

    public static final UUID RBL_DEVICE_RX_UUID = UUID
            .fromString("713D0002-503E-4C75-BA94-3148F18D941E");

    public static final UUID RBL_DEVICE_TX_UUID = UUID
            .fromString("713D0003-503E-4C75-BA94-3148F18D941E");

    public static final UUID CCC = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID SERIAL_NUMBER_STRING = UUID
            .fromString("00002A25-0000-1000-8000-00805f9b34fb");

    private final static UUID BATTERY_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private final static UUID BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private static final UUID IMMEDIATE_ALERT_SERVICE =
            UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID IMMEDIATE_ALERT_LEVEL =
            UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    private static final UUID FETCH_X_VALUE =
            UUID.fromString("00002800-0000-1000-8000-00805f9b34fb");

    private static final UUID LOCATION_AND_NAVIGATION =
            UUID.fromString("00001819-0000-1000-8000-00805f9b34fb");

    private static final UUID LOCATION_AND_SPEED =
            UUID.fromString("00002a67-0000-1000-8000-00805f9b34fb");
    private static final UUID TX_POWER_SERVICE = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    private static final UUID TX_POWER_LEVEL = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");

    private static final UUID ACCEL_SERVICE_UUID = UUID.fromString("0000FFA0-0000-1000-8000-00805f9b34fb");
    private static final UUID ACCEL_ENABLE_UUID = UUID.fromString("0000FFA1-0000-1000-8000-00805f9b34fb");

    private static final UUID ACCEL_NEW = UUID.fromString("0000FFA2-0000-1000-8000-00805f9b34fb");
    private static final UUID X_ENABLE_UUID = UUID.fromString("0000FFA3-0000-1000-8000-00805f9b34fb");
    private static final UUID Y_ENABLE_UUID = UUID.fromString("0000FFA4-0000-1000-8000-00805f9b34fb");
    private static final UUID Z_ENABLE_UUID = UUID.fromString("0000FFA5-0000-1000-8000-00805f9b34fb");
    private static final UUID X_ENABLE_UUID_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final UUID CUSTOM_SERVICE = UUID.fromString("0000FFE0-0000-1000-8000-00805f9b34fb");
    private static final UUID CUSTOM_CHAR = UUID.fromString("0000FFE1-0000-1000-8000-00805f9b34fb");
    private static final UUID GATT_CHAR_UUID = UUID.fromString("00002803-0000-1000-8000-00805f9b34fb");
    private static final UUID X_ENABLE_GATT_CONFIG = UUID.fromString("0000003B-0000-1000-8000-00805f9b34fb");
    private static final byte[] ENABLE_SENSOR = {0x01};
    private BluetoothAdapter mBtAdapter = null;
    Integer x, y, z;
    private BluetoothGattService mVehicleInfoService;
    private BluetoothGattCharacteristic mLocationAndSpeedChar;
    private float mLatitude;
    BluetoothGattService keyService;
    private float mLongitude;
    BluetoothGattCharacteristic enableX, enableY, enableZ;
    List<BluetoothGattCharacteristic> list = new ArrayList<>();

    public static Timer mBatteryTimer, mRssiTimer;
    public static TimerTask task1, task2;
    public static BluetoothGatt mBluetoothGatt = null;
    Integer batterylevel = 0;

    private IRedBearServiceEventListener mIRedBearServiceEventListener;

    HashMap<String, BluetoothDevice> mDevices = null;
    private BluetoothGattCharacteristic txCharc = null;
    BluetoothGattCharacteristic chars = null;
    boolean statuss = false;

    public void startScanDevice() {
        if (mDevices != null) {
            mDevices.clear();
        } else {
            mDevices = new HashMap<String, BluetoothDevice>();
        }

        startScanDevices();
    }

    public void stopScanDevice() {
        stopScanDevices();
    }

    public void setListener(IRedBearServiceEventListener mListener) {
        mIRedBearServiceEventListener = mListener;
    }

    public boolean isBLEDevice(String address) {
        BluetoothDevice mBluetoothDevice = mDevices.get(address);
        if (mBluetoothDevice != null) {
            return isBLEDevice(address);
        }
        return false;
    }

    public void connectDevice(String address, boolean autoconnect) {
        //BluetoothDevice mBluetoothDevice = mDevices.get(address);
        BluetoothDevice mBluetoothDevice = Utils.getbluetoothdevice(address);
        if (mBluetoothDevice != null) {
            connect(mBluetoothDevice, autoconnect);
        }
    }

    public void disconnectDevice(String address) {
        System.out.println("address of bluetooth device" + address);
        //BluetoothDevice mBluetoothDevice = mDevices.get(address);
        BluetoothDevice mBluetoothDevice = Utils.getbluetoothdevice(address);
        if (mBluetoothDevice != null) {
            disconnect(mBluetoothDevice);
        } else {
            System.out.println("bluetooth device is null");
        }
    }

    public void readRssi(String deviceAddress) {
        readDeviceRssi(deviceAddress);
    }

    public void writeValue(String deviceAddress, char[] data) {
        if (txCharc != null) {
            String value = new String(data);

            if (txCharc.setValue(value)) {
                if (!mBluetoothGatt.writeCharacteristic(txCharc)) {
                    Log.e(TAG, "Error: writeCharacteristic!");
                }
            } else {
                Log.e(TAG, "Error: setValue!");
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public RedBearService getService() {
            return RedBearService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();

        if (mBtAdapter == null)
            return;

        if (mDevices == null) {
            mDevices = new HashMap<String, BluetoothDevice>();
        }

    }

    public boolean isBLEDevice(BluetoothDevice device) {
        if (mBluetoothGatt != null) {
            return true;
        } else {
            return false;
        }
    }

    private void startScanDevices() {
        if (mBtAdapter == null)
            return;

        mBtAdapter.startLeScan(mLeScanCallback);
    }

    protected void stopScanDevices() {
        if (mBtAdapter == null)
            return;

        mBtAdapter.stopLeScan(mLeScanCallback);
    }

    protected void readDeviceRssi(String address) {
        BluetoothDevice mDevice = mDevices.get(address);
        if (mDevice != null) {
            readDeviceRssi(mDevice);
        }
    }

    protected void readDeviceRssi(BluetoothDevice device) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    protected void connect(BluetoothDevice device, boolean autoconnect) {
        mBluetoothGatt = device.connectGatt(this, autoconnect, mGattCallback);
    }

    protected void disconnect(BluetoothDevice device) {
        System.out.println("trying to disconnect");
        if (mBluetoothGatt != null) {
            System.out.println("bluetooth gatt is not null");
            mBluetoothGatt.disconnect();
            //mBluetoothGatt.close();
            //mBluetoothGatt=null;
        } else {
            System.out.println("bluetooth gatt is null");
            mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
                //mBluetoothGatt.close();
                //mBluetoothGatt=null;
            }

        }


    }

    @Override
    public void onDestroy() {
        if (mBluetoothGatt == null)
            return;

        //mBluetoothGatt.disconnect();
        //mBluetoothGatt = null;


        super.onDestroy();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            if (mIRedBearServiceEventListener != null) {
                addDevice(device);
                if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                    mIRedBearServiceEventListener.onDeviceFound(
                            device.getAddress(), device.getName(), rssi,
                            device.getBondState(), scanRecord, device.getUuids());
                }

            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            //super.onCharacteristicChanged(gatt, characteristic);
            System.out.println("the value is=" + characteristic.getUuid());
            if (characteristic.getUuid().equals(X_ENABLE_UUID)) {
                x = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
            }
            if (characteristic.getUuid().equals(Y_ENABLE_UUID)) {
                y = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
            }
            if (characteristic.getUuid().equals(Z_ENABLE_UUID)) {
                z = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0) * -1;
            }
            double x1 = x / 64.0;
            double y1 = y / 64.0;
            double z1 = z / 64.0;
            System.out.println("integer value of x1 is=" + x1);
            System.out.println("integer value of y1 is=" + y1);
            System.out.println("integer value of z1 is=" + z1);
            double values[] = {x1, y1, z1};
            if (mIRedBearServiceEventListener != null) {
                mIRedBearServiceEventListener.onDeviceReadValue(values);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, final int status) {
            //super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                String uuid = characteristic.getUuid().toString();
            }
            chars = characteristic;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        ;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            final int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            final BluetoothDevice device = gatt.getDevice();
            if (mIRedBearServiceEventListener != null) {
                mIRedBearServiceEventListener.onDeviceConnectStateChange(
                        device.getAddress(), newState);
            }
            if (mRssiTimer != null) {
                mRssiTimer.cancel();
            }
            if (task2 != null) {
                task2.cancel();
            }
            try {
                task2 = new TimerTask() {
                    @Override
                    public void run() {

                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            readDeviceRssi(device);

                            if (mBluetoothGatt != null) {
                                mBluetoothGatt.discoverServices();
                            }
                            if (chars != null) {
                                batterylevel = chars.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                            }
                        } else {
                            if (mRssiTimer != null) {
                                mRssiTimer.cancel();
                            }
                            if (task2 != null) {
                                task2.cancel();
                            }
                        }


                    }
                };
                mRssiTimer = new Timer();
                mRssiTimer.schedule(task2, 500,800);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor device, int status) {
            super.onDescriptorRead(gatt, device, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor arg0, int status) {
            super.onDescriptorWrite(gatt, arg0, status);
            //mBluetoothGatt.readCharacteristic(enableY);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            BluetoothDevice device = gatt.getDevice();
            if (mIRedBearServiceEventListener != null) {
                mIRedBearServiceEventListener.onDeviceRssiUpdate(
                        device.getAddress(), rssi, status, batterylevel);
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService rblService = mBluetoothGatt
                    .getService(RBL_SERVICE);
            list.clear();

            for (BluetoothGattService service : gatt.getServices()) {
                if (service.getUuid().equals(BATTERY_UUID)) {
                    Log.d(TAG, String.valueOf(service.getUuid()));
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(BATTERY_LEVEL);
                    if (characteristic != null) {
                        mBluetoothGatt.readCharacteristic(characteristic);
                    }

                }

            }
            if (rblService == null) {
                return;
            }

            List<BluetoothGattCharacteristic> Characteristic = rblService
                    .getCharacteristics();

            for (BluetoothGattCharacteristic a : Characteristic) {

            }

            BluetoothGattCharacteristic rxCharc = rblService
                    .getCharacteristic(RBL_DEVICE_RX_UUID);
            if (rxCharc == null) {
                return;
            }

            txCharc = rblService.getCharacteristic(RBL_DEVICE_TX_UUID);
            if (txCharc == null) {
                return;
            }

            enableNotification(true, rxCharc);

            if (mIRedBearServiceEventListener != null) {
                mIRedBearServiceEventListener.onDeviceCharacteristicFound();
            }


        }
    };

    public boolean enableNotification(boolean enable,
                                      BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            return false;
        }
        if (!mBluetoothGatt.setCharacteristicNotification(characteristic,
                enable)) {
            return false;
        }

        BluetoothGattDescriptor clientConfig = characteristic
                .getDescriptor(CCC);
        if (clientConfig == null) {
            System.out.println("client config is null");
            return false;
        }
        if (enable) {
            Log.i(TAG, "enable notification");
            System.out.println("enable notification");
            clientConfig
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            Log.i(TAG, "disable notification");
            clientConfig
                    .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        return mBluetoothGatt.writeDescriptor(clientConfig);
    }

    void addDevice(BluetoothDevice mDevice) {
        String address = mDevice.getAddress();

        mDevices.put(address, mDevice);
    }


    public void BeepBuzzer(int value) {
        if (mBluetoothGatt != null) {
            BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(IMMEDIATE_ALERT_SERVICE);
            if (bluetoothGattService == null) {
                System.out.println("gatt service is null");
                return;
            } else {
                System.out.println("the gatt service is" + bluetoothGattService + "");
            }
            BluetoothGattCharacteristic characteristic =
                    bluetoothGattService.getCharacteristic(IMMEDIATE_ALERT_LEVEL);
            if (characteristic == null) {
                System.out.println("gatt char is null");
                return;
            } else {
            }
            /*
            byte[] arrayOfByte = new byte[1];
            switch (value) {
                case 0:
                    arrayOfByte[0] = (byte) 0x00;
                case 1:
                    arrayOfByte[0] = (byte) 0x01;
                    break;
                case 2:
                    arrayOfByte[0] = (byte) 0x02;
                    break;
            }
            */
            try {
                System.out.println("gatt character is" + characteristic + "");
                Thread.sleep(1000);
                //characteristic.setValue(arrayOfByte);
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
                System.out.println("status value" + status);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("bluetooth gatt is nulll");
        }

    }

    public boolean EnableAccel(int val) {
        if (mBluetoothGatt != null) {
            try {
                keyService = mBluetoothGatt.getService(ACCEL_SERVICE_UUID);
                Thread.sleep(1000);
                if (keyService == null) {
                    System.out.println("key service is null");
                    return false;
                } else {
                    // System.out.println("key service is not null");
                }
                BluetoothGattCharacteristic characteristic =
                        keyService.getCharacteristic(ACCEL_ENABLE_UUID);
                if (characteristic == null) {
                    System.out.println("ch is null");
                    return false;
                } else {
                    // System.out.println("ch is not null"+characteristic);
                }
                byte[] arrayOfByte = new byte[1];
                switch (val) {
                    case 1:
                        arrayOfByte[0] = (byte) 0x01;
                        break;
                    case 2:
                        arrayOfByte[0] = (byte) 0x00;
                        break;
                }
                try {

                    System.out.println("acc ch" + characteristic);
                    characteristic.setValue(arrayOfByte);
                    statuss = mBluetoothGatt.writeCharacteristic(characteristic);
                    System.out.println("acc status" + statuss);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        else{
            System.out.println("bluetooth gatt null");
        }
        return statuss;
    }

    public void getXYZ() {
        if (statuss) {
            enableX = keyService.getCharacteristic(X_ENABLE_UUID);
            if (enableX == null) {
                System.out.println("x char not found");
                return;
            } else {
                System.out.println("read characteristic");
                BluetoothGattDescriptor gatDescx = enableX.getDescriptor(X_ENABLE_UUID_DESC);
                if (gatDescx == null) {
                    System.out.println("x descriptor not found");
                    return;
                }
                try {

                    mBluetoothGatt.setCharacteristicNotification(enableX, true);
                    Thread.sleep(500);
                    gatDescx.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    boolean value = mBluetoothGatt.writeDescriptor(gatDescx);
                    System.out.println("return value of x" + value);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            enableY = keyService.getCharacteristic(Y_ENABLE_UUID);
            if (enableY == null) {
                System.out.println("y char not found");
                return;
            } else {
                System.out.println("read characteristic");
                BluetoothGattDescriptor gatDescy = enableY.getDescriptor(X_ENABLE_UUID_DESC);
                if (gatDescy == null) {
                    System.out.println("y descriptor not found");
                    return;
                }
                try {
                    mBluetoothGatt.setCharacteristicNotification(enableY, true);
                    Thread.sleep(500);
                    gatDescy.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    boolean valuey = mBluetoothGatt.writeDescriptor(gatDescy);
                    System.out.println("return value of y" + valuey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            enableZ = keyService.getCharacteristic(Z_ENABLE_UUID);
            if (enableZ == null) {
                System.out.println("z char not found");
                return;
            } else {
                System.out.println("read characteristic");
                BluetoothGattDescriptor gatdescz = enableZ.getDescriptor(X_ENABLE_UUID_DESC);
                if (gatdescz == null) {
                    System.out.println("z descriptor not found");
                    return;
                }
                try {
                    mBluetoothGatt.setCharacteristicNotification(enableZ, true);
                    Thread.sleep(500);
                    gatdescz.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    boolean valuez = mBluetoothGatt.writeDescriptor(gatdescz);
                    System.out.println("return value of z" + valuez);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }


        }
    }

}
