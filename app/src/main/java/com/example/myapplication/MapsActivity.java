package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
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
    // Ficheros donde se guardaran los datos de los circulos y las antenas
    private static final String FILENAME = "circulos.json";
    private static final String FILENAME2 = "antenas.json";
    private JsonObject res;
    // Arrays que guardaran los circulos y antenas pintados
    private JsonArray circulosArray;
    private JsonArray antenasArray;
    //Guarda la posicion del ultimo circulo colocado
    private Location ultimoCirculo = null;
    private JsonObject ultimaAntena;
    private boolean cambioAntena;
    // Escala de colores que usaremos dependiendo el nivel de señal
    int[] colors = {Color.rgb(0, 100, 0), Color.GREEN, Color.YELLOW, Color.rgb(255, 165, 0), Color.RED};
    List<LatLng> locationList = new ArrayList<>();
    //private CountDownTimer countDownTimer;
    private double radio = 6;
    private int grosorCirculo;
    private int etapa;
    private int circulosEtapa;
    private String datosCelda = "";
    // Nos permitiran tener la pantalla encendida sin necesidad de interaccion
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    private boolean tresge= false;

    private boolean cuatroge= true;
    int colorIndex;
    double nivelMedio ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(this);
        res = new JsonObject();
        circulosArray = new JsonArray();
        antenasArray = new JsonArray();
        ultimaAntena=null;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        etapa = 1;
        circulosEtapa = 0;
        grosorCirculo = 3;
        cambioAntena=false;

        // Inicializamos estas variables para mantener la pantalla encendida
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WAKE_LOCK
            }, 0);
            return;
        }
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MiApp::MyWakeLockTag");
        wakeLock.acquire();
        // Esto se ejecutará siempre que llegue una nueva ubicación
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
                // Si la distancia es suficiente se colocara un nuevo circulo
                if (colocar) {

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

                    Circulo circulo = new Circulo(location.toString(), colorIndex, nivelMedio, datosCelda, etapa, circulosEtapa);
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
                locationList.add(latLng);
                //mostramos el punto actual
                if(colocar&&circulosEtapa==1) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                }
                try {
                    // Miramos la ubicacion de la antena actual
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

    private void añadirAntena(JsonObject antena) {
        if(antenasArray.contains(antena)){
            return;
        }
        antenasArray.add(antena);
        try {
            StorageHelper.saveStringToFile(FILENAME2, antena.toString(), this);
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
        //countDownTimer.cancel();
        stopLocationUpdates();
        wakeLock.release();
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
        datosCelda = "";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            }, 0);

            return 0;
        }
        // Vamos a obtener los datos de la celda a la que estamos conectados y el nivel de señal
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        int signal = 0;
        for (CellInfo info : cellInfoList) {
            if (info instanceof CellInfoLte && cuatroge) {
                CellInfoLte cellInfoLte = (CellInfoLte) info;
                CellIdentityLte id = cellInfoLte.getCellIdentity();
                // Obtener detalles de la celda LTE
                StringBuilder text = new StringBuilder();
                text.append("LTE ID:{cid: ").append(id.getCi());
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                }else{
                    text.append(" mcc: ").append(id.getMccString());
                    text.append(" mnc: ").append(id.getMncString());
                }
                text.append(" tac: ").append(id.getTac());
                text.append("} Level: ").append(cellInfoLte.getCellSignalStrength().getLevel()).append("\n");
                int level = cellInfoLte.getCellSignalStrength().getLevel();
                // Cogemos el valor mas alto de señal
                if (signal < level) {
                    signal = level;
                    datosCelda = text.toString();
                }

                System.out.println("OPCION 4G");
            } else if (info instanceof CellInfoWcdma && tresge) {

                System.out.println("OPCION 3G");
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) info;
                CellIdentityWcdma id = cellInfoWcdma.getCellIdentity();
                // Obtener detalles de la celda WCDMA
                StringBuilder text = new StringBuilder();
                text.append("WCDMA ID:{cid: ").append(id.getCid());
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                }else{
                    text.append(" mcc: ").append(id.getMccString());
                    text.append(" mnc: ").append(id.getMncString());
                }
                text.append(" lac: ").append(id.getLac());
                text.append("} Level: ").append(cellInfoWcdma.getCellSignalStrength().getLevel()).append("\n");
                int level = cellInfoWcdma.getCellSignalStrength().getLevel();
                if (signal < level) {
                    signal = level;
                    datosCelda = text.toString();
                }
            }
        }
        return signal;
    }

<<<<<<< HEAD

=======
    // Vamos a obtener el nivel medio de señal que tienen las celdas que hay en nuestra ubicación
>>>>>>> master
    public double getHalfLevel() {
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
        int celdas = 0;
        for (CellInfo info : cellInfoList) {
            if (info instanceof CellInfoLte && cuatroge) {
                CellInfoLte cellInfoLte = (CellInfoLte) info;
                int level = cellInfoLte.getCellSignalStrength().getLevel();
                signal += level;
                celdas++;
            }else if( info instanceof CellInfoWcdma && tresge){
                CellInfoWcdma cellInfoWcdma= (CellInfoWcdma) info;
                int level = cellInfoWcdma.getCellSignalStrength().getLevel();
                signal += level;
                celdas++;
            }
        }
        if(celdas==0)return 0;
        return signal/celdas;
    }

    // Esto se ejecutara cuando pulsemos el boton de nueva etapa
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

    public void obtener4G(View v) {

        System.out.println("Estás usando 4G");
        cuatroge=true;
        tresge=false;
        colorIndex=getLevel();
        nivelMedio= getHalfLevel();
    }

    public void obtener3G(View v) {
        System.out.println("Estás usando 3G");
        cuatroge=false;
        tresge=true;
        colorIndex=getLevel();
        nivelMedio= getHalfLevel();
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

        int mcc;
        int mnc;
        int lac;
        int cellId;

        if (tresge) {
            GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

            System.out.println("antenaaa 3g");
            if (cellLocation == null)
                return;
            mcc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(0, 3));
            mnc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(3));
            lac = cellLocation.getLac();
            cellId = cellLocation.getCid();
        } else if (cuatroge) {

            System.out.println("antenaaa 4g");
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();

            if (cellInfoList == null || cellInfoList.isEmpty())
                return;

            CellInfoLte cellInfoLte = null;

            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte) {
                    cellInfoLte = (CellInfoLte) cellInfo;
                    break;
                }
            }

            if (cellInfoLte == null)
                return;

            CellIdentityLte cellLocation = cellInfoLte.getCellIdentity();

            System.out.println("antenaaa");
            if (cellLocation == null)
                return;
            mcc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(0, 3));
            mnc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(3));
            lac = cellLocation.getTac();
            cellId = cellLocation.getCi();
        } else {
            return;
<<<<<<< HEAD
        }
=======
        int mcc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(0, 3));
        int mnc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(3));
        int lac = cellLocation.getLac();
        int cellId = cellLocation.getCid();
        // Realizaremos una cosulta para obtener los datos de la antena a la que estamos conectados
>>>>>>> master
        String url = "https://data.mongodb-api.com/app/data-fcpji/endpoint/db/getcellinfo?mcc=" + mcc + "&mnc=" + mnc + "&area=" + lac + "&cellid=" + cellId;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // Aqui se realiza la consulta
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        // Tratamos la respuesta obtenida
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
                        añadirAntena(jsonObject);
                        if(jsonObject.equals(ultimaAntena))
                            return;
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





