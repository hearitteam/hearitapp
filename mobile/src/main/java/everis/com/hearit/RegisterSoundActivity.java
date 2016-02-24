package everis.com.hearit;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

public class RegisterSoundActivity extends AppCompatActivity {

	private Context ctx;
	private EditText sound_name;
	private Button register_sound;
	private boolean registering = false;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_sound);

		ctx = this;

		sound_name = (EditText) findViewById(R.id.sound_name);
		register_sound = (Button) findViewById(R.id.register_sound);
		register_sound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {
				toggleRegistration();
			}
		});
	}

	private void toggleRegistration () {
		registering = !registering;
		if (registering) {
			String fileName = sound_name.getText().toString();

			if (fileName.isEmpty()) {
				Toast.makeText(ctx, "Chose a name for this sound", Toast.LENGTH_SHORT).show();
				registering = !registering;
				return;
			}

			if (checkName(fileName)) {
				Toast.makeText(ctx, "Sound name already exists. Change name...", Toast.LENGTH_SHORT).show();
				registering = !registering;
				return;
			}
			register_sound.setText("Stop registration");
			recordAudio(fileName + ".mp4");
		} else {
			register_sound.setText("Register sound");
		}
	}

	private boolean checkName (String fileName) {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearIt/sound/" + fileName;
		File dir = new File(path);
		return dir.exists();
	}

	public void recordAudio (final String fileName) {
		final MediaRecorder recorder = new MediaRecorder();
		ContentValues values = new ContentValues(3);
		values.put(MediaStore.MediaColumns.TITLE, fileName);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearIt/sound/";
		File dir = HiUtils.getFilesDirectory();
		if (!dir.exists())
			dir.mkdirs();
		String audio_file = path + fileName;

		recorder.setOutputFile(audio_file);

		try {
			recorder.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}

		final ProgressDialog mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Recording");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Stop recording", new DialogInterface.OnClickListener() {
			public void onClick (DialogInterface dialog, int whichButton) {
				mProgressDialog.dismiss();
				recorder.stop();
				recorder.release();
				HiSharedPreferences.addSound(ctx, fileName);
			}
		});

		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel (DialogInterface p1) {
				recorder.stop();
				recorder.release();
				HiSharedPreferences.addSound(ctx, fileName);
			}
		});
		recorder.start();
		mProgressDialog.show();
	}
}
