import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.Headers;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.Okio;

import static net.ducksmanager.util.Settings.toSHA1;


class DownloadHandlerMock {
    static final String TEST_USER = "demotestuser";
    static final String TEST_PASS = "demotestpass";

    static final HashMap<String, Object> state = new HashMap<>();
    static {
        state.put("hasNewPurchase", false);
    }

    static final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            String path = request.getPath();
            System.err.println("Mocking " + path);
            if (path.contains("/internal/")) {
                return dispatchForCover(path);
            }
            else if (path.contains("/edges/")) {
                return dispatchForEdges(path);
            }
            else {
                return dispatchForDmServer(path, request.getHeaders());
            }
        }

        // Mocks that are internal to tests (photo mocks for instance)
        private MockResponse dispatchForCover(String path) {
            List<String> parts = Arrays.asList(path.split("/"));
            try {
                return new MockResponse().setBody(getImageFixture("covers/" + parts.get(parts.size()-1)));
            }
            catch (IOException e) {
                return new MockResponse().setResponseCode(404);
            }
        }

        private MockResponse dispatchForEdges(String path) {
            List<String> parts = Arrays.asList(path.split("/"));
            try {
                return new MockResponse().setBody(getImageFixture(
                    "edges/" + TextUtils.join("/", parts.subList(4, parts.size())),
                    null));
            }
            catch (IOException e) {
                return new MockResponse().setResponseCode(404);
            }
        }

        private MockResponse dispatchForDmServer(String path, Headers headers) {

            if (headers.get("x-dm-pass") != null && !headers.get("x-dm-pass").equals(toSHA1(DownloadHandlerMock.TEST_PASS))) {
                return new MockResponse().setResponseCode(401);
            }
            if (path.contains("cover-id/download")) {
                return dispatchForCover(path);
            }
            return new MockResponse().setBody(getJsonFixture("dm-server/" + path.replaceFirst("/", "")));
        }
    };

    private static String getJsonFixture(String name) {
        String path="fixtures/" + name + ".json";

        InputStream inputStream = openPathAsStream(path);
        return convertStreamToString(inputStream);
    }

    private static Buffer getImageFixture(String name, String extension) throws IOException {
        Buffer buffer = new Buffer();
        buffer.writeAll(Okio.source(openPathAsStream(String.format("fixtures/%s%s", name, extension == null ? "" : "." + extension))));
        return buffer;
    }

    private static Buffer getImageFixture(String name) throws IOException {
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
