package com.predatum.predatoid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by marcos on 3/13/15.
 */
public class CurrentMusicTrackInfoActivity extends Activity {

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    final String TAG = "MusicRetriever";
    ContentResolver mContentResolver;
    List<Item> mItems = new ArrayList<Item>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("com.predatum.predatoid", "activity created!!");
        super.onCreate(savedInstanceState);

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

        mContentResolver = this.getContentResolver();


/*
        Set<String> extrasList = this.getIntent().getExtras().keySet();

        for (String str : extrasList) {
            Log.d("extra: ", str);
        }
*/

        registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(this.getClass().getName(), "broadcast received!!!!");

            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            String track = intent.getStringExtra("track");
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");

            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Log.i(TAG, "Querying media...");
            Log.i(TAG, "URI: " + uri.toString());
            // Perform a query on the content resolver. The URI we're passing specifies that we
// want to query for all audio media on external storage (e.g. SD card)
            Cursor cur = mContentResolver.query(uri, null,
                    MediaStore.Audio.Media.IS_MUSIC + " = 1 AND " + MediaStore.Audio.Media.ARTIST + " = '" + artist + "'"
                    , null, null);
            Log.i(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
            if (cur == null) {
// Query failed...
                Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
                return;
            }
            if (!cur.moveToFirst()) {
// Nothing to query. There is no music on the device. How boring.
                Log.e(TAG, "Failed to move cursor to first row (no query results).");
                return;
            }
            Log.i(TAG, "Listing...");
            // retrieve the indices of the columns where the ID, title, etc. of the song are
            int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
            // add each song to mItems
            do {
                Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
                mItems.add(new Item(
                        cur.getLong(idColumn),
                        cur.getString(artistColumn),
                        cur.getString(titleColumn),
                        cur.getString(albumColumn),
                        cur.getLong(durationColumn)));
            } while (cur.moveToNext());
            Log.i(TAG, "Done querying media. MusicRetriever is ready.");



// retrieve the indices of the columns where the ID, title, etc. of the song are
            Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
            Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));



            String filename = intent.getStringExtra("filename");
            String albumId = intent.getStringExtra("albumId");
            String year = intent.getStringExtra(MediaStore.Audio.AudioColumns.YEAR);
            String size = intent.getStringExtra(MediaStore.Audio.AudioColumns.SIZE);

            //en action metachanged recoge duration
            Long duration = intent.getLongExtra(MediaStore.Audio.AudioColumns.DURATION, 0);
            Toast.makeText(CurrentMusicTrackInfoActivity.this, track, Toast.LENGTH_SHORT).show();

        }
    };

    public static class Item {
        long id;
        String artist;
        String title;
        String album;
        long duration;
        public Item(long id, String artist, String title, String album, long duration) {
            this.id = id;
            this.artist = artist;
            this.title = title;
            this.album = album;
            this.duration = duration;
        }
        public long getId() {
            return id;
        }
        public String getArtist() {
            return artist;
        }
        public String getTitle() {
            return title;
        }
        public String getAlbum() {
            return album;
        }
        public long getDuration() {
            return duration;
        }
        public Uri getURI() {
            return ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        }
    }

}
