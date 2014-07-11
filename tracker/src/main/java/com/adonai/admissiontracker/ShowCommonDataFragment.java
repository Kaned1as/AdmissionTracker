package com.adonai.admissiontracker;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;
import com.adonai.admissiontracker.entities.Statistics;
import com.adonai.admissiontracker.views.DoubleTextView;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by adonai on 05.07.14.
 */
public class ShowCommonDataFragment extends AbstractShowDataFragment {

    private static final String TITLE_KEY = "page.title";               // MANDATORY
    private static final String INST_KEY = "parent.institution";        // MANDATORY
    private static final String URL_KEY = "page.url";                   // MANDATORY
    private static final String NAME_KEY = "favorite.name";
    private static final String MAX_BUDGET = "max.budget.number";

    private Elements mStudents = null;

    private ToggleButton mFavButton;
    private DoubleTextView mListNumber, mPriority, mPoints, mOriginalsAbove, mCopiesAbove, mReclaimedAbove;
    private DoubleTextView mLastTimestamp, mTotalReclaimed, mNeededPoints;

    private NameSelectorListener mNameSelectorListener = new NameSelectorListener();
    private FavoriteClickListener mFavClickListener = new FavoriteClickListener();
    private ShowStatisticsClickListener mStatClickListener = new ShowStatisticsClickListener();

    public static ShowCommonDataFragment forFavorite(Favorite data) {
        final ShowCommonDataFragment result = new ShowCommonDataFragment();
        final Bundle args = new Bundle();
        args.putInt(INST_KEY, data.getParentInstitution());
        args.putString(TITLE_KEY, data.getTitleRaw());
        args.putString(URL_KEY, data.getUrl());

        args.putInt(MAX_BUDGET, data.getMaxBudgetCount());
        args.putString(NAME_KEY, data.getName());
        result.setArguments(args);
        return result;
    }

    public static ShowCommonDataFragment forPage(Constants.University parent, String title, String url, int maxBudget) {
        final ShowCommonDataFragment result = new ShowCommonDataFragment();
        final Bundle args = new Bundle();
        args.putInt(INST_KEY, parent.ordinal());
        args.putString(TITLE_KEY, title);
        args.putString(URL_KEY, url);
        args.putInt(MAX_BUDGET, maxBudget);
        result.setArguments(args);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.show_data_common_fragment, container, false);

        mNameSelector = (Spinner) rootView.findViewById(R.id.name_spinner);
        mNameSelector.setOnItemSelectedListener(mNameSelectorListener);

        mShowStatistics = (Button) rootView.findViewById(R.id.show_statistics_button);
        mShowStatistics.setOnClickListener(mStatClickListener);

        mFavButton = (ToggleButton) rootView.findViewById(R.id.favorite_button);
        mFavButton.setOnCheckedChangeListener(mFavClickListener);

        mListNumber = (DoubleTextView) rootView.findViewById(R.id.list_number);
        mPriority = (DoubleTextView) rootView.findViewById(R.id.priority);
        mPoints = (DoubleTextView) rootView.findViewById(R.id.points);

        mOriginalsAbove = (DoubleTextView) rootView.findViewById(R.id.originals);
        mCopiesAbove = (DoubleTextView) rootView.findViewById(R.id.copies);
        mReclaimedAbove = (DoubleTextView) rootView.findViewById(R.id.reclaimed_above);

