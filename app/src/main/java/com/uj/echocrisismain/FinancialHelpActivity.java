package com.uj.echocrisismain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FinancialHelpActivity extends AppCompatActivity {

    Button btnGoonj, btnGiveIndia, btnSEEDS, btnKetto, btnMilaap, btnImpactGuru;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_help);

        // NGOs
        btnGoonj = findViewById(R.id.btnGoonj);
        btnGiveIndia = findViewById(R.id.btnGiveIndia);
        btnSEEDS = findViewById(R.id.btnSEEDS);

        // Crowdfunding
        btnKetto = findViewById(R.id.btnKetto);
        btnMilaap = findViewById(R.id.btnMilaap);
        btnImpactGuru = findViewById(R.id.btnImpactGuru);

        // Open URLs when clicked
        btnGoonj.setOnClickListener(v -> openWebsite("https://goonj.org/"));
        btnGiveIndia.setOnClickListener(v -> openWebsite("https://www.giveindia.org/"));
        btnSEEDS.setOnClickListener(v -> openWebsite("https://www.seedsindia.org/"));

        btnKetto.setOnClickListener(v -> openWebsite("https://www.ketto.org/"));
        btnMilaap.setOnClickListener(v -> openWebsite("https://milaap.org/"));
        btnImpactGuru.setOnClickListener(v -> openWebsite("https://www.impactguru.com/"));
    }

    private void openWebsite(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}