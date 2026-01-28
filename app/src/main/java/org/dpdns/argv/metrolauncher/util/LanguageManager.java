package org.dpdns.argv.metrolauncher.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LanguageManager {

    private static final String PREFS_NAME = "metro_settings";
    private static final String KEY_LANGUAGE = "language";

    public static final String LANG_AUTO = "auto";
    public static final String LANG_EN = "en";
    public static final String LANG_ZH = "zh";
    public static final String LANG_HK = "hk";

    public static Context attachBaseContext(Context context) {
        String lang = getSavedLanguage(context);
        if (LANG_AUTO.equals(lang)) {
            return context;
        }
        return updateResources(context, lang);
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_AUTO);
    }

    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale;
        if (LANG_ZH.equals(language)) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else if (LANG_HK.equals(language)) {
            locale = Locale.TRADITIONAL_CHINESE;
        } else {
            locale = Locale.ENGLISH;
        }
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setLocaleForApi24(config, locale);
            return context.createConfigurationContext(config);
        } else {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        }
    }

    private static void setLocaleForApi24(Configuration config, Locale target) {
        config.setLocale(target);
        LocaleList localeList = new LocaleList(target);
        LocaleList.setDefault(localeList);
        config.setLocales(localeList);
    }
}
