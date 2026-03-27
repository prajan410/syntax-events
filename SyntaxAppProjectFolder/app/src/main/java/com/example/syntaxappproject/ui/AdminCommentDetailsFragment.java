package com.example.syntaxappproject.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.syntaxappproject.CommentRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment that displays detailed information about a comment for admin users.
 * Shows comment content, author details, report count, and provides options to
 * delete the comment, view the associated event, or view the user profile.
 *
 * <p>Data is passed via Bundle arguments from {@link AdminBrowseCommentsFragment}
 * when a comment is selected. The fragment also fetches additional event and
 * user data from Firestore as needed for navigation.</p>
 *
 * <p>Actions available:</p>
 * <ul>
 *   <li>Delete Comment - permanently removes the comment from Firestore</li>
 *   <li>View Event - navigates to event details for the associated event</li>
 *   <li>View User - navigates to user profile details</li>
 * </ul>
 */
public class AdminCommentDetailsFragment extends HomeBar {

    private String commentId;
    private String eventId;
    private String userId;
    private String userName;
    private String commentText;
    private int reportCount;
    private long timestamp;

    private TextView userNameText;
    private TextView timestampText;
    private TextView commentTextTextView;
    private TextView reportCountText;
    private TextView eventIdText;
    private TextView userIdText;
    private MaterialButton deleteButton;
    private MaterialButton viewEventButton;
    private MaterialButton viewUserButton;

    /** Repository for comment database operations. */
    private CommentRepository commentRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_comment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try { setupHotbar(view); } catch (Exception ignored) {}

        commentRepository = new CommentRepository();

        Bundle args = getArguments();
        if (args != null) {
            commentId = args.getString("commentId");
            eventId = args.getString("eventId");
            userId = args.getString("userId");
            userName = args.getString("userName");
            commentText = args.getString("commentText");
            reportCount = args.getInt("reportCount");
            timestamp = args.getLong("timestamp");
        }

        userNameText = view.findViewById(R.id.commentUserName);
        timestampText = view.findViewById(R.id.commentTimestamp);
        commentTextTextView = view.findViewById(R.id.commentText);
        reportCountText = view.findViewById(R.id.reportCount);
        eventIdText = view.findViewById(R.id.eventId);
        userIdText = view.findViewById(R.id.userId);
        deleteButton = view.findViewById(R.id.deleteButton);
        viewEventButton = view.findViewById(R.id.viewEventButton);
        viewUserButton = view.findViewById(R.id.viewUserButton);

        userNameText.setText(userName != null ? userName : "Unknown User");
        commentTextTextView.setText(commentText != null ? commentText : "No content");
        eventIdText.setText("Event ID: " + (eventId != null ? eventId : "Unknown"));
        userIdText.setText("User ID: " + (userId != null ? userId : "Unknown"));

        if (reportCount > 0) {
            reportCountText.setText(reportCount + " report" + (reportCount > 1 ? "s" : ""));
            reportCountText.setTextColor(0xFFFF9800);
        } else {
            reportCountText.setText("No reports");
            reportCountText.setTextColor(0xFF888888);
        }
        if (timestamp > 0) {
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
            timestampText.setText(sdf.format(date));
        } else {
            timestampText.setText("Unknown date");
        }
        deleteButton.setOnClickListener(v -> deleteComment());
        viewEventButton.setOnClickListener(v -> viewEvent());
        viewUserButton.setOnClickListener(v -> viewUser());

        MaterialButton doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).popBackStack();
        });

        View headerTitle = view.findViewById(R.id.headerTitle);
        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();
    }

    /**
     * Permanently deletes the comment from Firestore.
     * Shows a confirmation dialog before deletion and navigates back on success.
     */
    private void deleteComment() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to permanently delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    commentRepository.deleteComment(commentId, success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Navigates to the event details screen for the event this comment belongs to.
     * Fetches the full event data from Firestore and passes it via Bundle.
     */
    private void viewEvent() {
        FirebaseFirestore.getInstance().collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", eventId);
                    bundle.putString("name", documentSnapshot.getString("name"));
                    bundle.putString("description", documentSnapshot.getString("description"));
                    bundle.putString("organizerUid", documentSnapshot.getString("organizerUid"));
                    bundle.putString("location", documentSnapshot.getString("location"));
                    bundle.putString("startingEventDate", documentSnapshot.getString("startingEventDate"));
                    bundle.putString("endingEventDate", documentSnapshot.getString("endingEventDate"));
                    bundle.putString("startingRegistrationPeriod", documentSnapshot.getString("startingRegistrationPeriod"));
                    bundle.putString("endingRegistrationPeriod", documentSnapshot.getString("endingRegistrationPeriod"));
                    Long capacity = documentSnapshot.getLong("capacity");
                    bundle.putLong("capacity", capacity != null ? capacity : 0);

                    Navigation.findNavController(requireView()).navigate(R.id.adminEventDetails, bundle);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load event data", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates to the user profile details screen for the author of this comment.
     * Fetches the full user profile data from Firestore and passes it via Bundle.
     */
    private void viewUser() {
        FirebaseFirestore.getInstance().collection("profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("profileId", userId);
                    bundle.putString("name", documentSnapshot.getString("name"));
                    bundle.putString("email", documentSnapshot.getString("email"));
                    bundle.putString("phone", documentSnapshot.getString("phone"));
                    bundle.putString("deviceId", documentSnapshot.getString("deviceId"));
                    bundle.putBoolean("isAdmin", documentSnapshot.getBoolean("isAdmin") != null && documentSnapshot.getBoolean("isAdmin"));
                    bundle.putBoolean("isOrganizer", documentSnapshot.getBoolean("isOrganizer") != null && documentSnapshot.getBoolean("isOrganizer"));
                    bundle.putBoolean("isEntrant", documentSnapshot.getBoolean("isEntrant") != null && documentSnapshot.getBoolean("isEntrant"));
                    bundle.putBoolean("isDeleted", false);
                    bundle.putString("role", getRoleLabel(documentSnapshot));

                    Navigation.findNavController(requireView()).navigate(R.id.adminProfileDetails, bundle);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Builds a human-readable role label from a profile document.
     * Prioritizes Admin over other roles since it's the highest privilege.
     *
     * @param doc the Firestore document snapshot containing user profile data
     * @return a string representing the user's role (Admin, Organizer, Entrant, or None)
     */
    private String getRoleLabel(DocumentSnapshot doc) {
        boolean isAdmin = doc.getBoolean("isAdmin") != null && doc.getBoolean("isAdmin");
        boolean isOrganizer = doc.getBoolean("isOrganizer") != null && doc.getBoolean("isOrganizer");
        boolean isEntrant = doc.getBoolean("isEntrant") != null && doc.getBoolean("isEntrant");

        if (isAdmin) return "Admin";
        if (isOrganizer) return "Organizer";
        if (isEntrant) return "Entrant";
        return "None";
    }
}