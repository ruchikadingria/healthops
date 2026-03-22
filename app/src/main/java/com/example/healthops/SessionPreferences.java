package com.example.healthops;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionPreferences {

    private static final String PREFS = "healthops_session";
    private static final String KEY_ROLE = "role";
    private static final String KEY_DISPLAY_NAME = "display_name";

    public static final String ROLE_STAFF = "staff";
    public static final String ROLE_PATIENT = "patient";

    private SessionPreferences() {}

    public static void setStaffSession(Context context, String emailOrId) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        e.putString(KEY_ROLE, ROLE_STAFF);
        e.putString(KEY_DISPLAY_NAME, friendlyName(emailOrId));
        e.apply();
    }

    public static void setPatientSession(Context context, String patientIdOrEmail) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        e.putString(KEY_ROLE, ROLE_PATIENT);
        e.putString(KEY_DISPLAY_NAME, friendlyName(patientIdOrEmail));
        e.apply();
    }

    private static String friendlyName(String raw) {
        if (raw == null || raw.isEmpty()) return "User";
        String s = raw.trim();
        if (s.contains("@")) {
            s = s.substring(0, s.indexOf('@'));
        }
        if (s.isEmpty()) return "User";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static boolean isPatient(Context context) {
        return ROLE_PATIENT.equals(
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ROLE, ROLE_STAFF));
    }

    public static String getDisplayName(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_DISPLAY_NAME, "User");
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
