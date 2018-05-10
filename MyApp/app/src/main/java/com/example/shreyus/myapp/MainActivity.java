package com.example.shreyus.myapp;

import java.util.ArrayList;

//SMS import
import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MainActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    //Googlemap instant
    private GoogleMap mMap;

    //SMS instant
    private static final int MY_PERMISSION_REQUEST_SEND_SMS = 0;
    Button sendBtn;
    EditText txtphoneNo;
    EditText txtMessage;
    String phoneNum1 = "5556",phoneNum2 = "5554", phoneNum3 = "5556";//vm1 5554, vm2 5556
    String message = "Help!";
    boolean boolSend1 = false, boolSend2 = false, boolSend3 = false;
    //private EditText num;
    //private EditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GoogleMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //SMS
        Button sendContact1=(Button)findViewById(R.id.contact1);//click button1 action:
        Button sendContact2=(Button)findViewById(R.id.contact2);
        Button sendContact3=(Button)findViewById(R.id.contact3);
        //contact1.setOnClickListener(new ButtonClickListener());

        sendContact1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                boolSend1 = true;
                sendSMSMessage();
            }
        });
        sendContact2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                boolSend2 = true;
                sendSMSMessage();
            }
        });
        sendContact3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                boolSend3 = true;
                sendSMSMessage();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else if (mMap != null){
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    //SMS
    protected void sendSMSMessage() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_SEND_SMS);
        } else {
            SendTextMsg();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SendTextMsg();
                } else {
                    Toast.makeText(getApplicationContext(),"SMS faild, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void SendTextMsg() {
        SmsManager smsManager = SmsManager.getDefault();

        if(boolSend1){
            smsManager.sendTextMessage(phoneNum1, null, message, null, null);
            boolSend1 = false;
        }else if(boolSend2){
            smsManager.sendTextMessage(phoneNum2, null, message, null, null);
            boolSend2 = false;
        }else if(boolSend3){
            smsManager.sendTextMessage(phoneNum3, null, message, null, null);
            boolSend3 = false;
        }

        //Default
        //smsManager.sendTextMessage(phoneNum, null, message, null, null);

        Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, the nearest helping point is directing.",Toast.LENGTH_LONG).show();
    }
}