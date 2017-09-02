import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class ScreenshotTestRule implements MethodRule {

    private static final String SCREENSHOTS_PATH = "test-screenshots";

    public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } catch (Throwable t) {
                    captureScreenshot(frameworkMethod.getName());
                    throw t;
                }
            }

            void captureScreenshot(String fileName) {
                takeScreenshot(fileName, WtdTest.getActivityInstance());
            }
        };
    }

    private static void takeScreenshot(String name, Activity activity)
    {
        View scrView = activity.getWindow().getDecorView().getRootView();
        scrView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(scrView.getDrawingCache());
        scrView.setDrawingCacheEnabled(false);

        OutputStream out = null;
        File imagePath = new File(activity.getFilesDir(), SCREENSHOTS_PATH);
        imagePath.mkdirs();
        File imageFile = new File(imagePath, name + ".png");

        try {
            out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}