package com.example.shreyus.myapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import helpers.MqttHelper;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

class Locations {
    private String name;
    private String lat;
    private String longi;

    public Locations(String name, String lat, String longi){
        this.name = name;
        this.lat = lat;
        this.longi = longi;
    }

    public String getName(){
        return this.name;
    }

    public String getLat(){
        return this.lat;
    }

    public String getLongi(){
        return this.longi;
    }
}

//FragmentActivity
public class MainActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    /********************************************** Pre-define ******************************************/

    /******** To-Do list *********/
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    //User Setting
    //public static final String EXTRA_MSG="com.example.myapp.usersetting";
    public static final String person1_id = "person1";
    public static final String person2_id = "person2";
    public static final String person3_id = "person3";
    public static final String person1_name= "person1_name";
    public static final String person2_name = "person2_name";
    public static final String person3_name = "person3_name";
    public static int RESULT_UPDATE = 886;
    public static final String person1_img= "person1_img";
    public static final String person2_img= "person2_img";
    public static final String person3_img= "person3_img";
    public static final String VIBRATION = "Vibration";
    public static final String LIST = "list";
    public static final String MAP = "googlemap";
    public static final String BATTERY = "battery";

    public boolean boolVibrate;
    public boolean boolList;
    public boolean boolNavigation;
    public boolean boolBattery;
    public boolean firstTime = true;

    //MQTT
    public MqttHelper mqttHelper;
    byte[] encodedPayload = new byte[0];
    TextView dataReceived;
    public boolean firstMqtt = true;
    public String pub_current_location = "Current";
    public String pub_dest_location = "Dest";
    //final String serverUri = "tcp://192.168.43.185:1883";
    final String serverUri = "tcp://192.168.1.145:61613";
    final String clientId = "ExampleAndroidClient" + System.currentTimeMillis();
    final String subscriptionTopic = "Test";
    final String username = "admin";
    final String password = "password";

    String PLACES_API_KEY = "AIzaSyBVGJYHClfBB8sMIkb1wNqJLqeLlYkcnzo";

    //Google maps
    private GoogleMap mMap;

    //Current Location
    private FusedLocationProviderClient locationClient;

    //JSON closest location
    ListView jsontxt;
    String url, urljson;
    String currentLat;
    String currentLongi;
    String destLat = "123";
    String destLongi = "456";
    String destName = "A safe space";


    //SMS
    private static final int MY_PERMISSION_REQUEST_SEND_SMS = 0;

    //For sending sms
    String phoneNum1 , phoneNum2 , phoneNum3;//vm1 5554, vm2 5556
    String personName1,personName2,personName3;
    String message = "Help!";
    TextView txtContact1, txtContact2, txtContact3;

