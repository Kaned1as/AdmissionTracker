package com.adonai.admissiontracker;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by adonai on 27.06.14.
 */
public abstract class BaseFragment extends Fragment implements Handler.Callback {

    protected Handler mHandler;
    protected ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler(this);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.wait_please);
        mProgressDialog.setMessage(getString(R.string.retrieving_data));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(mProgressDialog.isShowing())
            mProgressDialog.hide();

        switch (msg.what) {
            case Constants.NETWORK_ERROR:
                Toast.makeText(getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    public static class WithZeroAdapter<T> extends ArrayAdapter<T> {

        public WithZeroAdapter(Context context, List<T> objects) {
            super(context, R.layout.tall_list_item, objects);
        }

        @Override
        public T getItem(int position) {
            if(position == 0)
                return null;
            else
                return super.getItem(position - 1);
        }

        @Override
        public int getCount() {
            return super.getCount() + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return newView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return newView(position, convertView, parent);
        }

        public View newView(int position, View convertView, ViewGroup parent) {
            final View view;
            final TextView text;
            final T item = getItem(position);

            if (convertView == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.tall_list_item, parent, false);
            else
                view = convertView;

            text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item == null ? getContext().getString(R.string.select_from_list) : item.toString());

            return view;
        }
    }

    public MainFlowActivity getMainActivity() {
        return (MainFlowActivity) getActivity();
    }
}
