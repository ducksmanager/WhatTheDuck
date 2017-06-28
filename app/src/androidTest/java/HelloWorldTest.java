import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelloWorldTest {

    private String mStringToBetyped;

    @Rule
    public ActivityTestRule<WhatTheDuck> mActivityRule = new ActivityTestRule<>(
        WhatTheDuck.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        mStringToBetyped = "Username";
    }

    @Test
    public void changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.username))
            .perform(typeText(mStringToBetyped), closeSoftKeyboard());
        onView(withId(R.id.login)).perform(click());
    }
}

