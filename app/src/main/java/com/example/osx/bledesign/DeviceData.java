package com.example.osx.bledesign;

import android.os.ParcelUuid;

/**
 * Created by osx on 07/11/15.
 */
public class DeviceData {
    public String address;
    public int volume;
    public int buzzer_volume;

    public int getAveragerssi() {
        return averagerssi;
    }

    public void setAveragerssi(int averagerssi) {
        this.averagerssi = averagerssi;
    }

    public int averagerssi;
    public int getBuzzer_volume() {
        return buzzer_volume;
    }

    public void setBuzzer_volume(int buzzer_volume) {
        this.buzzer_volume = buzzer_volume;
    }

    public String getTone_location() {
        return tone_location;
    }

    public void setTone_location(String tone_location) {
        this.tone_location = tone_location;
    }

    public String tone_location;
    public String distance;

    public int getEven_odd() {
        return even_odd;
    }

    public void setEven_odd(int even_odd) {
        this.even_odd = even_odd;
    }

    public int even_odd;
    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int battery;

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public byte[] image;

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanReadData() {
        return scanReadData;
    }

    public void setScanReadData(byte[] scanReadData) {
        this.scanReadData = scanReadData;
    }

    public ParcelUuid[] getUuids() {
        return uuids;
    }

    public void setUuids(ParcelUuid[] uuids) {
        this.uuids = uuids;
    }

    public String name;

    public int bondState;

    public int rssi;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public byte[] scanReadData;

    public ParcelUuid[] uuids;

    private double latitude,longitude;
}
