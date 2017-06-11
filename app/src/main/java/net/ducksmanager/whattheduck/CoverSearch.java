package net.ducksmanager.whattheduck;

import java.util.HashMap;

public class CoverSearch extends RetrieveTask {

    public static List cls;

    public CoverSearch(List cls, Integer imageResource) {
        super("/cover-id/search", R.id.progressBarConnection, false, buildFileList(imageResource));
        CoverSearch.cls = cls;
    }

    private static HashMap<String, Integer> buildFileList(Integer imageResource) {
        HashMap<String, Integer> files = new HashMap<>();
        files.put("wtd_jpg", imageResource);
        return files;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting cover search : " + System.currentTimeMillis());
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        System.out.println("Ending cover search : " + System.currentTimeMillis());
    }
}