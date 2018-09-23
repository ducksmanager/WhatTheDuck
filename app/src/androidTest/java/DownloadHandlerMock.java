import android.net.UrlQuerySanitizer;
import android.text.TextUtils;

import net.ducksmanager.util.Settings;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.Okio;


class DownloadHandlerMock {
    public static final String TEST_USER = "demotestuser";
    public static final String TEST_PASS = "demotestpass";

    static final HashMap<String, Object> state = new HashMap<>();
    static {
        state.put("hasNewPurchase", false);
    }

    static final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            System.out.println("Mocking " + request.getPath());
            if (request.getPath().contains("/internal/")) {
                return dispatchForInternal(request);
            }
            else if (request.getPath().contains("/dm-server/")) {
                return dispatchForDmServer(request);
            }
            else if (request.getPath().contains("/edges/")) {
                return dispatchForEdges(request);
            }
            else {
                return dispatchForDm(request);
            }
        }

        // Mocks that are internal to tests (photo mocks for instance)
        private MockResponse dispatchForInternal(RecordedRequest request) {
            List<String> parts = Arrays.asList(request.getPath().split("/"));
            if (parts.contains("covers")) {
                return new MockResponse().setBody(getImageFixture("covers/" + parts.get(parts.size()-1)));
            }
            return new MockResponse().setStatus("404");
        }

        private MockResponse dispatchForDm(RecordedRequest request) {
            if (request.getPath().endsWith(WhatTheDuck.DUCKSMANAGER_PAGE_WITH_REMOTE_URL)) {
                return new MockResponse().setBody(WtdTest.mockServer.url("/dm-server/").toString());
            }
            return new MockResponse().setStatus("404");
        }
        private MockResponse dispatchForEdges(RecordedRequest request) {
            List<String> parts = Arrays.asList(request.getPath().split("/"));
            return new MockResponse().setBody(getImageFixture(
                "edges/" + TextUtils.join("/", parts.subList(4, parts.size())),
                null));
        }

        private MockResponse dispatchForDmServer(RecordedRequest request) {
            UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(request.getPath());
            String username = sanitizer.getValue("pseudo_user");
            if (username == null) {
                List<String> parts = Arrays.asList(request.getPath().split("/"));
                if (parts.contains("countries")) {
                    return new MockResponse().setBody(getLocalizedJsonFixture("dm-server/countries"));
                }
                if (parts.contains("publications")) {
                    return new MockResponse().setBody(getLocalizedJsonFixture("dm-server/publications"));
                }
                if (parts.contains("issues")) {
                    return new MockResponse().setBody(getLocalizedJsonFixture("dm-server/issues"));
                }
                if (parts.containsAll(Arrays.asList("cover-id", "search"))) {
                    return new MockResponse().setBody(getJsonFixture("dm-server/cover-search"));
                }
                if (parts.containsAll(Arrays.asList("cover-id", "download"))) {
                    return new MockResponse().setBody(getImageFixture("covers/" + parts.get(parts.size()-1)));
                }

                return new MockResponse().setStatus("500");
            }
            switch (username) {
                case TEST_USER:
                    if (sanitizer.hasParameter("ajouter_achat")) {
                        state.put("hasNewPurchase", true);
                        return new MockResponse().setBody("OK");
                    }
                    if (sanitizer.hasParameter("get_achats")) {
                        return new MockResponse().setBody(getJsonFixture((Boolean) state.get("hasNewPurchase") ? "dm/purchasesWithNew" : "dm/purchases"));
                    }
                    if (sanitizer.getValue("mdp_user").equals(Settings.toSHA1(TEST_PASS))) {
                        return new MockResponse().setBody(getLocalizedJsonFixture("dm/collection"));
                    }
                    break;
            }
            return new MockResponse().setBody("0");
        }
    };

    private static String getJsonFixture(String name) {
        String path="fixtures/" + name + ".json";

        InputStream inputStream = openPathAsStream(path);
        return convertStreamToString(inputStream);
    }

    private static String getLocalizedJsonFixture(String name) {
        return getJsonFixture(name + "/" + WtdTest.currentLocale.getLocale().getLanguage());
    }

    private static Buffer getImageFixture(String name, String extension) {
        Buffer buffer = new Buffer();
        try {
            buffer.writeAll(Okio.source(openPathAsStream(String.format("fixtures/%s%s", name, extension == null ? "" : "." + extension))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    private static Buffer getImageFixture(String name) {
        return getImageFixture(name, "jpg");
    }

    private static InputStream openPathAsStream(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = loader.getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalStateException("Invalid path: " + path);
        }

        return inputStream;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
