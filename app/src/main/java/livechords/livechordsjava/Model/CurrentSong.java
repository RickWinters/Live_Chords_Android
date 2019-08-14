package livechords.livechordsjava.Model;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CurrentSong {
    private static final String TAG = "MYDEBUF_CurrentSong";
    private String artist;
    private String title;
    private HashMap previousMap;


    //GENERATORS
    public CurrentSong() {
        this.artist = "Flogging_Molly";
        this.title = "Drunken_Lullabies";
    }

    public CurrentSong(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    public void ParseJson(String reply) {
        Log.d(TAG, "ParseJson() called with: reply = [" + reply + "]");
        HashMap<String, Object> replyMap = GetMap(reply);
        HashMap itemMap = (HashMap) replyMap.get("item");
        title = (String) itemMap.get("name");
        ArrayList artists = (ArrayList) itemMap.get("artists");
        HashMap artistMap = (HashMap) artists.get(0);
        artist = (String) artistMap.get("name");
        Log.d(TAG, "ParseJson() returned: " + artist + " - " + title);
    }

    //FUNCTINOS
    public HashMap<String, Object> GetMap(String reply) {
        //Log.d(TAG, "GetMap() called with: reply = [" + reply + "]");

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
                            map.put(key, GetMap(value.toString()));
                        } else if (value.isJsonArray() && value.toString().contains(":")) {

                            List<HashMap<String, Object>> list = new ArrayList<>();
                            JsonArray array = value.getAsJsonArray();
                            if (null != array) {
                                for (JsonElement element : array) {
                                    list.add(GetMap(element.toString()));
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
            previousMap = map;
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return previousMap;
        }
    }

    //GETTERS AND SETTERS
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Artist = " + artist + " - title = " + title;
    }
}
