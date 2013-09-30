/*
 * Copyright (C) 2013 Light Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarTrafficAndCarrier extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarTrafficAndCarrier";

    private static final String PREF_TRAFFIC_ENABLE = "status_bar_show_traffic";
    private static final String PREF_TRAFFIC_COLOR = "traffic_color";
    private static final String PREF_CARRIER_ENABLE = "status_bar_show_carrier";
    private static final String PREF_CARRIER_COLOR = "carrier_color";

    private static final int MENU_RESET = Menu.FIRST;

    private CheckBoxPreference mTrafficEnable;
    private ColorPickerPreference mTrafficColor;
    private CheckBoxPreference mCarrierEnable;
    private ColorPickerPreference mCarrierColor;

    private boolean mCheckPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCustomView();
    }
    
    private PreferenceScreen createCustomView() {
        mCheckPreferences = false;
        PreferenceScreen prefSet = getPreferenceScreen();
        if (prefSet != null) {
            prefSet.removeAll();
        }

        addPreferencesFromResource(R.xml.status_bar_traffic_and_carrier);
        prefSet = getPreferenceScreen();
        
        mTrafficEnable = (CheckBoxPreference) findPreference(PREF_TRAFFIC_ENABLE);
        mTrafficEnable.setOnPreferenceChangeListener(this);
        mTrafficEnable.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_SHOW_TRAFFIC, 1) == 1);

        mTrafficColor = (ColorPickerPreference) findPreference(PREF_TRAFFIC_COLOR);
        mTrafficColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_COLOR, -2);
        if (intColor == -2) {
            intColor = getResources().getColor(
                    com.android.internal.R.color.holo_blue_light);
        } else {
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTrafficColor.setSummary(hexColor);
        }
        mTrafficColor.setNewPreviewColor(intColor);

        mCarrierEnable = (CheckBoxPreference) findPreference(PREF_CARRIER_ENABLE);
        mCarrierEnable.setOnPreferenceChangeListener(this);
        mCarrierEnable.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_SHOW_CARRIER, 1) == 1);

        mCarrierColor = (ColorPickerPreference) findPreference(PREF_CARRIER_COLOR);
        mCarrierColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR, -2);
        if (intColor == -2) {
            intColor = getResources().getColor(
                    com.android.internal.R.color.holo_blue_light);
        } else {
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mCarrierColor.setSummary(hexColor);
        }
        mCarrierColor.setNewPreviewColor(intColor);

        setHasOptionsMenu(true);
        mCheckPreferences = true;
        return prefSet;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.navbar_reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.status_bar_reset);
        alertDialog.setMessage(R.string.statusbar_traffic_and_carrier_reset);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_SHOW_TRAFFIC, 1);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_TRAFFIC_COLOR, -2);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_SHOW_CARRIER, 1);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_CARRIER_COLOR, -2);
                createCustomView();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }

        if (preference == mTrafficEnable) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TRAFFIC,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mTrafficColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_COLOR,
                    intHex);
            return true;
        } else if (preference == mCarrierEnable) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_CARRIER,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mCarrierColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR,
                    intHex);
            return true;
        }
        return false;
    }
}
