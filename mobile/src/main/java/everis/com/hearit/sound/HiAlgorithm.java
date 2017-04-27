package everis.com.hearit.sound;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 27/04/17.
 */

public class HiAlgorithm {

    private int CHUNK_SIZE = 4096;
    private int LOWER_LIMIT = 0;
    private int UPPER_LIMIT = 300;
    private int[] RANGE = new int[]{LOWER_LIMIT, 40, 80, 120, 180, UPPER_LIMIT + 1};

    private double[] highscores = new double[RANGE.length];
    private int[] recordPoints = new int[RANGE.length];

    public void doTransformAndSaveSound(String filename) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_algorithm", "txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Integer> audio = getIntListFromFile(filename);

        final int totalSize = audio.size();
        HiUtils.log("HiAlgorithm", "totalSize: " + totalSize);

        int amountPossible = totalSize / CHUNK_SIZE;

        //When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][];

        //For all the chunks:
        for (int times = 0; times < amountPossible; times++) {
            Complex[] complex = new Complex[CHUNK_SIZE];
            for (int i = 0; i < CHUNK_SIZE; i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio.get((times * CHUNK_SIZE) + i), 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }


        for (int i = 0; i < results.length; i++) {

            //For every line of data:
            for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {
                //Get the magnitude:
                double mag = Math.log(results[i][freq].abs() + 1);

                //Find out which range we are in:
                int index = getIndex(freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highscores[index]) {
                    highscores[index] = mag;
                    recordPoints[index] = freq;
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
        while (RANGE[i] < freq)
            i++;
        return i;
    }

}
