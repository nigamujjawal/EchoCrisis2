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
        setContentView(R.layout.activity_welcome);

        android.widget.Button btnGetStarted = findViewById(R.id.btnGetStarted);
        android.widget.Button btnSignIn = findViewById(R.id.btnSignIn);

        // Since we are coming from Login, we are already signed in.
        // Hide the "Sign In" button if it exists, or just repurpose the screen.
        // For now, let's just make 'Get Started' go to Dashboard.

        if (btnSignIn != null) {
            btnSignIn.setVisibility(android.view.View.GONE);
        }

        // Auto-navigate after 2 seconds
        new Handler().postDelayed(() -> {
            // Check if activity is still valid (not finished by button click)
            if (!isFinishing()) {
                Intent intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // 2 seconds delay

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
