package net.ducksmanager.util;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.ORB;

class OpenCvTask {
    private Bitmap bitmap;
    private MatOfKeyPoint keypoints = new MatOfKeyPoint();
    private Mat descriptors = new Mat();

    OpenCvTask(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    boolean generateKeyPointsAndDescriptors() {
        Mat img = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, img);
        ORB orb;
        if (!img.empty()) {
            orb = ORB.create(2000, 1.02f, 100);
            orb.detectAndCompute(img, new Mat(), keypoints, descriptors);

            return !keypoints.empty() && !descriptors.empty();
        }
        return false;
    }

    MatOfKeyPoint getKeyPoints() {
        return keypoints;
    }


    Mat getDescriptors() {
        return descriptors;
    }
}
