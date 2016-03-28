/*
HAW Hamburg
Media Systems
SoSe 15
Mobile Systeme
Projekt: FreqRec

von Florian Langhorst & Maximilian Schön

FreqRec dient zur Aufnahme&Analyse von Audiofrequenzen

Vorgehensweise:

1. Start der App
2. Drücken des Aufnahmebuttons
3. Aufnahme des Tones für ca. 5 Sekunden
4. Drücken des Stoppbuttons
5. Gemessene Frequenz erscheint

Quellen: Progressbar: http://developer.samsung.com/technical-doc/view.do?v=T000000086
         FFT:http://wendykierp.github.io/JTransforms/apidocs/

 */
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

    private double maxValueF1 = 0;
    private int frequencyF1 = 0;

    private double maxValueR1 = 0;
    private int frequencyR1 = 0;

    private double maxValueR2 = 0;
    private int frequencyR2 = 0;

    private double maxValueR3 = 0;
    private int frequencyR3 = 0;

    private double maxValueR4 = 0;
    private int frequencyR4 = 0;

    private double maxValueF2 = 0;
    private int frequencyF2 = 0;

//    private Chronometer chrono;
//    private long time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize); // Recording via microphone

        final TextView rec = (TextView) findViewById(R.id.recText); // Start/Stop Text
        final TextView freq = (TextView) findViewById(R.id.freq);   // Result in Hz
        final ProgressBar pB = (ProgressBar) findViewById(R.id.volume); // Voice Volume
//        chrono =(Chronometer)findViewById(R.id.chrono); // Stopwatch, which shows the time while recording

        final ImageButton statusBtn = (ImageButton) findViewById(R.id.status); // Button to start and stop recording
        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                chrono.setBase(SystemClock.elapsedRealtime()+time); // Reset of the chronometer
//                chrono.start(); //Stopwatch starts

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

//                    String out = "\n";
//                    for (int i = 0; i < dataLength; i++) {
//                        out += i + ": " + magnitude[i] + "\n";
//                    }
//
//                    Log.d("out", out);

                    maxValueF1=0; // Reset maxValueF1
                    maxValueF2=0; // Reset maxValueF2
                    maxValueR1=0;
                    maxValueR2=0;
                    maxValueR3=0;
                    maxValueR4=0;

                    //out += frequency + ", " + magnitude[i] + "\n";
                    int dataLength = (FRAMES_PER_BUFFER / 2) - 1;
                    for (int i = 0; i < dataLength; i++) {
                        int frequency = (SAMPLE_RATE * i) / (FRAMES_PER_BUFFER * 2);

                        if (frequency >= 0 && frequency <= 22000 && magnitude[i] > maxValueF1) {
                            maxValueF1 = magnitude[i];
                            frequencyF1 = frequency;
                        }

                        if (frequency >= 190 && frequency <= 290 && magnitude[i] > maxValueR1) {
                            maxValueR1 = magnitude[i];
                            frequencyR1 = frequency;
                        }

                        if (frequency >= 240 && frequency <= 340 && magnitude[i] > maxValueR2) {
                            maxValueR2 = magnitude[i];
                            frequencyR2 = frequency;
                        }

                        if (frequency >= 340 && frequency <= 390 && magnitude[i] > maxValueR3) {
                            maxValueR3 = magnitude[i];
                            frequencyR3 = frequency;
                        }

                        if (frequency >= 390 && frequency <= 440 && magnitude[i] > maxValueR4) {
                            maxValueR4 = magnitude[i];
                            frequencyR4 = frequency;
                        }

                    }

                    String result = "";

                    if (frequencyF1 >= 120 && frequencyF1 < 170) {
                        frequencyF2 = frequencyR1;
                        maxValueF2 = maxValueR1;
                        double diff = Math.abs(frequencyF1 - frequencyF2);
                        if (diff >= 70 && diff <= 120) {
                            result = interpret(calFormula(frequencyF1, frequencyF2));
                        } else {
                            result = "ไม่ใช่แตงโม";
                        }
                    } else if (frequencyF1 >= 170 && frequencyF1 <= 220) {
                        frequencyF2 = frequencyR2;
                        maxValueF2 = maxValueR2;
                        double diff = Math.abs(frequencyF1 - frequencyF2);
                        if (diff >= 70 && diff <= 120) {
                            result = interpret(calFormula(frequencyF1, frequencyF2));
                        } else {
                            result = "ไม่ใช่แตงโม";
                        }
                    } else if (frequencyF1 >= 221 && frequencyF1 <= 280) {
                        result = "แตงโมดิบ";
                    } else {
                        result = "ไม่ใช่แตงโม";
                    }


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
        double a = 86380.40465 * 10E-10;
        double b = 123087.15045 * 10E-10;
        double c = 175269.93248 * 10E-10;
        double d = 1.82861 * 10E-10;
        return a * (f1*f1) + b * (f1*f2) + c * (f2*f2) + d;
    }
    //Finishing the activity
    @Override
    public void onDestroy() {
        audioRecord.release();

        super.onDestroy();
    }

}





















