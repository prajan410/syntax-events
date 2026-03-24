package com.example.syntaxappproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

/**
 * Fragment for admin to browse all uploaded images in the system.
 * Fetches poster images from Firestore event documents and displays
 * them in a RecyclerView. Listens for real-time updates so newly
 * uploaded posters appear without requiring a manual refresh.
 *
 * Images are stored as Base64 strings in the {@code poster} field of
 * each event document. Bitmaps are pre-cached via {@link ImageCacheManager}
 * for smooth scrolling performance.
 */
public class AdminBrowseImages extends Fragment {

    private ArrayList<ImageItem> imageList;
    private ArrayList<String> imageIds;
    private ImageAdapter adapter;
    private RecyclerView recyclerView;
    private View loadingSpinner;

    // Held so we can detach the listener when the fragment is destroyed
    private ListenerRegistration firestoreListener;

    /**
     * Empty public constructor required by the Fragment lifecycle.
     */
    public AdminBrowseImages() {
    }

    /**
     * Inflates the layout, initializes the RecyclerView, shows the loading
     * spinner, and attaches a real-time Firestore snapshot listener that
     * keeps the image list up to date automatically.
     *
     * @param inflater           used to inflate the fragment layout
     * @param container          parent view the fragment will attach to
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the root view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);

        recyclerView = view.findViewById(R.id.recycler_images);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        imageList = new ArrayList<>();
        imageIds = new ArrayList<>();
        adapter = new ImageAdapter(imageList, imageIds);
        recyclerView.setAdapter(adapter);

        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        attachFirestoreListener();

        return view;
    }

    /**
     * Attaches a real-time snapshot listener to the Firestore {@code events}
     * collection. On each update the image list is rebuilt from scratch so
     * deletions and edits are reflected immediately.
     */
    private void attachFirestoreListener() {
        firestoreListener = FirebaseFirestore.getInstance()
                .collection("events")
                .addSnapshotListener((querySnapshots, error) -> {
                    if (!isAdded()) return;

                    if (error != null) {
                        if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Failed to load images: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshots == null) return;

                    imageList.clear();
                    imageIds.clear();

                    for (DocumentSnapshot doc : querySnapshots) {
                        String posterBase64 = doc.getString("poster");
                        if (posterBase64 == null || posterBase64.isEmpty()) continue;

                        String eventName = doc.getString("name");
                        if (eventName == null || eventName.isEmpty()) {
                            eventName = "Unknown Event";
                        }

                        preCacheImage(doc.getId(), posterBase64);
                        imageList.add(new ImageItem(posterBase64, eventName));
                        imageIds.add(doc.getId());
                    }

                    adapter.notifyDataSetChanged();

                    if (loadingSpinner != null) {
                        loadingSpinner.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    if (imageList.isEmpty()) {
                        Toast.makeText(getContext(),
                                "No images found",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Pre-caches the Base64 string and decoded {@link Bitmap} in
     * {@link ImageCacheManager} so the adapter can display images
     * immediately without blocking the main thread.
     *
     * @param eventId    the event ID used as the cache key
     * @param base64Data the Base64 encoded image string
     */
    private void preCacheImage(String eventId, String base64Data) {
        ImageCacheManager.putBase64(eventId, base64Data);

        new Thread(() -> {
            try {
                byte[] decoded = Base64.decode(base64Data, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    ImageCacheManager.put(eventId, bitmap);
                }
            } catch (Exception ignored) {
                // Adapter will decode on demand if caching fails
            }
        }).start();
    }

    /**
     * Detaches the Firestore snapshot listener when the fragment is destroyed
     * to prevent memory leaks and callbacks on a dead fragment.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }
}