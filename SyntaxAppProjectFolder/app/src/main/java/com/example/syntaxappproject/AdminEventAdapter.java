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

import java.util.ArrayList;
/**
 * adapter used to display event items in the admin event list
 * it connects event data with the recyclerview item layout
 * each row represents one event that the admin can view
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private ArrayList<EventDetail> eventList;
    private ArrayList<String> eventIds;

    public AdminEventAdapter(ArrayList<EventDetail> eventList, ArrayList<String> eventIds) {
    /**
     * creates the adapter with the event data and their firestore ids
     *
     * @param eventList list of events to display
     * @param eventIds firestore document ids for each event
     */
    public AdminEventAdapter(ArrayList<Event> eventList, ArrayList<String> eventIds) {
        this.eventList = eventList;
        this.eventIds = eventIds;
    }

    @NonNull
    @Override
    /**
     * creates the view holder for each event row in the recyclerview
     *
     * @param parent parent view group
     * @param viewType type of the view
     * @return a new EventViewHolder
     */
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventDetail event = eventList.get(position);
        holder.titleText.setText(event.getName());
        holder.organizerText.setText(event.getOrganizerUid());
        holder.locationText.setText(event.getLocation());
/**
 * binds the event data to the view holder so it shows correctly
 *
 * @param holder the view holder for the row
 * @param position position of the event in the list
 */    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.titleText.setText(event.title);
        holder.organizerText.setText(event.organizer);
        holder.locationText.setText(event.location);
        holder.detailsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());
            bundle.putString("organizerUid", event.getOrganizerUid());
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
            Navigation.findNavController(v).navigate(R.id.adminEventDetails, bundle);});
        }
    @Override
    /**
     * returns the number of events in the list
     *
     * @return number of events
     */
    public int getItemCount() {
        return eventList.size();}

    // create the view holder for each event item in the recyclerview
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView organizerText;
        TextView locationText;
        Button detailsButton;
        /**
         * view holder class that stores the views for one event item
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_event_title);
            organizerText = itemView.findViewById(R.id.tv_event_organizer);
            locationText = itemView.findViewById(R.id.tv_event_location);
            detailsButton = itemView.findViewById(R.id.btn_event_details);}
        }
}