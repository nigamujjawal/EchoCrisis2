package com.uj.echocrisismain;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    EditText userInput;
    View sendBtn;
    RecyclerView recyclerView;
    ChatAdapter adapter;
    List<Message> messageList = new ArrayList<>();

    // OkHttpClient with increased timeouts
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Time to connect
            .readTimeout(60, TimeUnit.SECONDS) // Time to read server response
            .writeTimeout(30, TimeUnit.SECONDS) // Time to send request
            .build();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Replace with your server's local IP (same network as Android device)
    private final String SERVER_URL = "http://10.136.60.246:5000/chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userInput = findViewById(R.id.userInput);
        sendBtn = findViewById(R.id.sendBtn);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendBtn.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessage(message, true);
                sendMessageToBot(message);
                userInput.setText("");
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new Message(text, isUser));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessageToBot(String message) {
        try {
            JSONObject json = new JSONObject();
            json.put("message", message);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            android.util.Log.d("ChatActivity", "Sending to: " + SERVER_URL);

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String errorMsg = "Connection Failed: " + e.getLocalizedMessage();
                    android.util.Log.e("ChatActivity", errorMsg, e);
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String resStr = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(resStr);
                            String reply = jsonResponse.getString("reply");

                            runOnUiThread(() -> addMessage(reply, false));
                        } catch (Exception e) {
                            runOnUiThread(() -> addMessage("Parsing error: " + e.getMessage(), false));
                        }
                    } else {
                        String failMsg = "Server Error: " + response.code();
                        android.util.Log.e("ChatActivity", failMsg);
                        runOnUiThread(() -> addMessage(failMsg, false));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Setup Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
