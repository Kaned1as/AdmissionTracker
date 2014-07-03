package com.adonai.admissiontracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixAdViewListener;
import com.mobclix.android.sdk.MobclixIABRectangleMAdView;

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
                    builder.setPositiveButton(R.string.click_ad, new DialogInterface.OnClickListener() {
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
                    });
                    builder.create().show();
                }
                return true;
            }
        });
    }
}
