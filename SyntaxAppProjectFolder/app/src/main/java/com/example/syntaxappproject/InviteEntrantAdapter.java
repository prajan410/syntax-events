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

    public interface OnInviteClickListener {
        void onInviteClick(Profile profile, String userId);
    }

    private List<Profile> profiles;
    private List<String> userIds;
    private OnInviteClickListener listener;

    public InviteEntrantAdapter(List<Profile> profiles, List<String> userIds, OnInviteClickListener listener) {
        this.profiles = profiles;
        this.userIds = userIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_entrant, parent, false);
        return new InviteViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return profiles == null ? 0 : profiles.size();
    }

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

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tvInviteName);
            emailText = itemView.findViewById(R.id.tvInviteEmail);
            phoneText = itemView.findViewById(R.id.tvInvitePhone);
            inviteButton = itemView.findViewById(R.id.btnInviteEntrant);
        }
    }
}