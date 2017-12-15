/*
 * Copyright (C) 2017 Benzo Rom
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
package com.benzo.settings;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.TrafficStats;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.benzo.settings.preference.CustomSeekBarPreference;
import com.benzo.settings.preference.SystemSettingSwitchPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusbarSettings extends SettingsPreferenceFragment implements
	OnPreferenceChangeListener  {

    private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
    private static final String NETWORK_TRAFFIC_HIDEARROW = "network_traffic_hidearrow";
    private static final String NETWORK_TRAFFIC_AUTOHIDE = "network_traffic_autohide";
    private static final String NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold";
    private static final String SYSTEMUI_THEME_STYLE = "systemui_theme_style";

    private CustomSeekBarPreference mNetTrafficAutohideThreshold;
    private SystemSettingSwitchPreference mNetMonitor;
    private SystemSettingSwitchPreference mNetTrafficAutohide;
    private SystemSettingSwitchPreference mNetTrafficHidearrow;
    private ListPreference mSystemUIThemeStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_settings);
        ContentResolver resolver = getActivity().getContentResolver();

        boolean isNetMonitorEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 0, UserHandle.USER_CURRENT) == 1;
        mNetMonitor = (SystemSettingSwitchPreference) findPreference(NETWORK_TRAFFIC_STATE);
        mNetMonitor.setChecked(isNetMonitorEnabled);
        mNetMonitor.setOnPreferenceChangeListener(this);

        mNetTrafficHidearrow =
            (SystemSettingSwitchPreference) findPreference(NETWORK_TRAFFIC_HIDEARROW);
        mNetTrafficHidearrow.setChecked((Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_HIDEARROW, 0) == 1));
        mNetTrafficHidearrow.setOnPreferenceChangeListener(this);

        mNetTrafficAutohide =
            (SystemSettingSwitchPreference) findPreference(NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficAutohide.setChecked((Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE, 0) == 1));
        mNetTrafficAutohide.setOnPreferenceChangeListener(this);

        mNetTrafficAutohideThreshold = (CustomSeekBarPreference) findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
        int netTrafficAutohideThreshold = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 10);
        mNetTrafficAutohideThreshold.setValue(netTrafficAutohideThreshold);
        mNetTrafficAutohideThreshold.setOnPreferenceChangeListener(this);

	// SystemUI Theme
        mSystemUIThemeStyle = (ListPreference) findPreference(SYSTEMUI_THEME_STYLE);
        int systemUIThemeStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.SYSTEM_UI_THEME, 0);
        int valueIndex = mSystemUIThemeStyle.findIndexOfValue(String.valueOf(systemUIThemeStyle));
        mSystemUIThemeStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mSystemUIThemeStyle.setSummary(mSystemUIThemeStyle.getEntry());
        mSystemUIThemeStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BENZO;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateNetworkTrafficState(boolean value) {
        if (!value) {
            mNetTrafficHidearrow.setEnabled(false);
            mNetTrafficAutohide.setEnabled(false);
            mNetTrafficAutohideThreshold.setEnabled(false);
        } else {
            mNetTrafficHidearrow.setEnabled(true);
            mNetTrafficAutohide.setEnabled(true);
            mNetTrafficAutohideThreshold.setEnabled(true);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetMonitor) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            updateNetworkTrafficState(value);
            return true;
        } else if (preference == mNetTrafficHidearrow) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_HIDEARROW, value ? 1 : 0);
            return true;
        } else if (preference == mNetTrafficAutohide) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE, value ? 1 : 0);
            return true;
        } else if (preference == mNetTrafficAutohideThreshold) {
            int threshold = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, threshold * 1);
            return true;
        } else if (preference == mSystemUIThemeStyle) {
            String value = (String) newValue;
            Settings.System.putInt(getContentResolver(), Settings.System.SYSTEM_UI_THEME, Integer.valueOf(value));
            int valueIndex = mSystemUIThemeStyle.findIndexOfValue(value);
            mSystemUIThemeStyle.setSummary(mSystemUIThemeStyle.getEntries()[valueIndex]);
        }
        return false;
    }
}
