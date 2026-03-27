package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.syntaxappproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.CaptureManager;

public class QRCodeScannerFragment extends HomeBar {

    private DecoratedBarcodeView barcodeView;
    private CaptureManager capture;
    private boolean flashlightOn = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_scan_qrcode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        barcodeView = view.findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText("");
        
        capture = new CaptureManager(requireActivity(), barcodeView);
        capture.initializeFromIntent(requireActivity().getIntent(), savedInstanceState);
        capture.decode();
        barcodeView.decodeContinuous(callback);

        View backButton      = view.findViewById(R.id.back_button);
        View flashButton     = view.findViewById(R.id.flashlight_button);
        View instructionText = view.findViewById(R.id.instructionText);
        View flashLabel      = view.findViewById(R.id.flashlightLabel);

        backButton.setTranslationX(-60f);
        backButton.animate().alpha(1f).translationX(0f)
                .setDuration(400).setStartDelay(100).start();

        instructionText.setTranslationY(-20f);
        instructionText.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(200).start();

        flashButton.setTranslationY(40f);
        flashButton.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(300).start();

        flashLabel.animate().alpha(1f)
                .setDuration(300).setStartDelay(450).start();

        FloatingActionButton fab = view.findViewById(R.id.flashlight_button);
        fab.setOnClickListener(v -> {
            if(flashlightOn) {
                flashlightOn = false;
                barcodeView.setTorchOff();
            } else {
                flashlightOn = true;
                barcodeView.setTorchOn();
            }
        });

        view.findViewById(R.id.back_button).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String qrText = result.getText();
            if (qrText != null) {
                barcodeView.pause();
                Bundle bundle = new Bundle();
                bundle.putString("eventId", qrText);
                Navigation.findNavController(requireView()).navigate(R.id.toEventDetailFragment, bundle);
            }
        }
    };
}
