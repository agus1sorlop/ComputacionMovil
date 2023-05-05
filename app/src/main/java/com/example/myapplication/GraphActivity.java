package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private TextView textView;

    // Gráfica para mostrar los datos completos
    private GraphView grafica;

    // Tabla que mostrará los datos por etapa
    TableLayout tableLayout;

    // Fichero del que se recogen los datos
    private static final String FILENAME="circulos.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        textView = findViewById(R.id.textView2);
        grafica = findViewById(R.id.grafica);
        String jsonString = null;
        try {
            jsonString = StorageHelper.readStringFromFile(FILENAME, this);
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading file: ", e);
        }
        if (jsonString != null) {
            // Creamos un array de 5 elementos inicializados a 0
            // Cada elemento corresponde a un nivel de señal
            int[] array = new int[5];
            for (int i = 0; i < array.length; i++) {
                array[i] = 0;
            }
            try {
                System.out.println(jsonString);
                JSONObject jsonObject = new JSONObject(jsonString);
                System.out.println(jsonObject.get("circulos"));
                JSONArray jsonArray = new JSONArray(jsonObject.get("circulos").toString());
                int length = jsonArray.length();
                List<DataPoint> dataPoints = new ArrayList<>(length);
                // Recorremos los datos de todos los círculos del recorrido
                for (int i = 0; i < length; i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    System.out.println(jsonObject);
                    // Se incrementa el elemento  del nivel de la señal correspondiente
                    int grado = jsonObject.getInt("grade");
                    array[grado]++;
                }
                // Se crea el gráfico de barras con los resultados obtenidos
                BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[]{
                        new DataPoint(0,array[0]),
                        new DataPoint(1,array[1]),
                        new DataPoint(2,array[2]),
                        new DataPoint(3,array[3]),
                        new DataPoint(4,array[4])
                });
                series.setSpacing(50); // ajusta la separación entre las barras
                series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                    @Override
                    public int get(DataPoint data) {
                        return Color.rgb((int) data.getX() * 255 / 4, (int) data.getY() * 255 / 6, 100);
                    }
                });
                series.setDrawValuesOnTop(true);
                series.setValuesOnTopColor(Color.BLUE);
                grafica.addSeries(series);
            } catch (JSONException e) {
                Log.e("MainActivity", "Error parsing JSON: ", e);
            }
        } else {
            textView.setText("Fichero vacio");
        }
    }

    public void onButtonPressed(View v){
        showLocationUpdates();
    }

    public void showLocationUpdates(){

            String jsonString = null;
            try {
                jsonString = StorageHelper.readStringFromFile(FILENAME, this);
            } catch (IOException e) {
                Log.e("MainActivity", "Error reading file: ", e);
            }
            if (jsonString != null) {
                try {
                    System.out.println(jsonString);
                    JSONObject jsonObject = new JSONObject(jsonString);
                    System.out.println(jsonObject.get("circulos"));
                    JSONArray jsonArray = new JSONArray(jsonObject.get("circulos").toString());
                    int length = jsonArray.length();
                    List<DataPoint> dataPoints = new ArrayList<>(length);
                    jsonObject = jsonArray.getJSONObject(length-1);
                    int etapas = jsonObject.getInt("etapa");
                    // Se inicializa la matriz que contendra los datos de la tabla a 0
                    int[][] array = new int[etapas][5];
                    for (int j=0; j < etapas; j++) {
                        for (int i = 0; i < array.length; i++) {
                            array[j][i] = 0;
                        }
                    }
                    // Se lee otra vez los datos del fichero con los datos
                    for (int i = 0; i < length; i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        System.out.println(jsonObject);
                        int grado = jsonObject.getInt("grade");
                        int etapa = jsonObject.getInt("etapa");
                        // Se aumenta el dato correspondiente
                        array[etapa-1][grado]++;
                    }
                    tableLayout = findViewById(R.id.tableLayout);

                    // Agrega una fila de encabezado
                    TableRow headerRow = new TableRow(this);
                    headerRow.setBackgroundColor(Color.LTGRAY);
                    headerRow.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    TextView headerTextView1 = new TextView(this);
                    headerTextView1.setText("Etapa");
                    headerTextView1.setPadding(8, 8, 8, 8);
                    headerTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    headerTextView1.setTypeface(headerTextView1.getTypeface(), Typeface.BOLD);
                    headerRow.addView(headerTextView1);

                    TextView headerTextView2 = new TextView(this);
                    headerTextView2.setText("Grado 0");
                    headerTextView2.setPadding(8, 8, 8, 8);
                    headerTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    headerTextView2.setTypeface(headerTextView2.getTypeface(), Typeface.BOLD);
                    headerRow.addView(headerTextView2);

                    TextView headerTextView3 = new TextView(this);
                    headerTextView3.setText("Grado 1");
                    headerTextView3.setPadding(8, 8, 8, 8);
                    headerTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    headerTextView3.setTypeface(headerTextView3.getTypeface(), Typeface.BOLD);
                    headerRow.addView(headerTextView3);

                    TextView headerTextView4 = new TextView(this);
                    headerTextView4.setText("Grado 2");
                    headerTextView4.setPadding(8, 8, 8, 8);
                    headerTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    headerTextView4.setTypeface(headerTextView4.getTypeface(), Typeface.BOLD);
                    headerRow.addView(headerTextView4);

                    TextView headerTextView5 = new TextView(this);
                    headerTextView5.setText("Grado 3");
                    headerTextView5.setPadding(8, 8, 8, 8);
                    headerTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    headerTextView5.setTypeface(headerTextView5.getTypeface(), Typeface.BOLD);
                    headerRow.addView(headerTextView5);

                    TextView headerTextView6 = new TextView(this);
                    headerTextView6.setText("Grado 4");
                    headerTextView6.setPadding(8, 8, 8, 8);
                    headerTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    headerTextView6.setTypeface(headerTextView6.getTypeface(), Typeface.BOLD);
                    headerRow.addView(headerTextView6);

                    tableLayout.addView(headerRow);

                    // Agrega filas de datos
                    for (int i = 1; i <= etapas; i++) {
                        TableRow dataRow = new TableRow(this);
                        dataRow.setLayoutParams(new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));

                        TextView dataTextView = new TextView(this);
                        dataTextView.setText(""+i);
                        dataTextView.setPadding(8, 8, 8, 8);
                        dataRow.addView(dataTextView);

                        TextView dataTextView1 = new TextView(this);
                        dataTextView1.setText(""+array[i-1][0] );
                        dataTextView1.setPadding(8, 8, 8, 8);
                        dataRow.addView(dataTextView1);

                        TextView dataTextView2 = new TextView(this);
                        dataTextView2.setText(""+array[i-1][1] );
                        dataTextView2.setPadding(8, 8, 8, 8);
                        dataRow.addView(dataTextView2);

                        TextView dataTextView3 = new TextView(this);
                        dataTextView3.setText(""+array[i-1][2] );
                        dataTextView3.setPadding(8, 8, 8, 8);
                        dataRow.addView(dataTextView3);

                        TextView dataTextView4 = new TextView(this);
                        dataTextView4.setText(""+array[i-1][3] );
                        dataTextView4.setPadding(8, 8, 8, 8);
                        dataRow.addView(dataTextView4);

                        TextView dataTextView5 = new TextView(this);
                        dataTextView5.setText(""+array[i-1][4] );
                        dataTextView5.setPadding(8, 8, 8, 8);
                        dataRow.addView(dataTextView5);

                        tableLayout.addView(dataRow);
                    }
                    // Ocultamos el gráfico anterior y el textview
                    textView.setVisibility(View.INVISIBLE);
                    grafica.setVisibility(View.INVISIBLE);

                } catch (JSONException e) {
                    Log.e("MainActivity", "Error parsing JSON: ", e);
                }
            } else {
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