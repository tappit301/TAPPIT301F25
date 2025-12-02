package com.example.eventapp;

import static org.junit.Assert.*;

import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Test;

public class ManageEventsFragmentTest {

    private FragmentScenario<ManageEventsFragment> launch() {
        return FragmentScenario.launchInContainer(
                ManageEventsFragment.class,
                null,
                R.style.Theme_EventApp,
                new androidx.fragment.app.FragmentFactory()   // ← FIX
        );
    }

    @Test
    public void fragmentLaunches() {
        launch().onFragment(f -> assertNotNull(f.getView()));
    }

    @Test
    public void recyclerViewPresent() {
        launch().onFragment(f -> {
            RecyclerView rv = f.requireView().findViewById(R.id.recyclerViewList);
            assertNotNull(rv);
        });
    }

    @Test
    public void editButtonDoesNotCrash() {
        launch().onFragment(f -> {
            View edit = f.requireView().findViewById(R.id.btnEditEvent);
            assertNotNull(edit);
            edit.performClick();
        });
    }
}
