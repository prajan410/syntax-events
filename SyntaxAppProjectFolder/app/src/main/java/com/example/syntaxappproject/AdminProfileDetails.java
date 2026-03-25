package com.example.syntaxappproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that displays detailed user profile information for admin users.
 * Allows admins to view user details, toggle user roles (Admin/Organizer/Entrant),
 * and delete user profiles. Prevents removing the last role from a user.
 */
public class AdminProfileDetails extends Fragment {

    private MaterialButton btnToggleAdmin;
    private MaterialButton btnToggleOrganizer;
    private MaterialButton btnToggleEntrant;

    private boolean currentAdmin;
    private boolean currentOrganizer;
    private boolean currentEntrant;

    private final ProfileRepository profileRepo = new ProfileRepository();
    private final AuthenticationService authService = new AuthenticationService();

    public AdminProfileDetails() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_profile_details, container, false);

        MaterialButton doneButton         = view.findViewById(R.id.btn_done);
        MaterialButton deleteDangerButton = view.findViewById(R.id.btn_delete_user);
        btnToggleAdmin     = view.findViewById(R.id.btn_toggle_admin);
        btnToggleOrganizer = view.findViewById(R.id.btn_toggle_organizer);
        btnToggleEntrant   = view.findViewById(R.id.btn_toggle_entrant);

        TextView nameText       = view.findViewById(R.id.tv_detail_name);
        TextView emailText      = view.findViewById(R.id.tv_detail_email);
        TextView roleText       = view.findViewById(R.id.tv_detail_role);
        TextView deviceIdText   = view.findViewById(R.id.tv_detail_device_id);
        TextView phoneText      = view.findViewById(R.id.tv_detail_phone);
        TextView statusText     = view.findViewById(R.id.tv_detail_status);
        TextView avatarText     = view.findViewById(R.id.tv_avatar_initial);
        View roleManagementCard = view.findViewById(R.id.roleManagementCard);
        View headerTitle        = view.findViewById(R.id.headerTitle);
        View heroCard           = view.findViewById(R.id.heroCard);
        View detailsCard        = view.findViewById(R.id.detailsCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        heroCard.setTranslationY(30f);
        heroCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(200).start();

        detailsCard.setTranslationY(30f);
        detailsCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(320).start();

        roleManagementCard.setTranslationY(30f);
        roleManagementCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(420).start();

        doneButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminBrowseProfiles);
        });

        Bundle args = getArguments();
        if (args == null) return view;

        String  profileId = args.getString("profileId");
        String  name      = args.getString("name");
        String  email     = args.getString("email");
        String  deviceId  = args.getString("deviceId");
        String  phone     = args.getString("phone");
        boolean isDeleted = args.getBoolean("isDeleted", false);

        currentAdmin     = args.getBoolean("isAdmin",     false);
        currentOrganizer = args.getBoolean("isOrganizer", false);
        currentEntrant   = args.getBoolean("isEntrant",   false);

        String  myUid        = authService.getCurrentUserId();
        boolean isOwnProfile = profileId != null && profileId.equals(myUid);

        nameText.setText(name != null ? name : "Unknown");
        emailText.setText(email != null ? email : "");
        deviceIdText.setText(deviceId != null ? deviceId : "");
        phoneText.setText(phone != null ? phone : "");

        if (name != null && !name.isEmpty()) {
            avatarText.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        if (isDeleted) {
            statusText.setText("● Inactive");
            statusText.setTextColor(0xFFEF9A9A);
            roleText.setText("Role: " + args.getString("role"));
            roleManagementCard.setVisibility(View.GONE);
            deleteDangerButton.setVisibility(View.GONE);
        } else {
            statusText.setText("● Active");
            statusText.setTextColor(0xFFA5D6A7);

            refreshRoleLabel(roleText);
            refreshRoleButtons();

            btnToggleAdmin.setOnClickListener(v -> {
                boolean willHaveAdmin = !currentAdmin;
                int remaining = (willHaveAdmin ? 1 : 0)
                        + (currentOrganizer ? 1 : 0)
                        + (currentEntrant ? 1 : 0);
                if (remaining == 0) return;// Prevent removing the last role from a user

                profileRepo.getProfile(profileId, existing -> {
                    if (existing == null || !isAdded()) return;
                    Profile updated = new Profile(
                            existing.getName(),
                            existing.getEmail(),
                            existing.getPhone(),
                            existing.isEntrant(),
                            existing.isOrganizer(),
                            willHaveAdmin,
                            existing.isNotificationsEnabled(),
                            existing.getDeviceId()
                    );
                    profileRepo.updateProfile(profileId, updated, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                currentAdmin = willHaveAdmin;
                                refreshRoleLabel(roleText);
                                refreshRoleButtons();
                                if (isOwnProfile && !currentAdmin) {
                                    NavHostFragment.findNavController(this)
                                            .navigate(R.id.homeFragment);// If admin removed their own admin role, navigate them away
                                }
                            }
                        });
                    });
                });
            });

            btnToggleOrganizer.setOnClickListener(v -> {
                boolean willHaveOrganizer = !currentOrganizer;
                int remaining = (currentAdmin ? 1 : 0)
                        + (willHaveOrganizer ? 1 : 0)
                        + (currentEntrant ? 1 : 0);
                if (remaining == 0) return;

                profileRepo.getProfile(profileId, existing -> {
                    if (existing == null || !isAdded()) return;
                    Profile updated = new Profile(
                            existing.getName(),
                            existing.getEmail(),
                            existing.getPhone(),
                            existing.isEntrant(),
                            willHaveOrganizer,
                            existing.isAdmin(),
                            existing.isNotificationsEnabled(),
                            existing.getDeviceId()
                    );
                    profileRepo.updateProfile(profileId, updated, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                currentOrganizer = willHaveOrganizer;
                                refreshRoleLabel(roleText);
                                refreshRoleButtons();
                            }
                        });
                    });
                });
            });

            btnToggleEntrant.setOnClickListener(v -> {
                boolean willHaveEntrant = !currentEntrant;
                int remaining = (currentAdmin ? 1 : 0)
                        + (currentOrganizer ? 1 : 0)
                        + (willHaveEntrant ? 1 : 0);
                if (remaining == 0) return;

                profileRepo.getProfile(profileId, existing -> {
                    if (existing == null || !isAdded()) return;
                    Profile updated = new Profile(
                            existing.getName(),
                            existing.getEmail(),
                            existing.getPhone(),
                            willHaveEntrant,
                            existing.isOrganizer(),
                            existing.isAdmin(),
                            existing.isNotificationsEnabled(),
                            existing.getDeviceId()
                    );
                    profileRepo.updateProfile(profileId, updated, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                currentEntrant = willHaveEntrant;
                                refreshRoleLabel(roleText);
                                refreshRoleButtons();
                            }
                        });
                    });
                });
            });

            deleteDangerButton.setOnClickListener(v ->
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete this profile?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                profileRepo.getProfile(profileId, existing -> {
                                    if (existing == null || !isAdded()) return;

                                    Map<String, Object> archived = new HashMap<>();
                                    archived.put("name", existing.getName());
                                    archived.put("email", existing.getEmail());
                                    archived.put("phone", existing.getPhone());
                                    archived.put("isEntrant", existing.isEntrant());
                                    archived.put("isOrganizer", existing.isOrganizer());
                                    archived.put("isAdmin", existing.isAdmin());
                                    archived.put("notificationsEnabled", existing.isNotificationsEnabled());
                                    archived.put("deviceId", existing.getDeviceId());
                                    archived.put("deletedAt", com.google.firebase.Timestamp.now());

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("deleted-profiles")
                                            .add(archived)
                                            .addOnSuccessListener(unused ->
                                                    profileRepo.deleteProfile(profileId, success -> {
                                                        if (!isAdded()) return;
                                                        requireActivity().runOnUiThread(() -> {
                                                            if (success) {
                                                                if (isOwnProfile) {
                                                                    NavHostFragment.findNavController(this)
                                                                            .navigate(R.id.profileSetupFragment);
                                                                } else {
                                                                    NavHostFragment.findNavController(this).navigateUp();
                                                                }
                                                            }
                                                        });
                                                    })
                                            );
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show()
            );
        }

        return view;
    }

    private void refreshRoleLabel(TextView roleText) {
        StringBuilder sb = new StringBuilder("Role: ");
        if (currentAdmin) sb.append("Admin ");
        if (currentOrganizer) sb.append("Organizer ");
        if (currentEntrant) sb.append("Entrant");
        String result = sb.toString().trim();
        if (result.equals("Role:")) result = "Role: None";
        roleText.setText(result);
    }

    private void refreshRoleButtons() {
        int activeRoles = (currentAdmin ? 1 : 0)
                + (currentOrganizer ? 1 : 0)
                + (currentEntrant ? 1 : 0);

        applyRoleButton(btnToggleAdmin, currentAdmin, activeRoles == 1 && currentAdmin);
        applyRoleButton(btnToggleOrganizer, currentOrganizer, activeRoles == 1 && currentOrganizer);
        applyRoleButton(btnToggleEntrant, currentEntrant, activeRoles == 1 && currentEntrant);
    }

    /**
     * Configures a role toggle button based on current role state.
     * If it's the user's last remaining role, disable the button and show "Remove Role" in disabled state.
     *
     * @param button     the button to configure
     * @param hasRole    whether the user currently has this role
     * @param isLastRole whether this is the user's only role
     */
    private void applyRoleButton(MaterialButton button, boolean hasRole, boolean isLastRole) {
        if (isLastRole) {
            button.setText("Remove Role");
            button.setTextColor(0xFFAAAAAA);
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFEEEEEE));
            button.setEnabled(false);
        } else if (hasRole) {
            button.setText("Remove Role");
            button.setTextColor(0xFFFFFFFF);
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE53935));
            button.setEnabled(true);
        } else {
            button.setText("Add Role");
            button.setTextColor(0xFFFFFFFF);
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF43A047));
            button.setEnabled(true);
        }
    }
}