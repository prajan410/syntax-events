package com.example.syntaxappproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.ui.AdminFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment where admin can browse all user profiles in the system.
 *
 * <p>This fragment displays a combined list of active and deleted user profiles:</p>
 * <ul>
 *   <li>Active profiles from the {@code profiles} collection (marked with "Active" status)</li>
 *   <li>Deleted/archived profiles from the {@code deleted-profiles} collection (marked with "Inactive" status)</li>
 * </ul>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Real-time search by name or email (updates as user types)</li>
 *   <li>Count badge showing number of profiles in current view</li>
 *   <li>Click on any profile to navigate to {@link AdminProfileDetails} for detailed management</li>
 *   <li>Entrance animations for visual appeal</li>
 * </ul>
 *
 * <p>Navigation: Clicking Done returns to {@link AdminFragment}.</p>
 *
 * @see ProfileAdapter
 * @see AdminProfileDetails
 * @see AdminFragment
 */
public class AdminBrowseProfiles extends Fragment {

    /** Log tag for debugging. */
    private static final String TAG = "AdminBrowseProfiles";

    /** Complete list of all profiles (active + deleted). */
    private ArrayList<Profile> allProfiles;

    /** Firestore document IDs corresponding to each profile. */
    private ArrayList<String> allProfileIds;

    /** Flags indicating whether each profile is deleted (true = deleted, false = active). */
    private ArrayList<Boolean> allDeletedFlags;

    /** Adapter that binds profile data to the RecyclerView. */
    private ProfileAdapter adapter;

    /** RecyclerView displaying the list of profiles. */
    private RecyclerView recyclerView;

    /** Spinner shown while profiles are loading. */
    private View loadingSpinner;

    /** Text shown when no profiles match the current filter. */
    private TextView emptyText;

    /** Search input for filtering profiles by name or email. */
    private EditText searchInput;

    /** Badge showing the number of profiles in the current view. */
    private TextView countBadge;

    /**
     * Empty public constructor required for fragment instantiation.
     */
    public AdminBrowseProfiles() {}

    /**
     * Inflates the layout, initializes views, sets up animations,
     * configures the search bar, and loads profiles from Firestore.
     *
     * @param inflater           the layout inflater used to inflate the fragment's view
     * @param container          the parent view group that the fragment's UI attaches to
     * @param savedInstanceState previously saved instance state, if any
     * @return the inflated view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        recyclerView   = view.findViewById(R.id.recycler_profiles);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        emptyText      = view.findViewById(R.id.emptyText);
        searchInput    = view.findViewById(R.id.searchInput);
        countBadge     = view.findViewById(R.id.profileCountBadge);

        View headerTitle    = view.findViewById(R.id.headerTitle);
        View mainCard       = view.findViewById(R.id.mainCard);
        Button doneButton   = view.findViewById(R.id.doneButton);

        doneButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminFragment);
        });

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        countBadge.animate().alpha(1f)
                .setDuration(300).setStartDelay(200).start();

        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(250).start();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        allProfiles = new ArrayList<>();
        allProfileIds = new ArrayList<>();
        allDeletedFlags = new ArrayList<>();
        adapter = new ProfileAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Search text changed: '" + s.toString() + "'");
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        loadAllProfiles();

        return view;
    }

    /**
     * Fetches active and deleted profiles from Firestore in parallel and
     * populates the RecyclerView once both tasks complete.
     *
     * <p>Uses {@link Tasks#whenAllComplete} to wait for both queries to finish
     * before updating the UI. Active profiles are flagged with {@code false}
     * (not deleted), deleted profiles are flagged with {@code true}.</p>
     */
    private void loadAllProfiles() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> activeTask  = db.collection("profiles").get();
        Task<QuerySnapshot> deletedTask = db.collection("deleted-profiles").get();

        Tasks.whenAllComplete(activeTask, deletedTask).addOnCompleteListener(t -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                allProfiles.clear();
                allProfileIds.clear();
                allDeletedFlags.clear();

                if (activeTask.isSuccessful() && activeTask.getResult() != null) {
                    for (DocumentSnapshot doc : activeTask.getResult()) {
                        Profile p = doc.toObject(Profile.class);
                        if (p != null) {
                            allProfiles.add(p);
                            allProfileIds.add(doc.getId());
                            allDeletedFlags.add(false);
                        }
                    }
                }
                if (deletedTask.isSuccessful() && deletedTask.getResult() != null) {
                    for (DocumentSnapshot doc : deletedTask.getResult()) {
                        Profile p = doc.toObject(Profile.class);
                        if (p != null) {
                            allProfiles.add(p);
                            allProfileIds.add(doc.getId());
                            allDeletedFlags.add(true);
                        }
                    }
                }
                loadingSpinner.setVisibility(View.GONE);

                int count = allProfiles.size();
                countBadge.setText(count + (count == 1 ? " profile" : " profiles"));

                applyFilter();
            });
        });
    }

    /**
     * Applies the search filter to the profile list.
     *
     * <p>Filters by name and email using case-insensitive substring matching.
     * Always filters from the full list ({@code allProfiles}) rather than the
     * previously filtered list to ensure consistent results when backspacing.</p>
     */
    private void applyFilter() {
        String query = searchInput.getText().toString().toLowerCase();
        Log.d(TAG, "Applying filter with query: '" + query + "', total profiles: " + allProfiles.size());

        List<Profile> filteredProfiles = new ArrayList<>();
        List<String> filteredIds = new ArrayList<>();
        List<Boolean> filteredFlags = new ArrayList<>();

        for (int i = 0; i < allProfiles.size(); i++) {
            Profile profile = allProfiles.get(i);
            String name = profile.getName() != null ? profile.getName().toLowerCase() : "";
            String email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";

            boolean matches = query.isEmpty() || name.contains(query) || email.contains(query);

            if (matches) {
                filteredProfiles.add(profile);
                filteredIds.add(allProfileIds.get(i));
                filteredFlags.add(allDeletedFlags.get(i));
            }
        }

        Log.d(TAG, "Filtered profiles count: " + filteredProfiles.size());

        adapter.updateData(filteredProfiles, filteredIds, filteredFlags);

        countBadge.setText(filteredProfiles.size() + (filteredProfiles.size() == 1 ? " profile" : " profiles"));

        if (filteredProfiles.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(query.isEmpty() ? "No profiles found" : "No matching profiles");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}