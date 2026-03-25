package com.example.syntaxappproject;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment that displays detailed information about an event poster image for admin users.
 * Shows event details, organizer information, and provides option to remove the poster.
 * Event data is fetched from Firestore using the eventId passed via bundle arguments.
 */
public class AdminImageDetails extends Fragment {

    public AdminImageDetails() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_image_details, container, false);

        MaterialButton removeImageButton = view.findViewById(R.id.btn_remove_image);
        MaterialButton doneButton = view.findViewById(R.id.doneButton);
        ImageView imageView = view.findViewById(R.id.img_detail);
        TextView organizerNameText = view.findViewById(R.id.tv_organizer_name);
        TextView organizerIdText = view.findViewById(R.id.tv_organizer_id);
        TextView eventNameText = view.findViewById(R.id.tv_event_name);
        TextView eventIdText = view.findViewById(R.id.tv_event_id);

        Bundle args = getArguments();
        if (args == null) return view;

        String imageId = args.getString("imageId");
        if (imageId == null) return view;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(imageId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(getContext(), "Event data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String organizerUid = eventDoc.getString("organizerUid");
                    String eventName = eventDoc.getString("name");

                    eventNameText.setText("Event Name: " + (eventName != null ? eventName : "N/A"));
                    eventIdText.setText("Event ID: " + imageId);

                    if (organizerUid != null) {
                        db.collection("profiles").document(organizerUid)
                                .get()
                                .addOnSuccessListener(profileDoc -> {
                                    if (profileDoc.exists()) {
                                        String organizerName = profileDoc.getString("name");
                                        organizerNameText.setText("Organizer: " + (organizerName != null ? organizerName : "N/A"));
                                        organizerIdText.setText("Organizer ID: " + organizerUid);
                                    } else {

                                        db.collection("deleted-profiles").document(organizerUid)// Check deleted-profiles collection for archived organizer accounts
                                                .get()
                                                .addOnSuccessListener(deletedDoc -> {
                                                    String organizerName = deletedDoc.exists() ?
                                                            deletedDoc.getString("name") : "N/A";
                                                    organizerNameText.setText("Organizer: " + organizerName);
                                                    organizerIdText.setText("Organizer ID: " + organizerUid);
                                                })
                                                .addOnFailureListener(e -> {
                                                    organizerNameText.setText("Organizer: N/A");
                                                    organizerIdText.setText("Organizer ID: " + organizerUid);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    organizerNameText.setText("Organizer: N/A");
                                    organizerIdText.setText("Organizer ID: " + organizerUid);
                                });
                    } else {
                        organizerNameText.setText("Organizer: N/A");
                        organizerIdText.setText("Organizer ID: N/A");
                    }

                    Bitmap bitmap = ImageCacheManager.get(imageId);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event info", Toast.LENGTH_SHORT).show()
                );

        removeImageButton.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Removal")
                        .setMessage("Are you sure you want to remove this image?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            db.collection("events")
                                    .document(imageId)
                                    .update("poster", null)
                                    .addOnSuccessListener(unused ->
                                            NavHostFragment.findNavController(this).navigateUp()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to remove image", Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        doneButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.adminBrowseImages)
        );

        return view;
    }
}