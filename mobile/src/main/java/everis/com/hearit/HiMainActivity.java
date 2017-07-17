package everis.com.hearit;

import android.Manifest;
import android.app.ActivityManager;
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

import everis.com.hearit.utils.HiUtils;

public class HiMainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    public static boolean restartListenerService = false;

    private Button show_list;
    private ImageView listen_sound;
    private HiMainActivity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start cascading permission requests (first WriteStorage, then RecordAudio)
        // Each request will call onRequestPermissionsResult callback
        requestPermissionWriteStorage();
    }

    /**
     * Checks/Requests WriteStorage permission.
     * <p>Will call onRequestPermissionsResult callback once done.</p>
     */
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

    /**
     * Checks/Requests RecordAudio permission.
     * <p>Will call onRequestPermissionsResult callback once done.</p>
     */
    private void requestPermissionRecordAudio() {
        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            mkdirSoundsFolderAndStartApp();
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
                    mkdirSoundsFolderAndStartApp();
                } else {
                    Toast.makeText(this, "Permission record sound not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /**
     * Creates a dedicated folder in DCIM folder. Then it starts the app.
     */
    private void mkdirSoundsFolderAndStartApp() {
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

    @Override
    protected void onResume() {
        super.onResume();

        if (isListenerServiceRunning()) {
            if (listen_sound.getAnimation() == null) {
                listen_sound.startAnimation(AnimationUtils.loadAnimation(act, R.anim.rotate));
            }
        } else if (restartListenerService) {
            setService();
            restartListenerService = false;
        } else if (listen_sound != null && listen_sound.getAnimation() != null) {
            listen_sound.clearAnimation();
        }
    }

    /**
     * Starts the app. All the needed permission are given.
     */
    private void startApp() {
        act = this;

        show_list = (Button) findViewById(R.id.show_list);
        show_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), HiSoundListActivity.class);
                startActivity(intent);
            }
        });

        listen_sound = (ImageView) findViewById(R.id.listen_sound);
        listen_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setService();
            }
        });
    }

    /**
     * Toggle the MainService for background listening
     */
    private void setService() {
        if (!isListenerServiceRunning()) {
            startService(new Intent(getBaseContext(), HiMainService.class));
            listen_sound.startAnimation(AnimationUtils.loadAnimation(act, R.anim.rotate));
        } else {
            stopService(new Intent(getBaseContext(), HiMainService.class));
            listen_sound.clearAnimation();
        }
    }

    /**
     * Checks MainService status
     */
    private boolean isListenerServiceRunning() {
        String listenerService = HiMainService.class.toString().replace("class ", "");

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (listenerService.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
