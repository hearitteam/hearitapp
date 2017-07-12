package everis.com.hearit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by mauriziomento on 22/05/17.
 */

public class MatchedActivity extends AppCompatActivity {

    private TextView soundNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched);

        //TODO wake-up screen

        soundNameView = (TextView) findViewById(R.id.matched_sound_name);

        String soundName = getIntent().getStringExtra("soundName");

        soundNameView.setText(soundName);

        MainActivity.restartListenerService = true;

        // Restart Service
        //startService(new Intent(getBaseContext(), MainService.class));
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}
