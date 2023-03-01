package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class TelephonyActivity extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telephony);
        telephonyManager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        textView = findViewById(R.id.textView3);
    }

    public void onButtonPressed(View v){
        currentNetworwInfo();
    }

    public void currentNetworwInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String []{Manifest.permission.READ_PHONE_STATE}, 0);
            Log.d("Telephony", "request permission");
            return;
        }
        String text = "";
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        for (CellInfo cell : cellInfos) {
            if (cell instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cell;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    cellInfoLte.getCellIdentity().getMcc();
                } else {
                    text += cellInfoLte.getCellIdentity().getMccString() + "\n";
                    text += cellInfoLte.getCellIdentity().getMncString() + "\n";
                    text += cellInfoLte.getCellIdentity().getTac() + "\n";
                    text += cellInfoLte.getCellIdentity().getCi() + "\n";
                }
            }
        }

        //text += telephonyManager.getVoiceNetworkType() + "\n";
        //text += telephonyManager.getNetworkOperatorName() + "\n";
        //text += telephonyManager.getDataNetworkType() + "\n";
        textView.setText(text);
        Log.d("Telephony", "permission granted");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch(requestCode){
            case 0:{
                if(grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    currentNetworwInfo();
                }else{
                    Toast.makeText(this,"Need permission to work", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public void cellInfo(View v) {
        StringBuilder text = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            }, 0);

            return;
        }
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        text.append("Found ").append(cellInfoList.size()).append(" cells\b");
        for(CellInfo info : cellInfoList){
            if(info instanceof CellInfoLte){
                CellInfoLte cellInfoLte = (CellInfoLte) info;
                CellIdentityLte id = cellInfoLte.getCellIdentity();
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
            }
        }
        textView.setText(text);
    }
}