package everis.com.hearit.sound;

import android.media.AudioFormat;

/**
 * Created by mauriziomento on 09/05/17.
 */

public class HiSoundParams {
    //NB: >> FUZ_FACTOR ===> >> Precision
    public static final int FUZ_FACTOR = 8;
    public static int RECORDER_SAMPLERATE = 44100; // 8000
    public static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static int PLAYER_CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
    public static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static int CHUNK_SIZE = 1024; // 512
    public static int WINDOWS = 4096; // 512
    public static int LOWER_LIMIT = 400;
    public static int UPPER_LIMIT = 10000; // 7000
    public static int BINS = 8;
    public static int RECORDER_AMP_THRESHOLD = 700;
    public static int MATCHED_HITS_THRESHOLD = 3;
    public static int PERCENTAGE_MATCHED = 77;
    public static int MISSING_BEFORE_RESET = 2;
    public static float FREQ_RES = RECORDER_SAMPLERATE / CHUNK_SIZE;
    public static int MIN_K = (int) (LOWER_LIMIT / FREQ_RES);
    public static int MAX_K = (int) (UPPER_LIMIT / FREQ_RES);
    //TODO: consider linear vs log
    public static int[] RANGE = HiAlgorithmUtils.generateLogSpace(HiSoundParams.LOWER_LIMIT, HiSoundParams.UPPER_LIMIT, HiSoundParams.BINS);
}
