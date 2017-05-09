package everis.com.hearit.sound;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import everis.com.hearit.RecordSoundActivity;
import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 27/04/17.
 */

public class HiAlgorithm {

    private float freqRes = SoundParams.RECORDER_SAMPLERATE / SoundParams.CHUNK_SIZE;
    private float[] RANGE;

    public void doTransformAndSaveSound(RecordSoundActivity act, String filename, ArrayList<Byte> audio) {

        //TODO: consider linear vs log
        generateLogSpace(SoundParams.LOWER_LIMIT, SoundParams.UPPER_LIMIT, SoundParams.BINS);

        for (float r : RANGE) {
            HiUtils.log("HiAlgorithm", "Bins: " + r);
        }

        double[] highscores = new double[RANGE.length];
        int[] recordPoints = new int[RANGE.length];

        HiUtils.log("process time b: ", "" + HiSharedPreferences.getProcessTime(act, Calendar.getInstance().getTimeInMillis()));


        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_algorithm", "txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ArrayList<Integer> audio = getIntListFromFile(filename);

        HiUtils.log("process time a: ", "" + HiSharedPreferences.getProcessTime(act, Calendar.getInstance().getTimeInMillis()));


        final int totalSize = audio.size();
        HiUtils.log("HiAlgorithm", "totalSize: " + totalSize);

        int nWindows = totalSize / SoundParams.CHUNK_SIZE;

        //When turning into frequency domain we'll need complex numbers:
        Complex[][] matrix = new Complex[nWindows][];

        //For all the chunks:
        for (int w = 0; w < nWindows; w++) {
            Complex[] complex = new Complex[SoundParams.CHUNK_SIZE];
            for (int i = 0; i < SoundParams.CHUNK_SIZE; i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio.get((w * SoundParams.CHUNK_SIZE) + i), 0);
            }
            //Perform FFT analysis on the chunk:
            matrix[w] = FFT.fft(complex);
        }

        for (int w = 0; w < nWindows; w++) {
            //For every line of data:
            for (int freq = SoundParams.LOWER_LIMIT; freq <= SoundParams.UPPER_LIMIT; freq++) {

                //TODO: improve cast (use Round and handle first and last index??)
                int k = (int) (freq / freqRes);

                //Get the magnitude:
                double mag = Math.log(matrix[w][k].abs() + 1);

                //Find out which range we are in:
                int bin = getIndex(freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highscores[bin]) {
                    highscores[bin] = mag;
                    recordPoints[bin] = freq;
                }
            }

            //Write the points to a file:
            if (writer != null) {
                try {
                    for (int j = 0; j < (RANGE.length - 1); j++) {
                        writer.write(Integer.toString(recordPoints[j]) + "\t");
                    }
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            writer.close();
            HiUtils.log("HiAlgorithm", "END");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Integer> getIntListFromFile(String filename) {
        Scanner scanner = null;
        ArrayList<Integer> arrayList = new ArrayList<>();
        try {
            scanner = new Scanner(HiUtils.createOrGetFile(filename));
            while (scanner.hasNextInt()) {
                arrayList.add(scanner.nextInt());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    //Find out in which range

    public int getIndex(int freq) {
        int i = 0;
        while (i < RANGE.length - 1) {
            if (freq >= RANGE[i] && freq < RANGE[i + 1]) {
                return i;
            }
            i++;
        }
        return i;
    }

    private void generateLogSpace(int min, int max, int logBins) {
        double logarithmicBase = Math.E;
        double logMin = Math.log(min);
        double logMax = Math.log(max);
        double delta = (logMax - logMin) / logBins;
        //int[] indexes = new int[logBins + 1];
        double accDelta = 0;
        RANGE = new float[logBins + 1];
        for (int i = 0; i < logBins; i++) {
            RANGE[i] = (float) Math.pow(logarithmicBase, logMin + accDelta);
            accDelta += delta;// accDelta = delta * i
        }
        RANGE[logBins] = SoundParams.UPPER_LIMIT;
    }
}
