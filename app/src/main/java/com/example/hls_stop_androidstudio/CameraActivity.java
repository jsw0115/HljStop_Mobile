package com.example.hls_stop_androidstudio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.*;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.dnn.Dnn;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    final int MY_PERMISSION_REQUEST_CODE = 100;
    int counter = 0;

    AssetManager assetManager;

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;

    boolean startYolo = false;
    boolean firstTimeYolo = false;
    Net tinyYolo;

    public void YOLO(View Button)
    {
        if(startYolo == false) {
            startYolo = true;
            if (firstTimeYolo == false) {
                firstTimeYolo = true;

                String tinyYoloCfg = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny.cfg";
                String tinyYoloWeights = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny.weights";

                tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
            }
        }
        else
        {
            startYolo = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        int permission = ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA);
        if(permission == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        else
        {
            baseLoaderCallback = new BaseLoaderCallback(this) {
                @Override
                public void onManagerConnected(int status) {
                    super.onManagerConnected(status);

                    switch (status)
                    {
                        case BaseLoaderCallback.SUCCESS:
                            cameraBridgeViewBase.enableView();
                            break;
                        default:
                            super.onManagerConnected(status);
                            break;
                    }
                }
            };
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        if(requestCode == 0)
        {
            if(grantResults[0] == 0)
            {
                Toast.makeText(getApplicationContext(), "승인됨", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "거절됨", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();

        if(startYolo == true)
        {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            Mat imageBlob = Dnn.blobFromImage(frame, 0.00392,
                    new Size(416, 416), new Scalar(0, 0, 0), false, false);

            tinyYolo.setInput(imageBlob);
            tinyYolo.forward();
        }
        return frame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(), "There is an error.",Toast.LENGTH_SHORT).show();
        }
        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(cameraBridgeViewBase != null)
        {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null)
        {
            cameraBridgeViewBase.disableView();
        }
    }
}