import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import net.ducksmanager.whattheduck.AddIssue;
import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.IssueList;
import net.ducksmanager.whattheduck.PublicationList;
import net.ducksmanager.whattheduck.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddIssueTest extends WtdTest {

    @BeforeClass
    public static void initDownloadHelper() {
        WtdTest.initMockServers();
    }

    @Before
    public void login() {
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS);
    }

    @Test
    public void testShowAddIssueDialog() {
        assertCurrentActivityIsInstanceOf(CountryList.class, true);
        onView(withText("France")).perform(ViewActions.click());

        assertCurrentActivityIsInstanceOf(PublicationList.class, true);
        onView(withText(containsString("dynastie"))).perform(ViewActions.click());

        assertCurrentActivityIsInstanceOf(IssueList.class, true);
        onView(withId(R.id.onlyInCollectionSwitch)).perform(ViewActions.click());
        onView(withText(containsString("1"))).perform(ViewActions.click());

        assertCurrentActivityIsInstanceOf(AddIssue.class, true);

    }
}
