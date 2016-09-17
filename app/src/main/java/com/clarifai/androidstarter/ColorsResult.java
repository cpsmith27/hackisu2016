package com.clarifai.androidstarter;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chris on 9/17/16.
 */
public class ColorsResult {
    JSONObject json;
    JSONArray resultsArray;
    JSONObject results;
    JSONArray colorsArray;
    ArrayList<Color> colors;

    ColorsResult(String response) {
        try {
            colors = new ArrayList<>();
            json = new JSONObject(response);
            Log.d("custom", json.toString());
            if ("OK".equals(json.getString("status_code"))) {
                resultsArray = json.getJSONArray("results");
                results = resultsArray.getJSONObject(0);
                colorsArray = results.getJSONArray("colors");
                for(int i = 0; i < colorsArray.length(); i++) {
                    colors.add(new Color(colorsArray.getJSONObject(i)));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
