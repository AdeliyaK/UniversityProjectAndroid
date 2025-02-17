package com.example.serverconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        EditText email, password;
        CheckBox rememberMe;
        Button login;

        email = findViewById(R.id.Name);
        password = findViewById(R.id.Password);
        rememberMe = findViewById(R.id.RememberMe);
        login = findViewById(R.id.buttonLogin);

        SharedPreferences preferences=getSharedPreferences("checkbox", MODE_PRIVATE);
        String check=preferences.getString("rememberMe", "");
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
                if (buttonView.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Checked", Toast.LENGTH_SHORT).show();

                }else if(buttonView.isChecked()){
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Unchecked", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*
        autoCompleteTextView=findViewById(R.id.auto_complete_txt);
        adapterItems=new ArrayAdapter<String>(this,R.layout.list_item);

        autoCompleteTextView.setAdapter(adapterItems);
        adapterItems.addAll(Days);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(),item, Toast.LENGTH_SHORT).show();
            }
        });
    */
    }
}