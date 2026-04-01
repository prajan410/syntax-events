package com.example.syntaxappproject.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.BulletPointHelper;
import com.example.syntaxappproject.Comment;
import com.example.syntaxappproject.CommentAdapter;
import com.example.syntaxappproject.CommentRepository;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.ImageItem;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays full details for a single event, including its poster,
 * QR code, metadata, and a context-sensitive action button.
 */
public class EventDetailFragment extends HomeBar {

    private static final int COMMENTS_PER_PAGE = 10;

    private String eventId;
    private final AuthenticationService authService = new AuthenticationService();
    private final EventJoinRepository joinRepo = new EventJoinRepository();
    private CommentRepository commentRepository;
    private CommentAdapter commentAdapter;
    private RecyclerView commentsRecyclerView;
    private TextView emptyCommentsText;
    private EditText commentInput;
    private MaterialButton postCommentButton;
    private MaterialButton prevPageButton;
    private MaterialButton notifyButton;
    private MaterialButton nextPageButton;
    private TextView pageIndicator;
    private View paginationContainer;

    private List<Comment> allComments = new ArrayList<>();
    private int currentPage = 0;
    private int totalPages = 0;

    public boolean testingMode = false;
    private String uid;
    private boolean isOrganizer = false;
    private boolean isAdmin = false;
    private int profilesLoaded = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            testingMode = getArguments().getBoolean("testingMode", false);
        }
        if (!testingMode) super.onViewCreated(view, savedInstanceState);
        if (!testingMode) setupHotbar(view);

        ImageView eventPoster     = view.findViewById(R.id.eventPoster);
        ImageView eventQRCode     = view.findViewById(R.id.eventQRCode);
        TextView eventName        = view.findViewById(R.id.eventName);
        TextView description      = view.findViewById(R.id.eventDescription);
        TextView date             = view.findViewById(R.id.eventDate);
        TextView location         = view.findViewById(R.id.eventLocation);
        TextView regiPeriod       = view.findViewById(R.id.eventRegiPeriod);
        TextView capacity         = view.findViewById(R.id.eventCapacity);
        TextView wLCount          = view.findViewById(R.id.eventWLCount);
        TextView lotteryCriteria  = view.findViewById(R.id.eventLotteryCriteria);
        MaterialButton joinButton = view.findViewById(R.id.joinButton);
        MaterialButton doneButton = view.findViewById(R.id.doneButton);
        MaterialButton mapButton  = view.findViewById(R.id.mapButton);
        View headerTitle          = view.findViewById(R.id.headerTitle);
        View posterCard           = view.findViewById(R.id.posterCard);
        View nameCard             = view.findViewById(R.id.nameCard);
        View detailsCard          = view.findViewById(R.id.detailsCard);
        View actionCard           = view.findViewById(R.id.actionCard);
        View commentsCard         = view.findViewById(R.id.commentsCard);
        View notifyCard           = view.findViewById(R.id.notifyCard);
        commentsRecyclerView      = view.findViewById(R.id.commentsRecyclerView);
        emptyCommentsText         = view.findViewById(R.id.emptyCommentsText);
        commentInput              = view.findViewById(R.id.commentInput);
        postCommentButton         = view.findViewById(R.id.postCommentButton);
        prevPageButton            = view.findViewById(R.id.prevPageButton);
        nextPageButton            = view.findViewById(R.id.nextPageButton);
        pageIndicator             = view.findViewById(R.id.pageIndicator);
        paginationContainer       = view.findViewById(R.id.paginationContainer);
        notifyButton              = view.findViewById(R.id.notifyEntrantsButton);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
        posterCard.setTranslationY(30f);
        posterCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();
        nameCard.setTranslationY(30f);
        nameCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();
        detailsCard.setTranslationY(30f);
        detailsCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(380).start();
        commentsCard.setTranslationY(30f);
        commentsCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(460).start();
        actionCard.setTranslationY(30f);
        actionCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(540).start();
        notifyCard.setTranslationY(30f);
        notifyCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(540).start();
        doneButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        mapButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.to_mapFragment));

        uid = authService.getCurrentUserId();
        if (testingMode && uid == null) uid = "test_user";

        commentRepository = new CommentRepository();
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new CommentAdapter(uid, isOrganizer, isAdmin, this::deleteComment, this::reportComment);
        commentsRecyclerView.setAdapter(commentAdapter);

        prevPageButton.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                displayCurrentPage();
            }
        });

        nextPageButton.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                displayCurrentPage();
            }
        });

        new ProfileRepository().getProfile(uid, profile -> {
            if (profile != null) {
                isAdmin = profile.isAdmin();
            }
            checkAndUpdateAdapter();
        });

        new EventDetailRepository().getEventDetail(eventId, event -> {
            if (event != null && uid != null) {
                isOrganizer = uid.equals(event.getOrganizerUid());
            }
            checkAndUpdateAdapter();
        });

        loadComments();
        postCommentButton.setOnClickListener(v -> postComment());

        if (testingMode) {
            joinButton.setOnClickListener(v -> {
                if (uid == null) return;
                String currentText = wLCount.getText().toString();
                int currentCount = Integer.parseInt(currentText.replaceAll("[^0-9]", ""));
                if ("Join".equals(joinButton.getText().toString())) {
                    joinButton.setText("Leave");
                    wLCount.setText("Waitlist: " + (currentCount + 1));
                } else {
                    joinButton.setText("Join");
                    wLCount.setText("Waitlist: " + (currentCount - 1));
                }
            });
            return;
        }

        new EventDetailRepository().getEventDetail(eventId, event -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                eventName.setText(event.getName());
                description.setText(event.getDescription());
                date.setText(event.getStartingEventDate());
                location.setText(event.getLocation());
                regiPeriod.setText(event.getStartingRegistrationPeriod());
                capacity.setText("Capacity: " + event.getCapacity());
                wLCount.setText("Waitlist: " + event.getWaitlistCount());

                String criteria = event.getLotteryCriteria();
                if (criteria != null && !criteria.isEmpty()) {
                    List<String> criteriaList = BulletPointHelper.parseBulletPoints(criteria);
                    if (!criteriaList.isEmpty()) {
                        StringBuilder displayText = new StringBuilder();
                        for (String point : criteriaList) {
                            displayText.append("• ").append(point).append("\n");
                        }
                        lotteryCriteria.setText(displayText.toString().trim());
                        lotteryCriteria.setVisibility(View.VISIBLE);
                    } else {
                        lotteryCriteria.setVisibility(View.GONE);
                    }
                } else {
                    lotteryCriteria.setVisibility(View.GONE);
                }

                loadPoster(eventPoster);
                loadQRCode(eventQRCode);
                configureActionButton(joinButton, wLCount, event, notifyCard,notifyButton);
            });
        });
    }

    private void checkAndUpdateAdapter() {
        profilesLoaded++;
        if (profilesLoaded == 2) {
            requireActivity().runOnUiThread(() -> {
                commentAdapter.updateRoles(isOrganizer, isAdmin);
                displayCurrentPage();
            });
        }
    }

    private void loadComments() {
        Log.d("EventDetailFragment", "Loading comments for event: " + eventId);
        commentRepository.getCommentsForEvent(eventId, comments -> {
            Log.d("EventDetailFragment", "Comments received: " + (comments != null ? comments.size() : 0));
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                allComments = comments != null ? comments : new ArrayList<>();

                if (allComments.isEmpty()) {
                    emptyCommentsText.setVisibility(View.VISIBLE);
                    commentsRecyclerView.setVisibility(View.GONE);
                    paginationContainer.setVisibility(View.GONE);
                } else {
                    emptyCommentsText.setVisibility(View.GONE);
                    commentsRecyclerView.setVisibility(View.VISIBLE);

                    totalPages = (int) Math.ceil((double) allComments.size() / COMMENTS_PER_PAGE);
                    currentPage = 0;

                    displayCurrentPage();
                }
            });
        });
    }

    private void displayCurrentPage() {
        int start = currentPage * COMMENTS_PER_PAGE;
        int end = Math.min(start + COMMENTS_PER_PAGE, allComments.size());

        List<Comment> pageComments = allComments.subList(start, end);
        commentAdapter.setComments(pageComments);

        if (totalPages > 1) {
            paginationContainer.setVisibility(View.VISIBLE);
            pageIndicator.setText("Page " + (currentPage + 1) + " of " + totalPages);

            prevPageButton.setVisibility(currentPage > 0 ? View.VISIBLE : View.INVISIBLE);
            nextPageButton.setVisibility(currentPage < totalPages - 1 ? View.VISIBLE : View.INVISIBLE);
        } else {
            paginationContainer.setVisibility(View.GONE);
        }

        commentsRecyclerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        commentsRecyclerView.requestLayout();
    }

    private void postComment() {
        String commentText = commentInput.getText() != null ?
                commentInput.getText().toString().trim() : "";

        if (commentText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uid == null) {
            Toast.makeText(requireContext(), "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        new ProfileRepository().getProfile(uid, profile -> {
            if (!isAdded()) return;
            String userName = profile != null && profile.getName() != null ?
                    profile.getName() : "Anonymous";
            String deviceId = profile != null && profile.getDeviceId() != null ?
                    profile.getDeviceId() : "";

            Comment comment = new Comment(eventId, commentText, uid, userName, deviceId);

            commentRepository.addComment(comment, success -> {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        commentInput.setText("");
                        loadComments();
                        Toast.makeText(requireContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void deleteComment(Comment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    commentRepository.deleteComment(comment.getCommentId(), success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                loadComments();
                                Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reportComment(Comment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Report Comment")
                .setMessage("Are you sure you want to report this comment?")
                .setPositiveButton("Report", (dialog, which) -> {
                    commentRepository.reportComment(comment.getCommentId(), uid, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(), "Comment reported. Thank you for helping keep our community safe.", Toast.LENGTH_LONG).show();
                                if (comment.getReportedBy() == null) {
                                    comment.setReportedBy(new ArrayList<>());
                                }
                                comment.getReportedBy().add(uid);
                                comment.setReportCount(comment.getReportCount() + 1);
                                commentAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(requireContext(), "Failed to report comment", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadPoster(ImageView eventPoster) {
        if (ImageCacheManager.has(eventId)) {
            eventPoster.setImageBitmap(ImageCacheManager.get(eventId));
            return;
        }
        ImageItem.fetchByEventId(eventId, new ImageItem.ImageCallback() {
            @Override
            public void onImageLoaded(ImageItem imageItem) {
                if (imageItem == null || imageItem.imageUrl == null) return;
                new Thread(() -> {
                    try {
                        byte[] decoded = Base64.decode(imageItem.imageUrl, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        if (bitmap == null || !isAdded()) return;
                        ImageCacheManager.put(eventId, bitmap);
                        requireActivity().runOnUiThread(() -> eventPoster.setImageBitmap(bitmap));
                    } catch (Exception ignored) {}
                }).start();
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private void loadQRCode(ImageView eventQRCode) {
        FirebaseDatabase.getInstance()
                .getReference("event_qr_codes")
                .child(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists() || !isAdded()) return;
                    String base64 = snapshot.child("image").getValue(String.class);
                    if (base64 == null) return;
                    new Thread(() -> {
                        try {
                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            if (bitmap == null || !isAdded()) return;
                            requireActivity().runOnUiThread(() -> eventQRCode.setImageBitmap(bitmap));
                        } catch (Exception ignored) {}
                    }).start();
                });
    }

    private void configureActionButton(MaterialButton joinButton, TextView wLCount, EventDetail event, View notifyCard, MaterialButton notifyButton) {
        boolean isEventOrganizer = uid != null && uid.equals(event.getOrganizerUid());

        if (isEventOrganizer) {
            joinButton.setText("Manage Event");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            joinButton.setOnClickListener(v -> navigateToManageEvent());
            notifyCard.setVisibility(View.VISIBLE);
            notifyButton.setOnClickListener(v -> {
                navigateToNotifyEntrants();
            });
            return;
        }


        long now      = System.currentTimeMillis();
        long regStart = parseDateMillis(event.getStartingRegistrationPeriod());
        long regEnd   = parseDateMillis(event.getEndingRegistrationPeriod());
        boolean inWindow = regStart != -1 && regEnd != -1 && now >= regStart && now <= regEnd;

        if (!inWindow) {
            joinButton.setText(now < regStart ? "Registration Not Open" : "Registration Closed");
            joinButton.setAlpha(0.5f);
            joinButton.setEnabled(false);
            return;
        }

        joinRepo.hasJoined(eventId, uid, joined ->
                requireActivity().runOnUiThread(() -> joinButton.setText(joined ? "Leave" : "Join"))
        );
        joinButton.setEnabled(true);
        joinButton.setAlpha(1f);
        joinButton.setOnClickListener(v -> handleJoinLeave(joinButton, wLCount));
    }

    private void navigateToManageEvent() {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);
        Navigation.findNavController(requireView()).navigate(R.id.manageEventFragment, bundle);
    }
    private void navigateToNotifyEntrants(){
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);
        Navigation.findNavController(requireView()).navigate(R.id.notifyEntrantsFragment, bundle);
    }

    private void handleJoinLeave(MaterialButton joinButton, TextView wLCount) {
        if (uid == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        new EventDetailRepository().getEventDetail(eventId, freshEvent -> {
            if (!isAdded()) return;
            long eventCapacity = freshEvent.getCapacity();
            long waitlistCount = freshEvent.getWaitlistCount();

            joinRepo.hasJoined(eventId, uid, joined -> requireActivity().runOnUiThread(() -> {
                if (joined) {
                    joinRepo.leaveEvent(eventId, uid, success -> requireActivity().runOnUiThread(() -> {
                        if (success) {
                            joinButton.setText("Join");
                            joinButton.setAlpha(1f);
                            joinButton.setEnabled(true);
                            int c = Integer.parseInt(wLCount.getText().toString().replaceAll("[^0-9]", ""));
                            wLCount.setText("Waitlist: " + (c - 1));
                            Toast.makeText(getContext(), "You left the event", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to leave", Toast.LENGTH_SHORT).show();
                        }
                    }));
                } else {
                    if (eventCapacity > 0 && waitlistCount >= eventCapacity) {
                        joinButton.setAlpha(0.5f);
                        joinButton.setText("Waitlist Full");
                        joinButton.setEnabled(false);
                        Toast.makeText(getContext(), "Event is at capacity", Toast.LENGTH_SHORT).show();
                    } else {
                        joinRepo.joinEvent(eventId, uid, success -> requireActivity().runOnUiThread(() -> {
                            if (success) {
                                joinButton.setText("Leave");
                                int c = Integer.parseInt(wLCount.getText().toString().replaceAll("[^0-9]", ""));
                                wLCount.setText("Waitlist: " + (c + 1));
                                Toast.makeText(getContext(), "Successfully joined!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Join failed", Toast.LENGTH_SHORT).show();
                            }
                        }));
                    }
                }
            }));
        });
    }

    private long parseDateMillis(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return -1;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            sdf.setLenient(false);
            return sdf.parse(dateStr).getTime();
        } catch (Exception ignored) {}
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault());
            sdf.setLenient(false);
            return sdf.parse(dateStr).getTime();
        } catch (Exception ignored) {}
        return -1;
    }
}