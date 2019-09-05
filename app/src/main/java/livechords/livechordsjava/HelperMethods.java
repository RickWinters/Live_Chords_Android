package livechords.livechordsjava;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HelperMethods {
    private static final String TAG = "MYDEBUG_HelperMethods";

    public static HashMap<String, Object> GetJSONHashMap(String reply) {
        //Log.d(TAG, "GetJSONHashMap() called with: reply = [" + reply + "]");

        try {
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reply);
            Set<Map.Entry<String, JsonElement>> set = object.entrySet();
            Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
            HashMap<String, Object> map = new HashMap<>();

            while (iterator.hasNext()) {

                Map.Entry<String, JsonElement> entry = iterator.next();
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (null != value) {
                    if (!value.isJsonPrimitive()) {
                        if (value.isJsonObject()) {
                            map.put(key, GetJSONHashMap(value.toString()));
                        } else if (value.isJsonArray() && value.toString().contains(":")) {

                            List<HashMap<String, Object>> list = new ArrayList<>();
                            JsonArray array = value.getAsJsonArray();
                            if (null != array) {
                                for (JsonElement element : array) {
                                    list.add(GetJSONHashMap(element.toString()));
                                }
                                map.put(key, list);
                            }
                        } else if (value.isJsonArray() && !value.toString().contains(":")) {
                            map.put(key, value.getAsJsonArray());
                        }
                    } else {
                        map.put(key, value.getAsString());
                    }
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getResponse(URL url){
        Log.d(TAG, "getResponse() called with: url = [" + url + "]");
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setReadTimeout(5000);
            //connection.getInputStream();

            int status = connection.getResponseCode();
            //Log.i("ServerConnection", "Status = "+status);

            BufferedReader reader;
            String line;
            if (status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
            }

            //Log.i("ServerConnection", response.toString());
            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static String getResponse(URL url, String accestoken) {
        Log.d(TAG, "GetReply() called with: url = [" + url + "], accestoken = [" + accestoken + "]");
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Authorization", "Bearer " + accestoken);
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.getInputStream();
            int status = connection.getResponseCode();
            Log.i(TAG, "Status = "+status);

            //HANDLE ERRORS
            BufferedReader reader;
            String line;
            if (status == 204){
                response.append("No song playing");
            }

            else if (status == 401){
                response.append(SpotifyConnector.ACCESTOKENEXPIRED);
            }

            //Handle correct content
            else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
            //Log.d(TAG, "UpdateCurrentSong() returned: " + response.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "GetReply() returned: " + response.toString());
        return response.toString();
    }

    public static String[] cleanArtistTitleString(String artist, String title){
        title = title.replace("acoustic", "");
        title = title.replace("Acoustic", "");
        title = title.replace("-", "");
        title = title.replace("(", "");
        title = title.replace(")", "");
        title = title.replace("[","");
        title = title.replace("]","");
        title = title.replace("_live", "");
        title = title.replace("_Live", "");
        title = title.replace("\'", "");
        title = title.replace("Version", "");
        title = title.replace("version", "");
        title = title.replace(".", "");
        title = title.replace("é", "e");
        title = title.replace("ê", "e");
        title = title.replace("mono", "");
        title = title.replace("Mono", "");
        artist = artist.replace("?", "");
        artist = artist.replace("!", "");
        artist = artist.replace("'", "");

        title = title.trim();
        artist = artist.trim();

        return new String[]{artist, title};
    }

    public static String[] cleanBrackets(String artist, String title){
        artist = artist.replace("["," ");
        artist = artist.replace("]"," ");
        artist = artist.replace("("," ");
        artist = artist.replace(")"," ");

        title = title.replace("["," ");
        title = title.replace("]"," ");
        title = title.replace("("," ");
        title = title.replace(")"," ");

        return new String[]{artist, title};
    }
}
