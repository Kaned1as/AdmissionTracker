package com.adonai.admissiontracker;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

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
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(mProgressDialog.isShowing())
            mProgressDialog.hide();

        return true;
    }

    public MainFlowActivity getMainActivity() {
        return (MainFlowActivity) getActivity();
    }
}
