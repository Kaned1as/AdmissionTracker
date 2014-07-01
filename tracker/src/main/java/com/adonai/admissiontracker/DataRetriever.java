package com.adonai.admissiontracker;

import com.adonai.admissiontracker.entities.Favorite;
import com.adonai.admissiontracker.entities.Statistics;

import java.util.Date;

/**
 * @author Adonai
 */
public interface DataRetriever<T> {

    public static class StudentInfo {
        public Statistics stats;
        public Date admissionDate;
    }

    StudentInfo retrieveStatistics(Favorite fav, T data) throws Exception;

    BaseFragment getFragment();

}
