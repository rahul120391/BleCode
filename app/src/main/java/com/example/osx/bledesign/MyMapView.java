package com.example.osx.bledesign;

import android.content.ContentValues;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by osx on 23/12/15.
 */
public class MyMapView extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {
    View view = null;
    SupportMapFragment mapfragnent;
    GoogleMap gmap;
    public static final int TX_DEFAULT_VALUE = -59;
    LocationRequest mLocationRequest;

    Location mCurrentLocation;

    GoogleApiClient mGoogleApiClient;
    boolean flag = false;
    ArrayList<DeviceData> data;

    ArrayList<Marker> markerlist = new ArrayList<>();
    private static boolean flagg = false;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isGooglePlayServicesAvailable(getActivity())) {
            Utils.ShowSnackBar(getActivity(), getString(R.string.play_services_error));
            flag = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_mapview, null);
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
            mapfragnent = new SupportMapFragment();
            getFragmentManager().beginTransaction().replace(R.id.map_container, mapfragnent).commit();
            mapfragnent.getMapAsync(this);
            Mydatabase mydatabase = new Mydatabase(getActivity());
            data = mydatabase.getDataFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        flagg = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("google api connected");
        if (Utils.CheckLocationPermissions(getActivity())) {
            System.out.println("start updates");
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("inside location change");
        mCurrentLocation = location;
        if (mCurrentLocation != null) {
            //updateUI();
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLoaded() {
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                double latitude = data.get(i).getLatitude();
                double longitude = data.get(i).getLongitude();
                if (latitude != 0.0 && longitude != 0.0) {
                    LatLng latlng = new LatLng(latitude, longitude);
                    Marker marker = gmap.addMarker(new MarkerOptions().position(latlng).title(data.get(i).getAddress()));
                    markerlist.add(marker);
                    gmap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    gmap.animateCamera(CameraUpdateFactory.zoomTo(12));
                    gmap.getUiSettings().setIndoorLevelPickerEnabled(true);
                    gmap.setIndoorEnabled(true);
                }
            }
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gmap.setBuildingsEnabled(true);
        gmap.getUiSettings().setZoomControlsEnabled(false);
        gmap.getUiSettings().setMyLocationButtonEnabled(true);
        gmap.getUiSettings().setMapToolbarEnabled(false);
        gmap.setIndoorEnabled(true);
        gmap.setMyLocationEnabled(true);
        gmap.getUiSettings().setIndoorLevelPickerEnabled(true);
        LatLng latlng = new LatLng(30.7088499, 76.7019075);
        gmap.addMarker(new MarkerOptions().position(latlng).title("54:4A:16:5E:1B:A7"));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            if (mGoogleApiClient.isConnected()) {

            }
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
                System.out.println("disconnected successfully");
                mGoogleApiClient.disconnect();
            }

        }

    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null) {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void updateUI() {
        System.out.println("updated ui");
        if (null != mCurrentLocation && mGoogleApiClient.isConnected()) {
            LatLng latlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (gmap != null) {
                gmap.clear();
                gmap.addMarker(new MarkerOptions().position(latlng).title(latlng.latitude + "," + latlng.longitude));
                gmap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                gmap.animateCamera(CameraUpdateFactory.zoomTo(12));
                gmap.getUiSettings().setIndoorLevelPickerEnabled(true);
                gmap.setIndoorEnabled(true);

            }
        } else {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            if (mCurrentLocation != null && mGoogleApiClient.isConnected()) {
                LatLng latlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                if (gmap != null) {
                    gmap.clear();
                    gmap.addMarker(new MarkerOptions().position(latlng).title(latlng.latitude + "," + latlng.longitude));
                    gmap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    gmap.animateCamera(CameraUpdateFactory.zoomTo(12));
                    gmap.getUiSettings().setIndoorLevelPickerEnabled(true);
                    gmap.setIndoorEnabled(true);
                } else {
                    Utils.ShowSnackBar(getActivity(), "Unable to fetch location");
                }

            }
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        stopLocationUpdates();
    }


    public void onEventMainThread(final List<DeviceData> list) {
        if (list.size() > 0) {
            gmap.clear();
            for (int x = 0; x < markerlist.size(); x++) {
                markerlist.get(x).remove();
            }
            for (int i = 0; i < list.size(); i++) {
                int rssi = list.get(i).getRssi();
                double val = Utils.calculateDistance(-59, rssi);
                BigDecimal decimal = new BigDecimal(val);
                double dis = decimal.doubleValue();
                String address = list.get(i).getAddress();
                if (null != mCurrentLocation && mGoogleApiClient.isConnected()) {
                    final double latRadian = Math.toRadians(mCurrentLocation.getLatitude());

                    final double degLatKm = 110.574235;
                    final double degLongKm = 110.572833 * Math.cos(latRadian);
                    final double latitude = dis / 1000.0 / degLatKm;
                    final double longitude = dis / 1000.0 /
                            degLongKm;
                    System.out.println("current location latitude" + mCurrentLocation.getLatitude());
                    System.out.println("current location longitude" + mCurrentLocation.getLongitude());
                    final double minLat = mCurrentLocation.getLatitude() - latitude;
                    final double minLong = mCurrentLocation.getLongitude() - longitude;
                    System.out.println("new latitude" + minLat);
                    System.out.println("new longitude" + minLong);
                    Mydatabase datatbase = new Mydatabase(getContext());
                    if (datatbase.CheckIfRecordExists(address)) {
                        System.out.println("exists already");
                        ContentValues values = new ContentValues();
                        values.put(Mydatabase.LATI_TUDEE, minLat);
                        values.put(Mydatabase.LONGI_TUDEE, minLong);
                        values.put(Mydatabase.MAC_ADDRESS, address);
                        datatbase.UpdateTableData(values);
                    } else {
                        System.out.println("exists not");
                        ContentValues values = new ContentValues();
                        values.put(Mydatabase.LATI_TUDEE, minLat);
                        values.put(Mydatabase.LONGI_TUDEE, minLong);
                        values.put(Mydatabase.MAC_ADDRESS, address);
                        datatbase.AddTracker(values);
                    }
                    LatLng latlng = new LatLng(minLat, minLong);
                    Marker marker = gmap.addMarker(new MarkerOptions().position(latlng).title(address));
                    markerlist.add(marker);

                    if (flagg == false) {
                        gmap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                        gmap.animateCamera(CameraUpdateFactory.zoomTo(12));
                        flagg = true;
                    }

                    //gmap.getUiSettings().setIndoorLevelPickerEnabled(true);
                    gmap.setIndoorEnabled(true);
                }
            }
        }
    }

    public double calculateDistance(int txPower, double rssi) {
        return (Math.pow(10d, ((double) txPower - rssi) / (10 * 2))) / 32;
    }
}