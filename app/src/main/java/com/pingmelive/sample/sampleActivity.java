package com.pingmelive.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pingmelive.lib.pingMeLive;

public class sampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.pingmelive.sample.R.layout.activity_sample);

        String userID = "userabc";

        Button forceStop = findViewById(com.pingmelive.sample.R.id.forceStop);
        forceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                throw new RuntimeException("Here is an error so that you can test!");

            }
        });

        Button simpleEvent = findViewById(com.pingmelive.sample.R.id.simpleEvent);
        simpleEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(sampleActivity.this, "Simple event sent", Toast.LENGTH_SHORT).show();
                pingMeLive.simpleEvent("Simple event","This is a simple event!!");


            }
        });

        Button detailedEvent = findViewById(com.pingmelive.sample.R.id.detailedEvent);
        detailedEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(sampleActivity.this, "Detailed event sent", Toast.LENGTH_SHORT).show();
                pingMeLive.detailedEvent("Detailed event","This is a Detailed event!!","You can send big information here.");


            }
        });
    }
}