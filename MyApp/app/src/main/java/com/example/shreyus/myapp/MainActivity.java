package com.example.shreyus.myapp;


import java.util.ArrayList;

//Location import
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;

//SMS import
import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnSuccessListener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

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

    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    //User Setting
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

    /* MQTT declaration */
    public MqttHelper mqttHelper;
    TextView dataReceived;

    /* Place api key*/
    String PLACES_API_KEY = "AIzaSyBVGJYHClfBB8sMIkb1wNqJLqeLlYkcnzo";
    /**
     *  Two different battery warning levels:
     *  20% and 50%
     * */
    int LOW_BATTERY_LIFE = 50;
    int CRITICAL_BATTERY_LIFE = 20;

    //Google maps
    private GoogleMap mMap;

    //Current Location
    private FusedLocationProviderClient locationClient;

    //Navigation initialisation
    ListView jsontxt;
    String url, urljson;
    String urlDest;
    String currentLat;
    String currentLongi;
    String destLat = "123";
    String destLongi = "456";
    String destName = "A safe space";


    /** SMS */
    private static final int MY_PERMISSION_REQUEST_SEND_SMS = 0;

    /** Sending sms initialisation */
    String phoneNum1 , phoneNum2 , phoneNum3;
    String personName1,personName2,personName3;
    String message = "Help!";
    TextView txtContact1, txtContact2, txtContact3;

    boolean boolSend1 = false, boolSend2 = false, boolSend3 = false;

    /** For saving data after closing the app */
    public static final String PREFS_NAME = "MyContact";

    /** Used for vibration function */
    public static Vibrator v;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Change app title while initialing the app */
        changeTitle("Wristband Not Connected");

        /* retrieve data saved */
        SharedPreferences dataSaved = getSharedPreferences(PREFS_NAME, 0);
        UpdateBooleanUserSetting(dataSaved);

        /** Check if first time setup the app
         *  If yes, go to setting page
         *  and update the tuple in SharedPreferences
         * */
        if(dataSaved.getString("firstTimeSetup", null)==null){
            openUserSettings();
            SharedPreferences.Editor editor = dataSaved.edit();
            editor.putString("firstTimeSetup", "T");
            editor.commit();
        }

        /**
         *  To-do list Initialisation
         *  Get xml for the to-do list from list_todo.xml
         *  Set it invisible until it's been called
         *  Update the UI for the initialisation
         */
        mHelper = new TaskDbHelper(this);
        mTaskListView = findViewById(R.id.list_todo);
        mTaskListView.setVisibility(View.GONE);
        updateUI();

        /**
         * Initialise vibrator variable with vibrator_service
         */
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /** Link button variables to buttons in XML layout */
        Button sendContact1 = findViewById(R.id.contact1);//click button1 action:
        Button sendContact2 = findViewById(R.id.contact2);
        Button sendContact3 = findViewById(R.id.contact3);
        txtContact1 = findViewById(R.id.txtContact1);
        txtContact2 = findViewById(R.id.txtContact2);
        txtContact3 = findViewById(R.id.txtContact3);

        /**
         *  Read phone number from database during activity OnCreate
         *  Update Person shortcut image from SharedPreference
         *  Update Phone number
         *  Update Person name
         */
        UpdateImage(dataSaved,person1_img,sendContact1);
        UpdateImage(dataSaved,person2_img,sendContact2);
        UpdateImage(dataSaved,person3_img,sendContact3);
        phoneNum1 = dataSaved.getString(person1_id,null);
        phoneNum2 = dataSaved.getString(person2_id,null);
        phoneNum3 = dataSaved.getString(person3_id,null);
        personName1 = dataSaved.getString(person1_name,null);
        personName2 = dataSaved.getString(person2_name,null);
        personName3 = dataSaved.getString(person3_name,null);

        /**
         * Initialise optional destination list
         * Set to invisible
         */
        jsontxt = findViewById(R.id.jsonTXT);
        jsontxt.setVisibility(View.GONE);

        /**
         * If person name is not Null
         * Update name to the button
         */
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

        /**
         *  MQTT initialisation
         *  When opening the app
         *  Send "Hello from Android" to Raspberry Pi -> Indicating phone connection
         */
        dataReceived = findViewById(R.id.dataReceived);
        mqttHelper = new MqttHelper(getApplicationContext());
        if(firstTime) {
            startMqtt("Test","Hello ", "from Android","","" );
            firstTime = false;
        }

        //Current Location
        requestPermissions();
        locationClient = getFusedLocationProviderClient(this);

        final Button helpButton = findViewById(R.id.HELPbutton);

        /**
         *  Warn if battery level low, send HELP signal if battery below the certain level
         */
        batteryLife();

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpButton();
            }
        });

        //Google Map implementation
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //SMS Implementation
        sendContact1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolSend1 = true;
                getLocation(mMap, true);
            }
        });
        sendContact2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolSend2 = true;
                getLocation(mMap, true);
            }
        });
        sendContact3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolSend3 = true;
                getLocation(mMap, true);
            }
        });
    }

    /**
     * Function that is called when the HELP button is pressed on phone or wristband
     * First checks for location permissions
     * Obtains last known location from FusedLocationProviderClient (lat/longitude)
     * Creates two URLs.. One for current location via SMS (url), and one
     * to be used for the parseJsonData function to display nearest safe places
     * (urljson).
     */
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
                currentLongi= Double.toString(longi);

                if (location != null) {
                    //URL to send via SMS with current location
                    url = "http://maps.google.com/maps?z=12&t=m&q=" + lat + "+" + longi;

                    //URL for json formatted nearby safe locations
                    urljson = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + longi + "&rankby=distance&type=cafe|restaurant|food|drink|police&key=" + PLACES_API_KEY;

                    //Toast.makeText(getApplicationContext(), "Current location:\n" + lat + "," + longi, Toast.LENGTH_LONG).show();

                    sendSMSMessage(false);


                } else {
                    Toast.makeText(getApplicationContext(), "Cannot get GPS right now.", Toast.LENGTH_LONG).show();
                }
                /************ Parse Json *******************/
                if(boolNavigation || boolList) {
                    StringRequest request = new StringRequest(urljson, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String string) {
                            //If json exists, parse it for nearby safe locations
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
                    //If list and navigation are both turned off, only send current location
                    startMqtt("Current",currentLat,currentLongi,"list-on/off","nav-off");
                }
            }
        });
    }


    /**
     * To-do List
     * Get main_menu.xml and use inflater
     * to put the xml structure in main_activity
     * @param menu menu instance for the xml
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Update the database
     * For the to-do list
     * During click the + button
     * Using action_add_task.xml layout
     * @param item the button to add the to-do list is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                final EditText taskEditText = new EditText(this);
                /**
                 * Set dialog for user to input the list
                 */
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            /**
                             * Creating SQLiteDatabase
                             * To store the task
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                //Check if input is not whitespace
                                if(task.trim().length()>0) {
                                    mqttHelper.connect("ToDo", task);
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

    /**
     * Update the to-do list ui
     */
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
        /**
         * If the array adapter is null
         *      Create a new one to store/show the task
         * Else,
         *      Update the adapter
         */
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

    /**
     * Delete the task
     * From the database
     * @param view Delete current view for the specific task
     */
    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }


    /**
     * Update the User setting when open the app or return from user setting
     * @param dataSaved database name that data saved to
     */
    void UpdateBooleanUserSetting(SharedPreferences dataSaved){
        boolVibrate = dataSaved.getBoolean(VIBRATION, true);
        boolList = dataSaved.getBoolean(LIST, true);
        boolNavigation = dataSaved.getBoolean(MAP, true);
        boolBattery = dataSaved.getBoolean(BATTERY, true);
    }

    /**
     * Opens user settings activity
     */
    public void openUserSettings(){
        Intent intent = new Intent(this, UserSetting.class);

        intent.putExtra(person1_id, phoneNum1);
        intent.putExtra(person2_id, phoneNum2);
        intent.putExtra(person3_id, phoneNum3);

        startActivityForResult(intent,1);


    }

    /**
     * Update User Info Function after return from another Intent
     * @param requestCode requestCode -> to specific different Intent
     * @param resultCode ResultCode returned from the Intent
     * @param data Data pass back from the Intent
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        recreate();

        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(resultCode == RESULT_UPDATE){

            Toast.makeText(this, "You have successfully updated your contact details", Toast.LENGTH_LONG).show();

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

            /******** End Edit Update Info*************/
        }
    }

    /**
     *  Update the image for the user
     *  Decode the bitmap
     *  and update the button image
     *  @param dataSaved database name
     *  @param person Person id in the database
     *  @param sendContact button id
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void UpdateImage(SharedPreferences dataSaved, String person, Button sendContact){
        String bitmapFromSetting = dataSaved.getString(person,null);
        if(bitmapFromSetting!=null && !bitmapFromSetting.isEmpty()){
            Bitmap bitmap = decodeBase64(bitmapFromSetting);
            BitmapDrawable bdrawable = new BitmapDrawable(getResources(),bitmap);
            sendContact.setBackground(bdrawable);
        }
    }

    /**
     * Decode image string base64 to bitmap
     * In order to store the image in SharedPreference
     * to pass be
     * @param input bitmap string that stored in the database
     */
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    /********************************************** End: User setting ******************************************/

    /********************************************** Function: MQTT part ****************************************/

    /**
     * Function to publish and subscribe to/from mqtt broker
     * @param topic topic in MQTT broker to subscribe/publish to
     * @param currentLat Current latitude to send to wristband
     * @param currentLongi Current longitude to send to wristband
     * @param lat Destinaton latitude to send to wristband
     * @param longi Destinaton longitude to send to wristband
     */

    private void startMqtt(String topic,String currentLat, String currentLongi, String lat, String longi){
        switch (topic) {
            case("Current"):
                mqttHelper.connect("Current",currentLat+","+currentLongi+"\n"+lat+","+longi);
                break;
            case("Test"):
                mqttHelper.connect("Test",currentLat+","+currentLongi);
                break;
            default:
                throw new IllegalArgumentException("alkjdsflaskdjfals;kdjf ");
        }

        mqttHelper.setCallback(new MqttCallbackExtended(){
            @Override
            public void connectComplete(boolean b, String s){
                /**
                 * Connection complete -> change app title
                 */
                changeTitle("Wristband Connected");
            }
            @Override
            public void connectionLost(Throwable throwable) {
                /**
                 * Lost connection -> change app title
                 */
                changeTitle("Wristband Not Connected");
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                /**
                 * Subscribed and receive the message from the wristband
                 * Change app title
                 * If msg == Warning
                 *      Call Help function to start navigation
                 *  Else
                 *      Change title;
                 */
                String msgReceived = mqttMessage.toString();
                Log.w("Debug",msgReceived);
                dataReceived.setText(msgReceived);
                changeTitle("Wristband Connected");
                switch(msgReceived){
                    case ("Warning"):
                        HelpButton();
                        break;
                    default:
                        dataReceived.setText(msgReceived);
                        changeTitle("Wristband Connected");
                        break;
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
        });
    }

    /**
     * Function to change app title
     * @param title String to set the title to in the Action bar
     */
    void changeTitle(String title){
        getSupportActionBar().setTitle(title);
    }


    /**
     * Requests location permissions (Fine location)
     */
    private void requestPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    /**
     * Checks for location permissions first. If permissions are granted, then
     * enable "My location" and calls getLocation function to fetch current
     * location latitude/longitude data
     * @param googleMap Google maps map instance
     */
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
            getLocation(mMap, false);
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


    /**
     * SMS:
     * First, checks for SMS permissions
     * If at least one phone number has been set, sends message
     * Either set to send destination location (nearby safe place) by SMS
     * or only current location by SMS.
     * @param sendDest determines whether or not to send destination location by SMS
     *                 (if true, sends destination)
     */
    public void sendSMSMessage(boolean sendDest) {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_SEND_SMS);
        } else {
            if((phoneNum1 != null && !phoneNum1.isEmpty())||
               (phoneNum2 != null && !phoneNum2.isEmpty())||
               (phoneNum3 != null && !phoneNum3.isEmpty())){
                SendTextMsg(sendDest);

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
                }
                else {
                    Toast.makeText(getApplicationContext(),"SMS failed, please allow permission and try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Sends the SMS using SmsManager
     * Depending on how the function was called, either sends current location or
     * sends destination location (next safe place).
     * If contact icons are clicked in the app, sends current location to that specific contact.
     * If HELP button is pressed, sends to all contacts with a phone number set up.
     * @param boolDest
     */
    private void SendTextMsg(Boolean boolDest) {
        SmsManager smsManager = SmsManager.getDefault();

        message = "Your HELP is needed. Current location: "+ url;

        if(boolDest) {
            message = "Next safe place: "+ urlDest;
        }

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
        }else if(boolSend1 || boolSend2 || boolSend3) {
            Toast.makeText(getApplicationContext(),"Please set this contact number!", Toast.LENGTH_LONG).show();
            boolSend1=false;
            boolSend2=false;
            boolSend3=false;
        }
        else{
            if ((phoneNum1 != null && !phoneNum1.isEmpty()) && phoneNum1 !="") {
                smsManager.sendTextMessage(phoneNum1, null, message, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, HELP is coming.",Toast.LENGTH_LONG).show();

            }
            if ((phoneNum2 != null && !phoneNum2.isEmpty()) && phoneNum2 !="") {
                smsManager.sendTextMessage(phoneNum2, null, message, null, null);
            }
            if ((phoneNum3 != null && !phoneNum3.isEmpty()) && phoneNum3 !="") {
                smsManager.sendTextMessage(phoneNum3, null, message, null, null);
            }
            Toast.makeText(getApplicationContext(), "SMS sent. Please do not worry, HELP is coming.",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Reads the JSON url link as a string (jsonString)
     * Obtains the relevant results and stores in an array of class "Locations".
     * Also stores only names as a seperate array, to be displayed in a clickable
     * list in the UI, if the list option is enabled.
     * If "instant" navigation is turned on, app automatically navigates to nearest location
     * @param jsonString the content of the JSON url of nearby places, as a string
     */
    void parseJsonData(String jsonString) {
        try {
            JSONObject allJSON = new JSONObject(jsonString);
            JSONArray locationArray = allJSON.getJSONArray("results");
            JSONObject locationObj, geometry, location;

            final ArrayList<Locations> al = new ArrayList<Locations>();
            final ArrayList names = new ArrayList();

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
            }
            /******************************************/
            if(boolList){
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
            }
            //Toast.makeText(getApplicationContext(), "JSON function called.",Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void Navigation(ArrayList<Locations> al,int i){
        destLat = al.get(i).getLat();
        destLongi = al.get(i).getLongi();
        //Toast.makeText(MainActivity.this, "Before service in Navi...", Toast.LENGTH_LONG).show();

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+destLat+", "+destLongi+"&mode=w");
        urlDest= "http://maps.google.com/maps?z=12&t=m&q=" + destLat + "+" + destLongi;
        //Toast.makeText(MainActivity.this, urlDest.toString(), Toast.LENGTH_LONG).show();
        sendSMSMessage(true);

        if(boolNavigation || boolList) {
            startMqtt("Current", currentLat, currentLongi, destLat, destLongi);
            Intent intent = new Intent(this, LocationService.class);
            intent.putExtra("destinationLat",al.get(i).getLat());
            intent.putExtra("destinationLongi", destLongi);
            startService(intent);
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }


    /**
     * Checks battery life of the phone. If battery level is low, calls vibrate
     * function (optional) and displays message. Sends a notification to the phone
     * to alert the user that battery is low.
     * If battery level is critical, also sends a help signal (SMS) to contacts
     * with current location URL.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void batteryLife(){
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);

        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        //If battery life is very low, send text message to contacts with location

        if(batLevel <=CRITICAL_BATTERY_LIFE) {
            if(boolVibrate && boolBattery){
                vibrate();
            }
            if(boolBattery) {
                Toast.makeText(getApplicationContext(), "Current battery life is: " + batLevel + ". Critical battery life.", Toast.LENGTH_LONG).show();
            }
            getLocation(mMap, true);
        }
        //Warn if battery life is getting low - user should charge before heading out
        else if(batLevel <=LOW_BATTERY_LIFE){

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
        String body;
        if(batLevel<=CRITICAL_BATTERY_LIFE) {
            body = "Current battery life is: " + batLevel + " Critical Battery Life!";
        }
        else{
            body = "Current battery life is: " + batLevel+". Please consider charging before leaving";
        }
        NotificationManager notif = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify = new Notification.Builder(getApplicationContext()).setContentTitle(title).
                setContentText(body).setContentTitle(subject).setSmallIcon(R.drawable.config_configuration_settings_icon_64).build();
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
    }


    /**
     * First makes sure permissions are granted, uses FusedLocationProvider Client
     * to get last location when available (latitude/longitude), and animate the
     * Google map camera to the current location (or move camera if preferred)
     * Calls SMS function if bool is set (used when clicking on contacts).
     * @param mMap Google Map instance (to animate camera)
     * @param sendSMS Boolean to determine whether or not to send message with
     *                current location URL
     */
    void getLocation(final GoogleMap mMap, final boolean sendSMS){

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

                    if(sendSMS) {
                        url = "http://maps.google.com/maps?z=12&t=m&q=" + lat + "+" + longi;
                        sendSMSMessage(false);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Cannot get GPS right now.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Called by battery warning function to vibrate to alert user that phone
     * needs charging. Pattern set to vibrate once (no repeat).
     */
    void vibrate(){
        long[] pattern = {0,400,400,400,400,600};
        v.vibrate(pattern, -1);
    }
}
