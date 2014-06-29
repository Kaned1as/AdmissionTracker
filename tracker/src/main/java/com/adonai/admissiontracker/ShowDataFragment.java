package com.adonai.admissiontracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adonai.admissiontracker.entities.Favorite;

/**
 * Created by adonai on 27.06.14.
 */
public class ShowDataFragment extends BaseFragment {

    private Favorite mFavorite;

    public static ShowDataFragment forData(Favorite data) {
        final ShowDataFragment result = new ShowDataFragment();
        result.mFavorite = data;
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.show_data_fragment, container, false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mProgressDialog.show();
        getMainActivity().getService().retrievePage();
    }
}
