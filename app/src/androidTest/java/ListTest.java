import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.v7.widget.RecyclerView;

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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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

        Matcher<RecyclerView.ViewHolder> countryMatcher = getItemMatcher(currentLocale.getDefaultCountry().toLowerCase());
        onView((withId(R.id.itemList))).perform(scrollToHolder(countryMatcher), actionOnHolderItem(countryMatcher, click()));

        assertCurrentActivityIsInstanceOf(PublicationList.class, true);

        ScreenshotTestRule.takeScreenshot("Select issue - Publication list", getActivityInstance(), getScreenshotPath());
    }

    @Test
    public void testIssueList() {
        Matcher<RecyclerView.ViewHolder> countryMatcher = getItemMatcher(currentLocale.getDefaultCountry().toLowerCase());
        onView((withId(R.id.itemList))).perform(scrollToHolder(countryMatcher), actionOnHolderItem(countryMatcher, click()));

        Matcher<RecyclerView.ViewHolder> publicationMatcher = getItemMatcher(currentLocale.getDefaultPublication());
        onView((withId(R.id.itemList))).perform(scrollToHolder(publicationMatcher), actionOnHolderItem(publicationMatcher, click()));

        onView(allOf(withId(R.id.addToCollectionByPhotoButton), forceFloatingActionButtonsVisible())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.addToCollectionBySelectionButton), forceFloatingActionButtonsVisible())).check(matches(isDisplayed()));

        assertCurrentActivityIsInstanceOf(IssueList.class, true);
        ScreenshotTestRule.takeScreenshot("Collection - Issue list", getActivityInstance(), getScreenshotPath());
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
