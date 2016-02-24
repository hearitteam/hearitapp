package everis.com.hearit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import everis.com.hearit.sound.Complex;
import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 24/02/16.
 */
public class RegisterSoundAct extends AppCompatActivity {

	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "HearIt/Sound";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	short[] audioData;
	Complex[] fftTempArray;
	Complex[] fftArray;
	int[] bufferData;
	int bytesRecorded;
	private String fileName;
	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	private Context ctx;
	private EditText sound_name;
	private Button register_sound;
	private boolean registering = false;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_sound);

		ctx = this;

		init();

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
			startRecording();
		} else {
			register_sound.setText("Register sound");
			//stopRecording();
		}
	}

	protected void init () {

		bufferSize = AudioRecord.getMinBufferSize
				(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

		audioData = new short[bufferSize]; //short array that pcm data is put into.
	}

	private boolean checkName (String fileName) {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearIt/sound/" + fileName;
		File dir = new File(path);
		return dir.exists();
	}

	private String getFilename () {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + fileName +
				AUDIO_RECORDER_FILE_EXT_WAV);
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

	private void startRecording () {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE,
				RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING,
				bufferSize);
		int i = recorder.getState();
		if (i == 1)
			recorder.startRecording();

		isRecording = true;

		recordingThread = new Thread(new Runnable() {
			@Override
			public void run () {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");

		recordingThread.start();

		showRegisteringDialog();
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

	private void stopRecording () {
		if (null != recorder) {
			isRecording = false;

			int i = recorder.getState();
			if (i == 1)
				recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}

		copyWaveFile(getTempFilename(), getFilename());
		deleteTempFile();
	}

	private void deleteTempFile () {
		File file = new File(getTempFilename());
		file.delete();
	}

	private void showRegisteringDialog () {
		final ProgressDialog mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Recording");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Stop recording", new DialogInterface.OnClickListener() {
			public void onClick (DialogInterface dialog, int whichButton) {
				mProgressDialog.dismiss();
				stopRecording();
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
}
