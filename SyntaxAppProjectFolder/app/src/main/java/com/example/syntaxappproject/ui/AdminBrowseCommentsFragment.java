package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AdminCommentAdapter;
import com.example.syntaxappproject.Comment;
import com.example.syntaxappproject.CommentRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for administrators to browse and moderate all comments in the system.
 * Provides filtering by reported status, real-time search by user or comment text,
 * and navigation to comment details for moderation actions.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>All Comments filter - shows all comments</li>
 *   <li>Reported filter - shows only comments with at least one report</li>
 *   <li>Real-time search - filters as user types</li>
 *   <li>Click on any comment to view details and moderate</li>
 * </ul>
 */
public class AdminBrowseCommentsFragment extends HomeBar {

    /** RecyclerView that displays the list of comments. */
    private RecyclerView recyclerView;

    /** Adapter that binds comment data to the RecyclerView. */
    private AdminCommentAdapter adapter;

    /** Spinner shown while comments are loading. */
    private View loadingSpinner;

    /** Text shown when no comments match the current filter. */
    private TextView emptyText;

    /** Badge showing the number of comments in the current view. */
    private TextView countBadge;

    /** Search input for filtering comments by user or text. */
    private EditText searchInput;

    /** Button to show all comments. */
    private Button filterAllBtn;

    /** Button to show only comments with reports. */
    private Button filterReportedBtn;

    /** Complete list of all comments fetched from Firestore. */
    private List<Comment> allComments = new ArrayList<>();

    /** Currently filtered list of comments to display. */
    private List<Comment> filteredComments = new ArrayList<>();

    /** Repository for comment database operations. */
    private CommentRepository commentRepository;

    /** Current active filter: "all" or "reported". */
    private String currentFilter = "all";

    /**
     *  For testing, disables bypasses connecting to firestore
     */
    public boolean disableFirestoreForTest = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_browse_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try { setupHotbar(view); } catch (Exception ignored) {}

        commentRepository = new CommentRepository();

        recyclerView = view.findViewById(R.id.recycler_comments);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        emptyText = view.findViewById(R.id.emptyText);
        countBadge = view.findViewById(R.id.commentCountBadge);
        searchInput = view.findViewById(R.id.searchInput);
        filterAllBtn = view.findViewById(R.id.filterAllBtn);
        filterReportedBtn = view.findViewById(R.id.filterReportedBtn);

        View headerTitle = view.findViewById(R.id.headerTitle);
        View mainCard = view.findViewById(R.id.mainCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        countBadge.animate().alpha(1f)
                .setDuration(300).setStartDelay(200).start();

        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(250).start();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminCommentAdapter(this::openCommentDetails);
        recyclerView.setAdapter(adapter);

        setupFilters();
        setupSearch();
        loadAllComments();

        MaterialButton doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
    }

    /**
     * Configures the filter buttons to toggle between "All" and "Reported" views.
     */
    private void setupFilters() {
        filterAllBtn.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtons();
            applyFilters();
        });

        filterReportedBtn.setOnClickListener(v -> {
            currentFilter = "reported";
            updateFilterButtons();
            applyFilters();
        });
    }

    /**
     * Sets mock comments for testing purposes.
     * Used in instrumented tests to bypass Firestore.
     *
     * @param mockComments list of mock comments to display
     */
    public void setMockComments(List<Comment> mockComments) {
        this.allComments = mockComments;
        loadingSpinner.setVisibility(View.GONE);
        applyFilters();
    }

    /**
     * Updates the visual state of filter buttons.
     * Active button gets a green/orange color, inactive gets gray.
     */
    private void updateFilterButtons() {
        filterAllBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                currentFilter.equals("all") ? 0xFF000000 : 0xFFAAAAAA));
        filterReportedBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                currentFilter.equals("reported") ? 0xFFFF9800 : 0xFFAAAAAA));
    }

    /**
     * Sets up real-time search with TextWatcher that filters on every character change.
     */
    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads all comments from Firestore via the repository.
     * Shows loading spinner while fetching data.
     */
    private void loadAllComments() {
        if (disableFirestoreForTest) {
            loadingSpinner.setVisibility(View.GONE);
            if (allComments.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                countBadge.setText("0 comments");
            }
            return;
        }
        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        commentRepository.getAllComments(comments -> {
            requireActivity().runOnUiThread(() -> {
                loadingSpinner.setVisibility(View.GONE);
                allComments = comments;
                applyFilters();
            });
        });
    }

    /**
     * Applies both filter and search to the comment list.
     * Filters by reported status first, then applies search text filter.
     * Updates the adapter and UI accordingly.
     */
    private void applyFilters() {
        filteredComments.clear();

        String searchQuery = searchInput.getText().toString().toLowerCase();

        for (Comment comment : allComments) {
            boolean matchesFilter = true;
            boolean matchesSearch = true;

            switch (currentFilter) {
                case "reported":
                    matchesFilter = comment.getReportCount() > 0;
                    break;
                default:
                    matchesFilter = true;
            }

            if (!searchQuery.isEmpty()) {
                matchesSearch = comment.getCommentText().toLowerCase().contains(searchQuery) ||
                        comment.getUserName().toLowerCase().contains(searchQuery);
            }

            if (matchesFilter && matchesSearch) {
                filteredComments.add(comment);
            }
        }

        adapter.setComments(filteredComments);
        countBadge.setText(filteredComments.size() + " comments");

        if (filteredComments.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    /**
     * Navigates to the comment details screen for the selected comment.
     * Passes all necessary comment data via Bundle arguments.
     *
     * @param comment the comment to view details for
     */
    private void openCommentDetails(Comment comment) {
        Bundle bundle = new Bundle();
        bundle.putString("commentId", comment.getCommentId());
        bundle.putString("eventId", comment.getEventId());
        bundle.putString("userId", comment.getUserId());
        bundle.putString("userName", comment.getUserName());
        bundle.putString("commentText", comment.getCommentText());
        bundle.putInt("reportCount", comment.getReportCount());
        if (comment.getTimestamp() != null) {
            bundle.putLong("timestamp", comment.getTimestamp().getSeconds());
        }
        Navigation.findNavController(requireView()).navigate(R.id.adminCommentDetails, bundle);
    }
}