package com.example.osx.bledesign;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by osx on 04/01/16.
 */
public class LastKnownLocation extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {
    View view=null;
    SupportMapFragment mapfragnent;
    GoogleMap gmap;
    public static final int TX_DEFAULT_VALUE=-59;
    LocationRequest mLocationRequest;

    Location mCurrentLocation;

    GoogleApiClient mGoogleApiClient;
    static boolean  flag = false;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (!Utils.isGooglePlayServicesAvailable(getActivity())) {
            Utils.ShowSnackBar(getActivity(),getString(R.string.play_services_error));
            flag = true;
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try{
            view=inflater.inflate(R.layout.fragment_mapview, null);
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
            mapfragnent = new SupportMapFragment();
            getFragmentManager().beginTransaction().replace(R.id.map_container, mapfragnent).commit();
            mapfragnent.getMapAsync(this);

            getActivity().setTitle(getString(R.string.last_known));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("google api connected");
        if(Utils.CheckLocationPermissions(getActivity())){
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
        if(mCurrentLocation!=null && flag==false){
            updateUI();
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
        updateUI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap=googleMap;
        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gmap.setBuildingsEnabled(true);
        gmap.getUiSettings().setZoomControlsEnabled(false);
        gmap.getUiSettings().setMyLocationButtonEnabled(true);
        gmap.getUiSettings().setMapToolbarEnabled(false);

        gmap.setIndoorEnabled(true);
        gmap.setMyLocationEnabled(true);
        gmap.getUiSettings().setIndoorLevelPickerEnabled(true);
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
        flag=true;
        if (null != mCurrentLocation && mGoogleApiClient.isConnected()) {
            LatLng latlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (gmap != null) {
                gmap.clear();
                gmap.addMarker(new MarkerOptions().position(latlng).title(latlng.latitude + "," + latlng.longitude));
                gmap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                gmap.animateCamera(CameraUpdateFactory.zoomTo(12));
                gmap.getUiSettings().setIndoorLevelPickerEnabled(true);
                gmap.setIndoorEnabled(true);
                gmap.addCircle(new CircleOptions()
                        .center(new LatLng(latlng.latitude, latlng.longitude)).radius(200).strokeColor(Color.parseColor("#ff0000")).strokeWidth(5)
                        .fillColor(ContextCompat.getColor(getActivity(), android.R.color.transparent)));

            }
        } else {
            if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
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
                    gmap.addCircle(new CircleOptions()
                            .center(new LatLng(latlng.latitude, latlng.longitude)).radius(200).strokeColor(Color.parseColor("#ff0000")).strokeWidth(5)
                            .fillColor(ContextCompat.getColor(getActivity(), android.R.color.transparent)));
                }
                else {
                    Utils.ShowSnackBar(getActivity(), "Unable to fetch location");
                }

            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        flag=false;
        stopLocationUpdates();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        System.out.println("inside on prepare optionsmenu addtracker");
        menu.clear();
    }
}
