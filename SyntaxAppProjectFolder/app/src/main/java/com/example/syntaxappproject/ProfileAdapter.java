package com.example.syntaxappproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that binds a list of {@link Profile} objects to
 * individual profile cards in the admin user management screen.
 *
 * <p>Each card displays the user's name, role label, and status, with a
 * button to navigate to the full profile detail screen.</p>
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    /** The list of profiles to display. */
    private final ArrayList<Profile> profileList;

    /** Parallel list of Firestore document IDs corresponding to each profile. */
    private final ArrayList<String> profileIds;

    /**
     * Constructs the adapter with a list of profiles and their Firestore IDs.
     *
     * @param profileList the profiles to display
     * @param profileIds  the Firestore document IDs for each profile
     */
    public ProfileAdapter(ArrayList<Profile> profileList, ArrayList<String> profileIds) {
        this.profileList = profileList;
        this.profileIds = profileIds;
    }

    /**
     * Inflates the profile item layout and returns a new {@link ProfileViewHolder}.
     *
     * @param parent   the parent view group
     * @param viewType the view type (unused)
     * @return a new {@link ProfileViewHolder}
     */
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_item, parent, false);
        return new ProfileViewHolder(view);
    }

    /**
     * Binds profile data to the view holder at the given position.
     *
     * @param holder   the view holder to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        holder.nameText.setText(profile.getName());
        holder.roleText.setText(getRoleLabel(profile));
        holder.statusText.setText("Active");
        holder.detailsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("profileId", profileIds.get(position));
            bundle.putString("name", profile.getName());
            bundle.putString("email", profile.getEmail());
            bundle.putString("phone", profile.getPhone());
            bundle.putString("role", getRoleLabel(profile));
            bundle.putString("deviceId", profile.getDeviceId());
            bundle.putBoolean("isEntrant", profile.isEntrant());
            bundle.putBoolean("isOrganizer", profile.isOrganizer());
            bundle.putBoolean("isAdmin", profile.isAdmin());
            Navigation.findNavController(v).navigate(R.id.adminProfileDetails, bundle);
        });
    }

    /**
     * Returns the total number of profiles in the list.
     *
     * @return the item count
     */
    @Override
    public int getItemCount() { return profileList.size(); }

    /**
     * Builds a human-readable role label from a profile's boolean role flags.
     *
     * @param profile the profile to derive a role label from
     * @return a comma-separated role string, or {@code "None"} if no roles are set
     */
    private String getRoleLabel(Profile profile) {
        if (profile.isAdmin()) return "Admin";
        List<String> roles = new ArrayList<>();
        if (profile.isOrganizer()) roles.add("Organizer");
        if (profile.isEntrant()) roles.add("Entrant");
        return roles.isEmpty() ? "None" : String.join(", ", roles);
    }

    /**
     * ViewHolder for a single profile card item.
     */
    static class ProfileViewHolder extends RecyclerView.ViewHolder {

        /** Displays the user's full name. */
        TextView nameText;

        /** Displays the user's role label. */
        TextView roleText;

        /** Displays the user's account status. */
        TextView statusText;

        /** Button to navigate to the full profile detail screen. */
        Button detailsButton;

        /**
         * Constructs the ViewHolder and binds child views.
         *
         * @param itemView the inflated item view
         */
        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText      = itemView.findViewById(R.id.tv_profile_name);
            roleText      = itemView.findViewById(R.id.tv_profile_role);
            statusText    = itemView.findViewById(R.id.tv_profile_status);
            detailsButton = itemView.findViewById(R.id.btn_profile_details);
        }
    }
}
