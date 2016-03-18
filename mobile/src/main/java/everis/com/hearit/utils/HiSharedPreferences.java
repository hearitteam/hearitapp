package everis.com.hearit.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class HiSharedPreferences {

	private final static String PREFS_NAME = "hi_data";
	private final static String SP_SOUND_LIST = "sp_sound_list";

	private static SharedPreferences getSharedPreferences (Context ctx) {
		return ctx.getSharedPreferences(PREFS_NAME, 0);
	}

	private static SharedPreferences.Editor getEditor (Context ctx) {
		return getSharedPreferences(ctx).edit();
	}


	public static HashMap<String, Integer> getSounds (Context ctx) {
		String value = getSharedPreferences(ctx).getString(SP_SOUND_LIST, null);

		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		HashMap<String, Integer> map;
		try {
			map = gson.fromJson(value, new TypeToken<HashMap<String, Integer>>() {}.getType());

			if (map == null)
				map = new HashMap<>();
		} catch (Exception e) {
			map = new HashMap<>();
		}
		return map;
	}


	public static int addSound (Context ctx, String value) {
		int result;
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();

		HashMap<String, Integer> map = getSounds(ctx);
		if (!map.containsKey(value)) {
			map.put(value, 1);
			result = 1;
		} else {
			result = 0;
		}

		String value_str = gson.toJson(map);
		getEditor(ctx).putString(SP_SOUND_LIST, value_str).commit();

		HiUtils.log("Added Sound to list: " + value);

		return result;
	}

	public static void removeSound (Context ctx, String value) {
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();

		HashMap<String, Integer> map = getSounds(ctx);
		map.remove(value);

		String value_str = gson.toJson(map);
		getEditor(ctx).putString(SP_SOUND_LIST, value_str).commit();

		HiUtils.log("Removed Sound from the list: " + value);
	}

	public static void clearAll (Context ctx) {
		getSharedPreferences(ctx).edit().clear().commit();
	}
}
