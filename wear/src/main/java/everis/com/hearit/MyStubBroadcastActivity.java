package everis.com.hearit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class MyStubBroadcastActivity extends Activity {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = new Intent();
		i.setAction("everis.com.hearit.SHOW_NOTIFICATION");
		i.putExtra(MyPostNotificationReceiver.CONTENT_KEY, getString(R.string.title));
		sendBroadcast(i);
		finish();
	}
}
