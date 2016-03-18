package everis.com.hearit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.RegisterUtils;

/**
 * Created by mauriziomento on 24/02/16.
 */
public class RegisterSoundAct extends AppCompatActivity {

	private String fileName;
	private AudioRecord recorder = null;
	private Context ctx;
	private EditText sound_name;
	private Button register_sound;
	private boolean registering = false;
	private RegisterUtils registerUtils;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_sound);

		ctx = this;

		registerUtils = new RegisterUtils();

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
			fileName = sound_name.getText().toString();

			if (fileName.isEmpty()) {
				Toast.makeText(ctx, "Chose a name for this audio", Toast.LENGTH_SHORT).show();
				registering = !registering;
				return;
			}

			if (checkName(fileName)) {
				Toast.makeText(ctx, "Audio name already exists. Change name...", Toast.LENGTH_SHORT).show();
				registering = !registering;
				return;
			}
			register_sound.setText("Stop registration");
			registerUtils.startRecording(fileName);
			showRegisteringDialog();

		} else {
			register_sound.setText("Register sound");
			//stopRecording();
		}
	}

	private boolean checkName (String fileName) {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearIt/sound/" + fileName;
		File dir = new File(path);
		return dir.exists();
	}

	private void showRegisteringDialog () {
		final ProgressDialog mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Recording");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Stop recording", new DialogInterface.OnClickListener() {
			public void onClick (DialogInterface dialog, int whichButton) {
				mProgressDialog.dismiss();
				registerUtils.stopRecording();
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
		mProgressDialog.show();
	}

}
