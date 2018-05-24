package com.example.shreyus.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserSetting extends AppCompatActivity {
    public static final String PREFS_NAME = "MyContact";
    public static final String person1_id = "person1";
    public static final String person2_id = "person2";
    public static final String person3_id = "person3";
    public static final int RESULT_UPDATE = 886;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        Intent intent = getIntent();
//        String num1 = intent.getStringExtra(MainActivity.person1_id);
//        String num2 = intent.getStringExtra(MainActivity.person2_id);
//        String num3 = intent.getStringExtra(MainActivity.person3_id);

        /******** Display info *********/
        EditText editNum1 = findViewById(R.id.editNum1);
        EditText editNum2 = findViewById(R.id.editNum2);
        EditText editNum3 = findViewById(R.id.editNum3);

        SharedPreferences dataSaved = getSharedPreferences(PREFS_NAME, 0);
        String num1 = dataSaved.getString(person1_id,null);
        String num2 = dataSaved.getString(person2_id,null);
        String num3 = dataSaved.getString(person3_id,null);
        //EditText edittest = findViewById(R.id.test);

        editNum1.setText(num1);
        editNum2.setText(num2);
        editNum3.setText(num3);



    }

    public void SaveInfo(View view) {

        Intent tomainIntent = new Intent(this, MainActivity.class);

        EditText editNum1 = findViewById(R.id.editNum1);
        EditText editNum2 = findViewById(R.id.editNum2);
        EditText editNum3 = findViewById(R.id.editNum3);

        String num1 = editNum1.getText().toString();
        String num2 = editNum2.getText().toString();
        String num3 = editNum3.getText().toString();

        //saved to file
        SharedPreferences savedFile = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = savedFile.edit();
        editor.putString(person1_id, num1);
        editor.putString(person2_id,num2);
        editor.putString(person3_id,num3);
        editor.commit();

//        tomainIntent.putExtra(person1_id, num1);
//        tomainIntent.putExtra(person2_id, num2);
//        tomainIntent.putExtra(person3_id, num3);

        setResult(RESULT_UPDATE,tomainIntent);
        finish();

    }

}
