package everis.com.hearit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import everis.com.hearit.sound.HiAlgorithm;
import everis.com.hearit.sound.HiRecorderThread;
import everis.com.hearit.utils.HiDBUtils;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 24/02/16.
 */
public class RecordSoundActivity extends AppCompatActivity implements HiRecorderThread.HiRecorderCallback {

    public String fileName;
    private Context ctx;
    private EditText sound_name;
    private TextView importance;
    private int importanceValue;
    private Button record_sound;
    private boolean recording = false;

    private HiRecorderThread recorderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sound);

        ctx = this;

        importanceValue = 0;

        sound_name = (EditText) findViewById(R.id.sound_name);
        importance = (TextView) findViewById(R.id.importance);
        record_sound = (Button) findViewById(R.id.record_sound);
        record_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecordAudio();
            }
        });

        importance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importanceValue = (importanceValue + 1) % 3;
                int drawable = R.drawable.round_green;
                if (importanceValue == 0)
                    drawable = R.drawable.round_green;
                else if (importanceValue == 1)
                    drawable = R.drawable.round_yellow;
                else if (importanceValue == 2)
                    drawable = R.drawable.round_red;

                importance.setBackground(ctx.getResources().getDrawable(drawable));
            }
        });
    }

    private void toggleRecordAudio() {
        recording = !recording;
        if (recording) {
            fileName = sound_name.getText().toString();

            if (fileName.isEmpty()) {
                Toast.makeText(ctx, "Chose a name for this audio", Toast.LENGTH_SHORT).show();
                recording = !recording;
                return;
            }

            if (checkName(fileName)) {
                Toast.makeText(ctx, "Audio name already exists. Change name...", Toast.LENGTH_SHORT).show();
                recording = !recording;
                return;
            }
            record_sound.setText("Stop recording");

            HiUtils.log("HiAlgorithm", "Start execution");
            //HiSharedPreferences.setSP_TIME_PROCESS_START(this, Calendar.getInstance().getTimeInMillis());

            recorderThread = new HiRecorderThread(this);
            recorderThread.execute(fileName);

            showRecordingDialog();

        } else {
            record_sound.setText("Record sound");
        }
    }

    private boolean checkName(String fileName) {
        File dir = new File(HiUtils.getFilePath(fileName));
        return dir.exists();
    }

    private void showRecordingDialog() {
        final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Recording");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Stop recording", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                recorderThread.stopRecording();

                mProgressDialog.dismiss();
                HiDBUtils.saveSoundViewIntoDB(fileName, importanceValue);
                finish();
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface p1) {
                recorderThread.stopRecording();

                HiDBUtils.saveSoundViewIntoDB(fileName, importanceValue);
                finish();
            }
        });

        mProgressDialog.show();
    }

    @Override public void onFinishRecording(ArrayList<Short> audio) {
        HiAlgorithm algorithm = new HiAlgorithm();
        algorithm.transformSound(this, fileName, audio);
    }
}
