package com.uj.echocrisismain;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCRProcessorActivity extends AppCompatActivity {

    private TextView textViewOCR;
    private static final String TAG = "OCRProcessor";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_processor);

        textViewOCR = findViewById(R.id.textViewOCR);

        Uri imageUri = getIntent().getData();
        if (imageUri != null) {
            processImage(imageUri);
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processImage(Uri uri) {
        try {
            Bitmap bitmap;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(this::displayText)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Text recognition failed", e);
                        Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayText(Text visionText) {
        StringBuilder extractedText = new StringBuilder();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            extractedText.append(block.getText()).append("\n\n");
        }

        if (extractedText.length() == 0) {
            textViewOCR.setText("No text detected in the image.");
        } else {
            textViewOCR.setText(extractedText.toString());
        }
    }
}