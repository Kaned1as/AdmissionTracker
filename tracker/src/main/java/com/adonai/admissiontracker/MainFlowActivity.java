package com.adonai.admissiontracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;


public class MainFlowActivity extends Activity {

    private NetworkService mService;
    private final Object mServiceWaiter = new Object();

    private Intent mServiceCaller;
    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((NetworkService.ServiceRetriever) service).getService();
            synchronized (mServiceWaiter) {
                mServiceWaiter.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mServiceCaller = new Intent(this, NetworkService.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flow);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new SelectorFragment()).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(mServiceCaller, mServiceConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_flow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Blocks till the service is retrieved
     * @return service bound to this activity
     */
    public NetworkService getService() {
        return mService;
    }
}
