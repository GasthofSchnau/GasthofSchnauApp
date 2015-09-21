package de.gasthof_schnau.gasthofschnau;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import de.gasthof_schnau.gasthofschnau.lib.Util;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final Preference resetTutorial = findPreference(getString(R.string.pref_key_tutorial_reset));
        resetTutorial.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Util.makeToast(getActivity(), "Yo Hauke, hier musst du noch was machen" /* TODO */);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putBoolean("pref_key_tutorial_activated", true);
                return true;
            }
        });
    }


}
