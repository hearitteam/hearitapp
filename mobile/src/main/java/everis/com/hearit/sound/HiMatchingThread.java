package everis.com.hearit.sound;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import everis.com.hearit.model.Sound;
import everis.com.hearit.utils.HiUtils;

public class HiMatchingThread extends AsyncTask<Void, Void, Void> {

    private boolean isRecording = false;
    private ArrayList<Short> audio;
    private AudioRecord audioRecord;
    private Map<Sound, Integer> matchedMap;
    private Sound matchedSound;

    private HiMatchingCallback callback;


    public HiMatchingThread(HiMatchingCallback callback) {
        this.callback = callback;
        this.audio = new ArrayList<>();
        this.matchedMap = new HashMap<>();
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

            List<Sound> matchedSounds;

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

                    matchedSounds = hiMatchingAlgorithm.matchChunk(audio);

                    if (!matchedSounds.isEmpty()) {
                        for (Sound s : matchedSounds) {
                            for (Map.Entry<Sound, Integer> m : matchedMap.entrySet()) {
                                if (s.getHash() == m.getKey().getHash()) {
                                    Integer count = m.getValue();
                                    if (count == null) {
                                        matchedMap.put(s, 1);
                                        if (1 == HiSoundParams.MATCHED_HITS_THRESHOLD) {
                                            matchedSound = s;
                                        }
                                    } else {
                                        matchedMap.put(s, count + 1);
                                        if (count + 1 == HiSoundParams.MATCHED_HITS_THRESHOLD) {
                                            matchedSound = s;
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        //TODO: Decrement hit list
                        matchedMap.clear();
                        matchedSound = null;
                    }
                }

                if (matchedSound != null) {
                    isRecording = false;
                    callback.onSoundMatched(matchedSound);
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

    public void startRecording() {
        isRecording = true;
    }

    public void stopRecording() {
        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }

    public interface HiMatchingCallback {
        void onSoundMatched(Sound soundMatched);
    }
}