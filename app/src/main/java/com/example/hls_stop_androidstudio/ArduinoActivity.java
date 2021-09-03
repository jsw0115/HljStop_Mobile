package com.example.hls_stop_androidstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

import static android.speech.tts.TextToSpeech.ERROR;
import static app.akexorcist.bluetotohspp.library.BluetoothState.MESSAGE_READ;
import static app.akexorcist.bluetotohspp.library.BluetoothState.MESSAGE_WRITE;

public class ArduinoActivity extends AppCompatActivity {

    final static int BLUETOOTH_REQUEST_CODE = 1;

    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    BluetoothSPP bluetoothSPP;

    InputStream mInputStream = null;
    OutputStream mOutputStream = null;

    Handler mHandler;

    int readBufferPosition;
    byte[] readBuffer;
    Thread mWorkedThread;
    byte mDelimiter = 10;

    public ProgressDialog mDialog;

    Button btnBTOn;
    Button btnGetResult;
    TextView distance;

    private TextToSpeech txtSpeech;

    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino);

        txtSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != ERROR)
                {
                    txtSpeech.setLanguage(Locale.KOREAN);
                }
            }
        });

        btnBTOn = findViewById(R.id.btn_btOn);
        btnGetResult = findViewById(R.id.btn_getResult);
        distance = findViewById(R.id.dist);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> mDevice = bluetoothAdapter.getBondedDevices();
        bluetoothSPP = new BluetoothSPP(this);

        btnBTOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                    FuncVoiceOut("블루투스를 지원하지 않는 기기입니다.");
                }
                else {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
                        FuncVoiceOut("블루투스를 사용하려면 오른쪽 아래의 사용을 눌러주세요.");
                        btnBTOn.setText("블루투스 장치 선택");
                    }
                    else
                    {
                        if (mDevice.size() > 0) {
                            GetListDevicePaired();
                        }
                        btnBTOn.setText("블루투스 장치 선택");
                    }
                }
            }
        });

        /*
        bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            //데이터 수신되면
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(ArduinoActivity.this, message, Toast.LENGTH_SHORT).show(); // 토스트로 데이터 띄움
            }
        });


        bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                distance.setText(message);
            }
        });

         */

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case BLUETOOTH_REQUEST_CODE:
                if(resultCode == RESULT_OK)
                {
                    Set<BluetoothDevice> mDevice = bluetoothAdapter.getBondedDevices();

                    if (mDevice.size() > 0) {
                        GetListDevicePaired();
                    }

                }
                else if(resultCode == RESULT_CANCELED)
                {
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void GetListDevicePaired()
    {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        final int pairedDevicesCount = pairedDevices.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");
        FuncVoiceOut("블루투스 장치를 선택해주세요.");

        List<String> deviceLists = new ArrayList<>();
        for(BluetoothDevice device : pairedDevices)
        {
            deviceLists.add(device.getName());
        }
        final CharSequence[] items = deviceLists.toArray(new CharSequence[deviceLists.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == pairedDevicesCount)
                {
                    //
                }
                else
                {
                    SelectedDeviceConnect(items[i].toString());
                }
            }
        });

        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void SelectedDeviceConnect(final String deviceName)
    {
        bluetoothDevice = GetDeviceLists(deviceName);

        mDialog = new ProgressDialog(ArduinoActivity.this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Connecting...");
        mDialog.show();
        mDialog.setCancelable(false);

        Thread connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
                    bluetoothSocket.connect();

                    mInputStream = bluetoothSocket.getInputStream();
                    mOutputStream = bluetoothSocket.getOutputStream();

                    ReadData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    deviceName + "연결 완료", Toast.LENGTH_LONG).show();
                            FuncVoiceOut("블루투스 연결이 완료되었습니다.");
                            btnBTOn.setText("블루투스 OFF");
                            mDialog.dismiss();
                        }
                    });
                }
                catch (Exception e)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "블루투스 연결 오류",Toast.LENGTH_SHORT).show();
                            FuncVoiceOut("블루투스 연결 오류");
                        }
                    });
                }
            }
        });

        connect.start();
    }

    public BluetoothDevice GetDeviceLists(String name)
    {
        BluetoothDevice selectedDevice = null;
        for(BluetoothDevice device : pairedDevices)
        {
            if(name.equals(device.getName()))
            {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    void ReadData()
    {
        final Handler handler = new Handler();

        readBuffer = new byte[1024];
        readBufferPosition = 0;

        mWorkedThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for(int i = 0; i < bytesAvailable; i++)
                            {
                                byte b = packetBytes[i];
                                if(b == mDelimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes,0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            distance.setText(data + " m");
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }

                    }
                    catch (IOException ex)
                    {
                        finish();
                    }
                }
            }
        });

        mWorkedThread.start();
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

        try
        {
            mWorkedThread.interrupt();
            mInputStream.close();
            mOutputStream.close();
            bluetoothSocket.close();
        }
        catch (Exception e)
        {
            super.onDestroy();
        }
    }
}