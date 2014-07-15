package com.adonai.admissiontracker;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;

import java.sql.SQLException;

import static com.adonai.admissiontracker.Constants.*;


public class MainFlowActivity extends Activity {

    private NetworkService mService;

    private Intent mServiceCaller;
    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((NetworkService.ServiceRetriever) service).getService();
            handleIntentIfExists();
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

        startService(mServiceCaller); // чтобы сервис не умирал после закрытия UI
        final boolean result = bindService(mServiceCaller, mServiceConn, 0); // change to BIND_IMPORTANT
        if(!result)
            throw new RuntimeException("Unable to connect to service!");

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new SelectorFragment()).commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConn);
    }

    private void handleIntentIfExists() {
        if(getIntent().hasExtra(FAVORITE_EXTRA)) {
            final String favId = getIntent().getStringExtra(FAVORITE_EXTRA);
            try {
                final Favorite fav = DatabaseFactory.getHelper().getFavoritesDao().queryForId(favId);
                if(fav != null) {
                    final University univ = University.values()[fav.getParentInstitution()];
                    getFragmentManager()
                        .beginTransaction()
                            .addToBackStack(String.format("Showing%sDataFragment", univ.toString()))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new DataRetrieverFactory(univ).forFavorite(fav))
                        .commit();
                }
            } catch (SQLException e) {
                Log.e("MainFlow", String.format("Error retrieving favorite for notification click! Favorite is %s", favId), e);
            } finally {
                getIntent().removeExtra(FAVORITE_EXTRA);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if(mService != null)
            handleIntentIfExists();
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
            startActivity(new Intent(this, AdmissionPreferenceActivity.class));
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

    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
