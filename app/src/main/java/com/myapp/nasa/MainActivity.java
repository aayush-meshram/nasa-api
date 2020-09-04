package com.myapp.nasa;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final String API_KEY = "LblBHpe6fyA2Fwneu0PHBKmXds3yPXoE3fz87lpV";
    public nasa_api nasa_api;

    public ImageButton mButton;

    public TextView textView;
    public ImageView mImage;
    public TextView title;
    public RelativeLayout RLayout;
    public ImageButton restart;
    public ImageButton mAPOD;
    public ScrollView mScroll;
    public ImageButton secondButton;
    public ProgressBar mProgress;

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
        mAPOD = findViewById(R.id.apod);
        mScroll = findViewById(R.id.mScroll);
        secondButton = findViewById(R.id.task2);
        mProgress = findViewById(R.id.progress_circular);

        RLayout.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        mImage.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        restart.setVisibility(View.GONE);
        mButton.setVisibility(View.GONE);
        mScroll.setVisibility(View.GONE);
        secondButton.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);

        mAPOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButton.setVisibility(View.VISIBLE);
                secondButton.setVisibility(View.INVISIBLE);
                mAPOD.setVisibility(View.INVISIBLE);
                secondButton.setVisibility(View.INVISIBLE);

            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.nasa.gov/planetary/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        nasa_api = retrofit.create(nasa_api.class);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScroll.setVisibility(View.VISIBLE);
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
        if (today_day < i2 && today_month <= i1 + 1 && today_year <= i) {
            return;
        }
        mButton.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        onButtonClickVisibility();
        getNasa(i, i1 + 1, i2);
    }

    public void getNasa(int year, int month, int day) {
        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
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
                textView.setText(content);
                mProgress.setVisibility(View.GONE);
                title.setText(TITLE);
                restart.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this, "Loading Image, please wait", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        putImageOn(capture.getLink());
                    }
                }).start();

                Toast.makeText(MainActivity.this, "Press Retry to start again", Toast.LENGTH_SHORT).show();

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
        try {
            Download download = new Download();
            Bitmap bit = download.execute(url).get();
            mImage.setImageBitmap(bit);
            Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
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