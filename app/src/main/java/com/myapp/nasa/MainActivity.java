package com.myapp.nasa;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final String API_KEY = "LblBHpe6fyA2Fwneu0PHBKmXds3yPXoE3fz87lpV";
    public nasa_api nasa_api;

    public Button mButton;

    public TextView textView;
    public ImageView mImage;
    public TextView title;
    public RelativeLayout RLayout;
    public Button restart;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.content);
        mImage = findViewById(R.id.mImage);
        mButton = findViewById(R.id.mButton);
        title = findViewById(R.id.Title);
        RLayout = findViewById(R.id.layoutR);
        restart = findViewById(R.id.restart);

        RLayout.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        mImage.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        restart.setVisibility(View.GONE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.nasa.gov/planetary/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        nasa_api = retrofit.create(nasa_api.class);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        (DatePickerDialog.OnDateSetListener) MainActivity.this,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            }
        });

    }

    public void onButtonClickVisibility() {
        RLayout.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        int today_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int today_month = Calendar.getInstance().get(Calendar.MONTH);
        int today_year = Calendar.getInstance().get(Calendar.YEAR);
        if (today_day < i2 && today_month <= i1+1 && today_year <= i) {
            Toast.makeText(this, "Please choose a date before today", Toast.LENGTH_SHORT).show();
            return;
        }
        mButton.setVisibility(View.GONE);
        Toast.makeText(this, "DATE SELECTED", Toast.LENGTH_SHORT).show();
        onButtonClickVisibility();
        getNasa(i, i1 + 1, i2);
    }

    public void getNasa(int year, int month, int day) {
        Log.i("getNasa()", "in the function");
        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
        Log.i("getNasa", "date: " + date);
        Call<nasa> call = nasa_api.getNasa("https://api.nasa.gov/planetary/apod?api_key=" + API_KEY + "&date=" + date);
        call.enqueue(new Callback<nasa>() {
            @Override
            public void onResponse(Call<nasa> call, Response<nasa> response) {
                if (!response.isSuccessful()) {
                    textView.setText("CODE: " + response.code());
                }

                nasa capture = response.body();
                String content = capture.getBody();
                String TITLE = capture.getTitle();

                RLayout.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                mImage.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                Log.i("getNasa()", "setting content" + capture.getTitle());
                textView.setText(content);
                title.setText(TITLE);
                restart.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        putImageOn(capture.getLink());
                    }
                }).start();


                Log.i("API CALLING", "SUCCESS");
            }

            @Override
            public void onFailure(Call<nasa> call, Throwable t) {
                textView.setText(t.getMessage());
            }
        });
    }


    public class Download extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                Log.i("DOWNLOAD", "TRY PART");
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap mBitmap = BitmapFactory.decodeStream(in);
                return mBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void putImageOn(String url) {
        Log.i("putImageOn", "iNTO iT");
        try {
            Download download = new Download();
            Bitmap bit = download.execute(url).get();
            mImage.setImageBitmap(bit);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void restartActivity(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startSecondActivity(View v) {
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

}