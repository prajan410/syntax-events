package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Profile} model class.
 * <p>
 * Verifies that personal information, role flags, device identifier,
 * and notification preferences are correctly stored and retrieved.
 * </p>
 */
public class ProfileTest {

    private Profile entrantProfile;
    private Profile organizerProfile;
    private Profile dualRoleProfile;

    /**
     * Initializes three sample profiles before each test:
     * an entrant-only profile, an organizer-only profile,
     * and a profile with both roles assigned.
     */
    @Before
    public void setUp() {
        entrantProfile = new Profile(
                "Jane Doe",
                "jane.doe@ualberta.ca",
                "780-555-0192",
                true,
                false,
                false,
                true,
                "device-uid-00412"
        );

        organizerProfile = new Profile(
                "Marcus Lee",
                "marcus.lee@ualberta.ca",
                null,
                false,
                true,
                false,
                true,
                "device-uid-00881"
        );

        dualRoleProfile = new Profile(
                "Alex Ray",
                "alex.ray@ualberta.ca",
                "587-555-0344",
                true,
                true,
                false,
                false,
                "device-uid-00777"
        );

    }

    /**
     * Verifies that the no-arg constructor initializes {@code name} to {@code null}.
     */
    @Test
    public void testNoArgConstructor_nameIsNull() {
        assertNull(new Profile().getName());
    }

    /**
     * Verifies that the no-arg constructor initializes {@code email} to {@code null}.
     */
    @Test
    public void testNoArgConstructor_emailIsNull() {
        assertNull(new Profile().getEmail());
    }

    /**
     * Verifies that the no-arg constructor initializes {@code phone} to {@code null}.
     */
    @Test
    public void testNoArgConstructor_phoneIsNull() {
        assertNull(new Profile().getPhone());
    }

    /**
     * Verifies that the no-arg constructor initializes {@code deviceId} to {@code null}.
     */
    @Test
    public void testNoArgConstructor_deviceIdIsNull() {
        assertNull(new Profile().getDeviceId());
    }

    /**
     * Verifies that the no-arg constructor sets {@code isEntrant} to {@code false}.
     */
    @Test
    public void testNoArgConstructor_isEntrantFalse() {
        assertFalse(new Profile().isEntrant());
    }

    /**
     * Verifies that the no-arg constructor sets {@code isOrganizer} to {@code false}.
     */
    @Test
    public void testNoArgConstructor_isOrganizerFalse() {
        assertFalse(new Profile().isOrganizer());
    }

    /**
     * Verifies that the no-arg constructor sets {@code notificationsEnabled} to {@code false}.
     */
    @Test
    public void testNoArgConstructor_notificationsDisabled() {
        assertFalse(new Profile().isNotificationsEnabled());
    }

    /**
     * Verifies that {@link Profile#getName()} returns the correct name.
     */
    @Test
    public void testGetName_returnsCorrectName() {
        assertEquals("Jane Doe", entrantProfile.getName());
    }

    /**
     * Verifies that {@link Profile#getEmail()} returns the correct email address.
     */
    @Test
    public void testGetEmail_returnsCorrectEmail() {
        assertEquals("jane.doe@ualberta.ca", entrantProfile.getEmail());
    }

    /**
     * Verifies that {@link Profile#getPhone()} returns the correct phone number.
     */
    @Test
    public void testGetPhone_returnsCorrectPhone() {
        assertEquals("780-555-0192", entrantProfile.getPhone());
    }

    /**
     * Verifies that {@link Profile#getPhone()} returns {@code null}
     * when no phone number was provided.
     */
    @Test
    public void testGetPhone_nullWhenNotProvided() {
        assertNull(organizerProfile.getPhone());
    }

    /**
     * Verifies that {@link Profile#getDeviceId()} returns the correct device ID.
     */
    @Test
    public void testGetDeviceId_returnsCorrectId() {
        assertEquals("device-uid-00412", entrantProfile.getDeviceId());
    }

    /**
     * Verifies that {@link Profile#isNotificationsEnabled()} returns {@code true}
     * when notifications are enabled.
     */
    @Test
    public void testIsNotificationsEnabled_trueWhenSet() {
        assertTrue(entrantProfile.isNotificationsEnabled());
    }

    /**
     * Verifies that {@link Profile#isNotificationsEnabled()} returns {@code false}
     * when notifications are disabled.
     */
    @Test
    public void testIsNotificationsEnabled_falseWhenDisabled() {
        assertFalse(dualRoleProfile.isNotificationsEnabled());
    }

    /**
     * Verifies that {@link Profile#isEntrant()} returns {@code true}
     * for an entrant-only profile.
     */
    @Test
    public void testIsEntrant_trueForEntrant() {
        assertTrue(entrantProfile.isEntrant());
    }

    /**
     * Verifies that {@link Profile#isEntrant()} returns {@code false}
     * for an organizer-only profile.
     */
    @Test
    public void testIsEntrant_falseForOrganizer() {
        assertFalse(organizerProfile.isEntrant());
    }

    /**
     * Verifies that {@link Profile#isOrganizer()} returns {@code true}
     * for an organizer-only profile.
     */
    @Test
    public void testIsOrganizer_trueForOrganizer() {
        assertTrue(organizerProfile.isOrganizer());
    }

    /**
     * Verifies that {@link Profile#isOrganizer()} returns {@code false}
     * for an entrant-only profile.
     */
    @Test
    public void testIsOrganizer_falseForEntrant() {
        assertFalse(entrantProfile.isOrganizer());
    }

    /**
     * Verifies that a profile with both roles assigned has both
     * {@code isEntrant} and {@code isOrganizer} set to {@code true}.
     */
    @Test
    public void testDualRole_bothFlagsTrue() {
        assertTrue(dualRoleProfile.isEntrant());
        assertTrue(dualRoleProfile.isOrganizer());
    }

    /**
     * Verifies that {@link Profile#setEntrant(boolean)} correctly sets
     * the entrant flag to {@code false}.
     */
    @Test
    public void testSetEntrant_toFalse() {
        entrantProfile.setEntrant(false);
        assertFalse(entrantProfile.isEntrant());
    }

    /**
     * Verifies that {@link Profile#setEntrant(boolean)} correctly sets
     * the entrant flag to {@code true}.
     */
    @Test
    public void testSetEntrant_toTrue() {
        organizerProfile.setEntrant(true);
        assertTrue(organizerProfile.isEntrant());
    }

    /**
     * Verifies that {@link Profile#setOrganizer(boolean)} correctly sets
     * the organizer flag to {@code true}.
     */
    @Test
    public void testSetOrganizer_toTrue() {
        entrantProfile.setOrganizer(true);
        assertTrue(entrantProfile.isOrganizer());
    }

    /**
     * Verifies that {@link Profile#setOrganizer(boolean)} correctly sets
     * the organizer flag to {@code false}.
     */
    @Test
    public void testSetOrganizer_toFalse() {
        organizerProfile.setOrganizer(false);
        assertFalse(organizerProfile.isOrganizer());
    }
}
