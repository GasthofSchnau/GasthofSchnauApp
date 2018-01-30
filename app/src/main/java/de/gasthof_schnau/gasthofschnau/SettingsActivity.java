package de.gasthof_schnau.gasthofschnau;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_KEY_SYNC_NEWS = "pref_key_sync_aktuell";
    public static final String PREF_KEY_SYNC_EVENTS = "pref_key_sync_events";
    public static final String PREF_KEY_SYNC_SPEISEKARTE = "pref_key_sync_speisekarte";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

}
