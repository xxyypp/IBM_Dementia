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
    String phoneNum = "123456789";
    String message = "Help!";
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
        //num=(EditText)findViewById(R.id.number);//get num
        //content=(EditText)findViewById(R.id.content);//get sms
        Button contact1=(Button)findViewById(R.id.contact1);//click button1 action:
        //contact1.setOnClickListener(new ButtonClickListener());

        contact1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                sendSMSMessage();
            }
        });

//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
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
    protected void sendSMSMessage(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED ){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                //do nothing
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSION_REQUEST_SEND_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNum, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent. Please be do not worry, helper's coming!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "SMS failed, try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }
/*
    private final class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            String number= "123456789";
            String content="Help!";
            SmsManager manager=SmsManager.getDefault();//Deploy android.telephony.SmsManager
            ArrayList<String> texts=manager.divideMessage(content);
            for(String text:texts){
                manager.sendTextMessage(number, null, text, null, null);
            }
            Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();//Toast->show success or not;
        }
    }*/

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/



}