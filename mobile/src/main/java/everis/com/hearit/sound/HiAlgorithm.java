package everis.com.hearit.sound;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 27/04/17.
 */

public class HiAlgorithm {

    private BufferedWriter writerHash;
    private BufferedWriter writerAlgorithm;

    public List<String> transformSound(String filename, ArrayList<Short> audio) {

        List<String> soundHash = new ArrayList<>();

        int[] recordPoints = new int[HiSoundParams.RANGE.length];
        double[] highScores = new double[HiSoundParams.RANGE.length];

        int totalSize = audio.size();
        int nWindows = totalSize / HiSoundParams.WINDOWS;

        //When turning into frequency domain we'll need complex numbers:
        Complex[][] matrix = new Complex[nWindows][];

        initWriterHash(filename);
        initWriterAlgorithm(filename);

        writeTime(audio, filename);

        //HiUtils.log("HiAlgorithm", "totalSize: " + totalSize);

        //For all the chunks:
        for (int w = 0; w < nWindows; w++) {

            Complex[] complex = new Complex[HiSoundParams.CHUNK_SIZE];
            for (int i = 0; i < HiSoundParams.CHUNK_SIZE; i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio.get((w * HiSoundParams.CHUNK_SIZE) + i), 0);
            }
            //Perform FFT analysis on the chunk:
            matrix[w] = FFT.fft(complex);

            for (int k = HiSoundParams.MIN_K; k < HiSoundParams.MAX_K; k++) {
                //TODO: improve cast (use Round and handle first and last index??)
                float freq = HiSoundParams.FREQ_RES * (k + 1);

                //Get the magnitude:
                //double mag = Math.log(matrix[w][k].abs() + 1);
                double mag = Math.sqrt(
                        (matrix[w][k].re() * matrix[w][k].re()) + (matrix[w][k].im() * matrix[w][k].im())
                );

                //Find out which range we are in:
                int bin = HiAlgorithmUtils.getIndex(HiSoundParams.RANGE, freq);

                double power = mag * mag;
                //Save the highest power and corresponding frequency:
                if (power > highScores[bin]) {
                    //HiUtils.log("Saved highscore", "mag: " + mag + " freq: " + freq);
                    highScores[bin] = power;
                    recordPoints[bin] = (int) freq;
                }
            }

            String hash = HiAlgorithmUtils.getHash(HiSoundParams.RANGE, recordPoints);
            soundHash.add(hash);

            writeHash(hash);
            writeAlgorithm(recordPoints);
        }

        closeWriterHash();
        closeWriterAlgorithm();

        return soundHash;
    }

    private void writeTime(ArrayList<Short> audio, String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_time", "txt")));

            for (short b : audio) {
                try {
                    writer.write(b + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initWriterHash(String filename) {
        try {
            writerHash = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_algorithm_hash", "txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHash(String hash) {

        try {
            writerHash.write(hash);
            writerHash.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWriterHash() {
        try {
            writerHash.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initWriterAlgorithm(String filename) {
        try {
            writerAlgorithm = new BufferedWriter(new FileWriter(HiUtils.createOrGetFile(filename + "_algorithm", "txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAlgorithm(int[] recordPoints) {
        try {
            for (int j = 0; j < (HiSoundParams.RANGE.length - 1); j++) {
                writerAlgorithm.write(Integer.toString(recordPoints[j]) + "\t");
            }
            writerAlgorithm.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWriterAlgorithm() {
        try {
            writerAlgorithm.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
