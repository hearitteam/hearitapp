package everis.com.hearit.sound;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import everis.com.hearit.RecordSoundActivity;
import everis.com.hearit.utils.HiUtils;

public class HiRecorderThread extends AsyncTask<String, Integer, Void> {

    private boolean isRecording = false;
    private ArrayList<Short> audio;
    private RecordSoundActivity callback;

    public HiRecorderThread(RecordSoundActivity callback) {
        this.callback = callback;
        this.audio = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(String... params) {
        //String filename = params[0];

        String filePath;
        double sum;
        double amplitude;
        int read;
        int bufferReadResult;
        int bufferSize = HiSoundParams.CHUNK_SIZE;
        short[] buffer = new short[bufferSize];
        byte[] audioData = new byte[bufferSize];
        FileOutputStream os = null;

        isRecording = true;

        try {
            //int bufferSize = AudioRecord.getMinBufferSize(HiSoundParams.RECORDER_SAMPLERATE,
            //        HiSoundParams.RECORDER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING);

            filePath = HiUtils.getSoundsPath() + "/" + callback.fileName +".wav";

            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    HiSoundParams.RECORDER_SAMPLERATE,
                    HiSoundParams.RECORDER_CHANNELS,
                    HiSoundParams.RECORDER_AUDIO_ENCODING,
                    bufferSize);

            audioRecord.startRecording();

            try {
                os = new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            while (isRecording) {
                sum = 0;
                amplitude = 0;

                bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                read = audioRecord.read(audioData,0,bufferSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != read) {

                    try {
                        os.write(audioData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < bufferReadResult; i++) {
                        sum += buffer[i] * buffer[i];
                    }

                    if (bufferReadResult > 0) {
                        amplitude = sum / bufferReadResult;
                        //HiUtils.log("recording process", "sqrt amp: " + (int) Math.sqrt(amplitude));
                    }

                    if ((int) Math.sqrt(amplitude) > HiSoundParams.RECORDER_AMP_THRESHOLD) {
                        for (int i = 0; i < bufferReadResult; i++) {
                            audio.add(buffer[i]);
                            //HiUtils.log("recoding process", "byte read: " + buffer[i] + "");
                        }
                    }
                }

                //publishProgress(new Integer(r));
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            audioRecord.stop();
        } catch (Throwable t) {
            HiUtils.log("recording process", "Recording Failed");
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        //HiUtils.log("recoding process", "progress: " + progress[0].toString());
    }

    protected void onPostExecute(Void result) {
        HiUtils.log("recording process", "stop");
        callback.onFinishRecording(audio);
    }

    public void stopRecording() {
        isRecording = false;
    }

    public interface HiRecorderCallback {
        void onFinishRecording(ArrayList<Short> audio);
    }
}