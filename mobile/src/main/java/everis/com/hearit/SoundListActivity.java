package everis.com.hearit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 22/02/16.
 */
public class SoundListActivity extends AppCompatActivity {

    private SoundAdapter soundAdapter;
    //private ArrayList<Sound> soundList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //		.setAction("Action", null).show();
                //Intent intent = new Intent(getBaseContext(), RegisterSoundActivity.class);
                Intent intent = new Intent(getBaseContext(), RegisterSoundActivity.class);
                startActivity(intent);
            }
        });

        final ListView listview = (ListView) findViewById(R.id.listview);

		/*
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick (AdapterView<?> parent, final View view,
			                         int position, long id) {
				final Sound item = (Sound) parent.getItemAtPosition(position);
				view.animate().setDuration(2000).alpha(0)
						.withEndAction(new Runnable() {
							@Override
							public void run () {
								//soundList.remove(item);
								soundAdapter.notifyDataSetChanged();
								view.setAlpha(1);
							}
						});
			}

		});
		*/

        soundAdapter = new SoundAdapter(this, HiUtils.getSoundList(this));
        listview.setAdapter(soundAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        //soundList = new ArrayList<Sound>();
        //soundAdapter = new SoundAdapter(this, soundList);

        //soundAdapter.notifyDataSetChanged();

        soundAdapter.swapItems(HiUtils.getSoundList(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            HiSharedPreferences.clearAll(this);
            HiUtils.deleteAudioFiles();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
