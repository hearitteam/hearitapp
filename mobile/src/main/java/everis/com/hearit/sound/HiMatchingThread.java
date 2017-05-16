package everis.com.hearit.sound;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.util.ArrayList;

import everis.com.hearit.utils.HiUtils;

public class HiMatchingThread extends AsyncTask<Void, Void, Void> {

    private boolean isRecording = false;
    private ArrayList<Short> audio;
    private AudioRecord audioRecord;

    public HiMatchingThread() {
        this.audio = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(Void... params) {
        isRecording = true;

        try {
            //int bufferSize = AudioRecord.getMinBufferSize(HiSoundParams.RECORDER_SAMPLERATE,
            //        HiSoundParams.RECORDER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING);

            int bufferSize = HiSoundParams.CHUNK_SIZE;

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, HiSoundParams.RECORDER_SAMPLERATE,
                    HiSoundParams.RECORDER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING, bufferSize);

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            double sum;
            double amplitude = 0;

            HiMatchingAlgorithm hiMatchingAlgorithm = new HiMatchingAlgorithm();
            hiMatchingAlgorithm.initAlgorithm();

            while (isRecording) {
                sum = 0;
                audio.clear();

                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    sum += buffer[i] * buffer[i];
                }

                if (bufferReadResult > 0) {
                    amplitude = sum / bufferReadResult;
                    // HiUtils.log("matching process", "sqrt amp: " + (int) Math.sqrt(amplitude));
                }

                if ((int) Math.sqrt(amplitude) > HiSoundParams.RECORDER_AMP_THRESHOLD) {
                    for (int i = 0; i < bufferReadResult; i++) {
                        audio.add(buffer[i]);
                        //HiUtils.log("recoding process", "byte read: " + buffer[i] + "");
                    }

                    hiMatchingAlgorithm.matchChunk(audio);
                }
            }
        } catch (Throwable t) {
            HiUtils.log("matching process", "Recording Failed");
        }
        return null;
    }


    protected void onPostExecute(Void result) {
        HiUtils.log("matching process", "stop");
    }

    public void stopRecording() {
        isRecording = false;
        audioRecord.stop();
    }
}