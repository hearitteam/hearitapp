package everis.com.hearit.utils;

import java.util.ArrayList;

import everis.com.hearit.model.Sound;

/**
 * Created by mauriziomento on 18/04/17.
 */

public class HiDBUtils {

    public static void saveSoundIntoDB(String name, int importance) {
        Sound sound = new Sound(name, importance);
        sound.save();
    }

    public static ArrayList<Sound> getSoundListFromDB() {
        return new ArrayList<>(Sound.listAll(Sound.class));
    }

    public static void deleteSounds() {
        Sound.deleteAll(Sound.class);
    }
}
