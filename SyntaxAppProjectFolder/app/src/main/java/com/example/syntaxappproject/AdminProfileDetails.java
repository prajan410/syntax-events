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

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment that displays detailed information about a user profile for admin review.
 *
 * <p>Admins can view the user's name, email, phone, role, and device ID,
 * and may remove the profile entirely or strip the organizer role from the user.</p>
 *
 * <p>Profile data is passed via a {@link Bundle} from the previous screen.</p>
 */
public class AdminProfileDetails extends Fragment {

    /**
     * Required no-argument constructor.
     */
    public AdminProfileDetails() {}

    /**
     * Inflates the admin profile detail layout, binds profile data from arguments,
     * and sets up remove and role-management button handlers.
     *
     * @param inflater           the layout inflater
     * @param container          the parent view group
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the inflated root view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile_details, container, false);

        Button removeProfileButton  = view.findViewById(R.id.btn_remove_profile);
        Button removeOrganizerButton = view.findViewById(R.id.btn_remove_organizer);
        TextView nameText     = view.findViewById(R.id.tv_detail_name);
        TextView emailText    = view.findViewById(R.id.tv_detail_email);
        TextView roleText     = view.findViewById(R.id.tv_detail_role);
        TextView deviceIdText = view.findViewById(R.id.tv_detail_device_id);
        TextView phoneText    = view.findViewById(R.id.tv_detail_phone);
        TextView statusText   = view.findViewById(R.id.tv_detail_status);

        Bundle args = getArguments();
        if (args == null) return view;

        String profileId = args.getString("profileId");
        String name      = args.getString("name");
        String email     = args.getString("email");
        String role      = args.getString("role");
        String deviceId  = args.getString("deviceId");
        String phone     = args.getString("phone");
        boolean isEntrant = args.getBoolean("isEntrant", false);

        nameText.setText("Name: " + name);
        emailText.setText("Email: " + email);
        roleText.setText("Role: " + role);
        deviceIdText.setText("Device ID: " + deviceId);
        phoneText.setText("Phone: " + phone);
        statusText.setText("Status: Active");

        if (!"Organizer".equals(role) && !"Organizer, Entrant".equals(role)) {
            removeOrganizerButton.setVisibility(View.GONE);
        }

        removeProfileButton.setOnClickListener(v ->
                FirebaseFirestore.getInstance()
                        .collection("profiles")
                        .document(profileId)
                        .delete()
                        .addOnSuccessListener(unused ->
                                NavHostFragment.findNavController(this).navigateUp()
                        )
        );

        removeOrganizerButton.setOnClickListener(v ->
                FirebaseFirestore.getInstance()
                        .collection("profiles")
                        .document(profileId)
                        .update("isOrganizer", false)
                        .addOnSuccessListener(unused -> {
                            String updatedRole = isEntrant ? "Entrant" : "None";
                            roleText.setText("Role: " + updatedRole);
                            removeOrganizerButton.setVisibility(View.GONE);
                        })
        );

        return view;
    }
}
