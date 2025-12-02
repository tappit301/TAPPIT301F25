package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Fragment that displays all event categories.
 * Users can tap a category to browse events filtered by that type.
 */
public class ExploreEventsFragment extends Fragment {

    public ExploreEventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore_events, container, false);
    }

    /**
     * Sets up category buttons and handles navigation to category-specific event lists.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBackExplore);
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        LinearLayout catEntertainment = view.findViewById(R.id.catEntertainment);
        LinearLayout catSports = view.findViewById(R.id.catSports);
        LinearLayout catTechnology = view.findViewById(R.id.catTechnology);
        LinearLayout catHealth = view.findViewById(R.id.catHealth);
        LinearLayout catOthers = view.findViewById(R.id.catOthers);

        catEntertainment.setOnClickListener(v -> openCategory("Entertainment"));
        catSports.setOnClickListener(v -> openCategory("Sports"));
        catTechnology.setOnClickListener(v -> openCategory("Technology"));
        catHealth.setOnClickListener(v -> openCategory("Health"));
        catOthers.setOnClickListener(v -> openCategory("Others"));
    }

    /**
     * Navigates to the event list filtered by the selected category.
     *
     * @param category the category name to load
     */
    private void openCategory(String category) {
        Bundle bundle = new Bundle();
        bundle.putString("categoryName", category);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_exploreEventsFragment_to_categoryEventsFragment, bundle);
    }
}
