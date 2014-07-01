package com.adonai.admissiontracker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Pair;

import com.adonai.admissiontracker.entities.Favorite;
import com.adonai.admissiontracker.entities.Statistics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static com.adonai.admissiontracker.Constants.Universities.SPBU;

public class NetworkService extends Service implements Handler.Callback {

    private Handler mNetworkHandler;
    private HttpClient mClient = new HttpClient();

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

        final HandlerThread thr = new HandlerThread("ConnectionService");
        thr.start();
        mNetworkHandler = new Handler(thr.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            default:
                break;
        }

        return true; // we should handle all
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
