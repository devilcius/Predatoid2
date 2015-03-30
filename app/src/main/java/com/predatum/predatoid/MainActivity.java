package com.predatum.predatoid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent musicTrackerService = new Intent(this, MusicTrackerService.class);
        musicTrackerService.setAction("com.predatum.predatoid.action.UPDATE_PREDATUM");
        startService(musicTrackerService);
    }

/*    @Override
    protected void onStart() {
        finish();
    }*/
}
