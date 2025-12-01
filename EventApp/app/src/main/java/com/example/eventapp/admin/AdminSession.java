package com.example.eventapp.admin;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminSession {

    private static final String PREF = "ADMIN_PREFS";
    private static final String KEY = "IS_ADMIN";

    public static void login(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY, true).apply();
    }

    public static boolean isLoggedIn(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY, false);
    }

    public static void logout(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY, false).apply();
    }
}
