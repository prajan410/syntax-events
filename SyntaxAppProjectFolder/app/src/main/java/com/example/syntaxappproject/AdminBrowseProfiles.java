package com.example.syntaxappproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Fragment where admin can browse all user profiles in the system.
 * Loads active profiles from the {@code profiles} collection and deleted
 * profiles from the {@code deleted-profiles} collection, then displays
 * them together in a RecyclerView with an Active/Inactive status indicator.
 */
public class AdminBrowseProfiles extends Fragment {

    private ArrayList<Profile> profileList;
    private ArrayList<String> profileIds;
    private ArrayList<Boolean> deletedFlags;
    private ProfileAdapter adapter;
    private RecyclerView recyclerView;
    private View loadingSpinner;
    private View emptyText;

    /**
     * Empty public constructor required for fragment instantiation.
     */
    public AdminBrowseProfiles() {}

    /**
     * Creates the view for the admin browse profiles page.
     * Sets up the RecyclerView, entrance animations, and triggers a parallel
     * Firestore load of both active and deleted profiles.
     *
     * @param inflater           used to inflate the fragment layout
     * @param container          parent view that the fragment layout will be attached to
     * @param savedInstanceState previous saved state if there is one
     * @return the root view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        recyclerView   = view.findViewById(R.id.recycler_profiles);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        emptyText      = view.findViewById(R.id.emptyText);

        View headerTitle    = view.findViewById(R.id.headerTitle);
        View mainCard       = view.findViewById(R.id.mainCard);
        TextView countBadge = view.findViewById(R.id.profileCountBadge);
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

        profileList  = new ArrayList<>();
        profileIds   = new ArrayList<>();
        deletedFlags = new ArrayList<>();
        adapter      = new ProfileAdapter(profileList, profileIds, deletedFlags);
        recyclerView.setAdapter(adapter);

        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        loadAllProfiles(countBadge);

        return view;
    }

    /**
     * Fetches active and deleted profiles from Firestore in parallel and
     * populates the RecyclerView once both tasks complete.
     * Active profiles come from the {@code profiles} collection and are
     * flagged {@code false}; deleted profiles come from {@code deleted-profiles}
     * and are flagged {@code true}.
     *
     * @param countBadge the TextView used to display the total profile count
     */
    private void loadAllProfiles(TextView countBadge) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> activeTask  = db.collection("profiles").get();
        Task<QuerySnapshot> deletedTask = db.collection("deleted-profiles").get();

        Tasks.whenAllComplete(activeTask, deletedTask).addOnCompleteListener(t -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                profileList.clear();
                profileIds.clear();
                deletedFlags.clear();

                if (activeTask.isSuccessful() && activeTask.getResult() != null) {
                    for (DocumentSnapshot doc : activeTask.getResult()) {
                        Profile p = doc.toObject(Profile.class);
                        if (p != null) {
                            profileList.add(p);
                            profileIds.add(doc.getId());
                            deletedFlags.add(false);
                        }
                    }
                }

                if (deletedTask.isSuccessful() && deletedTask.getResult() != null) {
                    for (DocumentSnapshot doc : deletedTask.getResult()) {
                        Profile p = doc.toObject(Profile.class);
                        if (p != null) {
                            profileList.add(p);
                            profileIds.add(doc.getId());
                            deletedFlags.add(true);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                loadingSpinner.setVisibility(View.GONE);

                int count = profileList.size();
                countBadge.setText(count + (count == 1 ? " profile" : " profiles"));

                if (profileList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);
                }
            });
        });
    }
}