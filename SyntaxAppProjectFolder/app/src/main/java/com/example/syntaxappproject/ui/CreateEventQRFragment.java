package com.example.syntaxappproject.ui;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.syntaxappproject.QRCodeService;
import com.example.syntaxappproject.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CreateEventQRFragment extends HomeBar {

    private String eventId;
    private Bitmap qrBitmap;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event_qr_step, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        TextView successText = view.findViewById(R.id.success_text);
        ImageView qrPreview  = view.findViewById(R.id.event_qr_preview);
        View headerTitle     = view.findViewById(R.id.headerTitle);
        View qrCard          = view.findViewById(R.id.qrCard);
        View actionsCard     = view.findViewById(R.id.actionsCard);

        // --- Entrance Animations ---
        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        qrCard.setTranslationY(30f);
        qrCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(250).start();

        actionsCard.setTranslationY(30f);
        actionsCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(380).start();

        // --- Generate QR off main thread ---
        if (eventId != null) {
            if (successText != null) successText.setText(eventId + " successfully created!");

            new Thread(() -> {
                Bitmap bitmap = QRCodeService.generateQRCode(eventId);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    qrBitmap = bitmap;
                    if (qrPreview != null && bitmap != null) {
                        qrPreview.setImageBitmap(bitmap);
                    }
                });
            }).start();
        }


        view.findViewById(R.id.download_button).setOnClickListener(v -> downloadQRCode());

        view.findViewById(R.id.done_button).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_createEventQRFragment_to_homeFragment)
        );
    }


    /**
     * Saves the generated QR code bitmap as a PNG file to the device.
     * <p>
     * On Android Q (API 29) and above, uses {@link MediaStore} to write
     * to the shared {@code Pictures/SyntaxEvents} directory. On older
     * versions, writes to the app's external files directory instead.
     * </p>
     * <p>
     * Displays a toast on both success and failure. Does nothing if
     * {@link #qrBitmap} has not yet been populated.
     * </p>
     */
    private void downloadQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(getContext(), "QR code not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = "QR_" + eventId + ".png";
        OutputStream fos = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/SyntaxEvents");

                Uri imageUri = requireContext().getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri != null) {
                    fos = requireContext().getContentResolver().openOutputStream(imageUri);
                }
            } else {
                File imagesDir = new File(requireContext().getExternalFilesDir(null), "SyntaxEvents");
                if (!imagesDir.exists()) imagesDir.mkdirs();
                fos = new FileOutputStream(new File(imagesDir, filename));
            }

            if (fos != null) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(getContext(), "QR Code saved to Pictures", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to download QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
