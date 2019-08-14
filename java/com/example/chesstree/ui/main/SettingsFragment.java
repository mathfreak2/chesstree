package com.example.chesstree.ui.main;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.chesstree.R;


public class SettingsFragment extends PreferenceFragmentCompat {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences, s);
    }
}
