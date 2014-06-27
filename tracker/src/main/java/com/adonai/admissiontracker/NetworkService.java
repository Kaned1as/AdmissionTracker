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

import java.io.IOException;
import java.net.URL;

public class NetworkService extends Service implements Handler.Callback {

    private Handler mHandler;

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
        mHandler = new Handler(thr.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.getLooper().quit();
    }

    public void retrievePage(URL url, Handler callback) {
        mHandler.sendMessage(mHandler.obtainMessage(Opcodes.GET_URL, Pair.create(url, callback)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Opcodes.GET_URL:
                Pair<URL, Handler> args = (Pair<URL, Handler>) msg.obj;
                try {
                    args.second.sendMessage(args.second.obtainMessage(Opcodes.GET_URL, Jsoup.parse(args.first, 10000)));
                } catch (IOException e) {
                    args.second.sendMessage(args.second.obtainMessage(Opcodes.NETWORK_ERROR, R.string.network_error));
                }
                break;
            default:
                break;
        }

        return true; // we should handle all
    }
}
