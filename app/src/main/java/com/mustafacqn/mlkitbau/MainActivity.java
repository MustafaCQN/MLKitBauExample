package com.mustafacqn.mlkitbau;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCapture;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureConfig;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureFactory;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;

import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "TextRecognitionActivity";
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);

        startCaptureActivity();
    }

    public void AnalyzeTextActivity(Bitmap bitmap) {
        MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                .setLanguage("en")
                .create();
        MLTextAnalyzer analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        Task<MLText> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(mlText -> {
            Log.i(TAG, "AnalyzeTextActivity: Success, Text: " + mlText.getStringValue());
            textView.setText(mlText.getStringValue());
            stopAnalyzer(analyzer);
        }).addOnFailureListener(e -> {
            Log.i(TAG, "AnalyzeTextActivity: failed, due to: " + e.getMessage());
            stopAnalyzer(analyzer);
        });
    }

    private void stopAnalyzer(MLTextAnalyzer analyzer) {
        try {
            analyzer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCaptureActivity() {
        MLBcrCaptureConfig config = new MLBcrCaptureConfig.Factory()
                .setOrientation(MLBcrCaptureConfig.ORIENTATION_AUTO)
                .create();

        MLBcrCapture bcrCapture = MLBcrCaptureFactory.getInstance().getBcrCapture(config);
        bcrCapture.captureFrame(this, callback);
    }

    MLBcrCapture.Callback callback = new MLBcrCapture.Callback() {
        @Override
        public void onSuccess(MLBcrCaptureResult mlBcrCaptureResult) {
            if (mlBcrCaptureResult == null) {
                Log.i(TAG, "onSuccess: ");
                return;
            }
            Bitmap bitmap = mlBcrCaptureResult.getOriginalBitmap();
            AnalyzeTextActivity(bitmap);
        }

        @Override
        public void onCanceled() {
            Log.i(TAG, "CallBackonRecCanceled");
        }

        @Override
        public void onFailure(int i, Bitmap bitmap) {
            Log.i(TAG, "CallBackonRecFailed");
        }

        @Override
        public void onDenied() {
            Log.i(TAG, "CallBackonCameraDenied");
        }
    };

}