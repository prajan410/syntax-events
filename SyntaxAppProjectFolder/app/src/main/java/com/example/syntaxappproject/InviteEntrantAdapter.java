package com.example.syntaxappproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class InviteEntrantAdapter extends RecyclerView.Adapter<InviteEntrantAdapter.InviteViewHolder> {

    /**
     * Listener interface for handling invite button clicks.
     */
    public interface OnInviteClickListener {

        /**
         * Called when the invite button is clicked for a profile.
         *
         * @param profile the selected profile
         * @param userId the ID of the selected user
         */
        void onInviteClick(Profile profile, String userId);
    }

    private List<Profile> profiles;
    private List<String> userIds;
    private OnInviteClickListener listener;

    /**
     * Creates an adapter for displaying entrant profiles and invite buttons.
     *
     * @param profiles the list of profiles to display
     * @param userIds the list of user IDs corresponding to the profiles
     * @param listener the listener triggered when an invite button is clicked
     */
    public InviteEntrantAdapter(List<Profile> profiles, List<String> userIds, OnInviteClickListener listener) {
        this.profiles = profiles;
        this.userIds = userIds;
        this.listener = listener;
    }

    /**
     * Creates a new view holder for an invite entrant item.
     *
     * @param parent the parent view group
     * @param viewType the view type of the new view
     * @return a new InviteViewHolder instance
     */
    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_entrant, parent, false);
        return new InviteViewHolder(view);
    }

    /**
     * Binds a profile's data to the view holder and sets the invite button click listener.
     *
     * @param holder the view holder to bind data to
     * @param position the position of the item in the adapter
     */
    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        String userId = userIds.get(position);

        holder.nameText.setText(profile.getName() == null ? "No name" : profile.getName());
        holder.emailText.setText(profile.getEmail() == null ? "No email" : profile.getEmail());
        holder.phoneText.setText(profile.getPhone() == null || profile.getPhone().isEmpty()
                ? "No phone"
                : profile.getPhone());

        holder.inviteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInviteClick(profile, userId);
            }
        });
    }

    /**
     * Returns the number of profiles currently stored in the adapter.
     *
     * @return the number of items in the adapter
     */
    @Override
    public int getItemCount() {
        return profiles == null ? 0 : profiles.size();
    }

    /**
     * Replaces the adapter's current profile and user ID lists with new data.
     *
     * @param newProfiles the new list of profiles
     * @param newUserIds the new list of corresponding user IDs
     */
    public void updateData(List<Profile> newProfiles, List<String> newUserIds) {
        profiles = new ArrayList<>(newProfiles);
        userIds = new ArrayList<>(newUserIds);
        notifyDataSetChanged();
    }

    static class InviteViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView emailText;
        TextView phoneText;
        MaterialButton inviteButton;

        /**
         * Creates a view holder for an invite entrant item view.
         *
         * @param itemView the item view for this holder
         */
        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tvInviteName);
            emailText = itemView.findViewById(R.id.tvInviteEmail);
            phoneText = itemView.findViewById(R.id.tvInvitePhone);
            inviteButton = itemView.findViewById(R.id.btnInviteEntrant);
        }
    }
}