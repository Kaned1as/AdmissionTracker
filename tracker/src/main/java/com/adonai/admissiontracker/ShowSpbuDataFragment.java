package com.adonai.admissiontracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.NumberPicker;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

import static com.adonai.admissiontracker.Constants.University.SPBU;

/**
 * Created by adonai on 27.06.14.
 */
public class ShowSpbuDataFragment extends AbstractShowDataFragment {

    private static final String NAME_KEY = "favorite.name";

    private long mLastUpdated;

    private int mBudgetCount;

    private Elements mStudents = null;

    private ToggleButton mFavButton;
    private DoubleTextView mListNumber, mPriority, mPoints, mOriginalsAbove, mCopiesAbove, mReclaimedAbove;
    private DoubleTextView mLastTimestamp, mTotalReclaimed, mNeededPoints;

    private NameSelectorListener mNameSelectorListener = new NameSelectorListener();
    private FavoriteClickListener mFavClickListener = new FavoriteClickListener();
    private ShowStatisticsClickListener mStatClickListener = new ShowStatisticsClickListener();

    public static ShowSpbuDataFragment forFavorite(Favorite data) {
        final ShowSpbuDataFragment result = new ShowSpbuDataFragment();
        final Bundle args = new Bundle();
        args.putString(TITLE_KEY, data.getTitleRaw());
        args.putString(URL_KEY, data.getUrl());
        args.putString(NAME_KEY, data.getName());
        result.setArguments(args);
        return result;
    }

    public static ShowSpbuDataFragment forPage(String title, String url) {
        final ShowSpbuDataFragment result = new ShowSpbuDataFragment();
        final Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(URL_KEY, url);
        result.setArguments(args);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.show_data_spbu_fragment, container, false);

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

