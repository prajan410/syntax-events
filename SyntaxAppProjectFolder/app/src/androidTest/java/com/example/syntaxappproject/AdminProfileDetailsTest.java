package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.button.MaterialButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for AdminProfileDetails
 * avoid Espresso input/click injection on API 36
 */
@RunWith(AndroidJUnit4.class)
public class AdminProfileDetailsTest {

    private NavController mockNavController;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
    }

    private Bundle createActiveBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("profileId", "user-123");
        bundle.putString("name", "Jane Doe");
        bundle.putString("email", "jane@example.com");
        bundle.putString("phone", "7805551234");
        bundle.putString("deviceId", "device-123");
        bundle.putBoolean("isAdmin", true);
        bundle.putBoolean("isOrganizer", true);
        bundle.putBoolean("isEntrant", false);
        bundle.putBoolean("isDeleted", false);
        bundle.putString("role", "Admin Organizer");
        return bundle;
    }

    private Bundle createDeletedBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("profileId", "deleted-123");
        bundle.putString("name", "Old User");
        bundle.putString("email", "old@example.com");
        bundle.putString("phone", "5870001111");
        bundle.putString("deviceId", "device-old");
        bundle.putBoolean("isAdmin", false);
        bundle.putBoolean("isOrganizer", false);
        bundle.putBoolean("isEntrant", true);
        bundle.putBoolean("isDeleted", true);
        bundle.putString("role", "Entrant");
        return bundle;
    }

    private Bundle createSingleRoleBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("profileId", "single-123");
        bundle.putString("name", "Solo Organizer");
        bundle.putString("email", "solo@example.com");
        bundle.putString("phone", "7802223333");
        bundle.putString("deviceId", "device-solo");
        bundle.putBoolean("isAdmin", false);
        bundle.putBoolean("isOrganizer", true);
        bundle.putBoolean("isEntrant", false);
        bundle.putBoolean("isDeleted", false);
        bundle.putString("role", "Organizer");
        return bundle;
    }

    private FragmentScenario<AdminProfileDetails> launchFragment(Bundle args) {
        FragmentScenario<AdminProfileDetails> scenario = FragmentScenario.launchInContainer(
                AdminProfileDetails.class,
                args,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
        );

        return scenario;
    }

    @Test
    public void testActiveProfileDisplaysDetails() {
        FragmentScenario<AdminProfileDetails> scenario = launchFragment(createActiveBundle());

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();

            TextView name = root.findViewById(R.id.tv_detail_name);
            TextView email = root.findViewById(R.id.tv_detail_email);
            TextView phone = root.findViewById(R.id.tv_detail_phone);
            TextView deviceId = root.findViewById(R.id.tv_detail_device_id);
            TextView avatar = root.findViewById(R.id.tv_avatar_initial);
            TextView status = root.findViewById(R.id.tv_detail_status);
            TextView role = root.findViewById(R.id.tv_detail_role);

            MaterialButton toggleAdmin = root.findViewById(R.id.btn_toggle_admin);
            MaterialButton toggleOrganizer = root.findViewById(R.id.btn_toggle_organizer);
            MaterialButton toggleEntrant = root.findViewById(R.id.btn_toggle_entrant);
            View deleteButton = root.findViewById(R.id.btn_delete_user);

            assertEquals("Jane Doe", name.getText().toString());
            assertEquals("jane@example.com", email.getText().toString());
            assertEquals("7805551234", phone.getText().toString());
            assertEquals("device-123", deviceId.getText().toString());
            assertEquals("J", avatar.getText().toString());
            assertEquals("● Active", status.getText().toString());
            assertEquals("Role: Admin Organizer", role.getText().toString());

            assertEquals("Remove Role", toggleAdmin.getText().toString());
            assertEquals("Remove Role", toggleOrganizer.getText().toString());
            assertEquals("Add Role", toggleEntrant.getText().toString());

            assertTrue(toggleAdmin.isEnabled());
            assertTrue(toggleOrganizer.isEnabled());
            assertTrue(toggleEntrant.isEnabled());
            assertEquals(View.VISIBLE, deleteButton.getVisibility());
        });
    }

    @Test
    public void testDeletedProfileHidesManagement() {
        FragmentScenario<AdminProfileDetails> scenario = launchFragment(createDeletedBundle());

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();

            TextView status = root.findViewById(R.id.tv_detail_status);
            TextView role = root.findViewById(R.id.tv_detail_role);
            View roleManagementCard = root.findViewById(R.id.roleManagementCard);
            View deleteButton = root.findViewById(R.id.btn_delete_user);

            assertEquals("● Inactive", status.getText().toString());
            assertEquals("Role: Entrant", role.getText().toString());
            assertEquals(View.GONE, roleManagementCard.getVisibility());
            assertEquals(View.GONE, deleteButton.getVisibility());
        });
    }

    @Test
    public void testSingleRoleDisablesLastRoleRemoval() {
        FragmentScenario<AdminProfileDetails> scenario = launchFragment(createSingleRoleBundle());

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();

            TextView role = root.findViewById(R.id.tv_detail_role);
            MaterialButton toggleAdmin = root.findViewById(R.id.btn_toggle_admin);
            MaterialButton toggleOrganizer = root.findViewById(R.id.btn_toggle_organizer);
            MaterialButton toggleEntrant = root.findViewById(R.id.btn_toggle_entrant);

            assertEquals("Role: Organizer", role.getText().toString());

            assertEquals("Add Role", toggleAdmin.getText().toString());
            assertEquals("Remove Role", toggleOrganizer.getText().toString());
            assertEquals("Add Role", toggleEntrant.getText().toString());

            assertTrue(toggleAdmin.isEnabled());
            assertFalse(toggleOrganizer.isEnabled());
            assertTrue(toggleEntrant.isEnabled());
        });
    }

    @Test
    public void testDoneButtonNavigatesBack() {
        FragmentScenario<AdminProfileDetails> scenario = launchFragment(createActiveBundle());

        scenario.onFragment(fragment -> {
            View doneButton = fragment.requireView().findViewById(R.id.btn_done);
            doneButton.performClick();
        });

        verify(mockNavController).popBackStack();
    }
}