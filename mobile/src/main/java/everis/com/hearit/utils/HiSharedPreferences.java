package everis.com.hearit.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class HiSharedPreferences {

    private final static String PREFS_NAME = "hi_data";
    private final static String SP_TIME_PROCESS_START = "sp_time_process_start";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, 0);
    }

    private static SharedPreferences.Editor getEditor(Context ctx) {
        return getSharedPreferences(ctx).edit();
    }

    public static void clearAll(Context ctx) {
        getSharedPreferences(ctx).edit().clear().commit();
    }

    public static long getSP_TIME_PROCESS_START(Context ctx) {
        return getSharedPreferences(ctx).getLong(SP_TIME_PROCESS_START, 0l);
    }

    public static void setSP_TIME_PROCESS_START(Context ctx, long time) {
        getEditor(ctx).putLong(SP_TIME_PROCESS_START, time).commit();
    }

    public static long getProcessTime(Context ctx, long time) {
        return time - getSP_TIME_PROCESS_START(ctx);
    }
}
