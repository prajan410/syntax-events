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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * RecyclerView adapter for displaying a list of events to admin users.
 * Binds event data to item views and handles navigation to event details.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private ArrayList<EventDetail> eventList;
    private ArrayList<String> eventIds;

    /**
     * Constructs the adapter with event data and their corresponding Firestore document IDs.
     *
     * @param eventList list of event detail objects
     * @param eventIds  list of Firestore document IDs matching the events
     */
    public AdminEventAdapter(ArrayList<EventDetail> eventList, ArrayList<String> eventIds) {
        this.eventList = eventList;
        this.eventIds = eventIds;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventDetail event = eventList.get(position);
        String organizerUid = event.getOrganizerUid();

        holder.titleText.setText(event.getName());
        holder.organizerIdText.setText(organizerUid);
        holder.locationText.setText(event.getLocation());
        holder.organizerNameText.setText("Loading...");

        // Async fetch: get organizer's name from profiles collection using their UID
        if (organizerUid != null && !organizerUid.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("profiles")
                    .document(organizerUid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            holder.organizerNameText.setText(
                                    name != null && !name.isEmpty() ? name : "Unknown"
                            );
                        } else {
                            holder.organizerNameText.setText("Unknown");
                        }
                    })
                    .addOnFailureListener(e -> holder.organizerNameText.setText("Unknown"));
        } else {
            holder.organizerNameText.setText("Unknown");
        }

        holder.detailsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventIds.get(position));
            bundle.putString("organizerUid", organizerUid);
            bundle.putString("name", event.getName());
            bundle.putString("description", event.getDescription());
            bundle.putString("location", event.getLocation());
            bundle.putLong("capacity", event.getCapacity());
            bundle.putBoolean("geoReq", event.isGeoReq());
            bundle.putString("startingEventDate", event.getStartingEventDate());
            bundle.putString("endingEventDate", event.getEndingEventDate());
            bundle.putString("startingRegistrationPeriod", event.getStartingRegistrationPeriod());
            bundle.putString("endingRegistrationPeriod", event.getEndingRegistrationPeriod());
            bundle.putLong("waitlistCount", event.getWaitlistCount());
            bundle.putString("lotteryCriteria", event.getLotteryCriteria());
            bundle.putString("poster", event.getPoster());
            Navigation.findNavController(v).navigate(R.id.adminEventDetails, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class that caches references to the views in each event item.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView organizerNameText;
        TextView organizerIdText;
        TextView locationText;
        Button detailsButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText          = itemView.findViewById(R.id.tv_event_title);
            organizerNameText  = itemView.findViewById(R.id.tv_organizer_name);
            organizerIdText    = itemView.findViewById(R.id.tv_event_organizer);
            locationText       = itemView.findViewById(R.id.tv_event_location);
            detailsButton      = itemView.findViewById(R.id.btn_event_details);
        }
    }
}