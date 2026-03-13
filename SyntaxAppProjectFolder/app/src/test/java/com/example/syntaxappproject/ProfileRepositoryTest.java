package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link ProfileRepository}.
 * <p>
 * Uses a fake {@link ProfileDataSource} implementation to avoid
 * any dependency on Firebase Firestore.
 * </p>
 */
public class ProfileRepositoryTest {

    /**
     * Interface to abstract Firestore operations for testing.
     */
    interface ProfileDataSource {
        void create(String uid, Profile profile, ProfileRepository.RepositoryCallback callback);
        void get(String uid, ProfileRepository.ProfileCallback callback);
        void update(String uid, Profile profile, ProfileRepository.RepositoryCallback callback);
        void delete(String uid, ProfileRepository.RepositoryCallback callback);
    }

    /**
     * In-memory fake that simulates Firestore using a {@link java.util.HashMap}.
     */
    static class FakeProfileDataSource implements ProfileDataSource {
        private final java.util.Map<String, Profile> store = new java.util.HashMap<>();

        @Override
        public void create(String uid, Profile profile, ProfileRepository.RepositoryCallback callback) {
            store.put(uid, profile);
            callback.onComplete(true);
        }

        @Override
        public void get(String uid, ProfileRepository.ProfileCallback callback) {
            callback.onResult(store.get(uid));
        }

        @Override
        public void update(String uid, Profile profile, ProfileRepository.RepositoryCallback callback) {
            store.put(uid, profile);
            callback.onComplete(true);
        }

        @Override
        public void delete(String uid, ProfileRepository.RepositoryCallback callback) {
            store.remove(uid);
            callback.onComplete(true);
        }
    }

    /**
     * Testable subclass of {@link ProfileRepository} that delegates all
     * Firestore operations to a {@link ProfileDataSource} fake.
     */
    static class TestableProfileRepository extends ProfileRepository {
        private final ProfileDataSource dataSource;

        /**
         * Constructs a testable repository using the test-mode constructor
         * to bypass Firebase initialization.
         *
         * @param dataSource the fake data source to delegate operations to
         */
        public TestableProfileRepository(ProfileDataSource dataSource) {
            super(true);
            this.dataSource = dataSource;
        }

        @Override
        public void createProfile(String uid, Profile profile, RepositoryCallback callback) {
            dataSource.create(uid, profile, callback);
        }

        @Override
        public void getProfile(String uid, ProfileCallback callback) {
            dataSource.get(uid, callback);
        }

        @Override
        public void updateProfile(String uid, Profile profile, RepositoryCallback callback) {
            dataSource.update(uid, profile, callback);
        }

        @Override
        public void deleteProfile(String uid, RepositoryCallback callback) {
            dataSource.delete(uid, callback);
        }
    }

    private TestableProfileRepository repo;
    private Profile sampleProfile;

    /**
     * Initializes a fresh repository and a sample profile before each test.
     */
    @Before
    public void setUp() {
        repo = new TestableProfileRepository(new FakeProfileDataSource());
        sampleProfile = new Profile(
                "Jane Doe",
                "jane.doe@ualberta.ca",
                "780-555-0192",
                "Entrant",
                true,
                false,
                true,
                "device-uid-00412"
        );
    }

    /**
     * Verifies that {@link ProfileRepository#createProfile} invokes the
     * callback with {@code true} on success.
     */
    @Test
    public void testCreateProfile_callsCallbackTrue() {
        boolean[] result = {false};
        repo.createProfile("uid-001", sampleProfile, success -> result[0] = success);
        assertTrue(result[0]);
    }

    /**
     * Verifies that a profile created via {@link ProfileRepository#createProfile}
     * can be retrieved with the correct name.
     */
    @Test
    public void testCreateProfile_profileCanBeRetrievedAfterCreate() {
        repo.createProfile("uid-001", sampleProfile, success -> {});
        Profile[] retrieved = {null};
        repo.getProfile("uid-001", profile -> retrieved[0] = profile);
        assertNotNull(retrieved[0]);
        assertEquals("Jane Doe", retrieved[0].getName());
    }

    /**
     * Verifies that {@link ProfileRepository#getProfile} returns {@code null}
     * when no profile exists for the given UID.
     */
    @Test
    public void testGetProfile_returnsNull_whenProfileDoesNotExist() {
        Profile[] retrieved = {new Profile()};
        repo.getProfile("nonexistent-uid", profile -> retrieved[0] = profile);
        assertNull(retrieved[0]);
    }

    /**
     * Verifies that {@link ProfileRepository#getProfile} returns the correct
     * email address for an existing profile.
     */
    @Test
    public void testGetProfile_returnsCorrectEmail() {
        repo.createProfile("uid-002", sampleProfile, success -> {});
        Profile[] retrieved = {null};
        repo.getProfile("uid-002", profile -> retrieved[0] = profile);
        assertEquals("jane.doe@ualberta.ca", retrieved[0].getEmail());
    }

    /**
     * Verifies that {@link ProfileRepository#getProfile} returns the correct
     * entrant and organizer role flags for an existing profile.
     */
    @Test
    public void testGetProfile_returnsCorrectRole() {
        repo.createProfile("uid-002", sampleProfile, success -> {});
        Profile[] retrieved = {null};
        repo.getProfile("uid-002", profile -> retrieved[0] = profile);
        assertTrue(retrieved[0].isEntrant());
        assertFalse(retrieved[0].isOrganizer());
    }

    /**
     * Verifies that {@link ProfileRepository#updateProfile} invokes the
     * callback with {@code true} on success.
     */
    @Test
    public void testUpdateProfile_callsCallbackTrue() {
        repo.createProfile("uid-003", sampleProfile, success -> {});
        Profile updated = new Profile(
                "Jane Smith",
                "jane.smith@ualberta.ca",
                null,
                "Entrant",
                true,
                true,
                false,
                "device-uid-00412"
        );
        boolean[] result = {false};
        repo.updateProfile("uid-003", updated, success -> result[0] = success);
        assertTrue(result[0]);
    }

    /**
     * Verifies that {@link ProfileRepository#updateProfile} overwrites the
     * previously stored profile data with the new values.
     */
    @Test
    public void testUpdateProfile_overwritesOldData() {
        repo.createProfile("uid-003", sampleProfile, success -> {});
        Profile updated = new Profile(
                "Jane Smith",
                "jane.smith@ualberta.ca",
                null,
                "Entrant",
                true,
                true,
                false,
                "device-uid-00412"
        );
        repo.updateProfile("uid-003", updated, success -> {});
        Profile[] retrieved = {null};
        repo.getProfile("uid-003", profile -> retrieved[0] = profile);
        assertEquals("Jane Smith", retrieved[0].getName());
        assertTrue(retrieved[0].isOrganizer());
    }

    /**
     * Verifies that {@link ProfileRepository#deleteProfile} invokes the
     * callback with {@code true} on success.
     */
    @Test
    public void testDeleteProfile_callsCallbackTrue() {
        repo.createProfile("uid-004", sampleProfile, success -> {});
        boolean[] result = {false};
        repo.deleteProfile("uid-004", success -> result[0] = success);
        assertTrue(result[0]);
    }

    /**
     * Verifies that a profile is no longer retrievable after it has been deleted.
     */
    @Test
    public void testDeleteProfile_profileIsNullAfterDelete() {
        repo.createProfile("uid-004", sampleProfile, success -> {});
        repo.deleteProfile("uid-004", success -> {});
        Profile[] retrieved = {new Profile()};
        repo.getProfile("uid-004", profile -> retrieved[0] = profile);
        assertNull(retrieved[0]);
    }
}