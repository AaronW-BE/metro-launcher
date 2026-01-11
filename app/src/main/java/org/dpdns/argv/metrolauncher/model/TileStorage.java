package org.dpdns.argv.metrolauncher.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TileStorage {

    private static final String PREF = "metro_tiles";
    private static final String KEY = "pinned";

    public static void save(Context c, List<PinnedTile> tiles) {
        JSONArray arr = new JSONArray();
        try {
            for (PinnedTile t : tiles) {
                JSONObject o = new JSONObject();
                o.put("pkg", t.packageName);
                o.put("cls", t.className);
                o.put("label", t.label);
                arr.put(o);
            }
        } catch (JSONException ignored) {}

        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, arr.toString())
                .apply();
    }

    public static List<PinnedTile> load(Context c) {
        String json = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY, "[]");

        List<PinnedTile> result = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                PinnedTile t = new PinnedTile();
                t.packageName = o.getString("pkg");
                t.className = o.getString("cls");
                t.label = o.getString("label");

                result.add(t);
            }
        } catch (JSONException ignored) {}

        return result;
    }
}
