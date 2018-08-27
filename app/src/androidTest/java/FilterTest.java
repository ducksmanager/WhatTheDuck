import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilterTest extends WtdTest {

    public FilterTest() {
        super();
    }

    @BeforeClass
    public static void initDownloadHelper() {
        WtdTest.initMockServer();
    }

    @Before
    public void login() {
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
