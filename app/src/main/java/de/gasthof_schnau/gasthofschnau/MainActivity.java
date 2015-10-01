package de.gasthof_schnau.gasthofschnau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.gasthof_schnau.gasthofschnau.lib.TabManager;
import de.gasthof_schnau.gasthofschnau.lib.Util;
import de.gasthof_schnau.gasthofschnau.tab_fragments.ContactFragment;
import de.gasthof_schnau.gasthofschnau.tab_fragments.EventsFragment;
import de.gasthof_schnau.gasthofschnau.tab_fragments.NewsFragment;
import de.gasthof_schnau.gasthofschnau.tab_fragments.SpeisekarteFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final TabManager tabManager = new TabManager(this);
        tabManager.addTab(getString(R.string.tab_title_news), new NewsFragment());
        tabManager.addTab(getString(R.string.tab_title_events), new EventsFragment());
        tabManager.addTab(getString(R.string.tab_title_speisekarte), new SpeisekarteFragment());
        tabManager.addTab(getString(R.string.tab_title_contact), new ContactFragment());
        tabManager.init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}

