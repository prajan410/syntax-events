package com.example.syntaxappproject;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;

/**
 * Instrumented test class for verifying Firebase Realtime Database functionality.
 */
public class FirebaseDatabaseTest {

    /**
     * Tests basic read and write operations to the Firebase Realtime Database.
     * Sets a value in the "test" node and verifies it via a ValueEventListener.
     */
    @Test
    public void testFirebaseDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("test");

        myRef.setValue("hello realtime database");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                System.out.println("Value is: " + value);
                assert value != null;
                assert value.equals("hello realtime database");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Failed to read value: " + error.getMessage());
                assert false;
            }
        });
    }
}
