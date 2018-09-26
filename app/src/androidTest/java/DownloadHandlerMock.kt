import android.net.UrlQuerySanitizer
import android.text.TextUtils
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.WhatTheDuck
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.Okio
import java.io.IOException
import java.io.InputStream
import java.util.*


internal object DownloadHandlerMock {
    val TEST_USER = "demotestuser"
    val TEST_PASS = "demotestpass"

    val state = HashMap<String, Any>()

    val dispatcher: Dispatcher = object : Dispatcher() {

        override fun dispatch(request: RecordedRequest): MockResponse {
            println("Mocking " + request.path)
            return if (request.path.contains("/internal/")) {
                dispatchForInternal(request)
            } else if (request.path.contains("/dm-server/")) {
                dispatchForDmServer(request)
            } else if (request.path.contains("/edges/")) {
                dispatchForEdges(request)
            } else {
                dispatchForDm(request)
            }
        }

        // Mocks that are internal to tests (photo mocks for instance)
        private fun dispatchForInternal(request: RecordedRequest): MockResponse {
            val parts = Arrays.asList<String>(*request.path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            return if (parts.contains("covers")) {
                MockResponse().setBody(getImageFixture("covers/" + parts[parts.size - 1]))
            } else MockResponse().setStatus("404")
        }

        private fun dispatchForDm(request: RecordedRequest): MockResponse {
            return if (request.path.endsWith(WhatTheDuck.DUCKSMANAGER_PAGE_WITH_REMOTE_URL)) {
                MockResponse().setBody(WtdTest.mockServer.url("/dm-server/").toString())
            } else MockResponse().setStatus("404")
        }

        private fun dispatchForEdges(request: RecordedRequest): MockResponse {
            val parts = Arrays.asList<String>(*request.path.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray())
            return MockResponse().setBody(getImageFixture(
                    "edges/" + TextUtils.join("/", parts.subList(4, parts.size)), null))
        }

        private fun dispatchForDmServer(request: RecordedRequest): MockResponse {
            val sanitizer = UrlQuerySanitizer(request.path)
            val username = sanitizer.getValue("pseudo_user")
            if (username == null) {
                val parts = Arrays.asList<String>(*request.path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                if (parts.contains("countries")) {
                    return MockResponse().setBody(getLocalizedJsonFixture("dm-server/countries"))
                }
                if (parts.contains("publications")) {
                    return MockResponse().setBody(getLocalizedJsonFixture("dm-server/publications"))
                }
                if (parts.contains("issues")) {
                    return MockResponse().setBody(getLocalizedJsonFixture("dm-server/issues"))
                }
                if (parts.containsAll(Arrays.asList("cover-id", "search"))) {
                    return MockResponse().setBody(getJsonFixture("dm-server/cover-search"))
                }
                return if (parts.containsAll(Arrays.asList("cover-id", "download"))) {
                    MockResponse().setBody(getImageFixture("covers/" + parts[parts.size - 1]))
                } else MockResponse().setStatus("500")

            }
            when (username) {
                TEST_USER -> {
                    if (sanitizer.hasParameter("ajouter_achat")) {
                        state["hasNewPurchase"] = true
                        return MockResponse().setBody("OK")
                    }
                    if (sanitizer.hasParameter("get_achats")) {
                        return MockResponse().setBody(getJsonFixture(if (state["hasNewPurchase"] as Boolean) "dm/purchasesWithNew" else "dm/purchases"))
                    }
                    if (sanitizer.getValue("mdp_user") == Settings.toSHA1(TEST_PASS)) {
                        return MockResponse().setBody(getLocalizedJsonFixture("dm/collection"))
                    }
                }
            }
            return MockResponse().setBody("0")
        }
    }

    init {
        state["hasNewPurchase"] = false
    }

    private fun getJsonFixture(name: String): String {
        val path = "fixtures/$name.json"

        val inputStream = openPathAsStream(path)
        return convertStreamToString(inputStream)
    }

    private fun getLocalizedJsonFixture(name: String): String {
        return getJsonFixture(name + "/" + WtdTest.currentLocale.locale.language)
    }

    private fun getImageFixture(name: String, extension: String? = "jpg"): Buffer {
        val buffer = Buffer()
        try {
            buffer.writeAll(Okio.source(openPathAsStream(String.format("fixtures/%s%s", name, if (extension == null) "" else ".$extension"))))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return buffer
    }

    private fun openPathAsStream(path: String): InputStream {
        val loader = Thread.currentThread().contextClassLoader

        return loader.getResourceAsStream(path)
                ?: throw IllegalStateException("Invalid path: $path")
    }

    private fun convertStreamToString(`is`: java.io.InputStream): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
