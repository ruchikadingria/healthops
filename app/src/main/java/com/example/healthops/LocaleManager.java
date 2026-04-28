package com.example.healthops;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleManager {

    private static final String LANGUAGE_KEY = "selected_language";
    private static final String PREFERENCES = "LanguagePreferences";

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        // Save preference
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putString(LANGUAGE_KEY, languageCode).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(LANGUAGE_KEY, "en");
    }

    public static void applyLanguage(Context context) {
        String language = getLanguage(context);
        setLocale(context, language);
    }
}
