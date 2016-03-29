
package com.example.flo.freqrec;
import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jtransforms.fft.DoubleFFT_1D;


public class MainActivity extends Activity {


    private AudioRecord audioRecord;
    private DoubleFFT_1D fft;
    private Thread recordingThread;
    private boolean isRecording = false;

    private final int SAMPLE_RATE = 44100;
    private final int FRAMES_PER_BUFFER = 1024;
    private int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    private short[] buffer, sampleShortBuffer;
    private double magnitude[];
    private double MAGNITUDE_THRESHOLD = 10;
    private double sampleDoubleBuffer[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize); // Recording via microphone

        final TextView rec = (TextView) findViewById(R.id.recText); // Start/Stop Text
        final TextView freq = (TextView) findViewById(R.id.freq);   // Result in Hz
        final ProgressBar pB = (ProgressBar) findViewById(R.id.volume); // Voice Volume

        final ImageButton statusBtn = (ImageButton) findViewById(R.id.status); // Button to start and stop recording
        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRecording) {

                    fft = new DoubleFFT_1D(FRAMES_PER_BUFFER);
                    sampleShortBuffer = new short[FRAMES_PER_BUFFER];
                    sampleDoubleBuffer = new double[FRAMES_PER_BUFFER];
                    magnitude = new double [FRAMES_PER_BUFFER/2];
                    buffer = new short[bufferSize];

                    freq.setText(""); // Reset frequency-text before new measuring starts
//                    statusBtn.setImageResource(R.drawable.stoprec); // Change to Stop-Button
                    isRecording = true;
                    audioRecord.startRecording();// Microphone starts recording

                    recordingThread = new Thread(new Runnable() { //

                        @Override
                        public void run() {

                            try {


                                while (isRecording) {

                                    // Reading in from the recorded buffer and calculates the amplitude to set the progress to the progressbar
                                    double sum = 0;
                                    int readSize = audioRecord.read(buffer, 0, buffer.length);
                                    for (int i = 0; i < readSize; i++) {
                                        sum += buffer[i] * buffer[i];
                                    }
                                    if (readSize > 0) {
                                        final double amplitude = sum / readSize;
                                        pB.setProgress((int) Math.sqrt(amplitude));
                                    }

                                    audioRecord.read(sampleShortBuffer, 0, FRAMES_PER_BUFFER); // Gets microphone output to byte format
                                    convertToDouble(sampleShortBuffer, sampleDoubleBuffer); // Calling the function convertToDouble (see below)
                                    fft.realForward(sampleDoubleBuffer); //

                                    //Calculation to get the magnitude of the spectrum
                                    for (int i = 0; i < (FRAMES_PER_BUFFER / 2) - 1; i++) {
                                        double real = (2 * sampleDoubleBuffer[i]);
                                        double imag = (2 * sampleDoubleBuffer[i] + 1);
                                        double newMagnitude = Math.sqrt(real * real + imag * imag);
//                                        Log.d(".", newMagnitude + "|" + magnitude[i] + "|" + (newMagnitude > MAGNITUDE_THRESHOLD && newMagnitude > magnitude[i]));
                                        if (newMagnitude > MAGNITUDE_THRESHOLD && newMagnitude > magnitude[i]) {
                                            magnitude[i] = newMagnitude;
                                        }
                                    }

                                }

                            }finally {
//                                frequency = (SAMPLE_RATE * maxIndex) / (FRAMES_PER_BUFFER*2); // converts the largest peak to get the frequency
                                pB.setProgress(0); // Reset the ProgressBar

                            }

                        };

                    });
                    recordingThread.start();

                    rec.setTextColor(Color.parseColor("#FFFF002A"));// Change text-color
                    new CountDownTimer(5000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            rec.setText("" + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            stopRecording();
                        }
                    }.start();


                }
            }

            public boolean isInRange(int x, int from, int to) {
                return x >= from && x <= to;
            }

            public Point maxOfRange(int from, int to) {
                double maxValue = 0;
                int freq = 0;
                int dataLength = (FRAMES_PER_BUFFER / 2) - 1;
                for (int i = 0; i < dataLength; i++) {
                    int frequency = (SAMPLE_RATE * i) / (FRAMES_PER_BUFFER * 2);
                    if (isInRange(frequency, from, to) && magnitude[i] > maxValue) {
                        maxValue = magnitude[i];
                        freq = frequency;
                    }
                }

                return new Point(freq, maxValue);
            }

            public boolean isMelonDiff(int f1, int f2) {
                double diff = Math.abs(f1 - f2);
                return diff >= 70 && diff <= 120;
            }

            public String finding() {
                int f1;
                int f2;

                f1 = maxOfRange(120, 220).freqency;

                if (isInRange(f1, 120, 170)) {
                    f2 = maxOfRange(190, 290).freqency;
                    if (isMelonDiff(f1, f2)) {
                        Log.d("case1-1", "f1=" + f1 + "|f2=" + f2);
                        return interpret(calFormula(f1, f2));
                    }
                }

                if (isInRange(f1, 170, 220)) {
                    f2 = maxOfRange(240, 340).freqency;
                    if (isMelonDiff(f1, f2)) {
                        Log.d("case1-2", "f1=" + f1 + "|f2=" + f2);
                        return interpret(calFormula(f1, f2));
                    }
                }


                f1 = maxOfRange(220, 320).freqency;

                if (isInRange(f1, 221, 270)) {
                    f2 = maxOfRange(340, 390).freqency;
                    if (isMelonDiff(f1, f2)) {
                        Log.d("case2-1", "f1=" + f1 + "|f2=" + f2);
                        return interpret(calFormula(f1, f2));
                    }
                }

                if (isInRange(f1, 270, 320)) {
                    f2 = maxOfRange(390, 440).freqency;
                    if (isMelonDiff(f1, f2)) {
                        Log.d("case2-2", "f1=" + f1 + "|f2=" + f2);
                        return interpret(calFormula(f1, f2));
                    }
                }

                return "ไม่ใช่แตงโม";

            }

            private void stopRecording() {
                statusBtn.setImageResource(R.drawable.ic_record_audio); //Changes to Record Button
//                chrono.stop(); // Chronometer stops
                if (null != audioRecord) {
                    isRecording = false;
                    try {
                        recordingThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    recordingThread = null;
                    audioRecord.stop(); // Stop recording

                    rec.setText("Start");//Change Text
                    rec.setTextColor(Color.parseColor("#ff00ff01")); // Change text-color


                    String result = finding();


                    freq.setText(result); // Output of the recorded result

                }
            }

        });
    }



    //Converting the recorded shortbuffer to double values
    private void convertToDouble(short[] input, double[] output){
        double scale = 1 / 32768.0;
        for(int i = 0; i < input.length; i++){
            output[i] = input[i] * scale;
        }
    }

    public String interpret(double val) {
        Log.d("ripe-value", "" + val);
        if (val > 3.6) {
            return "ดิบ";
        } else if (val > 3.2 && val <= 3.6) {
            return "เริ่มสุก";
        } else if (val > 2.7 && val <= 3.2) {
            return "สุกพอดี";
        } else if (val > 2.4 && val <= 2.7) {
            return "แก่";
        } else if (val <= 2.4) {
            return "เน่า";
        } else {
            return "";
        }
    }

    public double calFormula(int f1, int f2) {
//        double a = 86380.40465 * 10E-10;
//        double b = 123087.15045 * 10E-10;
//        double c = 175269.93248 * 10E-10;
//        double d = 1.82861 * 10E-10;
        double a = 8.64 * 10E-6;
        double b = 1.23 * 10E-5;
        double c = 1.75 * 10E-5;
        double d = 1.83 * 10E-10;
        return a * (f1*f1) + b * (f1*f2) + c * (f2*f2) + d;
    }
    //Finishing the activity
    @Override
    public void onDestroy() {
        audioRecord.release();

        super.onDestroy();
    }

}

class Point {
    public int freqency;
    public double amplitude;

    public Point(int freqency, double amplitude) {
        this.freqency = freqency;
        this.amplitude = amplitude;
    }
}





















