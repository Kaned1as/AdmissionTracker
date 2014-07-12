package com.adonai.admissiontracker;

import com.adonai.admissiontracker.entities.Favorite;

/**
 * Created by adonai on 01.07.14.
 */
public class DataRetrieverFactory {

    private final Constants.University university;

    public DataRetrieverFactory(Constants.University university) {
        this.university = university;
    }

    public DataRetriever newInstance() {
        switch (university) {
            case SPBU:
                return new ShowSpbuDataFragment();
            case SPB_GMU:
            case ITMO:
                return new ShowCommonDataFragment();
            default:
                return null;
        }
    }

    public AbstractShowDataFragment forFavorite(Favorite selected) {
        switch (university) {
            case SPBU:
                return ShowSpbuDataFragment.forFavorite(selected);
            case SPB_GMU:
            case ITMO:
                return ShowCommonDataFragment.forFavorite(selected);
            default:
                return null;
        }
    }
}
