package everis.com.hearit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import everis.com.hearit.utils.HiDBUtils;
import everis.com.hearit.utils.HiSharedPreferences;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 22/02/16.
 */
public class HiSoundListActivity extends AppCompatActivity {

    private ListView listview;

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
                Intent intent = new Intent(getBaseContext(), HiRecordSoundActivity.class);
                startActivity(intent);
            }
        });

        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(new HiSoundAdapter(this, HiDBUtils.getSoundViewListFromDB()));
    }

    @Override
    public void onResume() {
        super.onResume();

        listview.setAdapter(new HiSoundAdapter(this, HiDBUtils.getSoundViewListFromDB()));
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
            HiDBUtils.deleteSounds();
            HiUtils.deleteAudioFiles();

            listview.setAdapter(null);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void RefreshAdapter() {
        listview.setAdapter(new HiSoundAdapter(this, HiDBUtils.getSoundViewListFromDB()));
    }
}
