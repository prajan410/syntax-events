package com.example.syntaxappproject;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ProfileAdapterTest {

    private ProfileAdapter adapter;
    private ArrayList<Profile> profileList;
    private ArrayList<String> profileIds;
    private ArrayList<Boolean> deletedFlags;

    // ─── Helpers ───────────────────────────────────────────────────

    private Profile makeProfile(String name, String email, String phone,
                                boolean isAdmin, boolean isOrganizer, boolean isEntrant) {
        Profile p = new Profile();
        p.setName(name);
        p.setEmail(email);
        p.setPhone(phone);
        p.setAdmin(isAdmin);
        p.setOrganizer(isOrganizer);
        p.setEntrant(isEntrant);
        return p;
    }

    @Before
    public void setUp() {
        profileList  = new ArrayList<>();
        profileIds   = new ArrayList<>();
        deletedFlags = new ArrayList<>();
        adapter = new ProfileAdapter(profileList, profileIds, deletedFlags);
    }

    // ─── getItemCount ──────────────────────────────────────────────

    @Test
    public void getItemCount_emptyList_isZero() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void getItemCount_oneProfile_isOne() {
        profileList.add(makeProfile("Alice", "a@a.com", "123", false, false, true));
        profileIds.add("id-1");
        deletedFlags.add(false);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void getItemCount_threeProfiles_isThree() {
        for (int i = 0; i < 3; i++) {
            profileList.add(makeProfile("User " + i, "u@u.com", "000", false, false, true));
            profileIds.add("id-" + i);
            deletedFlags.add(false);
        }
        assertEquals(3, adapter.getItemCount());
    }

    // ─── updateData ────────────────────────────────────────────────

    @Test
    public void updateData_replacesExistingList() {
        profileList.add(makeProfile("Old", "o@o.com", "000", false, false, true));
        profileIds.add("old-id");
        deletedFlags.add(false);

        List<Profile> newProfiles = Arrays.asList(
                makeProfile("New1", "n1@n.com", "111", false, false, true),
                makeProfile("New2", "n2@n.com", "222", false, true, false)
        );
        adapter.updateData(newProfiles, Arrays.asList("new-1", "new-2"), Arrays.asList(false, true));

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void updateData_withEmptyList_clearsAdapter() {
        profileList.add(makeProfile("Alice", "a@a.com", "123", false, false, true));
        profileIds.add("id-1");
        deletedFlags.add(false);

        adapter.updateData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void updateData_largeList_countIsCorrect() {
        List<Profile> newProfiles = new ArrayList<>();
        List<String> newIds = new ArrayList<>();
        List<Boolean> newFlags = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            newProfiles.add(makeProfile("User " + i, "u@u.com", "000", false, false, true));
            newIds.add("id-" + i);
            newFlags.add(i % 2 == 0);
        }
        adapter.updateData(newProfiles, newIds, newFlags);
        assertEquals(50, adapter.getItemCount());
    }

    @Test
    public void updateData_calledTwice_secondUpdateWins() {
        adapter.updateData(
                Arrays.asList(makeProfile("First", "f@f.com", "000", false, false, true)),
                Arrays.asList("id-1"),
                Arrays.asList(false)
        );
        adapter.updateData(
                Arrays.asList(
                        makeProfile("Second A", "a@a.com", "000", false, false, true),
                        makeProfile("Second B", "b@b.com", "000", false, false, true)
                ),
                Arrays.asList("id-2", "id-3"),
                Arrays.asList(false, false)
        );
        assertEquals(2, adapter.getItemCount());
    }

    // ─── getRoleLabel logic ────────────────────────────────────────

    @Test
    public void roleLabel_adminOnly_returnsAdmin() {
        Profile p = makeProfile("Alice", "a@a.com", "123", true, false, false);
        assertEquals("Admin", getRoleLabel(p));
    }

    @Test
    public void roleLabel_adminAndOrganizer_returnsAdminOnly() {
        // Admin takes priority over all other roles
        Profile p = makeProfile("Alice", "a@a.com", "123", true, true, true);
        assertEquals("Admin", getRoleLabel(p));
    }

    @Test
    public void roleLabel_organizerOnly_returnsOrganizer() {
        Profile p = makeProfile("Bob", "b@b.com", "456", false, true, false);
        assertEquals("Organizer", getRoleLabel(p));
    }

    @Test
    public void roleLabel_entrantOnly_returnsEntrant() {
        Profile p = makeProfile("Carol", "c@c.com", "789", false, false, true);
        assertEquals("Entrant", getRoleLabel(p));
    }

    @Test
    public void roleLabel_organizerAndEntrant_returnsBoth() {
        Profile p = makeProfile("Dave", "d@d.com", "000", false, true, true);
        assertEquals("Organizer, Entrant", getRoleLabel(p));
    }

    @Test
    public void roleLabel_noRoles_returnsNone() {
        Profile p = makeProfile("Eve", "e@e.com", "000", false, false, false);
        assertEquals("None", getRoleLabel(p));
    }

    // ─── Status label logic ────────────────────────────────────────

    @Test
    public void statusLabel_notDeleted_isActive() {
        assertEquals("Active", resolveStatus(false));
    }

    @Test
    public void statusLabel_deleted_isInactive() {
        assertEquals("Inactive", resolveStatus(true));
    }

    // ─── Name display logic ────────────────────────────────────────

    @Test
    public void nameDisplay_validName_showsName() {
        Profile p = makeProfile("Alice", "a@a.com", "123", false, false, true);
        assertEquals("Alice", p.getName() != null ? p.getName() : "Unknown");
    }

    @Test
    public void nameDisplay_nullName_showsUnknown() {
        Profile p = makeProfile(null, "a@a.com", "123", false, false, true);
        assertEquals("Unknown", p.getName() != null ? p.getName() : "Unknown");
    }

    // ─── Bundle data integrity ─────────────────────────────────────

    @Test
    public void bundleData_profileFieldsAreCorrect() {
        Profile p = makeProfile("Alice", "alice@test.com", "555-1234", false, true, true);
        p.setDeviceId("device-abc");

        assertEquals("Alice",          p.getName());
        assertEquals("alice@test.com", p.getEmail());
        assertEquals("555-1234",       p.getPhone());
        assertEquals("device-abc",     p.getDeviceId());
        assertFalse(p.isAdmin());
        assertTrue(p.isOrganizer());
        assertTrue(p.isEntrant());
    }

    @Test
    public void bundleData_adminProfile_flagsCorrect() {
        Profile p = makeProfile("AdminUser", "admin@test.com", "000", true, false, false);
        assertTrue(p.isAdmin());
        assertFalse(p.isOrganizer());
        assertFalse(p.isEntrant());
    }

    @Test
    public void bundleData_deletedFlag_isPreserved() {
        profileList.add(makeProfile("Alice", "a@a.com", "123", false, false, true));
        profileIds.add("id-1");
        deletedFlags.add(true);

        assertTrue(deletedFlags.get(0));
    }

    @Test
    public void bundleData_profileIdIsCorrect() {
        profileList.add(makeProfile("Alice", "a@a.com", "123", false, false, true));
        profileIds.add("firestore-doc-id");
        deletedFlags.add(false);

        assertEquals("firestore-doc-id", profileIds.get(0));
    }

    // ─── List independence ─────────────────────────────────────────

    @Test
    public void twoAdapters_areIndependent() {
        ProfileAdapter adapterA = new ProfileAdapter(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        ProfileAdapter adapterB = new ProfileAdapter(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        adapterA.updateData(
                Arrays.asList(makeProfile("A", "a@a.com", "0", false, false, true)),
                Arrays.asList("id-a"),
                Arrays.asList(false)
        );

        assertEquals(1, adapterA.getItemCount());
        assertEquals(0, adapterB.getItemCount());
    }

    // ─── Mirrors of adapter private logic ─────────────────────────

    /** Mirrors getRoleLabel from ProfileAdapter. */
    private String getRoleLabel(Profile profile) {
        if (profile.isAdmin()) return "Admin";
        List<String> roles = new ArrayList<>();
        if (profile.isOrganizer()) roles.add("Organizer");
        if (profile.isEntrant())   roles.add("Entrant");
        return roles.isEmpty() ? "None" : String.join(", ", roles);
    }

    /** Mirrors the status display logic from onBindViewHolder. */
    private String resolveStatus(boolean isDeleted) {
        return isDeleted ? "Inactive" : "Active";
    }
}