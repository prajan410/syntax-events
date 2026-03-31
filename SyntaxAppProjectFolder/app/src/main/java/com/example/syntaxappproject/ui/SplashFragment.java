package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EntrantHomeRepository;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.ImageItem;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment displayed on app launch that handles anonymous sign-in,
 * event image pre-caching, and navigation routing based on the user's profile.
 *
 * <p>When the user taps the enter button, the fragment signs in anonymously
 * via {@link AuthenticationService}, fetches all available events, and
 * pre-caches their poster images using {@link ImageCacheManager} while
 * displaying a progress bar. Once caching is complete, the user is routed
 * to the appropriate destination based on their role (entrant, organizer,
 * admin, or new user).</p>
 *
 * <p>{@link #authService} and {@link #profileRepo} are package-private to
 * allow injection via {@link #setAuthService} and {@link #setProfileRepo}
 * in instrumented tests.</p>
 */
public class SplashFragment extends Fragment {

    /**
     * Service used to sign in anonymously and retrieve the current user's UID.
     * Can be injected for testing via {@link #setAuthService(AuthenticationService)}.
     */
    AuthenticationService authService;

    /**
     * Repository used to fetch the current user's profile for role-based routing.
     * Can be injected for testing via {@link #setProfileRepo(ProfileRepository)}.
     */
    ProfileRepository profileRepo;

    /** The fill view inside the custom progress bar that grows to represent loading progress. */
    private View progressFill;

    /** The container view for the progress bar, shown while caching is in progress. */
    private View progressContainer;

    /**
     * Required empty public constructor. Sets the default layout to
     * {@code R.layout.fragment_splash}.
     */
    public SplashFragment() {
        super(R.layout.fragment_splash);
    }

    /**
     * Inflates the splash screen layout.
     *
     * @param inflater           used to inflate the fragment layout
     * @param container          parent view group the fragment UI attaches to
     * @param savedInstanceState previously saved instance state, if any
     * @return the inflated root view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    /**
     * Injects a custom {@link AuthenticationService} instance.
     * Used in instrumented tests to avoid real Firebase Auth calls.
     *
     * @param authService the mock or custom auth service to use
     */
    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Injects a custom {@link ProfileRepository} instance.
     * Used in instrumented tests to avoid real Firestore calls.
     *
     * @param profileRepo the mock or custom profile repository to use
     */
    public void setProfileRepo(ProfileRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    /**
     * Initializes UI views and entrance animations, then wires up the enter button.
     * The title card uses an overshoot scale animation, while the title text,
     * tagline, and button slide in with staggered delays.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View titleCard = view.findViewById(R.id.titleCard);
        View titleText = view.findViewById(R.id.titleText);
        View taglineText = view.findViewById(R.id.taglineText);
        MaterialButton enterButton = view.findViewById(R.id.enterButton);

        titleCard.setScaleX(0.3f);
        titleCard.setScaleY(0.3f);
        titleCard.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(600)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                .start();

        titleText.setTranslationX(-30f);
        titleText.animate().alpha(1f).translationX(0f)
                .setDuration(600).setStartDelay(200).start();

        taglineText.setTranslationX(-30f);
        taglineText.animate().alpha(1f).translationX(0f)
                .setDuration(500).setStartDelay(600).start();

        enterButton.setTranslationY(20f);
        enterButton.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(700).start();

        enterButton.setOnClickListener(v -> handleEnterButton());
    }

    /**
     * Handles the enter button tap. Signs in the user anonymously, then fetches
     * all events and pre-caches their poster images while updating the progress bar.
     *
     * <p>The enter button is hidden and replaced with an animated progress bar during
     * loading. If sign-in fails, the button is restored. Once all images are loaded
     * (or skipped if already cached), {@link #navigateByProfile(String)} is called.</p>
     *
     * <p>Image decoding is performed on a background thread to avoid blocking the UI.
     * Progress is tracked with an {@link AtomicInteger} to safely handle concurrent
     * callbacks from multiple image fetches.</p>
     */
    protected void handleEnterButton() {
        if (authService == null) authService = new AuthenticationService();

        MaterialButton enterButton = requireView().findViewById(R.id.enterButton);
        progressContainer = requireView().findViewById(R.id.cacheProgressBar);
        progressFill = requireView().findViewById(R.id.progressFill);
        TextView loadingText = requireView().findViewById(R.id.loadingText);

        enterButton.setEnabled(false);
        progressContainer.setAlpha(0f);
        progressContainer.setVisibility(View.VISIBLE);

        enterButton.animate().alpha(0f).setDuration(250).start();
        progressContainer.animate().alpha(1f).setDuration(250).start();
        loadingText.animate().alpha(1f).setDuration(300).setStartDelay(100).start();

        authService.signInAnonymously(success -> {
            if (!success) {
                requireActivity().runOnUiThread(() -> {
                    progressContainer.setVisibility(View.INVISIBLE);
                    enterButton.setEnabled(true);
                    enterButton.animate().alpha(1f).setDuration(200).start();
                    loadingText.animate().alpha(0f).setDuration(200).start();
                });
                return;
            }

            String uid = authService.getCurrentUserId();
            if (profileRepo == null) profileRepo = new ProfileRepository();

            new EntrantHomeRepository().getEvents(events -> {
                if (events == null || events.isEmpty()) {
                    navigateByProfile(uid);
                    return;
                }

                int total = events.size();
                AtomicInteger loaded = new AtomicInteger(0);

                for (EventDetail event : events) {
                    String eventId = event.getEventId();

                    if (ImageCacheManager.has(eventId)) {
                        int current = loaded.incrementAndGet();
                        int progress = (int) ((current / (float) total) * 100);
                        requireActivity().runOnUiThread(() -> {
                            setProgress(progress);
                            updateLoadingText(loadingText, progress);
                        });
                        if (current == total) navigateByProfile(uid);
                        continue;
                    }

                    ImageItem.fetchByEventId(eventId, new ImageItem.ImageCallback() {
                        @Override
                        public void onImageLoaded(ImageItem imageItem) {
                            new Thread(() -> {
                                try {
                                    if (imageItem != null && imageItem.imageUrl != null) {
                                        byte[] decoded = android.util.Base64.decode(imageItem.imageUrl, android.util.Base64.DEFAULT);
                                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                        if (bitmap != null) ImageCacheManager.put(eventId, bitmap);
                                    }
                                } catch (Exception ignored) {}

                                int current = loaded.incrementAndGet();
                                int progress = (int) ((current / (float) total) * 100);
                                requireActivity().runOnUiThread(() -> {
                                    setProgress(progress);
                                    updateLoadingText(loadingText, progress);
                                });
                                if (current == total) navigateByProfile(uid);
                            }).start();
                        }

                        @Override
                        public void onError(Exception e) {
                            int current = loaded.incrementAndGet();
                            int progress = (int) ((current / (float) total) * 100);
                            requireActivity().runOnUiThread(() -> {
                                setProgress(progress);
                                updateLoadingText(loadingText, progress);
                            });
                            if (current == total) navigateByProfile(uid);
                        }
                    });
                }
            });
        });
    }

    /**
     * Updates the width of {@link #progressFill} to visually represent the given
     * progress percentage within {@link #progressContainer}.
     *
     * <p>Width calculation is posted to the view's message queue via
     * {@link View#post(Runnable)} to ensure {@link #progressContainer} has been
     * laid out and its width is available.</p>
     *
     * @param progress integer percentage from 0 to 100
     */
    private void setProgress(int progress) {
        if (progressFill == null || progressContainer == null) return;
        progressContainer.post(() -> {
            int totalWidth = progressContainer.getWidth();
            int fillWidth = (int) ((progress / 100f) * totalWidth);
            progressFill.getLayoutParams().width = fillWidth;
            progressFill.requestLayout();
        });
    }

    /**
     * Updates the loading status text based on the current progress percentage.
     *
     * @param loadingText the {@link TextView} to update
     * @param progress    integer percentage from 0 to 100
     */
    private void updateLoadingText(TextView loadingText, int progress) {
        if (progress < 30) {
            loadingText.setText("Getting things ready...");
        } else if (progress < 60) {
            loadingText.setText("Loading events...");
        } else if (progress < 90) {
            loadingText.setText("Almost there...");
        } else {
            loadingText.setText("Done!");
        }
    }

    /**
     * Fetches the user's profile and navigates to the appropriate destination
     * based on their assigned role.
     *
     * <ul>
     *   <li>No profile → {@code R.id.action_splash_to_profile} (registration screen)</li>
     *   <li>Entrant or Organizer → {@code R.id.action_splash_to_home}</li>
     *   <li>Admin → {@code R.id.action_splash_to_admin}</li>
     *   <li>No matching role → {@code R.id.action_splash_to_profile}</li>
     * </ul>
     *
     * <p>Navigation is performed on the main thread. The fragment's attachment
     * is verified with {@link #isAdded()} before proceeding.</p>
     *
     * @param uid the Firebase UID of the currently signed-in user
     */
    protected void navigateByProfile(String uid) {
        if (profileRepo == null) profileRepo = new ProfileRepository();
        profileRepo.getProfile(uid, profile -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                NavController navController = NavHostFragment.findNavController(this);
                if (profile == null) {
                    navController.navigate(R.id.action_splash_to_profile);
                } else if (profile.isEntrant()) {
                    navController.navigate(R.id.action_splash_to_home);
                } else if (profile.isOrganizer()) {
                    navController.navigate(R.id.action_splash_to_home);
                } else if (profile.isAdmin()) {
                    navController.navigate(R.id.action_splash_to_admin);
                } else {
                    navController.navigate(R.id.action_splash_to_profile);
                }
            });
        });
    }
}
