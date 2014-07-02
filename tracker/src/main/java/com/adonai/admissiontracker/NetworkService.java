package com.adonai.admissiontracker;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;
import com.adonai.admissiontracker.entities.Statistics;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class NetworkService extends Service implements Handler.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String PREF_AUTOUPDATE = "auto.update.key";
    private final static long UPDATE_INTERVAL = 300000; // 5 минут

    private Handler mNetworkHandler;
    private HttpClient mClient = new HttpClient();
    private SharedPreferences mPreferences;

    private boolean isPeriodicCheckEnabled;

    public class ServiceRetriever extends Binder {

        public NetworkService getService() {
            return NetworkService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceRetriever();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseFactory.setHelper(this);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        final HandlerThread thr = new HandlerThread("ConnectionService");
        thr.start();
        mNetworkHandler = new Handler(thr.getLooper(), this);

        isPeriodicCheckEnabled = mPreferences.getBoolean(PREF_AUTOUPDATE, false);
        if(isPeriodicCheckEnabled)
            mNetworkHandler.sendEmptyMessageDelayed(Constants.UPDATE_FAVS, UPDATE_INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DatabaseFactory.releaseHelper();
        mNetworkHandler.getLooper().quit();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.GET_URL:
                final Pair<String, Handler> args = (Pair<String, Handler>) msg.obj;
                try {
                    final String pageData = mClient.getPageAndContextAsString(args.first);
                    final Document result = Jsoup.parse(pageData);
                    args.second.sendMessage(args.second.obtainMessage(Constants.GET_URL, new NetworkInfo(result, mClient.getCurrentURL(), mClient.getLastModified())));
                } catch (IOException e) {
                    args.second.sendMessage(args.second.obtainMessage(Constants.NETWORK_ERROR, R.string.network_error, 0, null));
                }
                break;
            case Constants.UPDATE_FAVS:
                try {
                    final List<Favorite> storedFavs = DatabaseFactory.getHelper().getFavoritesDao().queryForAll();
                    for(final Favorite curFav : storedFavs) {
                        final String pageData = mClient.getPageAndContextAsString(curFav.getUrl());
                        final Document page = Jsoup.parse(pageData);

                        final Constants.University institution = Constants.University.values()[curFav.getParentInstitution()];
                        final DataRetriever statRetriever = DataRetrieverFactory.newInstance(institution);
                        final Statistics stats = statRetriever.retrieveStatistics(curFav, page).stats;

                        final QueryBuilder<Statistics, Integer> qb = DatabaseFactory.getHelper().getStatDao().queryBuilder();
                        qb.where().eq("parent_id", curFav).and().eq("timestamp", stats.getTimestamp());
                        if (qb.queryForFirst() == null) // we haven't this row in DB
                            DatabaseFactory.getHelper().getStatDao().create(stats);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        return true; // we should handle all
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_AUTOUPDATE:
                mNetworkHandler.removeMessages(Constants.UPDATE_FAVS);
                isPeriodicCheckEnabled = sharedPreferences.getBoolean(key, false);
                if(isPeriodicCheckEnabled)
                    mNetworkHandler.sendEmptyMessageDelayed(Constants.UPDATE_FAVS, UPDATE_INTERVAL);
                break;
        }
    }

    public static class NetworkInfo {
        public Document content;
        public String fullURL;
        public long lastModified;

        public NetworkInfo(Document content, String fullURL, long lastModified) {
            this.content = content;
            this.fullURL = fullURL;
            this.lastModified = lastModified;
        }
    }

    public void retrievePage(String url, Handler callback) {
        mNetworkHandler.sendMessage(mNetworkHandler.obtainMessage(Constants.GET_URL, Pair.create(url, callback)));
    }
}
