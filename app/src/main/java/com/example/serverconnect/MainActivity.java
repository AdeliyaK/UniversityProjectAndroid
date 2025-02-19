package com.example.serverconnect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    static EditText email, password;
    static CheckBox rememberMe;
    static Button login;

    private static final String TAG = "Push_Android";
    private static final String SERVER_URL = "http://192.168.1.4:8000/";
    private static final String URL_TOKEN = "register-token/";

    static boolean b=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);


        requestNotificationPermission();

        email = findViewById(R.id.Name);
        password = findViewById(R.id.Password);
        rememberMe = findViewById(R.id.RememberMe);
        login = findViewById(R.id.buttonLogin);

        SharedPreferences preferences=getSharedPreferences("checkbox", MODE_PRIVATE);
        String check=preferences.getString("remember", "");


        if(check.equals("true")){
            Intent intent = new Intent(MainActivity.this, Schedule.class);
            startActivity(intent);

        }else if(check.equals("false")){
            Toast.makeText(getApplicationContext(), "Please Login", Toast.LENGTH_SHORT).show();
        }

        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Schedule.class);
                startActivity(intent);
            }
        });

        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();

                if (isChecked) {
                    editor.putString("remember", "true");
                    Toast.makeText(getApplicationContext(), "Checked", Toast.LENGTH_SHORT).show();

                } else {
                    editor.putString("remember", "false");
                    Toast.makeText(getApplicationContext(), "Unchecked", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, preferences.getString("remember", ""));
                }


                editor.apply();
            }
        });


    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                getFCMToken(); // Само ако разрешението е дадено
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        } else {
            getFCMToken(); // За Android < 13 не се изисква разрешение
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Вземи токена
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);


                    // Изпрати токена към сървъра
                    sendTokenToServer(token);
                });
    }





    static void sendTokenToServer(String token) {
        new Thread(() -> {
            try {
                // Отваряме връзка към сървъра
                URL url = new URL(SERVER_URL.concat(URL_TOKEN));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Създаваме JSON обект с токена
                JSONObject json = new JSONObject();
                json.put("token", token);
                json.put("email", email.getText().toString());
                json.put("password", password.getText().toString());

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                Log.d(TAG, "Token send request: " + json.toString());
                // Четем отговора от сървъра
                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d(TAG, "Token send response: " + responseCode + " | " + response.toString());

            } catch (Exception e) {
                Log.e(TAG, "Error sending token", e);
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted!");
            } else {
                Log.d(TAG, "Notification permission denied!");
            }
        }
    }
}