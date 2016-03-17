package com.example.osx.bledesign;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by osx on 20/11/15.
 */
public class Utils {

    public static void ShowSnackBar(Context context, String message) {
        Snackbar bar = Snackbar.make(((Activity) context).findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        View view = bar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(ContextCompat.getColor(context, R.color.white));
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        bar.show();
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, System.currentTimeMillis() + "", null);
        return Uri.parse(path);
    }

    public static BluetoothDevice getbluetoothdevice(String address) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        return device;
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, (Activity) context, 0).show();
            return false;
        }
    }

    public static boolean CheckLocationPermissions(Context context) {
        String permission1 = "android.permission.ACCESS_COARSE_LOCATION";
        String permission2 = "android.permission.ACCESS_FINE_LOCATION";
        int res = context.checkCallingOrSelfPermission(permission1);
        int res1 = context.checkCallingPermission(permission2);
        return (res == PackageManager.PERMISSION_GRANTED) || (res1 == PackageManager.PERMISSION_GRANTED);
    }

    public static Bitmap getbitmap(byte[] image) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        return bitmap;
    }

    public static int CheckConnectionState(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connection_state = bluetoothManager.getConnectionState(Utils.getbluetoothdevice(address), BluetoothProfile.GATT);
        return connection_state;
    }

    public static byte[] getimage(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imagebyte = stream.toByteArray();
        return imagebyte;
    }

    public static byte[] getimage(Context context) {
        Drawable d = context.getResources().getDrawable(R.drawable.header);
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return bitmapdata;
    }


    public static ArrayList<HashMap<String, String>> listRingtones(Context context) {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor cursor = manager.getCursor();
        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String id=cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            HashMap<String, String> map = new HashMap<>();
            if (title != null) {
                map.put("title", title);
                map.put("uri", uri);
                map.put("id",id);
                list.add(map);
            }

        }
        return list;
    }

    public static double calculatedis(int rssi){
        double d=Math.pow(10, (-96 - rssi) / 10 * 2);
        return d;
    }

    protected static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0;
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy=(0.8229884)*Math.pow(ratio,6.6525179) + 0.1820634;
            return accuracy;
        }
    }
}
