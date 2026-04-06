package com.example.syntaxappproject.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.NotificationRepository;
import com.example.syntaxappproject.R;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeBar} factory method to
 * create an instance of this fragment.
 */
public abstract class HomeBar extends Fragment {
    /**
     * Navigation function of homwbar to other fragments
     * @param view the view of current fragment
     */
    protected void setupHotbar(View view) {
        NavController navController = NavHostFragment.findNavController(this);
        View hotbar = view.findViewById(R.id.homebarFragment);

        hotbar.findViewById(R.id.homeButton).setOnClickListener(v ->
                navController.navigate(R.id.toHomeFragment));
        hotbar.findViewById(R.id.userButton).setOnClickListener(v ->
                navController.navigate(R.id.toUserFragment));
        hotbar.findViewById(R.id.qrScannerButton).setOnClickListener(v ->
                navController.navigate(R.id.toQrCodeScannerFragment));
        hotbar.findViewById(R.id.notificationButton).setOnClickListener(v ->
                navController.navigate(R.id.toNotificationFragment));

        String userId = new AuthenticationService().getCurrentUserId();
        if (userId != null) {
            new NotificationRepository().getNotificationsForUser(userId, notifications -> {
                if (!isAdded()) return;

                SharedPreferences prefs = requireContext().getSharedPreferences("seen_notifs", Context.MODE_PRIVATE);
                Set<String> seen = prefs.getStringSet("seen_ids", new HashSet<>());

                int unseen = 0;
                if (notifications != null) {
                    for (Notification n : notifications) {
                        if (n.getNotificationId() != null && !seen.contains(n.getNotificationId())) {
                            unseen++;
                        }
                    }
                }
                int finalUnseen = unseen;
                requireActivity().runOnUiThread(() -> {
                    if (!(this instanceof NotificationFragment)) {
                        showNotificationBadge(finalUnseen);
                    }
                });
            });
        }
    }
    public void showNotificationBadge(int count) {
        if (!isAdded()) return;
        View hotbar = requireView().findViewById(R.id.homebarFragment);
        if (hotbar == null) return;
        TextView badge = hotbar.findViewById(R.id.notificationBadge);
        if (badge == null) return;
        if (count <= 0) {
            badge.setVisibility(View.GONE);
        } else {
            badge.setText(count > 9 ? "9+" : String.valueOf(count));
            badge.setVisibility(View.VISIBLE);
        }
    }
}
