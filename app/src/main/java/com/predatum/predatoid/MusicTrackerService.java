package com.predatum.predatoid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.predatum.predatoid.audio.SongExtraInfo;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class MusicTrackerService extends Service {

    private String username;
    private String password;
    private ContentResolver contentResolver;
    private final String SUPPORTED_AUDIO_FILE_TYPE = "MP3";

    @Override
    public void onCreate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = prefs.getString(SettingsActivity.PREDATUM_USERNAME_KEY, "");
        password = prefs.getString(SettingsActivity.PREDATUM_PASSWORD_KEY, "");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isNetworkConnected())
        {
            return START_FLAG_RETRY;
        }
        String notificationMessage;
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.missing_credentials,
                    Toast.LENGTH_SHORT).show();
            notificationMessage = "Missing credentials. Update your settings.";
        } else {
            this.connectToPredatum();
            notificationMessage = "You're connected to predatum as " + username;
        }
        PredatoidNotification notification = new PredatoidNotification();
        notification.notify(this, notificationMessage, 2);
        this.startTracking();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    private void connectToPredatum() {
        try {
            Predatum.getInstance().checkPredatumConnection(this.username, this.password, getApplicationContext());
        } catch (JSONException error) {
            Log.e(getClass().getSimpleName(), error.toString());
        }
    }

    private void startTracking() {

        contentResolver = this.getContentResolver();
        registerReceiver(receiver, getIntentFilter());
    }
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            String track = intent.getStringExtra("track");
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");

            String queryString = MediaStore.Audio.Media.IS_MUSIC + " = 1" +
                    " AND " + MediaStore.Audio.Media.ARTIST + " = ?" +
                    " AND " + MediaStore.Audio.Media.ALBUM + " = ?" +
                    " AND " + MediaStore.Audio.Media.TITLE + " = ?";
            String[] queryStringArgs = {artist, album, track};
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cur = contentResolver.query(uri, null, queryString, queryStringArgs, null);
            if (cur == null) { //query failed
                Log.e(getClass().getSimpleName(), "Failed to retrieve music: cursor is null :-(");
                return;
            }
            if (!cur.moveToFirst()) { //nothing to query
                Log.e(getClass().getSimpleName(), "Failed to move cursor to first row (no query results).");
                return;
            }
            // retrieve the indices of the columns where the ID, title, etc. of the song are
            int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int filePathColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
            int yearColumn = cur.getColumnIndex(MediaStore.Audio.Media.YEAR);
            int sizeColumn = cur.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int trackColumn = cur.getColumnIndex(MediaStore.Audio.Media.TRACK);
            HashMap<String, Object> songToPost = new HashMap<>();

            // only one result needed
            String filePath = cur.getString(filePathColumn);
            songToPost.put("artist", cur.getString(artistColumn));
            songToPost.put("title", cur.getString(titleColumn));
            songToPost.put("album", cur.getString(albumColumn));
            songToPost.put("duration", (cur.getInt(durationColumn)) / 1000);
            songToPost.put("year", cur.getInt(yearColumn));
            songToPost.put("file_size", cur.getInt(sizeColumn));

            File audioFile = new File(filePath);
            songToPost.put("file_type", SUPPORTED_AUDIO_FILE_TYPE);
            Log.i(getClass().getSimpleName(), "Done querying media. MusicRetriever is ready.");
            Date fileDate = new Date(audioFile.lastModified());
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SongExtraInfo songExtraInfo = new SongExtraInfo(audioFile);
            songToPost.put("folder_path", audioFile.getParent());
            songToPost.put("file_date", formatter.format(fileDate));
            songToPost.put("genre", songExtraInfo.getSongGenre());
            songToPost.put("is_lame_encoded", songExtraInfo.isLameEncoded());
            songToPost.put("quality", songExtraInfo.getLamePreset());
            songToPost.put("bitrate", songExtraInfo.getBitrate());
            songToPost.put("start_time", formatter.format(new Date()));
            songToPost.put("file_name", audioFile.getName());
            songToPost.put("track", songExtraInfo.getTrackNumber());
            //TODO: set action according to state
            songToPost.put("action", "play");
            cur.close();

            try {
                Predatum.getInstance().updateNowPlaying(songToPost, getApplicationContext());
            } catch (IOException error) {
                Log.d(getClass().getSimpleName(), error.toString());
            } catch (JSONException error) {
                Log.d(getClass().getSimpleName(), error.toString());
            }
        }
    };

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.music.metachanged");
        intentFilter.addAction("com.htc.music.metachanged");
        intentFilter.addAction("fm.last.android.metachanged");
        intentFilter.addAction("com.sec.android.app.music.metachanged");
        intentFilter.addAction("com.nullsoft.winamp.metachanged");
        intentFilter.addAction("com.amazon.mp3.metachanged");
        intentFilter.addAction("com.miui.player.metachanged");
        intentFilter.addAction("com.real.IMP.metachanged");
        intentFilter.addAction("com.sonyericsson.music.metachanged");
        intentFilter.addAction("com.rdio.android.metachanged");
        intentFilter.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        intentFilter.addAction("com.andrew.apollo.metachanged");
        intentFilter.addAction("com.android.music.metachanged");
        intentFilter.addAction("com.android.music.playstatechanged");
        intentFilter.addAction("com.android.music.playbackcomplete");
        intentFilter.addAction("com.android.music.queuechanged");
        intentFilter.addAction("com.htc.music.playstatechanged");
        intentFilter.addAction("com.htc.music.playbackcomplete");
        intentFilter.addAction("com.htc.music.queuechanged");
        intentFilter.addAction("com.android.music.musicservicecommand");
        intentFilter.addAction("com.htc.music.musicservicecommand");

        return intentFilter;
    }
}
