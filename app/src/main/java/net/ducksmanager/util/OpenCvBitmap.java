package net.ducksmanager.util;

import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.ORB;

import timber.log.Timber;

public class OpenCvBitmap {
    private Bitmap bitmap;
    private MatOfKeyPoint keypoints = new MatOfKeyPoint();
    private Mat descriptors = new Mat();

    OpenCvBitmap(Bitmap bitmap) {
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

    public JSONObject getDescriptorsAsJson(){
        JSONObject obj = new JSONObject();

        if(descriptors.isContinuous()){
            int cols = descriptors.cols();
            int rows = descriptors.rows();
            int elemSize = (int) descriptors.elemSize();

            byte[] data = new byte[cols * rows * elemSize];

            descriptors.get(0, 0, data);

            try {
                obj.put("rows", descriptors.rows());
                obj.put("cols", descriptors.cols());
                obj.put("type", descriptors.type());
                obj.put("data", new String(Base64.encode(data, Base64.DEFAULT)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Timber.e("OpenCV error : descriptors Mat is not continuous");
        }
        return obj;
    }

    public static Mat getDescriptorsFromString(String json){
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);

            int rows = Integer.parseInt((String) jsonObject.get("rows"));
            int cols = Integer.parseInt((String) jsonObject.get("cols"));
            int type = Integer.parseInt((String) jsonObject.get("type"));

            String dataString = (String) jsonObject.get("data");
            byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT);

            Mat mat = new Mat(rows, cols, type);
            mat.put(0, 0, data);

            return mat;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getKeyPointsAsJson() {
        JSONObject o = new JSONObject();

        int i=0;
        for (KeyPoint keyPoint : keypoints.toList()) {
            JSONObject keyPointObject = new JSONObject();
            try {
                keyPointObject.put("x", keyPoint.pt.x);
                keyPointObject.put("y", keyPoint.pt.y);
                keyPointObject.put("angle", keyPoint.angle);
//                keyPointObject.put("class_id", keyPoint.class_id); // -1
//                keyPointObject.put("octave", keyPoint.octave); // unused
//                keyPointObject.put("response", keyPoint.response); // unused
//                keyPointObject.put("size", keyPoint.size); // unused
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                o.put(Integer.toString(i++), keyPointObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return o;
    }
}
