package com.example.eventapp.admin;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handles the admin login session state using SharedPreferences.
 * Allows logging in, checking login status, and logging out.
 */
public class AdminSession {

    private static final String PREF = "ADMIN_PREFS";
    private static final String KEY = "IS_ADMIN";

    /**
     * Marks the admin as logged in by saving a flag in SharedPreferences.
     *
     * @param context the application context
     */
    public static void login(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY, true).apply();
    }

    /**
     * Checks whether the admin is currently logged in.
     *
     * @param context the application context
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY, false);
    }

    /**
     * Logs the admin out by clearing the stored login flag.
     *
     * @param context the application context
     */
    public static void logout(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY, false).apply();
    }
}
