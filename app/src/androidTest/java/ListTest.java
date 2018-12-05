import net.ducksmanager.util.ReleaseNotes;
import net.ducksmanager.util.Settings;
import net.ducksmanager.whattheduck.IssueList;
import net.ducksmanager.whattheduck.ItemAdapter;
import net.ducksmanager.whattheduck.PublicationList;
import net.ducksmanager.whattheduck.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@RunWith(Parameterized.class)
@LargeTest
public class ListTest extends WtdTest {
    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return parameterData();
    }

    public ListTest(LocaleWithDefaultPublication currentLocale) {
        super(currentLocale);
    }

    @Before
    public void switchLocaleAndLogin() {
        switchLocale();
        overwriteSettingsAndHideMessages(
            Settings.MESSAGE_KEY_WELCOME,
            Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW,
            Settings.MESSAGE_KEY_DATA_CONSUMPTION,
            ReleaseNotes.current.getMessageId()
        );
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS);
    }

    @Before
    public void resetDownloadMockState() {
        DownloadHandlerMock.state.remove("server_offline");
    }

    @Test
    public void testPublicationList() {
        onView(allOf(withId(R.id.addToCollectionBySelectionButton), forceFloatingActionButtonsVisible()))
            .perform(click());

        goToPublicationListView(currentLocale.getDefaultCountry());
        assertCurrentActivityIsInstanceOf(PublicationList.class, true);

        ScreenshotTestRule.takeScreenshot("Select issue - Publication list", getActivityInstance(), getScreenshotPath());
    }

    @Test
    public void testIssueList() {
        goToPublicationListView(currentLocale.getDefaultCountry());
        goToIssueListView(currentLocale.getDefaultPublication());

        assertCurrentActivityIsInstanceOf(IssueList.class, true);

        onView(allOf(withId(R.id.addToCollectionByPhotoButton), forceFloatingActionButtonsVisible())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.addToCollectionBySelectionButton), forceFloatingActionButtonsVisible())).check(matches(isDisplayed()));

        ScreenshotTestRule.takeScreenshot("Collection - Issue list", getActivityInstance(), getScreenshotPath());
    }

    @Test
    public void testIssueListEdgeView() {
        goToPublicationListView("fr");
        goToIssueListView("fr/DDD");

        onView(withId(R.id.switchView)).perform(click());

        assertCurrentActivityIsInstanceOf(IssueList.class, true);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ScreenshotTestRule.takeScreenshot("Collection - Issue list - edge view", getActivityInstance(), getScreenshotPath());
    }

    private void goToPublicationListView(String countryCode) {
        Matcher<RecyclerView.ViewHolder> countryMatcher = getItemMatcher(countryCode.toLowerCase());
        onView(withId(R.id.itemList)).perform(scrollToHolder(countryMatcher), actionOnHolderItem(countryMatcher, click()));
    }

    private void goToIssueListView(String publicationCode) {
        Matcher<RecyclerView.ViewHolder> publicationMatcher = getItemMatcher(publicationCode);
        onView(withId(R.id.itemList)).perform(scrollToHolder(publicationMatcher), actionOnHolderItem(publicationMatcher, click()));
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
}
