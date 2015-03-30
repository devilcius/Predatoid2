package com.predatum.predatoid;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONException;

public class MusicTrackerService extends Service {
    public MusicTrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());;
        String username = prefs.getString("predatum_username", "");
        String password = prefs.getString("predatum_password", "");
        String notificationMessage = "";
        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.missing_credentials,
                    Toast.LENGTH_SHORT).show();
            notificationMessage = "Missing credentials. Update your settings.";
        } else {
            try {
                Predatum.getInstance().authenticateToPredatum(username, password, getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            notificationMessage = "You're logged in predatum as " + username;
        }
        PredatoidNotification notification = new PredatoidNotification();
        notification.notify(this, notificationMessage, 2);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
