package everis.com.hearit.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class HiSharedPreferences {

    private final static String PREFS_NAME = "hi_data";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, 0);
    }

    private static SharedPreferences.Editor getEditor(Context ctx) {
        return getSharedPreferences(ctx).edit();
    }

    public static void clearAll(Context ctx) {
        getSharedPreferences(ctx).edit().clear().commit();
    }
}
