import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.view.View;
import android.widget.ImageView;

import net.ducksmanager.util.CoverFlowActivity;
import net.ducksmanager.util.CoverFlowFileHandler;
import net.ducksmanager.util.ReleaseNotes;
import net.ducksmanager.util.Settings;
import net.ducksmanager.whattheduck.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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
        switchLocale();
        overwriteSettingsAndHideMessages(Settings.MESSAGE_KEY_WELCOME, ReleaseNotes.current.getMessageId());
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

        onView(allOf(withId(R.id.addToCollectionByPhotoButton), forceFloatingActionButtonsVisible())).perform(click());

        intended(expectedIntent);
        Intents.release();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ScreenshotTestRule.takeScreenshot("Cover search result", getActivityInstance(), getScreenshotPath());

        onView(allOf(withId(R.id.image), coverCurrentlyVisible()))
            .perform(click());

        ScreenshotTestRule.takeScreenshot("Cover search result - add issue", getActivityInstance(), getScreenshotPath());
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
