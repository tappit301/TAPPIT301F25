package com.example.eventapp.utils;

import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {

    private static FirebaseAuth auth;
    private static FirebaseFirestore firestore;
    private static FirebaseStorage storage;

    private static boolean isTestRuntime() {
        return Build.FINGERPRINT.contains("robolectric")
                || Build.HARDWARE.contains("goldfish")
                || Build.MODEL.contains("robolectric");
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            if (isTestRuntime() && FirebaseApp.getApps(null).isEmpty()) {
                return null;
            }
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static FirebaseFirestore getFirestore() {
        if (firestore == null) {

            if (isTestRuntime()) {
                firestore = FirebaseFirestore.getInstance();
                firestore.setFirestoreSettings(
                        new FirebaseFirestoreSettings.Builder()
                                .setPersistenceEnabled(false)
                                .build()
                );
                return firestore;
            }

            firestore = FirebaseFirestore.getInstance();
            firestore.setFirestoreSettings(
                    new FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .build()
            );
        }
        return firestore;
    }

    public static FirebaseStorage getStorage() {
        if (storage == null) {
            if (isTestRuntime() && FirebaseApp.getApps(null).isEmpty()) {
                return null;
            }
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }
}
