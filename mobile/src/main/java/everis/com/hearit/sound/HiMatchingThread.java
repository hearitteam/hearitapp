package everis.com.hearit.sound;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.AsyncTask;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import everis.com.hearit.model.Sound;
import everis.com.hearit.utils.HiUtils;

public class HiMatchingThread extends AsyncTask<Void, Void, Void> {

    private ArrayList<Short> audio;
    //TODO change MAP type (Anna)
    //private Map<Sound, Pair<Integer, Integer>> matchedMap;
    private Map<String, Integer> matchedMap;
    private String matchedSound;
    private HiMatchingCallback callback;
    private List<Sound> allSound;
    private boolean isRecording = false;

    public HiMatchingThread(HiMatchingCallback callback, List<Sound> allSound) {
        this.callback = callback;
        this.audio = new ArrayList<>();
        this.matchedMap = new HashMap<>();
        this.allSound = allSound;
    }

    @Override
    protected Void doInBackground(Void... params) {

        int bufferSize;
        short[] buffer;
        double sum;
        double amplitude;
        int bufferReadResult;
        int mismatched = 0;
        List<String> matchedSounds;
        NoiseSuppressor ns = null;
        AutomaticGainControl agc = null;
        AcousticEchoCanceler aec = null;
        HiMatchingAlgorithm hiMatchingAlgorithm = new HiMatchingAlgorithm();

        isRecording = true;

        try {

            bufferSize = HiSoundParams.CHUNK_SIZE;//AudioRecord.getMinBufferSize(HiSoundParams.RECORDER_SAMPLERATE, HiSoundParams.RECORDER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING);

            buffer = new short[bufferSize];

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
                audio = new ArrayList<>();

                bufferReadResult = audioRecord.read(buffer, 0, bufferSize);

                if (bufferReadResult > 0) {
                    for (int i = 0; i < bufferReadResult; i++) {
                        sum += buffer[i] * buffer[i];
                    }

                    amplitude = (int) Math.sqrt(sum / bufferReadResult);
                }

                if (amplitude > HiSoundParams.RECORDER_AMP_THRESHOLD) {

                    for (int i = 0; i < bufferReadResult; i++) {
                        audio.add(buffer[i]);
                    }

                    matchedSounds = hiMatchingAlgorithm.matchChunk(audio, this.allSound);

                    if (!matchedSounds.isEmpty()) {

                        Integer maxMatched = 0;

                        for (String s : matchedSounds) {

                            Integer count = matchedMap.get(s);

                            if (count == null) {
                                count = 1;
                            } else {
                                count++;
                            }

                            matchedMap.put(s, count);

                            if (count >= HiSoundParams.MATCHED_HITS_THRESHOLD && count > maxMatched) {
                                maxMatched = count;
                                matchedSound = s;
                            }
                        }

                        if (matchedSound != null) {
                            isRecording = false;
                        }

                    } else {
                        mismatched++;

                        if (mismatched >= HiSoundParams.MISSING_BEFORE_RESET) {
                            mismatched = 0;
                            matchedMap = new HashMap<>();
                            //HiUtils.log("HiMatchingAlgorithm", "clear map");
                        }
                    }
                }
            }

            audioRecord.stop();
            audioRecord.release();

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

            if (matchedSound != null) {
                callback.onSoundMatched(matchedSound);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /*@Override
    protected void onPostExecute(Void result) {
        HiUtils.log("matching process", "stop");
    }*/

    public void stopRecording() {
        isRecording = false;
    }

    public interface HiMatchingCallback {
        void onSoundMatched(String soundNameMatched);
    }
}