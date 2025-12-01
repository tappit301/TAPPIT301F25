package com.example.eventapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class QrCodeFragmentTest {

    private QrCodeFragment launch(Bundle args) {
        FragmentScenario<QrCodeFragment> scenario =
                FragmentScenario.launchInContainer(
                        QrCodeFragment.class,
                        args,
                        R.style.Theme_EventApp,
                        (androidx.fragment.app.FragmentFactory) null   // ✅ FIX
                );

        final QrCodeFragment[] holder = new QrCodeFragment[1];
        scenario.onFragment(f -> holder[0] = f);
        return holder[0];
    }

    @Test
    public void fragmentLaunches() {
        QrCodeFragment frag = launch(new Bundle());
        assertNotNull(frag.requireView());
    }

    @Test
    public void qrDataEmpty_showsWarning() {
        Bundle args = new Bundle();
        args.putString("qrData", "");

        QrCodeFragment frag = launch(args);

        TextView tv = frag.requireView().findViewById(R.id.qrText);
        assertEquals("⚠️ No QR data available", tv.getText().toString());
    }

    @Test
    public void qrDataPresent_generatesBitmap() {
        Bundle args = new Bundle();
        args.putString("qrData", "HELLO WORLD");

        QrCodeFragment frag = launch(args);

        ImageView img = frag.requireView().findViewById(R.id.qrImage);
        assertNotNull(img.getDrawable());
    }

    @Test
    public void backButton_navigatesCorrectly() {
        Bundle args = new Bundle();
        args.putBoolean("cameFromDetails", true);

        QrCodeFragment frag = launch(args);

        TestNavHostController nav =
                new TestNavHostController(ApplicationProvider.getApplicationContext());
        nav.setGraph(R.navigation.nav_graph);

        Navigation.setViewNavController(frag.requireView(), nav);

        View btn = frag.requireView().findViewById(R.id.btnBack);
        btn.performClick();

        assertNotNull(nav.getCurrentDestination());
    }
}
