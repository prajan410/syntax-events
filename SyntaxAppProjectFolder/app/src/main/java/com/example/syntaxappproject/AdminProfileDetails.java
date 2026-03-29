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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that displays detailed user profile information for admin users.
 *
 * <p>This fragment allows administrators to:</p>
 * <ul>
 *   <li>View user details (name, email, device ID, phone number)</li>
 *   <li>Toggle user roles (Admin, Organizer, Entrant)</li>
 *   <li>Delete user profiles (with confirmation and archiving)</li>
 *   <li>View active/inactive status</li>
 * </ul>
 *
 * <p>Role management features:</p>
 * <ul>
 *   <li>Prevents removing the last role from a user (users must have at least one role)</li>
 *   <li>Buttons dynamically change between "Add Role" and "Remove Role" based on current state</li>
 *   <li>If an admin removes their own admin role, they are navigated away to prevent lockout</li>
 * </ul>
 *
 * <p>When a profile is deleted, it is archived to the {@code deleted-profiles} collection
 * with a timestamp before being removed from the active {@code profiles} collection.</p>
 *
 * @see ProfileRepository
 * @see AuthenticationService
 * @see AdminBrowseProfiles
 */
public class AdminProfileDetails extends Fragment {

    /** Button to toggle admin role. */
    private MaterialButton btnToggleAdmin;

    /** Button to toggle organizer role. */
    private MaterialButton btnToggleOrganizer;

    /** Button to toggle entrant role. */
    private MaterialButton btnToggleEntrant;

    /** Current admin role status. */
    private boolean currentAdmin;

    /** Current organizer role status. */
    private boolean currentOrganizer;

    /** Current entrant role status. */
    private boolean currentEntrant;

    /** Repository for profile database operations. */
    private final ProfileRepository profileRepo = new ProfileRepository();

    /** Service for authentication and current user management. */
    private final AuthenticationService authService = new AuthenticationService();

    /** Default constructor required for fragment instantiation. */
    public AdminProfileDetails() {}

    /**
     * Inflates the layout, initializes views, applies entrance animations,
     * and sets up role toggle buttons and deletion functionality.
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
            Navigation.findNavController(requireView()).popBackStack();
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
                if (remaining == 0) return; // Prevent removing the last role from a user

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
                                            .navigate(R.id.homeFragment);
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

                                    // Archive the profile before deletion
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

    /**
     * Updates the role label TextView with the user's current roles.
     * Formats as "Role: Admin Organizer Entrant" or "Role: None" if no roles.
     *
     * @param roleText the TextView to update with the role label
     */
    private void refreshRoleLabel(TextView roleText) {
        StringBuilder sb = new StringBuilder("Role: ");
        if (currentAdmin) sb.append("Admin ");
        if (currentOrganizer) sb.append("Organizer ");
        if (currentEntrant) sb.append("Entrant");
        String result = sb.toString().trim();
        if (result.equals("Role:")) result = "Role: None";
        roleText.setText(result);
    }

    /**
     * Refreshes the state of all role toggle buttons based on current role flags.
     * Determines which roles are active and which is the last remaining role.
     */
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
     *
     * <p>Button states:</p>
     * <ul>
     *   <li>If it's the user's last remaining role: disabled "Remove Role" (gray)</li>
     *   <li>If the user has this role but not last: enabled "Remove Role" (red)</li>
     *   <li>If the user doesn't have this role: enabled "Add Role" (green)</li>
     * </ul>
     *
     * @param button     the button to configure
     * @param hasRole    whether the user currently has this role
     * @param isLastRole whether this is the user's only role (prevents removal)
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