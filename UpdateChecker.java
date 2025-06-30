package com.shop;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.shop.utils.ConfigUtil;
import org.json.JSONObject;

public class UpdateChecker {

    private static final String UPDATE_JSON_URL = ConfigUtil.getProperty("external.url") + "/update/update.json";

    public static JSONObject fetchUpdateInfo() throws Exception {
        URL url = new URL(UPDATE_JSON_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            StringBuilder json = new StringBuilder();
            int read;
            char[] buffer = new char[1024];
            while ((read = reader.read(buffer)) > 0) {
                json.append(buffer, 0, read);
            }
            return new JSONObject(json.toString());
        }
    }


    public static boolean isUpdateAvailable(String previousVersion, String serverVersion) {
        return previousVersion.compareTo(serverVersion) < 0;
    }
}

