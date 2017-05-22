package everis.com.hearit.utils;

import java.util.ArrayList;
import java.util.List;

import everis.com.hearit.model.HashChunks;
import everis.com.hearit.model.Sound;
import everis.com.hearit.model.SoundView;

/**
 * Created by mauriziomento on 18/04/17.
 */

public class HiDBUtils {

    public static void saveSoundIntoDB(String hash, String name, int importance) {

        List<Sound> sounds = Sound.find(Sound.class, "hash = ? and name = ?", hash, name);
        if (sounds.isEmpty()) {
            Sound sound = new Sound(hash, name, importance);
            sound.save();
        }
    }

    public static void saveHashIntoDB(String hash) {

        List<HashChunks> hashChunkses = HashChunks.find(HashChunks.class, "hash = ?", hash);
        if (hashChunkses.isEmpty()) {
            HashChunks hashChunks = new HashChunks(hash);
            hashChunks.save();
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
        HashChunks.deleteAll(HashChunks.class);
    }

    public static void saveHashAndSound(String hash, String name, int importance) {
        saveSoundIntoDB(hash, name, importance);
        saveHashIntoDB(hash);
    }

    public static List<Sound> getSoundsByHash(String hash) {
        return Sound.find(Sound.class, "hash = ?", hash);
    }
}
