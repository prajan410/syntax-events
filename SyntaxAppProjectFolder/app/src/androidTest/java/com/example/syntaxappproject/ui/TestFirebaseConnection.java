package com.example.syntaxappproject.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.syntaxappproject.ImageItem;
import com.example.syntaxappproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestFirebaseConnection {

    @Test
    public void writeAndReadBackImage() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bell);
        assertNotNull("Could not load bell.png from resources", originalBitmap);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        String testEventId = "test_bell_event_" + System.currentTimeMillis();
        

        Map<String, Object> data = new HashMap<>();
        data.put("image", encodedImage);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("event_posters").child(testEventId);

        CountDownLatch latch = new CountDownLatch(1);

        ref.setValue(data)
                .addOnSuccessListener(unused -> {
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String retrievedEncodedImage = snapshot.child("image").getValue(String.class);
                            assertNotNull("Retrieved image string is null", retrievedEncodedImage);
                            assertEquals("Sent and retrieved base64 strings are not the same", encodedImage, retrievedEncodedImage);

                            byte[] decodedByteArray = Base64.decode(retrievedEncodedImage, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                            assertNotNull("Could not decode retrieved string back to Bitmap", decodedBitmap);
                            
                            assertEquals("Original and decoded widths do not match", originalBitmap.getWidth(), decodedBitmap.getWidth());
                            assertEquals("Original and decoded heights do not match", originalBitmap.getHeight(), decodedBitmap.getHeight());

                            ref.removeValue();
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            fail("Read cancelled: " + error.getMessage());
                            latch.countDown();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    fail("Write failed: " + e.getMessage());
                    latch.countDown();
                });

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("Timed out waiting for Firebase response");
        }
    }
}
