package com.adonai.admissiontracker;

/**
 * Created by adonai on 01.07.14.
 */
public class DataRetrieverFactory {

    public static DataRetriever newInstance(Constants.University inst) {
        switch (inst) {
            case SPBU:
                return new ShowSpbuDataFragment();
            case SPB_GMU:
            case ITMO:
                return new ShowCommonDataFragment();
            default:
                return null;
        }
    }
}
