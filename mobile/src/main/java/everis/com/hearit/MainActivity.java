package everis.com.hearit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.musicg.wave.Wave;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import everis.com.hearit.sound.CompareSounds;
import everis.com.hearit.sound.DetectorThread;
import everis.com.hearit.sound.OnSignalsDetectedListener;
import everis.com.hearit.sound.RecorderThread;
import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

public class MainActivity extends AppCompatActivity implements OnSignalsDetectedListener {

	private Button show_list;
	private ImageView listen_sound;
	private boolean listening = false;
	private CompareSounds cs;
	private MainActivity act;

	private DetectorThread detectorThread;
	private RecorderThread recorderThread;
	private int numWhistleDetected = 0;
	private int numSoundDetected = 0;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		act = this;
		cs = new CompareSounds(getBaseContext());

		show_list = (Button) findViewById(R.id.show_list);
		show_list.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {
				Intent intent = new Intent(getBaseContext(), SoundListActivity.class);
				startActivity(intent);
			}
		});

		listen_sound = (ImageView) findViewById(R.id.listen_sound);
		listen_sound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {

				listening = !listening;

				if (listening) {

					listen_sound.startAnimation(
							AnimationUtils.loadAnimation(act, R.anim.rotate));

					//cs.start();
					recorderThread = new RecorderThread();
					//recorderThread.start();
					detectorThread = new DetectorThread(act, recorderThread);
					detectorThread.setOnSignalsDetectedListener(act);
					detectorThread.start();
				} else {

					listen_sound.clearAnimation();

					//cs.stop();
					if (recorderThread != null) {
						recorderThread.stopRecording();
						recorderThread = null;
					}
					if (detectorThread != null) {
						detectorThread.stopDetection();
						detectorThread = null;
					}
				}

			}
		});

		HiUtils.log("Comparing bass ");
		String pathToCompare = HiUtils.getFilesDirectory().toString();
		//Wave wave1 = new Wave(getInputStream());
		Wave wave1 = new Wave(pathToCompare + "/test.wav");
		Wave wave2 = new Wave(pathToCompare + "/Prova_to_compare.wav");
//		HiUtils.log("Score " + wave2.getFingerprintSimilarity(wave2).getScore());

	}

	private InputStream getInputStream () {
		HashMap<String, Integer> sounds = HiSharedPreferences.getSounds(this);

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

	@Override
	public void onWhistleDetected () {
		HiUtils.log("onWhistleDetected: " + (numWhistleDetected++));
	}

	@Override
	public void onSoundDetected () {
		HiUtils.log("onSoundDetected: " + (numSoundDetected++));
	}
}
