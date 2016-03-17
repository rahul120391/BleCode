package com.example.osx.bledesign;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Mydatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "data.db";
    public static final String TABLE_NAME = "Trackers";
    public static final String MAC_ADDRESS = "macaddress";
    public static final String TRACKER_NAME = "trackername";
    public static final String RSSI = "rssi";
    public static final String VOLUME_TYPE = "vol";
    public static final String DISTANCE = "distance";
    public static final String IMAGE_NAME = "image";
    public static final String BUZZER_TRACKER_VOLUME = "buzzervol";
    public static final String TONE_LOCATION = "tonelocation";
    public static final String LATI_TUDEE = "latitude";
    public static final String LONGI_TUDEE = "longitude";
    public static final int db_version = 1;
    public Mydatabase _database;
    SQLiteDatabase _db;
    Cursor cr;
    static Context _context;

    public Mydatabase(Context cnt) {
        super(cnt, DATABASE_NAME, null, db_version);
        _context = cnt;
        System.out.println("inside mydatabase");
    }

    public Mydatabase(Context context, String name, CursorFactory factory,
                      int version) {
        super(context, DATABASE_NAME, null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table Trackers (" + MAC_ADDRESS + " text not null unique, " + TRACKER_NAME + " text, " + RSSI + "  INTEGER, " + VOLUME_TYPE + "  INTEGER, " + BUZZER_TRACKER_VOLUME + " INTEGER, " + TONE_LOCATION + " text, " + DISTANCE + "  text, " + LATI_TUDEE + "  REAL," + LONGI_TUDEE + "  REAL," + IMAGE_NAME + " blob);";
        db.execSQL(sql);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d("inside", "on open");
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
            onCreate(db);
        }
    }

    public long AddTracker(ContentValues cnt) {
        _db = this.getWritableDatabase();
        long returnvalue = _db.insert(Mydatabase.TABLE_NAME, null, cnt);
        _db.close();
        return returnvalue;
    }

    public boolean CheckIfRecordExists(String macaddress) {
        _db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + MAC_ADDRESS + " = " + "'" + macaddress + "'";
        Cursor cursor = _db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        _db.close();
        return true;
    }

    public int UpdateTableData(ContentValues values) {
        _db = this.getWritableDatabase();
        String address = values.getAsString(MAC_ADDRESS);
        int value = _db.update(TABLE_NAME, values, MAC_ADDRESS + " = " + "'" + address + "'", null);
        _db.close();
        return value;
    }

    public ArrayList<DeviceData> getDataFromDatabase() {
        ArrayList<DeviceData> data = new ArrayList<>();
        String sql = "select * from " + TABLE_NAME;
        _db = this.getReadableDatabase();
        cr = _db.rawQuery(sql, null);
        if (cr != null) {
            if (cr.moveToFirst()) {
                do {


                    DeviceData dataa = new DeviceData();
                    if (!cr.isNull(1)) {
                        String tracker_name = cr.getString(1);
                        System.out.println("tracker name" + tracker_name);
                        dataa.setName(tracker_name);
                    }
                    if (!cr.isNull(3)) {
                        int volume = cr.getInt(3);
                        dataa.setVolume(volume);
                    }
                    if (!cr.isNull(5)) {
                        String location = cr.getString(5);
                        dataa.setTone_location(location);
                    }
                    if (!cr.isNull(7)) {
                        double lat = cr.getDouble(7);
                        dataa.setLatitude(lat);
                    }
                    if (!cr.isNull(8)) {
                        double lng = cr.getDouble(8);
                        dataa.setLongitude(lng);
                    }

                    if (!cr.isNull(4)) {
                        int buzzer_vol = cr.getInt(4);
                        dataa.setBuzzer_volume(buzzer_vol);
                    }
                    if (!cr.isNull(6)) {
                        String distance = cr.getString(6);
                        dataa.setDistance(distance);
                    }
                    int a=cr.getColumnIndex(Mydatabase.MAC_ADDRESS);
                    System.out.println("column index is"+a);
                    String mac_address = cr.getString(0);
                    System.out.println("mac_address" + mac_address);
                    dataa.setAddress(mac_address);
                    if (!cr.isNull(2)) {
                        int rssi = cr.getInt(2);
                        dataa.setRssi(rssi);
                        System.out.println("rssi value stored" + rssi);
                    }
                    if (!cr.isNull(9)) {
                        byte[] pic = cr.getBlob(9);
                        dataa.setImage(pic);
                    }


                    data.add(dataa);
                }
                while (cr.moveToNext());
            }
        }
        _db.close();
        return data;
    }

    public ArrayList<DeviceData> getrecord(String macaddress) {
        ArrayList<DeviceData> list = new ArrayList<>();
        _db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + MAC_ADDRESS + " = " + "'" + macaddress + "'";
        Cursor cursor = _db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {


                DeviceData dataa = new DeviceData();
                if (!cursor.isNull(1)) {
                    String tracker_name = cursor.getString(1);
                    dataa.setName(tracker_name);
                }
                if (!cursor.isNull(5)) {
                    String location = cursor.getString(5);
                    dataa.setTone_location(location);
                }

                if (!cursor.isNull(3)) {
                    int volume = cursor.getInt(3);
                    dataa.setVolume(volume);
                }


                if (!cursor.isNull(6)) {
                    String distance = cursor.getString(6);
                    dataa.setDistance(distance);
                }

                if (!cursor.isNull(4)) {
                    int buzzer_vol = cursor.getInt(4);
                    dataa.setBuzzer_volume(buzzer_vol);
                }

                if (!cursor.isNull(0)) {
                    String mac_address = cursor.getString(0);
                    dataa.setAddress(mac_address);
                }
                if (!cursor.isNull(2)) {
                    int rssi = cursor.getInt(2);
                    dataa.setRssi(rssi);
                }
                if (!cursor.isNull(9)) {
                    byte[] pic = cursor.getBlob(9);
                    dataa.setImage(pic);
                }

                list.add(dataa);
            }
            while (cursor.moveToNext());
        }
        _db.close();
        return list;
    }


    public DeviceData getlatlong(String macaddress) {
        DeviceData dataa = new DeviceData();
        _db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + MAC_ADDRESS + " = " + "'" + macaddress + "'";
        Cursor cursor = _db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                double latitude = cursor.getDouble(7);
                double longitude = cursor.getDouble(8);
                dataa.setLatitude(latitude);
                dataa.setLongitude(longitude);
            }
            while (cursor.moveToNext());
        }
        _db.close();
        return dataa;
    }


}
