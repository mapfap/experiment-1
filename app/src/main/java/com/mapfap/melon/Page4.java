package com.mapfap.melon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by mapfap on 3/30/16.
 */
public class Page4 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page4);
        TextView textView = (TextView) findViewById(R.id.textViewResult);
        String result = getIntent().getStringExtra("result");
        textView.setText(result);

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
