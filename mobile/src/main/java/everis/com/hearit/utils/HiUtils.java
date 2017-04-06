package everis.com.hearit.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import everis.com.hearit.Sound;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class HiUtils {

    public static final String AUDIO_RECORDER_FOLDER = "HearIt/Sound";
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";

    public static void log(String TAG, String msg) {
        Log.v(TAG, msg);
    }

    public static void log(String msg) {
        Log.v("Hear-It", msg);
    }

    public static void toastShort(Context ctx, String msg) {
        toast(ctx, msg, true);
    }

    public static void toastLong(Context ctx, String msg) {
        toast(ctx, msg, false);
    }

    private static void toast(Context ctx, String msg, boolean lengthShort) {
        if (ctx != null) {
            if (lengthShort)
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        }
    }

    public static File getFilesDirectory() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearIt/sound/";
        return new File(path);
    }

    public static ArrayList<Sound> getSoundList(Context ctx) {

        ArrayList<Sound> soundList = new ArrayList<>();
        HashMap<String, Integer> sounds = HiSharedPreferences.getSounds(ctx);

        for (HashMap.Entry<String, Integer> entry : sounds.entrySet()) {
            Sound s = new Sound(entry.getKey(), entry.getValue());
            if (s != null)
                soundList.add(s);
        }

        return soundList;
    }

    public static Sound getSoundFromName(Context ctx, String name) {
        for (Sound s : getSoundList(ctx)) {
            if (s.getName().equals(name))
                return s;
        }
        return null;
    }

    public static void deleteAudioFiles() {
        deleteRecursive(getFilesDirectory());
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        if (fileOrDirectory.isFile())
            fileOrDirectory.delete();
    }

    public static String getSoundsPath() {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), HiUtils.AUDIO_RECORDER_FOLDER);
        return folder.getAbsolutePath();
    }

    public static String getFilePath(String filename) {
        return (getSoundsPath() + "/" + filename +
                AUDIO_RECORDER_FILE_EXT_WAV);
    }
}
