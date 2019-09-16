package com.pingmelive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class sampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        String userID = "userabc";

        Button forceStop = findViewById(R.id.forceStop);
        forceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                throw new RuntimeException("Here is an error so that you can test!");

            }
        });

        Button simpleEvent = findViewById(R.id.simpleEvent);
        simpleEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(sampleActivity.this, "Simple event sent", Toast.LENGTH_SHORT).show();
                pingMeLive.simpleEvent("Simple event","This is a simple event!!");


            }
        });
    }
}