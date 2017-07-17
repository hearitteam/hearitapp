package everis.com.hearit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mauriziomento on 22/05/17.
 */

public class HiMatchedReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {

        String soundName = intent.getStringExtra("soundName");

        Intent matchedActivity = new Intent(context, HiMatchedActivity.class);
        matchedActivity.putExtra("soundName", soundName);
        matchedActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(matchedActivity);
    }
}
