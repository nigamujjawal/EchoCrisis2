package com.uj.echocrisismain;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class BankingHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banking_help); // your XML file name

        // Simple Guides
        findViewById(R.id.btnJanDhan).setOnClickListener(v ->
                openUrl("https://pmjdy.gov.in/"));

        findViewById(R.id.btnAadhaarLink).setOnClickListener(v ->
                openUrl("https://uidai.gov.in/my-aadhaar/update-aadhaar.html"));

        findViewById(R.id.btnUPISetup).setOnClickListener(v ->
                openUrl("https://www.npci.org.in/what-we-do/upi/product-overview"));

        // Locators
        findViewById(R.id.btnATMLocator).setOnClickListener(v ->
                openUrl("https://www.google.com/maps/search/ATM+near+me/"));

        findViewById(R.id.btnBankMitra).setOnClickListener(v ->
                openUrl("https://bankmitra.org/"));

        findViewById(R.id.btnCSC).setOnClickListener(v ->
                openUrl("https://findmycsc.nic.in/"));

        findViewById(R.id.btnLoanHelp).setOnClickListener(v ->
                openUrl("https://www.mudra.org.in/")) ;


    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}