import android.content.Intent;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilterTest extends WtdTest {

    public FilterTest() {
        super();
    }

    @Before
    public void login() {
        loginActivityRule.launchActivity(new Intent());
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS);
    }

    @Test
    public void testShowAddIssueDialog() {
        assertCurrentActivityIsInstanceOf(CountryList.class, true);
        onView(allOf(withId(R.id.addToCollectionBySelectionButton), forceFloatingActionButtonsVisible()))
            .perform(click());

        assertCurrentActivityIsInstanceOf(CountryList.class, true);

        onView(withId(R.id.filter))
            .perform(typeText("Fr"), closeSoftKeyboard());

        onView(withText("France"));
        onView(withText("Italy")).check(doesNotExist());


    }
}
