package com.example.eventapp.admin;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminSession {

    private static final String PREF_NAME = "admin_session";
    private static final String KEY_LOGGED_IN = "logged_in";

    public static void login(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply();
    }

    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }
}
