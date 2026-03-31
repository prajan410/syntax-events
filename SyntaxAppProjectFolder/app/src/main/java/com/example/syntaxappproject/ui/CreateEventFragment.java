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
import com.example.syntaxappproject.BulletPointHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment that presents the event creation form to an organizer.
 * Collects event name, description, location, capacity, event dates,
 * registration period dates, and lottery criteria. On successful validation,
 * the input is stored in a shared {@link EventViewModel} and the user is
 * navigated to the poster upload step.
 */
public class CreateEventFragment extends HomeBar {

    private TextInputEditText eventNameInput, descriptionInput, locationInput, capacityInput;
    private TextInputEditText eventStartDateInput, eventEndDateInput;
    private TextInputEditText regisStartDateInput, regisEndDateInput;
    private TextInputEditText lotteryCriteriaInput;
    private SwitchMaterial geoSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try { setupHotbar(view); } catch (Exception ignored) {}

        eventNameInput = view.findViewById(R.id.eventNameInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        locationInput = view.findViewById(R.id.locationInput);
        capacityInput = view.findViewById(R.id.capacityInput);
        eventStartDateInput = view.findViewById(R.id.eventStartDateInput);
        eventEndDateInput = view.findViewById(R.id.eventEndDateInput);
        regisStartDateInput = view.findViewById(R.id.regisStartDateInput);
        regisEndDateInput = view.findViewById(R.id.regisEndDateInput);
        geoSwitch = view.findViewById(R.id.geolocationSwitch);
        lotteryCriteriaInput = view.findViewById(R.id.lotteryCriteriaInput);

        BulletPointHelper.setupBulletPointField(lotteryCriteriaInput);

        eventStartDateInput.setOnClickListener(v -> showDatePicker(eventStartDateInput));
        eventEndDateInput.setOnClickListener(v -> showDatePicker(eventEndDateInput));
        regisStartDateInput.setOnClickListener(v -> showDatePicker(regisStartDateInput));
        regisEndDateInput.setOnClickListener(v -> showDatePicker(regisEndDateInput));

        View headerTitle = view.findViewById(R.id.headerTitle);
        View detailsCard = view.findViewById(R.id.detailsCard);
        View capacityCard = view.findViewById(R.id.capacityCard);
        View datesCard = view.findViewById(R.id.datesCard);
        View actionCard = view.findViewById(R.id.actionCard);
        View geolocationCard = view.findViewById(R.id.GeolocationCard);
        View lotteryCriteriaCard = view.findViewById(R.id.lotteryCriteriaCard);
        View stepIndicator = view.findViewById(R.id.stepIndicator);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();

        stepIndicator.setTranslationY(-20f);
        stepIndicator.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(150).start();

        detailsCard.setTranslationY(30f);
        detailsCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();

        capacityCard.setTranslationY(30f);
        capacityCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();

        datesCard.setTranslationY(30f);
        datesCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(380).start();

        geolocationCard.setTranslationY(30f);
        geolocationCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(350).start();

        lotteryCriteriaCard.setTranslationY(30f);
        lotteryCriteriaCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(430).start();

        actionCard.setTranslationY(30f);
        actionCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(500).start();

        NavController navController = NavHostFragment.findNavController(this);

        view.findViewById(R.id.continueButton).setOnClickListener(v -> {
            String name = getText(eventNameInput);
            String description = getText(descriptionInput);
            String location = getText(locationInput);
            String capacityStr = getText(capacityInput);
            String startingEventDate = getText(eventStartDateInput);
            String endingEventDate = getText(eventEndDateInput);
            String startingRegistrationPeriod = getText(regisStartDateInput);
            String endingRegistrationPeriod = getText(regisEndDateInput);

            String lotteryCriteria = BulletPointHelper.getPlainText(getText(lotteryCriteriaInput));

            if (name.isEmpty()) {
                toast("Event name is required");
                return;
            }
            if (description.isEmpty()) {
                toast("Description is required");
                return;
            }
            if (location.isEmpty()) {
                toast("Location is required");
                return;
            }
            if (!isInteger(capacityStr)) {
                toast("Capacity must be a valid number");
                return;
            }

            int capacity = 0;
            if (!capacityStr.isEmpty()) {
                if (!isInteger(capacityStr)) {
                    toast("Capacity must be a valid number");
                    return;
                }
                capacity = Integer.parseInt(capacityStr);
                if (capacity < 1) {
                    toast("Capacity must be 1 or greater");
                    return;
                }
            }

            EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
            viewModel.setName(name);
            viewModel.setGeoReq(geoSwitch.isChecked());
            viewModel.setDescription(description);
            viewModel.setLocation(location);
            viewModel.setCapacity(capacity);
            viewModel.setStartingEventDate(startingEventDate);
            viewModel.setEndingEventDate(endingEventDate);
            viewModel.setStartingRegistrationPeriod(startingRegistrationPeriod);
            viewModel.setEndingRegistrationPeriod(endingRegistrationPeriod);
            viewModel.setLotteryCriteria(lotteryCriteria);

            navController.navigate(R.id.toUploadImageFragment);
        });
    }

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

    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

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