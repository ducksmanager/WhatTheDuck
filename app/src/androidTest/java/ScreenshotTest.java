import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.MediaStore;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import net.ducksmanager.util.CoverFlowActivity;
import net.ducksmanager.util.CoverFlowFileHandler;
import net.ducksmanager.whattheduck.IssueList;
import net.ducksmanager.whattheduck.ItemAdapter;
import net.ducksmanager.whattheduck.PublicationList;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@RunWith(Parameterized.class)
@LargeTest
public class ScreenshotTest extends WtdTest {
    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new LocaleWithDefaultPublication[][]{
            {new LocaleWithDefaultPublication("se", "sv") {
                String getDefaultPublication() { return "se/KAP"; }
            }},
            {new LocaleWithDefaultPublication("us", "en") {
                String getDefaultPublication() { return "us/WDC"; }
            }},
            {new LocaleWithDefaultPublication("fr", "fr") {
                String getDefaultPublication() { return "fr/DDD"; }
            }}
        });
    }

    @BeforeClass
    public static void initDownloadHelper() {
        initMockServer();
    }

    public ScreenshotTest(LocaleWithDefaultPublication currentLocale) {
        WtdTest.currentLocale = currentLocale;
    }

    private String getScreenshotPath() {
        return ScreenshotTestRule.SCREENSHOTS_PATH_SHOWCASE + "/" + currentLocale.getLocale().getLanguage();
    }

    private void switchLocale() {
        Context context = WhatTheDuck.wtd.getApplicationContext();
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(currentLocale.getLocale());
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    @Before
    public void switchLocaleAndLogin() {
        switchLocale();
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS);
    }

    @Test
    public void testPublicationList() {
        onView(allOf(withId(R.id.addToCollectionBySelectionButton), forceFloatingActionButtonsVisible()))
            .perform(click());

        Matcher<RecyclerView.ViewHolder> countryMatcher = getItemMatcher(currentLocale.getDefaultCountry().toLowerCase());
        onView((withId(R.id.itemList))).perform(scrollToHolder(countryMatcher), actionOnHolderItem(countryMatcher, click()));

        assertCurrentActivityIsInstanceOf(PublicationList.class, true);

        ScreenshotTestRule.takeScreenshot("Publication list", getActivityInstance(), getScreenshotPath());
    }

    @Test
    public void testIssueList() {
        Matcher<RecyclerView.ViewHolder> countryMatcher = getItemMatcher(currentLocale.getDefaultCountry().toLowerCase());
        onView((withId(R.id.itemList))).perform(scrollToHolder(countryMatcher), actionOnHolderItem(countryMatcher, click()));

        Matcher<RecyclerView.ViewHolder> publicationMatcher = getItemMatcher(currentLocale.getDefaultPublication());
        onView((withId(R.id.itemList))).perform(scrollToHolder(publicationMatcher), actionOnHolderItem(publicationMatcher, click()));

        assertCurrentActivityIsInstanceOf(IssueList.class, true);
        ScreenshotTestRule.takeScreenshot("Issue list", getActivityInstance(), getScreenshotPath());
    }

    @Test
    public void testCoverFlowResults() {
        CoverFlowFileHandler.mockedResource = mockServer.url("/internal/photos/2648").toString();

        Intent resultData = new Intent();
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);

        Matcher<Intent> expectedIntent = hasAction(MediaStore.ACTION_IMAGE_CAPTURE);
        Intents.init();
        intending(expectedIntent).respondWith(result);

        onView(allOf(withId(R.id.addToCollectionByPhotoButton), forceFloatingActionButtonsVisible())).perform(click());

        intended(expectedIntent);
        Intents.release();

        ScreenshotTestRule.takeScreenshot("Cover search result", getActivityInstance(), getScreenshotPath());

        onView(allOf(withId(R.id.image), coverCurrentlyVisible()))
            .perform(click());

        ScreenshotTestRule.takeScreenshot("Cover search result - add issue", getActivityInstance(), getScreenshotPath());

    }

    private static Matcher<RecyclerView.ViewHolder> getItemMatcher(final String identifier) {
        return new BoundedMatcher<RecyclerView.ViewHolder, ItemAdapter.ViewHolder>(ItemAdapter.ViewHolder.class) {
            @Override
            protected boolean matchesSafely(ItemAdapter.ViewHolder item) {
                return item.titleTextView.getTag().equals(identifier);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("view holder with ID: " + identifier);
            }
        };
    }

    private static Matcher<Object> coverCurrentlyVisible() {
        return new BoundedMatcher<Object, ImageView>(ImageView.class) {
            @Override
            public boolean matchesSafely(final ImageView item) {
                return CoverFlowActivity.currentCoverUrl.equals(item.getTag()) && item.getVisibility() == View.VISIBLE;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Currently visible cover");
            }
        };
    }
}
