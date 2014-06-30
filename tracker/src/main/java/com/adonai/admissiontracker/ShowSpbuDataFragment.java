package com.adonai.admissiontracker;

import android.app.FragmentTransaction;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by adonai on 27.06.14.
 */
public class ShowSpbuDataFragment extends BaseFragment {

    private static final String TITLE_KEY = "page.title";       // MANDATORY
    private static final String URL_KEY = "page.url";           // MANDATORY
    private static final String INST_KEY = "university.index";  // MANDATORY

    private static final String NUM_KEY = "favorite.number";

    private long mLastUpdated;

    private Elements mStudents = null;

    private Spinner mNameSelector;
    private ToggleButton mFavButton;

    private NameSelectorListener mNameSelectorListener = new NameSelectorListener();
    private FavoriteClickListener mFavClickListener = new FavoriteClickListener();

    public static ShowSpbuDataFragment forFavorite(Favorite data) {
        final ShowSpbuDataFragment result = new ShowSpbuDataFragment();
        final Bundle args = new Bundle();
        args.putString(TITLE_KEY, data.getTitleRaw());
        args.putString(URL_KEY, data.getUrl());
        args.putInt(INST_KEY, data.getParentInstitution());
        args.putInt(NUM_KEY, data.getNumber());
        result.setArguments(args);
        return result;
    }

    public static ShowSpbuDataFragment forPage(Constants.Universities inst, String title, String url) {
        final ShowSpbuDataFragment result = new ShowSpbuDataFragment();
        final Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(URL_KEY, url);
        args.putInt(INST_KEY, inst.ordinal());
        result.setArguments(args);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.show_data_fragment, container, false);

        mNameSelector = (Spinner) rootView.findViewById(R.id.name_spinner);
        mNameSelector.setOnItemSelectedListener(mNameSelectorListener);
        mFavButton = (ToggleButton) rootView.findViewById(R.id.favorite_button);
        mFavButton.setOnCheckedChangeListener(mFavClickListener);

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
                    if(getArguments().containsKey(NUM_KEY)) { // it's favorite from DB
                        mNameSelector.setSelection(getArguments().getInt(NUM_KEY), true);
                        getArguments().remove(NUM_KEY);
                    }
                } else
                    Toast.makeText(getActivity(), R.string.no_updates_available, Toast.LENGTH_SHORT).show();
                break;
        }

        return super.handleMessage(msg);
    }

    private void updateNames() {
        final SpinnerAdapter nameAdapter = new NamesAdapter(getActivity(), mStudents);
        mNameSelector.setAdapter(nameAdapter);
    }

    private void updateGrid() {

    }

    private void returnToSelections() {
        getFragmentManager()
            .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, new SelectorFragment())
            .commit();
    }

    private static class NamesAdapter extends WithZeroAdapter<Element> {

        public NamesAdapter(Context context, Elements objects) {
            super(context, objects);
        }

        @Override
        public View newView(int position, View convertView, ViewGroup parent) {
            final View view;
            final TextView text;
            final Element row = getItem(position);


            if (convertView == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.tall_list_item, parent, false);
            else
                view = convertView;

            text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(row == null ? getContext().getString(R.string.select_from_list) : extractNameForStudent(row));

            return view;
        }
    }

    private class NameSelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final Element row = (Element) parent.getAdapter().getItem(position);
            mFavButton.setVisibility(row != null ? View.VISIBLE : View.INVISIBLE);
            if(row != null) {
                // update grid
                getMainActivity().getService().retrieveStatistics(mStudents, mNameSelector.getSelectedItemPosition(), mHandler);

                // update favorite button state
                final Favorite toPersist = createFavForStudent(position);
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

            }
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
        toCreate.setParentInstitution(getArguments().getInt(INST_KEY));
        toCreate.setNumber(index);
        toCreate.setName(extractNameForStudent(mStudents.get(index - 1)));

        return toCreate;
    }

    private static String extractNameForStudent(Element row) {
        final Elements columns = row.children();
        return Utils.join(Arrays.asList(columns.get(1).text(), columns.get(2).text(), columns.get(3).text()), " ");
    }
}