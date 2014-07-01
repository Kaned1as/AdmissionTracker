package com.adonai.admissiontracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Preference activity responsible for user settings handling
 * For now just loads single preference fragment
 *
 * @author Adonai
 */
public class AdmissionPreferenceActivity extends PreferenceActivity {

    AdmissionPreferenceFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preference_flow);

        mFragment = (AdmissionPreferenceFragment) getFragmentManager().findFragmentById(R.id.preference_fragment);
    }
}
