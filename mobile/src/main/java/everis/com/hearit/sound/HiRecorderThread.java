package everis.com.hearit.sound;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import everis.com.hearit.RecordSoundActivity;
import everis.com.hearit.utils.HiUtils;

public class HiRecorderThread extends AsyncTask<String, Integer, Void> {

    boolean isRecording = false;
    ArrayList<Byte> audio;
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

            File audioFile = HiUtils.createOrGetFile(filename);

            BufferedWriter writer = new BufferedWriter(new FileWriter(audioFile));

            // DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
            int bufferSize = AudioRecord.getMinBufferSize(SoundParams.RECORDER_SAMPLERATE,
                    SoundParams.RECORDER_CHANNELS, SoundParams.RECORDER_AUDIO_ENCODING);
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, SoundParams.RECORDER_SAMPLERATE,
                    SoundParams.RECORDER_CHANNELS, SoundParams.RECORDER_AUDIO_ENCODING, bufferSize);

            byte[] buffer = new byte[bufferSize];
            audioRecord.startRecording();
            int r = 0;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    // dos.write(buffer[i]);
                    //dos.writeBytes(Integer.toString(buffer[i]));
                    //writer.write(Integer.toString(buffer[i]));
                    //writer.newLine();

                    audio.add(buffer[i]);

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
        callback.onFinishRecording(audio);
    }

    public void stopRecording() {
        isRecording = false;
    }

    public interface HiRecorderCallback {
        void onFinishRecording(ArrayList<Byte> audio);
    }
}