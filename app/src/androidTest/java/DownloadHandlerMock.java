import android.net.UrlQuerySanitizer;

import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DownloadHandlerMock implements RetrieveTask.DownloadHandler {
    public static final String TEST_USER = "demotestuser";
    public static final String TEST_PASS = "demotestpass";

    @Override
    public String getPage(String url) {
        if (url.endsWith(WhatTheDuck.DUCKSMANAGER_PAGE_WITH_REMOTE_URL)) {
            return "http://dm-server-mock";
        }
        UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(url);
        switch(sanitizer.getValue("pseudo_user")) {
            case TEST_USER:
                if (sanitizer.getValue("mdp_user").equals(WhatTheDuck.toSHA1(TEST_PASS))) {
                    JSONObject result = new JSONObject();
                    try {
                        result
                            .put("numeros", new JSONObject()
                                .put("es/BCB", new JSONArray()
                                    .put(new JSONObject().put("Numero", "1").put("Etat", "bon"))
                                    .put(new JSONObject().put("Numero", "3").put("Etat", "mauvais"))
                                )
                                .put("fr/DDD", new JSONArray()
                                    .put(new JSONObject().put("Numero", "2").put("Etat", "moyen"))
                                    .put(new JSONObject().put("Numero", "4").put("Etat", "moyen"))
                                )
                            )
                            .put("static", new JSONObject()
                                .put("magazines", new JSONObject()
                                    .put("es/BBB", "Biblioteca Carl Barks")
                                    .put("fr/DDD", "La dynastie Donald Duck - Int\u00e9grale Carl Barks")
                                )
                                .put("pays", new JSONObject()
                                    .put("es", "Spain")
                                    .put("fr", "France")
                                )
                            );

                        return result.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }
        return "0";
    }
}