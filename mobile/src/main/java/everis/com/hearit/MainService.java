package everis.com.hearit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import everis.com.hearit.model.Sound;
import everis.com.hearit.sound.HiMatchingThread;
import everis.com.hearit.utils.HiDBUtils;
import everis.com.hearit.utils.HiUtils;

public class MainService extends Service implements HiMatchingThread.HiMatchingCallback {

    private HiMatchingThread hiMatchingThread;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        List<Sound> allSound = HiDBUtils.getSoundListFromDB();

        Map<String, Integer> soundLengthMap = new HashMap<>();
        for (Sound s : allSound) {
            Integer count = soundLengthMap.get(s.getName());
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            soundLengthMap.put(s.getName(), count);
        }

        for (int i = 0; i < allSound.size(); i++) {
            Sound currentSound = allSound.get(i);
            currentSound.setLength(soundLengthMap.get(currentSound.getName()));
            allSound.set(i, currentSound);
        }

        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        hiMatchingThread = new HiMatchingThread(this, allSound);
        hiMatchingThread.execute();

        return START_STICKY;
    }

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
