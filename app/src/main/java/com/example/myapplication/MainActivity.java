package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openLocationActivity(View v){
        Intent intent = new Intent(this,LocationActivity.class);
        startActivity(intent);
    }

    public void openTelActivity(View v){
        Intent intent = new Intent(this,TelephonyActivity.class);
        startActivity(intent);
    }

    public void openMapActivity(View v){
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

}
