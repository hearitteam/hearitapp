package everis.com.hearit.sound;

import android.content.Context;
import android.os.Handler;

import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.Wave;

import java.util.HashMap;

import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 22/02/16.
 */
public class CompareSounds {

	private final int POLL_INTERVAL = 500;
	private ListenMic listenMic;
	private Handler mSoundHandler;
	private Context ctx;
	private Runnable mPollSoundTask = new Runnable() {
		public void run () {

			Wave v = listenMic.getAudioWave();

			HashMap<String, Integer> sounds = HiSharedPreferences.getSounds(ctx);

			for (HashMap.Entry<String, Integer> entry : sounds.entrySet()) {
				HiUtils.log("Comparing " + entry.getKey());
				String pathToCompare = HiUtils.getFilesDirectory() + "/" + entry.getKey();
				Wave v2 = new Wave(pathToCompare);
///TODO: CONTINUE HERE (wrong v2)
				FingerprintSimilarity fs = v.getFingerprintSimilarity(v2);
				HiUtils.log("Score: " + fs.getScore());
			}

			mSoundHandler.postDelayed(mPollSoundTask, POLL_INTERVAL);
		}
	};

	public CompareSounds (Context ctx) {
		this.ctx = ctx;
		listenMic = new ListenMic();
		mSoundHandler = new Handler();
	}

	public void start () {
		if (listenMic.startRecording())
			mSoundHandler.postDelayed(mPollSoundTask, POLL_INTERVAL);
	}

	public void stop () {
		if (listenMic != null)
			listenMic.stopRecording();

		if (mSoundHandler != null)
			mSoundHandler.removeCallbacks(mPollSoundTask);
	}
}
