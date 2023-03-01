package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    private TextView textView;
    private LocationCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        client = LocationServices.getFusedLocationProviderClient(this);
        textView = findViewById(R.id.textView2);
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                System.out.print("holaaaaa");
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                textView = findViewById(R.id.textView2);
                String text = location.getLatitude() + " " + location.getLongitude();
                textView.setText(text);
            }
        };
    }

    public void onButtonPressed(View v){
        //showLastLocation();
        showLocationUpdates();
    }

    public void showLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 0);

            return;
        }
        client.getLastLocation().addOnSuccessListener(this,new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                String text = location.getLatitude() + " " + location.getLongitude();
                textView.setText(text);
                System.out.print("jee");
            }
        });
    }

    public void showLocationUpdates(){
        LocationRequest locationRequest = new LocationRequest.Builder(2000).setMinUpdateIntervalMillis(1000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 0);
            return;
        }
        client.requestLocationUpdates(locationRequest, callback, null);
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates(){
        client.removeLocationUpdates(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch(requestCode){
            case 0:{
                if(grantResults.length >0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    showLastLocation();
                }else{
                    Toast.makeText(this,"Need permission to work", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}