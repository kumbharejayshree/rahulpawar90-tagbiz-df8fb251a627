package com.tagloy.tagbiz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tagloy.tagbiz.utils.BackgroundClass;

public class ConnectionReceiver extends BroadcastReceiver {
    BackgroundClass backgroundClass;

    @Override
    public void onReceive(Context context, Intent intent) {
        backgroundClass = new BackgroundClass(context);
        if (intent == null || intent.getExtras() == null)
            return;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED){
//            backgroundClass.showMessage("Network connected!");
        }else {
            backgroundClass.showMessage("You are offline! Please check network connection.");
        }
    }
}
