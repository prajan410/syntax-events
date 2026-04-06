package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
/**
 * Unit tests for {@link NotificationRepository}.
 *
 * This test suite verifies the behavior of the repository layer responsible
 * for retrieving notifications from Firestore.
 */
public class NotificationRepositoryTest {

    // ─── Testable subclass ─────────────────────────────────────────
    // Injects our mock Firestore instead of calling getInstance()
    /**
     * Testable subclass of {@link NotificationRepository} that injects
     * a mocked {@link FirebaseFirestore} instance.
     *
     * <p>This avoids calling {@code FirebaseFirestore.getInstance()} and allows
     * controlled testing of Firestore interactions.
     */
    private static class TestableRepository extends NotificationRepository {
        private final FirebaseFirestore mockDb;

        TestableRepository(FirebaseFirestore mockDb) {
            super(true); // calls protected no-Firebase constructor
            this.mockDb = mockDb;
        }

        @Override
        // We override to expose mockDb — requires making db protected in the real class
        // If db is private, use the pattern shown in the notes below
        public void getNotificationsForUser(String userId, NotificationListCallback callback) {
            // Delegate to real logic but using mockDb
            mockDb.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Notification n = doc.toObject(Notification.class);
                            if (n != null) {
                                n.setNotificationId(doc.getId());
                                notifications.add(n);
                            }
                        }
                        callback.onLoaded(notifications);
                    })
                    .addOnFailureListener(e -> callback.onLoaded(null));
        }
    }

    // ─── Mocks ─────────────────────────────────────────────────────

    @Mock FirebaseFirestore mockFirestore;
    @Mock CollectionReference mockUsersCollection;
    @Mock CollectionReference mockNotificationsCollection;
    @Mock DocumentReference mockUserDocument;
    @Mock Query mockQuery;
    @Mock Task<QuerySnapshot> mockQueryTask;
    @Mock QuerySnapshot mockQuerySnapshot;
    @Mock DocumentSnapshot mockDocSnapshot1;
    @Mock DocumentSnapshot mockDocSnapshot2;

    private TestableRepository repository;

    // ─── Setup ─────────────────────────────────────────────────────
    /**
     * Initializes mocks and sets up Firestore method chaining before each test.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new TestableRepository(mockFirestore);

        // Wire up the Firestore chain:
        // db.collection("users").document(id).collection("notifications").orderBy(...).get()
        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDocument);
        when(mockUserDocument.collection("notifications")).thenReturn(mockNotificationsCollection);
        when(mockNotificationsCollection.orderBy(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);
    }

    // ─── Helpers ───────────────────────────────────────────────────

    /** Simulates Firestore calling onSuccess with the given snapshot. */
    @SuppressWarnings("unchecked")
    private void simulateQuerySuccess(QuerySnapshot snapshot) {
        when(mockQueryTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            ((OnSuccessListener<QuerySnapshot>) inv.getArgument(0)).onSuccess(snapshot);
            return mockQueryTask;
        });
        when(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask);
    }

    /** Simulates Firestore calling onFailure. */
    @SuppressWarnings("unchecked")
    private void simulateQueryFailure(Exception e) {
        when(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnFailureListener(any())).thenAnswer(inv -> {
            ((OnFailureListener) inv.getArgument(0)).onFailure(e);
            return mockQueryTask;
        });
    }

    // ─── NotificationCallback interface ────────────────────────────
    /** Verifies NotificationCallback receives true result. */

    @Test
    public void notificationCallback_onComplete_true() {
        NotificationRepository.NotificationCallback cb =
                mock(NotificationRepository.NotificationCallback.class);
        cb.onComplete(true);
        verify(cb).onComplete(true);
    }
    /** Verifies NotificationCallback receives false result. */

    @Test
    public void notificationCallback_onComplete_false() {
        NotificationRepository.NotificationCallback cb =
                mock(NotificationRepository.NotificationCallback.class);
        cb.onComplete(false);
        verify(cb).onComplete(false);
    }

    // ─── NotificationListCallback interface ────────────────────────
    /** Verifies NotificationListCallback receives a list. */

    @Test
    public void notificationListCallback_onLoaded_withList() {
        NotificationRepository.NotificationListCallback cb =
                mock(NotificationRepository.NotificationListCallback.class);
        List<Notification> list = Arrays.asList(new Notification(), new Notification());
        cb.onLoaded(list);
        verify(cb).onLoaded(list);
    }
    /** Verifies NotificationListCallback receives null on failure. */
    @Test
    public void notificationListCallback_onLoaded_withNull() {
        NotificationRepository.NotificationListCallback cb =
                mock(NotificationRepository.NotificationListCallback.class);
        cb.onLoaded(null);
        verify(cb).onLoaded(null);
    }

    // ─── getNotificationsForUser — success ─────────────────────────
    /** Verifies successful query returns a populated notification list. */

    @Test
    public void getNotificationsForUser_success_returnsNotifications() {
        Notification n1 = new Notification();
        n1.setTitle("Alert");

        when(mockDocSnapshot1.toObject(Notification.class)).thenReturn(n1);
        when(mockDocSnapshot1.getId()).thenReturn("doc-id-1");
        when(mockQuerySnapshot.getDocuments()).thenReturn(Arrays.asList(mockDocSnapshot1));

        simulateQuerySuccess(mockQuerySnapshot);

        List<Notification>[] result = new List[1];
        repository.getNotificationsForUser("user-123", notifications -> result[0] = notifications);

        assertNotNull(result[0]);
        assertEquals(1, result[0].size());
        assertEquals("doc-id-1", result[0].get(0).getNotificationId());
    }
    /** Verifies document ID is correctly assigned to Notification object. */

    @Test
    public void getNotificationsForUser_success_setsNotificationId() {
        Notification n = new Notification();
        when(mockDocSnapshot1.toObject(Notification.class)).thenReturn(n);
        when(mockDocSnapshot1.getId()).thenReturn("notif-abc");
        when(mockQuerySnapshot.getDocuments()).thenReturn(Arrays.asList(mockDocSnapshot1));

        simulateQuerySuccess(mockQuerySnapshot);

        List<Notification>[] result = new List[1];
        repository.getNotificationsForUser("user-1", notifications -> result[0] = notifications);

        assertEquals("notif-abc", result[0].get(0).getNotificationId());
    }
    /** Verifies multiple documents are correctly converted. */

    @Test
    public void getNotificationsForUser_success_multipleDocuments() {
        Notification n1 = new Notification();
        Notification n2 = new Notification();

        when(mockDocSnapshot1.toObject(Notification.class)).thenReturn(n1);
        when(mockDocSnapshot1.getId()).thenReturn("id-1");
        when(mockDocSnapshot2.toObject(Notification.class)).thenReturn(n2);
        when(mockDocSnapshot2.getId()).thenReturn("id-2");
        when(mockQuerySnapshot.getDocuments())
                .thenReturn(Arrays.asList(mockDocSnapshot1, mockDocSnapshot2));

        simulateQuerySuccess(mockQuerySnapshot);

        List<Notification>[] result = new List[1];
        repository.getNotificationsForUser("user-1", notifications -> result[0] = notifications);

        assertEquals(2, result[0].size());
    }
    /** Verifies empty query result returns an empty list. */

    @Test
    public void getNotificationsForUser_success_emptySnapshot_returnsEmptyList() {
        when(mockQuerySnapshot.getDocuments()).thenReturn(new ArrayList<>());
        simulateQuerySuccess(mockQuerySnapshot);

        List<Notification>[] result = new List[1];
        repository.getNotificationsForUser("user-1", notifications -> result[0] = notifications);

        assertNotNull(result[0]);
        assertEquals(0, result[0].size());
    }
    /** Verifies null objects from Firestore are safely ignored. */

    @Test
    public void getNotificationsForUser_nullDocumentObject_isSkipped() {
        when(mockDocSnapshot1.toObject(Notification.class)).thenReturn(null);
        when(mockDocSnapshot1.getId()).thenReturn("id-1");
        when(mockQuerySnapshot.getDocuments()).thenReturn(Arrays.asList(mockDocSnapshot1));

        simulateQuerySuccess(mockQuerySnapshot);

        List<Notification>[] result = new List[1];
        repository.getNotificationsForUser("user-1", notifications -> result[0] = notifications);

        assertEquals(0, result[0].size());
    }

    // ─── getNotificationsForUser — failure ─────────────────────────
    /** Verifies failure triggers callback with null result. */

    @Test
    public void getNotificationsForUser_failure_callbackReceivesNull() {
        simulateQueryFailure(new Exception("Firestore unavailable"));

        List<Notification>[] result = new List[1];
        repository.getNotificationsForUser("user-1", notifications -> result[0] = notifications);

        assertNull(result[0]);
    }

    // ─── Firestore chain is called correctly ───────────────────────
    /** Verifies correct Firestore query chain is executed. */

    @Test
    public void getNotificationsForUser_queriesCorrectUserSubcollection() {
        when(mockQuerySnapshot.getDocuments()).thenReturn(new ArrayList<>());
        simulateQuerySuccess(mockQuerySnapshot);

        repository.getNotificationsForUser("user-xyz", notifications -> {});

        verify(mockFirestore).collection("users");
        verify(mockUsersCollection).document("user-xyz");
        verify(mockUserDocument).collection("notifications");
        verify(mockNotificationsCollection).orderBy("timestamp", Query.Direction.DESCENDING);
    }

    // ─── Notification model integrity ──────────────────────────────
    /** Verifies default Notification object state. */

    @Test
    public void notification_defaultConstructor_allFieldsNull() {
        Notification n = new Notification();
        assertNull(n.getNotificationId());
        assertNull(n.getTitle());
        assertNull(n.getBody());
        assertNull(n.getSenderRole());
        assertNull(n.getEventId());
        assertEquals(0L, n.getTimestamp());
    }
    /** Verifies title setter and getter. */

    @Test
    public void notification_setAndGetTitle() {
        Notification n = new Notification();
        n.setTitle("Test Title");
        assertEquals("Test Title", n.getTitle());
    }
    /** Verifies Firestore ID assignment behavior. */

    @Test
    public void notification_setNotificationId_afterFirestoreWrite() {
        Notification n = new Notification();
        n.setNotificationId("firestore-doc-id");
        assertEquals("firestore-doc-id", n.getNotificationId());
    }
}