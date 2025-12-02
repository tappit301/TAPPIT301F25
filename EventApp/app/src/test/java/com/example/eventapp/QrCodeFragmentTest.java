package com.example.eventapp;

import static org.junit.Assert.*;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;

public class QrCodeFragmentTest {

    private FragmentScenario<QrCodeFragment> launch() {
        return FragmentScenario.launchInContainer(
                QrCodeFragment.class,
                null,
                R.style.Theme_EventApp,
                (FragmentFactory) null   // 👈 resolves ambiguous overload
        );
    }

    @Test
    public void fragmentLaunches() {
        launch().onFragment(fragment -> assertNotNull(fragment.getView()));
    }

    @Test
    public void qrMessage_defaultWarningShown() {
        launch().onFragment(fragment -> {
            TextView tv = fragment.requireView().findViewById(R.id.qrText);
            assertNotNull(tv);
            assertEquals("Scan this QR to view event details", tv.getText().toString());
        });
    }

    @Test
    public void backButtonDoesNotCrash() {
        launch().onFragment(fragment -> {
            View back = fragment.requireView().findViewById(R.id.btnBack);
            assertNotNull(back);
            back.performClick(); // works after import
        });
    }
}
