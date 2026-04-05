package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class ImageAdapterTest {

    private Context context;

    /**
     * Sets up the test context and clears the image cache before each test.
     */
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ImageCacheManager.clear();
    }

    /**
     * Clears the image cache after each test completes.
     */
    @After
    public void tearDown() {
        ImageCacheManager.clear();
    }

    /**
     * Creates a themed parent layout for inflating adapter item views.
     *
     * @return a {@link FrameLayout} using a Material Components theme
     */
    private FrameLayout createThemedParent() {
        Context themedContext = new ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar
        );
        return new FrameLayout(themedContext);
    }

    /**
     * Verifies that {@code getItemCount()} returns the correct number of images.
     */
    @Test
    public void testGetItemCount_returnsCorrectSize() {
        ArrayList<ImageItem> images = new ArrayList<>();
        images.add(new ImageItem("url1", "user1"));
        images.add(new ImageItem("url2", "user2"));

        ArrayList<String> ids = new ArrayList<>(Arrays.asList("id1", "id2"));

        ImageAdapter adapter = new ImageAdapter(images, ids);

        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Verifies that {@code updateData()} replaces the adapter's image list
     * and updates the item count accordingly.
     */
    @Test
    public void testUpdateData_replacesAdapterData() {
        ArrayList<ImageItem> oldImages = new ArrayList<>();
        oldImages.add(new ImageItem("old-url", "old-user"));
        ArrayList<String> oldIds = new ArrayList<>(Arrays.asList("old-id"));

        ImageAdapter adapter = new ImageAdapter(oldImages, oldIds);

        ArrayList<ImageItem> newImages = new ArrayList<>();
        newImages.add(new ImageItem("new-url", "new-user"));
        newImages.add(new ImageItem("another-url", "another-user"));
        ArrayList<String> newIds = new ArrayList<>(Arrays.asList("new-id-1", "new-id-2"));

        adapter.updateData(newImages, newIds);

        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Verifies that binding a view holder uses a cached bitmap when one
     * exists for the image ID and displays it in the preview image view.
     */
    @Test
    public void testOnBindViewHolder_usesCachedBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        ImageCacheManager.put("event-001", bitmap);

        ArrayList<ImageItem> images = new ArrayList<>();
        images.add(new ImageItem("ignored-url", "Yixing"));

        ArrayList<String> ids = new ArrayList<>(Arrays.asList("event-001"));

        ImageAdapter adapter = new ImageAdapter(images, ids);

        FrameLayout parent = createThemedParent();
        ImageAdapter.ImageViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.img_preview);
        assertNotNull(imageView.getDrawable());
    }

    /**
     * Verifies that clicking the preview image triggers navigation to
     * the admin image details screen with the correct image data.
     */
    @Test
    public void testImageClick_navigatesWithBundle() {
        Bitmap bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        ImageCacheManager.put("event-777", bitmap);

        ArrayList<ImageItem> images = new ArrayList<>();
        images.add(new ImageItem("poster-url", "Yixing Li"));

        ArrayList<String> ids = new ArrayList<>(Arrays.asList("event-777"));

        ImageAdapter adapter = new ImageAdapter(images, ids);

        FrameLayout parent = createThemedParent();
        ImageAdapter.ImageViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        NavController navController = mock(NavController.class);
        Navigation.setViewNavController(holder.itemView, navController);

        adapter.onBindViewHolder(holder, 0);

        holder.itemView.findViewById(R.id.img_preview).performClick();

        ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
        verify(navController).navigate(eq(R.id.adminImageDetails), bundleCaptor.capture());

        Bundle bundle = bundleCaptor.getValue();
        assertNotNull(bundle);
        assertEquals("event-777", bundle.getString("imageId"));
        assertEquals("poster-url", bundle.getString("imageUrl"));
        assertEquals("Yixing Li", bundle.getString("uploadedBy"));
    }
}