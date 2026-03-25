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
 * <p>Each card displays the user's name, role label, and active/inactive
 * status, with a button to navigate to the full profile detail screen.</p>
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private final ArrayList<Profile> profileList;
    private final ArrayList<String> profileIds;
    private final ArrayList<Boolean> deletedFlags;

    public ProfileAdapter(ArrayList<Profile> profileList,
                          ArrayList<String> profileIds,
                          ArrayList<Boolean> deletedFlags) {
        this.profileList  = profileList;
        this.profileIds   = profileIds;
        this.deletedFlags = deletedFlags;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_item, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile   = profileList.get(position);
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
            bundle.putString("profileId",    profileIds.get(position));
            bundle.putString("name",         profile.getName());
            bundle.putString("email",        profile.getEmail());
            bundle.putString("phone",        profile.getPhone());
            bundle.putString("role",         getRoleLabel(profile));
            bundle.putString("deviceId",     profile.getDeviceId());
            bundle.putBoolean("isEntrant",   profile.isEntrant());
            bundle.putBoolean("isOrganizer", profile.isOrganizer());
            bundle.putBoolean("isAdmin",     profile.isAdmin());
            bundle.putBoolean("isDeleted",   isDeleted);
            Navigation.findNavController(v).navigate(R.id.adminProfileDetails, bundle);
        });
    }

    @Override
    public int getItemCount() { return profileList.size(); }

    /**
     * Builds a human-readable role label from a profile's boolean role flags.
     * Prioritizes Admin over other roles since it's the highest privilege.
     */
    private String getRoleLabel(Profile profile) {
        if (profile.isAdmin()) return "Admin";
        List<String> roles = new ArrayList<>();
        if (profile.isOrganizer()) roles.add("Organizer");
        if (profile.isEntrant())   roles.add("Entrant");
        return roles.isEmpty() ? "None" : String.join(", ", roles);
    }

    /**
     * ViewHolder for a single profile card item.
     */
    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView roleText;
        TextView statusText;
        Button detailsButton;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText      = itemView.findViewById(R.id.tv_profile_name);
            roleText      = itemView.findViewById(R.id.tv_profile_role);
            statusText    = itemView.findViewById(R.id.tv_profile_status);
            detailsButton = itemView.findViewById(R.id.btn_profile_details);
        }
    }
}