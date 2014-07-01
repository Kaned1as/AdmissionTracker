package com.adonai.admissiontracker;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Dialog fragment showing preferences editing form
 *
 * @author adonai
 */
public class AdmissionPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
