package com.adonai.admissiontracker.entities;

import android.content.SharedPreferences;

import com.adonai.admissiontracker.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adonai on 27.06.14.
 */
public class FavoriteHolder {

    public final String title;
    public final String url;
    public String name;

    public FavoriteHolder(String title, String url) {
        this.title = title;
        this.url = url;
    }

    @Override
    public String toString() {
        return title;
    }

    public static void addFavorite(SharedPreferences prefs, FavoriteHolder toAdd) {
        final String existingPrefs = prefs.getString(Utils.FAVORITES_PREF, "");
        final String[] serializedFavs = existingPrefs.split(Utils.DELIMITER);
        final List<FavoriteHolder> existingFavs = new ArrayList<FavoriteHolder>(serializedFavs.length);
        for(final String fav : serializedFavs)
            if(!fav.isEmpty())
                existingFavs.add(new Gson().fromJson(fav, FavoriteHolder.class));

        for(final FavoriteHolder holder : existingFavs)
            if(holder.url.equals(toAdd.url))
                return; // already existing

        prefs.edit()
                .putString(Utils.FAVORITES_PREF, Utils.join(Arrays.asList(existingPrefs, new Gson().toJson(toAdd)), Utils.DELIMITER))
             .commit();
    }
}
