package com.uj.echocrisismain;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private static final long WELCOME_DELAY_MS = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome); // Make sure you create this layout

        boolean profileExists = getIntent().getBooleanExtra("profileExists", false);

        new Handler().postDelayed(() -> {
            Intent intent;
            if (profileExists) {
                intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
            } else {
                intent = new Intent(WelcomeActivity.this, ProfileSetupActivity.class);
            }
            startActivity(intent);
            finish();
        }, WELCOME_DELAY_MS);
    }
}
