

import android.app.Activity
import android.graphics.Bitmap
import android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import java.io.*

class ScreenshotTestRule : MethodRule {

    override fun apply(statement: Statement, frameworkMethod: FrameworkMethod, o: Any): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    statement.evaluate()
                } catch (t: Throwable) {
                    captureScreenshot(frameworkMethod.name)
                    throw t
                }

            }

            fun captureScreenshot(fileName: String) {
                takeScreenshot(fileName, WtdTest.activityInstance, SCREENSHOTS_PATH)
            }
        }
    }

    companion object {

        private val SCREENSHOTS_PATH = "test-screenshots"
        val SCREENSHOTS_PATH_SHOWCASE = "test-screenshots/showcase"

        fun takeScreenshot(name: String, activity: Activity, path: String) {
            val scrView = activity.window.decorView.rootView

            object : Thread() {
                override fun run() {
                    try {
                        runOnUiThread {
                            scrView.isDrawingCacheEnabled = true
                            val bitmap = Bitmap.createBitmap(scrView.drawingCache)
                            scrView.isDrawingCacheEnabled = false

                            var out: OutputStream? = null
                            val imagePath = File(activity.filesDir, path)
                            imagePath.mkdirs()
                            val imageFile = File(imagePath, "$name.png")

                            try {
                                out = FileOutputStream(imageFile)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                                out.flush()
                            } catch (e: FileNotFoundException) {
                                System.err.println(e.message)
                            } catch (e: IOException) {
                                System.err.println(e.message)
                            } finally {
                                try {
                                    out?.close()
                                } catch (e: Exception) {
                                    System.err.println(e.message)
                                }

                            }
                        }
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }

                }
            }.run()
        }
    }
}