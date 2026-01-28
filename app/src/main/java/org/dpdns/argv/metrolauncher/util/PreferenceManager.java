package org.dpdns.argv.metrolauncher.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "metro_prefs";
    private static final String KEY_LOCK_LAYOUT = "lock_layout";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isLayoutLocked(Context context) {
        return getPrefs(context).getBoolean(KEY_LOCK_LAYOUT, false);
    }

    public static void setLayoutLocked(Context context, boolean locked) {
        getPrefs(context).edit().putBoolean(KEY_LOCK_LAYOUT, locked).apply();
    }
}
