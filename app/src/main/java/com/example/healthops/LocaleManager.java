package com.example.healthops;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleManager {

    private static final String LANGUAGE_KEY = "selected_language";
    private static final String PREFERENCES = "LanguagePreferences";

    // ✅ Set & Save Language
    public static Context setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        // Save language
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putString(LANGUAGE_KEY, languageCode).apply();

        return context.createConfigurationContext(config); // ✅ modern approach
    }

    // ✅ Get saved language
    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(LANGUAGE_KEY, "en"); // default English
    }

    // ✅ Apply saved language
    public static Context applyLanguage(Context context) {
        return setLocale(context, getLanguage(context));
    }
}