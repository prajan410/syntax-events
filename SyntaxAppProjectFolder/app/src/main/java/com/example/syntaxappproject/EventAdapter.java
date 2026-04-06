package com.example.syntaxappproject;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

/**
 * RecyclerView adapter for displaying events for all events list recyclerview
 *
 * <p>Each row shows the event image and name</p>
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<EventDetail> events;
    private OnItemClickListener listener;

    public boolean isTestMode = false;

    public interface OnItemClickListener {
        void onItemClick(EventDetail event);
    }

    public EventAdapter(List<EventDetail> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * The single view of events in the list
     * display a image and a name
     * contain the event id.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventName;
        String boundEventId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventName = itemView.findViewById(R.id.eventName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventDetail event = events.get(position);
        String eventId = event.getEventId();

        holder.eventName.setText(event.getName());
        holder.boundEventId = eventId;

        Bitmap cached = ImageCacheManager.get(eventId);
        if (cached != null) {
            holder.eventImage.setImageBitmap(cached);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(event));
            return;
        }

        holder.eventImage.setImageResource(R.drawable.ic_launcher_background);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(event));
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.boundEventId = null;
        holder.eventImage.setImageResource(R.drawable.ic_launcher_background);
    }

    /**
     * Notify the view to update the list.
     * @param newList a new list to update
     */
    public void updateList(List<EventDetail> newList) {
        events = newList;
        if (!isTestMode) {
            notifyDataSetChanged();
        }
    }

    /**
     * Get the number of events in a list.
     * @return the size to events list
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Get the events list.
     * @return the list of events
     */
    public List<EventDetail> getEvents() {
        return events;
    }
}
