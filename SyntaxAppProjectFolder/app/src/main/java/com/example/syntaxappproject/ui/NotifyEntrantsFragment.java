package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.NotificationRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class NotifyEntrantsFragment extends Fragment {

    // --- Repos ---
    private final AuthenticationService authService = new AuthenticationService();
    private final NotificationRepository notificationRepository = new NotificationRepository();

    // --- Views ---
    private View sendToCard;
    private View messageCard;
    private MaterialButton sendButton;
    private MaterialButton coOrganizerInviteButton;

    private Chip chipWaitlist;
    private Chip chipSelected;
    private Chip chipCancelled;
    private Chip chipAll;

    private TextView recipientCount;
    private EditText messageInput;
    private TextView charCount;
    private EditText titleInput;
    private TextView titleCharCount;
    // --- Event ID passed from EventDetailFragment via Bundle ---
    private String eventId;

    public NotifyEntrantsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notify_entrants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Retrieve eventId from bundle ---
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // --- Bind views ---
        sendToCard     = view.findViewById(R.id.sendToCard);
        messageCard    = view.findViewById(R.id.messageCard);
        sendButton     = view.findViewById(R.id.sendButton);
        chipWaitlist   = view.findViewById(R.id.chipWaitlist);
        chipSelected   = view.findViewById(R.id.chipSelected);
        chipCancelled  = view.findViewById(R.id.chipCancelled);
        chipAll        = view.findViewById(R.id.chipAll);
        recipientCount = view.findViewById(R.id.recipientCount);
        messageInput   = view.findViewById(R.id.messageInput);
        charCount      = view.findViewById(R.id.charCount);
        titleInput = view.findViewById(R.id.titleInput);
        titleCharCount = view.findViewById(R.id.titleCharCount);


        // --- Hint color ---
        messageInput.setHintTextColor(Color.parseColor("#BBBBBB"));

        // --- Chips ---
        setupChip(chipWaitlist);
        setupChip(chipSelected);
        setupChip(chipCancelled);
        setupChip(chipAll);

        // --- Char counter ---
        setupCharCounter();

        // --- Send button ---
        sendButton.setOnClickListener(v -> sendNotification());

        coOrganizerInviteButton = view.findViewById(R.id.coOrganizerInviteButton);
        coOrganizerInviteButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            NavHostFragment.findNavController(this).navigate(R.id.coOrganizerInviteFragment, bundle);
        });

        // --- Animations ---
        animateIn();
    }

    /**
     * Applies checked/unchecked styling to a chip and attaches
     * a listener that refreshes the style and recipient count on change.
     */
    private void setupChip(Chip chip) {
        updateChipStyle(chip);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateChipStyle(chip);
            updateRecipientCount();
        });
    }

    /**
     * Updates a chip's background, stroke, and text color
     * based on whether it is currently checked.
     */
    private void updateChipStyle(Chip chip) {
        if (chip.isChecked()) {
            chip.setChipBackgroundColorResource(R.color.chip_checked_bg);
            chip.setChipStrokeColorResource(R.color.chip_checked_stroke);
            chip.setTextColor(getResources().getColor(R.color.chip_checked_stroke, null));
        } else {
            chip.setChipBackgroundColorResource(R.color.chip_unchecked_bg);
            chip.setChipStrokeColorResource(R.color.chip_unchecked_stroke);
            chip.setTextColor(Color.parseColor("#888888"));
        }
    }

    /**
     * Updates the "X groups selected" label beneath the chips.
     * Turns red if nothing is selected to warn the organizer.
     */
    private void updateRecipientCount() {
        int count = 0;
        if (chipWaitlist.isChecked())  count++;
        if (chipSelected.isChecked())  count++;
        if (chipCancelled.isChecked()) count++;
        if (chipAll.isChecked())       count++;

        if (count == 0) {
            recipientCount.setText("No groups selected");
            recipientCount.setTextColor(Color.parseColor("#E24B4A"));
        } else {
            recipientCount.setText(count + " group" + (count > 1 ? "s" : "") + " selected");
            recipientCount.setTextColor(Color.parseColor("#2ECC71"));
        }
    }

    /**
     * Attaches a TextWatcher to messageInput that updates
     * the character counter label as the user types.
     */
    private void setupCharCounter() {
        titleCharCount.setText("0 / 50");
        titleInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleCharCount.setText(s.length() + " / 50");
                titleCharCount.setTextColor(s.length() >= 40 //change color of title
                        ? Color.parseColor("#E24B4A")
                        : Color.parseColor("#BBBBBB"));

            }
        });
        charCount.setText("0 / 500");

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCount.setText(s.length() + " / 500");
                charCount.setTextColor(s.length() >= 450
                        ? Color.parseColor("#E24B4A")
                        : Color.parseColor("#BBBBBB"));
            }
        });

    }

    /**
     * Validates inputs then fires one sendToGroup() call per checked chip.
     */
    private void sendNotification() {
        String title = titleInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedGroups = new ArrayList<>();
        if (chipWaitlist.isChecked())  selectedGroups.add("WAITLIST");
        if (chipSelected.isChecked())  selectedGroups.add("SELECTED");
        if (chipCancelled.isChecked()) selectedGroups.add("CANCELLED");
        if (chipAll.isChecked())       selectedGroups.add("ALL");

        if (selectedGroups.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one group", Toast.LENGTH_SHORT).show();
            return;
        }

        Notification notif = new Notification();
        notif.setEventId(eventId);
        notif.setSenderId(authService.getCurrentUserId());
        notif.setTitle(title);
        notif.setSenderRole("ORGANIZER");
        notif.setTargetGroup(String.join(",", selectedGroups)); // e.g. "WAITLIST,SELECTED"
        notif.setBody(message);
        notif.setTimestamp(System.currentTimeMillis());
        notif.setStatus("SENT");

        notificationRepository.sendNotification(notif, eventId, selectedGroups, success ->
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        messageInput.setText("");
                        titleInput.setText("");
                        Toast.makeText(getContext(), "Notification sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to send", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    /**w
     * Builds a Notification object and writes it to Firestore via the repo.
     * On success, clears the input field.
     *
     * @param targetGroup one of "WAITLIST", "SELECTED", "CANCELLED", "ALL"
     * @param message     the message body to send
     */


    /**
     * Fades in cards sequentially with an 80ms stagger.
     * Matches entrance animation style used across other fragments.
     */
    private void animateIn() {
        View[] views = { sendToCard, messageCard, sendButton, };

        AnimatorSet set = new AnimatorSet();
        android.animation.Animator[] animators = new android.animation.Animator[views.length];

        for (int i = 0; i < views.length; i++) {
            ObjectAnimator fade = ObjectAnimator.ofFloat(views[i], "alpha", 0f, 1f);
            fade.setStartDelay(i * 80L);
            fade.setDuration(300);
            animators[i] = fade;
        }

        set.playTogether(animators);
        set.start();
    }
}