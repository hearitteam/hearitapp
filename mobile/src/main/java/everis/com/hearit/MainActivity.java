package everis.com.hearit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import everis.com.hearit.sound.CompareSounds;
import everis.com.hearit.sound.DetectorThread;
import everis.com.hearit.sound.OnSignalsDetectedListener;
import everis.com.hearit.sound.RecorderThread;
import everis.com.hearit.utils.HiUtils;

public class MainActivity extends AppCompatActivity implements OnSignalsDetectedListener {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionWriteStorage();
    }

    private void requestPermissionWriteStorage() {
        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            requestPermissionRecordAudio();
        }
    }

    private void requestPermissionRecordAudio() {
        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            mkdirSoundsFolder();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission write storage granted...making folder", Toast.LENGTH_LONG).show();
                    requestPermissionRecordAudio();
                } else {
                    Toast.makeText(this, "Permission write storage not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission record sound granted...making folder", Toast.LENGTH_LONG).show();
                    mkdirSoundsFolder();
                } else {
                    Toast.makeText(this, "Permission record sound not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void mkdirSoundsFolder() {

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), HiUtils.AUDIO_RECORDER_FOLDER);
        boolean existing = folder.isDirectory();
        boolean created = folder.mkdirs();
        if (created) {
            Toast.makeText(this, "Folder created...starting app", Toast.LENGTH_LONG).show();
            startApp();
        } else if (!existing) {
            Toast.makeText(this, "Error creating folder", Toast.LENGTH_LONG).show();
        } else if (existing) {
            startApp();
        }
    }

    private void startApp() {
        act = this;
        //cs = new CompareSounds(getBaseContext());
        cs = new CompareSounds();

        show_list = (Button) findViewById(R.id.show_list);
        show_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SoundListActivity.class);
                startActivity(intent);
            }
        });

        listen_sound = (ImageView) findViewById(R.id.listen_sound);
        listen_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        //String pathToCompare = HiUtils.getFilesDirectory().toString();
        //Wave wave1 = new Wave(getInputStream());
        //Wave wave1 = new Wave(pathToCompare + "/test.wav");
        //Wave wave2 = new Wave(pathToCompare + "/Prova_to_compare.wav");
//		HiUtils.log("Score " + wave2.getFingerprintSimilarity(wave2).getScore());

    }

    /*
    private InputStream getInputStream() {
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
    */

    @Override
    public void onWhistleDetected() {
        HiUtils.log("onWhistleDetected: " + (numWhistleDetected++));
    }

    @Override
    public void onSoundDetected() {
        HiUtils.log("onSoundDetected: " + (numSoundDetected++));
    }
}
