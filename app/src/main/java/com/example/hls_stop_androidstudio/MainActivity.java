package com.example.hls_stop_androidstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity extends AppCompatActivity {

    private Button btnArduino;
    private Button btnCamera;
    private TextToSpeech txtSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnArduino = findViewById(R.id.btn_Arduino);
        btnCamera = findViewById(R.id.btn_Camera);

        txtSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != ERROR)
                {
                    txtSpeech.setLanguage(Locale.KOREAN);
                }
            }
        });

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
                FuncVoiceOut("카메라를 선택하셨습니다. 버튼을 누르면 인식이 시작됩니다.");
                Intent caIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(caIntent);
            }
        });
    }

    private void FuncVoiceOut(String OutMsg) {
        if (OutMsg.length() < 1) return;

        txtSpeech.setPitch(1.0f);//목소리 톤1.0
        txtSpeech.setSpeechRate(1.0f);//목소리 속도
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            txtSpeech.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else
        {
            txtSpeech.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(txtSpeech != null){
            txtSpeech.stop();
            txtSpeech.shutdown();
            txtSpeech = null;
        }
    }
}