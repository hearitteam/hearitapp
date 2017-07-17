package everis.com.hearit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import everis.com.hearit.model.HiSoundView;
import everis.com.hearit.sound.HiSoundParams;
import everis.com.hearit.utils.HiDBUtils;
import everis.com.hearit.utils.HiUtils;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class HiSoundAdapter extends ArrayAdapter<HiSoundView> {

    private Context ctx;
    private ArrayList<HiSoundView> list;
    private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public HiSoundAdapter(Context context, List<HiSoundView> objects) {
        super(context, R.layout.list_item, R.id.sound_name, objects);

        this.ctx = context;
        this.list = new ArrayList<>(objects);

        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i).getName(), i);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.sound_name);
        textView.setText(list.get(position).getName());

        TextView importance = (TextView) rowView.findViewById(R.id.importance);
        int drawable = R.drawable.round_green;
        if (list.get(position).getImportance() == 0)
            drawable = R.drawable.round_green;
        else if (list.get(position).getImportance() == 1)
            drawable = R.drawable.round_yellow;
        else if (list.get(position).getImportance() == 2)
            drawable = R.drawable.round_red;

        importance.setBackground(ctx.getDrawable(drawable));

        ImageView sound_icon = (ImageView) rowView.findViewById(R.id.sound_icon);
        sound_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PlayRecording(rowView, position).execute(list.get(position).getName());
            }
        });


        ImageView delete_sound_icon = (ImageView) rowView.findViewById(R.id.delete_sound_icon);
        delete_sound_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(list.get(position).getName());
            }
        });

        return rowView;
    }

    @Override
    public long getItemId(int position) {
        String item = getItem(position).getName();
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

   /* public void UpdateItems(ArrayList<HiSoundView> list) {
        this.list.clear();
        this.list.addAll(list);

        this.notifyDataSetChanged();
    }*/

    private void deleteItem(final String soundName) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this.ctx);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HiDBUtils.deleteSound(soundName);
                HiUtils.deleteAudioFile(soundName);
                ((HiSoundListActivity) ctx).RefreshAdapter();
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private class PlayRecording extends AsyncTask<String, Void, Void> {

        private View rowView;
        private int position;
        private AudioTrack audioTrack;

        public PlayRecording(View rowView, int position) {
            this.rowView = rowView;
            this.position = position;
        }

        @Override
        protected Void doInBackground(String... params) {

            File file = HiUtils.GetFile(params[0]);
            int musicLength = (int) (file.length() / 2);
            try {
                //size & length of the file
                short[] music = new short[musicLength];

                //  Create a DataInputStream to read the audio data from the saved file
                InputStream is = new FileInputStream(file);

                //  Read the file into the "music" array
                BufferedInputStream bis = new BufferedInputStream(is);
                DataInputStream dis = new DataInputStream(bis);

                // Read the file into the music array.
                int i = 0;
                while (dis.available() > 0) {
                    music[i] = dis.readShort();
                    i++;
                }

                //  Close the input stream
                dis.close();

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, HiSoundParams.RECORDER_SAMPLERATE, HiSoundParams.PLAYER_CHANNELS, HiSoundParams.RECORDER_AUDIO_ENCODING, music.length, AudioTrack.MODE_STREAM);
                audioTrack.play();
                audioTrack.write(music, 0, musicLength);
                audioTrack.setNotificationMarkerPosition(musicLength);
                audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {

                    @Override
                    public void onMarkerReached(AudioTrack track) {
                        try {
                            audioTrack.stop();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        setPlayButton();
                    }

                    @Override
                    public void onPeriodicNotification(AudioTrack track) {
                    }
                });

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            setStopButton();
        }

        private void setStopButton() {
            ImageView sound_icon = (ImageView) rowView.findViewById(R.id.sound_icon);
            sound_icon.setImageDrawable(ctx.getDrawable(R.drawable.stop_circle_outline));

            sound_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        audioTrack.stop();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    setPlayButton();
                }
            });
        }

        private void setPlayButton() {
            ImageView sound_icon = (ImageView) rowView.findViewById(R.id.sound_icon);
            sound_icon.setImageDrawable(ctx.getDrawable(R.drawable.play_circle_outline));

            sound_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new PlayRecording(rowView, position).execute(list.get(position).getName());
                }
            });
        }
    }
}
