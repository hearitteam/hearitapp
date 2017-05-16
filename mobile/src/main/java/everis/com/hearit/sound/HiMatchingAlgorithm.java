package everis.com.hearit.sound;

import java.util.ArrayList;
import java.util.List;

import everis.com.hearit.model.Sound;
import everis.com.hearit.utils.HiDBUtils;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 16/05/17.
 */

public class HiMatchingAlgorithm {

    private float freqRes = HiSoundParams.RECORDER_SAMPLERATE / HiSoundParams.CHUNK_SIZE;
    private int[] RANGE;
    private double[] highscores;
    private int[] recordPoints;

    private Complex[] matrix;

    public void initAlgorithm() {
        //TODO: consider linear vs log
        RANGE = HiAlgorithmUtils.generateLogSpace(HiSoundParams.LOWER_LIMIT, HiSoundParams.UPPER_LIMIT, HiSoundParams.BINS);

        highscores = new double[RANGE.length];
        recordPoints = new int[RANGE.length];

        for (Sound s : HiDBUtils.getSoundListFromDB()) {
            HiUtils.log("HiMatchingAlgorithm", "existing hashes: " + s.getHash() + " - " + s.getName());
        }
    }

    public void matchChunk(ArrayList<Short> audio) {

        for (short s : audio) {
//            HiUtils.log("HiMatchingAlgorithm", "audio: " + s);
        }

        //For all the chunks:
        Complex[] complex = new Complex[HiSoundParams.CHUNK_SIZE];
        for (int i = 0; i < HiSoundParams.CHUNK_SIZE; i++) {
            //Put the time domain data into a complex number with imaginary part as 0:
            complex[i] = new Complex(audio.get(i), 0);
        }
        //Perform FFT analysis on the chunk:
        matrix = FFT.fft(complex);

        for (int freq = HiSoundParams.LOWER_LIMIT; freq <= HiSoundParams.UPPER_LIMIT; freq++) {

            //TODO: improve cast (use Round and handle first and last index??)
            int k = (int) (freq / freqRes);

            //Get the magnitude:
            double mag = Math.log(matrix[k].abs() + 1);

            //Find out which range we are in:
            int bin = HiAlgorithmUtils.getIndex(RANGE, freq);

            //Save the highest magnitude and corresponding frequency:
            if (mag > highscores[bin]) {
                highscores[bin] = mag;
                recordPoints[bin] = freq;
            }
        }

        String hash = HiAlgorithmUtils.getHash(RANGE, recordPoints);

        HiUtils.log("HiMatchingAlgorithm", "try to match: " + hash);

        List<Sound> matchedSounds = HiDBUtils.getSoundsByHash(hash);

        if (!matchedSounds.isEmpty()) {
            HiUtils.log("HiMatchingAlgorithm", "MATCHEEEEEEDDDDD");
        }
    }
}
