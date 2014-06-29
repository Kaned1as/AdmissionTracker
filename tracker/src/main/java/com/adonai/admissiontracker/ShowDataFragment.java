package com.adonai.admissiontracker;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by adonai on 27.06.14.
 */
public class ShowDataFragment extends BaseFragment {

    private Favorite mFavorite;
    private Elements mStudents = null;

    private Spinner mNameSelector;
    private ToggleButton mFavButton;

    private NameSelectorListener mNameSelectorListener = new NameSelectorListener();
    private FavoriteClickListener mFavClickListener = new FavoriteClickListener();

    public static ShowDataFragment forData(Favorite data) {
        final ShowDataFragment result = new ShowDataFragment();
        result.mFavorite = data;
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.show_data_fragment, container, false);

        mNameSelector = (Spinner) rootView.findViewById(R.id.name_spinner);
        mNameSelector.setOnItemSelectedListener(mNameSelectorListener);
        mFavButton = (ToggleButton) rootView.findViewById(R.id.favorite_button);
        mFavButton.setOnCheckedChangeListener(mFavClickListener);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mStudents == null) {
            mProgressDialog.show();
            getMainActivity().getService().retrievePage(mFavorite.getUrl(), mHandler);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.GET_URL: // got our URL back
                final Document page = (Document) msg.obj;
                final Element tableBody = page.select("tbody").first();
                if (tableBody == null) {
                    Toast.makeText(getActivity(), R.string.no_data_available, Toast.LENGTH_SHORT).show();
                    returnToSelections();
                } else
                    updateNames(tableBody);

                break;
        }

        return super.handleMessage(msg);
    }

    private void updateNames(Element tableBody) {
        mStudents = tableBody.children();
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

    private static class NamesAdapter extends WithZeroAdapter {

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
            if(row == null)
                text.setText(R.string.select_from_list);
            else {
                final Elements columns = row.children();
                text.setText(Utils.join(Arrays.asList(columns.get(1).text(), columns.get(2).text(), columns.get(3).text()), " "));
            }

            return view;
        }
    }

    private class NameSelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final Element row = (Element) parent.getAdapter().getItem(position);
            mFavButton.setVisibility(row != null ? View.VISIBLE : View.INVISIBLE);
            if(row != null) {
                final TextView text = (TextView) view.findViewById(android.R.id.text1);
                mFavorite.setName(text.getText().toString());
                try {
                    final Favorite inDb = DatabaseFactory.getHelper().getFavoritesDao().queryForSameId(mFavorite);
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
                    DatabaseFactory.getHelper().getFavoritesDao().createOrUpdate(mFavorite);
                    Toast.makeText(getActivity(), R.string.added_to_favs, Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }
            else
                try {
                    DatabaseFactory.getHelper().getFavoritesDao().delete(mFavorite);
                    Toast.makeText(getActivity(), R.string.removed_from_favs, Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }
        }
    }
}
