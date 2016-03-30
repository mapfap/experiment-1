package com.mapfap.melon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.squareup.picasso.Picasso;

/**
 * Created by mapfap on 3/30/16.
 */
public class Page2 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page2);
        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton2);
        Picasso.with(this).load(R.drawable.howto).fit().centerCrop().into(imageButton);

        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Page3.class);
                startActivity(intent);
            }
        });
    }
}