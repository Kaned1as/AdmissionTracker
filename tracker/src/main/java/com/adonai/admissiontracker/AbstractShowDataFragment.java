package com.adonai.admissiontracker;

import android.app.FragmentTransaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;
import com.adonai.admissiontracker.entities.Statistics;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.text.ParseException;

/**
 * Created by adonai on 05.07.14.
 */
public abstract class AbstractShowDataFragment extends BaseFragment implements DataRetriever {

    protected Button mShowStatistics;
    protected Spinner mNameSelector;

    protected class NamesAdapter extends WithZeroAdapter<Element> {

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
            text.setText(row == null ? getContext().getString(R.string.select_from_list) : position + ". " + extractNameForStudent(row));

            return view;
        }
    }

    protected class FavoriteClickListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked)
                try {
                    DatabaseFactory.getHelper().getFavoritesDao().createOrUpdate(createFavForStudent(mNameSelector.getSelectedItemPosition()));
                    Toast.makeText(getActivity(), R.string.added_to_favs, Toast.LENGTH_SHORT).show();
                    mShowStatistics.setVisibility(View.VISIBLE);
                } catch (SQLException e) {
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }
            else
                try {
                    DatabaseFactory.getHelper().getFavoritesDao().delete(createFavForStudent(mNameSelector.getSelectedItemPosition()));
                    Toast.makeText(getActivity(), R.string.removed_from_favs, Toast.LENGTH_SHORT).show();
                    mShowStatistics.setVisibility(View.INVISIBLE);
                } catch (SQLException e) {
                    Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
                }
        }
    }

    protected class ShowStatisticsClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                final Favorite selected = createFavForStudent(mNameSelector.getSelectedItemPosition());
                final Favorite inDb = DatabaseFactory.getHelper().getFavoritesDao().queryForSameId(selected);
                if(inDb == null)
                    throw new SQLException("Not found!");

                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack("ShowingStatisticsFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .hide(AbstractShowDataFragment.this)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .add(R.id.container, StatisticsFragment.forFavorite(selected))
                        .commit();
            } catch (SQLException e) {
                Toast.makeText(getActivity(), R.string.favorite_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected Element findRowWithName(Elements table, String name) throws ParseException {
        for(Element row : table)
            if(extractNameForStudent(row).equals(name))
                return row;

        throw new ParseException(getString(R.string.name_not_found), table.size());
    }

    @Override
    public boolean isUpdate(Statistics newStat) {
        try {
            final QueryBuilder<Statistics, Integer> qb = DatabaseFactory.getHelper().getStatDao().queryBuilder();
            final Statistics last = qb.orderBy("timestamp", false).queryForFirst();
            return last != null && !newStat.contentEquals(last);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected abstract String extractNameForStudent(Element row);

    protected abstract Favorite createFavForStudent(int index);
}
