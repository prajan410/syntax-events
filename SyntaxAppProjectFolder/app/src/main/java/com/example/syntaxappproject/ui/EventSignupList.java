package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.R;

import java.util.ArrayList;

public class EventSignupList extends HomeBar {
    private ArrayList<String> finalEntrants; // remove later
    private ArrayList<String> invitedEntrants; // remove later
    private ArrayList<String> waitingEntrants; // remove later
    private ArrayList<String> cancelledEntrants; // remove later
    boolean displayFinalEntrants = false;
    boolean displayInvitedEntrants = false;
    boolean displayWaitingList = false;
    boolean displayCancelledEntrants = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_signup_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView(view.findViewById(R.id.finalEntrants));
        setupRecyclerView(view.findViewById(R.id.invitedEntrants));
        setupRecyclerView(view.findViewById(R.id.waitingList));
        setupRecyclerView(view.findViewById(R.id.cancelledEntrants));


        view.findViewById(R.id.finalEntrantsTitle).setOnClickListener(v -> {
            displayFinalEntrants = !displayFinalEntrants;
            if (displayFinalEntrants) {
                view.findViewById(R.id.finalEntrants).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.finalEntrants).setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.invitedEntrantsTitle).setOnClickListener(v -> {
            displayInvitedEntrants = !displayInvitedEntrants;
            if (displayInvitedEntrants) {
                view.findViewById(R.id.invitedEntrants).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.invitedEntrants).setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.waitingListTitle).setOnClickListener(v -> {
            displayWaitingList = !displayWaitingList;
            if (displayWaitingList) {
                view.findViewById(R.id.waitingList).setVisibility(View.VISIBLE);
                } else {
                view.findViewById(R.id.waitingList).setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.cancelledEntrantsTitle).setOnClickListener(v -> {
            displayCancelledEntrants = !displayCancelledEntrants;
            if (displayCancelledEntrants) {
                view.findViewById(R.id.cancelledEntrants).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.cancelledEntrants).setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Adapters will be set here once implemented
    }
}