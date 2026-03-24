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
import androidx.appcompat.widget.AppCompatButton;
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
    private ArrayList<String> imageIds; // Firestore document IDs (event IDs)

    /**
     * Creates the adapter with the list of images and their Firestore event IDs.
     *
     * @param imageList list of {@link ImageItem} objects to display
     * @param imageIds  Firestore document IDs corresponding to each image
     */
    public ImageAdapter(ArrayList<ImageItem> imageList, ArrayList<String> imageIds) {
        this.imageList = imageList;
        this.imageIds = imageIds;
    }

    /**
     * Inflates the item layout and creates a new {@link ImageViewHolder}.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type (unused, single type)
     * @return a new ImageViewHolder
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds image data to the ViewHolder. Checks the bitmap cache first,
     * then the Base64 cache, then falls back to decoding from the item directly.
     *
     * @param holder   the ViewHolder for this row
     * @param position position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem item = imageList.get(position);
        String eventId = imageIds.get(position);

        // 1. Bitmap already cached — display immediately
        if (ImageCacheManager.has(eventId)) {
            Bitmap cachedBitmap = ImageCacheManager.get(eventId);
            if (cachedBitmap != null) {
                holder.imageView.setImageBitmap(cachedBitmap);
                setupClickListener(holder, item, eventId);
                return;
            }
        }

        // 2. Base64 cached — decode in background
        if (ImageCacheManager.hasBase64(eventId)) {
            decodeAndDisplayImage(holder, ImageCacheManager.getBase64(eventId), eventId);
            setupClickListener(holder, item, eventId);
            return;
        }

        // 3. Decode directly from item data
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
     *
     * @param holder     the ViewHolder containing the target ImageView
     * @param base64Data the Base64 encoded image string
     * @param eventId    the event ID used as the cache key
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

    /**
     * Posts a fallback placeholder image to the ImageView on the main thread.
     *
     * @param holder the ViewHolder containing the target ImageView
     */
    private void setPlaceholder(ImageViewHolder holder) {
        if (holder.imageView != null) {
            holder.imageView.post(() ->
                    holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image));
        }
    }

    /**
     * Attaches a click listener to the details button that navigates to the
     * admin image details screen, passing the event ID, Base64 string, and
     * uploader name as navigation arguments.
     *
     * @param holder  the ViewHolder containing the button
     * @param item    the ImageItem for this row
     * @param eventId the Firestore event document ID
     */
    private void setupClickListener(ImageViewHolder holder, ImageItem item, String eventId) {
        holder.detailsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("imageId", eventId);
            bundle.putString("imageUrl", item.imageUrl);
            bundle.putString("uploadedBy", item.uploadedBy);
            Navigation.findNavController(v).navigate(R.id.adminImageDetails, bundle);
        });
    }

    /**
     * Returns the total number of images in the list.
     *
     * @return size of {@code imageList}
     */
    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    /**
     * Replaces the adapter's dataset and refreshes the RecyclerView.
     *
     * @param newList updated list of {@link ImageItem} objects
     * @param newIds  updated list of Firestore event IDs
     */
    public void updateData(ArrayList<ImageItem> newList, ArrayList<String> newIds) {
        this.imageList = newList;
        this.imageIds = newIds;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder that holds references to the image preview and details button
     * for a single row in the RecyclerView.
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        AppCompatButton detailsButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_preview);
            detailsButton = itemView.findViewById(R.id.btn_image_details);
        }
    }
}