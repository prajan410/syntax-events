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
 * activity that shows detailed information about a selected event for admin
 * admin can view event information and perform actions like deleting the event
 * event data is passed from the previous screen
 */
public class AdminEventDetails extends Fragment {
    /**
     * empty public constructor for this fragment
     */
    public AdminEventDetails() {
    }

    /**
     * creates the view for the event details page
     * it shows the selected event information and sets up the remove button
     *
     * @param inflater used to inflate the fragment layout
     * @param container parent view that the fragment layout will be attached to
     * @param savedInstanceState previous saved state if there is one
     * @return the root view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_event_details, container, false);
        Button removeEventButton = view.findViewById(R.id.btn_remove_event);
        TextView titleText = view.findViewById(R.id.tv_detail_event_title);
        TextView descriptionText = view.findViewById(R.id.tv_detail_event_description);
        TextView organizerText = view.findViewById(R.id.tv_detail_event_organizer);
        TextView locationText = view.findViewById(R.id.tv_detail_event_location);
        Bundle args = getArguments();
        if (args == null) {
            return view;
        }
        String eventId = args.getString("eventId");
        String title = args.getString("title");
        String description = args.getString("description");
        String organizer = args.getString("organizer");
        String location = args.getString("location");
        titleText.setText("Title: " + title);
        descriptionText.setText("Description: " + description);
        organizerText.setText("Organizer: " + organizer);
        locationText.setText("Location: " + location);
        // remove the selected event from the database or storage
        removeEventButton.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(eventId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                                if (isAdded()) {
                                    NavHostFragment.findNavController(this).navigateUp();
                                }
                            });
        });
        return view;
    }
}