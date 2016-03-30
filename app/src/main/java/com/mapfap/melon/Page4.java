package com.mapfap.melon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by mapfap on 3/30/16.
 */
public class Page4 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page4);
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        String details = intent.getStringExtra("details");
        int level = intent.getIntExtra("level", 0);

        TextView textView = (TextView) findViewById(R.id.textViewResult);
        textView.setText(result);

        EditText editText = (EditText) findViewById(R.id.editTextDetails);
        editText.setText(details);
        editText.setKeyListener(null);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarRipe);
        progressBar.setProgress(level * 20);

        Button button = (Button) findViewById(R.id.buttonRestart);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Page3.class);
                startActivity(intent);
            }
        });
    }
}
