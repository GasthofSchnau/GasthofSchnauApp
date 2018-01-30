package de.gasthof_schnau.gasthofschnau;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference.OnPreferenceChangeListener onChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary("Momentan gesetzt auf: " + newValue.toString());
                return true;
            }
        };

        Preference aktuellSyncPreference = findPreference("pref_key_sync_aktuell");
        aktuellSyncPreference.setSummary("Momentan gesetzt auf: " + ((ListPreference) aktuellSyncPreference).getEntry());
        aktuellSyncPreference.setOnPreferenceChangeListener(onChangeListener);

        Preference eventsSyncPreference = findPreference("pref_key_sync_events");
        eventsSyncPreference.setSummary("Momentan gesetzt auf: " + ((ListPreference) eventsSyncPreference).getEntry());
        eventsSyncPreference.setOnPreferenceChangeListener(onChangeListener);

        Preference speisekarteSyncPreference = findPreference("pref_key_sync_speisekarte");
        speisekarteSyncPreference.setSummary("Momentan gesetzt auf: " + ((ListPreference) speisekarteSyncPreference).getEntry());
        speisekarteSyncPreference.setOnPreferenceChangeListener(onChangeListener);

    }



}
