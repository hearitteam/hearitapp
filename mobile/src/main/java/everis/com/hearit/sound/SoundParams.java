package everis.com.hearit.sound;

import android.media.AudioFormat;

/**
 * Created by mauriziomento on 09/05/17.
 */

public class SoundParams {


    //public static int RECORDER_SAMPLERATE = 8000;
    public static int RECORDER_SAMPLERATE = 44100;

    public static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;

    public static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
    //public static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    public static int CHUNK_SIZE = 4096;
    public static int LOWER_LIMIT = 150;
    public static int UPPER_LIMIT = 10000;
    public static int BINS = 12;
}