//    String num1 = phoneNum1;
//    String num2 = phoneNum2;
//    String num3 = phoneNum3;

    boolean boolSend1 = false, boolSend2 = false, boolSend3 = false;

    //For saving data after closing
    public static final String PREFS_NAME = "MyContact";

    //Used for vibration function
    public static Vibrator v;


    /********************************************** End Pre-define ******************************************/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changeTitle("Wristband Not Connected");

        //retrieve data saved
        SharedPreferences dataSaved = getSharedPreferences(PREFS_NAME, 0);
        UpdateBooleanUserSetting(dataSaved);

        //Check if first time setup
        if(dataSaved.getString("firstTimeSetup", null)==null){
            openUserSettings();
            SharedPreferences.Editor editor = dataSaved.edit();
            editor.putString("firstTimeSetup", "T");
            editor.commit();
        }

        //To-do list
        mHelper = new TaskDbHelper(this);
        mTaskListView = findViewById(R.id.list_todo);
        mTaskListView.setVisibility(View.GONE);
        updateUI();

        //sendNotification();

        //Initialise vibrator variable with vibrator_service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Link button variables to buttons in XML layout

        Button sendContact1 = (Button) findViewById(R.id.contact1);//click button1 action:
        Button sendContact2 = (Button) findViewById(R.id.contact2);
        Button sendContact3 = (Button) findViewById(R.id.contact3);
        txtContact1 = findViewById(R.id.txtContact1);
        txtContact2 = findViewById(R.id.txtContact2);
        txtContact3 = findViewById(R.id.txtContact3);

        //Read phone number from database during activity Create

        UpdateImage(dataSaved,person1_img,sendContact1);
        UpdateImage(dataSaved,person2_img,sendContact2);
        UpdateImage(dataSaved,person3_img,sendContact3);

        phoneNum1 = dataSaved.getString(person1_id,null);
        phoneNum2 = dataSaved.getString(person2_id,null);
        phoneNum3 = dataSaved.getString(person3_id,null);
        personName1 = dataSaved.getString(person1_name,null);
        personName2 = dataSaved.getString(person2_name,null);
        personName3 = dataSaved.getString(person3_name,null);

        jsontxt = findViewById(R.id.jsonTXT);
        jsontxt.setVisibility(View.GONE);

        if(personName1 != null && !personName1.isEmpty()){
            txtContact1.setText(personName1);
        }else{
            txtContact1.setText("Contact 1");
        }
        if(personName2 != null && !personName2.isEmpty()){
            txtContact2.setText(personName2);
        }else{
            txtContact2.setText("Contact 2");
        }
        if(personName3 != null && !personName3.isEmpty()){
            txtContact3.setText(personName3);
        }else{
            txtContact3.setText("Contact 3");
        }

        //Toast.makeText(getApplicationContext(), "Current nums are: "+phoneNum1 + " , "+phoneNum2+" , "+phoneNum3+" .", Toast.LENGTH_LONG).show();

        //MQTT
        dataReceived = findViewById(R.id.dataReceived);
        mqttHelper = new MqttHelper(getApplicationContext());

        if(firstTime) {
            startMqtt("Test","Hello ", "from Android","","" );
            //sendMqtt("Test","","","","");
            firstTime = false;
        }

        //Current Location
        requestPermissions();
        locationClient = getFusedLocationProviderClient(this);

        final Button helpButton = findViewById(R.id.HELPbutton);

        //Warn if battery level low, send HELP signal if battery below 15%
        batteryLife();


        /********************************* Help Button Implementation *********************************/
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpButton();
            }
        });
        /********************************* End Help Button *********************************/

        /********************************* Google Map Implementation *********************************/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /********************************* SMS Implementation *********************************/


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

    }

    /************ Help Button Implementation *******************/
    void HelpButton(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                double lat = location.getLatitude();
                double longi = location.getLongitude();
                currentLat = Double.toString(lat);
                currentLongi=Double.toString(longi);

                if (location != null) {
                    //url = "http://maps.google.com/maps?z=12&t=m&q=loc:" + lat + "+" + longi;
                    url = "http://maps.google.com/maps?z=12&t=m&q=" + lat + "+" + longi;

                    urljson = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + longi + "&rankby=distance&type=cafe|restaurant|food|drink|police&key=" + PLACES_API_KEY;

                    //Toast.makeText(getApplicationContext(), "Current location:\n" + lat + "," + longi, Toast.LENGTH_LONG).show();
                    sendSMSMessage();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot get GPS right now.", Toast.LENGTH_LONG).show();
                }
                /************ Parse Json *******************/
                if(boolList || boolNavigation) {
                    StringRequest request = new StringRequest(urljson, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String string) {
                            parseJsonData(string);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getApplicationContext(), "Some error occurred when requesting nearest locations!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
                    rQueue.add(request);
                }else{
                    startMqtt("Test",currentLat,currentLongi,currentLat,currentLongi);
                }
            }
        });
    }

    /******* To-Do List *************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                if(task.trim().length()>0) {
                                    SQLiteDatabase db = mHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                    db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                            null,
                                            values,
                                            SQLiteDatabase.CONFLICT_REPLACE);
                                    db.close();
                                    updateUI();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;
            case R.id.user_setting:
                openUserSettings();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);



            mTaskListView.setAdapter(mAdapter);

            if(mAdapter.getCount()!=0){
                mTaskListView.setVisibility(View.VISIBLE);
            }

        } else {

            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();

            if(mAdapter.getCount()!=0) {
                mTaskListView.setVisibility(View.VISIBLE);
            }
            else{
                mTaskListView.setVisibility(View.GONE);
            }
        }

        cursor.close();
        db.close();
    }
    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }
    /********************************************** Implementation: User setting ******************************************/
    void UpdateBooleanUserSetting(SharedPreferences dataSaved){
        boolVibrate = dataSaved.getBoolean(VIBRATION, true);
        boolList = dataSaved.getBoolean(LIST, true);
        boolNavigation = dataSaved.getBoolean(MAP, true);
        boolBattery = dataSaved.getBoolean(BATTERY, true);
    }
    public void userSetting(View view){
        openUserSettings();
    }

    public void openUserSettings(){
        Intent intent = new Intent(this, UserSetting.class);

        intent.putExtra(person1_id, phoneNum1);
        intent.putExtra(person2_id, phoneNum2);
        intent.putExtra(person3_id, phoneNum3);

        startActivityForResult(intent,1);


    }

    //Update User Info
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        recreate();

        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(resultCode == RESULT_UPDATE){

            Toast.makeText(this, "You have successfully updated your contact details", Toast.LENGTH_LONG).show();
            /******** Edit Update Info*************/

            SharedPreferences dataSaved = getSharedPreferences(PREFS_NAME, 0);
            UpdateBooleanUserSetting(dataSaved);
            Button sendContact1 = findViewById(R.id.contact1);//click button 1 action:
            Button sendContact2 = findViewById(R.id.contact2);
            Button sendContact3 = findViewById(R.id.contact3);

            //Update Image

            UpdateImage(dataSaved, person1_img, sendContact1);
            sendContact1.setText("");
            UpdateImage(dataSaved, person2_img, sendContact2);
            UpdateImage(dataSaved, person3_img, sendContact3);

            //Update phone Number
            phoneNum1 = dataSaved.getString(person1_id,null);
            phoneNum2 = dataSaved.getString(person2_id,null);
            phoneNum3 = dataSaved.getString(person3_id,null);

            //Update Person Name
            personName1 = dataSaved.getString(person1_name,null);
            personName2 = dataSaved.getString(person2_name,null);
            personName3 = dataSaved.getString(person3_name,null);

            if(personName1 != null && !personName1.isEmpty()){
                txtContact1.setText(personName1);
            }else{
                txtContact1.setText("Contact 1");
            }
            if(personName2 != null && !personName2.isEmpty()){
                txtContact2.setText(personName2);
            }else{
                txtContact2.setText("Contact 2");
            }
            if(personName3 != null && !personName3.isEmpty()){
                txtContact3.setText(personName3);
            }else{
                txtContact3.setText("Contact 3");
            }


//            if((personName1 != null && !personName1.isEmpty())||
//               (personName2 != null && !personName2.isEmpty())||
//               (personName3 != null && !personName3.isEmpty())){
//                sendContact1.setText(personName1);
//                sendContact2.setText(personName2);
//                sendContact3.setText(personName3);
//            }else{
//                sendContact1.setText("Contact 1");
//                sendContact2.setText("Contact 2");
//                sendContact3.setText("Contact 3");
//            }

            /******** End Edit Update Info*************/
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void UpdateImage(SharedPreferences dataSaved, String person, Button sendContact){
        String bitmapFromSetting = dataSaved.getString(person,null);
        if(bitmapFromSetting!=null && !bitmapFromSetting.isEmpty()){
            Bitmap bitmap = decodeBase64(bitmapFromSetting);
            //img.setImageBitmap(bitmap);
            BitmapDrawable bdrawable = new BitmapDrawable(getResources(),bitmap);
            sendContact.setBackground(bdrawable);
        }


    }

    // Decode image string base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    /********************************************** End: User setting ******************************************/

    /********************************************** Function: MQTT part ****************************************/
    void sendMqtt(String topic,String currentLat, String currentLongi, String lat, String longi){
        Log.i("MQTT", "In send MQTT function ************************");
        //mqttHelper.publishToTopic(topic,"Hello from Android");
        mqttHelper.publishMessage(topic,"Hello from Android");
        /*try {
                        mqttHelper.mqttAndroidClient.publish(topic, ("Hello from Android").getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }*/

    }
    private void startMqtt(String topic,String currentLat, String currentLongi, String lat, String longi){
        switch(topic){
            case("Current"):
                mqttHelper.connect(topic,currentLat+","+currentLongi+"\n"+lat+","+longi);
            case("Test"):
                mqttHelper.connect(topic,currentLat+","+currentLongi);
//            default:
//                mqttHelper.connect("",currentLat+","+currentLongi);
        }
        mqttHelper.setCallback(new MqttCallbackExtended(){
            @Override
            public void connectComplete(boolean b, String s){
                changeTitle("Wristband Connected");
            }
            @Override
            public void connectionLost(Throwable throwable) {
                changeTitle("Wristband Not Connected");
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String msgReceived = mqttMessage.toString();
                Toast.makeText(getApplicationContext(), "Received:"+msgReceived, Toast.LENGTH_LONG).show();
                Log.w("Debug",msgReceived);
                dataReceived.setText(msgReceived);
                changeTitle("Wristband Connected");
                switch(msgReceived){
                    case ("Warning"):
                        HelpButton();
                    default:
                        dataReceived.setText(msgReceived);
                        changeTitle("Wristband Connected");

                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
        });

    }
    //0 0 0
    //0 1 1
    //1 0 0
    //1 1 1

    void changeTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    /********************************************** End: MQTT part ******************************************/

    /********************************************** Function: Location Implementation ******************************************/
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
            getLocation(mMap);
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /********************************************** Function: SMS part ******************************************/
    protected void sendSMSMessage() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_SEND_SMS);
        } else {
            if((phoneNum1 != null && !phoneNum1.isEmpty())||
               (phoneNum2 != null && !phoneNum2.isEmpty())||
               (phoneNum3 != null && !phoneNum3.isEmpty())){
                SendTextMsg();
            }else{
                Toast.makeText(getApplicationContext(),"Please set your contact number(s)!", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SendTextMsg();
                } else {
                    Toast.makeText(getApplicationContext(),"SMS failed, please allow permission and try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //***** Main function to send txt message to pre-define contact short-cut *******
    private void SendTextMsg() {
        SmsManager smsManager = SmsManager.getDefault();

        if(boolSend1 && (phoneNum1 != null && !phoneNum1.isEmpty())){
            smsManager.sendTextMessage(phoneNum1, null, message, null, null);
            boolSend1 = false;
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, help is coming.",Toast.LENGTH_LONG).show();
        }else if(boolSend2 && (phoneNum2 != null && !phoneNum2.isEmpty())){
            smsManager.sendTextMessage(phoneNum2, null, message, null, null);
            boolSend2 = false;
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, help is coming.",Toast.LENGTH_LONG).show();
        }else if(boolSend3 && (phoneNum3 != null && !phoneNum3.isEmpty())){
            smsManager.sendTextMessage(phoneNum3, null, message, null, null);
            boolSend3 = false;
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, help is coming.",Toast.LENGTH_LONG).show();
        }else if((boolSend1 && (phoneNum1 == null || phoneNum1.isEmpty())) || (boolSend2 && (phoneNum2 == null || phoneNum2.isEmpty())) || (boolSend3 && (phoneNum3 == null || phoneNum3.isEmpty()))) {
            Toast.makeText(getApplicationContext(),"Please set this contact number!", Toast.LENGTH_LONG).show();
            boolSend1=false;
            boolSend2=false;
            boolSend3=false;
        }
        else{
            if ((phoneNum1 != null && !phoneNum1.isEmpty()) && phoneNum1 !="") {
                smsManager.sendTextMessage(phoneNum1, null, url, null, null);
            }
            if ((phoneNum2 != null && !phoneNum2.isEmpty()) && phoneNum2 !="") {
                smsManager.sendTextMessage(phoneNum2, null, url, null, null);
            }
            if ((phoneNum3 != null && !phoneNum3.isEmpty()) && phoneNum3 !="") {
                smsManager.sendTextMessage(phoneNum3, null, url, null, null);
            }
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, HELP is coming.",Toast.LENGTH_LONG).show();
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
            JSONObject locationObj, geometry, location;

            final ArrayList<Locations> al = new ArrayList<Locations>();
            final ArrayList names = new ArrayList();
            final ArrayList<ArrayList<String>> destLocation = new ArrayList<ArrayList<String>>();
            ArrayList<String> arrayDest = new ArrayList<>();

            for(int i = 0; i < locationArray.length(); ++i) {
                locationObj = locationArray.getJSONObject(i);
                geometry = locationObj.getJSONObject("geometry");
                location = geometry.getJSONObject("location");
                destLat = location.getString("lat");
                destLongi = location.getString("lng");
                destName = locationObj.getString("name");
                al.add(new Locations(destName,destLat, destLongi));
                names.add(destName);

            }
            /************ Navigation *******************/
            if(boolNavigation){
                Navigation(al,0);
            }else{
                startMqtt("Test",currentLat,currentLongi,currentLat,currentLongi);
            }
            /******************************************/

            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, names);
            jsontxt.setVisibility(View.VISIBLE);
            jsontxt.setAdapter(adapter);
            jsontxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   //Toast.makeText(MainActivity.this, al.get(position).getLat().toString(), Toast.LENGTH_LONG).show();
                    Navigation(al,position);
                }
            });
            //Toast.makeText(getApplicationContext(), "JSON function called.",Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void Navigation(ArrayList<Locations> al,int i){
        startMqtt("Current",currentLat,currentLongi, al.get(i).getLat(), al.get(i).getLongi());
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+al.get(i).getLat()+", "+al.get(i).getLongi()+"&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    /********************************************** End Search the nearest safe place  *****************************************/


    /********************************************** Function: Search the nearest safe place  ***********************************/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void batteryLife(){
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);

        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        //If battery life is very low, send text message to contacts with location

        if(batLevel <=20) {
            if(boolVibrate && boolBattery){
                vibrate();
            }
            if(boolBattery) {
                Toast.makeText(getApplicationContext(), "Current battery life is: " + batLevel + ". Critical battery life.", Toast.LENGTH_LONG).show();
            }
            //sendNotification(batLevel);
            getLocationSMS();
        }
        //Warn if battery life is getting low - user should charge before heading out
        else if(batLevel <=50){

            //sendNotification();
            sendNotification(batLevel);

            if(boolVibrate && boolBattery){
                vibrate();
            }
            if(boolBattery){
                Toast.makeText(getApplicationContext(), "Current battery life is: " + batLevel + ". Please consider charging before leaving.", Toast.LENGTH_LONG).show();
            }

        }
    }

   @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void sendNotification(int batLevel){

        String title = "Battery";
        String subject = "Battery Low Level";
        String body = "Current battery life is: " + batLevel + " Critical Battery Life!";
        NotificationManager notif = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify = new Notification.Builder(getApplicationContext()).setContentTitle(title).
                setContentText(body).setContentTitle(subject).setSmallIcon(R.drawable.config_configuration_settings_icon_64).build();
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
    }

    /********************************************** End Search the nearest safe place  *****************************************/
    void getLocationSMS(){
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

                    url = "http://maps.google.com/maps?z=12&t=m&q=" + lat + "+" + longi;

                    sendSMSMessage();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot get GPS right now.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void getLocation(final GoogleMap mMap){

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

                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longi),8.0f));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longi),16.0f));

                } else {
                    Toast.makeText(getApplicationContext(), "Oops, cannot get GPS right now.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void vibrate(){
        long[] pattern = {0,400,400,400,400,600};
        v.vibrate(pattern, -1);
    }
}