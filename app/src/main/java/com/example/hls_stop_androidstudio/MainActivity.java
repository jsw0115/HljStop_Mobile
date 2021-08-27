package com.example.hls_stop_androidstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnArduino;
    private Button btnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnArduino = findViewById(R.id.btn_Arduino);
        btnCamera = findViewById(R.id.btn_Camera);

        btnArduino.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent adIntent = new Intent(MainActivity.this, ArduinoActivity.class);
                startActivity(adIntent);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent caIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(caIntent);
            }
        });
    }
}