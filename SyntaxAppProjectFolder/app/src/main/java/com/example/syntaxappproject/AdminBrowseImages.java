package com.example.syntaxappproject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Fragment for administrators to browse all event posters stored in the cache.
 * Displays images in a grid layout with entrance animations.
 */
public class AdminBrowseImages extends Fragment {

    private ArrayList<ImageItem> imageList;
    private ArrayList<String> imageIds;
    private ArrayList<ImageItem> filteredList;
    private ArrayList<String> filteredIds;
    private ImageAdapter adapter;
    private RecyclerView recyclerView;
    private View loadingSpinner;
    private View emptyText;
    private View mainCard;
    private View headerTitle;
    private TextView imageCountBadge;

    private DatabaseReference postersRef;
    private ValueEventListener postersListener;

    /**
     * Default constructor required for fragment instantiation.
     */
    public AdminBrowseImages() {}

    /**
     * Inflates the layout, initializes views, sets up animations,
     * and loads cached event posters.
     *
     * @param inflater           the layout inflater used to inflate the fragment's view
     * @param container          the parent view group that the fragment's UI attaches to
     * @param savedInstanceState previously saved instance state, if any
     * @return the inflated view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);

        recyclerView    = view.findViewById(R.id.recycler_images);
        loadingSpinner  = view.findViewById(R.id.loadingSpinner);
        emptyText       = view.findViewById(R.id.emptyText);
        mainCard        = view.findViewById(R.id.mainCard);
        headerTitle     = view.findViewById(R.id.headerTitle);
        imageCountBadge = view.findViewById(R.id.imageCountBadge);

        Button doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminFragment);
        });

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        imageCountBadge.animate().alpha(1f)
                .setDuration(300).setStartDelay(200).start();

        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(250).start();

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        imageList    = new ArrayList<>();
        imageIds     = new ArrayList<>();
        filteredList = new ArrayList<>();
        filteredIds  = new ArrayList<>();
        adapter      = new ImageAdapter(filteredList, filteredIds);
        recyclerView.setAdapter(adapter);

        loadingSpinner.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        loadFromCache();

        return view;
    }

    /**
     * Reads all bitmaps directly from ImageCacheManager and publishes
     * them to the adapter instantly. No database call, no decoding.
     */
    private void loadFromCache() {
        imageList.clear();
        imageIds.clear();

        for (Map.Entry<String, Bitmap> entry : ImageCacheManager.getAll().entrySet()) {
            String eventId = entry.getKey();
            imageList.add(new ImageItem(eventId, eventId));
            imageIds.add(eventId);
        }

        filteredList.clear();
        filteredIds.clear();
        filteredList.addAll(imageList);
        filteredIds.addAll(imageIds);

        adapter.notifyDataSetChanged();
        loadingSpinner.setVisibility(View.GONE);

        int count = imageList.size();
        imageCountBadge.setText(count + (count == 1 ? " poster" : " posters"));

        updateEmptyState();
    }

    /**
     * Updates the UI based on whether there are images to display.
     * Shows the empty text if no images exist, otherwise shows the RecyclerView.
     */
    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    /**
     * Cleans up Firebase database listeners when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postersRef != null && postersListener != null) {
            postersRef.removeEventListener(postersListener);
            postersListener = null;
        }
    }
}