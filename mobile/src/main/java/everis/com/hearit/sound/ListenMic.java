package everis.com.hearit.sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class ListenMic {
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	short[] audioData;
	Complex[] fftTempArray;
	Complex[] fftArray;
	int[] bufferData;
	int bytesRecorded;
	int mPeakPos;
	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;

	public ListenMic () {

		//setButtonHandlers();
		//enableButtons(false);

		bufferSize = AudioRecord.getMinBufferSize
				(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

		audioData = new short[bufferSize]; //short array that pcm data is put into.

	}

	private String getFilename () {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	public double[] calculateFFT (byte[] signal) {
		final int mNumberOfFFTPoints = 1024;
		double mMaxFFTSample;

		double temp;
		Complex[] y;
		Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
		double[] absSignal = new double[mNumberOfFFTPoints / 2];

		for (int i = 0; i < mNumberOfFFTPoints; i++) {
			temp = (double) ((signal[2 * i] & 0xFF) | (signal[2 * i + 1] << 8)) / 32768.0F;
			complexSignal[i] = new Complex(temp, 0.0);
		}

		y = FFT.fft(complexSignal); // --> Here I use FFT class

		mMaxFFTSample = 0.0;
		mPeakPos = 0;
		for (int i = 0; i < (mNumberOfFFTPoints / 2); i++) {
			absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
			if (absSignal[i] > mMaxFFTSample) {
				mMaxFFTSample = absSignal[i];
				mPeakPos = i;
			}
		}

		return absSignal;

	}

	private void writeAudioDataToFile () {

		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int read = 0;
		if (null != os) {
			while (isRecording) {
				read = recorder.read(data, 0, bufferSize);
				if (read > 0) {
					//absNormalizedSignal = calculateFFT(data); // --> HERE ^__^
				}

				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void compareAudio () {

		double[] fft;
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();

		int read = 0;
		while (isRecording) {
			read = recorder.read(data, 0, bufferSize);
			if (read > 0) {
				fft = calculateFFT(data); // --> HERE ^__^
			}

			if (AudioRecord.ERROR_INVALID_OPERATION != read) {

			}
		}
	}


	public Wave getAudioWave () {

		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int read = 0;
		if (null != os) {
			//while (isRecording) {
			read = recorder.read(data, 0, bufferSize);
			if (read > 0) {
				//absNormalizedSignal = calculateFFT(data); // --> HERE ^__^
			}

			if (AudioRecord.ERROR_INVALID_OPERATION != read) {
				try {
					os.write(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new Wave(getTempFilename());
	}


	public void convert () {


	}

	public void calculate () {
		Complex[] fftTempArray = new Complex[bufferSize];
		for (int i = 0; i < bufferSize; i++) {
			fftTempArray[i] = new Complex(audioData[i], 0);
		}
		Complex[] fftArray = FFT.fft(fftTempArray);

		double[] micBufferData = new double[bufferSize];
		final int bytesPerSample = 2;
		final double amplification = 100.0;
		for (int index = 0, floatIndex = 0; index < bytesRecorded - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
			double sample = 0;
			for (int b = 0; b < bytesPerSample; b++) {
				int v = bufferData[index + b];
				if (b < bytesPerSample - 1 || bytesPerSample == 1) {
					v &= 0xFF;
				}
				sample += v << (b * 8);
			}
			double sample32 = amplification * (sample / 32768.0);
			micBufferData[floatIndex] = sample32;
		}


	}


	private String getTempFilename () {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	public boolean startRecording () {
		try {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

			recorder.startRecording();

			isRecording = true;

			recordingThread = new Thread(new Runnable() {

				public void run () {
					writeAudioDataToFile();
				}
			}, "AudioRecorder Thread");

			recordingThread.start();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void stopRecording () {
		if (null != recorder) {
			isRecording = false;

			recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}

		copyWaveFile(getTempFilename(), getFilename());
		// deleteTempFile();
	}

	private void deleteTempFile () {
		File file = new File(getTempFilename());
		file.delete();
	}

	private void copyWaveFile (String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 2;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

		byte[] data = new byte[bufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			HiUtils.log("File size: " + totalDataLen);

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);

			while (in.read(data) != -1) {
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader (
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {
		//another code

	}

	/*
	private View.OnClickListener btnClick = new View.OnClickListener() {
		public void onClick (View v) {
			switch (v.getId()) {
				case R.id.btStart: {
					AppLog.logString("Start Recording");
					enableButtons(true);
					startRecording();
					break;
				}
				case R.id.btStop: {
					AppLog.logString("Stop Recording");
					enableButtons(false);
					stopRecording();
					calculate();
					break;

				}
			}
		}
	};
	*/
}
