package com.adonai.admissiontracker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;

public class NetworkService extends Service implements Handler.Callback {

    private Handler mNetworkHandler;

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

    public void retrievePage(URL url, Handler callback) {
        mNetworkHandler.sendMessage(mNetworkHandler.obtainMessage(Opcodes.GET_URL, Pair.create(url, callback)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Opcodes.GET_URL:
                Pair<URL, Handler> args = (Pair<URL, Handler>) msg.obj;
                try {
                    final Document result = Jsoup.parse(args.first.openStream(), "windows-1251", args.first.toString());
                    args.second.sendMessage(args.second.obtainMessage(Opcodes.GET_URL, result));
                } catch (IOException e) {
                    args.second.sendMessage(args.second.obtainMessage(Opcodes.NETWORK_ERROR, R.string.network_error, 0, null));
                }
                break;
            default:
                break;
        }

        return true; // we should handle all
    }
}
