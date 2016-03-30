package com.mapfap.melon;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Created by mapfap on 3/30/16.
 */
public class Page3 extends Activity {


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

    private TextView rec;
    private ProgressBar pB;

    public void onClick() {
        if (!isRecording) {

            fft = new DoubleFFT_1D(FRAMES_PER_BUFFER);
            sampleShortBuffer = new short[FRAMES_PER_BUFFER];
            sampleDoubleBuffer = new double[FRAMES_PER_BUFFER];
            magnitude = new double[FRAMES_PER_BUFFER / 2];
            buffer = new short[bufferSize];

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
                            fft.realForward(sampleDoubleBuffer);

                            for (int i = 0; i < (FRAMES_PER_BUFFER / 2) - 1; i++) {
                                double real = (2 * sampleDoubleBuffer[i]);
                                double imag = (2 * sampleDoubleBuffer[i] + 1);
                                double newMagnitude = Math.sqrt(real * real + imag * imag);
                                if (newMagnitude > MAGNITUDE_THRESHOLD && newMagnitude > magnitude[i]) {
                                    magnitude[i] = newMagnitude;
                                }
                            }
                        }

                    } finally {
                        pB.setProgress(0);
                    }
                }
            });
            recordingThread.start();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page3);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        rec = (TextView) findViewById(R.id.recText);
        pB = (ProgressBar) findViewById(R.id.volume);

        onClick();
    }

    private void stopRecording() {
        if (null != audioRecord) {
            isRecording = false;
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;
            audioRecord.stop();

            // TODO: NEW INTENT HERE ?
        }
    }

    private void convertToDouble(short[] input, double[] output){
        double scale = 1 / 32768.0;
        for(int i = 0; i < input.length; i++){
            output[i] = input[i] * scale;
        }
    }

    @Override
    public void onDestroy() {
        audioRecord.release();
        super.onDestroy();
    }

}
