package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.button.MaterialButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class InviteEntrantAdapterTest {

    private Context context;

    /**
     * Initializes the test context before each test runs.
     */
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * Creates a themed parent layout for inflating adapter views.
     *
     * @return a themed FrameLayout
     */
    private FrameLayout createThemedParent() {
        Context themedContext = new ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar
        );
        return new FrameLayout(themedContext);
    }

    /**
     * Creates a test Profile object with the given values.
     *
     * @param name the profile name
     * @param email the profile email
     * @param phone the profile phone
     * @return a Profile instance
     */
    private Profile makeProfile(String name, String email, String phone) {
        Profile profile = new Profile();
        profile.setName(name);
        profile.setEmail(email);
        profile.setPhone(phone);
        return profile;
    }

    /**
     * Verifies that getItemCount returns the correct number of profiles.
     */
    @Test
    public void testGetItemCount_returnsCorrectSize() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(makeProfile("Alice", "alice@test.com", "111"));
        profiles.add(makeProfile("Bob", "bob@test.com", "222"));

        List<String> ids = new ArrayList<>(Arrays.asList("u1", "u2"));

        InviteEntrantAdapter adapter = new InviteEntrantAdapter(profiles, ids, null);

        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Verifies that binding a view holder sets the correct text values.
     */
    @Test
    public void testOnBindViewHolder_setsTextsCorrectly() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(makeProfile("Alice Smith", "alice@test.com", "1112223333"));

        List<String> ids = new ArrayList<>(Arrays.asList("u1"));

        InviteEntrantAdapter adapter = new InviteEntrantAdapter(profiles, ids, null);

        FrameLayout parent = createThemedParent();
        InviteEntrantAdapter.InviteViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        TextView name = holder.itemView.findViewById(R.id.tvInviteName);
        TextView email = holder.itemView.findViewById(R.id.tvInviteEmail);
        TextView phone = holder.itemView.findViewById(R.id.tvInvitePhone);

        assertEquals("Alice Smith", name.getText().toString());
        assertEquals("alice@test.com", email.getText().toString());
        assertEquals("1112223333", phone.getText().toString());
    }

    /**
     * Verifies that fallback text is shown when profile fields are missing.
     */
    @Test
    public void testOnBindViewHolder_usesFallbackText() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(makeProfile(null, null, ""));

        List<String> ids = new ArrayList<>(Arrays.asList("u1"));

        InviteEntrantAdapter adapter = new InviteEntrantAdapter(profiles, ids, null);

        FrameLayout parent = createThemedParent();
        InviteEntrantAdapter.InviteViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        TextView name = holder.itemView.findViewById(R.id.tvInviteName);
        TextView email = holder.itemView.findViewById(R.id.tvInviteEmail);
        TextView phone = holder.itemView.findViewById(R.id.tvInvitePhone);

        assertEquals("No name", name.getText().toString());
        assertEquals("No email", email.getText().toString());
        assertEquals("No phone", phone.getText().toString());
    }

    /**
     * Verifies that clicking the invite button triggers the listener
     * with the correct profile and user ID.
     */
    @Test
    public void testInviteButton_callsListener() {
        List<Profile> profiles = new ArrayList<>();
        Profile profile = makeProfile("Alice", "alice@test.com", "123");
        profiles.add(profile);

        List<String> ids = new ArrayList<>(Arrays.asList("user-123"));

        final Profile[] clickedProfile = {null};
        final String[] clickedId = {null};

        InviteEntrantAdapter adapter = new InviteEntrantAdapter(
                profiles,
                ids,
                (p, userId) -> {
                    clickedProfile[0] = p;
                    clickedId[0] = userId;
                }
        );

        FrameLayout parent = createThemedParent();
        InviteEntrantAdapter.InviteViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        MaterialButton inviteButton = holder.itemView.findViewById(R.id.btnInviteEntrant);
        assertNotNull(inviteButton);

        inviteButton.performClick();

        assertEquals("Alice", clickedProfile[0].getName());
        assertEquals("user-123", clickedId[0]);
    }

    /**
     * Verifies that updateData replaces the adapter's data
     * and updates the displayed content.
     */
    @Test
    public void testUpdateData_replacesAdapterData() {
        List<Profile> oldProfiles = new ArrayList<>();
        oldProfiles.add(makeProfile("Old", "old@test.com", "111"));
        List<String> oldIds = new ArrayList<>(Arrays.asList("old-id"));

        InviteEntrantAdapter adapter = new InviteEntrantAdapter(oldProfiles, oldIds, null);

        List<Profile> newProfiles = new ArrayList<>();
        newProfiles.add(makeProfile("New User", "new@test.com", "999"));
        newProfiles.add(makeProfile("Another User", "another@test.com", "888"));
        List<String> newIds = new ArrayList<>(Arrays.asList("new1", "new2"));

        adapter.updateData(newProfiles, newIds);

        assertEquals(2, adapter.getItemCount());

        FrameLayout parent = createThemedParent();
        InviteEntrantAdapter.InviteViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        TextView name = holder.itemView.findViewById(R.id.tvInviteName);
        assertEquals("New User", name.getText().toString());
    }
}