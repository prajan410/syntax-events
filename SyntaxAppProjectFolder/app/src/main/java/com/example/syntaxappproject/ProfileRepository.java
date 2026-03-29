package com.example.syntaxappproject;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository class responsible for managing {@link Profile} data
 * in Firebase Firestore for the SyntaxEvents application.
 * <p>
 * Acts as the data access layer for profile creation, retrieval,
 * updating, and deletion. All operations are asynchronous and return
 * results via callback interfaces.
 * </p>
 *
 * <p>Outstanding issues: no error handling for network failures beyond
 * the success/failure boolean passed to callbacks.</p>
 */
public class ProfileRepository {

    private FirebaseFirestore db = null;

    /**
     * Constructs a ProfileRepository and initializes the Firestore instance.
     */
    public ProfileRepository() {
        db = FirebaseFirestore.getInstance();
    }

    protected ProfileRepository(boolean testMode) {
    }

    /**
     * Creates a new profile document in Firestore for the given UID.
     *
     * @param uid      the unique user ID to use as the document key
     * @param profile  the {@link Profile} object to store
     * @param callback the {@link RepositoryCallback} invoked with the result
     */
    public void createProfile(String uid, Profile profile, RepositoryCallback callback) {
        db.collection("profiles")
                .document(uid)
                .set(profile)
                .addOnCompleteListener(task ->
                        callback.onComplete(task.isSuccessful()));
    }

    /**
     * Retrieves the profile associated with the given UID from Firestore.
     *
     * @param uid      the unique user ID of the profile to retrieve
     * @param callback the {@link ProfileCallback} invoked with the result,
     *                 or {@code null} if no profile exists for the given UID
     */
    public void getProfile(String uid, ProfileCallback callback) {
        db.collection("profiles")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Profile profile = documentSnapshot.toObject(Profile.class);
                        callback.onResult(profile);
                    } else {
                        callback.onResult(null);
                    }
                });
    }

    /**
     * Updates an existing profile document in Firestore for the given UID.
     *
     * @param uid      the unique user ID of the profile to update
     * @param profile  the updated {@link Profile} object
     * @param callback the {@link RepositoryCallback} invoked with the result
     */
    public void updateProfile(String uid, Profile profile, RepositoryCallback callback) {
        db.collection("profiles")
                .document(uid)
                .set(profile)
                .addOnCompleteListener(task ->
                        callback.onComplete(task.isSuccessful()));
    }

    /**
     * Deletes the profile document associated with the given UID from Firestore.
     *
     * @param uid      the unique user ID of the profile to delete
     * @param callback the {@link RepositoryCallback} invoked with the result
     */
    public void deleteProfile(String uid, RepositoryCallback callback) {
        db.collection("profiles")
                .document(uid)
                .delete()
                .addOnCompleteListener(task ->
                        callback.onComplete(task.isSuccessful()));
    }

    /**
     * Callback interface for operations that return a success or failure result.
     */
    public interface RepositoryCallback {
        /**
         * Called when the repository operation completes.
         *
         * @param success {@code true} if the operation succeeded
         */
        void onComplete(boolean success);
    }

    /**
     * Callback interface for operations that return a {@link Profile}.
     */
    public interface ProfileCallback {
        /**
         * Called when the profile retrieval completes.
         *
         * @param profile the retrieved {@link Profile}, or {@code null} if not found
         */
        void onResult(Profile profile);
    }
}
