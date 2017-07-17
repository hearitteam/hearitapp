package everis.com.hearit.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class HiUtils {

    public static final String AUDIO_RECORDER_FOLDER = "HearIt/HiSound";
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = "wav";

    public static void log(String TAG, String msg) {
        Log.v(TAG, msg);
    }

    public static void deleteAudioFile(String soundName) {
        deleteRecursive(new File(HiUtils.getFilePath(soundName)));
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

    public static String getFilePath(String filename, String extension) {
        return (getSoundsPath() + "/" + filename + "." + extension);
    }

    public static String getFilePath(String filename) {
        return getFilePath(filename, AUDIO_RECORDER_FILE_EXT_WAV);
    }

    public static File GetFile(String filename) {
        return new File(getFilePath(filename));
    }

    public static File createOrGetFile(String filename) {
        return createOrGetFile(filename, AUDIO_RECORDER_FILE_EXT_WAV);
    }

    public static File createOrGetFile(String filename, String extension) {
        try {
            File file = new File(HiUtils.getFilePath(filename, extension));
            file.createNewFile();
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }
    }
}
