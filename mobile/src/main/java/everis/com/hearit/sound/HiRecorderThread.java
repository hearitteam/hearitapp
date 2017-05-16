package everis.com.hearit.sound;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.util.ArrayList;

import everis.com.hearit.RecordSoundActivity;
import everis.com.hearit.utils.HiUtils;

public class HiRecorderThread extends AsyncTask<String, Integer, Void> {

    boolean isRecording = false;
    ArrayList<Short> audio;
    private RecordSoundActivity callback;

    public HiRecorderThread(RecordSoundActivity callback) {
        this.callback = callback;
        this.audio = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(String... params) {
        String filename = params[0];
        isRecording = true;

        try {
            //int bufferSize = AudioRecord.getMinBufferSize(HiSoundParams.RECORDER_SAMPLERATE,
            //        HiSoundParams.RECORDER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING);
            int bufferSize = HiSoundParams.CHUNK_SIZE;

            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, HiSoundParams.RECORDER_SAMPLERATE,
                    HiSoundParams.RECORDER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING, bufferSize);

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            double sum;
            double amplitude = 0;

            while (isRecording) {
                sum = 0;

                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    sum += buffer[i] * buffer[i];
                }

                if (bufferReadResult > 0) {
                    amplitude = sum / bufferReadResult;
                    HiUtils.log("recording process", "sqrt amp: " + (int) Math.sqrt(amplitude));
                }

                if ((int) Math.sqrt(amplitude) > HiSoundParams.RECORDER_AMP_THRESHOLD) {
                    for (int i = 0; i < bufferReadResult; i++) {
                        audio.add(buffer[i]);
                        //HiUtils.log("recoding process", "byte read: " + buffer[i] + "");
                    }
                }

                //publishProgress(new Integer(r));
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