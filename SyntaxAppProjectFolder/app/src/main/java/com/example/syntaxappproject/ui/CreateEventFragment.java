package com.example.syntaxappproject.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.BulletPointHelper;
import com.example.syntaxappproject.EventViewModel;
import com.example.syntaxappproject.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
 */
public class CreateEventFragment extends HomeBar {

    private TextInputEditText eventNameInput, descriptionInput, capacityInput;
    private TextInputEditText eventStartDateInput, eventEndDateInput;
    private TextInputEditText regisStartDateInput, regisEndDateInput;
    private TextInputEditText lotteryCriteriaInput;
    private TextInputEditText locationInput;
    private SwitchMaterial geoSwitch;
    private SwitchMaterial privateEventSwitch;
    private List<String> allCities = new ArrayList<>();
    private boolean citiesLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try { setupHotbar(view); } catch (Exception ignored) {}

        privateEventSwitch  = view.findViewById(R.id.privateEventSwitch);
        eventNameInput      = view.findViewById(R.id.eventNameInput);
        descriptionInput    = view.findViewById(R.id.descriptionInput);
        locationInput       = view.findViewById(R.id.locationInput);
        capacityInput       = view.findViewById(R.id.capacityInput);
        eventStartDateInput = view.findViewById(R.id.eventStartDateInput);
        eventEndDateInput   = view.findViewById(R.id.eventEndDateInput);
        regisStartDateInput = view.findViewById(R.id.regisStartDateInput);
        regisEndDateInput   = view.findViewById(R.id.regisEndDateInput);
        geoSwitch           = view.findViewById(R.id.geolocationSwitch);
        lotteryCriteriaInput = view.findViewById(R.id.lotteryCriteriaInput);

        BulletPointHelper.setupBulletPointField(lotteryCriteriaInput);

        // Date pickers
        eventStartDateInput.setOnClickListener(v -> showDatePicker(eventStartDateInput));
        eventEndDateInput.setOnClickListener(v -> showDatePicker(eventEndDateInput));
        regisStartDateInput.setOnClickListener(v -> showDatePicker(regisStartDateInput));
        regisEndDateInput.setOnClickListener(v -> showDatePicker(regisEndDateInput));

        // Location autocomplete setup
        toggleLocationField(geoSwitch.isChecked());
        geoSwitch.setOnCheckedChangeListener((btn, isChecked) -> toggleLocationField(isChecked));
        loadCitiesAsync();

        // Entrance animations
        View headerTitle       = view.findViewById(R.id.headerTitle);
        View detailsCard       = view.findViewById(R.id.detailsCard);
        View capacityCard      = view.findViewById(R.id.capacityCard);
        View datesCard         = view.findViewById(R.id.datesCard);
        View actionCard        = view.findViewById(R.id.actionCard);
        View geolocationCard   = view.findViewById(R.id.GeolocationCard);
        View lotteryCriteriaCard = view.findViewById(R.id.lotteryCriteriaCard);
        View stepIndicator     = view.findViewById(R.id.stepIndicator);

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
        geolocationCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(350).start();
        lotteryCriteriaCard.setTranslationY(30f);
        lotteryCriteriaCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(430).start();
        actionCard.setTranslationY(30f);
        actionCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(500).start();

        NavController navController = NavHostFragment.findNavController(this);

