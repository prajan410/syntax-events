package com.example.syntaxappproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.ui.EventSignupListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of entrants (users) with their names and emails.
 *
 * <p>This adapter is used in various parts of the application to display entrant information,
 * particularly in the event signup list where organizers can view entrants by category
 * (waiting list, selected, final, cancelled).</p>
 *
 * <p>The adapter uses Android's built-in {@code simple_list_item_2} layout which provides
 * two text lines - perfect for displaying a user's name on the first line and email on the second.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Displays user name and email in a two-line format</li>
 *   <li>Handles null values gracefully with fallback text ("Unknown User", "No email")</li>
 *   <li>Updates dynamically when new data is set</li>
 * </ul>
 *
 * @see Profile
 * @see EventSignupListFragment
 */
public class EntrantNameAdapter extends RecyclerView.Adapter<EntrantNameAdapter.ViewHolder> {

    /** List of profiles to display in the RecyclerView. */
    private List<Profile> profiles = new ArrayList<>();

    /**
     * Updates the adapter with a new list of profiles and refreshes the view.
     *
     * <p>If the provided list is null, an empty list is used instead to prevent
     * NullPointerExceptions. This method also notifies the RecyclerView that
     * the underlying data has changed, triggering a UI update.</p>
     *
     * @param profiles the new list of profiles to display, or null to clear the list
     */
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles != null ? profiles : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Creates and inflates the view holder for an entrant item.
     *
     * <p>Uses Android's built-in {@code android.R.layout.simple_list_item_2} which provides
     * a two-line layout with {@code text1} and {@code text2} TextView IDs.</p>
     *
     * @param parent   the parent view group
     * @param viewType the view type (unused, single type)
     * @return a new ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds profile data to the view holder at the given position.
     *
     * <p>Extracts the user's name and email from the Profile object and sets them
     * on the appropriate TextViews. Handles null values by providing fallback text:
     * <ul>
     *   <li>If name is null, displays "Unknown User"</li>
     *   <li>If email is null, displays "No email"</li>
     * </ul>
     * </p>
     *
     * @param holder   the view holder to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        String name = profile.getName() != null ? profile.getName() : "Unknown User";
        String email = profile.getEmail() != null ? profile.getEmail() : "No email";

        holder.text1.setText(name);
        holder.text2.setText(email);
    }

    /**
     * Returns the total number of profiles in the adapter.
     *
     * @return the number of profiles, or 0 if the list is null
     */
    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * ViewHolder that caches references to the views in each entrant item.
     *
     * <p>Uses Android's built-in {@code android.R.id.text1} and {@code android.R.id.text2}
     * IDs which are standard in the {@code simple_list_item_2} layout.</p>
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView for displaying the user's name (primary text). */
        TextView text1;

        /** TextView for displaying the user's email (secondary text). */
        TextView text2;

        /**
         * Constructs a ViewHolder and binds child views.
         *
         * @param itemView the inflated item view containing the TextViews
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}