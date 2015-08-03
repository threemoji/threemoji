package com.threemoji.threemoji.utility;

import android.util.Log;

import com.threemoji.threemoji.Threemoji;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class EmojiVector {
    public static final String TAG = EmojiVector.class.getSimpleName();
    private static JSONObject emojiLibrary = null;

    public Map<String, Integer> map = new HashMap<String, Integer>();

    public EmojiVector(String emoji1, String emoji2, String emoji3) {
        String[] emojis = {emoji1, emoji2, emoji3};
        try {
            for (String emoji : emojis) {
                for (String tag : getTagsFromJSON(emoji)) {
                    if (map.containsKey(tag)) {
                        int prevCount = map.get(tag);
                        map.put(tag, prevCount + 1);
                    } else {
                        map.put(tag, 1);
                    }
                }
            }

            Log.d(TAG, "Created vector: " + map.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error reading JSON: " + e.getMessage());
        }
    }

    public EmojiVector(HashMap<String, Integer> map) {
        this.map = map;
        Log.d(TAG, "Loaded vector: " + map.toString());
    }
    public int getMatchValue(EmojiVector other) {
        Map<String, Integer> otherMap = other.map;
        int score = 0;
        for (Map.Entry<String, Integer> component : map.entrySet()) {
            String tag = component.getKey();
            if (otherMap.containsKey(tag)) {
                score += component.getValue() * otherMap.get(tag);
            }
        }
        return score;
    }

    private static HashSet<String> getTagsFromJSON(String emoji) throws JSONException {
        HashSet<String> tags = new HashSet<String>();
        String key = emoji.substring(6).toUpperCase();

        if (emojiLibrary == null) {
            emojiLibrary = new JSONObject(loadJSONFromAsset());
        }
        JSONObject emojiData = emojiLibrary.getJSONObject(key);

        tags.add(emojiData.getString("category"));
        tags.add(emojiData.getString("name"));

        JSONArray keywords = emojiData.getJSONArray("keywords");
        JSONArray aliases = emojiData.getJSONArray("aliases");
        for (int i = 0; i < keywords.length(); i++) {
            tags.add(keywords.getString(i));
        }
        for (int i = 0; i < aliases.length(); i++) {
            tags.add(aliases.getString(i));
        }

        return tags;
    }

    private static String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = Threemoji.getContext().getAssets().open("emoji_by_unicode.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "Could not load asset");
            return null;
        }
        return json;
    }
}
