package com.example.shreyus.myapp;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import helpers.MqttHelper;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationService extends IntentService {

    private MqttHelper mqttHelper;
    private FusedLocationProviderClient locationClient;
    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public LocationService() {
        super("LocationService");
    }

    public void onCreate(){
        super.onCreate();
        locationClient = getFusedLocationProviderClient(this);
        mqttHelper = new MqttHelper(getApplicationContext());
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        try {
            final String destLat = intent.getStringExtra("destinationLat");
            final String destLongi = intent.getStringExtra("destinationLongi");
            while(true) {
                Thread.sleep(10000);
                locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        double lat = location.getLatitude();
                        double longi = location.getLongitude();
                        if (location != null) {
                            mqttHelper.connect("Current", lat + "," + longi + "\n" + destLat + "," + destLongi);
                        } else {
                            mqttHelper.connect("Current", "oops, service works, but no location!");
                        }
                    }
                });
            }


        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }
}

