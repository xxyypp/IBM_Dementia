package com.example.shreyus.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class UserSetting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        Intent intent = getIntent();
        String person1 = intent.getStringExtra(MainActivity.person1);
        String person2 = intent.getStringExtra(MainActivity.person2);
        String person3 = intent.getStringExtra(MainActivity.person3);

        TextView txt1 = findViewById(R.id.person1);
        TextView txt2 = findViewById(R.id.person2);
        TextView txt3 = findViewById(R.id.person3);
        txt1.setText(person1);
        txt2.setText(person2);
        txt3.setText(person3);




    }
}
