package com.mapfap.melon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
        String result = getIntent().getStringExtra("result");
        String details = getIntent().getStringExtra("details");

        TextView textView = (TextView) findViewById(R.id.textViewResult);
        textView.setText(result);

        EditText editText = (EditText) findViewById(R.id.editTextDetails);
        editText.setText(details);
        editText.setKeyListener(null);

        ImageView imageView = (ImageView) findViewById(R.id.imageViewResult);
        Picasso.with(this).load(R.drawable.resul200).fit().centerCrop().into(imageView);

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
