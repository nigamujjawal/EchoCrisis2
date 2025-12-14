package com.uj.echocrisismain;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SchemeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SchemesAdapter schemesAdapter;
    private SchemesDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scheme);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewSchemes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new SchemesDatabaseHelper(this);

        // Reload schemes from JSON
        loadLocalSchemes();

        // Get list from DB
        List<SchemeModel> schemesList = dbHelper.getAllSchemes();

        // ✅ Pass click listener using lambda, no interface implementation needed
        schemesAdapter = new SchemesAdapter(schemesList, scheme -> {
            Toast.makeText(this, "Clicked: " + scheme.getTitle(), Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(schemesAdapter);
    }

    private void loadLocalSchemes() {
        // Clear previous data
        dbHelper.clearAllSchemes();

        try {
            InputStream is = getResources().openRawResource(R.raw.schemes);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                dbHelper.insertScheme(
                        obj.getString("title"),
                        obj.getString("description"),
                        obj.getString("eligibility"),
                        obj.getString("link")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
