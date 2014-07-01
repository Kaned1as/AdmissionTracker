package com.adonai.admissiontracker;

import com.adonai.admissiontracker.entities.Favorite;

import static com.adonai.admissiontracker.Constants.Universities;

/**
 * Created by adonai on 01.07.14.
 */
public class DataRetrieverFactory {

    public static DataRetriever newInstance(Favorite fav) {
        final Universities university = Universities.values()[fav.getParentInstitution()];
        switch (university) {
            case SPBU:
                return ShowSpbuDataFragment.forFavorite(fav);
            default:
                return null;
        }
    }

}
