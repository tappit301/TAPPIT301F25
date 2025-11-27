package com.example.eventapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class GuestUtils {

    private static final String PREFS_NAME = "APP_PREFS";
    private static final String KEY_GUEST_ID = "GUEST_ID";

    /**
     * Returns a persistent guestId for this device.
     * If none exists, generates a new UUID and saves it.
     */
    public static String getOrCreateGuestId(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String existing = prefs.getString(KEY_GUEST_ID, null);
        if (existing != null) {
            return existing;
        }

        String newId = UUID.randomUUID().toString();
        prefs.edit().putString(KEY_GUEST_ID, newId).apply();
        return newId;
    }
}
