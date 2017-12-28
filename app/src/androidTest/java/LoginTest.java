import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest extends WtdTest {

    @Test
    public void testMockEnabled() {
        String result = new DownloadHandlerMock().getPage("WhatTheDuck_server.php");
        Assert.assertEquals(result, "http://dm-server-mock");
    }

    @Test
    public void testLogin() {
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS);

        assertCurrentActivityIsInstanceOf(CountryList.class, true);
    }

    @Test
    public void testLoginInvalidCredentials() {
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS + "_invalid");

        assertCurrentActivityIsInstanceOf(CountryList.class, false);
        assertToastShown(R.string.input_error__invalid_credentials);
    }
}
