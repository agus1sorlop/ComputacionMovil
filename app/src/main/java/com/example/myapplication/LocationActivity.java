package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;

public class LocationActivity extends AppCompatActivity {

    private TextView textView;

    private static final String FILENAME="circulos.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        textView = findViewById(R.id.textView2);
    }

    public void onButtonPressed(View v){
        showLocationUpdates();
    }

    public void showLocationUpdates(){
        String cadena = null;
        try {
            cadena = StorageHelper.readStringFromFile(FILENAME,this);
        } catch (IOException e) {
            Log.e("MainActivity","Error reading file: ",e);
        }
        if(cadena!=null){
            textView.setText(cadena);
        }else{
            textView.setText("Fichero vacio");
        }

    }

    /*@Override
    protected void onPause(){
        super.onPause();
        stopLocationUpdates();
    }
    Faltaria onResume!!!!!
    private void stopLocationUpdates(){
        client.removeLocationUpdates(callback);
    }
    */

}