package com.adonai.admissiontracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by adonai on 02.07.14.
 */
public class CheckProPreference extends CheckBoxPreference {

    public CheckProPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CheckProPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckProPreference(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onClick() {
    }

    private void init() {
        setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(isChecked()) // делать нечего, всё честно
                    CheckProPreference.super.onClick();
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.but_excuse);
                    builder.setMessage(R.string.only_pro);
                    //builder.setPositiveButton(R.string.click_ad, new MobClixOnClickListener());
                    //builder.setPositiveButton(R.string.click_ad, new MobClixOnClickListener());
                    builder.setNeutralButton(R.string.buy_pro, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=com.adonai.admissiontracker.pro"));
                            getContext().startActivity(intent);
                        }
                    });
                    builder.create().show();
                }
                return true;
            }
        });
    }

    private class MobClixOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final AlertDialog.Builder adBuilder = new AlertDialog.Builder(getContext());
            final AdView adView = new AdView(getContext());
            adView.setAdUnitId("ca-app-pub-2426537600463724/5449840696");
            adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adBuilder.setView(adView);

            /*AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Эмулятор
                .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4") // Тестовый телефон Galaxy Nexus
                .build();*/

            final Dialog advertiseDialog = adBuilder.create();
            adView.pause();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    if (!CheckProPreference.this.isChecked())
                        CheckProPreference.super.onClick();
                    advertiseDialog.dismiss();
                }

            });
            advertiseDialog.show();
        }
    }

    /*
    private class MobClixOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final AlertDialog.Builder adBuilder = new AlertDialog.Builder(getContext());
            final MobclixIABRectangleMAdView adView = new MobclixIABRectangleMAdView(getContext());
            adBuilder.setView(adView);
            final Dialog advertiseDialog = adBuilder.create();
            advertiseDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    adView.pause();
                }
            });
            advertiseDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    adView.pause();
                }
            });
            adView.addMobclixAdViewListener(new MobclixAdViewListener() {

                @Override
                public void onSuccessfulLoad(MobclixAdView mobclixAdView) {

                }

                @Override
                public void onFailedLoad(MobclixAdView mobclixAdView, int i) {

                }

                @Override
                public void onAdClick(MobclixAdView mobclixAdView) {
                    if (!CheckProPreference.this.isChecked())
                        CheckProPreference.super.onClick();

                    // extend clicktime for a day
                    getEditor().putLong(NetworkService.PREF_CLICKTIME, System.currentTimeMillis()).apply();
                    advertiseDialog.dismiss();
                }

                @Override
                public boolean onOpenAllocationLoad(MobclixAdView mobclixAdView, int i) {
                    return false;
                }

                @Override
                public void onCustomAdTouchThrough(MobclixAdView mobclixAdView, String s) {

                }

                @Override
                public String keywords() {
                    return null;
                }

                @Override
                public String query() {
                    return null;
                }
            });
            advertiseDialog.show();
        }
    }
    */
}
