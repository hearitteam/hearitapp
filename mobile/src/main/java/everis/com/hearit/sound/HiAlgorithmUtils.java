package everis.com.hearit.sound;

import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 16/05/17.
 */

public class HiAlgorithmUtils {

    //NB: >> FUZ_FACTOR ===> >> Precision
    private static final int FUZ_FACTOR = 10;

    public static int[] generateLogSpace(int min, int max, int logBins) {
        double logarithmicBase = Math.E;
        double logMin = Math.log(min);
        double logMax = Math.log(max);
        double delta = (logMax - logMin) / logBins;
        //int[] indexes = new int[logBins + 1];
        double accDelta = 0;
        int[] RANGE = new int[logBins + 1];
        for (int i = 0; i < logBins; i++) {
            RANGE[i] = (int) Math.pow(logarithmicBase, logMin + accDelta);
            accDelta += delta;// accDelta = delta * i
        }
        RANGE[logBins] = HiSoundParams.UPPER_LIMIT;

        for (int r : RANGE) {
            HiUtils.log("HiAlgorithm", "Bins: " + r);
        }

        return RANGE;
    }

    //Find out in which BIN
    public static int getIndex(int[] RANGE, int freq) {
        int i = 0;
        while (i < RANGE.length - 1) {
            if (freq >= RANGE[i] && freq < RANGE[i + 1]) {
                return i;
            }
            i++;
        }
        return i;
    }

    public static String getHash(int[] RANGE, int[] recordPoints) {

        String hash = "";
        String hashNoCorrection = "";
        int correction = 0;
        for (int i = recordPoints.length - 1; i >= 0; i--) {
            correction = recordPoints[i] % (RANGE[i] / FUZ_FACTOR);
            //correction = recordPoints[i] % FUZ_FACTOR;
            hash += String.valueOf((recordPoints[i] - correction) + " ");
            hashNoCorrection += String.valueOf(recordPoints[i] + " ");
        }

        HiUtils.log("HiAlgorithm", "hash: " + hash);
        HiUtils.log("HiAlgorithm", "hashNoCorrection: " + hashNoCorrection);

        //return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR)) * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100 + (p1 - (p1 % FUZ_FACTOR));
        //return getMd5(hash);
        return hash;
    }
}
