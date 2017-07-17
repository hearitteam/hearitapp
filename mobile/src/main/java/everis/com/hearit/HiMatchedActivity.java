package everis.com.hearit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by mauriziomento on 22/05/17.
 */

public class HiMatchedActivity extends AppCompatActivity {

    private TextView soundNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched);

        //TODO wake-up screen
        soundNameView = (TextView) findViewById(R.id.matched_sound_name);
        soundNameView.setText(getIntent().getStringExtra("soundName"));

        HiMainActivity.restartListenerService = true;
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}
