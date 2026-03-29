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
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of events to admin users.
 *
 * <p>This adapter is used in {@link AdminBrowseEvents} to display all events
 * in the system. Each event card displays:</p>
 * <ul>
 *   <li>Event title/name</li>
 *   <li>Organizer name (fetched asynchronously from profiles collection)</li>
 *   <li>Organizer UID</li>
 *   <li>Event location</li>
 *   <li>Details button to navigate to {@link AdminEventDetails} for full event management</li>
 * </ul>
 *
 * <p>The organizer name is fetched asynchronously from the {@code profiles}
 * collection using the organizer's UID, with a "Loading..." placeholder while
 * the fetch is in progress.</p>
 *
 * @see EventDetail
 * @see AdminBrowseEvents
 * @see AdminEventDetails
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    /** List of events to display in the RecyclerView. */
    private ArrayList<EventDetail> eventList;

    /** Firestore document IDs corresponding to each event. */
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

    /**
     * Creates and inflates the view holder for an event item.
     *
     * @param parent   the parent view group
     * @param viewType the view type (unused, single type)
     * @return a new EventViewHolder
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the view holder at the given position.
     *
     * <p>Sets the event title, location, and organizer UID. Fetches the
     * organizer's name asynchronously from the profiles collection and updates
     * the view when the data is loaded.</p>
     *
     * @param holder   the view holder to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventDetail event = eventList.get(position);
        String organizerUid = event.getOrganizerUid();

        holder.titleText.setText(event.getName());
        holder.organizerIdText.setText(organizerUid);
        holder.locationText.setText(event.getLocation());
        holder.organizerNameText.setText("Loading...");

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

    /**
     * Returns the total number of events in the list.
     *
     * @return the number of events
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Updates the adapter with new data and refreshes the RecyclerView.
     *
     * <p>This method clears the existing lists and replaces them with the new data,
     * then notifies the RecyclerView to redraw. Used primarily for search/filtering
     * in {@link AdminBrowseEvents}.</p>
     *
     * @param newEventList the new list of events
     * @param newEventIds  the new list of event IDs
     */
    public void updateData(List<EventDetail> newEventList, List<String> newEventIds) {
        this.eventList.clear();
        this.eventList.addAll(newEventList);
        this.eventIds.clear();
        this.eventIds.addAll(newEventIds);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder that caches references to the views in each event item.
     * Provides efficient access to UI elements without repeated findViewById calls.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        /** Displays the event title/name. */
        TextView titleText;

        /** Displays the organizer's name (fetched asynchronously). */
        TextView organizerNameText;

        /** Displays the organizer's UID. */
        TextView organizerIdText;

        /** Displays the event location. */
        TextView locationText;

        /** Button to navigate to the full event detail screen. */
        Button detailsButton;

        /**
         * Constructs the ViewHolder and binds child views.
         *
         * @param itemView the inflated item view
         */
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