package everis.com.hearit.sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import everis.com.hearit.utils.HiUtils;

public class HiRecorderThread extends AsyncTask<String, Integer, Void> {

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    boolean isRecording = false;

    private HiRecorderCallback callback;

    public HiRecorderThread(HiRecorderCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(String... params) {
        String filename = params[0];
        isRecording = true;

        try {

            File audioFile = HiUtils.createOrGetFile(filename);

            BufferedWriter writer = new BufferedWriter(new FileWriter(audioFile));

            // DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
            int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

            byte[] buffer = new byte[bufferSize];
            audioRecord.startRecording();
            int r = 0;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    // dos.write(buffer[i]);
                    //dos.writeBytes(Integer.toString(buffer[i]));
                    writer.write(Integer.toString(buffer[i]));
                    writer.newLine();
                    //byte b = buffer[i];
                    //if (b != 0)
                    //  b++;
                    //HiUtils.log("recoding process", "byte read: " + buffer[i] + "");
                }
                publishProgress(new Integer(r));
                r++;
            }
            writer.close();
            audioRecord.stop();
            // dos.close();
        } catch (Throwable t) {
            HiUtils.log("recoding process", "Recording Failed");
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        //HiUtils.log("recoding process", "progress: " + progress[0].toString());
    }

    protected void onPostExecute(Void result) {
        HiUtils.log("recoding process", "stop");
        callback.onFinishRegistration();
    }

    public void stopRegistration() {
        isRecording = false;
    }

    public interface HiRecorderCallback {
        void onFinishRegistration();
    }
}