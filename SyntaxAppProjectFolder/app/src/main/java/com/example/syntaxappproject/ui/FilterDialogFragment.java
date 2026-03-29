package com.example.syntaxappproject.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.syntaxappproject.EventFilterViewModel;
import com.example.syntaxappproject.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Dialog fragment that show the filter.
 * User can select start date, end date and minimum capacity
 * to filter the display list of events.
 */
public class FilterDialogFragment extends DialogFragment {
    private TextInputEditText startDateFilter;
    private TextInputEditText endDateFilter;
    private TextInputEditText capacityFilter;
    private EventFilterViewModel filterViewModel;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_filter, null);

        startDateFilter = view.findViewById(R.id.startDateFilter);
        endDateFilter = view.findViewById(R.id.endDateFilter);
        capacityFilter = view.findViewById(R.id.capacityFilter);

        startDateFilter.setOnClickListener(v -> showDatePicker(startDateFilter));
        endDateFilter.setOnClickListener(v -> showDatePicker(endDateFilter));

        filterViewModel = new ViewModelProvider(requireActivity()).get(EventFilterViewModel.class);

        populateFields();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Filter Events")
                .setView(view)
                .setPositiveButton("Apply", (dialog, which) -> applyFilters())
                .setNeutralButton("Clear", (dialog, which) -> clearFilters())
                .setNegativeButton("Cancel", null)
                .create();
    }

    /**
     * Refill the previous filters.
     */
    private void populateFields() {
        if (filterViewModel.getStartValue() != null) {
            startDateFilter.setText(filterViewModel.getStartValue());
        }

        if (filterViewModel.getEndValue() != null) {
            endDateFilter.setText(filterViewModel.getEndValue());
        }

        if (filterViewModel.getCapacityValue() != -1L) {
            capacityFilter.setText(String.valueOf(filterViewModel.getCapacityValue()));
        }
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

    /**
     * Apply filters, store the value in filter view model
     */
    private void applyFilters() {
        String start = getText(startDateFilter);
        String end = getText(endDateFilter);
        String capacityStr = getText(capacityFilter);

        long cap = -1L;

        if (!capacityStr.isEmpty()) {
            cap = Long.parseLong(capacityStr.trim());
        }

        filterViewModel.setFilters(start, end, cap);
    }

    /**
     * Clear the whole filters
     */
    private void clearFilters() {

        filterViewModel.clearFilters();

        startDateFilter.setText("");
        endDateFilter.setText("");
        capacityFilter.setText("");

    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
