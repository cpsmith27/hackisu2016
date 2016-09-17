package com.clarifai.androidstarter;

import org.json.JSONObject;

/**
 * Created by chris on 9/17/16.
 */

public class Color {
    String hex;
    String name;
    double density;

    Color(JSONObject json) {
        try {
            hex = json.getString("hex");
            name = json.getJSONObject("w3c").getString("name");
            density = json.getDouble("density");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return name + " (" + (density * 100) + "%)";
    }
}