package everis.com.hearit.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import everis.com.hearit.model.Sound;
import everis.com.hearit.model.SoundView;

/**
 * Created by mauriziomento on 18/04/17.
 */

public class HiDBUtils {

    public static void saveSoundIntoDB(String hash, String name, int importance) {

        List<Sound> sounds = Sound.find(Sound.class, "hash = ? and name = ?", hash, name);

        Log.i("dbUtils", "Try to save name: " + name + " hash:" + hash);

        if (sounds.isEmpty()) {
            Log.i("dbUtils", "Saving name: " + name + " hash:" + hash);
            Sound sound = new Sound(hash, name, importance);
            sound.save();
        } else {
            Log.i("dbUtils", "Not empty: " + name + " hash:" + hash);
            for (Sound s : HiDBUtils.getSoundListFromDB()) {
                HiUtils.log("dbUtils", "existing hashes: " + s.getHash() + " - " + s.getName());
            }
        }
    }

    public static ArrayList<Sound> getSoundListFromDB() {
        return new ArrayList<>(Sound.listAll(Sound.class));
    }

    public static void saveSoundViewIntoDB(String name, int importance) {
        SoundView soundView = new SoundView(name, importance);
        soundView.save();
    }


    public static ArrayList<SoundView> getSoundViewListFromDB() {
        return new ArrayList<>(SoundView.listAll(SoundView.class));
    }


    public static void deleteSounds() {
        Sound.deleteAll(Sound.class);
        SoundView.deleteAll(SoundView.class);
    }

    public static void saveHashAndSound(String hash, String name, int importance) {
        saveSoundIntoDB(hash, name, importance);
    }

    public static List<Sound> getSoundsByHash(String hash) {
        return Sound.find(Sound.class, "hash = ?", hash);
    }
}
