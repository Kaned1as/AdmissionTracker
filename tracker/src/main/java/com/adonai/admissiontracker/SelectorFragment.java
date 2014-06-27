package com.adonai.admissiontracker;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.nodes.Document;

/**
 * A placeholder fragment containing a simple view.
 */
public class SelectorFragment extends BaseFragment {

    private Spinner mInstSelector;
    private LinearLayout mSpinnersHolder;

    private int mSelectedInstitution = 0;
    private Document retrievedData = null;

    private AdapterView.OnItemSelectedListener mInstSelectListener = new SelectorListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.institution_selector_fragment, container, false);

        mInstSelector = (Spinner) rootView.findViewById(R.id.institution_spinner);
        mInstSelector.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.institutions_array)));
        mInstSelector.setOnItemSelectedListener(mInstSelectListener);

        mSpinnersHolder = (LinearLayout) rootView.findViewById(R.id.spinners_container);

        return rootView;
    }

    private class SelectorListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSpinnersHolder.removeAllViews();
            mSelectedInstitution = position;
            switch (position) {
                case 0: // nothing selected
                    break;
                case 1: // spbu
                    mProgressDialog.show();
                    getMainActivity().getService().retrievePage(PageAddresses.SPBU, mHandler);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Opcodes.GET_URL: // got our URL back
                retrievedData = (Document) msg.obj;
                updateLayouts();
                break;
            case Opcodes.NETWORK_ERROR:
                Toast.makeText(getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return super.handleMessage(msg);
    }

    private void updateLayouts() {
        if(retrievedData == null || mSelectedInstitution == 0)
            return;

        switch (mSelectedInstitution) {
            case 1: // spbu
                setLayoutSpbu();
                break;
            default:
                break;
        }
    }

    private void setLayoutSpbu() {

    }
}
