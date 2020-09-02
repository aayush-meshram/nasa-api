package com.myapp.nasa;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        autoCompleteTextView = findViewById(R.id.actv);
        mText = findViewById(R.id.mText);
        mImage = findViewById(R.id.mImage);

        myList = new ArrayList<>();

        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!myList.get(position).nasa_id.isEmpty()) {
                    String nasa_id =myList.get(position).nasa_id;
                    String url = "https://images-api.nasa.gov/asset/"+nasa_id;
                    String img_url = getImageURL(url);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            putImageOn(img_url);
                        }
                    });

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
                    }
                }
                return false;
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

    public String thumb_url;
    public String getImageURL(String URL)   {
        RequestQueue mQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject collection = response.getJSONObject("collection");
                    JSONArray items = collection.getJSONArray("items");
                    JSONObject thumb = items.getJSONObject(0);
                    thumb_url = (String) thumb.get("href");
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
        return thumb_url;
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
                    Log.i("IN THE ", "response");
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
                        Log.i("TAAAG!", "onResponse: " + CONTENT.getTitle() + " id :" + CONTENT.getNasa_id());
                        CONTENT.setTitle(title);
                        CONTENT.setNasa_id(nasa_id);
                        stringList.add(CONTENT);
                        mList.add(title);
                        Log.i("CHECKING STRINGLIST", "CONTENT " + CONTENT + " title: " + CONTENT.getTitle());
                        Log.i("IN THE " + title, "title and nasa" + nasa_id + "content" + CONTENT);
                        System.out.println(stringList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //NOTIFYING CHANGE DONE IN DATASET CHANGED
                myList.addAll(stringList);
                autoSuggestAdapter.setData(mList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mQueue.add(request);
    }
}