package com.example.hls_stop_androidstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class ArduinoActivity extends AppCompatActivity {

    final static int BLUETOOTH_REQUEST_CODE = 1;

    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    BluetoothSPP bluetoothSPP;

    InputStream mInputStream = null;
    OutputStream mOutputStream = null;

    public ProgressDialog mDialog;

    Button btnBTOn;
    Button btnGetResult;

    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino);

        btnBTOn = findViewById(R.id.btn_btOn);
        btnGetResult = findViewById(R.id.btn_getResult);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> mDevice = bluetoothAdapter.getBondedDevices();

        btnBTOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
                        btnBTOn.setText("블루투스 끄기");
                    }
                    else
                    {

                    }
                }

            }
        });

        btnGetResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener()
                {
                    public void onDataReceived(byte[] data, String text)
                    {
                        TextView temp = findViewById(R.id.temp);
                        TextView humd = findViewById(R.id.humd);

                        String[] array = text.split(",");

                        temp.setText(array[0].concat("C"));
                        humd.setText(array[1].concat("%") );

                    }
                });
            }
        });
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    deviceName + "연결 완료", Toast.LENGTH_LONG).show();

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

    void ReceptionData()
    {
        final Handler handler = new Handler();


    }

}