package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.Gson;
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient client;
    private LocationCallback callback;

    private TelephonyManager telephonyManager;
    private static final String FILENAME = "circulos.json";

    private JsonObject res;
    private JsonArray circulosArray;
    private JsonArray antenasArray;
    private JsonObject ultimaAntena;
    int[] colors = {Color.rgb(0, 100, 0), Color.GREEN, Color.YELLOW, Color.rgb(255, 165, 0), Color.RED};
    List<LatLng> locationList = new ArrayList<>();
    private CountDownTimer countDownTimer;
    //Guarda la posicion del ultimo circulo colocado
    private Location ultimoCirculo = null;
    private double radio = 6;
    private int etapa;
    private int circulosEtapa;
    private int grosorCirculo;
    private boolean cambioAntena;

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
        res = new JsonObject();
        circulosArray = new JsonArray();
        antenasArray = new JsonArray();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        etapa = 1;
        ultimaAntena=null;
        circulosEtapa = 0;
        grosorCirculo = 3;
        cambioAntena=false;
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                System.out.println("callback");
                boolean colocar = false;
                if (location != null && (ultimoCirculo == null || location.distanceTo(ultimoCirculo) >= radio * 2)) {
                    colocar = true;
                    if (ultimoCirculo != null) {    //Este codigo es para dibujar los circulos pegados
                        double[] vector = {location.getLatitude() - ultimoCirculo.getLatitude(), location.getLongitude() - ultimoCirculo.getLongitude()};
                        double distancia = location.distanceTo(ultimoCirculo); // Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2));
                        location.setLatitude(ultimoCirculo.getLatitude() + vector[0] * 2 * radio / distancia);
                        location.setLongitude(ultimoCirculo.getLongitude() + vector[1] * 2 * radio / distancia);
                    }
                    ultimoCirculo = location;
                }
                if (colocar) {
                    int colorIndex = getLevel();
                    int color = colors[4 - colorIndex];
                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(location.getLatitude(), location.getLongitude()))
                            .radius(radio)
                            .strokeWidth(grosorCirculo)
                            .strokeColor(Color.BLACK) // Color oscuro
                            .fillColor(color); // Utiliza el color seleccionado del gradiente
                    circulosEtapa++;
                    // Limpiamos el mapa de circulos
                    //mMap.clear(); // Limpiamos cualquier círculo anterior
                    mMap.addCircle(circleOptions);
                    // Añadir datos al fichero
                    Circulo circulo = new Circulo(location.toString(), colorIndex, etapa, circulosEtapa);
                    añadirCirculo(circulo);
                    // añadimos minicirculo si hay cambio de antena
                    if(cambioAntena) {
                        circleOptions = new CircleOptions()
                                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                                .radius(radio / 3)
                                .strokeWidth(grosorCirculo)
                                .strokeColor(Color.BLACK) // Color oscuro
                                .fillColor(Color.BLACK);
                        mMap.addCircle(circleOptions);
                        cambioAntena=false;
                    }
                }
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
                if(colocar&&circulosEtapa==1) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                }
                try {
                    obtenerUbicacionAntena();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

    private void añadirCirculo(Circulo circulo) {
        circulosArray.add(circulo.toJson());
        res.add("circulos", circulosArray);
        try {
            StorageHelper.saveStringToFile(FILENAME, res.toString(), this);
        } catch (IOException e) {
            Log.e("MainActivity", "Error saving file: ", e);
        }
    }

    private void showPosition() {
        showLocationUpdates();
    }

    public void showLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(2000).setMinUpdateIntervalMillis(2000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 0);
            return;
        }
        client.requestLocationUpdates(locationRequest, callback, null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Cancela el contador
        countDownTimer.cancel();
        stopLocationUpdates();

    }

    private void stopLocationUpdates() {
        client.removeLocationUpdates(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showLocationUpdates();
                } else {
                    Toast.makeText(this, "Need permission to work", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public int getLevel() {
        StringBuilder text = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            }, 0);

            return 0;
        }
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        int signal = 0;
        for (CellInfo info : cellInfoList) {
            if (info instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) info;
                int level = cellInfoLte.getCellSignalStrength().getLevel();
                if (signal < level)
                    signal = level;
            }
        }
        return signal;
    }

    public void nuevaEtapa(View v) {
        if (circulosEtapa > 0) {
            etapa++;
            circulosEtapa = 0;
            if (grosorCirculo == 3) {
                grosorCirculo = 6;
            } else {
                grosorCirculo = 3;
            }
        }
    }

    private void obtenerUbicacionAntena() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.INTERNET
            }, 0);
            return;
        }
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        System.out.println("antenaaa");
        if(cellLocation==null)
            return;
        int mcc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(0, 3));
        int mnc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(3));
        int lac = cellLocation.getLac();
        int cellId = cellLocation.getCid();
        String url = "https://data.mongodb-api.com/app/data-fcpji/endpoint/db/getcellinfo?mcc=" + mcc + "&mnc=" + mnc + "&area=" + lac + "&cellid=" + cellId;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            System.out.println("siiii");
            // Operaciones http
        } else {
            System.out.println("nooo");
            // Mostrar errores
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Procesar la respuesta del servidor aquí
                        System.out.println("eeeee: " + response);
                        if(response==null||response.equals("null")){
                            return;
                        }
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                        if(jsonObject.equals(ultimaAntena))
                            return;
                        /*for(JsonElement json: antenasArray)
                            if(json.equals((JsonElement) jsonObject))
                                return;
                        antenasArray.add(jsonObject);
                         */
                        // coloreamos nueva antena
                        double latitude = jsonObject.get("lat").getAsDouble();
                        double longitude = jsonObject.get("lon").getAsDouble();
                        CircleOptions circleOptions = new CircleOptions()
                                .center(new LatLng(latitude, longitude))
                                .radius(radio)
                                .strokeWidth(grosorCirculo)
                                .strokeColor(Color.BLACK)
                                .fillColor(Color.MAGENTA);

                        mMap.addCircle(circleOptions);
                        // cambiamos color antena de la antigua antena
                        if(ultimaAntena!=null) {
                            latitude = ultimaAntena.get("lat").getAsDouble();
                            longitude = ultimaAntena.get("lon").getAsDouble();
                            circleOptions = new CircleOptions()
                                    .center(new LatLng(latitude, longitude))
                                    .radius(radio)
                                    .strokeWidth(grosorCirculo)
                                    .strokeColor(Color.BLACK)
                                    .fillColor(Color.BLUE);

                            mMap.addCircle(circleOptions);
                            cambioAntena=true;
                        }
                        ultimaAntena = jsonObject;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", "Error en la solicitud: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error en la solicitud: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
        requestQueue.add(stringRequest);

    }

}





