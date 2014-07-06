package com.adonai.admissiontracker;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
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
import java.util.Date;

import static com.adonai.admissiontracker.Constants.University.SPBU;

/**
 * Created by adonai on 27.06.14.
 */
public class ShowSpbuDataFragment extends AbstractShowDataFragment implements DataRetriever {

    private static final String TITLE_KEY = "page.title";       // MANDATORY
    private static final String URL_KEY = "page.url";           // MANDATORY
    private static final String NAME_KEY = "favorite.name";

    private long mLastUpdated;

    private Elements mStudents = null;

    private Spinner mNameSelector;
    private ToggleButton mFavButton;
    private DoubleTextView mListNumber, mAdmissionDate, mPoints, mOriginalsAbove, mCopiesAbove, mReclaimedAbove;
    private DoubleTextView mLastTimestamp, mTotalReclaimed, mNeededPoints;

    private NameSelectorListener mNameSelectorListener = new NameSelectorListener();
    private FavoriteClickListener mFavClickListener = new FavoriteClickListener();

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
        mFavButton = (ToggleButton) rootView.findViewById(R.id.favorite_button);
        mFavButton.setOnCheckedChangeListener(mFavClickListener);

        mListNumber = (DoubleTextView) rootView.findViewById(R.id.list_number);
        mAdmissionDate = (DoubleTextView) rootView.findViewById(R.id.date);
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
                    returnToSelections();
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
        mAdmissionDate.setText(SPBU.getTimeFormat().format(stInfo.admissionDate));
        //mPoints.setText();
        //mOriginalsAbove.setText();
        //mCopiesAbove.setText();
        //mReclaimedAbove.setText();
        mLastTimestamp.setText(Constants.VIEW_FORMAT.format(stInfo.stats.getTimestamp()));
        //mTotalReclaimed.setText();
        //mNeededPoints.setText();

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
            Toast.makeText(getActivity(), R.string.cannot_update_statistics, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearGrid() {
        mListNumber.setText("");
        mAdmissionDate.setText("");
        //mPoints.setText();
        //mOriginalsAbove.setText();
        //mCopiesAbove.setText();
        //mReclaimedAbove.setText();
        mLastTimestamp.setText("");
        //mTotalReclaimed.setText();
        //mNeededPoints.setText();
    }

    private void returnToSelections() {
        getFragmentManager()
            .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, new SelectorFragment())
            .commit();
    }

    private StudentInfo retrieveStatistics(Favorite fav, Elements data) throws ParseException {
        final StudentInfo result = new StudentInfo();

        final Statistics currentStatistics = new Statistics();
        currentStatistics.setParent(fav);

        final Element myRow = findRowWithName(data, fav.getName());
        final Elements myColumns = myRow.children();
        final Date currentAdmissionDate = SPBU.getTimeFormat().parse(myColumns.get(5).text());

        currentStatistics.setTotalSubmitted(data.size());

        result.stats = currentStatistics;
        result.admissionDate = currentAdmissionDate;

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

    @Override
    public BaseFragment getFragment() {
        return this;
    }

    @Override
    public boolean canTrackTime() {
        return true;
    }

    private class NameSelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final Element row = (Element) parent.getAdapter().getItem(position);
            mFavButton.setVisibility(row != null ? View.VISIBLE : View.INVISIBLE);
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
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }

            } else
                clearGrid();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class FavoriteClickListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked)
                try {
                    DatabaseFactory.getHelper().getFavoritesDao().createOrUpdate(createFavForStudent(mNameSelector.getSelectedItemPosition()));
                    Toast.makeText(getActivity(), R.string.added_to_favs, Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }
            else
                try {
                    DatabaseFactory.getHelper().getFavoritesDao().delete(createFavForStudent(mNameSelector.getSelectedItemPosition()));
                    Toast.makeText(getActivity(), R.string.removed_from_favs, Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * Creates favorite to store in DB for selected index
     * @param index index beginning from 1
     * @return favorite instance for selected student
     */
    private Favorite createFavForStudent(int index) {
        if(index <= 0 || index > mStudents.size())
            throw new IllegalArgumentException("Unknown student index!");

        final Favorite toCreate = new Favorite(getArguments().getString(TITLE_KEY), getArguments().getString(URL_KEY));
        toCreate.setParentInstitution(SPBU.ordinal());
        toCreate.setName(extractNameForStudent(mStudents.get(index - 1)));

        return toCreate;
    }

    @Override
    protected String extractNameForStudent(Element row) {
        final Elements columns = row.children();
        return Utils.join(Arrays.asList(columns.get(1).text(), columns.get(2).text(), columns.get(3).text()), " ");
    }
}
