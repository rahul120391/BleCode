package com.example.osx.bledesign;

import android.os.ParcelUuid;

public interface IRedBearServiceEventListener {
	void onDeviceFound(String deviceAddress, String name, int rssi,
					   int bondState, byte[] scanRecord, ParcelUuid[] uuids);

	void onDeviceRssiUpdate(String deviceAddress, int rssi, int state, int batterylevel);

	void onDeviceConnectStateChange(String deviceAddress, int state);

	void onDeviceReadValue(double[] value);
	
	void onDeviceCharacteristicFound();
}