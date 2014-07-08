package com.adonai.admissiontracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.adonai.admissiontracker.entities.Favorite;
import com.jjoe64.graphview.LineGraphView;

/**
 * Created by adonai on 07.07.14.
 */
public class StatisticsFragment extends BaseFragment {

    private LineGraphView mLineGraph;
    private Spinner mStatisticsSpinner;

    public static StatisticsFragment forFavorite(Favorite selected) {
        return new StatisticsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.statistics_fragment, container, false);

        mLineGraph = (LineGraphView) rootView.findViewById(R.id.statistics_graph);
        mStatisticsSpinner = (Spinner) rootView.findViewById(R.id.statistics_mode_spinner);
        mStatisticsSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.tall_list_item, getResources().getStringArray(R.array.statistics_type_array)));

        return rootView;
    }
}
