package com.example.syntaxappproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Adapter used to display image items in a RecyclerView.
 * Each item represents a poster image pulled from a Firestore event document,
 * stored as a Base64 string. Bitmaps are decoded on a background thread and
 * cached via {@link ImageCacheManager} so repeated scrolls are instant.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private ArrayList<ImageItem> imageList;
    private ArrayList<String> imageIds;

    public ImageAdapter(ArrayList<ImageItem> imageList, ArrayList<String> imageIds) {
        this.imageList = imageList;
        this.imageIds = imageIds;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem item = imageList.get(position);
        String eventId = imageIds.get(position);

        if (ImageCacheManager.has(eventId)) {
            Bitmap cachedBitmap = ImageCacheManager.get(eventId);
            if (cachedBitmap != null) {
                holder.imageView.setImageBitmap(cachedBitmap);
                setupClickListener(holder, item, eventId);
                return;
            }
        }

        if (ImageCacheManager.hasBase64(eventId)) {
            decodeAndDisplayImage(holder, ImageCacheManager.getBase64(eventId), eventId);
            setupClickListener(holder, item, eventId);
            return;
        }

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            decodeAndDisplayImage(holder, item.imageUrl, eventId);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        setupClickListener(holder, item, eventId);
    }

    /**
     * Decodes a Base64 image string on a background thread and posts the
     * resulting {@link Bitmap} to the ImageView on the main thread.
     * Falls back to a placeholder on any decoding failure.
     */
    private void decodeAndDisplayImage(ImageViewHolder holder, String base64Data, String eventId) {
        holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);

        new Thread(() -> {
            try {
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                if (bitmap != null) {
                    ImageCacheManager.put(eventId, bitmap);
                    ImageCacheManager.putBase64(eventId, base64Data);
                    if (holder.imageView != null) {
                        holder.imageView.post(() -> holder.imageView.setImageBitmap(bitmap));
                    }
                } else {
                    setPlaceholder(holder);
                }
            } catch (Exception e) {
                setPlaceholder(holder);
            }
        }).start();
    }

    private void setPlaceholder(ImageViewHolder holder) {
        if (holder.imageView != null) {
            holder.imageView.post(() ->
                    holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image));
        }
    }

    /**
     * Attaches a click listener that navigates to admin image details with the event ID,
     * Base64 string, and uploader name as navigation arguments.
     */
    private void setupClickListener(ImageViewHolder holder, ImageItem item, String eventId) {
        holder.imageView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("imageId", eventId);
            bundle.putString("imageUrl", item.imageUrl);
            bundle.putString("uploadedBy", item.uploadedBy);
            Navigation.findNavController(v).navigate(R.id.adminImageDetails, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    public void updateData(ArrayList<ImageItem> newList, ArrayList<String> newIds) {
        this.imageList = newList;
        this.imageIds = newIds;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder that holds a reference to the image preview for a single
     * grid cell in the RecyclerView.
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_preview);
        }
    }
}