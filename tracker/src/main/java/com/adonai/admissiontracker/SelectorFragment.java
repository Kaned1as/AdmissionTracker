package com.adonai.admissiontracker;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SelectorFragment extends BaseFragment {

    private Spinner mInstSelector;
    private Spinner mFavSelector;
    private LinearLayout mSpinnersHolder;

    private int mSelectedInstitution = 0;

    private AdapterView.OnItemSelectedListener mInstSelectListener = new InstitutionSelectorListener();
    private AdapterView.OnItemSelectedListener mFavSelectListener = new FavoriteSelectorListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.institution_selector_fragment, container, false);

        mInstSelector = (Spinner) rootView.findViewById(R.id.institution_spinner);
        mInstSelector.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.tall_list_item, getResources().getStringArray(R.array.institutions_array)));
        mInstSelector.setSelection(1); // Пока у нас всего один институт
        mInstSelector.setOnItemSelectedListener(mInstSelectListener);

        mFavSelector = (Spinner) rootView.findViewById(R.id.favorites_spinner);
        mFavSelector.setOnItemSelectedListener(mFavSelectListener);

        mSpinnersHolder = (LinearLayout) rootView.findViewById(R.id.spinners_container);

        return rootView;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.GET_URL: // got our URL back
                final NetworkService.NetworkInfo ni = (NetworkService.NetworkInfo) msg.obj;
                updateLayouts(ni.content);
                break;
        }

        return super.handleMessage(msg);
    }

    private void updateLayouts(Document doc) {
        if(mSelectedInstitution == 0) // should never happen
            return;

        try {
            final List<Favorite> favsForSelectedInst = DatabaseFactory.getHelper().getFavoritesDao().queryForEq("parentInstitution", mSelectedInstitution);
            if(!favsForSelectedInst.isEmpty()) {
                mFavSelector.setVisibility(View.VISIBLE);
                final FavoriteElementAdapter favAdapter = new FavoriteElementAdapter(getActivity(), favsForSelectedInst);
                mFavSelector.setAdapter(favAdapter);
            } else
                mFavSelector.setVisibility(View.INVISIBLE);
        } catch (SQLException e) {
            Toast.makeText(getActivity(), R.string.database_error, Toast.LENGTH_SHORT).show();
        }
        switch (mSelectedInstitution) {
            case 1: // spbu
                setLayoutSpbu(doc.select(".treeview > ul").first());
                break;
            default:
                break;
        }
    }

    private void setLayoutSpbu(final Element tree) {
        // ul --> [li --> div, ul]
        final Elements childListItems = tree.children(); // list of `li`s
        if(tree.children().select("ul").size() == 0) { // this is final subtree
            final Elements divs = tree.select("div");
            for(final Element div : divs) {
                final TextView link = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.link_item, mSpinnersHolder, false);
                final String url = div.select("a[href]").attr("href");
                link.setText(div.text());
                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Favorite holder = new Favorite(div.text(), url);
                        holder.setParentInstitution(mInstSelector.getSelectedItemPosition());

                        getFragmentManager()
                            .beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .replace(R.id.container, ShowSpbuDataFragment.forData(holder))
                            .commit();
                    }
                });
                mSpinnersHolder.addView(link);
            }
            return;
        }

        final Spinner levelSelector = new Spinner(getActivity());
        final SpbuElementAdapter elementAdapter = new SpbuElementAdapter(getActivity(), childListItems);
        levelSelector.setAdapter(elementAdapter);
        levelSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // delete all following views
                final int indexToStartDeletion = mSpinnersHolder.indexOfChild(levelSelector) + 1;
                mSpinnersHolder.removeViews(indexToStartDeletion, mSpinnersHolder.getChildCount() - indexToStartDeletion);

                final Element selected = elementAdapter.getItem(position);
                if(selected != null)
                    setLayoutSpbu(selected.child(1)); // this will be ul
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mSpinnersHolder.addView(levelSelector);
    }

    private class InstitutionSelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSpinnersHolder.removeAllViews();
            mSelectedInstitution = position;
            switch (position) {
                case 0: // nothing selected
                    break;
                case 1: // spbu
                    mProgressDialog.show();
                    getMainActivity().getService().retrievePage(Constants.SPBU, mHandler);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class FavoriteSelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final Favorite selectedFav = (Favorite) mFavSelector.getAdapter().getItem(position);
            if(selectedFav == null) // prompt selected
                return;

            switch (mSelectedInstitution) {
                case 1: // SPBU

                    getFragmentManager()
                        .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, ShowSpbuDataFragment.forData(selectedFav))
                        .commit();
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class SpbuElementAdapter extends WithZeroAdapter<Element> {

        public SpbuElementAdapter(Context context, Elements objects) {
            super(context, objects);
        }

        @Override
        public View newView(int position, View convertView, ViewGroup parent) {
            final View view;
            final TextView text;
            final Element item = getItem(position);

            if (convertView == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.tall_list_item, parent, false);
            else
                view = convertView;

            text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item == null ? getContext().getString(R.string.select_from_list) : item.child(0).text().substring(1));

            return view;
        }
    }

    private class FavoriteElementAdapter extends WithZeroAdapter<Favorite> {

        public FavoriteElementAdapter(Context context, List<Favorite> objects) {
            super(context, objects);
        }

        @Override
        public View newView(int position, View convertView, ViewGroup parent) {
            final View view;
            final TextView text;
            final Favorite item = getItem(position);

            if (convertView == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.tall_list_item, parent, false);
            else
                view = convertView;

            text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item == null ? getContext().getString(R.string.select_from_favs) : item.getTitle());

            return view;
        }
    }
}
