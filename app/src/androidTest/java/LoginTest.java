import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.koushikdutta.ion.Ion;

import junit.framework.Assert;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest extends WtdTest {

    @BeforeClass
    public static void initDownloadHelper() {
        WtdTest.initMockServers();
    }

    @Test
    public void testMockEnabled() throws IOException {
        Ion
            .with(getActivityInstance().getApplicationContext())
            .load(mockServers.get("dm").url("WhatTheDuck_server.php").toString())
            .asString()
            .setCallback((e, result) ->
                Assert.assertEquals(result, "http://dm-server-mock")
            );
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
