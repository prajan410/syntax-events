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
 * <p>This adapter is used in {@link AdminBrowseProfiles} to display a combined
 * list of active and deleted profiles. Each card displays:</p>
 * <ul>
 *   <li>User's full name</li>
 *   <li>User's role(s) - Admin, Organizer, Entrant, or None</li>
 *   <li>Status indicator (Active/Inactive) with appropriate color coding</li>
 *   <li>Details button to navigate to {@link AdminProfileDetails} for full management</li>
 * </ul>
 *
 * <p>Deleted profiles are marked with red "Inactive" status, while active profiles
 * show green "Active" status.</p>
 *
 * @see Profile
 * @see AdminBrowseProfiles
 * @see AdminProfileDetails
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    /** List of profiles to display in the RecyclerView. */
    private ArrayList<Profile> profileList;

    /** Firestore document IDs corresponding to each profile. */
    private ArrayList<String> profileIds;

    /** Flags indicating whether each profile is deleted (true = deleted, false = active). */
    private ArrayList<Boolean> deletedFlags;

    /**
     * Constructs the adapter with the provided data lists.
     *
     * @param profileList  the list of profiles to display
     * @param profileIds   the Firestore document IDs for each profile
     * @param deletedFlags flags indicating deleted status for each profile
     */
    public ProfileAdapter(ArrayList<Profile> profileList,
                          ArrayList<String> profileIds,
                          ArrayList<Boolean> deletedFlags) {
        this.profileList = profileList;
        this.profileIds = profileIds;
        this.deletedFlags = deletedFlags;
    }

    /**
     * Creates and inflates the view holder for a profile item.
     *
     * @param parent   the parent view group
     * @param viewType the view type (unused, single type)
     * @return a new ProfileViewHolder
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
     * Sets user name, role label, status indicator, and click listener for details button.
     *
     * @param holder   the view holder to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        boolean isDeleted = deletedFlags.get(position);

        holder.nameText.setText(profile.getName() != null ? profile.getName() : "Unknown");
        holder.roleText.setText(getRoleLabel(profile));

        if (isDeleted) {
            holder.statusText.setText("Inactive");
            holder.statusText.setTextColor(0xFFE53935);
        } else {
            holder.statusText.setText("Active");
            holder.statusText.setTextColor(0xFF43A047);
        }

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
            bundle.putBoolean("isDeleted", isDeleted);
            Navigation.findNavController(v).navigate(R.id.adminProfileDetails, bundle);
        });
    }

    /**
     * Returns the total number of profiles in the list.
     *
     * @return the number of profiles
     */
    @Override
    public int getItemCount() {
        return profileList.size();
    }

    /**
     * Updates the adapter with new data and refreshes the RecyclerView.
     *
     * <p>This method clears the existing lists and replaces them with the new data,
     * then notifies the RecyclerView to redraw. Used primarily for search/filtering
     * in {@link AdminBrowseProfiles}.</p>
     *
     * @param newProfileList   the new list of profiles
     * @param newProfileIds    the new list of profile IDs
     * @param newDeletedFlags  the new list of deleted flags
     */
    public void updateData(List<Profile> newProfileList, List<String> newProfileIds, List<Boolean> newDeletedFlags) {
        this.profileList.clear();
        this.profileList.addAll(newProfileList);
        this.profileIds.clear();
        this.profileIds.addAll(newProfileIds);
        this.deletedFlags.clear();
        this.deletedFlags.addAll(newDeletedFlags);
        notifyDataSetChanged();
    }

    /**
     * Builds a human-readable role label from a profile's boolean role flags.
     *
     * <p>Prioritizes Admin over other roles since it's the highest privilege.
     * For users with multiple roles, returns comma-separated list of roles.</p>
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
     * ViewHolder that caches references to the views in each profile card.
     * Provides efficient access to UI elements without repeated findViewById calls.
     */
    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        /** Displays the user's full name. */
        TextView nameText;

        /** Displays the user's role(s). */
        TextView roleText;

        /** Displays the user's account status (Active/Inactive). */
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
            nameText = itemView.findViewById(R.id.tv_profile_name);
            roleText = itemView.findViewById(R.id.tv_profile_role);
            statusText = itemView.findViewById(R.id.tv_profile_status);
            detailsButton = itemView.findViewById(R.id.btn_profile_details);
        }
    }
}