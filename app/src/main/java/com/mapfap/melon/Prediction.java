package com.mapfap.melon;

import android.util.Log;

/**
 * Created by mapfap on 3/30/16.
 */
public class Prediction {
    public String result;
    public String details;
    public int level;

    public Prediction(int level, String result, String details) {
        this.level = level;
        this.result = result;
        this.details = details;
    }

}
