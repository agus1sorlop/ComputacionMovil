package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient client;
    private LocationCallback callback;

    List<LatLng> locationList = new ArrayList<>();
    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(this);
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                //esto es para mostrar los colores segun la cantidad de cobertura que haya
                int[] colors = {Color.GREEN, Color.YELLOW, Color.RED};
                GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

                //para saber la cantidad de cobertura que tenemos
                float accuracy = location.getAccuracy();
                //para medirla entre 0 y 1 y convertirla a un color Gradient
                float value = Math.min(accuracy / 100.0f, 1.0f);
                int colorIndex = (int) (value * (colors.length - 1));
                int color = colors[colorIndex];
                //añadimos el color para pasarlo al circulo
                gradient.setColor(color);






                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(location.getLatitude(), location.getLongitude()))
                        .radius(accuracy)
                        .strokeWidth(3)
                        .strokeColor(Color.BLACK) // Color oscuro
                        .fillColor(color); // Utiliza el color seleccionado del gradiente
                mMap.clear(); // Limpiamos cualquier círculo anterior
                mMap.addCircle(circleOptions);

               //cogemos la ubicacion actual
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                String text = location.getLatitude() + " " + location.getLongitude();
               // LatLng murcia = new LatLng(location.getLatitude(), location.getLongitude());

                //para unir un punto con el otro (simplemente por probar)
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(locationList)
                        .color(Color.GRAY)
                        .width(5);
                mMap.addPolyline(polylineOptions);

                //añadimos a la lista de posiciones
                locationList.add(latLng);
                //mostramos el punto actual
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

     showPosition();

    }

    private void showPosition(){
        showLocationUpdates();
    }

    public void showLocationUpdates(){
        LocationRequest locationRequest = new LocationRequest.Builder(2000).setMinUpdateIntervalMillis(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 0);
            return;
        }
        client.requestLocationUpdates(locationRequest, callback, null);
    }

    @Override
    protected void onPause(){
        super.onPause();

        // Cancela el contador
        countDownTimer.cancel();
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
                    showLocationUpdates();
                }else{
                    Toast.makeText(this,"Need permission to work", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

}