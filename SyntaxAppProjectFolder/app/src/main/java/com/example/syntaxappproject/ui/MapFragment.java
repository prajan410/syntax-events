package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * A Fragment that displays a map with pins for entrants who have signed up for an event.
 * Uses osmdroid for map rendering and Firebase Firestore for entrant location data.
 */
public class MapFragment extends HomeBar {
    MapView map;
    private String eventId;
    FirebaseFirestore db;
    ProfileRepository profileRepo;
    AuthenticationService authService;
    EventDetailRepository eventRepo;

    /**
     * Initializes the fragment, sets up osmdroid configuration, and initializes repositories.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());
        if (db == null) db = FirebaseFirestore.getInstance();
        if (profileRepo == null) profileRepo = new ProfileRepository();
        if (authService == null) authService = new AuthenticationService();
        if (eventRepo == null) eventRepo = new EventDetailRepository();
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    /**
     * Inflates the layout for the map fragment.
     * @param inflater The LayoutInflater object.
     * @param container The parent view group.
     * @param savedInstanceState The saved instance state.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * Sets up the UI, initializes the map view, and starts loading data.
     * @param view The fragment view.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view);

        map = view.findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController controller = map.getController();
        controller.setZoom(10.0);
        controller.setCenter(new GeoPoint(53.5461, -113.4938)); // Default Edmonton

        if (eventId != null) {
            loadEntrantLocations();
        } else {
            Toast.makeText(getContext(), "No event ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads the geolocation data for all entrants on the waitlist from Firestore
     * and adds markers to the map.
     */
    void loadEntrantLocations() {
        if (db == null) return;
        db.collection("events")
                .document(eventId)
                .collection("waitlist-entrants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double lat = document.getDouble("latitude");
                        Double lon = document.getDouble("longitude");
                        String userId = document.getId();

                        if (lat != null && lon != null) {
                            addMarkerForUser(userId, lat, lon);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MapFragment", "Error loading entrants", e));
    }

    /**
     * Adds a marker for a specific user to the map after fetching their profile name.
     * @param userId The ID of the user.
     * @param lat The latitude.
     * @param lon The longitude.
     */
    void addMarkerForUser(String userId, double lat, double lon) {
        profileRepo.getProfile(userId, profile -> {
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Marker marker = new Marker(map);
                    marker.setPosition(new GeoPoint(lat, lon));
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle(profile.getName());
                    map.getOverlays().add(marker);
                    map.invalidate();
                });
            }
        });
    }

    /**
     * Resumes map rendering when the fragment resumes.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    /**
     * Pauses map rendering when the fragment pauses.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}
