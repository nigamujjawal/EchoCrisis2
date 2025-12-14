package com.uj.echocrisismain;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.telephony.SmsManager;
import android.app.PendingIntent;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class DashboardActivity extends AppCompatActivity {

    Button btnSOS;
    View btnMentalSupport, btnLegalHelp, btnShowScheme, btnSuggestShelter, btnManageContacts;
    private static final String TAG = "DashboardActivity";

    // 🔴 Emergency number default (India)
    private static final String EMERGENCY_NUMBER = "112";

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> locationPermissionLauncher;
    private ActivityResultLauncher<String> callPermissionLauncher;
    private ActivityResultLauncher<String> smsPermissionLauncher; // NEW

    private static final String PREFS_SOS = "sos_prefs";
    private static final String KEY_PENDING_SOS = "pending_sos"; // simple delimited queue

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Store last known location for SMS message
    private Double lastLatitude = null;
    private Double lastLongitude = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnSOS = findViewById(R.id.btnSOS);
        btnMentalSupport = findViewById(R.id.btnMentalSupport);
        btnLegalHelp = findViewById(R.id.btnLegalHelp);
        btnShowScheme = findViewById(R.id.btnShowScheme);
        btnSuggestShelter = findViewById(R.id.btnSuggestShelter);

        // Location permission launcher
        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        sendSosWithLocation();
                    } else {
                        Toast.makeText(this,
                                "Location denied — sending SOS without location",
                                Toast.LENGTH_SHORT).show();
                        sendSosWithoutLocation();
                    }
                });

        // Call permission launcher
        callPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        makeEmergencyCall();
                    } else {
                        Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // SMS permission launcher
        smsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted, send SMS then check call
                        sendSmsIfCustomNumber();
                        checkCallPermission();
                    } else {
                        Toast.makeText(this, "SMS permission denied - calling only", Toast.LENGTH_SHORT).show();
                        checkCallPermission();
                    }
                });

        // SOS button
        btnSOS.setOnClickListener(v -> showSosConfirmationDialog());

        // Manage Emergency Contacts button
        btnManageContacts = findViewById(R.id.btnManageContacts);
        btnManageContacts.setOnClickListener(v -> startActivity(new Intent(this, EmergencyContactsActivity.class)));

        btnMentalSupport.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        btnLegalHelp.setOnClickListener(v -> startActivity(new Intent(this, LegalHelpActivity.class)));

        btnShowScheme.setOnClickListener(v -> startActivity(new Intent(this, SchemeActivity.class)));

        btnSuggestShelter.setOnClickListener(v -> startActivity(new Intent(this, ShelterActivity.class)));

        // Try to flush any pending SOS saved while offline
        flushPendingSosIfAny();
    }

    private void initiateSosSequence() {
        Log.d(TAG, "initiateSosSequence called");
        Toast.makeText(this, "Starting SOS sequence...", Toast.LENGTH_SHORT).show();

        // Load emergency contacts list
        List<EmergencyContact> contacts = loadEmergencyContacts();
        Log.d(TAG, "Loaded " + contacts.size() + " emergency contacts");
        Toast.makeText(this, "Found " + contacts.size() + " emergency contacts", Toast.LENGTH_SHORT).show();

        if (!contacts.isEmpty()) {
            // Has emergency contacts -> Send SMS to all then call first contact
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "SMS permission already granted, sending SMS");
                sendSmsToEmergencyContacts(contacts);
                checkCallPermission();
            } else {
                Log.d(TAG, "Requesting SMS permission");
                Toast.makeText(this, "Requesting SMS permission...", Toast.LENGTH_SHORT).show();
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            }
        } else {
            // No contacts saved -> Default 112 call only
            Log.d(TAG, "No emergency contacts, calling 112");
            Toast.makeText(this, "No contacts - calling 112", Toast.LENGTH_SHORT).show();
            checkCallPermission();
        }
    }

    private List<EmergencyContact> loadEmergencyContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_SOS, MODE_PRIVATE);
        String contactsJson = prefs.getString("emergency_contacts", "[]");

        try {
            JSONArray jsonArray = new JSONArray(contactsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                contacts.add(EmergencyContact.fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading emergency contacts", e);
        }

        return contacts;
    }

    // Confirmation dialog
    private void showSosConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Emergency SOS")
                .setMessage("Are you sure you want to send an emergency alert?")
                .setPositiveButton("SEND SOS", (dialog, which) -> checkLocationPermission())
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            sendSosWithLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void sendSosWithLocation() {
        Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            lastLatitude = location.getLatitude();
                            lastLongitude = location.getLongitude();
                            Log.d(TAG, "Location obtained: " + lastLatitude + ", " + lastLongitude);
                            Toast.makeText(this, "Location found! Sending SOS...", Toast.LENGTH_SHORT).show();
                            // IMMEDIATELY send SMS and make call
                            initiateSosSequence();
                            // Then save to Firestore in background
                            createSosDocument(lastLatitude, lastLongitude);
                        } else {
                            Log.w(TAG, "Last location is null - sending without location");
                            Toast.makeText(this, "Location not available - sending SOS without location",
                                    Toast.LENGTH_LONG).show();
                            sendSosWithoutLocation();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Location failed: " + e.getMessage(), e);
                        Toast.makeText(this, "Failed to get location: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        sendSosWithoutLocation();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting location", e);
            Toast.makeText(this, "Location permission error", Toast.LENGTH_SHORT).show();
            sendSosWithoutLocation();
        }
    }

    private void sendSosWithoutLocation() {
        lastLatitude = null;
        lastLongitude = null;
        Log.d(TAG, "Sending SOS without location");
        // IMMEDIATELY send SMS and make call
        initiateSosSequence();
        // Then save to Firestore in background
        createSosDocument(null, null);
    }

    // New: createSosDocument delegates to attemptWriteSos which handles retry/local
    // queue
    private void createSosDocument(Double latitude, Double longitude) {
        // Only attempt server write when user is signed-in. If not, queue locally and
        // inform user.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // No authenticated user -> queue SOS locally instead of trying to write to
            // Firestore
            Map<String, Object> data = new HashMap<>();
            String uid = "anonymous";
            data.put("uid", uid);
            data.put("timestamp", System.currentTimeMillis());
            data.put("status", "requested");

            SharedPreferences prefs = getSharedPreferences(PREFS_SOS, MODE_PRIVATE);
            String emergencyNumber = prefs.getString("emergency_number", EMERGENCY_NUMBER);
            data.put("emergency_number", emergencyNumber);

            if (latitude != null && longitude != null) {
                Map<String, Object> loc = new HashMap<>();
                loc.put("lat", latitude);
                loc.put("lng", longitude);
                data.put("location", loc);
            }

            savePendingSos(data);
            Toast.makeText(this, "You are not signed in. SOS queued locally and SMS/Call proceeded.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SharedPreferences prefs = getSharedPreferences(PREFS_SOS, MODE_PRIVATE);
        String emergencyNumber = prefs.getString("emergency_number", EMERGENCY_NUMBER);

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("timestamp", System.currentTimeMillis());
        data.put("status", "requested");
        data.put("emergency_number", emergencyNumber);

        if (latitude != null && longitude != null) {
            Map<String, Object> loc = new HashMap<>();
            loc.put("lat", latitude);
            loc.put("lng", longitude);
            data.put("location", loc);
        }

        // If no network, queue locally but still proceed with SMS/Call
        if (!isNetworkAvailable()) {
            savePendingSos(data);
            Toast.makeText(this, "No network — SOS queued but SMS/Call proceeding", Toast.LENGTH_LONG).show();
            return;
        }

        attemptWriteSos(data, 1);
    }

    // attemptWriteSos - retries up to 3 times for transient UNAVAILABLE errors with
    // backoff
    private void attemptWriteSos(Map<String, Object> data, int attempt) {
        db.collection("sos_requests")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "SOS logged to server", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "SOS ID: " + doc.getId());
                    // SMS/Call already initiated, no need to call again
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "SOS failed (attempt " + attempt + ")", e);
                    boolean shouldRetry = false;

                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException ffe = (FirebaseFirestoreException) e;
                        // If PERMISSION_DENIED, do not retry - likely security rules or auth issue
                        if (ffe.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w(TAG, "Permission denied writing SOS - will queue locally and stop retries");
                            savePendingSos(data);
                            Toast.makeText(this, "Unable to log SOS to server (permission denied) — queued locally",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Retry only on UNAVAILABLE or ABORTED transient errors
                        if ((ffe.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE
                                || ffe.getCode() == FirebaseFirestoreException.Code.ABORTED) && attempt < 3) {
                            shouldRetry = true;
                        }
                    }

                    if (shouldRetry) {
                        int next = attempt + 1;
                        long delayMs = 1000L * next; // simple linear backoff
                        Log.w(TAG, "Retrying SOS in " + delayMs + "ms (attempt " + next + ")");
                        mainHandler.postDelayed(() -> attemptWriteSos(data, next), delayMs);
                    } else {
                        // If permanent failure or retries exhausted, save locally for later
                        savePendingSos(data);
                        Toast.makeText(this, "Failed to send SOS — queued locally", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Save pending SOS to SharedPreferences as a simple delimited queue
    private void savePendingSos(Map<String, Object> data) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_SOS, MODE_PRIVATE);
            String existing = prefs.getString(KEY_PENDING_SOS, "");
            String serialized = new JSONObject(data).toString();
            String combined = existing.isEmpty() ? serialized : existing + "||" + serialized;
            prefs.edit().putString(KEY_PENDING_SOS, combined).apply();
            Log.d(TAG, "Saved pending SOS locally");
        } catch (Exception ex) {
            Log.e(TAG, "Failed to save pending SOS", ex);
        }
    }

    // Try to flush any pending SOS entries (called on app start)
    private void flushPendingSosIfAny() {
        if (!isNetworkAvailable())
            return;
        SharedPreferences prefs = getSharedPreferences(PREFS_SOS, MODE_PRIVATE);
        String queue = prefs.getString(KEY_PENDING_SOS, "");
        if (queue == null || queue.isEmpty())
            return;

        String[] items = queue.split("\\|\\|"); // split on ||
        for (String s : items) {
            try {
                JSONObject json = new JSONObject(s);
                Map<String, Object> map = jsonObjectToMap(json);
                attemptWriteSos(map, 1);
            } catch (Exception ex) {
                Log.e(TAG, "Invalid pending SOS item", ex);
            }
        }
        prefs.edit().remove(KEY_PENDING_SOS).apply();
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null)
                return false;
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } catch (Exception e) {
            Log.e(TAG, "Network check failed", e);
            return false;
        }
    }

    private void checkCallPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            makeEmergencyCall();
        } else {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void sendSmsToEmergencyContacts(List<EmergencyContact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            Log.d(TAG, "No emergency contacts to send SMS to");
            Toast.makeText(this, "No emergency contacts found!", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();

        // Build SIMPLE message (some carriers block emojis/long messages)
        String message = "EMERGENCY! I need help urgently. SOS from EchoCrisis app.";

        if (lastLatitude != null && lastLongitude != null) {
            message += " Location: https://maps.google.com/?q=" + lastLatitude + "," + lastLongitude;
        }

        Log.d(TAG, "SMS Message: " + message);
        Log.d(TAG, "Message length: " + message.length() + " chars");
        Toast.makeText(this, "Sending SMS to " + contacts.size() + " contacts...", Toast.LENGTH_SHORT).show();

        final int[] sentCount = { 0 };
        final int[] failCount = { 0 };
        final int totalContacts = contacts.size();

        for (EmergencyContact contact : contacts) {
            String phone = contact.getPhone();
            Log.d(TAG, "Processing contact: " + contact.getName() + " - Phone: '" + phone + "'");

            if (phone == null || phone.trim().isEmpty()) {
                Log.e(TAG, "Empty phone number for " + contact.getName());
                failCount[0]++;
                continue;
            }

            // Clean phone number - remove spaces, dashes
            phone = phone.replaceAll("[\\s\\-()]", "");
            Log.d(TAG, "Cleaned phone: '" + phone + "'");

            if (phone.length() < 10) {
                Log.e(TAG, "Phone number too short: " + phone);
                Toast.makeText(this, "Invalid number: " + phone, Toast.LENGTH_SHORT).show();
                failCount[0]++;
                continue;
            }

            try {
                // Create sent PendingIntent to track delivery
                String SENT_ACTION = "SMS_SENT_" + System.currentTimeMillis();
                Intent sentIntent = new Intent(SENT_ACTION);
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                final String finalPhone = phone;
                final String contactName = contact.getName();

                // Register receiver for this SMS
                BroadcastReceiver sentReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                Log.d(TAG, "SMS actually sent to: " + contactName + " (" + finalPhone + ")");
                                runOnUiThread(() -> Toast.makeText(context,
                                        "SMS sent to " + contactName, Toast.LENGTH_SHORT).show());
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Log.e(TAG, "SMS FAILED - Generic failure for " + finalPhone);
                                runOnUiThread(() -> Toast.makeText(context,
                                        "SMS FAILED (Generic) to " + contactName, Toast.LENGTH_LONG).show());
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Log.e(TAG, "SMS FAILED - No service for " + finalPhone);
                                runOnUiThread(() -> Toast.makeText(context,
                                        "SMS FAILED - No Service!", Toast.LENGTH_LONG).show());
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Log.e(TAG, "SMS FAILED - Null PDU for " + finalPhone);
                                runOnUiThread(() -> Toast.makeText(context,
                                        "SMS FAILED (Null PDU)", Toast.LENGTH_LONG).show());
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                Log.e(TAG, "SMS FAILED - Radio off for " + finalPhone);
                                runOnUiThread(() -> Toast.makeText(context,
                                        "SMS FAILED - Radio Off!", Toast.LENGTH_LONG).show());
                                break;
                            default:
                                Log.e(TAG, "SMS FAILED - Unknown error: " + getResultCode());
                                break;
                        }
                        try {
                            context.unregisterReceiver(this);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                };

                registerReceiver(sentReceiver, new IntentFilter(SENT_ACTION), Context.RECEIVER_NOT_EXPORTED);

                // Send the SMS
                Log.d(TAG, "Calling sendTextMessage to: " + phone);
                smsManager.sendTextMessage(phone, null, message, sentPI, null);
                sentCount[0]++;
                Log.d(TAG, "sendTextMessage called successfully for: " + phone);

            } catch (Exception e) {
                Log.e(TAG, "Exception sending SMS to " + phone + ": " + e.getMessage(), e);
                Toast.makeText(this, "Error sending to " + contact.getName() + ": " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                failCount[0]++;
            }
        }

        Log.d(TAG, "SMS sending complete. Sent: " + sentCount[0] + ", Failed: " + failCount[0]);
        if (sentCount[0] > 0) {
            Toast.makeText(this, "SOS SMS sent to " + sentCount[0] + " contact(s)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to send any SMS! Check numbers.", Toast.LENGTH_LONG).show();
        }
    }

    // Legacy method for backward compatibility with single emergency number
    private void sendSmsIfCustomNumber() {
        // First try to use new contacts list
        List<EmergencyContact> contacts = loadEmergencyContacts();
        if (!contacts.isEmpty()) {
            sendSmsToEmergencyContacts(contacts);
            return;
        }

        // Fallback to legacy single number
        SharedPreferences prefs = getSharedPreferences(PREFS_SOS, MODE_PRIVATE);
        String emergencyNumber = prefs.getString("emergency_number", EMERGENCY_NUMBER);

        if (!emergencyNumber.equals(EMERGENCY_NUMBER)) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                String message = "🚨 EMERGENCY! I need help urgently.";
                smsManager.sendTextMessage(emergencyNumber, null, message, null, null);
                Toast.makeText(this, "SMS sent to " + emergencyNumber, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "SMS failed", e);
                Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makeEmergencyCall() {
        // Use saved emergency number or default
        SharedPreferences prefs = getSharedPreferences(PREFS_SOS, MODE_PRIVATE);
        String numberToCall = prefs.getString("emergency_number", EMERGENCY_NUMBER);

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + numberToCall));
        startActivity(intent);
    }

    // Convert JSONObject to Map recursively
    private Map<String, Object> jsonObjectToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        if (json == null)
            return map;
        java.util.Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }
            map.put(key, value);
        }
        return map;
    }

    // Convert JSONArray to List recursively
    private java.util.List<Object> jsonArrayToList(JSONArray array) throws JSONException {
        java.util.List<Object> list = new java.util.ArrayList<>();
        if (array == null)
            return list;
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }
            list.add(value);
        }
        return list;
    }
}
