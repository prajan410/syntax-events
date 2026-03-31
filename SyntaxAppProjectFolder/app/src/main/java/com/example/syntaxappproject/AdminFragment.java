package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.chip.Chip;

/**
 * Fragment that displays the admin dashboard.
 * Provides navigation to admin tools including event browsing, profile management,
 * image moderation, comment moderation, and notification logs.
 * If the current user also has an entrant or organizer role, a "Back Home" button
 * is shown to return them to their regular view.
 */
public class AdminFragment extends Fragment {

    /**
     * Required empty public constructor for this fragment.
     */
    public AdminFragment() {}

    /**
     * Creates and returns the view for the admin dashboard.
     * Inflates the layout, sets up entry animations for all UI elements,
     * wires up navigation click listeners for each admin tool, and conditionally
     * shows a "Back Home" button for users who also hold an entrant or organizer role.
     *
     * @param inflater           used to inflate the fragment layout
     * @param container          parent view that the fragment's UI will be attached to
     * @param savedInstanceState previous saved state, if any
     * @return the root view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        View heroCard       = view.findViewById(R.id.heroCard);
        View headerTitle    = view.findViewById(R.id.headerTitle);
        View headerSubtitle = view.findViewById(R.id.headerSubtitle);
        Chip adminBadge     = view.findViewById(R.id.adminBadge);
        View toolsLabel     = view.findViewById(R.id.toolsLabel);
        View actionsCard    = view.findViewById(R.id.actionsCard);
        View btnEvents      = view.findViewById(R.id.cardBrowseEvents);
        View btnProfiles    = view.findViewById(R.id.cardBrowseProfiles);
        View btnImages      = view.findViewById(R.id.cardBrowseImages);
        View btnComments    = view.findViewById(R.id.cardBrowseComments);
        View btnNotifs      = view.findViewById(R.id.cardNotificationLogs);
        View btnBack        = view.findViewById(R.id.backCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f).setDuration(400)
                .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();

        heroCard.setTranslationY(30f);
        heroCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100)
                .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();

        headerSubtitle.animate().alpha(1f).setDuration(300).setStartDelay(200).start();
        adminBadge.animate().alpha(1f).setDuration(300).setStartDelay(300).start();
        toolsLabel.animate().alpha(1f).setDuration(300).setStartDelay(380).start();
        animateCard(actionsCard, 440);

        btnEvents.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.adminBrowseEvents));
        btnProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.adminBrowseProfiles));
        btnImages.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.adminBrowseImages));
        btnComments.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.adminBrowseComments));

        AuthenticationService authService = new AuthenticationService();
        String uid = authService.getCurrentUserId();
        if (uid != null) {
            new ProfileRepository().getProfile(uid, profile -> {
                if (profile == null || getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (profile.isEntrant() || profile.isOrganizer()) {
                        btnBack.setVisibility(View.VISIBLE);
                        btnBack.setOnClickListener(v -> {
                            if (profile.isEntrant()) {
                                NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
                            } else {
                                NavHostFragment.findNavController(this).navigate(R.id.organizerEventsFragment);
                            }
                        });
                    }
                });
            });
        }

        return view;
    }

    /**
     * Animates a card view into visibility with a slide-up and fade-in effect.
     * The card starts slightly below its final position and translucent,
     * then animates to full opacity and its natural position.
     *
     * @param card       the view to animate
     * @param startDelay the delay in milliseconds before the animation begins,
     *                   used to stagger animations across multiple cards
     */
    private void animateCard(View card, int startDelay) {
        card.setTranslationY(30f);
        card.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(startDelay)
                .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
    }
}