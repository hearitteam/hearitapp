package everis.com.hearit.sound;

/**
 * Created by mauriziomento on 16/05/17.
 */

public class HiAlgorithmUtils {

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

        //for (int r : RANGE) {
        //HiUtils.log("HiAlgorithm", "Bins: " + r);
        //}

        return RANGE;
    }

    //Find out in which BIN
    public static int getIndex(int[] RANGE, float freq) {
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
        //String hashNoCorrection = "";
        int correction;

        for (int i = recordPoints.length - 1; i >= 0; i--) {
            correction = recordPoints[i] % (RANGE[i] / HiSoundParams.FUZ_FACTOR);
            hash += String.valueOf((recordPoints[i] - correction) + " ");
            //hashNoCorrection += String.valueOf(recordPoints[i] + " ");
        }

        //HiUtils.log("HiAlgorithm", "hash: " + hash);
        //HiUtils.log("HiAlgorithm", "hashNoCorrection: " + hashNoCorrection);

        return hash;
    }
}
