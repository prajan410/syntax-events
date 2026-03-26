package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.EventViewModel;
import com.example.syntaxappproject.R;
import com.google.android.material.textfield.TextInputEditText;
/**
 * Fragment that presents the event creation form to an organizer.
 * <p>
 * Collects event name, description, location, capacity, event dates,
 * and registration period dates. On successful validation, the input
 * is stored in a shared {@link EventViewModel} and the user is
 * navigated to the poster upload step.
 * </p>
 *
 * <p>Extends {@link HomeBar} to inherit the bottom navigation hotbar.</p>
 *
 * <p>Outstanding issues: geo-requirement flag is not yet collected on
 * this screen; lottery criteria input is also absent.</p>
 */
public class CreateEventFragment extends HomeBar {

    private TextInputEditText eventNameInput, descriptionInput, locationInput, capacityInput;
    private TextInputEditText eventStartDateInput, eventEndDateInput;
    private TextInputEditText regisStartDateInput, regisEndDateInput;

    /**
     * Inflates the create event layout.
     *
     * @param inflater  the layout inflater
     * @param container the parent view group
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the inflated view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }


    /**
     * Called immediately after {@link #onCreateView}. Binds input fields,
     * sets up date pickers, applies entrance animations, and attaches the
     * continue button click handler.
     *
     * @param view               the view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view);

        eventNameInput    = view.findViewById(R.id.eventNameInput);
        descriptionInput  = view.findViewById(R.id.descriptionInput);
        locationInput     = view.findViewById(R.id.locationInput);
        capacityInput     = view.findViewById(R.id.capacityInput);
        eventStartDateInput = view.findViewById(R.id.eventStartDateInput);
        eventEndDateInput   = view.findViewById(R.id.eventEndDateInput);
        regisStartDateInput = view.findViewById(R.id.regisStartDateInput);
        regisEndDateInput   = view.findViewById(R.id.regisEndDateInput);

        // Date pickers
        eventStartDateInput.setOnClickListener(v -> showDatePicker(eventStartDateInput));
        eventEndDateInput.setOnClickListener(v -> showDatePicker(eventEndDateInput));
        regisStartDateInput.setOnClickListener(v -> showDatePicker(regisStartDateInput));
        regisEndDateInput.setOnClickListener(v -> showDatePicker(regisEndDateInput));

        // Entrance animations
        View headerTitle = view.findViewById(R.id.headerTitle);
        View detailsCard = view.findViewById(R.id.detailsCard);
        View capacityCard = view.findViewById(R.id.capacityCard);
        View datesCard   = view.findViewById(R.id.datesCard);
        View actionCard  = view.findViewById(R.id.actionCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        detailsCard.setTranslationY(30f);
        detailsCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(200).start();

        capacityCard.setTranslationY(30f);
        capacityCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(300).start();

        datesCard.setTranslationY(30f);
        datesCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(380).start();

        actionCard.setTranslationY(30f);
        actionCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(450).start();

        NavController navController = NavHostFragment.findNavController(this);

        view.findViewById(R.id.continueButton).setOnClickListener(v -> {
            String name        = getText(eventNameInput);
            String description = getText(descriptionInput);
            String location    = getText(locationInput);
            String capacityStr = getText(capacityInput);
            String startingEventDate = getText(eventStartDateInput);
            String endingEventDate = getText(eventEndDateInput);
            String startingRegistrationPeriod = getText(regisStartDateInput);
            String endingRegistrationPeriod = getText(regisEndDateInput);

            // --Error check inputs --
            if (name.isEmpty()) {
                toast("Event name is required"); return;
            }
            if (description.isEmpty()) {
                toast("Description is required"); return;
            }
            if (location.isEmpty()) {
                toast("Location is required"); return;
            }
            if (!isInteger(capacityStr)) {
                toast("Capacity must be a valid number"); return;
            }

            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                toast("Capacity must be greater than 0"); return;
            }

            // -- Save values into view model --
            EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
            viewModel.setName(name);
            viewModel.setDescription(description);
            viewModel.setLocation(location);
            viewModel.setCapacity(capacity);
            viewModel.setStartingEventDate(startingEventDate);
            viewModel.setEndingEventDate(endingEventDate);
            viewModel.setStartingRegistrationPeriod(startingRegistrationPeriod);
            viewModel.setEndingRegistrationPeriod(endingRegistrationPeriod);

            navController.navigate(R.id.toUploadImageFragment);
        });
    }


    /**
     * Displays a {@link android.app.DatePickerDialog} and writes the selected
     * date into the given input field in {@code YYYY-MM-DD} format.
     *
     * @param target the {@link TextInputEditText} to populate with the selected date
     */
    private void showDatePicker(TextInputEditText target) {
        android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                    target.setText(date);
                },
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
                java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }


    /**
     * Safely extracts and trims the text from a {@link TextInputEditText}.
     *
     * @param field the input field to read
     * @return the trimmed string, or an empty string if the field is {@code null}
     */
    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }


    /**
     * Displays a short {@link Toast} message.
     *
     * @param msg the message to display
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * Checks whether the given string can be parsed as an integer.
     *
     * @param str the string to validate
     * @return {@code true} if {@code str} is a valid integer, {@code false} otherwise
     */
    private boolean isInteger(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
