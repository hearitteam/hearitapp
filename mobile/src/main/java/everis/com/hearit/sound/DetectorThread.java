package everis.com.hearit.sound;

/*
 * Copyright (C) 2012 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * musicg api in Google Code: http://code.google.com/p/musicg/
 * Android Application in Google Play: https://play.google.com/store/apps/details?id=com.whistleapp
 *
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.musicg.api.WhistleApi;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import everis.com.hearit.R;
import everis.com.hearit.Sound;
import everis.com.hearit.utils.AudioUtils;
import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;
import everis.com.hearit.utils.RegisterUtils;

public class DetectorThread extends Thread {

	private static final int RECORDER_BPP = 16;
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "HearIt/Sound";
	private static final String AUDIO_RECORDER_TEMP_FILE = "listen_temp.raw";
	long totalAudioLen = 0;
	long totalDataLen = totalAudioLen + 36;
	long longSampleRate = RECORDER_SAMPLERATE;
	int channels = 1;
	long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * RECORDER_CHANNELS / 8;
	short[] audioData;
	private Activity act;
	private RecorderThread recorder;
	private WaveHeader waveHeader;
	private HiSoundApi detectionApi;
	private WhistleApi whistleApi;
	private volatile Thread _thread;
	private AudioUtils audioUtils;
	private AudioRecord audioRecord = null;
	private LinkedList<Boolean> whistleResultList = new LinkedList<Boolean>();
	private int numWhistles;
	private int whistleCheckLength = 3;
	private int whistlePassScore = 3;
	private OnSignalsDetectedListener onSignalsDetectedListener;

	private int bufferSize = 0;

	public DetectorThread (Activity act, RecorderThread recorder) {
		this.act = act;
		this.recorder = recorder;

		audioUtils = new AudioUtils();

		bufferSize = AudioRecord.getMinBufferSize
				(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

		audioData = new short[bufferSize]; //short array that pcm data is put into.

		waveHeader = new WaveHeader();
		waveHeader.setChannels(AudioFormat.CHANNEL_IN_MONO);
		waveHeader.setBitsPerSample(AudioFormat.ENCODING_PCM_16BIT);
		waveHeader.setSampleRate(44100);
		//whistleApi = new WhistleApi(waveHeader);
		//detectionApi = new HiSoundApi(waveHeader);
	}

	public static byte[] convertStreamToByteArray (InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[10240];
		int i = Integer.MAX_VALUE;
		while ((i = is.read(buff, 0, buff.length)) > 0) {
			baos.write(buff, 0, 4096);
			HiUtils.log("i size: " + i);
		}

		return baos.toByteArray(); // be sure to close InputStream in calling function
	}

	private void initBuffer () {
		numWhistles = 0;
		whistleResultList.clear();

		// init the first frames
		for (int i = 0; i < whistleCheckLength; i++) {
			whistleResultList.add(false);
		}
		// end init the first frames
	}

	public void start () {
		//deleteFile(getFilename());

		audioUtils.startRecording("fileToCompare");

		//_thread = new Thread(this);
		//_thread.start();
	}

	/*
	public void run () {
		try {
			byte[] buffer;
			initBuffer();

			Thread thisThread = Thread.currentThread();
			while (_thread == thisThread) {
				// detect sound
				buffer = recorder.getFrameBytes();

				// audio analyst
				if (buffer != null) {
					// sound detected
					// whistle detection
					//System.out.println("*Whistle:");
					boolean isWhistle = whistleApi.isWhistle(buffer);
					if (whistleResultList.getFirst()) {
						numWhistles--;
					}

					whistleResultList.removeFirst();
					whistleResultList.add(isWhistle);

					if (isWhistle) {
						numWhistles++;
					}
					//System.out.println("num:" + numWhistles);

					if (numWhistles >= whistlePassScore) {
						// clear buffer
						initBuffer();
						onWhistleDetected();
					}
					// end whistle detection
				} else {
					// no sound detected
					if (whistleResultList.getFirst()) {
						numWhistles--;
					}
					whistleResultList.removeFirst();
					whistleResultList.add(false);
				}
				// end audio analyst
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

	public void stopDetection () {
		_thread = null;
		/////TODO changed String matchingSound = registerUtils.stopAndCompare(act);
		// Sound s = HiUtils.getSoundFromName(act, matchingSound);
		// showSoundDialog(s);
		//copyWaveFile(getTempFilename(), getFilename());
	}

	public void run () {
		try {


			byte[] buffer;
			initBuffer();

			Thread thisThread = Thread.currentThread();
			while (_thread == thisThread) {
				// detect sound
				buffer = recorder.getFrameBytes();

				// audio analyst
				if (buffer != null) {
					// sound detected

					//HiUtils.log("buffer size: " + buffer.length);

					//HiUtils.log("Buffer lenght " + buffer.length);

					//writeAudioDataToFile(buffer);
					//copyWaveFile(getTempFilename(), getFilename());

					//printSound(buffer);
					//deleteTempFile();

					/*
					int sizeSound = buffer.length;
					int sizeHeader = getByteHeader().length;

					byte[] sound = new byte[sizeSound + sizeHeader];
					System.arraycopy(getByteHeader(), 0, sound, 0, getByteHeader().length);
					System.arraycopy(buffer, 0, sound, getByteHeader().length, buffer.length);

					HiUtils.log("sound size: " + sound.length);
					*/


					/*

					if (detectionApi.isSpecificSound(stream)) {
						onSoundDetected();
						HiUtils.log("Sound detected");
					}
					*/

				} else {
					//HiUtils.log("No detection");
				}
				// end audio analyst
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void compareSounds () {
		String pathToCompare = HiUtils.getFilesDirectory().toString();

		//Wave w1 = new Wave(waveHeader, sound);
		Wave w1 = new Wave(getFilename());
		//Wave w1 = new Wave(pathToCompare + "/prova.wav");
		Wave w2 = new Wave(pathToCompare + "/fff.wav");

		HiUtils.log("Comparing " + getFilename() + " and " + pathToCompare + "/fff.wav");

		float score = w1.getFingerprintSimilarity(w2).getScore();
		//float score = 0;
		HiUtils.log("Score " + score);
		if (score > 0.1) {
			//HiUtils.toastShort(ctx, "Score " + score);
			HiUtils.log("FOUND!!! Score " + score);
		}
	}

	private void printSound (byte[] sound) {
		for (Byte b : sound) {
			HiUtils.log("PrintSound - " + b.intValue());
		}
	}

	private String getFilename () {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/fileToCompare" +
				AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private void deleteFile (String filename) {
		File file = new File(filename);
		file.delete();
	}


	/*
	private void convertByteArrayToWave(byte[] array){
		long length = (long)(array.length / audioFormat.getFrameSize());
		AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, audioFormat, length);
	}
*/

	private InputStream getInputStream () {
		HashMap<String, Integer> sounds = HiSharedPreferences.getSounds(act);

		for (HashMap.Entry<String, Integer> entry : sounds.entrySet()) {
			HiUtils.log("Comparing " + entry.getKey());
			String pathToCompare = HiUtils.getFilesDirectory() + "/" + entry.getKey();
			try {
				InputStream is = new FileInputStream(pathToCompare);
				return is;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	private void writeAudioDataToFile (byte[] data) {
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			os.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
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

	private void onWhistleDetected () {
		if (onSignalsDetectedListener != null) {
			onSignalsDetectedListener.onWhistleDetected();
		}
	}

	private void onSoundDetected () {
		if (onSignalsDetectedListener != null) {
			onSignalsDetectedListener.onSoundDetected();
		}
	}

	public void setOnSignalsDetectedListener (OnSignalsDetectedListener listener) {
		onSignalsDetectedListener = listener;
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

			writeWaveFileHeader(out, totalAudioLen, totalDataLen,
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

	private void writeWaveFileHeader (
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {
		byte[] header = new byte[44];

		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}


	private void showSoundDialog (Sound sound) {

		String message = "";

		int importanceColor = android.R.color.darker_gray;
		if (sound != null) {
			message = sound.getName();

			if (sound.getImportance() == 0) {
				importanceColor = android.R.color.holo_green_dark;
			} else if (sound.getImportance() == 1) {
				importanceColor = android.R.color.holo_orange_light;
				((Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
			} else if (sound.getImportance() == 2) {
				importanceColor = android.R.color.holo_red_dark;
				((Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(2000);
			}
		} else {
			message = "No matched sound";
		}

		LayoutInflater inflater = act.getLayoutInflater();
		View v = inflater.inflate(R.layout.custom_dialog, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setView(v);
		final Dialog dialog = builder.create();

		v.findViewById(R.id.popup).setBackgroundColor(act.getResources().getColor(importanceColor));
		((TextView) v.findViewById(R.id.title)).setText("Matched Sound");
		((TextView) v.findViewById(R.id.message)).setText(message);
		v.findViewById(R.id.positiveButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

}
