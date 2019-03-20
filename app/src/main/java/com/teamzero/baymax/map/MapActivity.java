package com.teamzero.baymax.map;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.teamzero.baymax.initial.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean locationPermission = false;
    private static final int locationRequestCode = 1234;
    private static int ERROR_DIALOG_REQUEST = 9001;
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 20;
    private Location currentLocation;
    private MarkerOptions place;
    private Polyline currentPolyline;
    String lat, lng;
    private SupportMapFragment mapFragment;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        showRequestedPlace(lat, lng);
        if (locationPermission) {
            getDeviceLoaction(true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gMap.setMyLocationEnabled(true);
            View mapView = mapFragment.getView();
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            locationButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        lng = intent.getStringExtra("lng");
        lat = intent.getStringExtra("lat");
        Toast.makeText(this, lat + "  " + lng, Toast.LENGTH_SHORT).show();
        System.out.println(lat + " " + lng);
        if (isServicesOk()) {
            getLocationPermission();
            ImageButton own = findViewById(R.id.own);
            own.setOnClickListener(e -> {
                getDeviceLoaction(false);
            });
            ImageButton req = findViewById(R.id.reqloc);
            req.setOnClickListener(e -> {
                LatLng place = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                showLocation(place, DEFAULT_ZOOM);
            });
            Button go = (Button) findViewById(R.id.go);
            go.setOnClickListener(e -> {
                if (currentLocation != null) {
                    StringBuilder sb = getDirection(lng, lat);
                    System.out.println(sb.toString());
                    new FetchURL(MapActivity.this).execute(sb.toString(), "driving");
                } else
                    Toast.makeText(this, "Current location is not available", Toast.LENGTH_LONG).show();
            });

        } else this.finish();
    }

    private void showRequestedPlace(String lat, String lng) {
        Toast.makeText(this, "Showing Requested Place", Toast.LENGTH_SHORT).show();
        LatLng place = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        showLocation(place, DEFAULT_ZOOM);
        gMap.addMarker(new MarkerOptions().position(place).title(" "));
    }

    private StringBuilder getDirection(String longitude, String latitude) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
        sb.append("&destination=" + latitude + "," + longitude);
        sb.append("&mode=driving");
        sb.append("&key=" + "AIzaSyAs0ZB1NENJSA4RYzFhovA1PO6dejpCyJo");
        return sb;


    }

    private void getDeviceLoaction(boolean flag) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (locationPermission) {
                Task location = fusedLocationProviderClient.getLastLocation();
                Toast.makeText(this, "Getting Device Location", Toast.LENGTH_SHORT).show();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            currentLocation = (Location) task.getResult();
                            if(!flag)
                            showLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }
                    }
                });
            }
        } catch (SecurityException e) {

        }
    }

    private void showLocation(LatLng latLng, float zoom) {
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermission = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        locationRequestCode);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    locationRequestCode);
        }
    }

    private void initMap() {
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermission = false;
        switch (requestCode) {
            case locationRequestCode: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermission = false;
                            return;
                        }
                    }
                    locationPermission = true;
                    initMap();
                }
            }
        }
    }


    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = gMap.addPolyline((PolylineOptions) values[0]);
    }


    public boolean isServicesOk() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Sorry", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
