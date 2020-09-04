package com.myapp.nasa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;

    public AutoCompleteTextView autoCompleteTextView;
    public TextView mText;
    public ImageView mImage;
    public List<nasaInfo> myList;
    public AutoSuggestAdapter autoSuggestAdapter;
    public Handler handler;
    public String thumb_url;
    public ImageButton mRetry;
    public ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        autoCompleteTextView = findViewById(R.id.actv);
        mText = findViewById(R.id.mText);
        mImage = findViewById(R.id.mImage);
        mRetry = findViewById(R.id.mRetry);
        mProgress = findViewById(R.id.progress);

        mProgress.setVisibility(View.GONE);
        mRetry.setVisibility(View.GONE);

        myList = new ArrayList<>();

        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);


        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!myList.get(position).nasa_id.isEmpty()) {
                    Toast.makeText(MainActivity2.this, "Loading image, please wait for a short while", Toast.LENGTH_SHORT).show();
                    mRetry.setVisibility(View.VISIBLE);
                    mProgress.setVisibility(View.VISIBLE);
                    autoCompleteTextView.setVisibility(View.GONE);
                    String nasa_id =myList.get(position).nasa_id;
                    String url = "https://images-api.nasa.gov/asset/"+nasa_id;
                    getImageURL(url);
                }
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                autoCompleteTextView.clearListSelection();
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        makeApiCall(autoCompleteTextView.getText().toString());
                        //NOTIFYING AUTOCOMPLETE VIA HANDLER TO UPDATE LIST after TRIGGER_AUTO_COMPLETE milliseconds
                    }
                }
                return false;
            }
        });

    }

    public class Download12 extends AsyncTask<String, Void, Bitmap> {

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
            Download12 download = new Download12();
            Bitmap bit = download.execute(url).get();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mImage.setImageBitmap(bit);
                }
            });
            mProgress.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getImageURL(String URL)   {
        RequestQueue mQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject collection = response.getJSONObject("collection");
                    JSONArray items = collection.getJSONArray("items");
                    JSONObject thumb = items.getJSONObject(0);
                    thumb_url = (String) thumb.getString("href");
                    thumb_url = "https://" + thumb_url.substring(7);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            putImageOn(thumb_url);
                        }
                    }).start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    public void makeApiCall(String query) {
        String URL = "https://images-api.nasa.gov/search?q=" + query + "&media_type=image";
        RequestQueue mQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<nasaInfo> stringList = new ArrayList<>();
                List<String> mList = new ArrayList<>();
                try {
                    JSONObject collection = response.getJSONObject("collection");
                    JSONArray items = collection.getJSONArray("items");

                    for (int i = 0; i < 10; i++) {
                        if (items.length() < 10)
                            break;
                        JSONObject item = items.getJSONObject(i);
                        JSONArray data = item.getJSONArray("data");
                        JSONObject details = data.getJSONObject(0);

                        String title = details.getString("title");
                        String nasa_id = details.getString("nasa_id");
                        nasaInfo CONTENT = new nasaInfo(title, nasa_id);
                        CONTENT.title = title;
                        CONTENT.nasa_id = nasa_id;
                        CONTENT.setTitle(title);
                        CONTENT.setNasa_id(nasa_id);
                        stringList.add(CONTENT);
                        mList.add(title);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //NOTIFYING CHANGE DONE IN DATASET CHANGED
                myList.addAll(stringList);
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mQueue.add(request);
    }

    public void RestartApp(View v)  {
        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
        startActivity(intent);
    }
}