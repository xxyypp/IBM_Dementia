package com.example.shreyus.myapp;

import java.util.ArrayList;

//Location import
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;

//SMS import
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnSuccessListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import helpers.MqttHelper;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    /********************************************** Pre-define ******************************************/

    //MQTT
    MqttHelper mqttHelper;
    TextView dataReceived;

    String PLACES_API_KEY = "AIzaSyBVGJYHClfBB8sMIkb1wNqJLqeLlYkcnzo";

    //Google maps
    private GoogleMap mMap;

    //Current Location
    private FusedLocationProviderClient locationClient;

    //JSON closest location
    ListView jsontxt;
    String url;
    String urljson;
    String destLat = "123";
    String destLongi = "456";

    //SMS
    private static final int MY_PERMISSION_REQUEST_SEND_SMS = 0;
    String phoneNum1 = "5556", phoneNum2 = "5554", phoneNum3 = "5556";//vm1 5554, vm2 5556
    String message = "Help!";
    boolean boolSend1 = false, boolSend2 = false, boolSend3 = false;
    //private EditText num;
    //private EditText content;
    /********************************************** End Pre-define ******************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MQTT
        dataReceived = (TextView)findViewById(R.id.dataReceived);
        startMqtt();

        //Current Location
        requestPermissions();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        final Button helpButton = findViewById(R.id.HELPbutton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions();
                    return;
                }
                locationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        double lat = location.getLatitude();
                        double longi = location.getLongitude();
                        if (location != null) {
                            TextView textView = findViewById(R.id.location);
                            url = "http://maps.google.com/maps?z=12&t=m&q=loc:" + lat + "+" + longi;
                            urljson = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + longi + "&rankby=distance&type=cafe&key=" + PLACES_API_KEY;
                            textView.setText(Double.toString(lat) + " , " + Double.toString(longi));
                            //Toast.makeText(getApplicationContext(), "Current location:\n" + lat + "," + longi, Toast.LENGTH_LONG).show();
                            sendSMSMessage();
                        } else {
                            Toast.makeText(getApplicationContext(), "Cannot get GPS right now.", Toast.LENGTH_LONG).show();
                        }
                        /************ Parse Json *******************/
                        jsontxt = findViewById(R.id.jsonTXT);
                        StringRequest request = new StringRequest(urljson, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String string) {
                                parseJsonData(string);
                                //get longi/lat
                                parseJsonLatLongi(string);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Toast.makeText(getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
                        rQueue.add(request);

                    }
                });

            }
        });

        //GoogleMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //SMS
        Button sendContact1 = (Button) findViewById(R.id.contact1);//click button1 action:
        Button sendContact2 = (Button) findViewById(R.id.contact2);
        Button sendContact3 = (Button) findViewById(R.id.contact3);
        //contact1.setOnClickListener(new ButtonClickListener());

        sendContact1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolSend1 = true;
                sendSMSMessage();
            }
        });
        sendContact2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolSend2 = true;
                sendSMSMessage();
            }
        });
        sendContact3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolSend3 = true;
                sendSMSMessage();
            }
        });




        /*Button MQTTbutton = (Button) findViewById(R.id.MQTTbutton);

        MQTTbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String clientId = MqttClient.generateClientId();
                MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(), "192.168.43.203:1883",
                        clientId);

                try {
                    IMqttToken token = client.connect();
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // We are connected
                            Log.d("Success", "onSuccess");
                            Toast.makeText(getApplicationContext(), "MQTT SUCCESS", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Log.d("Fail", "onFailure");
                            Toast.makeText(getApplicationContext(), "MQTT FAIL", Toast.LENGTH_LONG).show();

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        });*/

    }

    //MQTT
    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended(){
            @Override
            public void connectComplete(boolean b, String s){}
            @Override
            public void connectionLost(Throwable throwable) {}
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
        });
    }

    //Location
    private void requestPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
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

    /********************************************** Function: SMS part ******************************************/
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
                    Toast.makeText(getApplicationContext(),"SMS failed, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //***** Main function to send txt message to pre-define contact short-cut *******
    private void SendTextMsg() {
        SmsManager smsManager = SmsManager.getDefault();

        if(boolSend1){
            smsManager.sendTextMessage(phoneNum1, null, message, null, null);
            boolSend1 = false;
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, the nearest helping point is directing.",Toast.LENGTH_LONG).show();
        }else if(boolSend2){
            smsManager.sendTextMessage(phoneNum2, null, message, null, null);
            boolSend2 = false;
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, the nearest helping point is directing.",Toast.LENGTH_LONG).show();
        }else if(boolSend3){
            smsManager.sendTextMessage(phoneNum3, null, message, null, null);
            boolSend3 = false;
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, the nearest helping point is directing.",Toast.LENGTH_LONG).show();
        }else{
            smsManager.sendTextMessage(phoneNum1, null, url, null, null);
            smsManager.sendTextMessage(phoneNum2, null, url, null, null);
            smsManager.sendTextMessage(phoneNum3, null, url, null, null);
            Toast.makeText(getApplicationContext(), "Current location has sent. Please do not worry, the nearest helping point is directing.",Toast.LENGTH_LONG).show();
        }

        //Default
        //smsManager.sendTextMessage(phoneNum, null, message, null, null);


    }
    /********************************************** End SMS function ******************************************/

    /********************************************** Function: Search the nearest safe place  *****************************************/
    void parseJsonData(String jsonString) {
        try {
            JSONObject allJSON = new JSONObject(jsonString);
            JSONArray locationArray = allJSON.getJSONArray("results");
            ArrayList al = new ArrayList();

            for(int i = 0; i < locationArray.length(); ++i) {
                JSONObject locationObj = locationArray.getJSONObject(i);
                al.add(locationObj.getString("name"));
            }
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, al);
            jsontxt.setAdapter(adapter);
            Toast.makeText(getApplicationContext(), "JSON function called.",Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //************ Search the nearest safe place *******************
    void parseJsonLatLongi(String jsonString) {
        try {
            JSONObject allJSON = new JSONObject(jsonString);
            JSONArray locationArray = allJSON.getJSONArray("results");
            ArrayList al = new ArrayList();

            //for(int i = 0; i < locationArray.length(); ++i) {
            JSONObject locationObj = locationArray.getJSONObject(0);
            JSONObject geometry = locationObj.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            al.add(location.getString("lat"));
            destLat = location.getString("lat");
            destLongi = location.getString("lng");
            Toast.makeText(getApplicationContext(), "Dest lat longi: ." + destLat + "," + destLongi,Toast.LENGTH_LONG).show();

            /************ Navigation *******************/
            TextView destLocation = findViewById(R.id.destLocation);
            destLocation.setText(destLat+ " , " +destLongi);
            //destLocation
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+destLat+","+destLongi+"&mode=w");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }


            //}
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, al);
            jsontxt.setAdapter(adapter);
            Toast.makeText(getApplicationContext(), "JSON function parseJsonLatLongi called.",Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /********************************************** End Search the nearest safe place  *****************************************/

}