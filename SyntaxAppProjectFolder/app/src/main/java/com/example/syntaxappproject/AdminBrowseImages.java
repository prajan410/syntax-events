package com.example.syntaxappproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * fragment for admin to browse all uploaded images in the system
 * it gets image data from Firebase Realtime Database and displays them in a recyclerview
 * admin can look at the images and manage them from here
 */
public class AdminBrowseImages extends Fragment {

    private ArrayList<ImageItem> imageList;
    private ArrayList<String> imageIds;
    private ImageAdapter adapter;

    /**
     * empty public constructor for this fragment
     */
    public AdminBrowseImages() {
    }

    /**
     * creates the view for the admin browse images page
     * it sets up the recyclerview and loads image data from Firebase Realtime Database
     *
     * @param inflater           used to inflate the fragment layout
     * @param container          parent view that the fragment layout will be attached to
     * @param savedInstanceState previous saved state if there is one
     * @return the root view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_images);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        imageList = new ArrayList<>();
        imageIds = new ArrayList<>();
        adapter = new ImageAdapter(imageList, imageIds);
        recyclerView.setAdapter(adapter);

        // Fetch posters from Realtime Database
        FirebaseDatabase.getInstance().getReference("event_posters")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        imageList.clear();
                        imageIds.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            String eventId = postSnapshot.getKey();
                            String base64Image = postSnapshot.child("image").getValue(String.class);
                            if (base64Image != null) {
                                imageList.add(new ImageItem(base64Image, "Event: " + eventId));
                                imageIds.add(eventId);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle potential errors
                    }
                });

        return view;
    }
}
