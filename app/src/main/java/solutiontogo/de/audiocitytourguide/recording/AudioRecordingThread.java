package solutiontogo.de.audiocitytourguide.recording;

import java.io.IOException;
import android.media.MediaRecorder;

/**
 * Created by maheshkandhari on 1/27/2017.
 */

public class AudioRecordingThread extends Thread {
    private String outputFile;
    private boolean isRecording = true;
    private AudioRecordingHandler handler = null;
    private static final int SAMPLING_RATE = 44100;
    private static final int ENCODING_BIT_RATE = 96000;

    public AudioRecordingThread(String outputFile, AudioRecordingHandler handler) {
        this.outputFile = outputFile;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {

            MediaRecorder myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            myRecorder.setAudioSamplingRate(SAMPLING_RATE);
            myRecorder.setAudioEncodingBitRate(ENCODING_BIT_RATE);
            myRecorder.setOutputFile(outputFile);

            myRecorder.prepare();
            myRecorder.start();

            while (isRecording) {
                if(handler != null) {
                    handler.updateVisualizerView(myRecorder.getMaxAmplitude());
                }
            }

            myRecorder.stop();
            myRecorder.release();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopRecording() {
        isRecording = false;
    }
}
