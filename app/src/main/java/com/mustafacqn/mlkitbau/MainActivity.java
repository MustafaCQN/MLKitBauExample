package com.mustafacqn.mlkitbau;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.classification.MLImageClassification;
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer;
import com.huawei.hms.mlsdk.classification.MLLocalClassificationAnalyzerSetting;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.gesture.MLGesture;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzer;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzerFactory;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzerSetting;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int CAMERA_PERMISSION_CODE_IMAGECLASSY = 100;
    private final int CAMERA_PERMISSION_CODE_HANDGESTURE = 100;
    private final int CAMERA_REQUEST_IMAGECLASSY = 1000;
    private final int CAMERA_REQUEST_HANDGESTURE = 1100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Image Classification Button Click Event
    public void onButtonPressedImageClassy(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE_IMAGECLASSY);
        }else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_IMAGECLASSY);
        }
    }

    // Hand Gesture Button Click Event
    public void onButtonPressedHandGesture(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE_HANDGESTURE);
        }else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_HANDGESTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Image Classification Request Results
        if(requestCode == CAMERA_PERMISSION_CODE_IMAGECLASSY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_IMAGECLASSY);
            }else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }

            // Hand Gesture Request Results
        }else if (requestCode == CAMERA_PERMISSION_CODE_HANDGESTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_HANDGESTURE);
            }else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Image Classification Activity Result
        if (requestCode == CAMERA_REQUEST_IMAGECLASSY && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Kameradan gelen statik görseli enkapsüle eder.
            MLFrame frame = MLFrame.fromBitmap(photo);

            // MLLocalClassificationAnalyzerSettings den factory yardımı ile bir settings oluşturuyoruz. min kabul edilebilir yüzde = 80%
            MLLocalClassificationAnalyzerSetting setting = new MLLocalClassificationAnalyzerSetting.Factory()
                    .setMinAcceptablePossibility(0.8f)
                    .create();

            MLImageClassificationAnalyzer analyzer = MLAnalyzerFactory.getInstance().getLocalImageClassificationAnalyzer(setting);

            Task<List<MLImageClassification>> task = analyzer.asyncAnalyseFrame(frame);
            task.addOnSuccessListener(classifications -> {
                TextView resultTextView = findViewById(R.id.result_textview);
                if (classifications.size() > 0){
                    resultTextView.setText(classifications.get(0).getName());
                }else {
                    resultTextView.setText("Cannot recognize the Item");
                }
                try {
                    analyzer.stop();
                } catch (IOException e) {
                    Toast.makeText(this, "Analyzer Error on Stopping, Ex: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                    MLException mlException = (MLException)e;
                    int errorCode = mlException.getErrCode();
                    String errorMessage = mlException.getMessage();
                    Toast.makeText(this, "Conversation error, Ex: " + errorMessage, Toast.LENGTH_LONG).show();
            });

            // Hand Gesture Activity Result
        }else if (requestCode == CAMERA_REQUEST_HANDGESTURE && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Kameradan gelen statik görseli enkapsüle eder.
            MLFrame frame = new MLFrame.Creator().setBitmap(photo).create();

            MLGestureAnalyzerSetting setting = new MLGestureAnalyzerSetting.Factory().create();
            MLGestureAnalyzer analyzer = MLGestureAnalyzerFactory.getInstance().getGestureAnalyzer(setting);

            Task<List<MLGesture>> task = analyzer.asyncAnalyseFrame(frame);
            task.addOnSuccessListener(results -> {
                int resultCategory = results.get(0).getCategory();
                TextView resultTextView = findViewById(R.id.result_textview);
                switch (resultCategory) {
                    case 11:
                        resultTextView.setText("Thumbs Up");
                        break;
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        resultTextView.setText("Number is: " + (resultCategory+1));

                }
                analyzer.stop();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Detection Failed, Ex: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERRORGESTURE", "onActivityResult: " + e.getMessage());
            });


        }
    }
}