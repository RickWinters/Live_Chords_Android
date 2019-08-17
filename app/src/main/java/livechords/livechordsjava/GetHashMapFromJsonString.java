package livechords.livechordsjava;

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

public class GetHashMapFromJsonString {
    public static HashMap<String, Object> GetMap(String reply) {
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
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
