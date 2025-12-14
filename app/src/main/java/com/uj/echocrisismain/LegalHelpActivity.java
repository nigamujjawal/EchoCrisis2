
package com.uj.echocrisismain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LegalHelpActivity extends AppCompatActivity {

    View btnGoToInsurance, btnFinancialHelp, btnMedicalHelp, btnBankingHelp;
    TextView txtWelcome;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_help);

        btnGoToInsurance = findViewById(R.id.btnGoToInsurance);
        btnFinancialHelp = findViewById(R.id.btnFinancialHelp);
        btnMedicalHelp = findViewById(R.id.btnMedicalHelp);
        btnBankingHelp = findViewById(R.id.btnBankingHelp);
        txtWelcome = findViewById(R.id.txtWelcome);

        // Receiving data from MainActivity
        String username = getIntent().getStringExtra("username");
        txtWelcome.setText("Welcome, " + username);

        btnGoToInsurance.setOnClickListener(v -> {
            android.util.Log.e("LegalHelp", "CLICKED: Insurance Claim Button");
            Toast.makeText(LegalHelpActivity.this, "Opening Insurance Claim...", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(LegalHelpActivity.this, InsuranceClaimActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                android.util.Log.e("LegalHelp", "StartActivity called");
            } catch (Exception e) {
                android.util.Log.e("LegalHelp", "ERROR starting activity: " + e.getMessage());
                e.printStackTrace();
            }
        });

        btnFinancialHelp.setOnClickListener(v -> {
            Toast.makeText(LegalHelpActivity.this, "Opening Financial Help...", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(LegalHelpActivity.this, FinancialHelpActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnMedicalHelp.setOnClickListener(v -> {
            Toast.makeText(LegalHelpActivity.this, "Opening Medical Help...", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(LegalHelpActivity.this, MedicalHelpActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnBankingHelp.setOnClickListener(v -> {
            Toast.makeText(LegalHelpActivity.this, "Opening Banking Help...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LegalHelpActivity.this, BankingHelpActivity.class);
            startActivity(intent);
        });
    }
}