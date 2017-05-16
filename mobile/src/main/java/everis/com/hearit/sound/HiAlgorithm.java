package everis.com.hearit.sound;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import everis.com.hearit.RecordSoundActivity;
import everis.com.hearit.utils.HiDBUtils;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 27/04/17.
 */

public class HiAlgorithm {

    private float freqRes = HiSoundParams.RECORDER_SAMPLERATE / HiSoundParams.CHUNK_SIZE;
    private int[] RANGE;

    private BufferedWriter writer = null;
    private BufferedWriter writer2 = null;
    private BufferedWriter writer3 = null;

    private double[] highscores;
    private int[] recordPoints;
    private String filename;
    private int nWindows;
    private Complex[][] matrix;

    public static String rightPadZeros(String str, int num) {
        return String.format("%1$-" + num + "s", str).replace(' ', '0');
    }

    public static String getMd5(String input) {
        String output = "";
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");

            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(Integer.toHexString(b & 0xff));
            }
            output = sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return output;
    }

    private void initAlgorithm() {
        //TODO: consider linear vs log
        RANGE = HiAlgorithmUtils.generateLogSpace(HiSoundParams.LOWER_LIMIT, HiSoundParams.UPPER_LIMIT, HiSoundParams.BINS);

        highscores = new double[RANGE.length];
        recordPoints = new int[RANGE.length];

        try {
            writer = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_algorithm_hash", "txt")));
            writer2 = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_algorithm", "txt")));
            writer3 = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_time", "txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //HiUtils.log("process time: ", "" + HiSharedPreferences.getProcessTime(act, Calendar.getInstance().getTimeInMillis()));
    }

    public void transformSound(RecordSoundActivity act, String filename, ArrayList<Short> audio) {

        this.filename = filename;

        initAlgorithm();

        for (short b : audio) {
            try {
                writer3.write(b + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer3.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int totalSize = audio.size();
        HiUtils.log("HiAlgorithm", "totalSize: " + totalSize);

        nWindows = totalSize / HiSoundParams.CHUNK_SIZE;

        //When turning into frequency domain we'll need complex numbers:
        matrix = new Complex[nWindows][];

        //For all the chunks:
        for (int w = 0; w < nWindows; w++) {
            Complex[] complex = new Complex[HiSoundParams.CHUNK_SIZE];
            for (int i = 0; i < HiSoundParams.CHUNK_SIZE; i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio.get((w * HiSoundParams.CHUNK_SIZE) + i), 0);
            }
            //Perform FFT analysis on the chunk:
            matrix[w] = FFT.fft(complex);
        }

        for (int w = 0; w < nWindows; w++) {
            //For every line of data:
            for (int freq = HiSoundParams.LOWER_LIMIT; freq <= HiSoundParams.UPPER_LIMIT; freq++) {

                //TODO: improve cast (use Round and handle first and last index??)
                int k = (int) (freq / freqRes);

                //Get the magnitude:
                double mag = Math.log(matrix[w][k].abs() + 1);

                //Find out which range we are in:
                int bin = HiAlgorithmUtils.getIndex(RANGE, freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highscores[bin]) {
                    highscores[bin] = mag;
                    recordPoints[bin] = freq;
                }
            }

            //Write the points:
            if (writer != null) {
                try {
                    String hash = HiAlgorithmUtils.getHash(RANGE, recordPoints);
                    writer.write(hash);

                    for (int j = 0; j < (RANGE.length - 1); j++) {
                        writer2.write(Integer.toString(recordPoints[j]) + "\t");
                    }
                    writer.newLine();
                    writer2.newLine();

                    HiDBUtils.saveHashAndSound(hash, filename, 0);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            writer.close();
            writer2.close();
            HiUtils.log("HiAlgorithm", "END");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