                final Element tableBody = ni.content.select("tbody").first();
                if (tableBody == null) {
                    Toast.makeText(getActivity(), R.string.no_data_available, Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStack();
                } else if(mLastUpdated == 0 || mLastUpdated < ni.lastModified) {
                    mStudents = tableBody.children();
                    mLastUpdated = ni.lastModified;

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
                } else
                    Toast.makeText(getActivity(), R.string.no_updates_available, Toast.LENGTH_SHORT).show();
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

    private void updateGrid(Favorite fav, StudentInfo stInfo) throws ParseException {
        mListNumber.setText((mStudents.indexOf(findRowWithName(mStudents, fav.getName())) + 1) + "/" + stInfo.stats.getTotalSubmitted());
        mPriority.setText(fav.getPriority().toString());
        mPoints.setText(fav.getPoints().toString());
        mOriginalsAbove.setText(stInfo.stats.getOriginalsAbove().toString());
        mCopiesAbove.setText(stInfo.stats.getCopiesAbove().toString());
        mReclaimedAbove.setText(stInfo.stats.getReclaimedAbove().toString());
        mLastTimestamp.setText(Constants.VIEW_FORMAT.format(stInfo.stats.getTimestamp()));
        mTotalReclaimed.setText(stInfo.stats.getReclaimedToday().toString());
        if(fav.getMaxBudgetCount() == 0) {
            mNeededPoints.setText("?");
            mNeededPoints.setTextColor(Color.RED);
            mNeededPoints.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.select_budget_count);
                    final NumberPicker np = new NumberPicker(getActivity());
                    np.setMinValue(0);
                    np.setMaxValue(500);
                    np.setValue(80);
                    builder.setView(np);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mBudgetCount = np.getValue();
                                final Favorite toPersist = createFavForStudent(mNameSelector.getSelectedItemPosition());
                                final StudentInfo stInfo = retrieveStatistics(toPersist, mStudents);
                                updateGrid(toPersist, stInfo);
                                if(mFavButton.isChecked()) // should update value in DB
                                    DatabaseFactory.getHelper().getFavoritesDao().createOrUpdate(toPersist);
                            } catch (ParseException | SQLException e) {
                                Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    builder.create().show();
                }
            });
        } else {
            mNeededPoints.setText(stInfo.stats.getNeededPoints().toString());
            mNeededPoints.setOnClickListener(null);
            mNeededPoints.setTextColor(Color.WHITE);
        }

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
            Log.e("DataShowFragment", "Error while inserting statistics!", e);
            Toast.makeText(getActivity(), R.string.cannot_update_statistics, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearGrid() {
        mListNumber.setText("");
        mPriority.setText("");
        mPoints.setText("");
        mOriginalsAbove.setText("");
        mCopiesAbove.setText("");
        mReclaimedAbove.setText("");
        mLastTimestamp.setText("");
        mTotalReclaimed.setText("");
        mNeededPoints.setText("");
    }

    private StudentInfo retrieveStatistics(Favorite fav, Elements data) throws ParseException {
        final StudentInfo result = new StudentInfo();

        final Statistics currentStatistics = new Statistics();
        currentStatistics.setParent(fav);

        final Element myRow = findRowWithName(data, fav.getName());
        final Elements myColumns = myRow.children();

        currentStatistics.setTotalSubmitted(data.size());
        currentStatistics.setTimestamp(new Date(mLastUpdated));

        final String myType = myColumns.get(6).text();

        int originalsAbove = 0;
        int copiesAbove = 0;
        int reclaimedAbove = 0;
        int totalReclaimed = 0;
        Integer maxBudget = fav.getMaxBudgetCount();
        final Queue<Integer> allPoints = new PriorityQueue<>(data.size(), Collections.reverseOrder());
        for(Element row : data) {
            final Elements columns = row.children();
            final int points = columns.get(5).text().isEmpty() ? 0 : Integer.valueOf(columns.get(5).text());
            final String type = columns.get(6).text();
            final boolean isOriginal = columns.get(8).text().equals("Да");
            final boolean isReclaimed = !columns.get(8).text().matches("Да|Нет");
            final boolean isCheater = type.equals("в/к") || type.equals("б/э");
            final boolean isBetter = points > fav.getPoints() || points == fav.getPoints() && data.indexOf(row) < data.indexOf(myRow);

            if(isReclaimed) { // забрал документы - не в счёт
                ++totalReclaimed;
                if(isBetter || isCheater)
                    ++reclaimedAbove;
                continue;
            }

            if(isCheater)
                --maxBudget;

            allPoints.offer(points);
            if (isCheater || isBetter) { // отнимаем от бюджетных мест
                if (isOriginal)
                    ++originalsAbove;
                else
                    ++copiesAbove;
            }
        }

        int neededPoints = 0;
        while (allPoints.size() > 0 && maxBudget > 0) {
            neededPoints = allPoints.poll();
            --maxBudget;
        }

        currentStatistics.setCopiesAbove(copiesAbove);
        currentStatistics.setOriginalsAbove(originalsAbove);
        currentStatistics.setNeededPoints(neededPoints);
        currentStatistics.setReclaimedAbove(reclaimedAbove);
        currentStatistics.setReclaimedToday(totalReclaimed);

        result.stats = currentStatistics;

        return result;
    }

    @Override
    public StudentInfo retrieveStatistics(Favorite fav, NetworkService.NetworkInfo data) throws ParseException {
        final Element tableBody = data.content.select("tbody").first();
        if (tableBody == null)
            return null;

        final StudentInfo stInfo = retrieveStatistics(fav, tableBody.children());
        stInfo.stats.setTimestamp(new Date(data.lastModified));

        return stInfo;
    }

    private class NameSelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final Element row = (Element) parent.getAdapter().getItem(position);
            mFavButton.setVisibility(row != null ? View.VISIBLE : View.INVISIBLE);
            mShowStatistics.setVisibility(row != null ? View.VISIBLE : View.INVISIBLE);
            if(row != null) {
                final Favorite toPersist = createFavForStudent(position);

                // update favorite button state
                try {
                    final Favorite inDb = DatabaseFactory.getHelper().getFavoritesDao().queryForSameId(toPersist);

                    mFavButton.setOnCheckedChangeListener(null);
                    if(inDb != null) { // уже присутствует в БД, помечаем выделенным
                        mFavButton.setChecked(true);
                        mBudgetCount = inDb.getMaxBudgetCount() != null ? inDb.getMaxBudgetCount() : 0;
                        toPersist.setMaxBudgetCount(mBudgetCount);
                    } else // иначе снимаем выделение
                        mFavButton.setChecked(false);
                    mFavButton.setOnCheckedChangeListener(mFavClickListener);
                } catch (SQLException e) {
                    Log.e("DataShowFragment", "Error retrieving favorite!", e);
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }

                // update grid
                try {
                    final StudentInfo stInfo = retrieveStatistics(toPersist, mStudents);
                    updateGrid(toPersist, stInfo);
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), R.string.wrong_data, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), R.string.wrong_page_format, Toast.LENGTH_SHORT).show();
                }
            } else
                clearGrid();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
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
        toCreate.setParentInstitution(SPBU.ordinal());
        toCreate.setName(extractNameForStudent(row));
        toCreate.setPriority(Integer.valueOf(columns.get(7).text()));
        toCreate.setPoints(columns.get(5).text().isEmpty() ? 0 : Integer.valueOf(columns.get(5).text()));
        toCreate.setMaxBudgetCount(mBudgetCount);

        return toCreate;
    }

    @Override
    protected String extractNameForStudent(Element row) {
        final Elements columns = row.children();
        return Utils.join(Arrays.asList(columns.get(2).text(), columns.get(3).text(), columns.get(4).text()), " ");
    }
}
