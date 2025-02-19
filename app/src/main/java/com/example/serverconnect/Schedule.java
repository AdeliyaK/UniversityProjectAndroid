package com.example.serverconnect;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.services.storage.file.PropertyFile;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule extends AppCompatActivity {

    String []Days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private static final String SERVER_URL = "http://192.168.1.4:8000/";
    private static final String URL_DATA = "data/";
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    Button logout;
    TextView textView;
    private static final String TAG = "Push_Android";

    public class Lecture {
        private String title;
        private String type;
        private String professor;
        private String schedule;

        public Lecture(String title, String type, String professor, String schedule) {
            this.title = title;
            this.type = type;
            this.professor = professor;
            this.schedule = schedule;
        }

        public String getTitle() { return title; }
        public String getType() { return type; }
        public String getProfessor() { return professor; }
        public String getSchedule() { return schedule; }
    }


    private TableLayout tableLayout;
    private List<Lecture> lectureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        autoCompleteTextView=findViewById(R.id.auto_complete_txt);
        adapterItems=new ArrayAdapter<String>(this,R.layout.list_item);

        autoCompleteTextView.setAdapter(adapterItems);
        adapterItems.addAll(Days);



        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Log.d(TAG,"Clicked");
                requestData(item);



                tableLayout = findViewById(R.id.table); // Намираме TableLayout

                // Създаваме списък с лекции
                lectureList = new ArrayList<>();
                lectureList.add(new Lecture("Математика", "Лекция", "Доц. Иванов", "Понеделник 10:00"));
                lectureList.add(new Lecture("Физика", "Упражнение", "Проф. Петров", "Вторник 14:00"));
                lectureList.add(new Lecture("Програмиране", "Лекция", "Д-р Георгиева", "Сряда 09:00"));
                lectureList.add(new Lecture("Бази данни", "Лаборатория", "Инж. Стоянов", "Четвъртък 16:00"));
                lectureList.add(new Lecture("Мрежова сигурност", "Семинар", "Проф. Димитров", "Петък 11:30"));

                populateTable();



                Toast.makeText(getApplicationContext(),item, Toast.LENGTH_SHORT).show();
            }
        });

        logout=findViewById(R.id.buttonLogOut);
        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                finish();
            }
        });

    }
    private void populateTable() {
        for (final Lecture lecture : lectureList) {
            TableRow row = new TableRow(this);
            row.setPadding(10, 10, 10, 10);

            TextView titleView = new TextView(this);
            titleView.setText(lecture.getTitle());
            titleView.setGravity(Gravity.CENTER);
            titleView.setPadding(20, 10, 20, 10);

            TextView typeView = new TextView(this);
            typeView.setText(lecture.getType());
            typeView.setGravity(Gravity.CENTER);
            typeView.setPadding(20, 10, 20, 10);

            row.addView(titleView);
            row.addView(typeView);

            row.setOnClickListener(v -> {
                Gson gson = new Gson();
                String json = gson.toJson(lecture);

                Lecture clickedLecture = gson.fromJson(json, Lecture.class);

                String message = clickedLecture.getType() + ": " + clickedLecture.getTitle() +
                        "\nПреподавател: " + clickedLecture.getProfessor() +
                        "\nЧас: " + clickedLecture.getSchedule();

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            });

            tableLayout.addView(row);
        }
    }
    void requestData(String data){
        new Thread(() -> {
            try {
                // Отваряме връзка към сървъра
                URL url = new URL(SERVER_URL.concat(URL_DATA));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Създаваме JSON обект с токена
                JSONObject json = new JSONObject();
                json.put("email", "Tuka");
                json.put("password", "E");
                json.put("data",data);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                textView=findViewById(R.id.textView);
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

                /*
                int start = 0;
                int end;

                while (start < response.length() - 2) {
                    end = response.indexOf("],", start);

                    if (end == -1) {
                        end = response.length() - 2; // Ако няма повече "],", вземи до края
                    }
                    end--;
                    if(start==0)
                        start+=2;
                    else start+=3;
                    // Извличаме частта и я принтираме
                    String extracted = response.substring(start, end + 1);
                    Log.d(TAG, "Response: " + extracted);

                    start = end + 2; // Преминаване към следващия елемент
                }
 */
/*
                Pattern pattern = Pattern.compile("\\[(\\d+), (\\d+), (\\d{2}:\\d{2}:\\d{2})\\]");
                Matcher matcher = pattern.matcher(response);

                List<Table> t = new ArrayList<>();

                // Обхождане на намерените резултати
                while (matcher.find()) {
                    int f = Integer.parseInt(matcher.group(1));
                    int s = Integer.parseInt(matcher.group(2));
                    int th = Integer.parseInt(matcher.group(3));
                    String timeString = matcher.group(4); // Вземаме времето от група 3

                    // Преобразуваме времето в LocalTime
                    LocalTime l = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        l = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
                        t.add(new Table(f, s, th, l));
                    }

                    // Принтиране на резултата
                    System.out.println("Found time: " + l);
                }



 */


                runOnUiThread(() -> {
                    String formatted = response.toString()
                            .replaceAll("\\], \\[", "\n")  // Добавя нов ред между списъците
                            .replaceAll("\\[\\[", "\n")    // Нов ред след първия "[["
                            .replaceAll("\\]\\]", "\n");
                    textView.setText(formatted);
                });

                Log.d(TAG, "Token send response: " + responseCode + " | " + response.toString());

            } catch (Exception e) {
                Log.e(TAG, "Error sending data", e);
            }
        }).start();

    }
}