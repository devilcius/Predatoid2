package com.predatum.predatoid;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;


/**
 * Helper class for showing and canceling predatoid
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class PredatoidNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Predatoid";
    /**
     * Action name to close predatoid.
     */
    public static final String EXIT_PREDATOID_ACTION = "com.predatum.predatoid.EXIT_PREDATOID";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     *
     * @see #cancel(Context)
     */
    public void notify(final Context context,
                              final String credentialMessage, final int number) {
        final Resources res = context.getResources();
        final String title = res.getString(R.string.app_name);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_LIGHTS)

                        // Set required fields, including the small icon, the
                        // notification title, and text.
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(credentialMessage)

                        // All fields below this line are optional.

                        // Use a default priority (recognized on devices running Android
                        // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_LOW)

                        // Set ticker text (preview) information for this notification.
  //              .setTicker(credentialMessage)

                        // Show a number. This is useful when stacking notifications of
                        // a single type.
                .setNumber(number)

                        // Set the pending intent to be initiated when the user touches
                        // the notification.
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, SettingsActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(credentialMessage))
                .addAction(
                        R.drawable.ic_action_exit,
                        res.getString(R.string.action_exit),
                        PendingIntent.getService(
                                context,
                                1,
                                new Intent(context, MusicTrackerService.class)
                                        .setAction(EXIT_PREDATOID_ACTION),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )
                )
                                .addAction(
                                        R.drawable.ic_action_settings,
                                        res.getString(R.string.action_settings),
                                        PendingIntent.getActivity(
                                                context,
                                                0,
                                                Intent.createChooser(new Intent(context, SettingsActivity.class)
                                                        , "Settings"),
                                                PendingIntent.FLAG_UPDATE_CURRENT))
                                .setAutoCancel(false)
                                .setOngoing(true);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, int)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}