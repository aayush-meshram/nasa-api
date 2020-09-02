package com.myapp.nasa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    public AutoCompleteTextView autoCompleteTextView;
    public TextView mText;
    public ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        autoCompleteTextView = findViewById(R.id.actv);
        mText = findViewById(R.id.mText);
        mImage = findViewById(R.id.mImage);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

    }
}