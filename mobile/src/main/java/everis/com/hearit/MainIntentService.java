package everis.com.hearit;

import android.app.IntentService;
import android.content.Intent;

import everis.com.hearit.sound.HiMatchingThread;

public class MainIntentService extends IntentService {

    private static final String ACTION_START = "everis.com.hearit.action.START";
    private static final String ACTION_STOP = "everis.com.hearit.action.STOP";

    private HiMatchingThread hiMatchingThread;

    public MainIntentService() {
        super("MainIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                handleActionStart();
            } else if (ACTION_STOP.equals(action)) {
                handleActionStop();
            }
        }
    }


    private void handleActionStart() {
        hiMatchingThread = new HiMatchingThread();
        hiMatchingThread.execute();
    }


    private void handleActionStop() {
        hiMatchingThread.stopRecording();
    }
}
