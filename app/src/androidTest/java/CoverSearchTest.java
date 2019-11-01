import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import net.ducksmanager.util.CoverFlowActivity;
import net.ducksmanager.util.CoverFlowFileHandler;
import net.ducksmanager.whattheduck.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;


@RunWith(Parameterized.class)
@LargeTest
public class CoverSearchTest extends WtdTest {

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return parameterData();
    }

    @Before
    public void switchLocaleAndLogin() {
        whatTheDuckActivityRule.launchActivity(new Intent());
        switchLocale();
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS);
    }

    public CoverSearchTest(LocaleWithDefaultPublication currentLocale) {
        super(currentLocale);
    }

    @Test
    public void testCoverFlowResults() {
        CoverFlowFileHandler.mockedResource = mockServer.url("/internal/covers/2648").toString();

        Intent resultData = new Intent();
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);

        Matcher<Intent> expectedIntent = hasAction(MediaStore.ACTION_IMAGE_CAPTURE);
        Intents.init();
        intending(expectedIntent).respondWith(result);

        onView(withId(R.id.addToCollectionByPhotoButton))
            .perform(forceFloatingActionButtonsVisible(true))
            .perform(click());

        intended(expectedIntent);
        Intents.release();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ScreenshotTestRule.takeScreenshot("Cover search result", getActivityInstance(), getScreenshotPath());

        onView(allOf(withId(R.id.image), coverCurrentlyVisible())).perform(click());

        ScreenshotTestRule.takeScreenshot("Cover search result - add issue", getActivityInstance(), getScreenshotPath());
    }

    private static Matcher<Object> coverCurrentlyVisible() {
        return new BoundedMatcher<Object, ImageView>(ImageView.class) {
            @Override
            public boolean matchesSafely(final ImageView item) {
                return CoverFlowActivity.currentSuggestion.getCoverSearchIssue().getCoverFullUrl().equals(item.getTag()) && item.getVisibility() == View.VISIBLE;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Currently visible cover");
            }
        };
    }
}