        mLastTimestamp = (DoubleTextView) rootView.findViewById(R.id.last_updated);
        mTotalReclaimed = (DoubleTextView) rootView.findViewById(R.id.total_reclaimed);
        mNeededPoints = (DoubleTextView) rootView.findViewById(R.id.needed_points);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mStudents == null) {
            mProgressDialog.show();
            getMainActivity().getService().retrievePage(getArguments().getString(URL_KEY), mHandler);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.GET_URL: // got our URL back
                final NetworkService.NetworkInfo ni = (NetworkService.NetworkInfo) msg.obj;

                mStudents = ni.content.select("tr:has(td:matches(\\d+))");
                if (mStudents.size() == 0) {
                    Toast.makeText(getActivity(), R.string.no_data_available, Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStack();
                } else  {
                    updateNames();
                    try {
                        if(getArguments().containsKey(NAME_KEY)) { // it's favorite from DB
                            final String name = getArguments().getString(NAME_KEY);
                            mNameSelector.setSelection(mStudents.indexOf(findRowWithName(mStudents, name)) + 1, true);
                            getArguments().remove(NAME_KEY);
                        }
                    } catch (ParseException e) {
                        Toast.makeText(getActivity(), R.string.name_not_found, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

        return super.handleMessage(msg);
    }

    private void updateNames() {
        final SpinnerAdapter nameAdapter = new NamesAdapter(getActivity(), mStudents);
        // preserve previously selected item index
        final int previousIndex = mNameSelector.getSelectedItemPosition();
        mNameSelector.setAdapter(nameAdapter);
        mNameSelector.setSelection(previousIndex);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.data_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                mProgressDialog.show();
                getMainActivity().getService().retrievePage(getArguments().getString(URL_KEY), mHandler);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public StudentInfo retrieveStatistics(Favorite fav, NetworkService.NetworkInfo data) throws Exception {
        final Elements tableBody = data.content.select("tr:has(td:matches(\\d+))");
        if (tableBody.isEmpty())
            return null;

        return retrieveStatistics(fav, tableBody);
    }

    @Override
    public BaseFragment getFragment() {
        return this;
    }

    private class NameSelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final Element row = (Element) parent.getAdapter().getItem(position);
            mFavButton.setVisibility(row != null ? View.VISIBLE : View.INVISIBLE);
            mShowStatistics.setVisibility(row != null ? View.VISIBLE : View.INVISIBLE);
            if(row != null) {
                // update grid
                final Favorite toPersist = createFavForStudent(position);
                try {
                    final StudentInfo stInfo = retrieveStatistics(toPersist, mStudents);
                    updateGrid(toPersist, stInfo);
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), R.string.wrong_data, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), R.string.wrong_page_format, Toast.LENGTH_SHORT).show();
                }

                // update favorite button state
                try {
                    final Favorite inDb = DatabaseFactory.getHelper().getFavoritesDao().queryForSameId(toPersist);

                    mFavButton.setOnCheckedChangeListener(null);
                    if(inDb != null) // уже присутствует в БД, помечаем выделенным
                        mFavButton.setChecked(true);
                    else // иначе снимаем выделение
                        mFavButton.setChecked(false);
                    mFavButton.setOnCheckedChangeListener(mFavClickListener);
                } catch (SQLException e) {
                    Log.e("DataShowFragment", "Error retrieving favorite!", e);
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }

            } else
                clearGrid();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private void updateGrid(Favorite fav, StudentInfo stInfo) throws ParseException {
        mListNumber.setText((mStudents.indexOf(findRowWithName(mStudents, fav.getName())) + 1) + "/" + stInfo.stats.getTotalSubmitted());
        mPriority.setText(fav.getPriority().toString());
        mPoints.setText(fav.getPoints().toString());
        mOriginalsAbove.setText(stInfo.stats.getOriginalsAbove().toString());
        mCopiesAbove.setText(stInfo.stats.getCopiesAbove().toString());
        mReclaimedAbove.setText("0");
        mLastTimestamp.setText(Constants.VIEW_FORMAT.format(stInfo.stats.getTimestamp()));
        mTotalReclaimed.setText("0");
        mNeededPoints.setText(stInfo.stats.getNeededPoints().toString());

        try {
            //PreferenceFlow.class.getName();
            // update stats in DB if it's needed
            final Favorite inDb = DatabaseFactory.getHelper().getFavoritesDao().queryForSameId(stInfo.stats.getParent());
            if(inDb != null) {
                final QueryBuilder<Statistics, Integer> qb = DatabaseFactory.getHelper().getStatDao().queryBuilder();
                qb.where().eq("parent_id", inDb).and().eq("timestamp", stInfo.stats.getTimestamp());
                if (qb.queryForFirst() == null) // we haven't this field in DB
                    DatabaseFactory.getHelper().getStatDao().create(stInfo.stats);
            }
        } catch (SQLException e) {
            Log.e("DataShowFragment", "Error creating statistics!", e);
            Toast.makeText(getActivity(), R.string.cannot_update_statistics, Toast.LENGTH_SHORT).show();
        }
    }

    private StudentInfo retrieveStatistics(Favorite fav, Elements data) throws ParseException {
        final StudentInfo result = new StudentInfo();

        final Statistics currentStatistics = new Statistics();
        currentStatistics.setParent(fav);

        final Element myRow = findRowWithName(data, fav.getName());
        final Elements myColumns = myRow.children();

        currentStatistics.setTotalSubmitted(data.size());
        currentStatistics.setTimestamp(new Date());

        int originalsAbove = 0;
        int copiesAbove = 0;
        final Queue<Integer> allPoints = new PriorityQueue<>(data.size(), Collections.reverseOrder());
        for(Element row : data) {
            final Elements columns = row.children();
            final int points = Integer.valueOf(columns.get(5).text());
            allPoints.offer(points);
            final boolean isOriginal = columns.get(6).text().equals("да");

            if(points > fav.getPoints()) {
                if (isOriginal)
                    originalsAbove++;
                else
                    copiesAbove++;
            }
        }

        int neededPoints = 0;
        int passed = fav.getMaxBudgetCount();
        while (allPoints.size() > 0 && passed > 0) {
            neededPoints = allPoints.poll();
            passed--;
        }

        currentStatistics.setCopiesAbove(copiesAbove);
        currentStatistics.setOriginalsAbove(originalsAbove);
        currentStatistics.setNeededPoints(neededPoints);

        result.stats = currentStatistics;

        return result;
    }

    private void clearGrid() {
        mListNumber.setText("");
        mPriority.setText("");
        //mPoints.setText();
        //mOriginalsAbove.setText();
        //mCopiesAbove.setText();
        //mReclaimedAbove.setText();
        mLastTimestamp.setText("");
        //mTotalReclaimed.setText();
        //mNeededPoints.setText();
    }

    /**
     * Creates favorite to store in DB for selected index
     * @param index index beginning from 1
     * @return favorite instance for selected student
     */
    protected Favorite createFavForStudent(int index) {
        if(index <= 0 || index > mStudents.size())
            throw new IllegalArgumentException("Unknown student index!");

        final Element row = mStudents.get(index - 1);
        final Elements columns = row.children();

        final Favorite toCreate = new Favorite(getArguments().getString(TITLE_KEY), getArguments().getString(URL_KEY));
        toCreate.setParentInstitution(getArguments().getInt(INST_KEY));
        toCreate.setName(extractNameForStudent(row));
        toCreate.setPriority(Integer.valueOf(columns.get(2).text()));
        toCreate.setMaxBudgetCount(getArguments().getInt(MAX_BUDGET));
        toCreate.setPoints(Integer.valueOf(columns.get(5).text()));

        return toCreate;
    }

    @Override
    protected String extractNameForStudent(Element row) {
        final Elements columns = row.children();
        return columns.get(1).text();
    }
}
