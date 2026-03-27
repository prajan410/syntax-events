package com.example.syntaxappproject;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Service class responsible for managing Firebase anonymous authentication
 * in the SyntaxEvents application.
 * <p>
 * Acts as a control class that wraps {@link FirebaseAuth} to provide
 * sign-in, sign-out, and user identification functionality. All authentication
 * results are returned asynchronously via the {@link AuthCallback} interface.
 * </p>
 *
 * <p>Outstanding issues: only anonymous authentication is supported;
 * email/password or OAuth providers are not yet implemented.</p>
 */
public class AuthenticationService {

    private FirebaseAuth auth;

    /**
     * Constructs an AuthenticationService and initializes the FirebaseAuth instance.
     */
    public AuthenticationService() {
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Signs in the current user anonymously via Firebase.
     * <p>
     * If a user is already signed in, the callback is immediately invoked
     * with {@code true}. Otherwise, a new anonymous sign-in is attempted.
     * </p>
     *
     * @param callback the {@link AuthCallback} to invoke with the result
     */
    public void signInAnonymously(AuthCallback callback) {
        if (auth.getCurrentUser() != null) {
            callback.onComplete(true);
            return;
        }
        auth.signInAnonymously()
                .addOnCompleteListener(task ->
                        callback.onComplete(task.isSuccessful()));
    }

    /**
     * Returns the unique user ID of the currently signed-in user.
     *
     * @return the UID string, or {@code null} if no user is signed in
     */
    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Signs out the currently authenticated user.
     */
    public void signOut() {
        auth.signOut();
    }

    /**
     * Callback interface for receiving the result of an authentication operation.
     */
    public interface AuthCallback {
        /**
         * Called when the authentication operation completes.
         *
         * @param success {@code true} if authentication succeeded, {@code false} otherwise
         */
        void onComplete(boolean success);
    }
}
