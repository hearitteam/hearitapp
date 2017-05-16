package everis.com.hearit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import everis.com.hearit.sound.HiMatchingThread;

public class MainService extends Service {

    private HiMatchingThread hiMatchingThread;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        hiMatchingThread = new HiMatchingThread();
        hiMatchingThread.execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        hiMatchingThread.stopRecording();
    }
}
