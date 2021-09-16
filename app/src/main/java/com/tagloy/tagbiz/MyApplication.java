package com.tagloy.tagbiz;

import android.app.Application;

import com.github.anrwatchdog.ANRWatchDog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


public class MyApplication extends Application {
    public FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        new ANRWatchDog(25000).setReportMainThreadOnly().start();
        //Firebase Crashlytics configuration
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
}
