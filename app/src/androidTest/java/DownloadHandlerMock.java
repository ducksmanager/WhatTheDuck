import android.net.UrlQuerySanitizer;

import com.orhanobut.mockwebserverplus.Fixture;

import net.ducksmanager.whattheduck.WhatTheDuck;

import java.util.Arrays;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;


public class DownloadHandlerMock {
    public static final String TEST_USER = "demotestuser";
    public static final String TEST_PASS = "demotestpass";
    static Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            System.out.println("Mocking " + request.getRequestUrl());
            if (request.getRequestUrl().url().toString().contains("/dm-server/")) {
                return dispatchForDmServer(request);
            }
            else {
                return dispatchForDm(request);
            }
        }

        private MockResponse dispatchForDm(RecordedRequest request) {
            if (request.getRequestUrl().url().toString().endsWith(WhatTheDuck.DUCKSMANAGER_PAGE_WITH_REMOTE_URL)) {
                return new MockResponse().setBody(WtdTest.mockServer.url("/dm-server/"));
            }
            return new MockResponse().setStatus("404");
        }

        private MockResponse dispatchForDmServer(RecordedRequest request) {
            UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(request.getRequestUrl().url().toString());
            String username = sanitizer.getValue("pseudo_user");
            if (username == null) {
                if (request.getRequestUrl().pathSegments().contains("publications")) {
                    return new MockResponse().setBody(Fixture.parseFrom("dm-server/publications").body);
                }
                if (request.getRequestUrl().pathSegments().contains("issues")) {
                    return new MockResponse().setBody(Fixture.parseFrom("dm-server/issues").body);
                }
                if (request.getRequestUrl().pathSegments().containsAll(Arrays.asList("cover-id", "search"))) {
                    return new MockResponse().setBody(Fixture.parseFrom("dm-server/cover-search").body);
                }
                return new MockResponse().setStatus("500");
            }
            switch (username) {
                case TEST_USER:
                    if (sanitizer.getValue("mdp_user").equals(WhatTheDuck.toSHA1(TEST_PASS))) {
                        return new MockResponse().setBody(Fixture.parseFrom("dm/collection").body);
                    }
                    break;
            }
            return new MockResponse().setBody("0");
        }
    };
}
