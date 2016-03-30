package com.mapfap.melon;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by mapfap on 3/30/16.
 */
public class Predictor {

    private static final double A = 8.64 * 10E-7;
    private static final double B = 1.23 * 10E-6;
    private static final double C = 1.75 * 10E-6;
    private static final double D = 1.83 * 10E-11;

    private double[] magnitude;
    private int sampleRate;
    private int framePerBuffer;

    public Predictor(double[] magnitude, int sampleRate, int framePerBuffer) {
        this.magnitude = magnitude;
        this.sampleRate = sampleRate;
        this.framePerBuffer = framePerBuffer;
    }

    public boolean isMelonDiff(int f1, int f2) {
        double diff = Math.abs(f1 - f2);
        return diff >= 70 && diff <= 120;
    }

    public Prediction predict() {
        int f1;
        int f2;

        f1 = maxOfRange(120, 220).freqency;

        if (isInRange(f1, 120, 170)) {
            f2 = maxOfRange(190, 290).freqency;
            if (isMelonDiff(f1, f2)) {
                Log.d("case1-1", "f1=" + f1 + "|f2=" + f2);
                return findRipeStatus(calFormula(f1, f2));
            }
        }

        if (isInRange(f1, 170, 220)) {
            f2 = maxOfRange(240, 340).freqency;
            if (isMelonDiff(f1, f2)) {
                Log.d("case1-2", "f1=" + f1 + "|f2=" + f2);
                return findRipeStatus(calFormula(f1, f2));
            }
        }


        f1 = maxOfRange(220, 320).freqency;

        if (isInRange(f1, 221, 270)) {
            f2 = maxOfRange(340, 390).freqency;
            if (isMelonDiff(f1, f2)) {
                Log.d("case2-1", "f1=" + f1 + "|f2=" + f2);
                return findRipeStatus(calFormula(f1, f2));
            }
        }

        if (isInRange(f1, 270, 320)) {
            f2 = maxOfRange(390, 440).freqency;
            if (isMelonDiff(f1, f2)) {
                Log.d("case2-2", "f1=" + f1 + "|f2=" + f2);
                return findRipeStatus(calFormula(f1, f2));
            }
        }

        return new Prediction(0, "ไม่ใช่แตงโม", "โปรดตรวจสอบสิ่งที่ทำการเคาะว่าเป็นแตงโมหรือไม่\n กรุณาทำการเคาะอีกครั้ง");

    }

    public Prediction findRipeStatus(double val) {
        Log.d("ripe-value", "" + val);
        if (val > 3.6) {
            return new Prediction(1, "ดิบ", "ดิบ ยังไม่เหมาะแก่การรับประทาน ควรเก็บแตงโมไว้อีก 3-5 วัน");
        } else if (val > 3.2 && val <= 3.6) {
            return new Prediction(2, "เริ่มสุก", "เริ่มสุก แตงโมยังสุกไม่เต็มที่ ควรเก็บไว้อีก 1-2 วัน");
        } else if (val > 2.7 && val <= 3.2) {
            return new Prediction(3, "สุกพอดี", "สุกกำลังพอดี เนื้อแตงโมมีความกรอบกำลังพอดี เป็นแตงโมที่เหมาะแก่การรับประทานมากที่สุด");
        } else if (val > 2.4 && val <= 2.7) {
            return new Prediction(4, "แก่", "สุกเกินไป สามารถรับประทานได้ แต่เนื้อแตงโมอาจมีความกรอบน้อย");
        } else if (val <= 2.4) {
            return new Prediction(5, "เน่า", "ไส้ล้มหรือไส้แตก แตงโมในลักษณะนี้ไม่เหมาะแก่การรับประทาน");
        } else {
            return new Prediction(0, "?", "เกิดข้อผิดพลาด กรุณาลองอีกครั้ง");
        }
    }

    public double calFormula(int f1, int f2) {
        return A * (f1*f1) + B * (f1*f2) + C * (f2*f2) + D;
    }

    public Point maxOfRange(int from, int to) {
        double maxValue = 0;
        int freq = 0;
        int dataLength = (framePerBuffer / 2) - 1;
        for (int i = 0; i < dataLength; i++) {
            int frequency = (sampleRate * i) / (framePerBuffer * 2);
            if (isInRange(frequency, from, to) && magnitude[i] > maxValue) {
                maxValue = magnitude[i];
                freq = frequency;
            }
        }
        return new Point(freq, maxValue);
    }

    public boolean isInRange(int x, int from, int to) {
        return x >= from && x <= to;
    }
}
