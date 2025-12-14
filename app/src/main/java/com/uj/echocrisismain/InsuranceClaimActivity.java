package com.uj.echocrisismain;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class InsuranceClaimActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText editTextDescription;
    private Spinner spinnerInsuranceType;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("InsuranceClaim", "Activity Starting...");
        try {
            setContentView(R.layout.activity_insurance_claim);
            android.util.Log.d("InsuranceClaim", "Layout Set Successfully");
        } catch (Exception e) {
            android.util.Log.e("InsuranceClaim", "Layout Error: " + e.getMessage());
            e.printStackTrace();
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        android.util.Log.d("InsuranceClaim", "Allocating Views...");
        spinnerInsuranceType = findViewById(R.id.spinnerInsuranceType);
        editTextDescription = findViewById(R.id.editTextDescription);
    }

    private void setupClickListeners() {
        Button buttonUploadPhotos = findViewById(R.id.buttonUploadPhotos);
        Button buttonDownloadTemplate = findViewById(R.id.buttonDownloadTemplate);
        Button buttonSubmitClaim = findViewById(R.id.buttonSubmitClaim);

        buttonUploadPhotos.setOnClickListener(v -> showImageOptions());
        buttonDownloadTemplate.setOnClickListener(v -> checkStoragePermissionAndDownload());
        buttonSubmitClaim.setOnClickListener(v -> submitClaim());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImage();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadTemplateFile();
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /*** -------- PHOTO UPLOAD / OCR -------- ***/
    private void showImageOptions() {
        String[] options = { "Capture from Camera", "Upload from Gallery" };

        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndCapture();
                    } else {
                        pickImageFromGallery();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA },
                    REQUEST_CAMERA_PERMISSION);
        } else {
            captureImage();
        }
    }

    private void captureImage() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Insurance_Claim_Photo");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Photo for insurance claim");

            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create image URI", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to capture image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void pickImageFromGallery() {
        try {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            if (pickIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
            } else {
                Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri selectedUri = null;

            if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                selectedUri = data.getData();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && imageUri != null) {
                selectedUri = imageUri;
            }

            if (selectedUri != null) {
                processImageForOCR(selectedUri);
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processImageForOCR(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            if (bitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                return;
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(this::handleOCRSuccess)
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "OCR failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Image processing failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void handleOCRSuccess(Text result) {
        if (result == null) {
            Toast.makeText(this, "No text result received", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder extractedText = new StringBuilder();
        for (Text.TextBlock block : result.getTextBlocks()) {
            if (block != null && block.getText() != null) {
                extractedText.append(block.getText()).append("\n");
            }
        }

        String finalText = extractedText.toString().trim();
        if (finalText.isEmpty()) {
            Toast.makeText(this, "No text found in image", Toast.LENGTH_SHORT).show();
        } else {
            // Append to existing text rather than replacing
            String existingText = editTextDescription.getText().toString();
            if (!existingText.isEmpty()) {
                editTextDescription.setText(existingText + "\n\n" + finalText);
            } else {
                editTextDescription.setText(finalText);
            }
            Toast.makeText(this, "Text extracted successfully", Toast.LENGTH_SHORT).show();
        }
    }

    /*** -------- TEMPLATE DOWNLOAD -------- ***/
    private void checkStoragePermissionAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ doesn't need WRITE_EXTERNAL_STORAGE for app-specific directories
            downloadTemplateFile();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        REQUEST_STORAGE_PERMISSION);
            } else {
                downloadTemplateFile();
            }
        }
    }

    private void downloadTemplateFile() {
        try {
            String templateText = generateClaimTemplate();

            File downloadsDir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                downloadsDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
            } else {
                downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }

            if (!downloadsDir.exists()) {
                boolean created = downloadsDir.mkdirs();
                if (!created) {
                    Toast.makeText(this, "Failed to create downloads directory", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String fileName = "InsuranceClaimTemplate_" + System.currentTimeMillis() + ".txt";
            File file = new File(downloadsDir, fileName);

            try (OutputStream os = new FileOutputStream(file)) {
                os.write(templateText.getBytes());
                os.flush();
            }

            Toast.makeText(this, "Template saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to save template: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String generateClaimTemplate() {
        return "INSURANCE CLAIM LETTER TEMPLATE\n\n" +
                "Date: _______________\n\n" +
                "To: [Insurance Company Name]\n" +
                "Address: [Insurance Company Address]\n\n" +
                "Subject: Insurance Claim Request - Policy No: _______________\n\n" +
                "Dear Claims Department,\n\n" +
                "I am writing to formally request an insurance claim under my policy number _______________.\n\n" +
                "INCIDENT DETAILS:\n" +
                "Date of Incident: _______________\n" +
                "Time of Incident: _______________\n" +
                "Location: _______________\n\n" +
                "DESCRIPTION OF DAMAGE/LOSS:\n" +
                "_______________________________________________\n" +
                "_______________________________________________\n" +
                "_______________________________________________\n\n" +
                "ESTIMATED COST OF DAMAGES: $_______________\n\n" +
                "I have attached the following supporting documents:\n" +
                "☐ Photos of damage\n" +
                "☐ Police report (if applicable)\n" +
                "☐ Receipts/invoices\n" +
                "☐ Other: _______________\n\n" +
                "Please process this claim at your earliest convenience. I can be reached at:\n" +
                "Phone: _______________\n" +
                "Email: _______________\n\n" +
                "Thank you for your prompt attention to this matter.\n\n" +
                "Sincerely,\n\n" +
                "_______________\n" +
                "[Your Full Name]\n" +
                "[Your Address]\n" +
                "[City, State, ZIP]\n";
    }

    /*** -------- CLAIM SUBMISSION -------- ***/
    private void submitClaim() {
        String description = editTextDescription.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(this, "Please provide a claim description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerInsuranceType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select an insurance type", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Submit Claim")
                .setMessage("Are you sure you want to submit this claim?\n\nInsurance Type: "
                        + spinnerInsuranceType.getSelectedItem().toString()
                        + "\n\nDescription: " + description.substring(0, Math.min(description.length(), 100))
                        + (description.length() > 100 ? "..." : ""))
                .setPositiveButton("Submit", (dialog, which) -> {
                    // TODO: Implement actual server submission
                    performClaimSubmission();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performClaimSubmission() {
        // Simulate submission process
        Toast.makeText(this, "Submitting claim...", Toast.LENGTH_SHORT).show();

        // In real implementation: upload claim to server here
        // For now, just clear the form after successful "submission"
        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(this, "Claim submitted successfully! Reference ID: CLM" + System.currentTimeMillis(),
                    Toast.LENGTH_LONG).show();
            clearForm();
        }, 2000);
    }

    private void clearForm() {
        editTextDescription.setText("");
        spinnerInsuranceType.setSelection(0);
        imageUri = null;
    }
}