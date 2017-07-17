package everis.com.hearit.utils;

import java.util.ArrayList;
import java.util.List;

import everis.com.hearit.model.HiSound;
import everis.com.hearit.model.HiSoundView;

/**
 * Created by mauriziomento on 18/04/17.
 */

public class HiDBUtils {

    public static void saveSoundIntoDB(String hash, String name, int importance) {

        List<HiSound> sounds = HiSound.find(HiSound.class, "hash = ? and name = ?", hash, name);

        if (sounds.isEmpty()) {
            HiSound sound = new HiSound(hash, name, importance);
            sound.save();
        }
    }

    public static ArrayList<HiSound> getSoundListFromDB() {
        return new ArrayList<>(HiSound.listAll(HiSound.class));
    }

    public static void saveSoundViewIntoDB(String name, int importance) {
        HiSoundView soundView = new HiSoundView(name, importance);
        soundView.save();
    }


    public static ArrayList<HiSoundView> getSoundViewListFromDB() {
        return new ArrayList<>(HiSoundView.listAll(HiSoundView.class));
    }


    public static void deleteSounds() {
        HiSound.deleteAll(HiSound.class);
        HiSoundView.deleteAll(HiSoundView.class);
    }

    public static void deleteSound(String soundName) {
        HiSound.deleteAll(HiSound.class, "name = ?", soundName);
        HiSoundView.deleteAll(HiSoundView.class, "name = ?", soundName);
    }

    public static void saveHashAndSound(String hash, String name, int importance) {
        saveSoundIntoDB(hash, name, importance);
    }

    public static List<HiSound> getSoundsByHash(String hash) {
        try {
            return HiSound.find(HiSound.class, "hash = ?", hash);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
