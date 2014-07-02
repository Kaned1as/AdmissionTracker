package com.adonai.admissiontracker;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by adonai on 02.07.14.
 */
public class CheckProPreference extends CheckBoxPreference {

    public CheckProPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CheckProPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckProPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.but_excuse);
                builder.setMessage(R.string.only_pro);
                builder.create().show();
            }
        });
    }
}
