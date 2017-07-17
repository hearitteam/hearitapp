package everis.com.hearit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.List;

import everis.com.hearit.model.HiSound;
import everis.com.hearit.sound.HiMatchingThread;
import everis.com.hearit.utils.HiDBUtils;

public class HiMainService extends Service implements HiMatchingThread.HiMatchingCallback {

    private HiMatchingThread hiMatchingThread;

    public HiMainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        List<HiSound> allSound = HiDBUtils.getSoundListFromDB();

        /*for (HiSound s : allSound) {
            HiUtils.log("HiMatchingAlgorithm", "Existing: " + s.getHash() + " -" + s.getName());
        }*/

        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        hiMatchingThread = new HiMatchingThread(this, allSound);
        hiMatchingThread.execute();

        return START_STICKY;
    }

    /**
     * Sends broadcast message for the matched sound
     * <p>everis.com.hearit.matchedSound</p>
     */
    public void sendMatchedBroadcast(String soundNameMatched) {
        Intent i = new Intent("everis.com.hearit.matchedSound");
        i.putExtra("soundName", soundNameMatched);
        sendBroadcast(i);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        hiMatchingThread.stopRecording();
        hiMatchingThread.cancel(true);
    }

    @Override
    public void onSoundMatched(String soundNameMatched) {
        sendMatchedBroadcast(soundNameMatched);
    }
}
