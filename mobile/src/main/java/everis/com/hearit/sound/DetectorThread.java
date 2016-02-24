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

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;

import com.musicg.api.WhistleApi;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

public class DetectorThread extends Thread {

	private Context ctx;

	private RecorderThread recorder;
	private WaveHeader waveHeader;
	private HiSoundApi detectionApi;
	private WhistleApi whistleApi;
	private volatile Thread _thread;

	private LinkedList<Boolean> whistleResultList = new LinkedList<Boolean>();
	private int numWhistles;
	private int whistleCheckLength = 3;
	private int whistlePassScore = 3;

	private OnSignalsDetectedListener onSignalsDetectedListener;

	public DetectorThread (Context ctx, RecorderThread recorder) {
		this.ctx = ctx;
		this.recorder = recorder;
		AudioRecord audioRecord = recorder.getAudioRecord();

		int bitsPerSample = 0;
		if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
			bitsPerSample = 16;
		} else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
			bitsPerSample = 8;
		}

		int channel = 0;
		// whistle detection only supports mono channel
		if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) {
			channel = 1;
		}

		waveHeader = new WaveHeader();
		waveHeader.setChannels(channel);
		waveHeader.setBitsPerSample(bitsPerSample);
		waveHeader.setSampleRate(audioRecord.getSampleRate());
		whistleApi = new WhistleApi(waveHeader);
		detectionApi = new HiSoundApi(waveHeader);
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
		_thread = new Thread(this);
		_thread.start();
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

					byte[] stream = convertStreamToByteArray(getInputStream());
					HiUtils.log("stream lenght: " + stream.length);

					if (detectionApi.isSpecificSound(stream)) {
						onSoundDetected();
						HiUtils.log("Sound detected");
					}

				} else {
					//HiUtils.log("No detection");
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
				if (buffer != null && buffer.length > 1) {
					// sound detected

					HiUtils.log("Buffer lenght " + buffer.length);

					String pathToCompare = HiUtils.getFilesDirectory().toString();

					Wave w1 = new Wave(waveHeader, buffer);
					Wave w2 = new Wave(pathToCompare + "/test.wav");

					HiUtils.log("Score " + w1.getFingerprintSimilarity(w2).getScore());
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

	private InputStream getInputStream () {
		HashMap<String, Integer> sounds = HiSharedPreferences.getSounds(ctx);

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
}
