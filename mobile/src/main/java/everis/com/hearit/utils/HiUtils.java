package everis.com.hearit.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

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

    public static void deleteAudioFiles() {
        deleteRecursive(new File(getSoundsPath()));
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
