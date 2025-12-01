package com.example.eventapp;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class ExploreEventsFragmentTest {

    /** Launch fragment without FragmentScenario */
    private ExploreEventsFragment launchFragment() {
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class)
                .setup()
                .get();

        ExploreEventsFragment fragment = new ExploreEventsFragment();

        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.add(fragment, null);
        tx.commitNow();

        return fragment;
    }

    @Test
    public void fragmentLaunchesSuccessfully() {
        ExploreEventsFragment fragment = launchFragment();
        assertNotNull(fragment.getView());
    }

    @Test
    public void viewsArePresent() {
        ExploreEventsFragment fragment = launchFragment();

        RecyclerView rv = fragment.requireView().findViewById(R.id.rvExploreEvents);
        LinearLayout emptyLayout = fragment.requireView().findViewById(R.id.exploreEmptyLayout);
        ImageButton filterButton = fragment.requireView().findViewById(R.id.btnFilter);

        assertNotNull(rv);
        assertNotNull(emptyLayout);
        assertNotNull(filterButton);
    }

    @Test
    public void filterButtonDoesNotCrashWhenClicked() {
        ExploreEventsFragment fragment = launchFragment();

        ImageButton btn = fragment.requireView().findViewById(R.id.btnFilter);
        assertNotNull(btn);

        // Robolectric-safe click
        btn.performClick();

        assertTrue(true);
    }

    @Test
    public void emptyStateAppearsForEmptyList() {
        ExploreEventsFragment fragment = launchFragment();

        LinearLayout emptyLayout =
                fragment.requireView().findViewById(R.id.exploreEmptyLayout);
        RecyclerView rv =
                fragment.requireView().findViewById(R.id.rvExploreEvents);

        // Clear list through reflection
        List<Event> filtered =
                TestUtils.getPrivateList(fragment, "filteredList");
        filtered.clear();

        TestUtils.invokePrivate(fragment, "updateEmptyState");

        assertEquals(View.VISIBLE, emptyLayout.getVisibility());
        assertEquals(View.GONE, rv.getVisibility());
    }

    @Test
    public void recyclerViewUpdatesWhenEventsAdded() {
        ExploreEventsFragment fragment = launchFragment();

        List<Event> filtered =
                TestUtils.getPrivateList(fragment, "filteredList");

        filtered.clear();
        filtered.add(new Event(
                "Concert", "Desc", "01/12/2099",
                "20:00", "Hall", "Entertainment"));

        TestUtils.invokePrivate(fragment, "updateEmptyState");

        RecyclerView rv = fragment.requireView().findViewById(R.id.rvExploreEvents);

        assertEquals(1, rv.getAdapter().getItemCount());
    }
}
