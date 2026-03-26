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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
/**
 *this fragment allows the admin to browse all events in the system
 *it loads event data from Firebase Firestore and displays them in a RecyclerView
 *each event is shown using the AdminEventAdapter so the admin can view event information
 *basically this page lets the admin see the list of events that currently exist in the database
 */
public class AdminBrowseEvents extends Fragment {

    private ArrayList<EventDetail> eventList;
    private ArrayList<String> eventIds;
    private AdminEventAdapter adapter;
    /**
     * empty public constructor for this fragment
     */
    public AdminBrowseEvents() {
    }
    /**
     * creates the view for the admin browse events page
     * it sets up the recyclerview and loads event data from firestore
     *
     * @param inflater used to inflate the fragment layout
     * @param container parent view that the fragment layout will be attached to
     * @param savedInstanceState previous saved state if there is one
     * @return the root view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_events, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList = new ArrayList<>();
        eventIds = new ArrayList<>();
        adapter = new AdminEventAdapter(eventList, eventIds);


        recyclerView.setAdapter(adapter);
        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    eventIds.clear();
                    // loop through all events and save them into the list
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        EventDetail event = doc.toObject(EventDetail.class);
                        if (event != null) {
                            eventList.add(event);
                            eventIds.add(doc.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
        return view;
    }
}