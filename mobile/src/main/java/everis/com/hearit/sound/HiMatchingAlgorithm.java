package everis.com.hearit.sound;

import java.util.ArrayList;
import java.util.List;

import everis.com.hearit.model.Sound;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 16/05/17.
 */

public class HiMatchingAlgorithm {

    public List<String> matchChunk(ArrayList<Short> audio, List<Sound> allSound) {

        String hash = CalculateHash(audio);

        HiUtils.log("HiMatchingAlgorithm", "try to match: " + hash);

        return PartialMatched(allSound, hash);
    }

    private String CalculateHash(ArrayList<Short> audio) {

        int[] recordPoints = new int[HiSoundParams.RANGE.length];
        double[] highScores = new double[HiSoundParams.RANGE.length];

        Complex[] matrix;
        Complex[] complex = new Complex[HiSoundParams.CHUNK_SIZE];

        //For all the chunks:
        for (int i = 0; i < HiSoundParams.CHUNK_SIZE; i++) {
            //Put the time domain data into a complex number with imaginary part as 0:
            complex[i] = new Complex(audio.get(i), 0);
        }

        //Perform FFT analysis on the chunk:
        matrix = FFT.fft(complex);

        for (int k = HiSoundParams.MIN_K; k < HiSoundParams.MAX_K; k++) {
            //TODO: improve cast (use Round and handle first and last index??)
            float freq = HiSoundParams.FREQ_RES * (k + 1);

            double mag = Math.sqrt(
                    (matrix[k].re() * matrix[k].re()) + (matrix[k].im() * matrix[k].im())
            );

            //Find out which range we are in:
            int bin = HiAlgorithmUtils.getIndex(HiSoundParams.RANGE, freq);

            //Save the highest magnitude and corresponding frequency:
            if (mag > highScores[bin]) {
                //HiUtils.log("Saved highscore", "mag: " + mag + " freq: " + freq);
                highScores[bin] = mag;
                recordPoints[bin] = (int) freq;
            }
        }

        return HiAlgorithmUtils.getHash(HiSoundParams.RANGE, recordPoints);
    }

    private List<String> PartialMatched(List<Sound> allSound, String hash) {

        List<String> soundMatched = new ArrayList<>();

        String[] hashSplit = hash.split(" ");

        int[] arrayHash = new int[hashSplit.length];
        for (int i = 0; i < hashSplit.length; i++) {
            arrayHash[i] = Integer.parseInt(hashSplit[i]);
        }

        for (int i = 0; i < allSound.size(); i++) {

            int countEquals = 0;
            int[] soundArrayHash = allSound.get(i).getArrayHash();
            Sound sound = allSound.get(i);

            for (int j = 0; j < soundArrayHash.length; j++) {
                if (arrayHash[j] == soundArrayHash[j]) {
                    countEquals++;
                }
            }

            int percentage = ((countEquals * 100) / soundArrayHash.length);

            if (percentage >= HiSoundParams.PERCENTAGE_MATCHED) {
                String soundName = sound.getName();
                if (!soundMatched.contains(soundName)) {
                    soundMatched.add(soundName);
                }
                HiUtils.log("HiMatchingAlgorithm", "matched: " + hash + "   with: " + soundName);
            }
        }

        return soundMatched;
    }
}
