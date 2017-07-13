package everis.com.hearit.sound;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.AsyncTask;
import android.os.Build;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import everis.com.hearit.RecordSoundActivity;
import everis.com.hearit.utils.HiUtils;

public class HiRecorderThread extends AsyncTask<String, Integer, Void> {

    private ArrayList<Short> audio;
    private RecordSoundActivity callback;
    private boolean isRecording = false;


    public HiRecorderThread(RecordSoundActivity callback) {
        this.callback = callback;
        this.audio = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(String... params) {

        int bufferSize;
        short[] buffer;
        double sum;
        double amplitude;
        int bufferReadResult;
        NoiseSuppressor ns = null;
        AutomaticGainControl agc = null;
        AcousticEchoCanceler aec = null;

        isRecording = true;

        try {

            File file = HiUtils.createOrGetFile(callback.fileName);
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);

            bufferSize = AudioRecord.getMinBufferSize(HiSoundParams.RECORDER_SAMPLERATE, HiSoundParams.RECORDER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING);

            buffer = new short[bufferSize];

            // Create a new AudioRecord object to record the audio.
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    HiSoundParams.RECORDER_SAMPLERATE,
                    HiSoundParams.RECORDER_CHANNELS,
                    HiSoundParams.RECORDER_AUDIO_ENCODING,
                    bufferSize);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                int audioSessionId = audioRecord.getAudioSessionId();

                if (NoiseSuppressor.isAvailable()) {
                    ns = NoiseSuppressor.create(audioSessionId);
                    HiUtils.log("recording process", "NoiseSuppressor is Enabled: " + ns.getEnabled());
                } else {
                    HiUtils.log("recording process", "NoiseSuppressor failed :(");
                }
                if (AutomaticGainControl.isAvailable()) {
                    agc = AutomaticGainControl.create(audioSessionId);
                    HiUtils.log("recording process", "AutomaticGainControl is Enabled: " + agc.getEnabled());
                } else {
                    HiUtils.log("recording process", "AutomaticGainControl failed :(");
                }
                if (AcousticEchoCanceler.isAvailable()) {
                    aec = AcousticEchoCanceler.create(audioSessionId);
                    HiUtils.log("recording process", "AcousticEchoCanceler is Enabled: " + aec.getEnabled());
                } else {
                    HiUtils.log("recording process", "AcousticEchoCanceler failed :(");
                }
            }

            audioRecord.startRecording();

            while (isRecording) {
                sum = 0;
                amplitude = 0;

                bufferReadResult = audioRecord.read(buffer, 0, bufferSize);

                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);

                    sum += buffer[i] * buffer[i];
                }

                if (bufferReadResult > 0) {
                    amplitude = (int) Math.sqrt(sum / bufferReadResult);
                }

                if (amplitude > HiSoundParams.RECORDER_AMP_THRESHOLD) {
                    for (int i = 0; i < bufferReadResult; i++) {
                        audio.add(buffer[i]);
                    }
                }
            }

            audioRecord.stop();
            audioRecord.release();
            dos.close();

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (ns != null) {
                    ns.release();
                }
                if (agc != null) {
                    agc.release();
                }
                if (aec != null) {
                    aec.release();
                }
            }

        } catch (Exception ex) {
            HiUtils.log("recording process", ex.getMessage());
        }

        return null;
    }

    protected void onPostExecute(Void result) {
        callback.onFinishRecording(audio);
    }

    public void stopRecording() {
        isRecording = false;
    }

    public interface HiRecorderCallback {
        void onFinishRecording(ArrayList<Short> audio);
    }
}