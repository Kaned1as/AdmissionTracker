package com.adonai.admissiontracker;

import android.app.FragmentTransaction;
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

import com.adonai.admissiontracker.entities.Favorite;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A placeholder fragment containing a simple view.
 */
public class SelectorFragment extends BaseFragment {

    private Spinner mInstSelector;
    private LinearLayout mSpinnersHolder;

    private int mSelectedInstitution = 0;

    private AdapterView.OnItemSelectedListener mInstSelectListener = new InstitutionSelectorListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.institution_selector_fragment, container, false);

        mInstSelector = (Spinner) rootView.findViewById(R.id.institution_spinner);
        mInstSelector.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.tall_list_item, getResources().getStringArray(R.array.institutions_array)));
        mInstSelector.setOnItemSelectedListener(mInstSelectListener);

        mSpinnersHolder = (LinearLayout) rootView.findViewById(R.id.spinners_container);

        return rootView;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.GET_URL: // got our URL back
                updateLayouts((Document) msg.obj);
                break;
        }

        return super.handleMessage(msg);
    }

    private void updateLayouts(Document doc) {
        if(mSelectedInstitution == 0)
            return;

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
                        getFragmentManager()
                            .beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .replace(R.id.container, ShowDataFragment.forData(holder))
                            .commit();
                    }
                });
                mSpinnersHolder.addView(link);
            }
            return;
        }

        final Spinner levelSelector = new Spinner(getActivity());
        final WithZeroAdapter elementAdapter = new WithZeroAdapter(getActivity(), childListItems);
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
}