        view.findViewById(R.id.continueButton).setOnClickListener(v -> {
            String name                      = getText(eventNameInput);
            String description               = getText(descriptionInput);
            String location                  = locationInput.getText() != null ? locationInput.getText().toString().trim() : "";
            String capacityStr               = getText(capacityInput);
            String startingEventDate         = getText(eventStartDateInput);
            String endingEventDate           = getText(eventEndDateInput);
            String startingRegistrationPeriod = getText(regisStartDateInput);
            String endingRegistrationPeriod  = getText(regisEndDateInput);
            String lotteryCriteria           = BulletPointHelper.getPlainText(getText(lotteryCriteriaInput));
            boolean geoEnabled               = geoSwitch.isChecked();

            if (name.isEmpty())        { toast("Event name is required"); return; }
            if (description.isEmpty()) { toast("Description is required"); return; }

            // Location is only required + validated when geo is enabled
            if (geoEnabled) {
                if (location.isEmpty()) {
                    toast("Location is required when geolocation is enabled");
                    locationInput.requestFocus();
                    return;
                }
                if (citiesLoaded) {
                    boolean valid = false;
                    for (String c : allCities) {
                        if (c.equalsIgnoreCase(location)) { valid = true; break; }
                    }
                    if (!valid) {
                        toast("Please select a valid city from the suggestions");
                        locationInput.requestFocus();
                        return;
                    }
                }
            }

            if (!isInteger(capacityStr))   { toast("Capacity must be a valid number"); return; }

            int capacity = 0;
            if (!capacityStr.isEmpty()) {
                capacity = Integer.parseInt(capacityStr);
                if (capacity < 1) { toast("Capacity must be 1 or greater"); return; }
            }

            EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
            viewModel.setName(name);
            viewModel.setGeoReq(geoEnabled);
            viewModel.setDescription(description);
            viewModel.setLocation(geoEnabled ? location : "");
            viewModel.setCapacity(capacity);
            viewModel.setStartingEventDate(startingEventDate);
            viewModel.setEndingEventDate(endingEventDate);
            viewModel.setStartingRegistrationPeriod(startingRegistrationPeriod);
            viewModel.setEndingRegistrationPeriod(endingRegistrationPeriod);
            viewModel.setLotteryCriteria(lotteryCriteria);
            viewModel.setPrivateEvent(privateEventSwitch.isChecked());

            navController.navigate(R.id.toUploadImageFragment);
        });
    }

    // ── City autocomplete ────────────────────────────────────────────────────

    private void loadCitiesAsync() {
        new Thread(() -> {
            List<String> cities = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(requireContext().getAssets().open("worldcities.csv")))) {

                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    // Handle quoted CSV properly
                    List<String> values = new ArrayList<>();
                    StringBuilder current = new StringBuilder();
                    boolean inQuotes = false;

                    for (char c : line.toCharArray()) {
                        if (c == '"') {
                            inQuotes = !inQuotes;
                        } else if (c == ',' && !inQuotes) {
                            values.add(current.toString().trim());
                            current.setLength(0);
                        } else {
                            current.append(c);
                        }
                    }
                    values.add(current.toString().trim());

                    // Based on your headers: city_ascii is index 1, country is index 5
                    if (values.size() >= 6) {
                        String city = values.get(1).replace("\"", "");  // city_ascii
                        String country = values.get(5).replace("\"", ""); // country
                        if (!city.isEmpty() && !country.isEmpty()) {
                            cities.add(city + ", " + country);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("CreateEventFragment", "Failed to load cities", e);
            }

            allCities = cities;
            citiesLoaded = true;

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                setupLocationAutocomplete();
                Log.d("CreateEventFragment", "Loaded " + allCities.size() + " cities");
                if (allCities.size() > 0) {
                    Log.d("CreateEventFragment", "Sample: " + allCities.get(0));
                }
            });
        }).start();
    }

    private void setupLocationAutocomplete() {
        Log.d("CreateEventFragment", "setupLocationAutocomplete called");
        Log.d("CreateEventFragment", "allCities size: " + allCities.size());

        // Create popup window for suggestions
        PopupWindow popupWindow = new PopupWindow(requireContext());
        ListView listView = new ListView(requireContext());
        listView.setBackgroundColor(0xFFFFFFFF);
        listView.setDividerHeight(1);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );
        listView.setAdapter(adapter);

        popupWindow.setContentView(listView);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(400);
        popupWindow.setFocusable(false);  // CHANGE: Don't let popup steal focus
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.editbox_background));

        // Text watcher for the input field
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                Log.d("CreateEventFragment", "Text changed: '" + query + "', length: " + query.length());

                if (query.length() >= 2) {
                    List<String> suggestions = new ArrayList<>();
                    for (String city : allCities) {
                        String cityName = city.split(",")[0].toLowerCase().trim();
                        if (cityName.startsWith(query)) {
                            suggestions.add(city);
                            if (suggestions.size() >= 20) break;
                        }
                    }

                    Log.d("CreateEventFragment", "Found " + suggestions.size() + " suggestions");

                    adapter.clear();
                    adapter.addAll(suggestions);
                    adapter.notifyDataSetChanged();

                    if (!suggestions.isEmpty()) {
                        if (!popupWindow.isShowing()) {
                            Log.d("CreateEventFragment", "Showing popup window");
                            popupWindow.showAsDropDown(locationInput, 0, 0);
                            // Keep keyboard open
                            locationInput.requestFocus();
                        }
                    } else if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                } else {
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        locationInput.addTextChangedListener(textWatcher);

        // Handle item click
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = adapter.getItem(position);
            Log.d("CreateEventFragment", "Selected: " + selected);

            // Get just the city name (before the comma)
            String selectedCity = selected.split(",")[0].trim();

            // Set the text to just the city name (or full with country if you prefer)
            locationInput.setText(selectedCity);
            locationInput.setSelection(selectedCity.length());

            // Dismiss popup after selection
            popupWindow.dismiss();

            // Keep keyboard open and focused
            locationInput.requestFocus();

            // Show keyboard
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(locationInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });

        // Handle focus - don't dismiss popup immediately on focus change
        locationInput.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("CreateEventFragment", "Focus changed: " + hasFocus);
            if (!hasFocus && popupWindow.isShowing()) {
                // Delay dismissal slightly to allow for item clicks
                locationInput.postDelayed(() -> {
                    if (popupWindow.isShowing() && !locationInput.hasFocus()) {
                        popupWindow.dismiss();
                    }
                }, 200);
            }
        });
    }

    private void toggleLocationField(boolean enabled) {
        locationInput.setEnabled(enabled);
        locationInput.setText("");
        locationInput.setAlpha(enabled ? 1f : 0.4f);
        locationInput.setHint(enabled
                ? "Start typing a city..."
                : "Enable geolocation to set location");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void showDatePicker(TextInputEditText target) {
        new android.app.DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) ->
                        target.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
                java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isInteger(String str) {
        if (str == null || str.isBlank()) return false;
        try { Integer.parseInt(str); return true; }
        catch (NumberFormatException e) { return false; }
    }
}