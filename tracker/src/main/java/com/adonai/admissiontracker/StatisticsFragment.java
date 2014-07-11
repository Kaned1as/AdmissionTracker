package com.adonai.admissiontracker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;
import com.adonai.admissiontracker.entities.Statistics;
import com.j256.ormlite.stmt.QueryBuilder;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static android.view.ViewGroup.LayoutParams;
import static com.jjoe64.graphview.GraphViewSeries.*;

/**
 * Created by adonai on 07.07.14.
 */
public class StatisticsFragment extends BaseFragment {

    private static final String FAV_KEY = "favorite.key";       // MANDATORY

    private LineGraphView mLineGraph;
    private Spinner mStatisticsSpinner;
    private StatisticsModeSelectListener mStatisticsSelectListener = new StatisticsModeSelectListener();

    private List<Statistics> mStatistics;

    public static StatisticsFragment forFavorite(Favorite selected) {
        final StatisticsFragment result = new StatisticsFragment();
        final Bundle args = new Bundle();
        args.putString(FAV_KEY, selected.getTitle());
        result.setArguments(args);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.statistics_fragment, container, false);
        final LinearLayout holder = (LinearLayout) rootView.findViewById(R.id.statistics_holder);

        mLineGraph = new LineGraphView(getActivity(), getString(R.string.statistics));
        mLineGraph.setDrawBackground(true);
        mLineGraph.setCustomLabelFormatter(new CustomLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // transform number to time
                    return Constants.DDMM.format(new Date((long) value));
                } else
                    return String.valueOf((int) value);
            }
        });
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.addView(mLineGraph, lp);

        mStatisticsSpinner = (Spinner) rootView.findViewById(R.id.statistics_mode_spinner);
        mStatisticsSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.tall_list_item, getResources().getStringArray(R.array.statistics_type_array)));
        mStatisticsSpinner.setOnItemSelectedListener(mStatisticsSelectListener);

        try {
            final String favoriteId = getArguments().getString(FAV_KEY);
            final QueryBuilder<Statistics, Integer> qb = DatabaseFactory.getHelper().getStatDao().queryBuilder();
            mStatistics = qb.where().eq("parent_id", favoriteId).query();
            if(mStatistics.isEmpty()) {
                Toast.makeText(getActivity(), R.string.statistics_not_collected, Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }

        } catch (SQLException e) {
            Log.e("StatisticsFragment", "Error retrieving statistics!", e);
            Toast.makeText(getActivity(), R.string.cannot_load_statistics, Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();
        }

        return rootView;
    }

    private class StatisticsModeSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mLineGraph.removeAllSeries();

            if(mStatistics.isEmpty()) // нет статистики, делать нечего
                return;

            final GraphView.GraphViewData[] data = new GraphView.GraphViewData[mStatistics.size()];
            for(int i = 0; i < mStatistics.size(); ++i) {
                final Statistics one = mStatistics.get(i);
                final long x = one.getTimestamp().getTime();
                final Integer y;
                switch (position) {
                    case 0: // originals above
                        y = one.getOriginalsAbove();
                        break;
                    case 1: // copies above
                        y = one.getCopiesAbove();
                        break;
                    case 2: // reclaimed above
                        y = one.getReclaimedAbove();
                        break;
                    case 3: // total submitted
                        y = one.getTotalSubmitted();
                        break;
                    case 4: // reclaimed today
                        y = one.getReclaimedToday();
                        break;
                    case 5: // needed points
                        y = one.getNeededPoints();
                        break;
                    default:
                        y = 0;
                        break;
                }
                data[i] = new GraphView.GraphViewData(x, y != null ? y : 0);
            }
            final GraphViewSeries series = new GraphViewSeries(parent.getItemAtPosition(position).toString(), new GraphViewSeriesStyle(Color.rgb(90, 250, 00), 5), data);
            mLineGraph.addSeries(series);
            //final Statistics firstStat = mStatistics.get(0);
            //final Statistics lastStat = mStatistics.get(mStatistics.size() - 1);
            //mLineGraph.setViewPort(firstStat.getTimestamp().getTime(), lastStat.getTimestamp().getTime());
            mLineGraph.setScalable(true);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
