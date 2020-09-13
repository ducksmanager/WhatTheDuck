import WtdTest.Companion.mockServer
import android.text.TextUtils
import net.ducksmanager.util.Settings.Companion.toSHA1
import okhttp3.Headers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.source
import java.io.IOException
import java.io.InputStream
import java.util.*

internal object DownloadHandlerMock {
    const val TEST_USER = "demotestuser"
    const val TEST_PASS = "demotestpass"
    val state = HashMap<String, Any>()

    init {
        state["hasNewPurchase"] = false
    }

    val dispatcher: Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val path = request.path
            println("Mocking $path")
            return when {
                path!!.contains("/internal/") -> dispatchForCover(path)
                path.contains("/edges/") -> dispatchForEdges(path)
                else -> dispatchForDmServer(path, request.headers)
            }
        }

        // Mocks that are internal to tests (photo mocks for instance)
        private fun dispatchForCover(path: String?): MockResponse {
            val parts = listOf(*path!!.split("/").toTypedArray())
            return try {
                MockResponse().setBody(getImageFixture("covers/" + parts[parts.size - 1]))
            } catch (e: IOException) {
                MockResponse().setResponseCode(404)
            }
        }

        private fun dispatchForEdges(path: String?): MockResponse {
            val parts = listOf(*path!!.split("/").toTypedArray())
            return try {
                MockResponse().setBody(getImageFixture(
                    "edges/" + TextUtils.join("/", parts.subList(4, parts.size)),
                    null))
            } catch (e: IOException) {
                MockResponse().setResponseCode(404)
            }
        }

        private fun dispatchForDmServer(path: String?, headers: Headers): MockResponse {
            if (headers["x-dm-pass"] != null && headers["x-dm-pass"] != toSHA1(TEST_PASS, null)) {
                return MockResponse().setResponseCode(401)
            }
            return if (path!!.contains("cover-id/download")) {
                dispatchForCover(path)
            } else MockResponse().setBody(getJsonFixture("dm-server/" + path.replaceFirst("/".toRegex(), "")))
        }
    }

    private fun getJsonFixture(name: String): String {
        val json = convertStreamToString(openPathAsStream("fixtures/$name.json"))
        return json.replace("http://mocked/", mockServer!!.url("/internal/covers/").toString(), true)
    }

    @Throws(IOException::class)
    private fun getImageFixture(name: String, extension: String?): Buffer {
        val buffer = Buffer()
        buffer.writeAll(
            openPathAsStream(
                String.format("fixtures/%s%s", name, if (extension == null) "" else ".$extension")
            ).source())
        return buffer
    }

    @Throws(IOException::class)
    private fun getImageFixture(name: String): Buffer = getImageFixture(name, "jpg")

    private fun openPathAsStream(path: String): InputStream {
        var path2=path.replace("_.json", "empty.json")
        val loader = Thread.currentThread().contextClassLoader
        var resourceAsStream = loader!!.getResourceAsStream(path2)
        if (resourceAsStream == null) {
            path2=path2.replace(".json", "/${WtdTest.currentLocale!!.defaultCountry}.json")
            resourceAsStream = loader.getResourceAsStream(path2)
        }
        return resourceAsStream
            ?: throw IllegalStateException("Invalid path: $path2")
    }

    private fun convertStreamToString(inputStream: InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}