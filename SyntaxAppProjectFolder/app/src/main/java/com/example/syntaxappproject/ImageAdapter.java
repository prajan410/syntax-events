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

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * adapter used to display image items in a recyclerview
 * it connects image data with the layout used for each row
 * each item in the list represents one uploaded image
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private ArrayList<ImageItem> imageList;
    private ArrayList<String> imageIds;

    /**
     * creates the adapter with the list of images and their ids
     *
     * @param imageList list of images to display
     * @param imageIds  firestore document ids for each image
     */
    public ImageAdapter(ArrayList<ImageItem> imageList, ArrayList<String> imageIds) {
        this.imageList = imageList;
        this.imageIds = imageIds;
    }

    /**
     * creates the view holder for each image item in the recyclerview
     *
     * @param parent   parent view group
     * @param viewType type of view being created
     * @return a new ImageViewHolder
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * binds the image data to the view holder so it displays correctly
     *
     * @param holder   the view holder for the row
     * @param position position of the image in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem item = imageList.get(position);

        if (item.imageUrl != null && !item.imageUrl.startsWith("http")) {
            // Handle Base64 encoded image
            try {
                byte[] decodedString = Base64.decode(item.imageUrl, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageView.setImageBitmap(decodedByte);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else {
            // Handle URL
            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imageView);
        }

        holder.detailsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("imageId", imageIds.get(position));
            bundle.putString("imageUrl", item.imageUrl);
            bundle.putString("uploadedBy", item.uploadedBy);
            Navigation.findNavController(v).navigate(R.id.adminImageDetails, bundle);
        });
    }

    /**
     * returns the number of images in the list
     *
     * @return number of images
     */
    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        AppCompatButton detailsButton;

        /**
         * view holder class that stores the views for a single image item
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_preview);
            detailsButton = itemView.findViewById(R.id.btn_image_details);
        }
    }
}
