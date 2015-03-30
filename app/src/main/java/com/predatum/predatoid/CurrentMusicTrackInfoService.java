package com.predatum.predatoid;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CurrentMusicTrackInfoService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String CHECK_LOGGED_IN_ACTION = "com.predatum.predatoid.action.CHECK_LOGGED_IN";
    private static final String UPDATE_PREDATUM_ACTION = "com.predatum.predatoid.action.UPDATE_PREDATUM";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.predatum.predatoid.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.predatum.predatoid.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void updatePredatum(Context context, String param1, String param2) {
        Intent intent = new Intent(context, CurrentMusicTrackInfoService.class);
        intent.setAction(CHECK_LOGGED_IN_ACTION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void checkUserLoggedIn(Context context, String param1, String param2) {
        Intent intent = new Intent(context, CurrentMusicTrackInfoService.class);
        intent.setAction(UPDATE_PREDATUM_ACTION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public CurrentMusicTrackInfoService() {
        super("CurrentMusicTrackInfoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String notificationMessage = "";
        if (intent != null) {
            final String action = intent.getAction();
            if (CHECK_LOGGED_IN_ACTION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleCheckLoggedIn(param1, param2);
            } else if (UPDATE_PREDATUM_ACTION.equals(action)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());;
                String username = prefs.getString("predatum_username", "");
                String password = prefs.getString("predatum_password", "");
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

                handleUpdatePredatum(notificationMessage);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleCheckLoggedIn(String param1, String param2) {
        //TODO: check if user is logged in
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleUpdatePredatum(String message) {
        PredatoidNotification notification = new PredatoidNotification();
        notification.notify(this, message, 2);
    }
}
