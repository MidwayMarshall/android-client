package com.podevs.android.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PrefLegacy {
    public static void putStringSet(SharedPreferences prefs, String key, final Collection<String> newVal) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, new JSONArray(newVal).toString()).commit();
    }

    public static Set<String> getStringSet(SharedPreferences prefs, String key) {
        final String str = prefs.getString(key, null);
        if (str == null) {
            return null;
        }
        try {
            final JSONArray jsonArray=new JSONArray(str);
            final Set<String> result=new HashSet<>();
            for(int i=0;i<jsonArray.length();++i)
                result.add(jsonArray.getString(i));
            return result;
        } catch(final JSONException e) {
            e.printStackTrace();
            prefs.edit().remove(key).commit();
        }
        return null;
    }
}