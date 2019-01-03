package io.erhardt.dacol;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Waldemar Erhardt on 23.12.18.
 */
public class DacolMap {

    private Map<String, Object> map = new HashMap<>();
    public static DacolMap build() {
        return new DacolMap();
    }

    public DacolMap put(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    public Map<String, Object> getMap() {
        return this.map;
    }


    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this.map);
    }

    public static Map<String, String> buildMap(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}

