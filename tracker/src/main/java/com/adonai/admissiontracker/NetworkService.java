package com.adonai.admissiontracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.widget.RemoteViews;

import com.adonai.admissiontracker.database.DatabaseFactory;
import com.adonai.admissiontracker.entities.Favorite;
import com.adonai.admissiontracker.entities.Statistics;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NetworkService extends Service implements Handler.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String PREF_AUTOUPDATE = "auto.update.key";
    public final static String PREF_CLICKTIME = "last.click.time";

    private final static long UPDATE_INTERVAL = 300000; // 5 минут
    private final static long DROP_MARK_INTERVAL = 86400000; // 1 день

    private static final int NEWS_NOTIFICATION_ID = 17002;

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
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NEWS_NOTIFICATION_ID);
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
                    args.second.sendMessage(args.second.obtainMessage(Constants.GET_URL, new NetworkInfo(result, mClient.getLastModified())));
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
                        stats.setTimestamp(new Date(mClient.getLastModified()));

                        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        final QueryBuilder<Statistics, Integer> qb = DatabaseFactory.getHelper().getStatDao().queryBuilder();
                        qb.where().eq("parent_id", curFav).and().eq("timestamp", stats.getTimestamp());
                        if (qb.queryForFirst() == null) { // we haven't this row in DB
                            DatabaseFactory.getHelper().getStatDao().create(stats); // сохраняем текущую статистику в БД

                            final Notification toShow = createNotification(Constants.VIEW_FORMAT.format(stats.getTimestamp()));
                            nm.notify(NEWS_NOTIFICATION_ID, toShow); // запускаем уведомление
                        }
                    }
                    mNetworkHandler.sendEmptyMessageDelayed(Constants.UPDATE_FAVS, UPDATE_INTERVAL);

                    if(!getPackageName().endsWith(".pro")) { // это обычное приложение, сбрасываем статус
                        long lastClicked = mPreferences.getLong(PREF_CLICKTIME, 0l);
                        if(System.currentTimeMillis() > lastClicked + DROP_MARK_INTERVAL)
                            mPreferences.edit().putBoolean(PREF_AUTOUPDATE, false).apply();
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
        public long lastModified;

        public NetworkInfo(Document content, long lastModified) {
            this.content = content;
            this.lastModified = lastModified;
        }
    }

    public void retrievePage(String url, Handler callback) {
        mNetworkHandler.sendMessage(mNetworkHandler.obtainMessage(Constants.GET_URL, Pair.create(url, callback)));
    }

    // Создаем уведомление в статусной строке - для принудительно живого сервиса в Foreground-режиме
    private Notification createNotification(String title)
    {
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification);
        views.setTextViewText(R.id.notification_title, getString(R.string.updates_present));
        views.setTextViewText(R.id.notification_text, title);

        final Notification notification = new Notification();
        notification.contentView = views;
        notification.icon = R.drawable.ic_launcher; // иконка
        notification.ledOnMS = 1000;
        notification.ledOffMS = 10000;
        notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.ledARGB = Color.parseColor("#FFD8BD");
        notification.tickerText = getString(R.string.updates_present);
        notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;

        final Intent intent = new Intent(this, MainFlowActivity.class); // при клике на уведомление открываем приложение
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        notification.contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        return notification;
    }
}
