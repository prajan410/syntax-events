package com.example.syntaxappproject;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.R;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<EventDetail> events;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EventDetail event);
    }

    public EventAdapter(List<EventDetail> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

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

    public void updateList(List<EventDetail> newList) {
        events = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
